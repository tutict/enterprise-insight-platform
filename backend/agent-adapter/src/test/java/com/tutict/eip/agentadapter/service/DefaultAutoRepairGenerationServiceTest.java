package com.tutict.eip.agentadapter.service;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.agentadapter.storage.MarkerProjectFileWriter;
import com.tutict.eip.agentadapter.verify.ProjectVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAutoRepairGenerationServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void repairAfterVerificationFailureAndStopOnSuccess() throws Exception {
        SequencedAgentAdapter agentAdapter = new SequencedAgentAdapter();
        SequencedVerifier verifier = new SequencedVerifier();
        OllamaProperties properties = new OllamaProperties();
        properties.setOutputRoot(tempDir);
        AutoRepairGenerationService service = new DefaultAutoRepairGenerationService(
                agentAdapter,
                new MarkerProjectFileWriter(properties),
                verifier
        );
        AutoRepairGenerationRequest request = new AutoRepairGenerationRequest();
        request.setPrompt("Generate a tiny Java project.");
        request.setTargetDirectory("demo-app");
        request.setVerifyCommands(List.of(List.of("mvn", "test")));
        request.setMaxRepairRounds(2);

        AutoRepairGenerationResponse response = service.generateAndRepair(request);

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.getStatus()).isEqualTo("VERIFIED");
        assertThat(response.getAttempts()).hasSize(2);
        assertThat(response.getAttempts().get(1).getPrompt()).contains("# VERIFICATION ERROR");
        assertThat(agentAdapter.prompts()).hasSize(2);
        assertThat(Files.readString(tempDir.resolve("demo-app/src/main/java/App.java"))).isEqualTo("class App { }");
    }

    private static class SequencedAgentAdapter implements AgentAdapter {

        private final java.util.List<String> prompts = new java.util.ArrayList<>();

        @Override
        public String provider() {
            return "stub";
        }

        @Override
        public OllamaGenerationResult generate(AgentExecutionRequest request, Consumer<String> tokenConsumer) {
            prompts.add(request.getHarnessPrompt());
            String content = prompts.size() == 1
                    ? """
                    ===FILE START===
                    src/main/java/App.java
                    class App {
                    ===FILE END===
                    """
                    : """
                    ===FILE START===
                    src/main/java/App.java
                    class App { }
                    ===FILE END===
                    """;
            return new OllamaGenerationResult("stub-model", content, 1, 1);
        }

        private List<String> prompts() {
            return prompts;
        }
    }

    private static class SequencedVerifier implements ProjectVerifier {

        private int calls;

        @Override
        public VerificationResult verify(String targetDirectory, List<List<String>> commands) {
            calls++;
            if (calls == 1) {
                return new VerificationResult(false, "Compilation error: reached end of file", List.of());
            }
            return new VerificationResult(true, "All verification commands passed", List.of());
        }
    }
}
