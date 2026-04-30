package com.tutict.eip.agentadapter.storage;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.WrittenFile;
import com.tutict.eip.agentadapter.exception.AgentAdapterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalCodeFileWriterTest {

    @TempDir
    private Path tempDir;

    @Test
    void writeGeneratedCodeInsideOutputRoot() throws Exception {
        LocalCodeFileWriter writer = newWriter();

        WrittenFile writtenFile = writer.write("src/main/java/User.java", "class User {}");

        Path target = tempDir.resolve("src/main/java/User.java");
        assertThat(Files.readString(target)).isEqualTo("class User {}");
        assertThat(writtenFile.getBytesWritten()).isEqualTo("class User {}".getBytes().length);
        assertThat(writtenFile.getAbsolutePath()).isEqualTo(target.toAbsolutePath().normalize().toString());
    }

    @Test
    void rejectPathTraversal() {
        LocalCodeFileWriter writer = newWriter();

        assertThatThrownBy(() -> writer.write("../escape.java", "bad"))
                .isInstanceOf(AgentAdapterException.class)
                .hasMessageContaining("escapes outputRoot");
    }

    private LocalCodeFileWriter newWriter() {
        OllamaProperties properties = new OllamaProperties();
        properties.setOutputRoot(tempDir);
        return new LocalCodeFileWriter(properties);
    }
}
