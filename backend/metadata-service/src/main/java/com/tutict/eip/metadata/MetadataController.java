package com.tutict.eip.metadata;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.dto.AgentProviderInfo;
import com.tutict.eip.common.dto.HarnessTemplateSummary;
import com.tutict.eip.common.security.RequireRoles;
import com.tutict.eip.common.security.RoleConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    @GetMapping("/templates")
    @RequireRoles({RoleConstants.ANALYST})
    public ApiResponse<List<HarnessTemplateSummary>> templates() {
        List<HarnessTemplateSummary> data = List.of(
                new HarnessTemplateSummary(
                        "harness-default",
                        "1.0.0",
                        List.of("ROLE", "GOAL", "MODULES", "CONSTRAINTS", "OUTPUT FORMAT"),
                        Instant.now()
                ),
                new HarnessTemplateSummary(
                        "repair-feedback",
                        "1.0.0",
                        List.of("ERROR CONTEXT", "FAILED COMMAND", "EXPECTED FIX", "OUTPUT FORMAT"),
                        Instant.now()
                )
        );
        return ApiResponse.ok(data);
    }

    @GetMapping("/agents")
    @RequireRoles({RoleConstants.ANALYST})
    public ApiResponse<List<AgentProviderInfo>> agents() {
        List<AgentProviderInfo> data = List.of(
                new AgentProviderInfo("ollama", "llama3.1", "http://localhost:11434/api/generate", "configured"),
                new AgentProviderInfo("agent-adapter", "local-default", "/api/agent-adapter/generate", "available")
        );
        return ApiResponse.ok(data);
    }
}
