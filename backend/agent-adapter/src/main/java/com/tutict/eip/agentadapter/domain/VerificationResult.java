package com.tutict.eip.agentadapter.domain;

import java.util.ArrayList;
import java.util.List;

public class VerificationResult {

    private boolean successful;
    private String summary;
    private List<VerificationCommandResult> commandResults = new ArrayList<>();

    public VerificationResult() {
    }

    public VerificationResult(boolean successful, String summary, List<VerificationCommandResult> commandResults) {
        this.successful = successful;
        this.summary = summary;
        this.commandResults = commandResults;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<VerificationCommandResult> getCommandResults() {
        return commandResults;
    }

    public void setCommandResults(List<VerificationCommandResult> commandResults) {
        this.commandResults = commandResults;
    }
}
