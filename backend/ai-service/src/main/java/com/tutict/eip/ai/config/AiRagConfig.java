package com.tutict.eip.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OllamaProperties.class, QdrantProperties.class, RagProperties.class})
public class AiRagConfig {
}
