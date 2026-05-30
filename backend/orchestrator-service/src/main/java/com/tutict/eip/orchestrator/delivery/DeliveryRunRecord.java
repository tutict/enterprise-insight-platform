package com.tutict.eip.orchestrator.delivery;

import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import com.tutict.eip.orchestrator.runtime.RunEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DeliveryRunRecord {

    private String runId;
    private String workspaceId = "demo-workspace";
    private String playbookId = "compile-generate-verify-repair";
    private String playbookName = "Compile Generate Verify Repair";
    private DeliveryRunStatus status = DeliveryRunStatus.REQUESTED;
    private OrchestratorRunRequest request;
    private OrchestratorRunResponse response;
    private List<RunEvent> events = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getPlaybookId() {
        return playbookId;
    }

    public void setPlaybookId(String playbookId) {
        this.playbookId = playbookId;
    }

    public String getPlaybookName() {
        return playbookName;
    }

    public void setPlaybookName(String playbookName) {
        this.playbookName = playbookName;
    }

    public DeliveryRunStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryRunStatus status) {
        this.status = status;
    }

    public OrchestratorRunRequest getRequest() {
        return request;
    }

    public void setRequest(OrchestratorRunRequest request) {
        this.request = request;
    }

    public OrchestratorRunResponse getResponse() {
        return response;
    }

    public void setResponse(OrchestratorRunResponse response) {
        this.response = response;
    }

    public List<RunEvent> getEvents() {
        return events;
    }

    public void setEvents(List<RunEvent> events) {
        this.events = events == null ? new ArrayList<>() : events;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
