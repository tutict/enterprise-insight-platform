package com.tutict.eip.orchestrator.evidence;

import com.tutict.eip.orchestrator.delivery.DeliveryRunRecord;
import com.tutict.eip.orchestrator.workspace.Workspace;

import java.time.Instant;

public record EvidencePackage(
        String workspaceId,
        String runId,
        String markdownPath,
        String jsonPath,
        String markdown,
        Workspace workspace,
        DeliveryRunRecord deliveryRun,
        Instant exportedAt
) {
}
