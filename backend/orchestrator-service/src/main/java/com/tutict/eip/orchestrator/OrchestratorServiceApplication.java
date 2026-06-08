package com.tutict.eip.orchestrator;

import com.tutict.eip.agentadapter.config.OllamaProperties;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStoreProperties;
import com.tutict.eip.orchestrator.project.ProjectAnalysisProperties;
import com.tutict.eip.orchestrator.workspace.WorkspaceStoreProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {
        "com.tutict.eip.orchestrator",
        "com.tutict.eip.harnesscompiler.service",
        "com.tutict.eip.agentadapter.config",
        "com.tutict.eip.agentadapter.service",
        "com.tutict.eip.agentadapter.storage",
        "com.tutict.eip.agentadapter.verify"
})
@EnableConfigurationProperties({
        OllamaProperties.class,
        DeliveryRunStoreProperties.class,
        ProjectAnalysisProperties.class,
        WorkspaceStoreProperties.class
})
public class OrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorServiceApplication.class, args);
    }
}
