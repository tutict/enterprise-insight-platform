package com.tutict.eip.agentadapter.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class OllamaGenerateRequest {

    private String model;
    private String prompt;
    private boolean stream;
    private Map<String, Object> options = new LinkedHashMap<>();

    public OllamaGenerateRequest() {
    }

    public OllamaGenerateRequest(String model, String prompt, boolean stream, Map<String, Object> options) {
        this.model = model;
        this.prompt = prompt;
        this.stream = stream;
        this.options = options == null ? new LinkedHashMap<>() : options;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
