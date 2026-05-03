package com.tutict.eip.harnesscompiler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.harnesscompiler.service.CompileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CompileControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<String> capturedRequirement = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        Map<String, String> constraints = new LinkedHashMap<>();
        constraints.put("framework", "Spring Boot 3");
        DslModel dsl = new DslModel(
                "demo",
                "spring-boot-backend",
                "Build a user login system",
                List.of("api", "authentication"),
                constraints,
                "Return complete source files"
        );
        CompileService compileService = new CompileService(
                requirement -> {
                    capturedRequirement.set(requirement);
                    return dsl;
                },
                ignored -> "ROLE\nGOAL\nMODULES\nCONSTRAINTS\nOUTPUT FORMAT"
        );
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new CompileController(compileService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnDslAndPrompt() throws Exception {

        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("requirement", "Build a user login system"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dsl.name").value("demo"))
                .andExpect(jsonPath("$.dsl.modules[1]").value("authentication"))
                .andExpect(jsonPath("$.prompt").value("ROLE\nGOAL\nMODULES\nCONSTRAINTS\nOUTPUT FORMAT"));

        assertThat(capturedRequirement.get()).isEqualTo("Build a user login system");
    }

    @Test
    void shouldRejectBlankRequirement() throws Exception {
        mockMvc.perform(post("/api/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("requirement", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
