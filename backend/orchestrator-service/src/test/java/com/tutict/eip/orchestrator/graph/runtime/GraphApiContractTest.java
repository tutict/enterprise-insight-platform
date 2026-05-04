package com.tutict.eip.orchestrator.graph.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.orchestrator.controller.GlobalExceptionHandler;
import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GraphApiContractTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        GraphEventStreamService streamService = new GraphEventStreamService();
        GraphExecutor executor = new GraphExecutor(streamService) {
            @Override
            public void executeAsync(String runId, GraphDefinition graph) {
                streamService.emit(GraphEvent.of(runId, "GRAPH_RUN_STARTED", null, null, Map.of("definition", graph)));
                streamService.emit(GraphEvent.of(runId, "GRAPH_RUN_COMPLETED", null, null, Map.of("status", "completed")));
            }
        };
        GraphCompileService compileService = new GraphCompileService();
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new GraphCompileController(compileService),
                        new GraphRunController(streamService, executor, compileService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void compileEndpointReturnsValidationEnvelope() throws Exception {
        mockMvc.perform(post("/api/graph/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validGraph())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.graph.startNodeId").value("start"))
                .andExpect(jsonPath("$.data.graph.edges[0].condition").value("success"));
    }

    @Test
    void runEndpointReturnsRunIdAndAcceptedGraph() throws Exception {
        mockMvc.perform(post("/api/graph/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("runId", "graph-contract-1", "graph", validGraph()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("graph run accepted"))
                .andExpect(jsonPath("$.data.runId").value("graph-contract-1"))
                .andExpect(jsonPath("$.data.graph.nodes[1].type").value("condition"));
    }

    @Test
    void runEndpointRejectsInvalidGraphContract() throws Exception {
        Map<String, Object> graph = new LinkedHashMap<>(validGraph());
        graph.put("edges", List.of());

        mockMvc.perform(post("/api/graph/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("graph", graph))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    private Map<String, Object> validGraph() {
        Map<String, Object> start = node("start", "start", "start", Map.of());
        Map<String, Object> condition = node("check", "check", "condition", Map.of("expression", "true"));
        Map<String, Object> end = node("end", "end", "end", Map.of());
        return Map.of(
                "id", "graph-contract",
                "name", "Graph Contract",
                "startNodeId", "start",
                "maxIterations", 3,
                "nodes", List.of(start, condition, end),
                "edges", List.of(
                        edge("start-check", "start", "check", "success", null),
                        edge("check-end", "check", "end", "success", null),
                        edge("check-start", "check", "start", "failed", 1)
                ),
                "metadata", Map.of("requiredRepairIterations", 0)
        );
    }

    private Map<String, Object> node(String id, String label, String type, Map<String, Object> config) {
        return Map.of("id", id, "label", label, "type", type, "config", config);
    }

    private Map<String, Object> edge(String id, String source, String target, String condition, Integer maxIterations) {
        Map<String, Object> edge = new LinkedHashMap<>();
        edge.put("id", id);
        edge.put("source", source);
        edge.put("target", target);
        edge.put("condition", condition);
        edge.put("label", condition);
        if (maxIterations != null) {
            edge.put("maxIterations", maxIterations);
        }
        return edge;
    }
}
