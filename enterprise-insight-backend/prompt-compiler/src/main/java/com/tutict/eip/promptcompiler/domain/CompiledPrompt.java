package com.tutict.eip.promptcompiler.domain;

import java.time.Instant;
import java.util.List;

public class CompiledPrompt {

    private String templateName;
    private String harnessPrompt;
    private List<String> sections;
    private Instant compiledAt;

    public CompiledPrompt() {
    }

    public CompiledPrompt(String templateName, String harnessPrompt, List<String> sections, Instant compiledAt) {
        this.templateName = templateName;
        this.harnessPrompt = harnessPrompt;
        this.sections = sections;
        this.compiledAt = compiledAt;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getHarnessPrompt() {
        return harnessPrompt;
    }

    public void setHarnessPrompt(String harnessPrompt) {
        this.harnessPrompt = harnessPrompt;
    }

    public List<String> getSections() {
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    public Instant getCompiledAt() {
        return compiledAt;
    }

    public void setCompiledAt(Instant compiledAt) {
        this.compiledAt = compiledAt;
    }
}
