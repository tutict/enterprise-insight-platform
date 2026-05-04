package com.tutict.eip.orchestrator.experiment;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExperimentAssignmentServiceTest {

    private final ExperimentAssignmentService service = new ExperimentAssignmentService();

    @Test
    void assignsTheSameSubjectToTheSameVariant() {
        List<ExperimentVariant> variants = List.of(
                new ExperimentVariant("control", 50, Map.of("model", "llama3.1")),
                new ExperimentVariant("treatment", 50, Map.of("model", "qwen2.5"))
        );

        ExperimentAssignment first = service.assign("prompt-profile-v2", "tenant-a:user-42", variants);
        ExperimentAssignment second = service.assign("prompt-profile-v2", "tenant-a:user-42", variants);

        assertThat(second).isEqualTo(first);
        assertThat(first.bucket()).isBetween(0, 9_999);
    }

    @Test
    void honorsZeroWeightVariants() {
        ExperimentAssignment assignment = service.assign(
                "compiler-template",
                "tenant-a",
                List.of(
                        new ExperimentVariant("disabled", 0, Map.of()),
                        new ExperimentVariant("enabled", 100, Map.of())
                )
        );

        assertThat(assignment.variantId()).isEqualTo("enabled");
    }

    @Test
    void rejectsInvalidExperimentDefinitions() {
        assertThatThrownBy(() -> service.assign("compiler-template", "tenant-a", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("variants must not be empty");
    }
}
