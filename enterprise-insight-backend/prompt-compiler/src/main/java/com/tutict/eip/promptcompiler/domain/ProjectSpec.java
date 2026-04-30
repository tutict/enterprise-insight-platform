package com.tutict.eip.promptcompiler.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class ProjectSpec {

    @NotBlank
    private String type;

    @NotEmpty
    private List<String> modules = new ArrayList<>();

    public ProjectSpec() {
    }

    public ProjectSpec(String type, List<String> modules) {
        this.type = type;
        this.modules = modules;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }
}
