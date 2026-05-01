package com.tutict.eip.agentadapter.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class AdapterFactory {

    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;
    private final LLMConfig config;

    public AdapterFactory(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, LLMConfig config) {
        this.restTemplateBuilder = Objects.requireNonNull(restTemplateBuilder, "restTemplateBuilder must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    public LLMAdapter createAdapter() {
        return create(config, restTemplateBuilder, objectMapper);
    }

    public static LLMAdapter create(LLMConfig config) {
        return create(config, new RestTemplateBuilder(), new ObjectMapper());
    }

    private static LLMAdapter create(LLMConfig config, RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        Objects.requireNonNull(config, "config must not be null");
        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(config.getRequestTimeout())
                .setReadTimeout(config.getRequestTimeout())
                .build();

        if (config.isLocal()) {
            return new OllamaAdapter(config, restTemplate, objectMapper);
        }
        if (config.isRemote()) {
            return new OpenAIAdapter(config, restTemplate, objectMapper);
        }
        if (config.isSmart()) {
            return new SmartRouter(
                    new OllamaAdapter(createLocalConfig(config), restTemplate, objectMapper),
                    new OpenAIAdapter(createRemoteConfig(config), restTemplate, objectMapper)
            );
        }
        throw new LLMAdapterException("Unsupported modelType: " + config.getModelType());
    }

    private static LLMConfig createLocalConfig(LLMConfig source) {
        LLMConfig config = copyCommonConfig(source);
        config.setModelType("local");
        config.setBaseUrl(firstNonBlank(source.getLocalBaseUrl(), source.getBaseUrl()));
        config.setModel(firstNonBlank(source.getLocalModel(), source.getModel()));
        return config;
    }

    private static LLMConfig createRemoteConfig(LLMConfig source) {
        LLMConfig config = copyCommonConfig(source);
        config.setModelType("remote");
        config.setBaseUrl(firstNonBlank(source.getRemoteBaseUrl(), source.getBaseUrl()));
        config.setModel(firstNonBlank(source.getRemoteModel(), source.getModel()));
        return config;
    }

    private static LLMConfig copyCommonConfig(LLMConfig source) {
        LLMConfig config = new LLMConfig();
        config.setApiKey(source.getApiKey());
        config.setStream(source.isStream());
        config.setRequestTimeout(source.getRequestTimeout());
        return config;
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }
}
