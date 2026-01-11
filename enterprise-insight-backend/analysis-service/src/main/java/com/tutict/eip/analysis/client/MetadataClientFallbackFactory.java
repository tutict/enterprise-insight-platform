package com.tutict.eip.analysis.client;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.ErrorCodes;
import com.tutict.eip.common.dto.DatasetSummary;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetadataClientFallbackFactory implements FallbackFactory<MetadataClient> {

    @Override
    public MetadataClient create(Throwable cause) {
        // 下游不可用时返回统一错误码
        return () -> ApiResponse.error("Metadata service unavailable", ErrorCodes.METADATA_UNAVAILABLE);
    }
}
