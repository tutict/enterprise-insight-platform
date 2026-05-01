package com.tutict.eip.harness.agent.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.harness.config.LLMProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(AdapterFactory.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LLMProperties properties;

    public AdapterFactory(RestTemplate restTemplate, ObjectMapper objectMapper, LLMProperties properties) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public LLMAdapter create() {
        String modelType = properties.getModelType();
        log.info("Creating LLM adapter modelType={} model={}", modelType, properties.getModel());
        if ("local".equalsIgnoreCase(modelType)) {
            return new OllamaAdapter(restTemplate, objectMapper, properties);
        }
        if ("remote".equalsIgnoreCase(modelType)) {
            return new OpenAIAdapter(restTemplate, objectMapper, properties);
        }
        throw new LLMAdapterException("Unsupported llm.model-type: " + modelType);
    }
}
