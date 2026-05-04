package com.tutict.eip.harnesscompiler.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DslFlowStep {

    private String id;
    private String label;
    private String type;
    private Map<String, Object> config = new LinkedHashMap<>();
    private List<DslFlowEdge> next = new ArrayList<>();

    public DslFlowStep() {
    }

    public DslFlowStep(String id, String label, String type, Map<String, Object> config, List<DslFlowEdge> next) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.config = config;
        this.next = next;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public List<DslFlowEdge> getNext() {
        return next;
    }

    public void setNext(List<DslFlowEdge> next) {
        this.next = next;
    }
}
