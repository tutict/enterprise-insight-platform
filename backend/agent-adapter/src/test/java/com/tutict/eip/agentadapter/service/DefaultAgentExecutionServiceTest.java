package com.tutict.eip.agentadapter.service;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.AgentExecutionResponse;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;
import com.tutict.eip.agentadapter.storage.LocalCodeFileWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAgentExecutionServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void executeWritesAgentOutputToLocalFile() throws Exception {
        OllamaProperties properties = new OllamaProperties();
        properties.setOutputRoot(tempDir);
        AgentExecutionService service = new DefaultAgentExecutionService(new StubAgentAdapter(), new LocalCodeFileWriter(properties));
        AgentExecutionRequest request = new AgentExecutionRequest();
        request.setHarnessPrompt("Generate Java code");
        request.setTargetPath("User.java");

        AgentExecutionResponse response = service.execute(request);

        assertThat(response.getProvider()).isEqualTo("stub");
        assertThat(response.getContent()).isEqualTo("class User {}");
        assertThat(Files.readString(tempDir.resolve("User.java"))).isEqualTo("class User {}");
    }

    private static class StubAgentAdapter implements AgentAdapter {

        @Override
        public String provider() {
            return "stub";
        }

        @Override
        public OllamaGenerationResult generate(AgentExecutionRequest request, Consumer<String> tokenConsumer) {
            tokenConsumer.accept("class User {}");
            return new OllamaGenerationResult("stub-model", "class User {}", 1, 10);
        }
    }
}
