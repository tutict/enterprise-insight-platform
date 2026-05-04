package com.tutict.eip.orchestrator.runtime;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RunEventStreamService {

    private static final long EMITTER_TIMEOUT_MILLIS = 30L * 60L * 1000L;
    private static final long RECONNECT_TIME_MILLIS = 2_000L;

    private final ConcurrentMap<String, RunStream> streams = new ConcurrentHashMap<>();

    public String createRun(String requestedRunId) {
        String runId = requestedRunId == null || requestedRunId.isBlank()
                ? UUID.randomUUID().toString()
                : requestedRunId;
        streams.computeIfAbsent(runId, RunStream::new);
        return runId;
    }

    public SseEmitter connect(String runId, String lastEventId) {
        RunStream stream = streams.computeIfAbsent(runId, RunStream::new);
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);
        stream.emitters.add(emitter);

        emitter.onCompletion(() -> stream.emitters.remove(emitter));
        emitter.onTimeout(() -> {
            stream.emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> stream.emitters.remove(emitter));

        replay(stream, emitter, lastEventId);
        return emitter;
    }

    public void emit(RunEvent event) {
        RunStream stream = streams.computeIfAbsent(event.getRunId(), RunStream::new);
        if (stream.completed.get()) {
            return;
        }
        event.setEventId(String.valueOf(stream.sequence.incrementAndGet()));
        stream.events.add(event);

        for (SseEmitter emitter : stream.emitters) {
            send(stream, emitter, event);
        }

        if (isTerminal(event.getType())) {
            stream.completed.set(true);
            for (SseEmitter emitter : stream.emitters) {
                emitter.complete();
            }
            stream.emitters.clear();
        }
    }

    public void pause(String runId) {
        RunStream stream = streams.computeIfAbsent(runId, RunStream::new);
        stream.paused.set(true);
        emit(RunEvent.of(runId, "RUN_PAUSED"));
    }

    public void resume(String runId) {
        RunStream stream = streams.computeIfAbsent(runId, RunStream::new);
        stream.paused.set(false);
        emit(RunEvent.of(runId, "RUN_RESUMED"));
    }

    public void cancel(String runId) {
        RunStream stream = streams.computeIfAbsent(runId, RunStream::new);
        stream.cancelled.set(true);
        emit(RunEvent.of(runId, "RUN_CANCELLED"));
    }

    public void retryStep(String runId, String step) {
        emit(RunEvent.of(runId, "STEP_RETRY_REQUESTED", step));
    }

    public boolean isCancelled(String runId) {
        RunStream stream = streams.get(runId);
        return stream != null && stream.cancelled.get();
    }

    public void awaitIfPaused(String runId) {
        RunStream stream = streams.get(runId);
        if (stream == null) {
            return;
        }

        while (stream.paused.get() && !stream.cancelled.get()) {
            try {
                Thread.sleep(250L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void replay(RunStream stream, SseEmitter emitter, String lastEventId) {
        long lastSeen = parseLastEventId(lastEventId);
        List<RunEvent> replayEvents = stream.events.stream()
                .filter(event -> parseLastEventId(event.getEventId()) > lastSeen)
                .toList();

        for (RunEvent event : replayEvents) {
            send(stream, emitter, event);
        }

        if (stream.completed.get()) {
            emitter.complete();
            stream.emitters.remove(emitter);
        }
    }

    private void send(RunStream stream, SseEmitter emitter, RunEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .id(event.getEventId())
                    .name(event.getType())
                    .reconnectTime(RECONNECT_TIME_MILLIS)
                    .data(event));
        } catch (IOException | IllegalStateException ex) {
            stream.emitters.remove(emitter);
            emitter.completeWithError(ex);
        }
    }

    private long parseLastEventId(String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(lastEventId);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private boolean isTerminal(String type) {
        return "RUN_COMPLETED".equals(type) || "RUN_FAILED".equals(type) || "RUN_CANCELLED".equals(type);
    }

    private static final class RunStream {
        private final AtomicLong sequence = new AtomicLong();
        private final CopyOnWriteArrayList<RunEvent> events = new CopyOnWriteArrayList<>();
        private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
        private final AtomicBoolean paused = new AtomicBoolean(false);
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicBoolean completed = new AtomicBoolean(false);

        private RunStream(String runId) {
        }
    }
}
