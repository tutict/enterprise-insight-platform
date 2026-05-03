package com.tutict.eip.agentadapter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaAgentAdapterTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void readStreamingOllamaResponse() throws IOException {
        startServer(exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertThat(requestBody).contains("Generate Java code");
            writeResponse(exchange, 200, """
                    {"response":"class ","done":false}
                    {"response":"User {}","done":false}
                    {"done":true}
                    """);
        });
        OllamaAgentAdapter adapter = newAdapter();
        AgentExecutionRequest request = new AgentExecutionRequest();
        request.setHarnessPrompt("Generate Java code");
        request.setTargetPath("User.java");
        List<String> tokens = new ArrayList<>();

        OllamaGenerationResult result = adapter.generate(request, tokens::add);

        assertThat(result.getContent()).isEqualTo("class User {}");
        assertThat(tokens).containsExactly("class ", "User {}");
        assertThat(result.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void retryRetryableHttpFailure() throws IOException {
        AtomicInteger attempts = new AtomicInteger();
        startServer(exchange -> {
            if (attempts.incrementAndGet() == 1) {
                writeResponse(exchange, 500, "temporary failure");
                return;
            }
            writeResponse(exchange, 200, """
                    {"response":"ok","done":false}
                    {"done":true}
                    """);
        });
        OllamaAgentAdapter adapter = newAdapter();
        AgentExecutionRequest request = new AgentExecutionRequest();
        request.setHarnessPrompt("Generate Java code");
        request.setTargetPath("User.java");

        OllamaGenerationResult result = adapter.generate(request, ignored -> {
        });

        assertThat(result.getContent()).isEqualTo("ok");
        assertThat(result.getAttemptCount()).isEqualTo(2);
        assertThat(attempts).hasValue(2);
    }

    private OllamaAgentAdapter newAdapter() {
        OllamaProperties properties = new OllamaProperties();
        properties.setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.setModel("llama3.1");
        properties.setMaxRetries(1);
        properties.setRetryBackoff(Duration.ofMillis(1));
        properties.setRequestTimeout(Duration.ofSeconds(5));
        return new OllamaAgentAdapter(HttpClient.newHttpClient(), new ObjectMapper(), properties);
    }

    private void startServer(ExchangeHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", handler::handle);
        server.start();
    }

    private void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/x-ndjson");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @FunctionalInterface
    private interface ExchangeHandler {
        void handle(HttpExchange exchange) throws IOException;
    }
}
