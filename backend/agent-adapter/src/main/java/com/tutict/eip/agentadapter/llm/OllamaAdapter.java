package com.tutict.eip.agentadapter.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class OllamaAdapter implements LLMAdapter {

    private static final Logger log = LoggerFactory.getLogger(OllamaAdapter.class);

    private final LLMConfig config;
    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;

    public OllamaAdapter(LLMConfig config, RestOperations restOperations, ObjectMapper objectMapper) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.restOperations = Objects.requireNonNull(restOperations, "restOperations must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        validateConfig();
    }

    @Override
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new LLMAdapterException("Prompt must not be blank");
        }

        URI uri = URI.create(normalizeBaseUrl(config.getBaseUrl()) + "/api/generate");
        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "prompt", prompt,
                "stream", config.isStream()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.info("Ollama request uri={} model={} stream={} promptChars={}",
                uri, config.getModel(), config.isStream(), prompt.length());
        try {
            ResponseEntity<String> response = restOperations.postForEntity(
                    uri,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            log.info("Ollama response status={} bodyChars={}",
                    response.getStatusCode().value(),
                    response.getBody() == null ? 0 : response.getBody().length());
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new LLMAdapterException("Ollama request failed with status: " + response.getStatusCode());
            }
            return parseOllamaResponse(response.getBody());
        } catch (RestClientException ex) {
            log.error("Ollama request failed model={} message={}", config.getModel(), ex.getMessage(), ex);
            throw new LLMAdapterException("Ollama request failed: " + ex.getMessage(), ex);
        }
    }

    private String parseOllamaResponse(String responseBody) {
        if (config.isStream()) {
            throw new LLMAdapterException("Streaming mode is not implemented by this generate(String prompt) method");
        }
        if (responseBody == null || responseBody.isBlank()) {
            throw new LLMAdapterException("Ollama response body is empty");
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode() && !errorNode.asText().isBlank()) {
                throw new LLMAdapterException("Ollama response error: " + errorNode.asText());
            }
            JsonNode responseNode = root.path("response");
            if (responseNode.isMissingNode()) {
                throw new LLMAdapterException("Ollama response does not contain response field");
            }
            return responseNode.asText();
        } catch (LLMAdapterException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LLMAdapterException("Failed to parse Ollama response", ex);
        }
    }

    private void validateConfig() {
        if (config.getBaseUrl() == null || config.getBaseUrl().isBlank()) {
            throw new LLMAdapterException("Ollama baseUrl must not be blank");
        }
        if (config.getModel() == null || config.getModel().isBlank()) {
            throw new LLMAdapterException("Ollama model must not be blank");
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
