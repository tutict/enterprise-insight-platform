package com.tutict.eip.orchestrator.workspace;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "workspaces")
public class WorkspaceStoreProperties {

    private String storageRoot = "../runtime-logs/workspaces";
    private String defaultWorkspaceId = "demo-workspace";

    public String getStorageRoot() {
        return storageRoot;
    }

    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }

    public String getDefaultWorkspaceId() {
        return defaultWorkspaceId;
    }

    public void setDefaultWorkspaceId(String defaultWorkspaceId) {
        this.defaultWorkspaceId = defaultWorkspaceId;
    }
}
