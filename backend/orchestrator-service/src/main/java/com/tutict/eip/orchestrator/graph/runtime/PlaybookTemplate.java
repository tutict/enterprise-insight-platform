package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.orchestrator.graph.model.GraphDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlaybookTemplate {

    private String id;
    private String name;
    private String description;
    private GraphDefinition graph;
    private Map<String, Object> defaultRunConfig = new LinkedHashMap<>();
    private List<String> evidence;

    public PlaybookTemplate() {
    }

    public PlaybookTemplate(
            String id,
            String name,
            String description,
            GraphDefinition graph,
            Map<String, Object> defaultRunConfig,
            List<String> evidence
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.graph = graph;
        this.defaultRunConfig = defaultRunConfig;
        this.evidence = evidence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GraphDefinition getGraph() {
        return graph;
    }

    public void setGraph(GraphDefinition graph) {
        this.graph = graph;
    }

    public Map<String, Object> getDefaultRunConfig() {
        return defaultRunConfig;
    }

    public void setDefaultRunConfig(Map<String, Object> defaultRunConfig) {
        this.defaultRunConfig = defaultRunConfig;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }
}
