package com.tutict.eip.orchestrator.graph.runtime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphEvent {

    @JsonIgnore
    private String eventId;

    private String runId;
    private String type;
    private String nodeId;
    private String edgeId;
    private long timestamp;
    private Map<String, Object> payload = new LinkedHashMap<>();

    public GraphEvent() {
    }

    public GraphEvent(String runId, String type, String nodeId, String edgeId, Map<String, Object> payload) {
        this.runId = runId;
        this.type = type;
        this.nodeId = nodeId;
        this.edgeId = edgeId;
        this.timestamp = Instant.now().getEpochSecond();
        this.payload = payload == null ? new LinkedHashMap<>() : payload;
    }

    public static GraphEvent of(String runId, String type, String nodeId, String edgeId, Map<String, Object> payload) {
        return new GraphEvent(runId, type, nodeId, edgeId, payload);
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(String edgeId) {
        this.edgeId = edgeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
