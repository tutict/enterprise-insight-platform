package com.tutict.eip.agentadapter.domain;

import jakarta.validation.constraints.NotBlank;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgentExecutionRequest {

    private String model;

    @NotBlank
    private String harnessPrompt;

    @NotBlank
    private String targetPath;

    private Map<String, Object> options = new LinkedHashMap<>();

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getHarnessPrompt() {
        return harnessPrompt;
    }

    public void setHarnessPrompt(String harnessPrompt) {
        this.harnessPrompt = harnessPrompt;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
