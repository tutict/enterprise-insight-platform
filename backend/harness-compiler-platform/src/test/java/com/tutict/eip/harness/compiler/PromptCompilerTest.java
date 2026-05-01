package com.tutict.eip.harness.compiler;

import com.tutict.eip.harness.domain.DSLModel;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptCompilerTest {

    private final PromptCompiler compiler = new DefaultPromptCompiler();

    @Test
    void shouldCompileDslIntoStructuredHarnessPrompt() {
        Map<String, String> constraints = new LinkedHashMap<>();
        constraints.put("language", "Java 17+");
        constraints.put("framework", "Spring Boot 3");
        DSLModel model = new DSLModel(
                "demo",
                "spring-boot-backend",
                "Build a user login system",
                List.of("api", "service", "authentication"),
                constraints,
                "Return complete source files with paths and code blocks"
        );

        String prompt = compiler.compile(model);

        assertThat(prompt).contains("ROLE");
        assertThat(prompt).contains("GOAL");
        assertThat(prompt).contains("MODULES");
        assertThat(prompt).contains("CONSTRAINTS");
        assertThat(prompt).contains("OUTPUT FORMAT");
        assertThat(prompt).contains("Build a user login system");
        assertThat(prompt).contains("- authentication");
        assertThat(prompt).contains("- framework: Spring Boot 3");
    }

    @Test
    void shouldRejectNullDslModel() {
        assertThatThrownBy(() -> compiler.compile(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dslModel must not be null");
    }
}
