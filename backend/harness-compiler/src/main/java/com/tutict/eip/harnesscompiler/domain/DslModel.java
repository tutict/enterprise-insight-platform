package com.tutict.eip.harnesscompiler.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DslModel {

    private String name;
    private String type;
    private String role;
    private String goal;
    private String task;
    private String requirement;
    private List<String> modules = new ArrayList<>();
    private List<DslFlowStep> flow = new ArrayList<>();
    private Map<String, String> constraints = new LinkedHashMap<>();
    private Map<String, Object> metadata = new LinkedHashMap<>();
    private String outputFormat;

    public DslModel() {
    }

    public DslModel(String name, String type, String requirement, List<String> modules,
                    Map<String, String> constraints, String outputFormat) {
        this.name = name;
        this.type = type;
        this.requirement = requirement;
        this.modules = modules;
        this.constraints = constraints;
        this.outputFormat = outputFormat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public List<DslFlowStep> getFlow() {
        return flow;
    }

    public void setFlow(List<DslFlowStep> flow) {
        this.flow = flow;
    }

    public Map<String, String> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, String> constraints) {
        this.constraints = constraints;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
}
