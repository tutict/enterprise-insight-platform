package com.tutict.eip.orchestrator.graph.model;

import java.util.ArrayList;
import java.util.List;

public class GraphCompileResult {

    private boolean valid;
    private GraphDefinition graph;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public GraphCompileResult() {
    }

    public GraphCompileResult(boolean valid, GraphDefinition graph, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.graph = graph;
        this.errors = errors;
        this.warnings = warnings;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public GraphDefinition getGraph() {
        return graph;
    }

    public void setGraph(GraphDefinition graph) {
        this.graph = graph;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
