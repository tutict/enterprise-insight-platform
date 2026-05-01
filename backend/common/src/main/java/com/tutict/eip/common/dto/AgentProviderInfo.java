package com.tutict.eip.common.dto;

public record AgentProviderInfo(
        String provider,
        String model,
        String endpoint,
        String status
) {
}
