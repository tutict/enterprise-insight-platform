package com.tutict.eip.orchestrator.project;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "project.analysis")
public class ProjectAnalysisProperties {

    private String root = "";
    private int maxDepth = 8;
    private int maxFiles = 5000;
    private int maxEvidencePerCategory = 80;
    private List<String> ignoredDirectories = new ArrayList<>(List.of(
            ".git",
            ".idea",
            ".mvn",
            "node_modules",
            "target",
            "dist",
            "build",
            "runtime-logs",
            "output"
    ));

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public int getMaxEvidencePerCategory() {
        return maxEvidencePerCategory;
    }

    public void setMaxEvidencePerCategory(int maxEvidencePerCategory) {
        this.maxEvidencePerCategory = maxEvidencePerCategory;
    }

    public List<String> getIgnoredDirectories() {
        return ignoredDirectories;
    }

    public void setIgnoredDirectories(List<String> ignoredDirectories) {
        this.ignoredDirectories = ignoredDirectories;
    }
}
