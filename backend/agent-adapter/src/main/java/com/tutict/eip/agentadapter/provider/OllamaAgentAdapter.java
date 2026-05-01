package com.tutict.eip.agentadapter.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.OllamaGenerateChunk;
import com.tutict.eip.agentadapter.domain.OllamaGenerateRequest;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;
import com.tutict.eip.agentadapter.exception.OllamaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

@Component
public class OllamaAgentAdapter implements AgentAdapter {

    private static final Logger log = LoggerFactory.getLogger(OllamaAgentAdapter.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OllamaProperties properties;

    public OllamaAgentAdapter(HttpClient ollamaHttpClient, ObjectMapper objectMapper, OllamaProperties properties) {
        this.httpClient = ollamaHttpClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public String provider() {
        return "ollama";
    }

    @Override
    public OllamaGenerationResult generate(AgentExecutionRequest request, Consumer<String> tokenConsumer) {
        int maxAttempts = properties.getMaxRetries() + 1;
        Instant startedAt = Instant.now();
        String model = resolveModel(request);
        log.info("Starting Ollama generation provider={} model={} maxAttempts={} targetPath={}",
                provider(), model, maxAttempts, request.getTargetPath());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Calling Ollama generate API attempt={} model={}", attempt, model);
                String content = callGenerateApi(request, tokenConsumer);
                long durationMillis = Duration.between(startedAt, Instant.now()).toMillis();
                log.info("Ollama generation completed model={} attempt={} durationMillis={} outputChars={}",
                        model, attempt, durationMillis, content.length());
                return new OllamaGenerationResult(model, content, attempt, durationMillis);
            } catch (OllamaClientException ex) {
                if (!ex.isRetryable() || attempt == maxAttempts) {
                    log.error("Ollama generation failed attempt={} retryable={} message={}",
                            attempt, ex.isRetryable(), ex.getMessage(), ex);
                    throw ex;
                }
                log.warn("Ollama generation retry scheduled attempt={} nextAttempt={} reason={}",
                        attempt, attempt + 1, ex.getMessage());
                sleepBeforeRetry(attempt);
            } catch (IOException ex) {
                if (attempt == maxAttempts) {
                    log.error("Ollama generation failed after IO error attempt={} message={}",
                            attempt, ex.getMessage(), ex);
                    throw new OllamaClientException("Ollama request failed: " + ex.getMessage(), ex, true);
                }
                log.warn("Ollama IO error retry scheduled attempt={} nextAttempt={} message={}",
                        attempt, attempt + 1, ex.getMessage());
                sleepBeforeRetry(attempt);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.error("Ollama generation interrupted attempt={}", attempt, ex);
                throw new OllamaClientException("Ollama request interrupted", ex, false);
            }
        }
        throw new OllamaClientException("Ollama request failed after retries", true);
    }

    private String callGenerateApi(AgentExecutionRequest request, Consumer<String> tokenConsumer)
            throws IOException, InterruptedException {
        String payload = objectMapper.writeValueAsString(new OllamaGenerateRequest(
                resolveModel(request),
                request.getHarnessPrompt(),
                true,
                request.getOptions()
        ));
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(generateUri())
                .timeout(properties.getRequestTimeout())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();
        HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        log.info("Ollama HTTP response status={} uri={}", response.statusCode(), httpRequest.uri());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = readErrorBody(response.body());
            boolean retryable = response.statusCode() == 429 || response.statusCode() >= 500;
            throw new OllamaClientException("Ollama HTTP " + response.statusCode() + ": " + body, retryable);
        }
        return readStreamingBody(response.body(), tokenConsumer);
    }

    private String readStreamingBody(InputStream inputStream, Consumer<String> tokenConsumer) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                OllamaGenerateChunk chunk = objectMapper.readValue(line, OllamaGenerateChunk.class);
                if (chunk.getError() != null && !chunk.getError().isBlank()) {
                    throw new OllamaClientException("Ollama stream error: " + chunk.getError(), false);
                }
                if (chunk.getResponse() != null && !chunk.getResponse().isEmpty()) {
                    content.append(chunk.getResponse());
                    tokenConsumer.accept(chunk.getResponse());
                }
                if (chunk.isDone()) {
                    break;
                }
            }
        }
        return content.toString();
    }

    private URI generateUri() {
        String baseUrl = properties.getBaseUrl();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return URI.create(baseUrl + "/api/generate");
    }

    private String resolveModel(AgentExecutionRequest request) {
        if (request.getModel() == null || request.getModel().isBlank()) {
            return properties.getModel();
        }
        return request.getModel();
    }

    private String readErrorBody(InputStream inputStream) throws IOException {
        try (inputStream) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sleepBeforeRetry(int attempt) {
        long baseMillis = properties.getRetryBackoff().toMillis();
        long sleepMillis = baseMillis * attempt;
        try {
            log.debug("Sleeping before Ollama retry sleepMillis={}", sleepMillis);
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new OllamaClientException("Retry interrupted", ex, false);
        }
    }
}
