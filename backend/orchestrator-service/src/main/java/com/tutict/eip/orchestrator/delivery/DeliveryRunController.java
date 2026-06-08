package com.tutict.eip.orchestrator.delivery;

import com.tutict.eip.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orchestrator/delivery-runs")
public class DeliveryRunController {

    private final DeliveryRunStore deliveryRunStore;

    public DeliveryRunController(DeliveryRunStore deliveryRunStore) {
        this.deliveryRunStore = deliveryRunStore;
    }

    @GetMapping
    public ApiResponse<List<DeliveryRunRecord>> list(@RequestParam(value = "workspaceId", required = false) String workspaceId) {
        List<DeliveryRunRecord> runs = workspaceId == null || workspaceId.isBlank()
                ? deliveryRunStore.list()
                : deliveryRunStore.list(workspaceId);
        return ApiResponse.ok("delivery runs loaded", runs);
    }

    @GetMapping("/{runId}")
    public ApiResponse<DeliveryRunRecord> get(@PathVariable String runId) {
        return ApiResponse.ok(
                "delivery run loaded",
                deliveryRunStore.find(runId)
                        .orElseThrow(() -> new IllegalArgumentException("Delivery run not found: " + runId))
        );
    }
}
