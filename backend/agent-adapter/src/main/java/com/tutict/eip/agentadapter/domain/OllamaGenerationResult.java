package com.tutict.eip.agentadapter.domain;

public class OllamaGenerationResult {

    private final String model;
    private final String content;
    private final int attemptCount;
    private final long durationMillis;

    public OllamaGenerationResult(String model, String content, int attemptCount, long durationMillis) {
        this.model = model;
        this.content = content;
        this.attemptCount = attemptCount;
        this.durationMillis = durationMillis;
    }

    public String getModel() {
        return model;
    }

    public String getContent() {
        return content;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public long getDurationMillis() {
        return durationMillis;
    }
}
