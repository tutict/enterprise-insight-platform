package com.tutict.eip.orchestrator.runtime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RunEvent {

    @JsonIgnore
    private String eventId;

    private String runId;
    private String type;
    private String step;
    private long timestamp;
    private Map<String, Object> payload;

    public RunEvent() {
    }

    public RunEvent(String runId, String type, String step, Map<String, Object> payload) {
        this.runId = runId;
        this.type = type;
        this.step = step;
        this.timestamp = Instant.now().getEpochSecond();
        this.payload = payload == null ? new LinkedHashMap<>() : payload;
    }

    public static RunEvent of(String runId, String type) {
        return new RunEvent(runId, type, null, new LinkedHashMap<>());
    }

    public static RunEvent of(String runId, String type, String step) {
        return new RunEvent(runId, type, step, new LinkedHashMap<>());
    }

    public static RunEvent of(String runId, String type, String step, Map<String, Object> payload) {
        return new RunEvent(runId, type, step, payload);
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

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
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
