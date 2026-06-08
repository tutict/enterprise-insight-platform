package com.tutict.eip.orchestrator.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tutict.eip.agentadapter.domain.AutoRepairAttempt;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.domain.GeneratedProjectFile;
import com.tutict.eip.agentadapter.domain.VerificationCommandResult;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.orchestrator.delivery.DeliveryRunRecord;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStore;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStoreProperties;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import com.tutict.eip.orchestrator.evidence.EvidencePackageService;
import com.tutict.eip.orchestrator.patchproposal.PatchProposalService;
import com.tutict.eip.orchestrator.project.ProjectAnalysisProperties;
import com.tutict.eip.orchestrator.project.ProjectScannerService;
import com.tutict.eip.orchestrator.runtime.RunEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkspaceControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void exportsWorkspaceEvidencePackage() throws Exception {
        Path repoRoot = tempDir.resolve("repo");
        write(repoRoot, "pom.xml", "<project><artifactId>demo</artifactId></project>");
        Path generatedRoot = tempDir.resolve("generated-fde-delivery");
        write(generatedRoot, "README.md", "ok");
        write(repoRoot, "src/main/java/com/acme/DemoController.java", """
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RequestMapping;

                @RequestMapping("/api/demo")
                class DemoController {
                    @GetMapping
                    String demo() {
                        return "ok";
                    }
                }
                """);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        WorkspaceStoreProperties workspaceProperties = new WorkspaceStoreProperties();
        workspaceProperties.setStorageRoot(tempDir.resolve("workspaces").toString());
        workspaceProperties.setDefaultWorkspaceId("demo-workspace");
        ProjectAnalysisProperties analysisProperties = new ProjectAnalysisProperties();
        analysisProperties.setRoot(repoRoot.toString());
        FileWorkspaceRepository workspaceRepository = new FileWorkspaceRepository(objectMapper, workspaceProperties, analysisProperties);
        WorkspaceRequest request = new WorkspaceRequest();
        request.setWorkspaceId("customer-a");
        request.setCustomerName("Customer A");
        request.setProjectName("Demo Repo");
        request.setRepoRoot(repoRoot.toString());
        workspaceRepository.save(request);

        DeliveryRunStoreProperties runProperties = new DeliveryRunStoreProperties();
        runProperties.setStorageRoot(tempDir.resolve("delivery-runs").toString());
        DeliveryRunStore deliveryRunStore = new DeliveryRunStore(objectMapper, runProperties);
        OrchestratorRunRequest runRequest = new OrchestratorRunRequest();
        runRequest.setRunId("run-1");
        runRequest.setWorkspaceId("customer-a");
        runRequest.setRequirement("Build a workspace evidence package");
        runRequest.setTargetDirectory("generated-fde-delivery");
        runRequest.setVerifyCommands(List.of(List.of("mvn", "test")));
        deliveryRunStore.create("run-1", runRequest);
        deliveryRunStore.appendEvent(RunEvent.of("run-1", "RUN_REQUESTED", null, Map.of("config", runRequest)));
        deliveryRunStore.appendEvent(RunEvent.of("run-1", "RUN_COMPLETED", null, Map.of("result", response(generatedRoot))));

        PatchProposalService patchProposalService = new PatchProposalService(
                deliveryRunStore,
                workspaceRepository,
                objectMapper,
                workspaceProperties
        );

        WorkspaceController controller = new WorkspaceController(
                workspaceRepository,
                new ProjectScannerService(analysisProperties),
                deliveryRunStore,
                new EvidencePackageService(deliveryRunStore, patchProposalService, objectMapper, workspaceProperties),
                patchProposalService
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/workspaces/customer-a/project-analysis/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rootPath").value(repoRoot.toString()))
                .andExpect(jsonPath("$.data.summary.apiEndpointCount").value(1));

        mockMvc.perform(get("/api/workspaces/customer-a/delivery-runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].workspaceId").value("customer-a"))
                .andExpect(jsonPath("$.data[0].runId").value("run-1"));

        mockMvc.perform(get("/api/workspaces/customer-a/delivery-runs/run-1/patch-proposal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"))
                .andExpect(jsonPath("$.data.files[0].targetPath").value("README.md"))
                .andExpect(jsonPath("$.data.files[0].changeType").value("CREATE"));

        mockMvc.perform(get("/api/workspaces/customer-a/delivery-runs/run-1/evidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workspaceId").value("customer-a"))
                .andExpect(jsonPath("$.data.markdown").value(org.hamcrest.Matchers.containsString("# FDE Delivery Evidence")))
                .andExpect(jsonPath("$.data.markdown").value(org.hamcrest.Matchers.containsString("Build a workspace evidence package")))
                .andExpect(jsonPath("$.data.markdown").value(org.hamcrest.Matchers.containsString("## Patch Proposal")))
                .andExpect(jsonPath("$.data.patchProposal.status").value("READY"));

        Path evidenceMarkdown = tempDir.resolve("workspaces/customer-a/evidence/run-1/evidence.md");
        Path evidenceJson = tempDir.resolve("workspaces/customer-a/evidence/run-1/evidence.json");
        Path proposalJson = tempDir.resolve("workspaces/customer-a/patch-proposals/run-1/proposal.json");
        assertThat(evidenceMarkdown).exists();
        assertThat(evidenceJson).exists();
        assertThat(proposalJson).exists();
    }

    private OrchestratorRunResponse response(Path generatedRoot) {
        DslModel dsl = new DslModel(
                "workspace-evidence",
                "spring-boot-backend",
                "Build a workspace evidence package",
                List.of("api"),
                new LinkedHashMap<>(),
                "Return files"
        );
        VerificationResult verification = new VerificationResult(
                true,
                "All verification commands passed",
                List.of(new VerificationCommandResult("mvn test", 0, false, "ok", "", 123))
        );
        AutoRepairGenerationResponse generation = new AutoRepairGenerationResponse(
                true,
                "VERIFIED",
                generatedRoot.toString(),
                1,
                "===FILE START===\nREADME.md\nok\n===FILE END===",
                verification,
                List.of(new AutoRepairAttempt(
                        1,
                        true,
                        "prompt",
                        "===FILE START===\nREADME.md\nok\n===FILE END===",
                        List.of(new GeneratedProjectFile(
                                "README.md",
                                generatedRoot.resolve("README.md").toString(),
                                2
                        )),
                        verification
                ))
        );
        return new OrchestratorRunResponse("run-1", dsl, "compiled prompt", generation, Instant.now());
    }

    private void write(Path root, String relativePath, String content) throws IOException {
        Path target = root.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
    }
}
