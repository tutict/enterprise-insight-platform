package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslFlowEdge;
import com.tutict.eip.harnesscompiler.domain.DslFlowStep;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateEngineTest {

    private final PromptTemplateEngine engine = new PromptTemplateEngine();

    @Test
    void replacesVariablesWithWhitespaceTolerantSyntax() {
        String rendered = engine.render("Hello {{ name }} from {{domain}}", Map.of(
                "name", "Graph Builder",
                "domain", "DSL"
        ));

        assertThat(rendered).isEqualTo("Hello Graph Builder from DSL");
    }

    @Test
    void rendersFlowWithConditionAndLoopGuard() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("model", "llama3.1");
        config.put("prompt", "Generate a service");
        DslFlowStep step = new DslFlowStep(
                "generate",
                "generate",
                "llm",
                config,
                List.of(new DslFlowEdge("verify", "success", "success", 2))
        );

        String rendered = engine.renderFlow(List.of(step));

        assertThat(rendered)
                .contains("1. [llm] generate (generate)")
                .contains("   - model: llama3.1")
                .contains("   - prompt: Generate a service")
                .contains("   - next(success maxIterations=2): verify");
    }
}
