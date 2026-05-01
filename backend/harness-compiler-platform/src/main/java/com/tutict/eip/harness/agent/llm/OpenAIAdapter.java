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
import java.util.List;
import java.util.Map;

public class OpenAIAdapter implements LLMAdapter {

    private static final Logger log = LoggerFactory.getLogger(OpenAIAdapter.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LLMProperties properties;

    public OpenAIAdapter(RestTemplate restTemplate, ObjectMapper objectMapper, LLMProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public String generate(String prompt) {
        validate(prompt);
        URI uri = URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/chat/completions");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        log.info("Calling OpenAI API uri={} model={} promptChars={}", uri, properties.getModel(), prompt.length());
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, new HttpEntity<>(body, headers), String.class);
            log.info("OpenAI API response status={} bodyChars={}",
                    response.getStatusCode().value(),
                    response.getBody() == null ? 0 : response.getBody().length());
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new LLMAdapterException("OpenAI API failed with status " + response.getStatusCode());
            }
            return parseResponse(response.getBody());
        } catch (RestClientException ex) {
            log.error("OpenAI API request failed message={}", ex.getMessage(), ex);
            throw new LLMAdapterException("OpenAI API request failed: " + ex.getMessage(), ex);
        }
    }

    private void validate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new LLMAdapterException("prompt must not be blank");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new LLMAdapterException("OpenAI api key is required");
        }
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new LLMAdapterException("OpenAI base url is required");
        }
        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new LLMAdapterException("OpenAI model is required");
        }
    }

    private String parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new LLMAdapterException("OpenAI response body is empty");
        }
        try {
            JsonNode contentNode = objectMapper.readTree(responseBody)
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new LLMAdapterException("OpenAI response content is missing");
            }
            return contentNode.asText();
        } catch (LLMAdapterException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LLMAdapterException("Failed to parse OpenAI response", ex);
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
