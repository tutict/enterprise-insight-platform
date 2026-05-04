package com.tutict.eip.agentadapter.verify;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.exception.AgentAdapterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessProjectVerifierSecurityTest {

    @TempDir
    private Path tempDir;

    @Test
    void rejectsShellBasedVerifyCommands() {
        ProcessProjectVerifier verifier = newVerifier();

        assertThatThrownBy(() -> verifier.verify("demo-app", List.of(List.of("powershell", "-Command", "Remove-Item *"))))
                .isInstanceOf(AgentAdapterException.class)
                .hasMessageContaining("executable is not allowed");
    }

    @Test
    void rejectsVerifyCommandArgumentsWithShellControlTokens() {
        ProcessProjectVerifier verifier = newVerifier();

        assertThatThrownBy(() -> verifier.verify("demo-app", List.of(List.of("mvn", "test;curl", "http://example.test"))))
                .isInstanceOf(AgentAdapterException.class)
                .hasMessageContaining("disallowed token");
    }

    @Test
    void rejectsTargetDirectoryOutsideOutputRoot() {
        ProcessProjectVerifier verifier = newVerifier();

        assertThatThrownBy(() -> verifier.verify("../escape", List.of(List.of("mvn", "test"))))
                .isInstanceOf(AgentAdapterException.class)
                .hasMessageContaining("escapes outputRoot");
    }

    private ProcessProjectVerifier newVerifier() {
        OllamaProperties properties = new OllamaProperties();
        properties.setOutputRoot(tempDir);
        return new ProcessProjectVerifier(properties);
    }
}
