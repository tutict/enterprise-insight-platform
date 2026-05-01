package com.tutict.eip.promptcompiler.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class PromptConstraints {

    private String db;

    private Map<String, Object> extensions = new LinkedHashMap<>();

    public PromptConstraints() {
    }

    public PromptConstraints(String db, Map<String, Object> extensions) {
        this.db = db;
        this.extensions = extensions;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    @JsonAnySetter
    public void putExtension(String key, Object value) {
        this.extensions.put(key, value);
    }
}
