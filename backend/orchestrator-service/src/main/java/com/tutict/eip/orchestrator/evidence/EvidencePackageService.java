package com.tutict.eip.orchestrator.evidence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.agentadapter.domain.AutoRepairAttempt;
import com.tutict.eip.agentadapter.domain.VerificationCommandResult;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.orchestrator.delivery.DeliveryRunRecord;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStore;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.patchproposal.PatchProposal;
import com.tutict.eip.orchestrator.patchproposal.PatchProposalFile;
import com.tutict.eip.orchestrator.patchproposal.PatchProposalService;
import com.tutict.eip.orchestrator.runtime.RunEvent;
import com.tutict.eip.orchestrator.workspace.Workspace;
import com.tutict.eip.orchestrator.workspace.WorkspaceStoreProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Service
public class EvidencePackageService {

    private final DeliveryRunStore deliveryRunStore;
    private final PatchProposalService patchProposalService;
    private final ObjectMapper objectMapper;
    private final Path workspaceRoot;

    public EvidencePackageService(
            DeliveryRunStore deliveryRunStore,
            PatchProposalService patchProposalService,
            ObjectMapper objectMapper,
            WorkspaceStoreProperties properties
    ) {
        this.deliveryRunStore = deliveryRunStore;
        this.patchProposalService = patchProposalService;
        this.objectMapper = objectMapper;
        this.workspaceRoot = Path.of(properties.getStorageRoot()).toAbsolutePath().normalize();
    }

