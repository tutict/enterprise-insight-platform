package com.tutict.eip.agentadapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
public class AgentAdapterConfig {

    @Bean
    public HttpClient ollamaHttpClient(OllamaProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.getRequestTimeout())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Bean(name = "agentAdapterTaskExecutor")
    public Executor agentAdapterTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(64);
        executor.setThreadNamePrefix("agent-adapter-");
        executor.setAwaitTerminationSeconds((int) Duration.ofSeconds(10).toSeconds());
        executor.initialize();
        return executor;
    }
}
