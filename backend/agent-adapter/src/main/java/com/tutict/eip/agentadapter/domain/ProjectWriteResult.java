package com.tutict.eip.agentadapter.domain;

import java.util.ArrayList;
import java.util.List;

public class ProjectWriteResult {

    private String projectRoot;
    private List<GeneratedProjectFile> files = new ArrayList<>();

    public ProjectWriteResult() {
    }

    public ProjectWriteResult(String projectRoot, List<GeneratedProjectFile> files) {
        this.projectRoot = projectRoot;
        this.files = files;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public List<GeneratedProjectFile> getFiles() {
        return files;
    }

    public void setFiles(List<GeneratedProjectFile> files) {
        this.files = files;
    }
}
