package com.tutict.eip.harness.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DslDocument {

    private String projectType;
    private List<String> modules = new ArrayList<>();
    private Map<String, Object> constraints = new LinkedHashMap<>();

    public DslDocument() {
    }

    public DslDocument(String projectType, List<String> modules, Map<String, Object> constraints) {
        this.projectType = projectType;
        this.modules = modules;
        this.constraints = constraints;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Object> constraints) {
        this.constraints = constraints;
    }
}
