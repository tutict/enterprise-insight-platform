package com.tutict.eip.harness;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.harness.agent.llm.AdapterFactory;
import com.tutict.eip.harness.agent.llm.LLMAdapter;
import com.tutict.eip.harness.config.LLMProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeAdapterFactory adapterFactory;

    @BeforeEach
    void setUp() {
        adapterFactory.reset();
    }

    @Test
    void shouldCompileRequirementAndGenerateCodeWithMockLlm() throws Exception {
        MvcResult compileResult = mockMvc.perform(post("/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "requirement", "Build a user login system with database persistence"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dsl.modules").isArray())
                .andExpect(jsonPath("$.prompt").exists())
                .andReturn();

        JsonNode compileJson = objectMapper.readTree(compileResult.getResponse().getContentAsString());
        String prompt = compileJson.path("prompt").asText();

        MvcResult generateResult = mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("prompt", prompt))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();

        JsonNode generateJson = objectMapper.readTree(generateResult.getResponse().getContentAsString());
        assertThat(prompt).contains("ROLE", "GOAL", "MODULES", "CONSTRAINTS", "OUTPUT FORMAT");
        assertThat(generateJson.path("code").asText()).contains("DemoController");
        assertThat(adapterFactory.getCreateCalls()).isEqualTo(1);
        assertThat(adapterFactory.getLastPrompt()).isEqualTo(prompt);
    }

    @TestConfiguration
    static class EndToEndTestConfig {

        @Bean
        @Primary
        FakeAdapterFactory fakeAdapterFactory(ObjectMapper objectMapper) {
            return new FakeAdapterFactory(objectMapper);
        }
    }

    static class FakeAdapterFactory extends AdapterFactory {

        private int createCalls;
        private String lastPrompt;

        FakeAdapterFactory(ObjectMapper objectMapper) {
            super(new RestTemplate(), objectMapper, new LLMProperties());
        }

        @Override
        public LLMAdapter create() {
            createCalls++;
            return prompt -> {
                lastPrompt = prompt;
                return """
                        ===FILE START===
                        src/main/java/com/example/DemoController.java
                        package com.example;

                        public class DemoController {
                        }
                        ===FILE END===
                        """;
            };
        }

        void reset() {
            createCalls = 0;
            lastPrompt = null;
        }

        int getCreateCalls() {
            return createCalls;
        }

        String getLastPrompt() {
            return lastPrompt;
        }
    }
}
