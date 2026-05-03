package com.tutict.eip.orchestrator.domain;

import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.harnesscompiler.domain.DslModel;

import java.time.Instant;

public class OrchestratorRunResponse {

    private String runId;
    private DslModel dsl;
    private String harnessPrompt;
    private AutoRepairGenerationResponse generation;
    private Instant createdAt;

    public OrchestratorRunResponse() {
    }

    public OrchestratorRunResponse(
            String runId,
            DslModel dsl,
            String harnessPrompt,
            AutoRepairGenerationResponse generation,
            Instant createdAt
    ) {
        this.runId = runId;
        this.dsl = dsl;
        this.harnessPrompt = harnessPrompt;
        this.generation = generation;
        this.createdAt = createdAt;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public DslModel getDsl() {
        return dsl;
    }

    public void setDsl(DslModel dsl) {
        this.dsl = dsl;
    }

    public String getHarnessPrompt() {
        return harnessPrompt;
    }

    public void setHarnessPrompt(String harnessPrompt) {
        this.harnessPrompt = harnessPrompt;
    }

    public AutoRepairGenerationResponse getGeneration() {
        return generation;
    }

    public void setGeneration(AutoRepairGenerationResponse generation) {
        this.generation = generation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
