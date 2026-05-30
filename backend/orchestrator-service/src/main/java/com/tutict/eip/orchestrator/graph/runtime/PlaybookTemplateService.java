package com.tutict.eip.orchestrator.graph.runtime;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlaybookTemplateService {

    public static final String DEFAULT_PLAYBOOK_ID = "compile-generate-verify-repair";

    public List<PlaybookTemplate> list() {
        return List.of(defaultDeliveryPlaybook());
    }

    public Optional<PlaybookTemplate> find(String id) {
        return list().stream()
                .filter(template -> template.getId().equals(id))
                .findFirst();
    }

    public PlaybookTemplate defaultDeliveryPlaybook() {
        return defaultDeliveryPlaybook(3, 1);
    }

    public PlaybookTemplate defaultDeliveryPlaybook(int maxIterations, int requiredRepairIterations) {
        Map<String, Object> defaultRunConfig = new LinkedHashMap<>();
        defaultRunConfig.put("model", "llama3.1");
        defaultRunConfig.put("targetDirectory", "generated-fde-delivery");
        defaultRunConfig.put("verifyCommands", List.of(List.of("mvn", "test")));
        defaultRunConfig.put("maxRepairRounds", 2);

        return new PlaybookTemplate(
                DEFAULT_PLAYBOOK_ID,
                "Compile Generate Verify Repair",
                "Default FDE delivery playbook for compiling requirements, generating implementation, verifying output, and repairing failures.",
                DefaultGraphDefinitions.compileGenerateVerifyRepair(maxIterations, requiredRepairIterations),
                defaultRunConfig,
                List.of("compiled DSL", "harness prompt", "runtime events", "generated files", "verification result")
        );
    }
}
