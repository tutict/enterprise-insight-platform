package com.tutict.eip.common.dto;

import java.time.Instant;
import java.util.List;

public record HarnessTemplateSummary(
        String name,
        String version,
        List<String> sections,
        Instant updatedAt
) {
}
