package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.CompileResponse;
import com.tutict.eip.harnesscompiler.domain.graph.GraphDefinition;
import com.tutict.eip.harnesscompiler.domain.graph.GraphEdge;
import com.tutict.eip.harnesscompiler.domain.graph.GraphNode;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphPromptCompilerTest {

    private final CompileService compileService = new CompileService(
            requirement -> {
                throw new AssertionError("DSL parser should not be used for graph compilation");
            },
            new DefaultPromptCompiler(),
            new GraphToDslCompiler()
    );

    @Test
    void shouldCompileGraphIntoDslBackedPrompt() {
        GraphDefinition graph = new GraphDefinition();
        graph.setId("graph-1");
        graph.setName("Repair Workflow");
        graph.setStartNodeId("start");
        graph.setNodes(List.of(
                node("start", "start", "start"),
                node("generate", "generate", "llm"),
                node("verify", "verify", "condition"),
                node("repair", "repair", "tool")
        ));
        graph.setEdges(List.of(
                edge("start-generate", "start", "generate", "success", null),
                edge("generate-verify", "generate", "verify", "success", null),
                edge("verify-repair", "verify", "repair", "failed", 2)
        ));

        CompileResponse response = compileService.compileFromGraph(graph);

        assertThat(response.getDsl().getType()).isEqualTo("workflow-graph");
        assertThat(response.getDsl().getFlow()).hasSize(4);
        assertThat(response.getPrompt())
                .contains("# ROLE")
                .contains("# GOAL")
                .contains("# TASK")
                .contains("# DSL")
                .contains("# WORKFLOW")
                .contains("- type: workflow-graph")
                .contains("[llm] generate")
                .contains("next(failed maxIterations=2): repair");
    }

    private GraphNode node(String id, String label, String type) {
        GraphNode node = new GraphNode();
        node.setId(id);
        node.setLabel(label);
        node.setType(type);
        node.setConfig(new LinkedHashMap<>());
        return node;
    }

    private GraphEdge edge(String id, String source, String target, String condition, Integer maxIterations) {
        GraphEdge edge = new GraphEdge();
        edge.setId(id);
        edge.setSource(source);
        edge.setTarget(target);
        edge.setCondition(condition);
        edge.setLabel(condition);
        edge.setMaxIterations(maxIterations);
        return edge;
    }
}
