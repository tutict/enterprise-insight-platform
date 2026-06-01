package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import com.tutict.eip.orchestrator.graph.model.GraphEdge;
import com.tutict.eip.orchestrator.graph.model.GraphNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DefaultGraphDefinitions {

    private DefaultGraphDefinitions() {
    }

    public static GraphDefinition compileGenerateVerifyRepair(int maxIterations, int requiredRepairIterations) {
        GraphDefinition graph = new GraphDefinition();
        graph.setId("compile-generate-verify-repair");
        graph.setName("Compile Generate Verify Repair");
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
        graph.getMetadata().put("playbookId", PlaybookTemplateService.DEFAULT_PLAYBOOK_ID);
        graph.getMetadata().put("playbookName", "Compile Generate Verify Repair");
        graph.getMetadata().put("source", "fde-delivery-playbook");
        graph.getMetadata().put("requiredRepairIterations", requiredRepairIterations);
        return graph;
    }

    public static GraphDefinition industryBusinessDiscovery(int maxIterations) {
        GraphDefinition graph = new GraphDefinition();
        graph.setId(PlaybookTemplateService.DISCOVERY_PLAYBOOK_ID);
        graph.setName("Industry And Business Discovery");
        graph.setStartNodeId("start");
        graph.setMaxIterations(maxIterations);

        GraphNode start = withPosition(new GraphNode("start", "start", "start"), 0, 120);

        GraphNode collect = llmNode(
                "collect",
                "collect sources",
                "Collect industry reports, competitor documentation, standards, and existing project context."
        );
        withPosition(collect, 240, 120);

        GraphNode extract = llmNode(
                "extract",
                "extract business signals",
                "Extract actors, workflows, pain points, vocabulary, constraints, and evidence-backed facts."
        );
        withPosition(extract, 520, 120);

        GraphNode model = llmNode(
                "model",
                "model domain",
                "Build a capability map, domain terms, core entities, and system boundary notes."
        );
        withPosition(model, 800, 120);

        GraphNode compare = llmNode(
                "compare",
                "compare gaps",
                "Compare existing project capabilities with market patterns and customer delivery needs."
        );
        withPosition(compare, 1080, 120);

        GraphNode recommend = llmNode(
                "recommend",
                "recommend backlog",
                "Recommend prioritized playbooks, implementation slices, risks, and next delivery records."
        );
        withPosition(recommend, 1360, 120);

        GraphNode verify = new GraphNode("verify-evidence", "verify evidence", "condition");
        verify.getConfig().put("expression", "metadata.evidenceComplete == true");
        withPosition(verify, 1640, 120);

        GraphNode gapFill = new GraphNode("fill-evidence-gaps", "fill evidence gaps", "tool");
        gapFill.getConfig().put("method", "POST");
        gapFill.getConfig().put("url", "/api/tool/discovery/evidence-gap");
        gapFill.getConfig().put("body", "{}");
        gapFill.getConfig().put("effect", "http");
        withPosition(gapFill, 1360, 280);

        GraphNode end = withPosition(new GraphNode("end", "end", "end"), 1920, 120);

        graph.setNodes(List.of(start, collect, extract, model, compare, recommend, verify, gapFill, end));

        GraphEdge startCollect = new GraphEdge("start-collect", "start", "collect", "success");
        GraphEdge collectExtract = new GraphEdge("collect-extract", "collect", "extract", "success");
        GraphEdge extractModel = new GraphEdge("extract-model", "extract", "model", "success");
        GraphEdge modelCompare = new GraphEdge("model-compare", "model", "compare", "success");
        GraphEdge compareRecommend = new GraphEdge("compare-recommend", "compare", "recommend", "success");
        GraphEdge recommendVerify = new GraphEdge("recommend-verify", "recommend", "verify-evidence", "success");
        GraphEdge verifyEnd = new GraphEdge("verify-end", "verify-evidence", "end", "success");
        GraphEdge verifyGapFill = new GraphEdge("verify-gap-fill", "verify-evidence", "fill-evidence-gaps", "failed");
        GraphEdge gapFillCollect = new GraphEdge("gap-fill-collect", "fill-evidence-gaps", "collect", "success");
        verifyGapFill.setMaxIterations(maxIterations);
        gapFillCollect.setMaxIterations(maxIterations);

        graph.setEdges(List.of(
                startCollect,
                collectExtract,
                extractModel,
                modelCompare,
                compareRecommend,
                recommendVerify,
                verifyEnd,
                verifyGapFill,
                gapFillCollect
        ));
        graph.setMetadata(new LinkedHashMap<>());
        graph.getMetadata().put("playbookId", PlaybookTemplateService.DISCOVERY_PLAYBOOK_ID);
        graph.getMetadata().put("playbookName", "Industry And Business Discovery");
        graph.getMetadata().put("source", "fde-discovery-playbook");
        graph.getMetadata().put("phase", "discovery");
        graph.getMetadata().put("evidenceComplete", true);
        return graph;
    }

    private static GraphNode llmNode(String id, String label, String prompt) {
        GraphNode node = new GraphNode(id, label, "llm");
        node.getConfig().put("model", "llama3.1");
        node.getConfig().put("prompt", prompt);
        return node;
    }

    private static GraphNode withPosition(GraphNode node, int x, int y) {
        node.getConfig().put("position", Map.of("x", x, "y", y));
        return node;
    }
}
