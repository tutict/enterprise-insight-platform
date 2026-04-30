package com.tutict.eip.agentadapter;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties.class)
public class AgentAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentAdapterApplication.class, args);
    }
}
