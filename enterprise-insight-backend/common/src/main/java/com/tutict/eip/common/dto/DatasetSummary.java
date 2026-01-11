package com.tutict.eip.common.dto;

import java.time.Instant;

// 数据集摘要 DTO
public record DatasetSummary(
        String code,
        String name,
        String type,
        int metrics,
        Instant updatedAt
) {}
