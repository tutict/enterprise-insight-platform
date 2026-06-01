package com.tutict.eip.orchestrator.graph.runtime;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PlaybookTemplateService {

    public static final String DEFAULT_PLAYBOOK_ID = "compile-generate-verify-repair";
    public static final String DISCOVERY_PLAYBOOK_ID = "industry-business-discovery";

    public List<PlaybookTemplate> list() {
        return List.of(defaultDeliveryPlaybook(), discoveryPlaybook());
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

    public PlaybookTemplate discoveryPlaybook() {
        return discoveryPlaybook(2);
    }

    public PlaybookTemplate discoveryPlaybook(int maxIterations) {
        Map<String, Object> defaultRunConfig = new LinkedHashMap<>();
        defaultRunConfig.put("phase", "discovery");
        defaultRunConfig.put("industries", List.of("AI engineering delivery", "enterprise developer productivity"));
        defaultRunConfig.put(
                "sourceTypes",
                List.of("industry report", "competitor docs", "open-source README", "existing project docs")
        );
        defaultRunConfig.put("evidenceRule", "Every material conclusion must cite a source or be marked as an assumption.");
        defaultRunConfig.put("deliverables", List.of(
                "industry-research.md",
                "business-analysis.md",
                "domain-model.md",
                "delivery-backlog.md"
        ));

        return new PlaybookTemplate(
                DISCOVERY_PLAYBOOK_ID,
                "Industry And Business Discovery",
                "Discovery playbook for collecting industry material, extracting business signals, "
                        + "modeling the existing project, and producing an evidence-backed delivery backlog.",
                DefaultGraphDefinitions.industryBusinessDiscovery(maxIterations),
                defaultRunConfig,
                List.of(
                        "source inventory",
                        "fact and assumption register",
                        "business capability map",
                        "domain model notes",
                        "gap analysis",
                        "prioritized delivery backlog"
                )
        );
    }
}
