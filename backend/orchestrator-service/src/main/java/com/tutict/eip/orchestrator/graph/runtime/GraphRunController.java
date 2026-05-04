package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.orchestrator.graph.model.GraphCompileResult;
import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
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
@RequestMapping("/api/graph/run")
public class GraphRunController {

    private final GraphEventStreamService streamService;
    private final GraphExecutor graphExecutor;
    private final GraphCompileService compileService;

    public GraphRunController(
            GraphEventStreamService streamService,
            GraphExecutor graphExecutor,
            GraphCompileService compileService
    ) {
        this.streamService = streamService;
        this.graphExecutor = graphExecutor;
        this.compileService = compileService;
    }

    @PostMapping
    public ApiResponse<GraphRunStartResponse> start(@RequestBody(required = false) GraphRunRequest request) {
        GraphRunRequest actualRequest = request == null ? new GraphRunRequest() : request;
        String runId = streamService.createRun(actualRequest.getRunId());
        GraphDefinition graph = actualRequest.getGraph() == null
                ? DefaultGraphDefinitions.compileGenerateVerifyRepair(
                        actualRequest.getMaxIterations(),
                        actualRequest.getRequiredRepairIterations()
                )
                : actualRequest.getGraph();
        GraphCompileResult compileResult = compileService.compile(graph);
        if (!compileResult.isValid()) {
            throw new IllegalArgumentException(String.join("; ", compileResult.getErrors()));
        }
        graph = compileResult.getGraph();

        graphExecutor.executeAsync(runId, graph);
        return ApiResponse.ok("graph run accepted", new GraphRunStartResponse(runId, graph));
    }

    @GetMapping(path = "/stream/{runId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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

    public static class GraphRunRequest {
        private String runId;
        private int maxIterations = 3;
        private int requiredRepairIterations = 1;
        private GraphDefinition graph;

        public String getRunId() {
            return runId;
        }

        public void setRunId(String runId) {
            this.runId = runId;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public int getRequiredRepairIterations() {
            return requiredRepairIterations;
        }

        public void setRequiredRepairIterations(int requiredRepairIterations) {
            this.requiredRepairIterations = requiredRepairIterations;
        }

        public GraphDefinition getGraph() {
            return graph;
        }

        public void setGraph(GraphDefinition graph) {
            this.graph = graph;
        }
    }

    public static class GraphRunStartResponse {
        private String runId;
        private GraphDefinition graph;

        public GraphRunStartResponse() {
        }

        public GraphRunStartResponse(String runId, GraphDefinition graph) {
            this.runId = runId;
            this.graph = graph;
        }

        public String getRunId() {
            return runId;
        }

        public void setRunId(String runId) {
            this.runId = runId;
        }

        public GraphDefinition getGraph() {
            return graph;
        }

        public void setGraph(GraphDefinition graph) {
            this.graph = graph;
        }
    }
}
