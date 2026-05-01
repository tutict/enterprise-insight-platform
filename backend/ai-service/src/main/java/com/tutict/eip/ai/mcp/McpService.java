package com.tutict.eip.ai.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class McpService {

    private final McpWebSocketClient webSocketClient;
    private final McpProperties properties;
    private final ObjectMapper objectMapper;

    public McpService(McpWebSocketClient webSocketClient, McpProperties properties, ObjectMapper objectMapper) {
        this.webSocketClient = webSocketClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public JsonNode infer(McpInferRequest request) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("prompt", request.prompt());
        if (request.context() != null && !request.context().isBlank()) {
            payload.put("context", request.context());
        }
        if (request.topK() != null) {
            payload.put("topK", request.topK());
        }
        if (request.options() != null && !request.options().isEmpty()) {
            payload.set("options", objectMapper.valueToTree(request.options()));
        }
        return send("infer", payload);
    }

    public JsonNode retrieve(McpRetrieveRequest request) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("query", request.query());
        if (request.topK() != null) {
            payload.put("topK", request.topK());
        }
        if (request.filters() != null && !request.filters().isEmpty()) {
            payload.set("filters", objectMapper.valueToTree(request.filters()));
        }
        return send("retrieve", payload);
    }

    private JsonNode send(String action, ObjectNode payload) {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("MCP is disabled");
        }
        ObjectNode message = objectMapper.createObjectNode();
        message.put("id", UUID.randomUUID().toString());
        message.put("action", action);
        message.set("payload", payload);

        String response = webSocketClient.send(message.toString());
        try {
            return objectMapper.readTree(response);
        } catch (Exception ex) {
            ObjectNode fallback = objectMapper.createObjectNode();
            fallback.put("raw", response);
            return fallback;
        }
    }
}
