package com.tutict.eip.orchestrator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import com.tutict.eip.orchestrator.service.OrchestratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrchestratorControllerContractTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        OrchestratorService service = request -> new OrchestratorRunResponse(
                request.getRunId(),
                new DslModel("demo", "spring-boot-backend", request.getRequirement(), List.of("api"), new LinkedHashMap<>(), "files"),
                "compiled prompt",
                new AutoRepairGenerationResponse(true, "VERIFIED", "agent-output/demo", 1, "files", null, List.of()),
                Instant.parse("2026-05-04T00:00:00Z")
        );
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrchestratorController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void runEndpointReturnsStableApiEnvelope() throws Exception {
        mockMvc.perform(post("/api/orchestrator/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "runId", "contract-run-1",
                                "requirement", "Build login API",
                                "targetDirectory", "contract-demo",
                                "verifyCommands", List.of(List.of("mvn", "test")),
                                "maxRepairRounds", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.message").value("orchestration completed"))
                .andExpect(jsonPath("$.data.runId").value("contract-run-1"))
                .andExpect(jsonPath("$.data.dsl.modules[0]").value("api"))
                .andExpect(jsonPath("$.data.harnessPrompt").value("compiled prompt"))
                .andExpect(jsonPath("$.data.generation.status").value("VERIFIED"));
    }

    @Test
    void runEndpointRejectsInvalidContractPayload() throws Exception {
        mockMvc.perform(post("/api/orchestrator/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "requirement", "",
                                "targetDirectory", "contract-demo",
                                "verifyCommands", List.of(List.of("mvn", "test"))
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
