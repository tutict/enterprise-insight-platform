package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.orchestrator.graph.model.GraphCompileResult;
import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import com.tutict.eip.orchestrator.graph.model.GraphEdge;
import com.tutict.eip.orchestrator.graph.model.GraphNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GraphCompileService {

    private static final Set<String> SUPPORTED_NODE_TYPES = Set.of(
            "start",
            "llm",
            "tool",
            "condition",
            "end",
            "compile",
            "generate",
            "verify",
            "repair"
    );

    public GraphCompileResult compile(GraphDefinition input) {
        GraphDefinition graph = normalize(input);
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        validateNodes(graph, errors, warnings);
        validateEdges(graph, errors, warnings);
        validateStartAndEnd(graph, errors, warnings);
        validateConditionBranches(graph, errors);
        validateLoops(graph, errors, warnings);
        validateReachability(graph, warnings);

        return new GraphCompileResult(errors.isEmpty(), graph, errors, warnings);
    }

    private GraphDefinition normalize(GraphDefinition input) {
        GraphDefinition graph = input == null
                ? DefaultGraphDefinitions.compileGenerateVerifyRepair(2, 1)
                : input;

        if (graph.getId() == null || graph.getId().isBlank()) {
            graph.setId("graph-" + UUID.randomUUID());
        }
        if (graph.getName() == null || graph.getName().isBlank()) {
            graph.setName("Untitled graph");
        }
        if (graph.getMaxIterations() <= 0) {
            graph.setMaxIterations(3);
        }
        if (graph.getNodes() == null) {
            graph.setNodes(new ArrayList<>());
        }
        if (graph.getEdges() == null) {
            graph.setEdges(new ArrayList<>());
        }
        if (graph.getMetadata() == null) {
            graph.setMetadata(new LinkedHashMap<>());
        }

        graph.getNodes().forEach(node -> {
            if (node.getConfig() == null) {
                node.setConfig(new LinkedHashMap<>());
            }
            if (node.getLabel() == null || node.getLabel().isBlank()) {
                node.setLabel(node.getId());
            }
        });

        graph.getEdges().forEach(edge -> {
            if (edge.getId() == null || edge.getId().isBlank()) {
                edge.setId(edge.getSource() + "-" + edge.getTarget());
            }
            if (edge.getCondition() == null || edge.getCondition().isBlank()) {
                edge.setCondition("always");
            }
            if (edge.getLabel() == null || edge.getLabel().isBlank()) {
                edge.setLabel(edge.getCondition());
            }
        });

        if (graph.getStartNodeId() == null || graph.getStartNodeId().isBlank()) {
            graph.getNodes().stream()
                    .filter(node -> "start".equals(node.getType()))
                    .findFirst()
                    .map(GraphNode::getId)
                    .ifPresent(graph::setStartNodeId);
        }

        return graph;
    }

    private void validateNodes(GraphDefinition graph, List<String> errors, List<String> warnings) {
        Set<String> ids = new HashSet<>();
        for (GraphNode node : graph.getNodes()) {
            if (node.getId() == null || node.getId().isBlank()) {
                errors.add("Node id is required.");
                continue;
            }
            if (!ids.add(node.getId())) {
                errors.add("Duplicate node id: " + node.getId());
            }
            if (node.getType() == null || !SUPPORTED_NODE_TYPES.contains(node.getType())) {
                errors.add("Unsupported node type for " + node.getId() + ": " + node.getType());
            }
            validateNodeConfig(node, warnings);
        }
    }

    private void validateNodeConfig(GraphNode node, List<String> warnings) {
        Map<String, Object> config = node.getConfig();
        if ("llm".equals(node.getType())) {
            if (isBlank(config.get("prompt"))) {
                warnings.add("llm node " + node.getId() + " has no prompt.");
            }
            if (isBlank(config.get("model"))) {
                warnings.add("llm node " + node.getId() + " has no model.");
            }
        }
        if ("tool".equals(node.getType()) && isBlank(config.get("url"))) {
            warnings.add("tool node " + node.getId() + " has no API URL.");
        }
        if ("condition".equals(node.getType()) && isBlank(config.get("expression"))) {
            warnings.add("condition node " + node.getId() + " has no expression.");
        }
    }

    private void validateEdges(GraphDefinition graph, List<String> errors, List<String> warnings) {
        Set<String> nodeIds = graph.getNodes().stream().map(GraphNode::getId).collect(Collectors.toSet());
        Set<String> edgeIds = new HashSet<>();
        for (GraphEdge edge : graph.getEdges()) {
            if (!edgeIds.add(edge.getId())) {
                errors.add("Duplicate edge id: " + edge.getId());
            }
            if (!nodeIds.contains(edge.getSource())) {
                errors.add("Edge " + edge.getId() + " references missing source: " + edge.getSource());
            }
            if (!nodeIds.contains(edge.getTarget())) {
                errors.add("Edge " + edge.getId() + " references missing target: " + edge.getTarget());
            }
            if (edge.getMaxIterations() != null && edge.getMaxIterations() <= 0) {
                errors.add("Edge " + edge.getId() + " maxIterations must be greater than zero.");
            }
            if ("failed".equals(edge.getCondition()) && edge.getMaxIterations() == null) {
                warnings.add("Failed branch " + edge.getId() + " has no maxIterations guard.");
            }
        }
    }

    private void validateStartAndEnd(GraphDefinition graph, List<String> errors, List<String> warnings) {
        long startCount = graph.getNodes().stream().filter(node -> "start".equals(node.getType())).count();
        long endCount = graph.getNodes().stream().filter(node -> "end".equals(node.getType())).count();
        Set<String> nodeIds = graph.getNodes().stream().map(GraphNode::getId).collect(Collectors.toSet());

        if (startCount != 1) {
            errors.add("Graph must contain exactly one start node.");
        }
        if (graph.getStartNodeId() == null || graph.getStartNodeId().isBlank()) {
            errors.add("Graph startNodeId is required.");
        } else if (!nodeIds.contains(graph.getStartNodeId())) {
            errors.add("Graph startNodeId references missing node: " + graph.getStartNodeId());
        }
        if (endCount == 0) {
            errors.add("Graph must contain at least one end node.");
        }
    }

    private void validateConditionBranches(GraphDefinition graph, List<String> errors) {
        Map<String, List<GraphEdge>> outgoing = graph.getEdges().stream()
                .collect(Collectors.groupingBy(GraphEdge::getSource));

        graph.getNodes().stream()
                .filter(node -> "condition".equals(node.getType()))
                .forEach(node -> {
                    Set<String> conditions = outgoing.getOrDefault(node.getId(), List.of()).stream()
                            .map(GraphEdge::getCondition)
                            .filter(condition -> condition != null && !condition.isBlank())
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet());

                    if (!conditions.contains("success")) {
                        errors.add("Condition node " + node.getId() + " must define a success branch.");
                    }
                    if (!conditions.contains("failed") && !conditions.contains("failure")) {
                        errors.add("Condition node " + node.getId() + " must define a failed branch.");
                    }
                });
    }

    private void validateLoops(GraphDefinition graph, List<String> errors, List<String> warnings) {
        graph.getEdges().stream()
                .filter(edge -> edge.getMaxIterations() != null)
                .filter(edge -> edge.getMaxIterations() > graph.getMaxIterations())
                .forEach(edge -> warnings.add("Edge " + edge.getId() + " maxIterations exceeds graph maxIterations."));

        Map<String, List<GraphEdge>> dagEdges = graph.getEdges().stream()
                .filter(edge -> edge.getMaxIterations() == null)
                .collect(Collectors.groupingBy(GraphEdge::getSource));
        if (hasCycleAfterRemovingBoundedLoopEdges(graph, dagEdges)) {
            errors.add("Graph must be a DAG after removing edges guarded by maxIterations.");
        }
    }

    private boolean hasCycleAfterRemovingBoundedLoopEdges(GraphDefinition graph, Map<String, List<GraphEdge>> outgoing) {
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();

        for (GraphNode node : graph.getNodes()) {
            if (detectCycle(node.getId(), outgoing, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycle(
            String nodeId,
            Map<String, List<GraphEdge>> outgoing,
            Set<String> visiting,
            Set<String> visited
    ) {
        if (visited.contains(nodeId)) {
            return false;
        }
        if (!visiting.add(nodeId)) {
            return true;
        }

        for (GraphEdge edge : outgoing.getOrDefault(nodeId, List.of())) {
            if (detectCycle(edge.getTarget(), outgoing, visiting, visited)) {
                return true;
            }
        }

        visiting.remove(nodeId);
        visited.add(nodeId);
        return false;
    }

    private void validateReachability(GraphDefinition graph, List<String> warnings) {
        if (graph.getStartNodeId() == null || graph.getStartNodeId().isBlank()) {
            return;
        }

        Map<String, List<GraphEdge>> outgoing = graph.getEdges().stream()
                .collect(Collectors.groupingBy(GraphEdge::getSource));
        Set<String> reachable = new HashSet<>();
        visitReachable(graph.getStartNodeId(), outgoing, reachable);

        graph.getNodes().stream()
                .map(GraphNode::getId)
                .filter(id -> !reachable.contains(id))
                .forEach(id -> warnings.add("Node " + id + " is not reachable from startNodeId."));
    }

    private void visitReachable(String nodeId, Map<String, List<GraphEdge>> outgoing, Set<String> reachable) {
        if (!reachable.add(nodeId)) {
            return;
        }
        for (GraphEdge edge : outgoing.getOrDefault(nodeId, List.of())) {
            visitReachable(edge.getTarget(), outgoing, reachable);
        }
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).isBlank();
    }
}
