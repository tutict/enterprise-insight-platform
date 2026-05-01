package com.tutict.eip.ai.mcp;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record McpRetrieveRequest(
        @NotBlank String query,
        Integer topK,
        Map<String, Object> filters
) {}
