package com.tutict.eip.ai.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tutict.eip.ai.config.QdrantProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class QdrantClient {

    private final RestClient restClient;
    private final QdrantProperties properties;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean collectionReady = new AtomicBoolean(false);

    public QdrantClient(QdrantProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public void ensureCollection(int vectorSize) {
        if (collectionReady.get()) {
            return;
        }
        boolean exists = collectionExists();
        if (!exists) {
            createCollection(vectorSize);
        }
        collectionReady.set(true);
    }

    public void upsert(List<QdrantPoint> points) {
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode pointsNode = payload.putArray("points");
        for (QdrantPoint point : points) {
            ObjectNode pointNode = pointsNode.addObject();
            pointNode.put("id", point.id());
            ArrayNode vectorNode = pointNode.putArray("vector");
            for (Double value : point.vector()) {
                vectorNode.add(value);
            }
            if (point.payload() != null && !point.payload().isEmpty()) {
                pointNode.set("payload", objectMapper.valueToTree(point.payload()));
            }
        }
        restClient.put()
                .uri("/collections/{collection}/points?wait=true", properties.getCollection())
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    public List<SearchHit> search(List<Double> vector, int limit) {
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode vectorNode = payload.putArray("query");
        for (Double value : vector) {
            vectorNode.add(value);
        }
        payload.put("limit", limit);
        payload.put("with_payload", true);
        JsonNode response = restClient.post()
                .uri("/collections/{collection}/points/query", properties.getCollection())
                .body(payload)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || response.get("result") == null) {
            return List.of();
        }
        List<SearchHit> hits = new ArrayList<>();
        for (JsonNode item : response.get("result")) {
            double score = item.get("score").asDouble();
            JsonNode payloadNode = item.get("payload");
            Map<String, Object> payloadMap = payloadNode == null ? Map.of() : objectMapper.convertValue(payloadNode, Map.class);
            hits.add(new SearchHit(score, payloadMap));
        }
        return hits;
    }

    private boolean collectionExists() {
        try {
            restClient.get()
                    .uri("/collections/{collection}", properties.getCollection())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException ex) {
            if (ex.getRawStatusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }

    private void createCollection(int vectorSize) {
        ObjectNode payload = objectMapper.createObjectNode();
        ObjectNode vectors = payload.putObject("vectors");
        vectors.put("size", vectorSize);
        vectors.put("distance", "Cosine");
        restClient.put()
                .uri("/collections/{collection}", properties.getCollection())
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    public record QdrantPoint(String id, List<Double> vector, Map<String, Object> payload) {
    }

    public record SearchHit(double score, Map<String, Object> payload) {
    }
}
