package com.tutict.eip.agentadapter.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "llm")
public class LLMConfig {

    private String modelType = "local";
    private String apiKey;
    private String baseUrl;
    private String model;
    private String localBaseUrl;
    private String localModel;
    private String remoteBaseUrl;
    private String remoteModel;
    private boolean stream = false;
    private Duration requestTimeout = Duration.ofMinutes(2);

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLocalBaseUrl() {
        return localBaseUrl;
    }

    public void setLocalBaseUrl(String localBaseUrl) {
        this.localBaseUrl = localBaseUrl;
    }

    public String getLocalModel() {
        return localModel;
    }

    public void setLocalModel(String localModel) {
        this.localModel = localModel;
    }

    public String getRemoteBaseUrl() {
        return remoteBaseUrl;
    }

    public void setRemoteBaseUrl(String remoteBaseUrl) {
        this.remoteBaseUrl = remoteBaseUrl;
    }

    public String getRemoteModel() {
        return remoteModel;
    }

    public void setRemoteModel(String remoteModel) {
        this.remoteModel = remoteModel;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public boolean isLocal() {
        return "local".equalsIgnoreCase(modelType);
    }

    public boolean isRemote() {
        return "remote".equalsIgnoreCase(modelType);
    }

    public boolean isSmart() {
        return "smart".equalsIgnoreCase(modelType);
    }
}
