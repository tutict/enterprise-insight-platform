package com.tutict.eip.harness.service;

import com.tutict.eip.harness.domain.DSLModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DSLParserTest {

    private final DSLParser parser = new RuleBasedDSLParser();

    @Test
    void shouldParseLoginRequirementIntoDslModel() {
        DSLModel model = parser.parse("做一个带登录、用户管理和数据库持久化的Spring Boot系统");

        assertThat(model.getName()).isEqualTo("ai-harness-generated-system");
        assertThat(model.getType()).isEqualTo("spring-boot-backend");
        assertThat(model.getRequirement()).contains("登录");
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
