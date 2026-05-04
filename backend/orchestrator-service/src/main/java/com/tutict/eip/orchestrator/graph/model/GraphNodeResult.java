package com.tutict.eip.orchestrator.graph.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphNodeResult {

    private boolean successful;
    private String status;
    private Map<String, Object> payload = new LinkedHashMap<>();

    public GraphNodeResult() {
    }

    public GraphNodeResult(boolean successful, String status, Map<String, Object> payload) {
        this.successful = successful;
        this.status = status;
        this.payload = payload;
    }

    public static GraphNodeResult success(String status, Map<String, Object> payload) {
        return new GraphNodeResult(true, status, payload);
    }

    public static GraphNodeResult fail(String status, Map<String, Object> payload) {
        return new GraphNodeResult(false, status, payload);
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

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
