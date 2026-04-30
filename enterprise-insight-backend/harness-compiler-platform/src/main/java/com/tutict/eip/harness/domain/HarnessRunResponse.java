package com.tutict.eip.harness.domain;

import java.time.Instant;

public class HarnessRunResponse {

    private String runId;
    private String harnessPrompt;
    private String outputPath;
    private String status;
    private Instant createdAt;

    public HarnessRunResponse() {
    }

    public HarnessRunResponse(String runId, String harnessPrompt, String outputPath, String status, Instant createdAt) {
        this.runId = runId;
        this.harnessPrompt = harnessPrompt;
        this.outputPath = outputPath;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getHarnessPrompt() {
        return harnessPrompt;
    }

    public void setHarnessPrompt(String harnessPrompt) {
        this.harnessPrompt = harnessPrompt;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
