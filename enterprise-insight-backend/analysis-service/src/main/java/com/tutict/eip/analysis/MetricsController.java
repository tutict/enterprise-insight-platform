package com.tutict.eip.analysis;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.security.RequireRoles;
import com.tutict.eip.common.security.RoleConstants;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class MetricsController {

    @GetMapping("/metrics")
    @RequireRoles({RoleConstants.ANALYST})
    // Mock 指标卡片：用于前端展示
    public ApiResponse<List<MetricCard>> metrics(@RequestParam(defaultValue = "sales_daily") String dataset) {
        List<MetricCard> data = List.of(
                new MetricCard("营收", new BigDecimal("128.6"), "万元", "+12%", Instant.now(), dataset),
                new MetricCard("订单数", new BigDecimal("3560"), "单", "+4.1%", Instant.now(), dataset),
                new MetricCard("客单价", new BigDecimal("361"), "元", "-1.3%", Instant.now(), dataset)
        );
        return ApiResponse.ok(data);
    }

    // 指标卡片 DTO
    public record MetricCard(
            @NotBlank String name,
            BigDecimal value,
            String unit,
            String trend,
            Instant updatedAt,
            String dataset
    ) {}
}
