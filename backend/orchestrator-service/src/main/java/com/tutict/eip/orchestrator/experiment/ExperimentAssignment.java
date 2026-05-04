package com.tutict.eip.orchestrator.experiment;

public record ExperimentAssignment(
        String experimentKey,
        String subjectKey,
        String variantId,
        int bucket
) {
}
