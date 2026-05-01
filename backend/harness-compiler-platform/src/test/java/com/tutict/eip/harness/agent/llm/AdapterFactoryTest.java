package com.tutict.eip.harness.agent.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.harness.config.LLMProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdapterFactoryTest {

    @Test
    void shouldCreateOllamaAdapterForLocalModelType() {
        LLMProperties properties = new LLMProperties();
        properties.setModelType("local");
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("llama3");
        AdapterFactory factory = new AdapterFactory(new RestTemplate(), new ObjectMapper(), properties);

        LLMAdapter adapter = factory.create();

        assertThat(adapter).isInstanceOf(OllamaAdapter.class);
    }

    @Test
    void shouldCreateOpenAIAdapterForRemoteModelType() {
        LLMProperties properties = new LLMProperties();
        properties.setModelType("remote");
        properties.setBaseUrl("https://api.openai.com/v1");
        properties.setApiKey("test-key");
        properties.setModel("gpt-4o-mini");
        AdapterFactory factory = new AdapterFactory(new RestTemplate(), new ObjectMapper(), properties);

        LLMAdapter adapter = factory.create();

        assertThat(adapter).isInstanceOf(OpenAIAdapter.class);
    }

    @Test
    void shouldRejectUnsupportedModelType() {
        LLMProperties properties = new LLMProperties();
        properties.setModelType("unknown");
        AdapterFactory factory = new AdapterFactory(new RestTemplate(), new ObjectMapper(), properties);

        assertThatThrownBy(factory::create)
                .isInstanceOf(LLMAdapterException.class)
                .hasMessageContaining("Unsupported llm.model-type");
    }
}
