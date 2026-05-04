package com.tutict.eip.orchestrator.graph.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphNode {

    private String id;
    private String label;
    private String type;
    private Map<String, Object> config = new LinkedHashMap<>();

    public GraphNode() {
    }

    public GraphNode(String id, String label, String type) {
        this.id = id;
        this.label = label;
        this.type = type;
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
}
