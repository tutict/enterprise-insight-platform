package com.tutict.eip.harness.agent;

public class AgentExecutionResult {

    private String outputPath;
    private String status;

    public AgentExecutionResult() {
    }

    public AgentExecutionResult(String outputPath, String status) {
        this.outputPath = outputPath;
        this.status = status;
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
}
