package com.tutict.eip.orchestrator.runtime;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orchestrator")
public class RunStreamController {

    private final RunEventStreamService streamService;
    private final RunExecutionEngine executionEngine;

    public RunStreamController(RunEventStreamService streamService, RunExecutionEngine executionEngine) {
        this.streamService = streamService;
        this.executionEngine = executionEngine;
    }

    @PostMapping("/run/start")
    public ApiResponse<RunStartResponse> start(@Valid @RequestBody OrchestratorRunRequest request) {
        String runId = streamService.createRun(request.getRunId());
        request.setRunId(runId);
        executionEngine.executeAsync(runId, request);
        return ApiResponse.ok("run accepted", new RunStartResponse(runId));
    }

    @GetMapping(path = "/run/stream/{runId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @PathVariable String runId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventIdHeader,
            @RequestParam(value = "lastEventId", required = false) String lastEventIdParam
    ) {
        String lastEventId = lastEventIdHeader == null || lastEventIdHeader.isBlank()
                ? lastEventIdParam
                : lastEventIdHeader;
        return streamService.connect(runId, lastEventId);
    }

    @PostMapping("/run/control")
    public ApiResponse<Void> control(@Valid @RequestBody ControlEvent event) {
        switch (event.getType()) {
            case "PAUSE" -> streamService.pause(event.getRunId());
            case "RESUME" -> streamService.resume(event.getRunId());
            case "CANCEL" -> streamService.cancel(event.getRunId());
            case "RETRY_STEP" -> streamService.retryStep(event.getRunId(), event.getStep());
            default -> throw new IllegalArgumentException("Unsupported control event: " + event.getType());
        }
        return ApiResponse.ok("control event accepted", null);
    }

    public static class RunStartResponse {
        private String runId;

        public RunStartResponse() {
        }

        public RunStartResponse(String runId) {
            this.runId = runId;
        }

        public String getRunId() {
            return runId;
        }

        public void setRunId(String runId) {
            this.runId = runId;
        }
    }

    public static class ControlEvent {
        @NotBlank
        private String type;

        @NotBlank
        private String runId;

        private String step;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRunId() {
            return runId;
        }

        public void setRunId(String runId) {
            this.runId = runId;
        }

        public String getStep() {
            return step;
        }

        public void setStep(String step) {
            this.step = step;
        }
    }
}
