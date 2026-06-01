package com.tutict.eip.orchestrator.project;

import com.tutict.eip.orchestrator.project.ProjectInventory.CodeEvidence;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProjectDeliveryBrief(
        String title,
        String summary,
        String requirement,
        String playbookId,
        String playbookName,
        String targetDirectory,
        List<List<String>> verifyCommands,
        int maxRepairRounds,
        Map<String, Object> options,
        List<CodeEvidence> evidence,
        Instant generatedAt
) {
}
