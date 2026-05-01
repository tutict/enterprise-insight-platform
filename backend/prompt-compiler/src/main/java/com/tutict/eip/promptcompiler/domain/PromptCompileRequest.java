package com.tutict.eip.promptcompiler.domain;

import jakarta.validation.constraints.NotBlank;

public class PromptCompileRequest {

    @NotBlank
    private String dsl;

    private String templateName;

    public PromptCompileRequest() {
    }

    public PromptCompileRequest(String dsl, String templateName) {
        this.dsl = dsl;
        this.templateName = templateName;
    }

    public String getDsl() {
        return dsl;
    }

    public void setDsl(String dsl) {
        this.dsl = dsl;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
