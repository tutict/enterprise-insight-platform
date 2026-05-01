package com.tutict.eip.agentadapter.storage;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.ProjectWriteResult;
import com.tutict.eip.agentadapter.exception.AgentAdapterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkerProjectFileWriterTest {

    @TempDir
    private Path tempDir;

    @Test
    void writeMultipleFileBlocksIntoProjectDirectory() throws Exception {
        MarkerProjectFileWriter writer = newWriter();
        String output = """
                ===FILE START===
                pom.xml
                <project></project>
                ===FILE END===
                ===FILE START===
                src/main/java/App.java
                class App {}
                ===FILE END===
                """;

        ProjectWriteResult result = writer.writeProject("demo-app", output);

        assertThat(result.getFiles()).hasSize(2);
        assertThat(Files.readString(tempDir.resolve("demo-app/pom.xml"))).isEqualTo("<project></project>");
        assertThat(Files.readString(tempDir.resolve("demo-app/src/main/java/App.java"))).isEqualTo("class App {}");
    }

    @Test
    void rejectOutputWithoutFileBlocks() {
        MarkerProjectFileWriter writer = newWriter();

        assertThatThrownBy(() -> writer.writeProject("demo-app", "plain text"))
                .isInstanceOf(AgentAdapterException.class)
                .hasMessageContaining("did not contain");
    }

    @Test
    void rejectGeneratedPathEscapingProjectRoot() {
        MarkerProjectFileWriter writer = newWriter();
        String output = """
                ===FILE START===
                ../escape.txt
                bad
                ===FILE END===
                """;

        assertThatThrownBy(() -> writer.writeProject("demo-app", output))
                .isInstanceOf(AgentAdapterException.class)
                .hasMessageContaining("escapes projectRoot");
    }

    private MarkerProjectFileWriter newWriter() {
        OllamaProperties properties = new OllamaProperties();
        properties.setOutputRoot(tempDir);
        return new MarkerProjectFileWriter(properties);
    }
}
