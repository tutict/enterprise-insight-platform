package com.tutict.eip.harness.controller;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.harness.domain.HarnessRunRequest;
import com.tutict.eip.harness.domain.HarnessRunResponse;
import com.tutict.eip.harness.service.HarnessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/harness")
public class HarnessController {

    private final HarnessService harnessService;

    public HarnessController(HarnessService harnessService) {
        this.harnessService = harnessService;
    }

    @PostMapping("/run")
    public ApiResponse<HarnessRunResponse> run(@Valid @RequestBody HarnessRunRequest request) {
        return ApiResponse.ok(harnessService.run(request));
    }
}
