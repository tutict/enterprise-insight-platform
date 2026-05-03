package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DslParserTest {

    private final DslParser parser = new RuleBasedDslParser();

    @Test
    void shouldParseLoginRequirementIntoDslModel() {
        DslModel model = parser.parse("Build a Spring Boot login system with user management and database persistence");

        assertThat(model.getName()).isEqualTo("ai-harness-generated-system");
        assertThat(model.getType()).isEqualTo("spring-boot-backend");
        assertThat(model.getRequirement()).contains("login");
        assertThat(model.getModules())
                .contains("api", "service", "domain", "authentication", "persistence");
        assertThat(model.getConstraints())
                .containsEntry("language", "Java 17+")
                .containsEntry("framework", "Spring Boot 3");
        assertThat(model.getOutputFormat()).contains("complete source files");
    }

    @Test
    void shouldRejectBlankRequirement() {
        assertThatThrownBy(() -> parser.parse(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requirement must not be blank");
    }
}
