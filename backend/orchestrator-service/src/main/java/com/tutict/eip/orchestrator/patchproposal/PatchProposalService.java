package com.tutict.eip.orchestrator.patchproposal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.agentadapter.domain.AutoRepairAttempt;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.domain.GeneratedProjectFile;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.orchestrator.delivery.DeliveryRunRecord;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStatus;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStore;
import com.tutict.eip.orchestrator.workspace.Workspace;
import com.tutict.eip.orchestrator.workspace.WorkspaceRepository;
import com.tutict.eip.orchestrator.workspace.WorkspaceStoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PatchProposalService {

    private static final Logger log = LoggerFactory.getLogger(PatchProposalService.class);
    private static final Set<String> DENIED_PATH_SEGMENTS = Set.of(".git", "target", "node_modules", "dist");
    private static final String GENERATED_OUTPUT_VERIFIED = "generated-output-verified";
    private static final String UNVERIFIED = "unverified";

    private final DeliveryRunStore deliveryRunStore;
    private final WorkspaceRepository workspaceRepository;
    private final ObjectMapper objectMapper;
    private final Path workspaceRoot;

    public PatchProposalService(
            DeliveryRunStore deliveryRunStore,
            WorkspaceRepository workspaceRepository,
            ObjectMapper objectMapper,
            WorkspaceStoreProperties properties
    ) {
        this.deliveryRunStore = deliveryRunStore;
        this.workspaceRepository = workspaceRepository;
        this.objectMapper = objectMapper;
        this.workspaceRoot = Path.of(properties.getStorageRoot()).toAbsolutePath().normalize();
    }

    public Optional<PatchProposal> find(Workspace workspace, String runId) {
        Path path = proposalPath(workspace, runId);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(path.toFile(), PatchProposal.class));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read patch proposal " + path, ex);
        }
    }

    public PatchProposal getOrGenerate(Workspace workspace, String runId) {
        return find(workspace, runId).orElseGet(() -> regenerate(workspace, runId));
    }

    public PatchProposal regenerateForRun(String runId) {
        DeliveryRunRecord record = deliveryRunStore.find(runId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery run not found: " + runId));
        Workspace workspace = workspaceFor(record);
        return regenerate(workspace, runId);
    }

    public PatchProposal regenerate(Workspace workspace, String runId) {
        DeliveryRunRecord record = loadRecord(workspace, runId);
        try {
            PatchProposal proposal = buildProposal(workspace, record);
            return persist(workspace, runId, proposal);
        } catch (RuntimeException ex) {
            log.warn("Failed to generate patch proposal workspaceId={} runId={}",
                    workspace.getWorkspaceId(), runId, ex);
            return persist(workspace, runId, failedProposal(workspace, runId, ex.getMessage(), record));
        }
    }

    public PatchProposalDiff readDiff(Workspace workspace, String runId, String fileId) {
        PatchProposal proposal = getOrGenerate(workspace, runId);
        PatchProposalFile file = proposal.getFiles().stream()
                .filter(item -> fileId.equals(item.getFileId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Patch proposal file not found: " + fileId));
        String diff = "";
        if (file.getDiffPath() != null && !file.getDiffPath().isBlank()) {
            Path diffPath = Path.of(file.getDiffPath()).toAbsolutePath().normalize();
            Path filesDir = filesDir(workspace, runId);
            if (!diffPath.startsWith(filesDir)) {
                throw new IllegalArgumentException("Patch proposal diff path is outside proposal files directory");
            }
            try {
                diff = Files.readString(diffPath);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to read patch proposal diff " + diffPath, ex);
            }
        }
        return new PatchProposalDiff(
                workspace.getWorkspaceId(),
                runId,
                file.getFileId(),
                file.getTargetPath(),
                file.getChangeType(),
                diff,
                file.getRejectedReason()
        );
    }

    private PatchProposal buildProposal(Workspace workspace, DeliveryRunRecord record) {
        if (record.getStatus() != DeliveryRunStatus.COMPLETED) {
            return failedProposal(workspace, record.getRunId(), "Delivery run is not completed.", record);
        }
        if (record.getResponse() == null || record.getResponse().getGeneration() == null) {
            return failedProposal(workspace, record.getRunId(), "Delivery run has no generation response.", record);
        }

        AutoRepairGenerationResponse generation = record.getResponse().getGeneration();
        List<GeneratedFileSource> sources = generatedSources(generation);
        if (sources.isEmpty()) {
            return failedProposal(workspace, record.getRunId(), "No generated files were recorded for the run.", record);
        }

        Path repoRoot = Path.of(workspace.getRepoRoot()).toAbsolutePath().normalize();
        if (!Files.isDirectory(repoRoot)) {
            return failedProposal(workspace, record.getRunId(), "Workspace repoRoot is not a directory: " + repoRoot, record);
        }

        List<PatchProposalFile> files = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        int index = 0;
        for (GeneratedFileSource source : sources) {
            TargetPathValidation validation = validateTargetPath(source.relativePath(), workspace);
            if (!validation.accepted()) {
                files.add(rejectedFile(source, validation.reason(), index++));
                continue;
            }
            files.add(compareFile(workspace, record.getRunId(), repoRoot, source, validation.targetPath(), index++));
        }

        long rejectedCount = files.stream()
                .filter(file -> file.getChangeType() == PatchProposalChangeType.REJECTED)
                .count();
        long changeCount = files.stream()
                .filter(file -> file.getChangeType() == PatchProposalChangeType.CREATE
                        || file.getChangeType() == PatchProposalChangeType.UPDATE)
                .count();

        if (rejectedCount > 0) {
            risks.add("Rejected " + rejectedCount + " generated file(s) due to path safety rules.");
        }
        risks.add("Verification was executed against generated output, not the workspace after applying this proposal.");

        PatchProposal proposal = baseProposal(workspace, record.getRunId(), record);
        proposal.setStatus(statusFor(changeCount, rejectedCount));
        proposal.setFiles(files);
        proposal.setChangeCount(Math.toIntExact(changeCount));
        proposal.setRejectedCount(Math.toIntExact(rejectedCount));
        proposal.setRisks(risks);
        return proposal;
    }

    private PatchProposalFile compareFile(
            Workspace workspace,
            String runId,
            Path repoRoot,
            GeneratedFileSource source,
            String targetPath,
            int index
    ) {
        Path generatedPath = Path.of(source.generatedPath()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(generatedPath)) {
            return rejectedFile(source, "Generated file does not exist: " + generatedPath, index);
        }
        Path target = repoRoot.resolve(targetPath).normalize();
        if (!target.startsWith(repoRoot)) {
            return rejectedFile(source, "Target path escapes workspace repoRoot: " + targetPath, index);
        }

        try {
            String newContent = Files.readString(generatedPath);
            String oldContent = Files.exists(target) ? Files.readString(target) : "";
            boolean exists = Files.exists(target);
            PatchProposalChangeType changeType = exists
                    ? oldContent.equals(newContent) ? PatchProposalChangeType.NO_CHANGE : PatchProposalChangeType.UPDATE
                    : PatchProposalChangeType.CREATE;

            PatchProposalFile file = new PatchProposalFile();
            file.setFileId(fileId(targetPath, index));
            file.setTargetPath(targetPath);
            file.setGeneratedPath(generatedPath.toString());
            file.setChangeType(changeType);
            file.setBytesWritten(source.bytesWritten());
            file.setOldSha256(exists ? sha256(oldContent) : null);
            file.setNewSha256(sha256(newContent));
            if (changeType == PatchProposalChangeType.CREATE || changeType == PatchProposalChangeType.UPDATE) {
                String diff = buildUnifiedDiff(targetPath, exists ? oldContent : "", newContent, changeType);
                Path diffPath = diffPath(workspace, runId, targetPath);
                Files.createDirectories(diffPath.getParent());
                Files.writeString(diffPath, diff);
                file.setDiffPath(diffPath.toString());
            }
            return file;
        } catch (IOException ex) {
            return rejectedFile(source, "Failed to compare generated file: " + ex.getMessage(), index);
        }
    }

    private PatchProposal failedProposal(
            Workspace workspace,
            String runId,
            String reason,
            DeliveryRunRecord record
    ) {
        PatchProposal proposal = baseProposal(workspace, runId, record);
        proposal.setStatus(PatchProposalStatus.FAILED_TO_GENERATE);
        proposal.setRisks(List.of(reason == null || reason.isBlank() ? "Patch proposal generation failed." : reason));
        return proposal;
    }

    private PatchProposal baseProposal(Workspace workspace, String runId, DeliveryRunRecord record) {
        PatchProposal proposal = new PatchProposal();
        proposal.setProposalId(runId);
        proposal.setWorkspaceId(workspace.getWorkspaceId());
        proposal.setRunId(runId);
        proposal.setProposalPath(proposalPath(workspace, runId).toString());
        proposal.setVerificationSourceRunId(runId);
        VerificationResult verification = record == null || record.getResponse() == null
                || record.getResponse().getGeneration() == null
                ? null
                : record.getResponse().getGeneration().getFinalVerificationResult();
        proposal.setVerificationScope(verification == null ? UNVERIFIED : GENERATED_OUTPUT_VERIFIED);
        proposal.setVerificationSuccessful(verification == null ? null : verification.isSuccessful());
        proposal.setVerificationSummary(verification == null ? null : verification.getSummary());
        proposal.setGeneratedAt(Instant.now());
        return proposal;
    }

    private PatchProposal persist(Workspace workspace, String runId, PatchProposal proposal) {
        try {
            Path path = proposalPath(workspace, runId);
            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), proposal);
            return proposal;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist patch proposal for run " + runId, ex);
        }
    }

    private DeliveryRunRecord loadRecord(Workspace workspace, String runId) {
        return deliveryRunStore.find(runId)
                .filter(record -> workspace.getWorkspaceId().equals(record.getWorkspaceId()))
                .orElseThrow(() -> new IllegalArgumentException("Delivery run not found in workspace: " + runId));
    }

    private Workspace workspaceFor(DeliveryRunRecord record) {
        String workspaceId = record.getWorkspaceId();
        if (workspaceId == null || workspaceId.isBlank()) {
            return workspaceRepository.ensureDefaultWorkspace();
        }
        return workspaceRepository.find(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));
    }

    private List<GeneratedFileSource> generatedSources(AutoRepairGenerationResponse generation) {
        List<AutoRepairAttempt> attempts = generation.getAttempts();
        if (attempts != null && !attempts.isEmpty()) {
            List<GeneratedProjectFile> files = attempts.get(attempts.size() - 1).getWrittenFiles();
            if (files != null && !files.isEmpty()) {
                return files.stream()
                        .map(file -> new GeneratedFileSource(
                                file.getRelativePath(),
                                generatedPath(generation, file),
                                file.getBytesWritten()
                        ))
                        .toList();
            }
        }
        return generatedSourcesFromProjectRoot(generation);
    }

    private List<GeneratedFileSource> generatedSourcesFromProjectRoot(AutoRepairGenerationResponse generation) {
        if (generation.getProjectRoot() == null || generation.getProjectRoot().isBlank()) {
            return List.of();
        }
        Path projectRoot = Path.of(generation.getProjectRoot()).toAbsolutePath().normalize();
        if (!Files.isDirectory(projectRoot)) {
            return List.of();
        }
        try (var stream = Files.walk(projectRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.naturalOrder())
                    .map(file -> {
                        String relativePath = normalizePath(projectRoot.relativize(file).toString());
                        try {
                            return new GeneratedFileSource(relativePath, file.toString(), Files.size(file));
                        } catch (IOException ex) {
                            return new GeneratedFileSource(relativePath, file.toString(), 0L);
                        }
                    })
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to list generated project files " + projectRoot, ex);
        }
    }

    private String generatedPath(AutoRepairGenerationResponse generation, GeneratedProjectFile file) {
        if (file.getAbsolutePath() != null && !file.getAbsolutePath().isBlank()) {
            return file.getAbsolutePath();
        }
        if (generation.getProjectRoot() == null || generation.getProjectRoot().isBlank()) {
            return file.getRelativePath();
        }
        return Path.of(generation.getProjectRoot()).resolve(file.getRelativePath()).normalize().toString();
    }

    private TargetPathValidation validateTargetPath(String rawPath, Workspace workspace) {
        if (rawPath == null || rawPath.isBlank()) {
            return TargetPathValidation.rejected("Generated file path is blank.");
        }
        String normalizedInput = normalizePath(rawPath.trim());
        if (normalizedInput.matches("^[A-Za-z]:/.*") || normalizedInput.startsWith("/")) {
            return TargetPathValidation.rejected("Generated file path must be repo-relative: " + rawPath);
        }
        Path normalizedPath = Path.of(normalizedInput).normalize();
        if (normalizedPath.isAbsolute()) {
            return TargetPathValidation.rejected("Generated file path must be repo-relative: " + rawPath);
        }
        String targetPath = normalizePath(normalizedPath.toString());
        if (targetPath.isBlank() || ".".equals(targetPath)) {
            return TargetPathValidation.rejected("Generated file path is not a file path: " + rawPath);
        }
        if (targetPath.equals("..") || targetPath.startsWith("../") || targetPath.contains("/../")) {
            return TargetPathValidation.rejected("Generated file path escapes repoRoot: " + rawPath);
        }
        List<String> segments = Arrays.asList(targetPath.split("/"));
        for (String segment : segments) {
            if (DENIED_PATH_SEGMENTS.contains(segment)) {
                return TargetPathValidation.rejected("Generated file path targets a denied directory: " + segment);
            }
        }
        if (!isAllowedByWorkspace(targetPath, workspace)) {
            return TargetPathValidation.rejected("Generated file path is outside workspace allowedPaths: " + targetPath);
        }
        return TargetPathValidation.accepted(targetPath);
    }

    private boolean isAllowedByWorkspace(String targetPath, Workspace workspace) {
        List<String> allowedPaths = workspace.getAllowedPaths();
        if (allowedPaths == null || allowedPaths.isEmpty()) {
            return true;
        }
        for (String allowedPath : allowedPaths) {
            String normalized = normalizeAllowedPath(allowedPath);
            if (normalized == null || normalized.isBlank()) {
                continue;
            }
            if (".".equals(normalized)
                    || targetPath.equals(normalized)
                    || targetPath.startsWith(normalized + "/")) {
                return true;
            }
        }
        return false;
    }

    private String normalizeAllowedPath(String allowedPath) {
        if (allowedPath == null || allowedPath.isBlank()) {
            return null;
        }
        String normalizedInput = normalizePath(allowedPath.trim());
        if (".".equals(normalizedInput)) {
            return ".";
        }
        if (normalizedInput.matches("^[A-Za-z]:/.*") || normalizedInput.startsWith("/")) {
            return null;
        }
        Path path = Path.of(normalizedInput).normalize();
        String normalized = normalizePath(path.toString());
        if (normalized.equals("..") || normalized.startsWith("../") || normalized.contains("/../")) {
            return null;
        }
        return normalized;
    }

    private PatchProposalStatus statusFor(long changeCount, long rejectedCount) {
        if (rejectedCount > 0) {
            return PatchProposalStatus.HAS_REJECTED_FILES;
        }
        if (changeCount == 0) {
            return PatchProposalStatus.NO_CHANGES;
        }
        return PatchProposalStatus.READY;
    }

    private PatchProposalFile rejectedFile(GeneratedFileSource source, String reason, int index) {
        PatchProposalFile file = new PatchProposalFile();
        String targetPath = source.relativePath() == null || source.relativePath().isBlank()
                ? "rejected-" + index
                : normalizePath(source.relativePath());
        file.setFileId(fileId(targetPath, index));
        file.setTargetPath(targetPath);
        file.setGeneratedPath(source.generatedPath());
        file.setChangeType(PatchProposalChangeType.REJECTED);
        file.setBytesWritten(source.bytesWritten());
        file.setRejectedReason(reason);
        return file;
    }

    private Path proposalPath(Workspace workspace, String runId) {
        return proposalDir(workspace, runId).resolve("proposal.json");
    }

    private Path proposalDir(Workspace workspace, String runId) {
        return workspaceRoot
                .resolve(safeId(workspace.getWorkspaceId()))
                .resolve("patch-proposals")
                .resolve(safeId(runId));
    }

    private Path filesDir(Workspace workspace, String runId) {
        return proposalDir(workspace, runId).resolve("files").toAbsolutePath().normalize();
    }

    private Path diffPath(Workspace workspace, String runId, String targetPath) {
        Path filesDir = filesDir(workspace, runId);
        Path path = filesDir.resolve(targetPath + ".diff").normalize();
        if (!path.startsWith(filesDir)) {
            throw new IllegalArgumentException("Diff path escapes patch proposal files directory: " + targetPath);
        }
        return path;
    }

    private String buildUnifiedDiff(
            String targetPath,
            String oldContent,
            String newContent,
            PatchProposalChangeType changeType
    ) {
        List<String> oldLines = lines(oldContent);
        List<String> newLines = lines(newContent);
        StringBuilder builder = new StringBuilder();
        builder.append("--- ")
                .append(changeType == PatchProposalChangeType.CREATE ? "/dev/null" : "a/" + targetPath)
                .append("\n");
        builder.append("+++ b/").append(targetPath).append("\n");
        builder.append("@@ -1,").append(oldLines.size()).append(" +1,").append(newLines.size()).append(" @@\n");
        for (String line : oldLines) {
            builder.append("-").append(line).append("\n");
        }
        for (String line : newLines) {
            builder.append("+").append(line).append("\n");
        }
        return builder.toString();
    }

    private List<String> lines(String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(content.split("\\R", -1));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String fileId(String targetPath, int index) {
        return sha256(targetPath + ":" + index).substring(0, 16);
    }

    private String normalizePath(String value) {
        return value.replace('\\', '/');
    }

    private String safeId(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private record GeneratedFileSource(String relativePath, String generatedPath, long bytesWritten) {
    }

    private record TargetPathValidation(boolean accepted, String targetPath, String reason) {
        private static TargetPathValidation accepted(String targetPath) {
            return new TargetPathValidation(true, targetPath, null);
        }

        private static TargetPathValidation rejected(String reason) {
            return new TargetPathValidation(false, null, reason);
        }
    }
}
