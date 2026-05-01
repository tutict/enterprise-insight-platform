package com.tutict.eip.harness.domain;

public class CompileResponse {

    private DSLModel dsl;
    private String prompt;

    public CompileResponse() {
    }

    public CompileResponse(DSLModel dsl, String prompt) {
        this.dsl = dsl;
        this.prompt = prompt;
    }

    public DSLModel getDsl() {
        return dsl;
    }

    public void setDsl(DSLModel dsl) {
        this.dsl = dsl;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
