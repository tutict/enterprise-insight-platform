package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.orchestrator.graph.model.GraphCompileResult;
import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
public class GraphCompileController {

    private final GraphCompileService compileService;

    public GraphCompileController(GraphCompileService compileService) {
        this.compileService = compileService;
    }

    @PostMapping("/compile")
    public ApiResponse<GraphCompileResult> compile(@RequestBody(required = false) GraphDefinition graph) {
        GraphCompileResult result = compileService.compile(graph);
        return ApiResponse.ok(result.isValid() ? "graph compiled" : "graph validation failed", result);
    }
}
