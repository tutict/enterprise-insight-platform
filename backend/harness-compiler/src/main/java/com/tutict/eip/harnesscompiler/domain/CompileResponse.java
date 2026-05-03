package com.tutict.eip.harnesscompiler.domain;

public class CompileResponse {

    private DslModel dsl;
    private String prompt;

    public CompileResponse() {
    }

    public CompileResponse(DslModel dsl, String prompt) {
        this.dsl = dsl;
        this.prompt = prompt;
    }

    public DslModel getDsl() {
        return dsl;
    }

    public void setDsl(DslModel dsl) {
        this.dsl = dsl;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
