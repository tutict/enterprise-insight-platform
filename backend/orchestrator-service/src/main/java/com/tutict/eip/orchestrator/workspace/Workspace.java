package com.tutict.eip.orchestrator.workspace;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Workspace {

    private String workspaceId;
    private String customerName;
    private String projectName;
    private String repoRoot;
    private String defaultBranch;
    private String workingBranch;
    private String worktreePath;
    private List<String> allowedPaths = new ArrayList<>();
    private List<List<String>> verifyCommands = new ArrayList<>();
    private String modelPolicy = "default";
    private String secretPolicy = "redact-known-sensitive-fields";
    private Instant createdAt;
    private Instant updatedAt;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRepoRoot() {
        return repoRoot;
    }

    public void setRepoRoot(String repoRoot) {
        this.repoRoot = repoRoot;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public String getWorkingBranch() {
        return workingBranch;
    }

    public void setWorkingBranch(String workingBranch) {
        this.workingBranch = workingBranch;
    }

    public String getWorktreePath() {
        return worktreePath;
    }

    public void setWorktreePath(String worktreePath) {
        this.worktreePath = worktreePath;
    }

    public List<String> getAllowedPaths() {
        return allowedPaths;
    }

    public void setAllowedPaths(List<String> allowedPaths) {
        this.allowedPaths = allowedPaths == null ? new ArrayList<>() : allowedPaths;
    }

    public List<List<String>> getVerifyCommands() {
        return verifyCommands;
    }

    public void setVerifyCommands(List<List<String>> verifyCommands) {
        this.verifyCommands = verifyCommands == null ? new ArrayList<>() : verifyCommands;
    }

    public String getModelPolicy() {
        return modelPolicy;
    }

    public void setModelPolicy(String modelPolicy) {
        this.modelPolicy = modelPolicy;
    }

    public String getSecretPolicy() {
        return secretPolicy;
    }

    public void setSecretPolicy(String secretPolicy) {
        this.secretPolicy = secretPolicy;
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
