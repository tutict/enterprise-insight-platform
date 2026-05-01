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
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OpenAIAdapter implements LLMAdapter {

    private static final Logger log = LoggerFactory.getLogger(OpenAIAdapter.class);

    private final LLMConfig config;
    private final RestOperations restOperations;
    private final ObjectMapper objectMapper;

    public OpenAIAdapter(LLMConfig config, RestOperations restOperations, ObjectMapper objectMapper) {
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

        URI uri = URI.create(normalizeBaseUrl(config.getBaseUrl()) + "/chat/completions");
        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        log.info("OpenAI request uri={} model={} promptChars={}", uri, config.getModel(), prompt.length());
        try {
            ResponseEntity<String> response = restOperations.postForEntity(
                    uri,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            log.info("OpenAI response status={} bodyChars={}",
                    response.getStatusCode().value(),
                    response.getBody() == null ? 0 : response.getBody().length());
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new LLMAdapterException("OpenAI request failed with status: " + response.getStatusCode());
            }
            return parseOpenAIResponse(response.getBody());
        } catch (RestClientException ex) {
            log.error("OpenAI request failed model={} message={}", config.getModel(), ex.getMessage(), ex);
            throw new LLMAdapterException("OpenAI request failed: " + ex.getMessage(), ex);
        }
    }

    private String parseOpenAIResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new LLMAdapterException("OpenAI response body is empty");
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new LLMAdapterException("OpenAI response does not contain choices[0].message.content");
            }
            return contentNode.asText();
        } catch (LLMAdapterException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LLMAdapterException("Failed to parse OpenAI response", ex);
        }
    }

    private void validateConfig() {
        if (config.getBaseUrl() == null || config.getBaseUrl().isBlank()) {
            throw new LLMAdapterException("OpenAI baseUrl must not be blank");
        }
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new LLMAdapterException("OpenAI apiKey must not be blank");
        }
        if (config.getModel() == null || config.getModel().isBlank()) {
            throw new LLMAdapterException("OpenAI model must not be blank");
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
