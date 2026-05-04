package com.tutict.eip.harnesscompiler.domain;

public class DslFlowEdge {

    private String target;
    private String condition;
    private String label;
    private Integer maxIterations;

    public DslFlowEdge() {
    }

    public DslFlowEdge(String target, String condition, String label, Integer maxIterations) {
        this.target = target;
        this.condition = condition;
        this.label = label;
        this.maxIterations = maxIterations;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }
}
