package com.tutict.eip.orchestrator.integration;

import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.agentadapter.service.AgentAdapter;
import com.tutict.eip.agentadapter.service.DefaultAutoRepairGenerationService;
import com.tutict.eip.agentadapter.storage.MarkerProjectFileWriter;
import com.tutict.eip.agentadapter.verify.ProjectVerifier;
import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.harnesscompiler.service.CompileService;
import com.tutict.eip.harnesscompiler.service.DefaultPromptCompiler;
import com.tutict.eip.harnesscompiler.service.RuleBasedDslParser;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import com.tutict.eip.orchestrator.service.DefaultOrchestratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineIntegrationTest {

    @TempDir
    private Path tempDir;

    @Test
    void compilesRequirementGeneratesProjectWritesFilesAndVerifies() throws Exception {
        OllamaProperties properties = new OllamaProperties();
        properties.setOutputRoot(tempDir);
        AtomicReference<List<List<String>>> verifyCommands = new AtomicReference<>();
        DefaultOrchestratorService orchestratorService = new DefaultOrchestratorService(
                new CompileService(new RuleBasedDslParser(), new DefaultPromptCompiler()),
                new DefaultAutoRepairGenerationService(
                        new FileBlockAgentAdapter(),
                        new MarkerProjectFileWriter(properties),
                        capturingSuccessfulVerifier(verifyCommands)
                )
        );
        OrchestratorRunRequest request = new OrchestratorRunRequest();
        request.setRunId("pipeline-1");
        request.setRequirement("Build a user login API with JWT and persistence.");
        request.setModel("stub-model");
        request.setTargetDirectory("pipeline-demo");
        request.setVerifyCommands(List.of(List.of("mvn", "test")));
        request.setMaxRepairRounds(1);

        OrchestratorRunResponse response = orchestratorService.run(request);

        assertThat(response.getRunId()).isEqualTo("pipeline-1");
        assertThat(response.getDsl().getModules()).contains("api", "authentication", "persistence");
        assertThat(response.getHarnessPrompt()).contains("# SECURITY CONTROLS");
        assertThat(response.getGeneration().isSuccessful()).isTrue();
        assertThat(response.getGeneration().getStatus()).isEqualTo("VERIFIED");
        assertThat(verifyCommands.get()).isEqualTo(List.of(List.of("mvn", "test")));
        assertThat(Files.readString(tempDir.resolve("pipeline-demo/src/main/java/App.java")))
                .contains("class App");
    }

    private ProjectVerifier capturingSuccessfulVerifier(AtomicReference<List<List<String>>> verifyCommands) {
        return (targetDirectory, commands) -> {
            verifyCommands.set(commands);
            return new VerificationResult(true, "All verification commands passed", List.of());
        };
    }

    private static final class FileBlockAgentAdapter implements AgentAdapter {
        @Override
        public String provider() {
            return "stub";
        }

        @Override
        public OllamaGenerationResult generate(AgentExecutionRequest request, Consumer<String> tokenConsumer) {
            return new OllamaGenerationResult(
                    "stub-model",
                    """
                    ===FILE START===
                    src/main/java/App.java
                    class App {}
                    ===FILE END===
                    """,
                    1,
                    1
            );
        }
    }
}
