package com.tutict.eip.agentadapter.verify;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.VerificationCommandResult;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.agentadapter.exception.AgentAdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessProjectVerifier implements ProjectVerifier {

    private static final Logger log = LoggerFactory.getLogger(ProcessProjectVerifier.class);
    private static final int MAX_OUTPUT_CHARS = 12000;

    private final OllamaProperties properties;

    public ProcessProjectVerifier(OllamaProperties properties) {
        this.properties = properties;
    }

    @Override
    public VerificationResult verify(String targetDirectory, List<List<String>> commands) {
        Path projectRoot = resolveProjectRoot(targetDirectory);
        List<VerificationCommandResult> results = new ArrayList<>();
        for (List<String> command : commands) {
            VerificationCommandResult result = runCommand(projectRoot, command);
            results.add(result);
            if (result.isTimedOut() || result.getExitCode() != 0) {
                return new VerificationResult(false, buildFailureSummary(result), results);
            }
        }
        return new VerificationResult(true, "All verification commands passed", results);
    }

    private VerificationCommandResult runCommand(Path projectRoot, List<String> command) {
        if (command == null || command.isEmpty()) {
            throw new AgentAdapterException("Verification command must not be empty");
        }
        String commandText = String.join(" ", command);
        Instant startedAt = Instant.now();
        log.info("Running verification command projectRoot={} command={}", projectRoot, commandText);
        Process process = null;
        try {
            process = new ProcessBuilder(command)
                    .directory(projectRoot.toFile())
                    .start();
            Process runningProcess = process;
            CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() -> readStream(runningProcess.getInputStream()));
            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> readStream(runningProcess.getErrorStream()));
            boolean finished = process.waitFor(properties.getVerificationTimeout().toMillis(), TimeUnit.MILLISECONDS);
            long durationMillis = Duration.between(startedAt, Instant.now()).toMillis();
            if (!finished) {
                process.destroyForcibly();
                log.warn("Verification command timed out command={} timeout={}", commandText, properties.getVerificationTimeout());
                return new VerificationCommandResult(commandText, -1, true, "", "Command timed out", durationMillis);
            }
            int exitCode = process.exitValue();
            String stdout = truncate(stdoutFuture.join());
            String stderr = truncate(stderrFuture.join());
            log.info("Verification command completed command={} exitCode={} durationMillis={}",
                    commandText, exitCode, durationMillis);
            return new VerificationCommandResult(commandText, exitCode, false, stdout, stderr, durationMillis);
        } catch (IOException ex) {
            throw new AgentAdapterException("Failed to start verification command: " + commandText, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AgentAdapterException("Verification command interrupted: " + commandText, ex);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
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

    private String readStream(java.io.InputStream inputStream) {
        try (inputStream) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "Failed to read process output: " + ex.getMessage();
        }
    }

    private String buildFailureSummary(VerificationCommandResult result) {
        return "Verification failed for command: " + result.getCommand()
                + System.lineSeparator()
                + "exitCode=" + result.getExitCode()
                + System.lineSeparator()
                + "stdout:"
                + System.lineSeparator()
                + result.getStdout()
                + System.lineSeparator()
                + "stderr:"
                + System.lineSeparator()
                + result.getStderr();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_OUTPUT_CHARS) {
            return value;
        }
        return value.substring(0, MAX_OUTPUT_CHARS) + System.lineSeparator() + "...<truncated>";
    }
}
