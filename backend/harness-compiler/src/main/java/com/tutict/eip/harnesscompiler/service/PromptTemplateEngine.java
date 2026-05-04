package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslFlowEdge;
import com.tutict.eip.harnesscompiler.domain.DslFlowStep;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptTemplateEngine {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");
    private static final String LIST_ITEM_TEMPLATE = "- {{value}}";
    private static final String KEY_VALUE_TEMPLATE = "- {{key}}: {{value}}";
    private static final String EMPTY_LIST_TEMPLATE = "- none";
    private static final String EMPTY_FLOW_TEMPLATE = "1. {{task}}";
    private static final String FLOW_STEP_TEMPLATE = "{{index}}. [{{type}}] {{label}} ({{id}})";
    private static final String FLOW_CONFIG_TEMPLATE = "   - {{key}}: {{value}}";
    private static final String FLOW_TOOL_TEMPLATE = "   - tool: {{method}} {{url}}";
    private static final String FLOW_EDGE_TEMPLATE = "   - next({{condition}}{{loopGuard}}): {{target}}";

    public String render(String template, Map<String, ?> variables) {
        if (template == null || template.isBlank()) {
            throw new IllegalArgumentException("template must not be blank");
        }

        Map<String, ?> context = variables == null ? Map.of() : variables;
        Matcher matcher = VARIABLE.matcher(template);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            Object value = context.get(matcher.group(1));
            matcher.appendReplacement(output, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public String renderList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return render(EMPTY_LIST_TEMPLATE, Map.of());
        }

        StringJoiner joiner = new StringJoiner("\n");
        for (String value : values) {
            joiner.add(render(LIST_ITEM_TEMPLATE, Map.of("value", safe(value))));
        }
        return joiner.toString();
    }

    public String renderKeyValues(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return render(EMPTY_LIST_TEMPLATE, Map.of());
        }

        StringJoiner joiner = new StringJoiner("\n");
        for (Map.Entry<String, String> entry : values.entrySet()) {
            joiner.add(render(KEY_VALUE_TEMPLATE, Map.of(
                    "key", safe(entry.getKey()),
                    "value", safe(entry.getValue())
            )));
        }
        return joiner.toString();
    }

    public String renderFlow(List<DslFlowStep> flow) {
        if (flow == null || flow.isEmpty()) {
            return render(EMPTY_FLOW_TEMPLATE, Map.of("task", "Execute the requested task as a single step."));
        }

        StringJoiner joiner = new StringJoiner("\n");
        int index = 1;
        for (DslFlowStep step : flow) {
            joiner.add(render(FLOW_STEP_TEMPLATE, Map.of(
                    "index", index,
                    "type", safe(step.getType()),
                    "label", safe(step.getLabel()),
                    "id", safe(step.getId())
            )));
            renderConfig(step, joiner);
            renderNext(step.getNext(), joiner);
            index++;
        }
        return joiner.toString();
    }

    private void renderConfig(DslFlowStep step, StringJoiner joiner) {
        Map<String, Object> config = step.getConfig();
        if (config == null || config.isEmpty()) {
            return;
        }

        Object model = config.get("model");
        if (model != null) {
            joiner.add(renderConfigLine("model", model));
        }

        Object prompt = config.get("prompt");
        if (prompt != null && !String.valueOf(prompt).isBlank()) {
            joiner.add(renderConfigLine("prompt", prompt));
        }

        Object expression = config.get("expression");
        if (expression != null && !String.valueOf(expression).isBlank()) {
            joiner.add(renderConfigLine("condition", expression));
        }

        Object url = config.get("url");
        if (url != null && !String.valueOf(url).isBlank()) {
            Object method = config.getOrDefault("method", "POST");
            joiner.add(render(FLOW_TOOL_TEMPLATE, Map.of(
                    "method", safe(method),
                    "url", safe(url)
            )));
        }
    }

    private String renderConfigLine(String key, Object value) {
        return render(FLOW_CONFIG_TEMPLATE, Map.of(
                "key", safe(key),
                "value", safe(value)
        ));
    }

    private void renderNext(List<DslFlowEdge> edges, StringJoiner joiner) {
        if (edges == null || edges.isEmpty()) {
            joiner.add("   - next: none");
            return;
        }

        for (DslFlowEdge edge : edges) {
            String condition = edge.getCondition() == null || edge.getCondition().isBlank()
                    ? "always"
                    : edge.getCondition();
            String loopGuard = edge.getMaxIterations() == null
                    ? ""
                    : " maxIterations=" + edge.getMaxIterations();
            joiner.add(render(FLOW_EDGE_TEMPLATE, Map.of(
                    "condition", safe(condition),
                    "loopGuard", loopGuard,
                    "target", safe(edge.getTarget())
            )));
        }
    }

    private String safe(Object value) {
        return PromptInputSanitizer.sanitizeUntrustedText(value);
    }
}
