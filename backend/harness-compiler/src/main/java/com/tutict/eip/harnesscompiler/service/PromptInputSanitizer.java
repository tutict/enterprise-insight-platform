package com.tutict.eip.harnesscompiler.service;

import java.util.regex.Pattern;

final class PromptInputSanitizer {

    private static final Pattern INSTRUCTION_OVERRIDE = Pattern.compile(
            "(?i)\\b(ignore|disregard|override)\\s+(all\\s+)?(previous|prior|above|system|developer)\\s+instructions?\\b"
    );
    private static final Pattern PROMPT_EXFILTRATION = Pattern.compile(
            "(?i)\\breveal\\s+(the\\s+)?(system|developer)\\s+prompt\\b"
    );

    private PromptInputSanitizer() {
    }

    static String sanitizeUntrustedText(Object value) {
        if (value == null) {
            return "";
        }

        String sanitized = String.valueOf(value)
                .replace("\r", "")
                .replace("```", "` ` `");
        sanitized = INSTRUCTION_OVERRIDE.matcher(sanitized).replaceAll("[blocked instruction override]");
        sanitized = PROMPT_EXFILTRATION.matcher(sanitized).replaceAll("[blocked prompt exfiltration]");
        return sanitized.trim();
    }
}
