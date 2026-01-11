package com.tutict.eip.analysis;

import com.tutict.eip.analysis.client.MetadataClient;
import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.dto.DatasetSummary;
import com.tutict.eip.common.security.RequireRoles;
import com.tutict.eip.common.security.RoleConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class DatasetProxyController {

    private final MetadataClient metadataClient;

    public DatasetProxyController(MetadataClient metadataClient) {
        this.metadataClient = metadataClient;
    }

    @GetMapping("/datasets")
    @RequireRoles({RoleConstants.ANALYST})
    // 通过 Feign 调用 metadata-service 获取数据集
    public ApiResponse<List<DatasetSummary>> datasets() {
        return metadataClient.datasets();
    }
}
