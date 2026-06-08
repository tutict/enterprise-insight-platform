package com.tutict.eip.orchestrator.patchproposal;

public record PatchProposalDiff(
        String workspaceId,
        String runId,
        String fileId,
        String targetPath,
        PatchProposalChangeType changeType,
        String diff,
        String rejectedReason
) {
}
