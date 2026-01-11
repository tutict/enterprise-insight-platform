package com.tutict.eip.analysis.client;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.dto.DatasetSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "metadata-service", fallbackFactory = MetadataClientFallbackFactory.class)
public interface MetadataClient {

    @GetMapping("/api/metadata/datasets")
    ApiResponse<List<DatasetSummary>> datasets();
}
