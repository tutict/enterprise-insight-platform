package com.tutict.eip.orchestrator.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.orchestrator.project.ProjectAnalysisProperties;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class FileWorkspaceRepository implements WorkspaceRepository {

    private final ObjectMapper objectMapper;
    private final WorkspaceStoreProperties properties;
    private final ProjectAnalysisProperties projectAnalysisProperties;
    private final Path storageRoot;

    public FileWorkspaceRepository(
            ObjectMapper objectMapper,
            WorkspaceStoreProperties properties,
            ProjectAnalysisProperties projectAnalysisProperties
    ) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.projectAnalysisProperties = projectAnalysisProperties;
        this.storageRoot = Path.of(properties.getStorageRoot()).toAbsolutePath().normalize();
    }

    @Override
    public Workspace save(WorkspaceRequest request) {
        Workspace existing = find(request.getWorkspaceId()).orElse(null);
        Instant now = Instant.now();
        Workspace workspace = new Workspace();
        workspace.setWorkspaceId(safeId(request.getWorkspaceId()));
        workspace.setCustomerName(request.getCustomerName());
        workspace.setProjectName(request.getProjectName());
        workspace.setRepoRoot(normalizeRepoRoot(request.getRepoRoot()));
        workspace.setDefaultBranch(blankToDefault(request.getDefaultBranch(), "main"));
        workspace.setWorkingBranch(request.getWorkingBranch());
        workspace.setWorktreePath(request.getWorktreePath());
        workspace.setAllowedPaths(request.getAllowedPaths());
        workspace.setVerifyCommands(request.getVerifyCommands());
        workspace.setModelPolicy(blankToDefault(request.getModelPolicy(), "default"));
        workspace.setSecretPolicy(blankToDefault(request.getSecretPolicy(), "redact-known-sensitive-fields"));
        workspace.setCreatedAt(existing == null ? now : existing.getCreatedAt());
        workspace.setUpdatedAt(now);
        write(workspace);
        return workspace;
    }

    @Override
    public Workspace ensureDefaultWorkspace() {
        return find(properties.getDefaultWorkspaceId()).orElseGet(() -> {
            WorkspaceRequest request = new WorkspaceRequest();
            request.setWorkspaceId(properties.getDefaultWorkspaceId());
            request.setCustomerName("Demo Customer");
            request.setProjectName("Enterprise Insight Platform");
            request.setRepoRoot(defaultRepoRoot());
            request.setAllowedPaths(List.of("."));
            request.setVerifyCommands(List.of(List.of("mvn", "test")));
            request.setDefaultBranch("main");
            return save(request);
        });
    }

    @Override
    public Optional<Workspace> find(String workspaceId) {
        Path path = workspacePath(workspaceId);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(path.toFile(), Workspace.class));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read workspace " + workspaceId, ex);
        }
    }

    @Override
    public List<Workspace> list() {
        ensureDefaultWorkspace();
        if (!Files.exists(storageRoot)) {
            return List.of();
        }
        try (var stream = Files.list(storageRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.resolve("workspace.json"))
                    .filter(Files::exists)
                    .map(path -> {
                        try {
                            return objectMapper.readValue(path.toFile(), Workspace.class);
                        } catch (IOException ex) {
                            throw new IllegalStateException("Failed to read workspace " + path, ex);
                        }
                    })
                    .sorted(Comparator.comparing(
                            Workspace::getUpdatedAt,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    ).reversed())
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to list workspaces", ex);
        }
    }

    private void write(Workspace workspace) {
        try {
            Path path = workspacePath(workspace.getWorkspaceId());
            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), workspace);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist workspace " + workspace.getWorkspaceId(), ex);
        }
    }

    private Path workspacePath(String workspaceId) {
        return storageRoot.resolve(safeId(workspaceId)).resolve("workspace.json");
    }

    private String normalizeRepoRoot(String repoRoot) {
        Path root = Path.of(repoRoot).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Workspace repoRoot is not a directory: " + root);
        }
        return root.toString();
    }

    private String defaultRepoRoot() {
        if (projectAnalysisProperties.getRoot() != null && !projectAnalysisProperties.getRoot().isBlank()) {
            return projectAnalysisProperties.getRoot();
        }
        Path current = Path.of("").toAbsolutePath().normalize();
        Path cursor = current;
        while (cursor != null) {
            if (Files.isDirectory(cursor.resolve("backend"))
                    && Files.isDirectory(cursor.resolve("enterprise-insight-backend-react"))) {
                return cursor.toString();
            }
            cursor = cursor.getParent();
        }
        return current.toString();
    }

    private String safeId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("workspaceId must not be blank");
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
