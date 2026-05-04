package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslFlowEdge;
import com.tutict.eip.harnesscompiler.domain.DslFlowStep;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.harnesscompiler.domain.graph.GraphDefinition;
import com.tutict.eip.harnesscompiler.domain.graph.GraphEdge;
import com.tutict.eip.harnesscompiler.domain.graph.GraphNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GraphToDslCompiler {

    private final PromptTemplateEngine templateEngine;

    public GraphToDslCompiler() {
        this(new PromptTemplateEngine());
    }

    public GraphToDslCompiler(PromptTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public DslModel compile(GraphDefinition graph) {
        validate(graph);

        Map<String, GraphNode> nodeById = indexNodes(graph.getNodes());
        Map<String, List<GraphEdge>> outgoing = indexOutgoingEdges(graph.getEdges());
        List<GraphNode> orderedNodes = orderNodes(graph, nodeById, outgoing);
        List<DslFlowStep> flow = new ArrayList<>();

        for (GraphNode node : orderedNodes) {
            List<DslFlowEdge> next = outgoing.getOrDefault(node.getId(), List.of()).stream()
                    .map(edge -> new DslFlowEdge(
                            edge.getTarget(),
                            normalize(edge.getCondition(), "always"),
                            normalize(edge.getLabel(), normalize(edge.getCondition(), "always")),
                            edge.getMaxIterations()))
                    .toList();
            flow.add(new DslFlowStep(
                    node.getId(),
                    normalize(node.getLabel(), node.getId()),
                    normalize(node.getType(), "task"),
                    sanitizeConfig(node.getConfig()),
                    next));
        }

        DslModel model = new DslModel();
        model.setName(normalize(graph.getName(), "graph-generated-prompt"));
        model.setType("workflow-graph");
        model.setRole(metadataString(graph, "role", "You are an AI workflow execution agent."));
        model.setGoal(metadataString(
                graph,
                "goal",
                templateEngine.render(
                        "Generate a structured prompt from workflow graph: {{graphName}}",
                        Map.of("graphName", model.getName())
                )
        ));
        model.setTask(metadataString(graph, "task", "Follow the graph definition, execute nodes by edge conditions, and respect loop guards."));
        model.setRequirement(buildRequirement(graph));
        model.setModules(extractModules(orderedNodes));
        model.setFlow(flow);
        model.setConstraints(buildConstraints(graph));
        model.setMetadata(buildMetadata(graph));
        model.setOutputFormat(metadataString(graph, "outputFormat", "Return a structured ROLE/GOAL/TASK prompt and include workflow-aware execution guidance."));
        return model;
    }

    private void validate(GraphDefinition graph) {
        if (graph == null) {
            throw new IllegalArgumentException("graph must not be null");
        }
        if (graph.getNodes() == null || graph.getNodes().isEmpty()) {
            throw new IllegalArgumentException("graph.nodes must not be empty");
        }
    }

    private Map<String, GraphNode> indexNodes(List<GraphNode> nodes) {
        Map<String, GraphNode> indexed = new LinkedHashMap<>();
        for (GraphNode node : nodes) {
            if (node.getId() == null || node.getId().isBlank()) {
                throw new IllegalArgumentException("graph node id must not be blank");
            }
            indexed.put(node.getId(), node);
        }
        return indexed;
    }

    private Map<String, List<GraphEdge>> indexOutgoingEdges(List<GraphEdge> edges) {
        Map<String, List<GraphEdge>> indexed = new LinkedHashMap<>();
        if (edges == null) {
            return indexed;
        }
        for (GraphEdge edge : edges) {
            if (edge.getSource() == null || edge.getTarget() == null) {
                throw new IllegalArgumentException("graph edge source and target must not be null");
            }
            indexed.computeIfAbsent(edge.getSource(), ignored -> new ArrayList<>()).add(edge);
        }
        return indexed;
    }

    private List<GraphNode> orderNodes(
            GraphDefinition graph,
            Map<String, GraphNode> nodeById,
            Map<String, List<GraphEdge>> outgoing
    ) {
        String startNodeId = normalize(graph.getStartNodeId(), graph.getNodes().get(0).getId());
        List<GraphNode> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        visit(startNodeId, nodeById, outgoing, visited, ordered);
        for (GraphNode node : graph.getNodes()) {
            visit(node.getId(), nodeById, outgoing, visited, ordered);
        }
        return ordered;
    }

    private void visit(
            String nodeId,
            Map<String, GraphNode> nodeById,
            Map<String, List<GraphEdge>> outgoing,
            Set<String> visited,
            List<GraphNode> ordered
    ) {
        if (nodeId == null || visited.contains(nodeId)) {
            return;
        }
        GraphNode node = nodeById.get(nodeId);
        if (node == null) {
            return;
        }

        visited.add(nodeId);
        ordered.add(node);
        for (GraphEdge edge : outgoing.getOrDefault(nodeId, List.of())) {
            visit(edge.getTarget(), nodeById, outgoing, visited, ordered);
        }
    }

    private Map<String, Object> sanitizeConfig(Map<String, Object> config) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        if (config == null) {
            return sanitized;
        }
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            if (!"position".equals(entry.getKey())) {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }

    private List<String> extractModules(List<GraphNode> nodes) {
        Set<String> modules = new LinkedHashSet<>();
        modules.add("graph-runtime");
        modules.add("prompt-generation");
        for (GraphNode node : nodes) {
            modules.add(normalize(node.getType(), "task"));
        }
        return new ArrayList<>(modules);
    }

    private Map<String, String> buildConstraints(GraphDefinition graph) {
        Map<String, String> constraints = new LinkedHashMap<>();
        constraints.put("promptStructure", "Must include ROLE, GOAL, TASK, WORKFLOW, CONSTRAINTS, OUTPUT sections");
        constraints.put("flowExecution", "Respect graph edge conditions and node order derived from startNodeId");
        constraints.put("loopGuard", templateEngine.render(
                "Do not exceed graph maxIterations={{maxIterations}}",
                Map.of("maxIterations", graph.getMaxIterations())
        ));
        constraints.put("extensibility", "Preserve node config and edge metadata for future node types");
        return constraints;
    }

    private Map<String, Object> buildMetadata(GraphDefinition graph) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (graph.getMetadata() != null) {
            metadata.putAll(graph.getMetadata());
        }
        metadata.put("sourceGraphId", graph.getId());
        metadata.put("startNodeId", graph.getStartNodeId());
        metadata.put("nodeCount", graph.getNodes().size());
        metadata.put("edgeCount", graph.getEdges() == null ? 0 : graph.getEdges().size());
        return metadata;
    }

    private String buildRequirement(GraphDefinition graph) {
        return templateEngine.render(
                "Compile workflow graph '{{graphName}}' into an execution-ready prompt.",
                Map.of("graphName", normalize(graph.getName(), graph.getId()))
        );
    }

    private String metadataString(GraphDefinition graph, String key, String fallback) {
        if (graph.getMetadata() == null) {
            return fallback;
        }
        Object value = graph.getMetadata().get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return String.valueOf(value);
    }

    private String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
