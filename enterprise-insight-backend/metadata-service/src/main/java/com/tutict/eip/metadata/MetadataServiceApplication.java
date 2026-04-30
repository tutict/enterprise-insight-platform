package com.tutict.eip.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class MetadataServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MetadataServiceApplication.class, args);
    }

    @Bean
    public ApplicationRunner logVirtualThreads(Environment environment) {
        return args -> {
            boolean enabled = Boolean.parseBoolean(
                    environment.getProperty("spring.threads.virtual.enabled", "false")
            );
            logger.info("Virtual threads enabled: {}", enabled);
            if (enabled) {
                Thread vt = Thread.ofVirtual()
                        .name("vt-check-metadata", 0)
                        .start(() -> logger.info("Virtual thread check: {}", Thread.currentThread().isVirtual()));
                try {
                    vt.join();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }
}
