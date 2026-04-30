package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.exception.PromptCompilerException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringPromptTemplateRegistryTest {

    @Test
    void resolveDefaultHarnessTemplateWhenNameIsBlank() {
        HarnessPromptTemplate harnessPromptTemplate = new HarnessPromptTemplate();
        SpringPromptTemplateRegistry registry = new SpringPromptTemplateRegistry(List.of(harnessPromptTemplate));

        PromptTemplate template = registry.resolve("");

        assertThat(template.name()).isEqualTo("harness-default");
    }

    @Test
    void rejectUnknownTemplate() {
        SpringPromptTemplateRegistry registry = new SpringPromptTemplateRegistry(List.of(new HarnessPromptTemplate()));

        assertThatThrownBy(() -> registry.resolve("missing-template"))
                .isInstanceOf(PromptCompilerException.class)
                .hasMessageContaining("Prompt template not found");
    }
}
