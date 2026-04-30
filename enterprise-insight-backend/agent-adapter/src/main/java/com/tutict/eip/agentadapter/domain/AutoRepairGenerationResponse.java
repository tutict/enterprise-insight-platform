package com.tutict.eip.agentadapter.domain;

import java.util.ArrayList;
import java.util.List;

public class AutoRepairGenerationResponse {

    private boolean successful;
    private String status;
    private String projectRoot;
    private int totalAttempts;
    private String finalOutput;
    private VerificationResult finalVerificationResult;
    private List<AutoRepairAttempt> attempts = new ArrayList<>();

    public AutoRepairGenerationResponse() {
    }

    public AutoRepairGenerationResponse(
            boolean successful,
            String status,
            String projectRoot,
            int totalAttempts,
            String finalOutput,
            VerificationResult finalVerificationResult,
            List<AutoRepairAttempt> attempts
    ) {
        this.successful = successful;
        this.status = status;
        this.projectRoot = projectRoot;
        this.totalAttempts = totalAttempts;
        this.finalOutput = finalOutput;
        this.finalVerificationResult = finalVerificationResult;
        this.attempts = attempts;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public String getFinalOutput() {
        return finalOutput;
    }

    public void setFinalOutput(String finalOutput) {
        this.finalOutput = finalOutput;
    }

    public VerificationResult getFinalVerificationResult() {
        return finalVerificationResult;
    }

    public void setFinalVerificationResult(VerificationResult finalVerificationResult) {
        this.finalVerificationResult = finalVerificationResult;
    }

    public List<AutoRepairAttempt> getAttempts() {
        return attempts;
    }

    public void setAttempts(List<AutoRepairAttempt> attempts) {
        this.attempts = attempts;
    }
}
