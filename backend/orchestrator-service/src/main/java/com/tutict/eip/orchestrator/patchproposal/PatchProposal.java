package com.tutict.eip.orchestrator.patchproposal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PatchProposal {

    private String proposalId;
    private String workspaceId;
    private String runId;
    private PatchProposalStatus status;
    private String proposalPath;
    private String verificationSourceRunId;
    private String verificationScope;
    private Boolean verificationSuccessful;
    private String verificationSummary;
    private int changeCount;
    private int rejectedCount;
    private List<PatchProposalFile> files = new ArrayList<>();
    private List<String> risks = new ArrayList<>();
    private Instant generatedAt;

    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public PatchProposalStatus getStatus() {
        return status;
    }

    public void setStatus(PatchProposalStatus status) {
        this.status = status;
    }

    public String getProposalPath() {
        return proposalPath;
    }

    public void setProposalPath(String proposalPath) {
        this.proposalPath = proposalPath;
    }

    public String getVerificationSourceRunId() {
        return verificationSourceRunId;
    }

    public void setVerificationSourceRunId(String verificationSourceRunId) {
        this.verificationSourceRunId = verificationSourceRunId;
    }

    public String getVerificationScope() {
        return verificationScope;
    }

    public void setVerificationScope(String verificationScope) {
        this.verificationScope = verificationScope;
    }

    public Boolean getVerificationSuccessful() {
        return verificationSuccessful;
    }

    public void setVerificationSuccessful(Boolean verificationSuccessful) {
        this.verificationSuccessful = verificationSuccessful;
    }

    public String getVerificationSummary() {
        return verificationSummary;
    }

    public void setVerificationSummary(String verificationSummary) {
        this.verificationSummary = verificationSummary;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public void setChangeCount(int changeCount) {
        this.changeCount = changeCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public List<PatchProposalFile> getFiles() {
        return files;
    }

    public void setFiles(List<PatchProposalFile> files) {
        this.files = files == null ? new ArrayList<>() : files;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks == null ? new ArrayList<>() : risks;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
