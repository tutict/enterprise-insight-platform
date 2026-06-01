package com.tutict.eip.orchestrator.project;

import com.tutict.eip.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/project-analysis")
public class ProjectAnalysisController {

    private final ProjectScannerService projectScannerService;

    public ProjectAnalysisController(ProjectScannerService projectScannerService) {
        this.projectScannerService = projectScannerService;
    }

    @GetMapping("/current")
    public ApiResponse<ProjectInventory> current() {
        return ApiResponse.ok("project analysis loaded", projectScannerService.scanCurrentProject());
    }

    @GetMapping("/current/delivery-brief")
    public ApiResponse<ProjectDeliveryBrief> currentDeliveryBrief() {
        return ApiResponse.ok("project delivery brief loaded", projectScannerService.createDeliveryBrief());
    }
}
