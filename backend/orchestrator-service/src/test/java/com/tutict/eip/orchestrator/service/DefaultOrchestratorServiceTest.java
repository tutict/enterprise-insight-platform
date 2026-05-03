package com.tutict.eip.orchestrator.service;

import com.tutict.eip.agentadapter.domain.AutoRepairGenerationRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.service.AutoRepairGenerationService;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.harnesscompiler.service.CompileService;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultOrchestratorServiceTest {

    @Test
    void shouldCompilePromptBeforeAgentExecution() {
        CompileService compileService = new CompileService(
                requirement -> new DslModel(
                        "demo",
                        "spring-boot-backend",
                        requirement,
                        List.of("api", "service"),
                        new LinkedHashMap<>(),
                        "Return files"
                ),
                dsl -> "compiled prompt for " + dsl.getRequirement()
        );
        AtomicReference<AutoRepairGenerationRequest> capturedGenerationRequest = new AtomicReference<>();
        AutoRepairGenerationService generationService = request -> {
            capturedGenerationRequest.set(request);
            return new AutoRepairGenerationResponse(true, "VERIFIED", "agent-output/demo", 1, "files", null, List.of());
        };
        DefaultOrchestratorService service = new DefaultOrchestratorService(compileService, generationService);
        OrchestratorRunRequest request = new OrchestratorRunRequest();
        request.setRequirement("Build a demo service");
        request.setModel("llama3.1");
        request.setTargetDirectory("demo");
        request.setVerifyCommands(List.of(List.of("mvn", "test")));

        OrchestratorRunResponse response = service.run(request);

        assertThat(response.getRunId()).isNotBlank();
        assertThat(response.getDsl().getRequirement()).isEqualTo("Build a demo service");
        assertThat(response.getHarnessPrompt()).isEqualTo("compiled prompt for Build a demo service");
        assertThat(response.getGeneration().getStatus()).isEqualTo("VERIFIED");
        assertThat(capturedGenerationRequest.get().getPrompt()).isEqualTo(response.getHarnessPrompt());
        assertThat(capturedGenerationRequest.get().getTargetDirectory()).isEqualTo("demo");
    }
}
