package com.tutict.eip.harness.service;

import com.tutict.eip.harness.domain.DSLModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RuleBasedDSLParser implements DSLParser {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedDSLParser.class);

    @Override
    public DSLModel parse(String requirement) {
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }

        String normalized = requirement.toLowerCase(Locale.ROOT);
        List<String> modules = new ArrayList<>();
        modules.add("api");
        modules.add("service");
        modules.add("domain");

        if (containsAny(normalized, "登录", "login", "auth", "jwt")) {
            modules.add("authentication");
        }
        if (containsAny(normalized, "数据库", "db", "mysql", "postgres", "持久化")) {
            modules.add("persistence");
        }
        if (containsAny(normalized, "前端", "页面", "ui", "react")) {
            modules.add("frontend-contract");
        }

        Map<String, String> constraints = new LinkedHashMap<>();
        constraints.put("language", "Java 17+");
        constraints.put("framework", "Spring Boot 3");
        constraints.put("quality", "production-ready, readable, testable");
        constraints.put("errorHandling", "throw explicit exceptions and return clear API errors");

        DSLModel model = new DSLModel(
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
