package com.tutict.eip.ai.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.security.RequireRoles;
import com.tutict.eip.common.security.RoleConstants;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/mcp")
public class McpController {

    private final McpService mcpService;

    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @PostMapping("/infer")
    @RequireRoles({RoleConstants.ANALYST})
    public ApiResponse<JsonNode> infer(@Valid @RequestBody McpInferRequest request) {
        return ApiResponse.ok(mcpService.infer(request));
    }

    @PostMapping("/retrieve")
    @RequireRoles({RoleConstants.ANALYST})
    public ApiResponse<JsonNode> retrieve(@Valid @RequestBody McpRetrieveRequest request) {
        return ApiResponse.ok(mcpService.retrieve(request));
    }
}
