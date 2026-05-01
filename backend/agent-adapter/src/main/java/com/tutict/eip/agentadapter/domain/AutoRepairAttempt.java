package com.tutict.eip.agentadapter.domain;

import java.util.ArrayList;
import java.util.List;

public class AutoRepairAttempt {

    private int attemptNumber;
    private boolean successful;
    private String prompt;
    private String generatedOutput;
    private List<GeneratedProjectFile> writtenFiles = new ArrayList<>();
    private VerificationResult verificationResult;

    public AutoRepairAttempt() {
    }

    public AutoRepairAttempt(
            int attemptNumber,
            boolean successful,
            String prompt,
            String generatedOutput,
            List<GeneratedProjectFile> writtenFiles,
            VerificationResult verificationResult
    ) {
        this.attemptNumber = attemptNumber;
        this.successful = successful;
        this.prompt = prompt;
        this.generatedOutput = generatedOutput;
        this.writtenFiles = writtenFiles;
        this.verificationResult = verificationResult;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getGeneratedOutput() {
        return generatedOutput;
    }

    public void setGeneratedOutput(String generatedOutput) {
        this.generatedOutput = generatedOutput;
    }

    public List<GeneratedProjectFile> getWrittenFiles() {
        return writtenFiles;
    }

    public void setWrittenFiles(List<GeneratedProjectFile> writtenFiles) {
        this.writtenFiles = writtenFiles;
    }

    public VerificationResult getVerificationResult() {
        return verificationResult;
    }

    public void setVerificationResult(VerificationResult verificationResult) {
        this.verificationResult = verificationResult;
    }
}
