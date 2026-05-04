package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.orchestrator.graph.model.GraphCompileResult;
import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import com.tutict.eip.orchestrator.graph.model.GraphEdge;
import com.tutict.eip.orchestrator.graph.model.GraphNode;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphCompileServiceTest {

    private final GraphCompileService service = new GraphCompileService();

    @Test
    void acceptsDagWithBoundedConditionLoop() {
        GraphCompileResult result = service.compile(DefaultGraphDefinitions.compileGenerateVerifyRepair(3, 1));

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void rejectsUnboundedCycle() {
        GraphDefinition graph = graph(
                List.of(
                        node("start", "start"),
                        node("generate", "llm"),
                        condition("verify"),
                        node("repair", "tool"),
                        node("end", "end")
                ),
                List.of(
                        edge("start-generate", "start", "generate", "success", null),
                        edge("generate-verify", "generate", "verify", "success", null),
                        edge("verify-end", "verify", "end", "success", null),
                        edge("verify-repair", "verify", "repair", "failed", null),
                        edge("repair-generate", "repair", "generate", "success", null)
                )
        );

        GraphCompileResult result = service.compile(graph);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Graph must be a DAG after removing edges guarded by maxIterations.");
    }

    @Test
    void requiresConditionSuccessAndFailedBranches() {
        GraphDefinition graph = graph(
                List.of(
                        node("start", "start"),
                        condition("verify"),
                        node("end", "end")
                ),
                List.of(
                        edge("start-verify", "start", "verify", "success", null),
                        edge("verify-end", "verify", "end", "success", null)
                )
        );

        GraphCompileResult result = service.compile(graph);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Condition node verify must define a failed branch.");
    }

    private GraphDefinition graph(List<GraphNode> nodes, List<GraphEdge> edges) {
        GraphDefinition graph = new GraphDefinition();
        graph.setId("test-graph");
        graph.setName("Test graph");
        graph.setStartNodeId("start");
        graph.setMaxIterations(3);
        graph.setNodes(nodes);
        graph.setEdges(edges);
        graph.setMetadata(new LinkedHashMap<>());
        return graph;
    }

    private GraphNode node(String id, String type) {
        return new GraphNode(id, id, type);
    }

    private GraphNode condition(String id) {
        GraphNode node = node(id, "condition");
        node.getConfig().put("expression", "repairIterations >= requiredRepairIterations");
        return node;
    }

    private GraphEdge edge(String id, String source, String target, String condition, Integer maxIterations) {
        GraphEdge edge = new GraphEdge(id, source, target, condition);
        edge.setMaxIterations(maxIterations);
        return edge;
    }
}
