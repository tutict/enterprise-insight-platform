package com.tutict.eip.orchestrator.graph.model;

public class GraphEdge {

    private String id;
    private String source;
    private String target;
    private String condition = "always";
    private Integer maxIterations;
    private String label;

    public GraphEdge() {
    }

    public GraphEdge(String id, String source, String target, String condition) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.condition = condition;
        this.label = condition;
    }

    public boolean matches(GraphNodeResult result) {
        if (condition == null || condition.isBlank() || "always".equalsIgnoreCase(condition)) {
            return true;
        }
        if ("success".equalsIgnoreCase(condition)) {
            return result.isSuccessful();
        }
        if ("failed".equalsIgnoreCase(condition) || "failure".equalsIgnoreCase(condition)) {
            return !result.isSuccessful();
        }
        return condition.equalsIgnoreCase(result.getStatus());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
