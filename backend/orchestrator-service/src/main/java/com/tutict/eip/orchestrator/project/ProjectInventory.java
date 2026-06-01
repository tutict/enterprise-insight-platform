package com.tutict.eip.orchestrator.project;

import java.time.Instant;
import java.util.List;

public record ProjectInventory(
        String rootPath,
        Instant generatedAt,
        ProjectSummary summary,
        List<ProjectModule> modules,
        List<ApiEndpointEvidence> apiEndpoints,
        List<FrontendRouteEvidence> frontendRoutes,
        List<BusinessCapability> businessCapabilities,
        List<CodeEvidence> documents,
        List<CodeEvidence> tests,
        List<DeliveryOpportunity> deliveryOpportunities
) {

    public record ProjectSummary(
            int scannedFiles,
            int moduleCount,
            int apiEndpointCount,
            int frontendRouteCount,
            int businessCapabilityCount,
            int documentCount,
            int testCount
    ) {
    }

    public record ProjectModule(
            String name,
            String path,
            String type,
            int fileCount,
            List<String> markers
    ) {
    }

    public record ApiEndpointEvidence(
            String method,
            String path,
            String sourcePath,
            int line
    ) {
    }

    public record FrontendRouteEvidence(
            String path,
            String sourcePath,
            int line
    ) {
    }

    public record BusinessCapability(
            String name,
            String category,
            int evidenceCount,
            List<CodeEvidence> evidence
    ) {
    }

    public record CodeEvidence(
            String kind,
            String name,
            String sourcePath,
            int line
    ) {
    }

    public record DeliveryOpportunity(
            String priority,
            String title,
            String rationale,
            List<CodeEvidence> evidence
    ) {
    }
}
