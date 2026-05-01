package com.tutict.eip.ai;

import com.tutict.eip.ai.rag.RagService;
import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.security.RequireRoles;
import com.tutict.eip.common.security.RoleConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AgentController {

    private final RagService ragService;

    public AgentController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/agent/ask")
    @RequireRoles({RoleConstants.ANALYST})
    public ApiResponse<AgentAnswer> ask(@Valid @RequestBody AgentQuery query) {
        AgentAnswer answer = ragService.answer(query.question());
        return ApiResponse.ok("agent knowledge response", answer);
    }

    public record AgentQuery(@NotBlank String question) {
    }

    public record AgentAnswer(String reply, String intent, List<String> artifacts, List<String> nextActions) {
    }
}
