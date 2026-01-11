package com.tutict.eip.ai;

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

    @PostMapping("/agent/ask")
    @RequireRoles({RoleConstants.ANALYST})
    // Mock AI Agent：返回建议 SQL 与下一步动作
    public ApiResponse<AgentAnswer> ask(@Valid @RequestBody AgentQuery query) {
        AgentAnswer answer = new AgentAnswer(
                "Received: " + query.question() + ". This is a mock agent. Check revenue and orders trend.",
                "insight_query",
                "SELECT day, revenue, orders FROM sales_daily WHERE day >= CURRENT_DATE - INTERVAL '7 days'",
                List.of("View last 30 days revenue", "Compare channel ROI", "Top 10 customers")
        );
        return ApiResponse.ok("mock agent response", answer);
    }

    // 问答请求 DTO
    public record AgentQuery(@NotBlank String question) {}

    // 问答响应 DTO
    public record AgentAnswer(String reply, String intent, String suggestedSql, List<String> nextActions) {}
}
