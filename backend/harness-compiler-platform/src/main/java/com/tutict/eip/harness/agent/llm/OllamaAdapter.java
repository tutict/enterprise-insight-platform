package com.tutict.eip.harness.agent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.harness.config.LLMProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

public class OllamaAdapter implements LLMAdapter {

    private static final Logger log = LoggerFactory.getLogger(OllamaAdapter.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LLMProperties properties;

    public OllamaAdapter(RestTemplate restTemplate, ObjectMapper objectMapper, LLMProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public String generate(String prompt) {
        validate(prompt);
        URI uri = URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/generate");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "prompt", prompt,
                "stream", false
        );

        log.info("Calling Ollama API uri={} model={} promptChars={}", uri, properties.getModel(), prompt.length());
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, new HttpEntity<>(body, headers), String.class);
            log.info("Ollama API response status={} bodyChars={}",
                    response.getStatusCode().value(),
                    response.getBody() == null ? 0 : response.getBody().length());
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new LLMAdapterException("Ollama API failed with status " + response.getStatusCode());
            }
            return parseResponse(response.getBody());
        } catch (RestClientException ex) {
            log.error("Ollama API request failed message={}", ex.getMessage(), ex);
            throw new LLMAdapterException("Ollama API request failed: " + ex.getMessage(), ex);
        }
    }

    private void validate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new LLMAdapterException("prompt must not be blank");
        }
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new LLMAdapterException("Ollama base url is required");
        }
        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new LLMAdapterException("Ollama model is required");
        }
    }

    private String parseResponse(String responseBody) {
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
                throw new LLMAdapterException("Ollama response field is missing");
            }
            return responseNode.asText();
        } catch (LLMAdapterException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LLMAdapterException("Failed to parse Ollama response", ex);
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
