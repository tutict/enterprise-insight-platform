package com.tutict.eip.promptcompiler.domain;

public class PromptCompileResponse {

    private DslDocument dslDocument;
    private CompiledPrompt compiledPrompt;

    public PromptCompileResponse() {
    }

    public PromptCompileResponse(DslDocument dslDocument, CompiledPrompt compiledPrompt) {
        this.dslDocument = dslDocument;
        this.compiledPrompt = compiledPrompt;
    }

    public DslDocument getDslDocument() {
        return dslDocument;
    }

    public void setDslDocument(DslDocument dslDocument) {
        this.dslDocument = dslDocument;
    }

    public CompiledPrompt getCompiledPrompt() {
        return compiledPrompt;
    }

    public void setCompiledPrompt(CompiledPrompt compiledPrompt) {
        this.compiledPrompt = compiledPrompt;
    }
}
