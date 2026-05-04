package com.tutict.eip.orchestrator.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrchestratorRunRequest {

    private String runId;

    @NotBlank
    private String requirement;

    private String model;

    @NotBlank
    private String targetDirectory;

    @NotEmpty
    private List<List<String>> verifyCommands = new ArrayList<>();

    @Min(0)
    @Max(5)
    private int maxRepairRounds = 2;

    private Map<String, Object> options = new LinkedHashMap<>();

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public List<List<String>> getVerifyCommands() {
        return verifyCommands;
    }

    public void setVerifyCommands(List<List<String>> verifyCommands) {
        this.verifyCommands = verifyCommands;
    }

    public int getMaxRepairRounds() {
        return maxRepairRounds;
    }

    public void setMaxRepairRounds(int maxRepairRounds) {
        this.maxRepairRounds = maxRepairRounds;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
