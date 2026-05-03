package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RuleBasedDslParser implements DslParser {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedDslParser.class);

    @Override
    public DslModel parse(String requirement) {
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }

        String normalized = requirement.toLowerCase(Locale.ROOT);
        List<String> modules = new ArrayList<>();
        modules.add("api");
        modules.add("service");
        modules.add("domain");

        if (containsAny(normalized, "login", "auth", "jwt", "user")) {
            modules.add("authentication");
        }
        if (containsAny(normalized, "database", "db", "mysql", "postgres", "persistence")) {
            modules.add("persistence");
        }
        if (containsAny(normalized, "frontend", "page", "ui", "react")) {
            modules.add("frontend-contract");
        }

        Map<String, String> constraints = new LinkedHashMap<>();
        constraints.put("language", "Java 17+");
        constraints.put("framework", "Spring Boot 3");
        constraints.put("quality", "production-ready, readable, testable");
        constraints.put("errorHandling", "throw explicit exceptions and return clear API errors");

        DslModel model = new DslModel(
                "ai-harness-generated-system",
                "spring-boot-backend",
                requirement,
                modules,
                constraints,
                "Return complete source files with paths and code blocks"
        );
        log.info("Parsed requirement into DSL modules={} requirementChars={}", modules, requirement.length());
        return model;
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
