package com.tutict.eip.metadata;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.dto.DataSourceInfo;
import com.tutict.eip.common.dto.DatasetSummary;
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

    @GetMapping("/datasets")
    @RequireRoles({RoleConstants.ANALYST})
    // Mock 数据集清单
    public ApiResponse<List<DatasetSummary>> datasets() {
        List<DatasetSummary> data = List.of(
                new DatasetSummary("sales_daily", "Daily Sales", "mysql", 12, Instant.now()),
                new DatasetSummary("marketing_roi", "Marketing ROI", "mysql", 8, Instant.now())
        );
        return ApiResponse.ok(data);
    }

    @GetMapping("/sources")
    @RequireRoles({RoleConstants.ADMIN})
    // Mock 数据源清单（仅管理员可见）
    public ApiResponse<List<DataSourceInfo>> sources() {
        List<DataSourceInfo> data = List.of(
                new DataSourceInfo("primary-mysql", "MySQL", "jdbc:mysql://localhost:3306/eip"),
                new DataSourceInfo("ods-postgres", "PostgreSQL", "jdbc:postgresql://localhost:5432/ods")
        );
        return ApiResponse.ok(data);
    }
}
