package com.tutict.eip.agentadapter.domain;

public class AgentExecutionResponse {

    private String provider;
    private String model;
    private String targetPath;
    private String absolutePath;
    private String content;
    private int attemptCount;
    private long durationMillis;
    private long bytesWritten;

    public AgentExecutionResponse() {
    }

    public AgentExecutionResponse(
            String provider,
            String model,
            String targetPath,
            String absolutePath,
            String content,
            int attemptCount,
            long durationMillis,
            long bytesWritten
    ) {
        this.provider = provider;
        this.model = model;
        this.targetPath = targetPath;
        this.absolutePath = absolutePath;
        this.content = content;
        this.attemptCount = attemptCount;
        this.durationMillis = durationMillis;
        this.bytesWritten = bytesWritten;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public void setBytesWritten(long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }
}
