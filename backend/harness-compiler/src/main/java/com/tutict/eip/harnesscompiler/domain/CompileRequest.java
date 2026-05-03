package com.tutict.eip.harnesscompiler.domain;

import jakarta.validation.constraints.NotBlank;

public class CompileRequest {

    @NotBlank(message = "requirement must not be blank")
    private String requirement;

    public CompileRequest() {
    }

    public CompileRequest(String requirement) {
        this.requirement = requirement;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }
}
