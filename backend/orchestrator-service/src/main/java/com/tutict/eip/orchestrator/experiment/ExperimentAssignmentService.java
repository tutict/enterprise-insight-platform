package com.tutict.eip.orchestrator.experiment;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class ExperimentAssignmentService {

    private static final int BUCKETS = 10_000;

    public ExperimentAssignment assign(String experimentKey, String subjectKey, List<ExperimentVariant> variants) {
        if (experimentKey == null || experimentKey.isBlank()) {
            throw new IllegalArgumentException("experimentKey must not be blank");
        }
        if (subjectKey == null || subjectKey.isBlank()) {
            throw new IllegalArgumentException("subjectKey must not be blank");
        }
        if (variants == null || variants.isEmpty()) {
            throw new IllegalArgumentException("variants must not be empty");
        }

        int totalWeight = variants.stream().mapToInt(ExperimentVariant::weight).sum();
        if (totalWeight <= 0) {
            throw new IllegalArgumentException("total variant weight must be greater than zero");
        }

        int bucket = bucket(experimentKey, subjectKey);
        int slot = Math.floorMod(bucket, totalWeight);
        int cursor = 0;
        for (ExperimentVariant variant : variants) {
            if (variant.weight() < 0) {
                throw new IllegalArgumentException("variant weight must not be negative: " + variant.id());
            }
            cursor += variant.weight();
            if (slot < cursor) {
                return new ExperimentAssignment(experimentKey, subjectKey, variant.id(), bucket);
            }
        }

        ExperimentVariant fallback = variants.getLast();
        return new ExperimentAssignment(experimentKey, subjectKey, fallback.id(), bucket);
    }

    private int bucket(String experimentKey, String subjectKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((experimentKey + ":" + subjectKey).getBytes(StandardCharsets.UTF_8));
            int value = ((hash[0] & 0xff) << 24)
                    | ((hash[1] & 0xff) << 16)
                    | ((hash[2] & 0xff) << 8)
                    | (hash[3] & 0xff);
            return Math.floorMod(value, BUCKETS);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required by the JVM", ex);
        }
    }
}
