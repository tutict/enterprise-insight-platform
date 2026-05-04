package com.tutict.eip.orchestrator.experiment;

import java.util.LinkedHashMap;
import java.util.Map;

public record ExperimentVariant(
        String id,
        int weight,
        Map<String, Object> config
) {
    public ExperimentVariant {
        config = config == null ? new LinkedHashMap<>() : new LinkedHashMap<>(config);
    }
}
