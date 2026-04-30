package com.tutict.eip.ai.mcp;

import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class McpWebSocketClient {

    private final WebSocketClient webSocketClient;
    private final McpProperties properties;

    public McpWebSocketClient(WebSocketClient webSocketClient, McpProperties properties) {
        this.webSocketClient = webSocketClient;
        this.properties = properties;
    }

    public String send(String payload) {
        String endpoint = properties.getWebsocketUrl();
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("MCP websocket URL is not configured");
        }

        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        McpTextHandler handler = new McpTextHandler(payload, responseFuture);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        String token = properties.getAuthToken();
        if (token != null && !token.isBlank()) {
            headers.add("Authorization", "Bearer " + token);
        }

        WebSocketSession session = null;
        try {
            ListenableFuture<WebSocketSession> handshake =
                    webSocketClient.doHandshake(handler, headers, URI.create(endpoint));
            session = handshake.get(properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);
            return responseFuture.get(properties.getResponseTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            responseFuture.completeExceptionally(ex);
            throw new IllegalStateException("MCP websocket call failed", ex);
        } finally {
            if (session != null && session.isOpen()) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (Exception ignore) {
                    // ignore close errors
                }
            }
        }
    }

    private static class McpTextHandler extends TextWebSocketHandler {

        private final String payload;
        private final CompletableFuture<String> responseFuture;

        private McpTextHandler(String payload, CompletableFuture<String> responseFuture) {
            this.payload = payload;
            this.responseFuture = responseFuture;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            session.sendMessage(new TextMessage(payload));
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            responseFuture.complete(message.getPayload());
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            responseFuture.completeExceptionally(exception);
        }
    }
}
