package com.tutict.eip.harness.domain;

import java.time.Instant;

public class CompiledHarnessPrompt {

    private String prompt;
    private Instant compiledAt;

    public CompiledHarnessPrompt() {
    }

    public CompiledHarnessPrompt(String prompt, Instant compiledAt) {
        this.prompt = prompt;
        this.compiledAt = compiledAt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Instant getCompiledAt() {
        return compiledAt;
    }

    public void setCompiledAt(Instant compiledAt) {
        this.compiledAt = compiledAt;
    }
}
