package com.tutict.eip.agentadapter.storage;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.GeneratedProjectFile;
import com.tutict.eip.agentadapter.domain.ProjectWriteResult;
import com.tutict.eip.agentadapter.exception.AgentAdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class MarkerProjectFileWriter implements ProjectFileWriter {

    private static final Logger log = LoggerFactory.getLogger(MarkerProjectFileWriter.class);
    private static final String FILE_START = "===FILE START===";
    private static final String FILE_END = "===FILE END===";

    private final OllamaProperties properties;

    public MarkerProjectFileWriter(OllamaProperties properties) {
        this.properties = properties;
    }

    @Override
    public ProjectWriteResult writeProject(String targetDirectory, String generatedOutput) {
        Path projectRoot = resolveProjectRoot(targetDirectory);
        List<ParsedFileBlock> blocks = parseFileBlocks(generatedOutput);
        if (blocks.isEmpty()) {
            throw new AgentAdapterException("LLM output did not contain any ===FILE START=== blocks");
        }
        List<GeneratedProjectFile> files = new ArrayList<>();
        for (ParsedFileBlock block : blocks) {
            files.add(writeFile(projectRoot, block));
        }
        log.info("Generated project written projectRoot={} fileCount={}", projectRoot, files.size());
        return new ProjectWriteResult(projectRoot.toString(), files);
    }

    private Path resolveProjectRoot(String targetDirectory) {
        Path outputRoot = properties.getOutputRoot().toAbsolutePath().normalize();
        Path relativeDirectory = Path.of(targetDirectory);
        if (relativeDirectory.isAbsolute()) {
            throw new AgentAdapterException("targetDirectory must be relative to outputRoot");
        }
        Path projectRoot = outputRoot.resolve(relativeDirectory).normalize();
        if (!projectRoot.startsWith(outputRoot)) {
            throw new AgentAdapterException("targetDirectory escapes outputRoot: " + targetDirectory);
        }
        return projectRoot;
    }

    private GeneratedProjectFile writeFile(Path projectRoot, ParsedFileBlock block) {
        Path relativePath = Path.of(block.relativePath());
        if (relativePath.isAbsolute()) {
            throw new AgentAdapterException("Generated file path must be relative: " + block.relativePath());
        }
        Path target = projectRoot.resolve(relativePath).normalize();
        if (!target.startsWith(projectRoot)) {
            throw new AgentAdapterException("Generated file path escapes projectRoot: " + block.relativePath());
        }
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            byte[] bytes = block.content().getBytes(StandardCharsets.UTF_8);
            Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Generated project file written relativePath={} absolutePath={} bytesWritten={}",
                    block.relativePath(), target, bytes.length);
            return new GeneratedProjectFile(block.relativePath(), target.toString(), bytes.length);
        } catch (IOException ex) {
            throw new AgentAdapterException("Failed to write generated file " + target, ex);
        }
    }

    private List<ParsedFileBlock> parseFileBlocks(String generatedOutput) {
        String[] lines = generatedOutput.split("\\R", -1);
        List<ParsedFileBlock> blocks = new ArrayList<>();
        int index = 0;
        while (index < lines.length) {
            if (!FILE_START.equals(lines[index].trim())) {
                index++;
                continue;
            }
            index++;
            while (index < lines.length && lines[index].isBlank()) {
                index++;
            }
            if (index >= lines.length) {
                throw new AgentAdapterException("Missing file path after ===FILE START===");
            }
            String relativePath = lines[index].trim();
            index++;
            StringBuilder content = new StringBuilder();
            while (index < lines.length && !FILE_END.equals(lines[index].trim())) {
                content.append(lines[index]);
                index++;
                if (index < lines.length && !FILE_END.equals(lines[index].trim())) {
                    content.append(System.lineSeparator());
                }
            }
            if (index >= lines.length) {
                throw new AgentAdapterException("Missing ===FILE END=== for file " + relativePath);
            }
            blocks.add(new ParsedFileBlock(relativePath, content.toString()));
            index++;
        }
        return blocks;
    }

    private record ParsedFileBlock(String relativePath, String content) {
    }
}
