package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import com.tutict.eip.orchestrator.graph.model.GraphEdge;
import com.tutict.eip.orchestrator.graph.model.GraphNode;

import java.util.LinkedHashMap;
import java.util.List;

public final class DefaultGraphDefinitions {

    private DefaultGraphDefinitions() {
    }

    public static GraphDefinition compileGenerateVerifyRepair(int maxIterations, int requiredRepairIterations) {
        GraphDefinition graph = new GraphDefinition();
        graph.setId("visual-builder-repair-loop");
        graph.setName("Visual Builder Repair Loop");
        graph.setStartNodeId("start");
        graph.setMaxIterations(maxIterations);
        GraphNode start = new GraphNode("start", "start", "start");
        GraphNode generate = new GraphNode("generate", "generate", "llm");
        generate.getConfig().put("model", "llama3.1");
        generate.getConfig().put("prompt", "Generate implementation from upstream context.");

        GraphNode verify = new GraphNode("verify", "verify", "condition");
        verify.getConfig().put("expression", "repairIterations >= requiredRepairIterations");

        GraphNode repair = new GraphNode("repair", "repair", "tool");
        repair.getConfig().put("method", "POST");
        repair.getConfig().put("url", "/api/tool/repair");
        repair.getConfig().put("body", "{}");
        repair.getConfig().put("effect", "repair-loop");

        GraphNode end = new GraphNode("end", "end", "end");

        graph.setNodes(List.of(start, generate, verify, repair, end));

        GraphEdge startGenerate = new GraphEdge("start-generate", "start", "generate", "success");
        GraphEdge generateVerify = new GraphEdge("generate-verify", "generate", "verify", "success");
        GraphEdge verifyEnd = new GraphEdge("verify-end", "verify", "end", "success");
        GraphEdge verifyRepair = new GraphEdge("verify-repair", "verify", "repair", "failed");
        GraphEdge repairGenerate = new GraphEdge("repair-generate", "repair", "generate", "success");
        verifyRepair.setMaxIterations(maxIterations);
        repairGenerate.setMaxIterations(maxIterations);

        graph.setEdges(List.of(startGenerate, generateVerify, verifyEnd, verifyRepair, repairGenerate));
        graph.setMetadata(new LinkedHashMap<>());
        graph.getMetadata().put("requiredRepairIterations", requiredRepairIterations);
        return graph;
    }
}
