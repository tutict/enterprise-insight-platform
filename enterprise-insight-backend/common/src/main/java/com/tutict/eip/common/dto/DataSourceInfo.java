package com.tutict.eip.common.dto;

// 数据源信息 DTO
public record DataSourceInfo(
        String name,
        String type,
        String jdbcUrl
) {}
