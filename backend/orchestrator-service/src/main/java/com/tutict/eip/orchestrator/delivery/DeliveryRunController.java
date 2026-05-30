package com.tutict.eip.orchestrator.delivery;

import com.tutict.eip.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ApiResponse<List<DeliveryRunRecord>> list() {
        return ApiResponse.ok("delivery runs loaded", deliveryRunStore.list());
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
