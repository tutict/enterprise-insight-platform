package com.tutict.eip.orchestrator.workspace;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceRequest {

    @NotBlank
    private String workspaceId;

    @NotBlank
    private String customerName;

    @NotBlank
    private String projectName;

    @NotBlank
    private String repoRoot;

    private String defaultBranch = "main";
    private String workingBranch;
    private String worktreePath;
    private List<String> allowedPaths = new ArrayList<>(List.of("."));
    private List<List<String>> verifyCommands = new ArrayList<>(List.of(List.of("mvn", "test")));
    private String modelPolicy = "default";
    private String secretPolicy = "redact-known-sensitive-fields";

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
        this.allowedPaths = allowedPaths;
    }

    public List<List<String>> getVerifyCommands() {
        return verifyCommands;
    }

    public void setVerifyCommands(List<List<String>> verifyCommands) {
        this.verifyCommands = verifyCommands;
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
}
