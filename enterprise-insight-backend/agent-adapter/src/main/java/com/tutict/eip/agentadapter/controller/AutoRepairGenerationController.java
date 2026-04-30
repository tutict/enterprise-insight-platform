package com.tutict.eip.agentadapter.controller;

import com.tutict.eip.agentadapter.domain.AutoRepairGenerationRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.service.AutoRepairGenerationService;
import com.tutict.eip.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent-adapter/auto-repair")
public class AutoRepairGenerationController {

    private final AutoRepairGenerationService autoRepairGenerationService;

    public AutoRepairGenerationController(AutoRepairGenerationService autoRepairGenerationService) {
        this.autoRepairGenerationService = autoRepairGenerationService;
    }

    @PostMapping("/generate")
    public ApiResponse<AutoRepairGenerationResponse> generateAndRepair(
            @Valid @RequestBody AutoRepairGenerationRequest request
    ) {
        return ApiResponse.ok(autoRepairGenerationService.generateAndRepair(request));
    }
}
