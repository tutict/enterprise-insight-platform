package com.tutict.eip.orchestrator.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import com.tutict.eip.orchestrator.runtime.RunEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryRunStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void persistsRequestEventsAndTerminalResponse() {
        DeliveryRunStore store = new DeliveryRunStore(objectMapper(), properties());
        OrchestratorRunRequest request = new OrchestratorRunRequest();
        request.setRunId("delivery-1");
        request.setRequirement("Build an API integration prototype");
        request.setTargetDirectory("generated-fde-delivery");
        request.setModel("llama3.1");
        request.setVerifyCommands(List.of(List.of("mvn", "test")));

        store.create("delivery-1", request);
        store.appendEvent(RunEvent.of("delivery-1", "RUN_REQUESTED", null, Map.of("config", request)));
        store.appendEvent(RunEvent.of("delivery-1", "RUN_COMPLETED", null, Map.of("result", response())));

        DeliveryRunRecord record = store.find("delivery-1").orElseThrow();

        assertThat(record.getRunId()).isEqualTo("delivery-1");
        assertThat(record.getStatus()).isEqualTo(DeliveryRunStatus.COMPLETED);
        assertThat(record.getRequest().getRequirement()).isEqualTo("Build an API integration prototype");
        assertThat(record.getEvents()).extracting(RunEvent::getType).containsExactly("RUN_REQUESTED", "RUN_COMPLETED");
        assertThat(record.getResponse().getGeneration().getStatus()).isEqualTo("VERIFIED");
        assertThat(store.list()).extracting(DeliveryRunRecord::getRunId).containsExactly("delivery-1");
    }

    private DeliveryRunStoreProperties properties() {
        DeliveryRunStoreProperties properties = new DeliveryRunStoreProperties();
        properties.setStorageRoot(tempDir.toString());
        return properties;
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private OrchestratorRunResponse response() {
        DslModel dsl = new DslModel(
                "delivery-api-prototype",
                "spring-boot-backend",
                "Build an API integration prototype",
                List.of("api", "service"),
                new LinkedHashMap<>(),
                "Return files"
        );
        AutoRepairGenerationResponse generation = new AutoRepairGenerationResponse(
                true,
                "VERIFIED",
                "generated-fde-delivery",
                1,
                "files",
                null,
                List.of()
        );
        return new OrchestratorRunResponse("delivery-1", dsl, "compiled prompt", generation, Instant.now());
    }
}
