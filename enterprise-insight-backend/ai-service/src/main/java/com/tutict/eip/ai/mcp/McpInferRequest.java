package com.tutict.eip.ai.mcp;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record McpInferRequest(
        @NotBlank String prompt,
        String context,
        Integer topK,
        Map<String, Object> options
) {}
