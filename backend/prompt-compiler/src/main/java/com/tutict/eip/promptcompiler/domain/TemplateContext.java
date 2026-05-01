package com.tutict.eip.promptcompiler.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class TemplateContext {

    private DslDocument dslDocument;
    private Map<String, Object> variables = new LinkedHashMap<>();

    public TemplateContext() {
    }

    public TemplateContext(DslDocument dslDocument, Map<String, Object> variables) {
        this.dslDocument = dslDocument;
        this.variables = variables;
    }

    public DslDocument getDslDocument() {
        return dslDocument;
    }

    public void setDslDocument(DslDocument dslDocument) {
        this.dslDocument = dslDocument;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
