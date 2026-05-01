package com.tutict.eip.agentadapter.storage;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.WrittenFile;
import com.tutict.eip.agentadapter.exception.AgentAdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class LocalCodeFileWriter implements CodeFileWriter {

    private static final Logger log = LoggerFactory.getLogger(LocalCodeFileWriter.class);

    private final OllamaProperties properties;

    public LocalCodeFileWriter(OllamaProperties properties) {
        this.properties = properties;
    }

    @Override
    public WrittenFile write(String targetPath, String content) {
        Path outputRoot = properties.getOutputRoot().toAbsolutePath().normalize();
        Path relativePath = Path.of(targetPath);
        if (relativePath.isAbsolute()) {
            log.warn("Rejected absolute targetPath={}", targetPath);
            throw new AgentAdapterException("targetPath must be relative to outputRoot");
        }
        Path target = outputRoot.resolve(relativePath).normalize();
        if (!target.startsWith(outputRoot)) {
            log.warn("Rejected targetPath escaping outputRoot targetPath={} outputRoot={}", targetPath, outputRoot);
            throw new AgentAdapterException("targetPath escapes outputRoot: " + targetPath);
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Generated code written targetPath={} absolutePath={} bytesWritten={}",
                    targetPath, target, bytes.length);
            return new WrittenFile(targetPath, target.toString(), bytes.length);
        } catch (IOException ex) {
            log.error("Failed to write generated code targetPath={} absolutePath={}", targetPath, target, ex);
            throw new AgentAdapterException("Failed to write generated code to " + target, ex);
        }
    }
}
