package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import com.tutict.eip.orchestrator.graph.model.GraphEdge;
import com.tutict.eip.orchestrator.graph.model.GraphNode;
import com.tutict.eip.orchestrator.graph.model.GraphNodeResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GraphExecutor {

    private static final int HARD_NODE_VISIT_LIMIT = 200;

    private final GraphEventStreamService streamService;

    public GraphExecutor(GraphEventStreamService streamService) {
        this.streamService = streamService;
    }

    public void executeAsync(String runId, GraphDefinition graph) {
        CompletableFuture.runAsync(() -> execute(runId, graph));
    }

    public void execute(String runId, GraphDefinition graph) {
        Map<String, GraphNode> nodesById = graph.getNodes().stream()
                .collect(Collectors.toMap(GraphNode::getId, Function.identity(), (left, right) -> right, LinkedHashMap::new));
        Map<String, List<GraphEdge>> outgoingEdges = graph.getEdges().stream()
                .collect(Collectors.groupingBy(GraphEdge::getSource, LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));
        Map<String, Integer> edgeIterations = new HashMap<>();
        GraphExecutionContext context = new GraphExecutionContext(graph);
        String currentNodeId = graph.getStartNodeId();
        int visits = 0;

        streamService.emit(GraphEvent.of(runId, "GRAPH_RUN_STARTED", null, null, payload(
                "definition", graph
        )));

        while (currentNodeId != null) {
            if (++visits > HARD_NODE_VISIT_LIMIT) {
                streamService.emit(GraphEvent.of(runId, "GRAPH_RUN_FAILED", currentNodeId, null, payload(
                        "error", "Graph execution exceeded hard node visit limit."
                )));
                return;
            }

            GraphNode node = nodesById.get(currentNodeId);
            if (node == null) {
                streamService.emit(GraphEvent.of(runId, "GRAPH_RUN_FAILED", currentNodeId, null, payload(
                        "error", "Graph node not found: " + currentNodeId
                )));
                return;
            }

            streamService.emit(GraphEvent.of(runId, "NODE_STARTED", node.getId(), null, payload("node", node)));
            GraphNodeResult result = executeNode(node, context);
            context.recordNodeResult(node.getId(), result);
            streamService.emit(GraphEvent.of(
                    runId,
                    result.isSuccessful() ? "NODE_SUCCEEDED" : "NODE_FAILED",
                    node.getId(),
                    null,
                    payload(
                            "status", result.getStatus(),
                            "result", result.getPayload()
                    )
            ));

            List<GraphEdge> outgoing = outgoingEdges.getOrDefault(node.getId(), List.of());
            GraphEdge nextEdge = selectNextEdge(outgoing, result, edgeIterations);

            if (nextEdge == null) {
                if (outgoing.stream().anyMatch(edge -> edge.matches(result))) {
                    streamService.emit(GraphEvent.of(runId, "GRAPH_RUN_FAILED", node.getId(), null, payload(
                            "error", "Loop maxIterations exhausted.",
                            "status", "loop_exhausted",
                            "result", result.getPayload()
                    )));
                    return;
                }

                streamService.emit(GraphEvent.of(
                        runId,
                        result.isSuccessful() ? "GRAPH_RUN_COMPLETED" : "GRAPH_RUN_FAILED",
                        node.getId(),
                        null,
                        payload(
                                "status", result.getStatus(),
                                "result", result.getPayload()
                        )
                ));
                return;
            }

            int iteration = edgeIterations.merge(nextEdge.getId(), 1, Integer::sum);
            streamService.emit(GraphEvent.of(runId, "EDGE_TRAVERSED", node.getId(), nextEdge.getId(), payload(
                    "edge", nextEdge,
                    "condition", nextEdge.getCondition(),
                    "iteration", iteration
            )));
            currentNodeId = nextEdge.getTarget();
        }

        streamService.emit(GraphEvent.of(runId, "GRAPH_RUN_COMPLETED", null, null, payload()));
    }

    private GraphEdge selectNextEdge(
            List<GraphEdge> edges,
            GraphNodeResult result,
            Map<String, Integer> edgeIterations
    ) {
        return edges.stream()
                .sorted(Comparator
                        .comparing((GraphEdge edge) -> edgePriority(edge, result))
                        .thenComparing(GraphEdge::getId))
                .filter(edge -> edge.matches(result))
                .filter(edge -> {
                    Integer maxIterations = edge.getMaxIterations();
                    return maxIterations == null || edgeIterations.getOrDefault(edge.getId(), 0) < maxIterations;
                })
                .findFirst()
                .orElse(null);
    }

    private int edgePriority(GraphEdge edge, GraphNodeResult result) {
        String condition = edge.getCondition();
        if (condition == null || condition.isBlank() || "always".equalsIgnoreCase(condition)) {
            return 3;
        }
        if (condition.equalsIgnoreCase(result.getStatus())) {
            return 0;
        }
        if ("success".equalsIgnoreCase(condition) || "failed".equalsIgnoreCase(condition) || "failure".equalsIgnoreCase(condition)) {
            return 1;
        }
        return 2;
    }

    private GraphNodeResult executeNode(GraphNode node, GraphExecutionContext context) {
        return switch (node.getType()) {
            case "start" -> GraphNodeResult.success("started", payload(
                    "detail", "Graph execution started."
            ));
            case "llm" -> GraphNodeResult.success("llm_completed", payload(
                    "detail", "LLM node completed.",
                    "model", node.getConfig().get("model"),
                    "prompt", node.getConfig().get("prompt")
            ));
            case "tool" -> executeTool(node, context);
            case "condition" -> executeCondition(node, context);
            case "end" -> GraphNodeResult.success("ended", payload(
                    "detail", "Graph execution reached end."
            ));
            case "compile" -> GraphNodeResult.success("compiled", payload(
                    "detail", "DSL compiled into execution prompt."
            ));
            case "generate" -> GraphNodeResult.success("generated", payload(
                    "detail", "Generation completed.",
                    "repairIterations", context.getRepairIterations()
            ));
            case "verify" -> executeVerify(context);
            case "repair" -> executeRepair(context);
            default -> GraphNodeResult.success("completed", payload(
                    "detail", "Node completed.",
                    "type", node.getType()
            ));
        };
    }

    private GraphNodeResult executeTool(GraphNode node, GraphExecutionContext context) {
        Map<String, Object> resultPayload = payload(
                "detail", "Tool node completed.",
                "method", node.getConfig().get("method"),
                "url", node.getConfig().get("url")
        );

        Object effect = node.getConfig().get("effect");
        if (effect != null && "repair-loop".equalsIgnoreCase(String.valueOf(effect))) {
            int repairIterations = context.incrementRepairIterations();
            resultPayload.put("repairIterations", repairIterations);
            resultPayload.put("detail", "Repair loop feedback prepared.");
        }

        return GraphNodeResult.success("tool_completed", resultPayload);
    }

    private GraphNodeResult executeCondition(GraphNode node, GraphExecutionContext context) {
        String expression = String.valueOf(node.getConfig().getOrDefault("expression", "")).trim();
        boolean passed = expression.isBlank()
                ? asBoolean(node.getConfig().getOrDefault("pass", true))
                : evaluateExpression(expression, context);

        if (passed) {
            return GraphNodeResult.success("success", payload(
                    "detail", "Condition branch evaluated to success.",
                    "expression", node.getConfig().get("expression")
            ));
        }

        return GraphNodeResult.fail("failed", payload(
                "detail", "Condition branch evaluated to failed.",
                "expression", node.getConfig().get("expression")
        ));
    }

    private boolean evaluateExpression(String expression, GraphExecutionContext context) {
        String normalized = expression.trim();
        if ("true".equalsIgnoreCase(normalized)) {
            return true;
        }
        if ("false".equalsIgnoreCase(normalized)) {
            return false;
        }

        for (String operator : List.of("==", "!=", ">=", "<=", ">", "<")) {
            int index = normalized.indexOf(operator);
            if (index > 0) {
                Object left = resolveOperand(normalized.substring(0, index).trim(), context);
                Object right = resolveOperand(normalized.substring(index + operator.length()).trim(), context);
                return compare(left, right, operator);
            }
        }

        return asBoolean(resolveOperand(normalized, context));
    }

    private Object resolveOperand(String token, GraphExecutionContext context) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String value = token.trim();
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        if ("null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            // Fall through to runtime context lookup.
        }

        return context.resolve(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean compare(Object left, Object right, String operator) {
        if ("==".equals(operator)) {
            if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
                return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue()) == 0;
            }
            return left == null ? right == null : left.equals(right);
        }
        if ("!=".equals(operator)) {
            if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
                return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue()) != 0;
            }
            return left == null ? right != null : !left.equals(right);
        }

        int comparison;
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            comparison = Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        } else if (left instanceof Comparable comparable && right != null) {
            comparison = comparable.compareTo(String.valueOf(right));
        } else {
            comparison = String.valueOf(left).compareTo(String.valueOf(right));
        }

        return switch (operator) {
            case ">" -> comparison > 0;
            case "<" -> comparison < 0;
            case ">=" -> comparison >= 0;
            case "<=" -> comparison <= 0;
            default -> false;
        };
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.doubleValue() != 0D;
        }
        if (value instanceof String text) {
            String normalized = text.trim().toLowerCase(Locale.ROOT);
            return "true".equals(normalized) || "success".equals(normalized) || "passed".equals(normalized);
        }
        return value != null;
    }

    private GraphNodeResult executeVerify(GraphExecutionContext context) {
        if (context.getRepairIterations() >= context.getRequiredRepairIterations()) {
            return GraphNodeResult.success("verified", payload(
                    "detail", "Verification passed.",
                    "repairIterations", context.getRepairIterations()
            ));
        }

        return GraphNodeResult.fail("verification_failed", payload(
                "detail", "Verification failed and requires repair.",
                "repairIterations", context.getRepairIterations()
        ));
    }

    private GraphNodeResult executeRepair(GraphExecutionContext context) {
        int nextIteration = context.incrementRepairIterations();
        if (nextIteration > context.getMaxIterations()) {
            return GraphNodeResult.fail("repair_exhausted", payload(
                    "detail", "Repair loop exhausted.",
                    "repairIterations", nextIteration
            ));
        }

        return GraphNodeResult.success("repair_prepared", payload(
                "detail", "Repair feedback prepared.",
                "repairIterations", nextIteration
        ));
    }

    private Map<String, Object> payload(Object... pairs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            Object value = pairs[index + 1];
            if (value != null) {
                payload.put(String.valueOf(pairs[index]), value);
            }
        }
        return payload;
    }

    private static final class GraphExecutionContext {
        private final GraphDefinition graph;
        private final int requiredRepairIterations;
        private final Map<String, GraphNodeResult> nodeResults = new LinkedHashMap<>();
        private int repairIterations;
        private GraphNodeResult lastResult;

        private GraphExecutionContext(GraphDefinition graph) {
            this.graph = graph;
            this.requiredRepairIterations = getInt(graph.getMetadata().get("requiredRepairIterations"), 1);
        }

        private void recordNodeResult(String nodeId, GraphNodeResult result) {
            nodeResults.put(nodeId, result);
            lastResult = result;
        }

        private int getRepairIterations() {
            return repairIterations;
        }

        private int incrementRepairIterations() {
            repairIterations += 1;
            return repairIterations;
        }

        private int getRequiredRepairIterations() {
            return requiredRepairIterations;
        }

        private int getMaxIterations() {
            return Math.max(graph.getMaxIterations(), 0);
        }

        private Object resolve(String token) {
            if ("repairIterations".equals(token)) {
                return repairIterations;
            }
            if ("requiredRepairIterations".equals(token)) {
                return requiredRepairIterations;
            }
            if ("maxIterations".equals(token)) {
                return getMaxIterations();
            }
            if (token.startsWith("metadata.")) {
                return resolveMapValue(graph.getMetadata(), token.substring("metadata.".length()));
            }
            if (token.startsWith("last.")) {
                return resolveResultValue(lastResult, token.substring("last.".length()));
            }

            int separator = token.indexOf('.');
            if (separator > 0) {
                String nodeId = token.substring(0, separator);
                String path = token.substring(separator + 1);
                GraphNodeResult result = nodeResults.get(nodeId);
                if (result != null) {
                    return resolveResultValue(result, path);
                }
                if ("successful".equals(path) || path.startsWith("payload.") || "status".equals(path)) {
                    return resolveResultValue(lastResult, path);
                }
            }

            return graph.getMetadata().get(token);
        }

        private Object resolveResultValue(GraphNodeResult result, String path) {
            if (result == null) {
                return null;
            }
            if ("successful".equals(path)) {
                return result.isSuccessful();
            }
            if ("status".equals(path)) {
                return result.getStatus();
            }
            if (path.startsWith("payload.")) {
                return resolveMapValue(result.getPayload(), path.substring("payload.".length()));
            }
            return result.getPayload().get(path);
        }

        @SuppressWarnings("unchecked")
        private Object resolveMapValue(Map<String, Object> values, String path) {
            Object current = values;
            for (String part : path.split("\\.")) {
                if (!(current instanceof Map<?, ?> map)) {
                    return null;
                }
                current = ((Map<String, Object>) map).get(part);
            }
            return current;
        }

        private int getInt(Object value, int fallback) {
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value instanceof String text) {
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException ignored) {
                    return fallback;
                }
            }
            return fallback;
        }
    }
}
