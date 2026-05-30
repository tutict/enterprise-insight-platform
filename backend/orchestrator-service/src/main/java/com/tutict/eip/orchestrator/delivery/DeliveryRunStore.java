package com.tutict.eip.orchestrator.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import com.tutict.eip.orchestrator.runtime.RunEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DeliveryRunStore {

    private final ObjectMapper objectMapper;
    private final Path storageRoot;
    private final int maxListSize;
    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    public DeliveryRunStore(ObjectMapper objectMapper, DeliveryRunStoreProperties properties) {
        this.objectMapper = objectMapper;
        this.storageRoot = Path.of(properties.getStorageRoot()).normalize();
        this.maxListSize = properties.getMaxListSize();
    }

    public DeliveryRunRecord create(String runId, OrchestratorRunRequest request) {
        DeliveryRunRecord record = new DeliveryRunRecord();
        Instant now = Instant.now();
        record.setRunId(runId);
        record.setRequest(request);
        record.setStatus(DeliveryRunStatus.REQUESTED);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        save(record);
        return record;
    }

    public void appendEvent(RunEvent event) {
        if (event == null || event.getRunId() == null || event.getRunId().isBlank()) {
            return;
        }

        mutate(event.getRunId(), record -> {
            record.getEvents().add(event);
            record.setStatus(statusForEvent(event.getType(), record.getStatus()));
            if ("RUN_COMPLETED".equals(event.getType()) || "RUN_FAILED".equals(event.getType())) {
                record.setResponse(extractResponse(event));
            }
            record.setUpdatedAt(Instant.now());
            return record;
        });
    }

    public Optional<DeliveryRunRecord> find(String runId) {
        Path path = pathFor(runId);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(read(path));
    }

    public List<DeliveryRunRecord> list() {
        if (!Files.exists(storageRoot)) {
            return List.of();
        }

        try (var stream = Files.list(storageRoot)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(this::read)
                    .sorted(Comparator.comparing(
                            DeliveryRunRecord::getUpdatedAt,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ).reversed())
                    .limit(maxListSize)
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to list delivery runs", ex);
        }
    }

    private DeliveryRunRecord mutate(String runId, DeliveryRunMutation mutation) {
        synchronized (locks.computeIfAbsent(runId, ignored -> new Object())) {
            DeliveryRunRecord record = find(runId).orElseGet(() -> {
                DeliveryRunRecord next = new DeliveryRunRecord();
                Instant now = Instant.now();
                next.setRunId(runId);
                next.setCreatedAt(now);
                next.setUpdatedAt(now);
                return next;
            });
            DeliveryRunRecord updated = mutation.apply(record);
            save(updated);
            return updated;
        }
    }

    private void save(DeliveryRunRecord record) {
        try {
            Files.createDirectories(storageRoot);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(pathFor(record.getRunId()).toFile(), record);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist delivery run " + record.getRunId(), ex);
        }
    }

    private DeliveryRunRecord read(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), DeliveryRunRecord.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read delivery run " + path.getFileName(), ex);
        }
    }

    private Path pathFor(String runId) {
        String safeRunId = runId.replaceAll("[^a-zA-Z0-9._-]", "_");
        return storageRoot.resolve(safeRunId + ".json");
    }

    private DeliveryRunStatus statusForEvent(String eventType, DeliveryRunStatus current) {
        return switch (eventType) {
            case "RUN_REQUESTED" -> DeliveryRunStatus.RUNNING;
            case "RUN_COMPLETED" -> DeliveryRunStatus.COMPLETED;
            case "RUN_FAILED" -> DeliveryRunStatus.FAILED;
            case "RUN_CANCELLED" -> DeliveryRunStatus.CANCELLED;
            default -> current == DeliveryRunStatus.REQUESTED ? DeliveryRunStatus.RUNNING : current;
        };
    }

    private OrchestratorRunResponse extractResponse(RunEvent event) {
        Object result = event.getPayload() == null ? null : event.getPayload().get("result");
        if (result == null) {
            return null;
        }
        return objectMapper.convertValue(result, OrchestratorRunResponse.class);
    }

    @FunctionalInterface
    private interface DeliveryRunMutation {
        DeliveryRunRecord apply(DeliveryRunRecord record);
    }
}
