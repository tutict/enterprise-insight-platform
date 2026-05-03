package com.tutict.eip.harnesscompiler.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DslModel {

    private String name;
    private String type;
    private String requirement;
    private List<String> modules = new ArrayList<>();
    private Map<String, String> constraints = new LinkedHashMap<>();
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

    public Map<String, String> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, String> constraints) {
        this.constraints = constraints;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
}
