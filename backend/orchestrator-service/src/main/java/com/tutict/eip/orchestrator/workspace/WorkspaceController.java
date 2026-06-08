package com.tutict.eip.orchestrator.workspace;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.orchestrator.delivery.DeliveryRunRecord;
import com.tutict.eip.orchestrator.delivery.DeliveryRunStore;
import com.tutict.eip.orchestrator.evidence.EvidencePackage;
import com.tutict.eip.orchestrator.evidence.EvidencePackageService;
import com.tutict.eip.orchestrator.patchproposal.PatchProposal;
import com.tutict.eip.orchestrator.patchproposal.PatchProposalDiff;
import com.tutict.eip.orchestrator.patchproposal.PatchProposalService;
import com.tutict.eip.orchestrator.project.ProjectDeliveryBrief;
import com.tutict.eip.orchestrator.project.ProjectInventory;
import com.tutict.eip.orchestrator.project.ProjectScannerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceRepository workspaceRepository;
    private final ProjectScannerService projectScannerService;
    private final DeliveryRunStore deliveryRunStore;
    private final EvidencePackageService evidencePackageService;
    private final PatchProposalService patchProposalService;

    public WorkspaceController(
            WorkspaceRepository workspaceRepository,
            ProjectScannerService projectScannerService,
            DeliveryRunStore deliveryRunStore,
            EvidencePackageService evidencePackageService,
            PatchProposalService patchProposalService
    ) {
        this.workspaceRepository = workspaceRepository;
        this.projectScannerService = projectScannerService;
        this.deliveryRunStore = deliveryRunStore;
        this.evidencePackageService = evidencePackageService;
        this.patchProposalService = patchProposalService;
    }

    @GetMapping
    public ApiResponse<List<Workspace>> list() {
        return ApiResponse.ok("workspaces loaded", workspaceRepository.list());
    }

    @PostMapping
    public ApiResponse<Workspace> save(@Valid @RequestBody WorkspaceRequest request) {
        return ApiResponse.ok("workspace saved", workspaceRepository.save(request));
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<Workspace> get(@PathVariable("workspaceId") String workspaceId) {
        return ApiResponse.ok("workspace loaded", workspace(workspaceId));
    }

    @GetMapping("/{workspaceId}/project-analysis/current")
    public ApiResponse<ProjectInventory> current(@PathVariable("workspaceId") String workspaceId) {
        return ApiResponse.ok("workspace project analysis loaded", projectScannerService.scanProject(workspace(workspaceId).getRepoRoot()));
    }

    @GetMapping("/{workspaceId}/project-analysis/current/delivery-brief")
    public ApiResponse<ProjectDeliveryBrief> deliveryBrief(@PathVariable("workspaceId") String workspaceId) {
        return ApiResponse.ok("workspace project delivery brief loaded", projectScannerService.createDeliveryBrief(workspace(workspaceId).getRepoRoot()));
    }

    @GetMapping("/{workspaceId}/delivery-runs")
    public ApiResponse<List<DeliveryRunRecord>> deliveryRuns(@PathVariable("workspaceId") String workspaceId) {
        return ApiResponse.ok("workspace delivery runs loaded", deliveryRunStore.list(workspaceId));
    }

    @GetMapping("/{workspaceId}/delivery-runs/{runId}/evidence")
    public ApiResponse<EvidencePackage> evidence(
            @PathVariable("workspaceId") String workspaceId,
            @PathVariable("runId") String runId
    ) {
        return ApiResponse.ok("evidence package exported", evidencePackageService.export(workspace(workspaceId), runId));
    }

    @GetMapping("/{workspaceId}/delivery-runs/{runId}/patch-proposal")
    public ApiResponse<PatchProposal> patchProposal(
            @PathVariable("workspaceId") String workspaceId,
            @PathVariable("runId") String runId
    ) {
        return ApiResponse.ok("patch proposal loaded", patchProposalService.getOrGenerate(workspace(workspaceId), runId));
    }

    @PostMapping("/{workspaceId}/delivery-runs/{runId}/patch-proposal/regenerate")
    public ApiResponse<PatchProposal> regeneratePatchProposal(
            @PathVariable("workspaceId") String workspaceId,
            @PathVariable("runId") String runId
    ) {
        return ApiResponse.ok("patch proposal regenerated", patchProposalService.regenerate(workspace(workspaceId), runId));
    }

    @GetMapping("/{workspaceId}/delivery-runs/{runId}/patch-proposal/files/{fileId}/diff")
    public ApiResponse<PatchProposalDiff> patchProposalDiff(
            @PathVariable("workspaceId") String workspaceId,
            @PathVariable("runId") String runId,
            @PathVariable("fileId") String fileId
    ) {
        return ApiResponse.ok("patch proposal diff loaded", patchProposalService.readDiff(workspace(workspaceId), runId, fileId));
    }

    private Workspace workspace(String workspaceId) {
        return workspaceRepository.find(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));
    }
}
