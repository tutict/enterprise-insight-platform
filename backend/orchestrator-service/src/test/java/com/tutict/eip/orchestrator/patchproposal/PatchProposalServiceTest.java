package com.tutict.eip.orchestrator.patchproposal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tutict.eip.agentadapter.domain.AutoRepairAttempt;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.domain.GeneratedProjectFile;
import com.tutict.eip.agentadapter.domain.VerificationCommandResult;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStore;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStoreProperties;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import com.tutict.eip.orchestrator.project.ProjectAnalysisProperties;
import com.tutict.eip.orchestrator.runtime.RunEvent;
import com.tutict.eip.orchestrator.workspace.FileWorkspaceRepository;
import com.tutict.eip.orchestrator.workspace.Workspace;
import com.tutict.eip.orchestrator.workspace.WorkspaceRequest;
import com.tutict.eip.orchestrator.workspace.WorkspaceStoreProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PatchProposalServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesPatchProposalAndRejectsUnsafePaths() throws Exception {
        Path repoRoot = tempDir.resolve("repo");
        write(repoRoot, "src/main/java/App.java", "class App {}\n");
        write(repoRoot, "README.md", "same\n");

        Path generatedRoot = tempDir.resolve("generated");
        write(generatedRoot, "src/main/java/App.java", "class App { String ok() { return \"ok\"; } }\n");
        write(generatedRoot, "docs/new.md", "# New doc\n");
        write(generatedRoot, "README.md", "same\n");
        write(generatedRoot, "escape.java", "class Escape {}\n");
        write(generatedRoot, "backend/target/generated.txt", "bad\n");

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        WorkspaceStoreProperties workspaceProperties = new WorkspaceStoreProperties();
        workspaceProperties.setStorageRoot(tempDir.resolve("workspaces").toString());
        ProjectAnalysisProperties analysisProperties = new ProjectAnalysisProperties();
        analysisProperties.setRoot(repoRoot.toString());
        FileWorkspaceRepository workspaceRepository = new FileWorkspaceRepository(
                objectMapper,
                workspaceProperties,
                analysisProperties
        );
        Workspace workspace = workspaceRepository.save(workspaceRequest(repoRoot));

        DeliveryRunStoreProperties runProperties = new DeliveryRunStoreProperties();
        runProperties.setStorageRoot(tempDir.resolve("delivery-runs").toString());
        DeliveryRunStore deliveryRunStore = new DeliveryRunStore(objectMapper, runProperties);
        OrchestratorRunRequest request = new OrchestratorRunRequest();
        request.setRunId("run-1");
        request.setWorkspaceId("customer-a");
        request.setRequirement("Create a review-ready patch proposal");
        request.setTargetDirectory("generated");
        request.setVerifyCommands(List.of(List.of("mvn", "test")));
        deliveryRunStore.create("run-1", request);
        deliveryRunStore.appendEvent(RunEvent.of("run-1", "RUN_REQUESTED", null, Map.of("config", request)));
        deliveryRunStore.appendEvent(RunEvent.of("run-1", "RUN_COMPLETED", null, Map.of("result", response(generatedRoot))));

        PatchProposalService service = new PatchProposalService(
                deliveryRunStore,
                workspaceRepository,
                objectMapper,
                workspaceProperties
        );

        PatchProposal proposal = service.regenerate(workspace, "run-1");

        assertThat(proposal.getStatus()).isEqualTo(PatchProposalStatus.HAS_REJECTED_FILES);
        assertThat(proposal.getChangeCount()).isEqualTo(2);
        assertThat(proposal.getRejectedCount()).isEqualTo(2);
        assertThat(proposal.getVerificationScope()).isEqualTo("generated-output-verified");
        assertThat(proposal.getVerificationSuccessful()).isTrue();
        assertThat(proposal.getFiles()).extracting(PatchProposalFile::getChangeType)
                .contains(
                        PatchProposalChangeType.UPDATE,
                        PatchProposalChangeType.CREATE,
                        PatchProposalChangeType.NO_CHANGE,
                        PatchProposalChangeType.REJECTED
                );

        PatchProposalFile update = proposal.getFiles().stream()
                .filter(file -> "src/main/java/App.java".equals(file.getTargetPath()))
                .findFirst()
                .orElseThrow();
        PatchProposalDiff diff = service.readDiff(workspace, "run-1", update.getFileId());
        assertThat(diff.diff())
                .contains("--- a/src/main/java/App.java")
                .contains("+++ b/src/main/java/App.java")
                .contains("-class App {}")
                .contains("+class App { String ok() { return \"ok\"; } }");
        assertThat(Path.of(update.getDiffPath())).exists();

        PatchProposalFile escaped = proposal.getFiles().stream()
                .filter(file -> "../escape.java".equals(file.getTargetPath()))
                .findFirst()
                .orElseThrow();
        assertThat(escaped.getRejectedReason()).contains("escapes repoRoot");
        assertThat(service.readDiff(workspace, "run-1", escaped.getFileId()).diff()).isEmpty();
        assertThat(tempDir.resolve("workspaces/customer-a/patch-proposals/run-1/proposal.json")).exists();
    }

    private WorkspaceRequest workspaceRequest(Path repoRoot) {
        WorkspaceRequest request = new WorkspaceRequest();
        request.setWorkspaceId("customer-a");
        request.setCustomerName("Customer A");
        request.setProjectName("Demo Repo");
        request.setRepoRoot(repoRoot.toString());
        request.setAllowedPaths(List.of("."));
        request.setVerifyCommands(List.of(List.of("mvn", "test")));
        return request;
    }

    private OrchestratorRunResponse response(Path generatedRoot) throws IOException {
        DslModel dsl = new DslModel(
                "patch-proposal",
                "spring-boot-backend",
                "Create a review-ready patch proposal",
                List.of("api"),
                new LinkedHashMap<>(),
                "Return files"
        );
        VerificationResult verification = new VerificationResult(
                true,
                "All verification commands passed",
                List.of(new VerificationCommandResult("mvn test", 0, false, "ok", "", 25))
        );
        List<GeneratedProjectFile> files = List.of(
                generatedFile(generatedRoot, "src/main/java/App.java"),
                generatedFile(generatedRoot, "docs/new.md"),
                generatedFile(generatedRoot, "README.md"),
                new GeneratedProjectFile("../escape.java", generatedRoot.resolve("escape.java").toString(), 16),
                generatedFile(generatedRoot, "backend/target/generated.txt")
        );
        AutoRepairGenerationResponse generation = new AutoRepairGenerationResponse(
                true,
                "VERIFIED",
                generatedRoot.toString(),
                1,
                "files",
                verification,
                List.of(new AutoRepairAttempt(1, true, "prompt", "files", files, verification))
        );
        return new OrchestratorRunResponse("run-1", dsl, "compiled prompt", generation, Instant.now());
    }

    private GeneratedProjectFile generatedFile(Path root, String relativePath) throws IOException {
        Path path = root.resolve(relativePath);
        return new GeneratedProjectFile(relativePath, path.toString(), Files.size(path));
    }

    private void write(Path root, String relativePath, String content) throws IOException {
        Path target = root.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
    }
}
