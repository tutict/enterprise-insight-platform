package com.tutict.eip.ai.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.tutict.eip.ai.config.OllamaProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Component
public class OllamaClient {

    private final RestClient restClient;
    private final OllamaProperties properties;

    public OllamaClient(OllamaProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public List<List<Double>> embed(List<String> inputs) {
        EmbedRequest request = new EmbedRequest(properties.getEmbeddingModel(), inputs);
        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/api/embed")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() == 404) {
                return embedLegacy(inputs);
            }
            throw ex;
        }
        if (response == null || response.get("embeddings") == null) {
            throw new IllegalStateException("Ollama embeddings response is empty");
        }
        List<List<Double>> vectors = new ArrayList<>();
        for (JsonNode embeddingNode : response.get("embeddings")) {
            List<Double> vector = new ArrayList<>();
            for (JsonNode valueNode : embeddingNode) {
                vector.add(valueNode.asDouble());
            }
            vectors.add(vector);
        }
        return vectors;
    }

    public String chat(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", systemPrompt),
                new ChatMessage("user", userPrompt)
        );
        ChatRequest request = new ChatRequest(properties.getModel(), messages, false);
        JsonNode response = restClient.post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("Ollama chat response is empty");
        }
        JsonNode messageNode = response.get("message");
        if (messageNode != null && messageNode.get("content") != null) {
            return messageNode.get("content").asText();
        }
        JsonNode legacy = response.get("response");
        if (legacy != null) {
            return legacy.asText();
        }
        throw new IllegalStateException("Ollama chat response missing content");
    }

    private record EmbedRequest(String model, List<String> input) {
    }

    private List<List<Double>> embedLegacy(List<String> inputs) {
        List<List<Double>> vectors = new ArrayList<>();
        for (String input : inputs) {
            EmbedLegacyRequest request = new EmbedLegacyRequest(properties.getEmbeddingModel(), input);
            JsonNode response = restClient.post()
                    .uri("/api/embeddings")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || response.get("embedding") == null) {
                throw new IllegalStateException("Ollama legacy embeddings response is empty");
            }
            List<Double> vector = new ArrayList<>();
            for (JsonNode valueNode : response.get("embedding")) {
                vector.add(valueNode.asDouble());
            }
            vectors.add(vector);
        }
        return vectors;
    }

    private record ChatRequest(String model, List<ChatMessage> messages, boolean stream) {
    }

    private record ChatMessage(String role, String content) {
    }

    private record EmbedLegacyRequest(String model, String prompt) {
    }
}
