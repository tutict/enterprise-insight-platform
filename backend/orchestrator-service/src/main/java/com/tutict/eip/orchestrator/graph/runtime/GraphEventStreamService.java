package com.tutict.eip.orchestrator.graph.runtime;

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
public class GraphEventStreamService {

    private static final long EMITTER_TIMEOUT_MILLIS = 30L * 60L * 1000L;
    private static final long RECONNECT_TIME_MILLIS = 2_000L;

    private final ConcurrentMap<String, GraphStream> streams = new ConcurrentHashMap<>();

    public String createRun(String requestedRunId) {
        String runId = requestedRunId == null || requestedRunId.isBlank()
                ? UUID.randomUUID().toString()
                : requestedRunId;
        streams.compute(runId, (id, existing) -> existing == null || existing.completed.get()
                ? new GraphStream(id)
                : existing);
        return runId;
    }

    public SseEmitter connect(String runId, String lastEventId) {
        GraphStream stream = streams.computeIfAbsent(runId, GraphStream::new);
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

    public void emit(GraphEvent event) {
        GraphStream stream = streams.computeIfAbsent(event.getRunId(), GraphStream::new);
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

    private void replay(GraphStream stream, SseEmitter emitter, String lastEventId) {
        long lastSeen = parseLastEventId(lastEventId);
        List<GraphEvent> replayEvents = stream.events.stream()
                .filter(event -> parseLastEventId(event.getEventId()) > lastSeen)
                .toList();

        for (GraphEvent event : replayEvents) {
            send(stream, emitter, event);
        }

        if (stream.completed.get()) {
            emitter.complete();
            stream.emitters.remove(emitter);
        }
    }

    private void send(GraphStream stream, SseEmitter emitter, GraphEvent event) {
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
        return "GRAPH_RUN_COMPLETED".equals(type) || "GRAPH_RUN_FAILED".equals(type);
    }

    private static final class GraphStream {
        private final AtomicLong sequence = new AtomicLong();
        private final CopyOnWriteArrayList<GraphEvent> events = new CopyOnWriteArrayList<>();
        private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
        private final AtomicBoolean completed = new AtomicBoolean(false);

        private GraphStream(String runId) {
        }
    }
}
