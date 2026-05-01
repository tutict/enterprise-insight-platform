package com.tutict.eip.agentadapter.controller;

import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.AgentExecutionResponse;
import com.tutict.eip.agentadapter.service.AgentExecutionService;
import com.tutict.eip.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/agent-adapter")
public class AgentExecutionController {

    private final AgentExecutionService agentExecutionService;
    private final Executor executor;

    public AgentExecutionController(
            AgentExecutionService agentExecutionService,
            @Qualifier("agentAdapterTaskExecutor") Executor executor
    ) {
        this.agentExecutionService = agentExecutionService;
        this.executor = executor;
    }

    @PostMapping("/execute")
    public ApiResponse<AgentExecutionResponse> execute(@Valid @RequestBody AgentExecutionRequest request) {
        return ApiResponse.ok(agentExecutionService.execute(request));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody AgentExecutionRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        executor.execute(() -> streamToEmitter(request, emitter));
        return emitter;
    }

    private void streamToEmitter(AgentExecutionRequest request, SseEmitter emitter) {
        try {
            agentExecutionService.stream(request, token -> sendToken(emitter, token), response -> {
                try {
                    emitter.send(SseEmitter.event().name("complete").data(response));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            });
        } catch (RuntimeException ex) {
            emitter.completeWithError(ex);
        }
    }

    private void sendToken(SseEmitter emitter, String token) {
        try {
            emitter.send(SseEmitter.event().name("token").data(token));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to send stream token", ex);
        }
    }
}