    public EvidencePackage export(Workspace workspace, String runId) {
        DeliveryRunRecord record = deliveryRunStore.find(runId)
                .filter(run -> workspace.getWorkspaceId().equals(run.getWorkspaceId()))
                .orElseThrow(() -> new IllegalArgumentException("Delivery run not found in workspace: " + runId));
        Instant exportedAt = Instant.now();
        PatchProposal patchProposal = patchProposalService.getOrGenerate(workspace, runId);
        String markdown = buildMarkdown(workspace, record, patchProposal, exportedAt);
        Path evidenceDir = workspaceRoot
                .resolve(workspace.getWorkspaceId())
                .resolve("evidence")
                .resolve(safeId(runId));
        Path markdownPath = evidenceDir.resolve("evidence.md");
        Path jsonPath = evidenceDir.resolve("evidence.json");
        EvidencePackage evidencePackage = new EvidencePackage(
                workspace.getWorkspaceId(),
                runId,
                markdownPath.toString(),
                jsonPath.toString(),
                markdown,
                workspace,
                record,
                patchProposal,
                exportedAt
        );
        try {
            Files.createDirectories(evidenceDir);
            Files.writeString(markdownPath, markdown);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), evidencePackage);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to export evidence package for run " + runId, ex);
        }
        return evidencePackage;
    }

    private String buildMarkdown(
            Workspace workspace,
            DeliveryRunRecord record,
            PatchProposal patchProposal,
            Instant exportedAt
    ) {
        StringBuilder builder = new StringBuilder();
        OrchestratorRunRequest request = record.getRequest();
        builder.append("# FDE Delivery Evidence\n\n");
        builder.append("- Workspace: ").append(workspace.getWorkspaceId()).append("\n");
        builder.append("- Customer: ").append(nullToDash(workspace.getCustomerName())).append("\n");
        builder.append("- Project: ").append(nullToDash(workspace.getProjectName())).append("\n");
        builder.append("- Repository: ").append(nullToDash(workspace.getRepoRoot())).append("\n");
        builder.append("- Run: ").append(record.getRunId()).append("\n");
        builder.append("- Status: ").append(record.getStatus()).append("\n");
        builder.append("- Exported At: ").append(exportedAt).append("\n\n");

        builder.append("## Requirement\n\n");
        builder.append(request == null ? "-" : fence(request.getRequirement())).append("\n\n");

        builder.append("## Playbook\n\n");
        builder.append("- ID: ").append(record.getPlaybookId()).append("\n");
        builder.append("- Name: ").append(record.getPlaybookName()).append("\n\n");

        builder.append("## Harness Prompt\n\n");
        String prompt = record.getResponse() == null ? "" : record.getResponse().getHarnessPrompt();
        builder.append(prompt == null || prompt.isBlank() ? "-" : fence(prompt)).append("\n\n");

        builder.append("## Run Events\n\n");
        if (record.getEvents().isEmpty()) {
            builder.append("- No events recorded.\n");
        } else {
            for (RunEvent event : record.getEvents()) {
                builder.append("- ")
                        .append(event.getType());
                if (event.getStep() != null) {
                    builder.append(" / ").append(event.getStep());
                }
                builder.append(" @ ").append(event.getTimestamp()).append("\n");
            }
        }
        builder.append("\n");

        builder.append("## Verification\n\n");
        VerificationResult verification = record.getResponse() == null || record.getResponse().getGeneration() == null
                ? null
                : record.getResponse().getGeneration().getFinalVerificationResult();
        appendVerification(builder, verification);

        builder.append("\n## Patch Proposal\n\n");
        appendPatchProposal(builder, patchProposal);

        builder.append("\n## Repair Attempts\n\n");
        if (record.getResponse() == null || record.getResponse().getGeneration() == null
                || record.getResponse().getGeneration().getAttempts() == null
                || record.getResponse().getGeneration().getAttempts().isEmpty()) {
            builder.append("- No attempts recorded.\n");
        } else {
            for (AutoRepairAttempt attempt : record.getResponse().getGeneration().getAttempts()) {
                builder.append("- Attempt ")
                        .append(attempt.getAttemptNumber())
                        .append(": ")
                        .append(attempt.isSuccessful() ? "successful" : "failed")
                        .append(", files=")
                        .append(attempt.getWrittenFiles() == null ? 0 : attempt.getWrittenFiles().size())
                        .append("\n");
            }
        }

        builder.append("\n## Final Output\n\n");
        String output = record.getResponse() == null || record.getResponse().getGeneration() == null
                ? ""
                : record.getResponse().getGeneration().getFinalOutput();
        builder.append(output == null || output.isBlank() ? "-" : fence(output));
        builder.append("\n");
        return builder.toString();
    }

    private void appendPatchProposal(StringBuilder builder, PatchProposal patchProposal) {
        if (patchProposal == null) {
            builder.append("- No patch proposal recorded.\n");
            return;
        }
        builder.append("- Status: ").append(patchProposal.getStatus()).append("\n");
        builder.append("- Changes: ").append(patchProposal.getChangeCount()).append("\n");
        builder.append("- Rejected Files: ").append(patchProposal.getRejectedCount()).append("\n");
        builder.append("- Verification Scope: ").append(nullToDash(patchProposal.getVerificationScope())).append("\n");
        builder.append("- Verification Successful: ")
                .append(patchProposal.getVerificationSuccessful() == null ? "-" : patchProposal.getVerificationSuccessful())
                .append("\n");
        builder.append("- Proposal Path: ").append(nullToDash(patchProposal.getProposalPath())).append("\n\n");
        if (!patchProposal.getRisks().isEmpty()) {
            builder.append("Risks:\n\n");
            for (String risk : patchProposal.getRisks()) {
                builder.append("- ").append(risk).append("\n");
            }
            builder.append("\n");
        }
        if (patchProposal.getFiles().isEmpty()) {
            builder.append("- No files recorded.\n");
            return;
        }
        builder.append("Files:\n\n");
        for (PatchProposalFile file : patchProposal.getFiles()) {
            builder.append("- ")
                    .append(file.getChangeType())
                    .append(": ")
                    .append(file.getTargetPath());
            if (file.getRejectedReason() != null && !file.getRejectedReason().isBlank()) {
                builder.append(" (").append(file.getRejectedReason()).append(")");
            }
            builder.append("\n");
        }
    }

    private void appendVerification(StringBuilder builder, VerificationResult verification) {
        if (verification == null) {
            builder.append("- No verification result recorded.\n");
            return;
        }
        builder.append("- Successful: ").append(verification.isSuccessful()).append("\n");
        builder.append("- Summary: ").append(nullToDash(verification.getSummary())).append("\n\n");
        for (VerificationCommandResult command : verification.getCommandResults()) {
            builder.append("### `").append(command.getCommand()).append("`\n\n");
            builder.append("- Exit Code: ").append(command.getExitCode()).append("\n");
            builder.append("- Timed Out: ").append(command.isTimedOut()).append("\n");
            builder.append("- Duration Ms: ").append(command.getDurationMillis()).append("\n\n");
            if (command.getStdout() != null && !command.getStdout().isBlank()) {
                builder.append("Stdout:\n\n").append(fence(command.getStdout())).append("\n\n");
            }
            if (command.getStderr() != null && !command.getStderr().isBlank()) {
                builder.append("Stderr:\n\n").append(fence(command.getStderr())).append("\n\n");
            }
        }
    }

    private String fence(String value) {
        return "```text\n" + value + "\n```";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String safeId(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
