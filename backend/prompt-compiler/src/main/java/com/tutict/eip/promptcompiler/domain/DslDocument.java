package com.tutict.eip.promptcompiler.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class DslDocument {

    @Valid
    @NotNull
    private ProjectSpec project;

    @Valid
    private PromptConstraints constraints;

    public DslDocument() {
    }

    public DslDocument(ProjectSpec project, PromptConstraints constraints) {
        this.project = project;
        this.constraints = constraints;
    }

    public ProjectSpec getProject() {
        return project;
    }

    public void setProject(ProjectSpec project) {
        this.project = project;
    }

    public PromptConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(PromptConstraints constraints) {
        this.constraints = constraints;
    }
}
