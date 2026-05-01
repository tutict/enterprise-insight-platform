package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.domain.CompiledPrompt;
import com.tutict.eip.promptcompiler.domain.DslDocument;
import com.tutict.eip.promptcompiler.domain.PromptConstraints;
import com.tutict.eip.promptcompiler.domain.ProjectSpec;
import com.tutict.eip.promptcompiler.domain.TemplateContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class HarnessPromptTemplate implements PromptTemplate {

    public static final String TEMPLATE_NAME = "harness-default";

    @Override
    public String name() {
        return TEMPLATE_NAME;
    }

    @Override
    public CompiledPrompt render(TemplateContext context) {
        DslDocument dslDocument = context.getDslDocument();
        ProjectSpec project = dslDocument.getProject();
        PromptConstraints constraints = dslDocument.getConstraints();
        List<String> sections = List.of("ROLE", "GOAL", "MODULES", "CONSTRAINTS", "OUTPUT FORMAT");

        StringBuilder prompt = new StringBuilder();
        prompt.append("# ROLE\n");
        prompt.append("You are an AI Harness coding agent that generates production-ready Java and Spring Boot code.\n\n");
        prompt.append("# GOAL\n");
        prompt.append("Convert the structured DSL into a complete, compilable implementation plan and code files.\n\n");
        prompt.append("# MODULES\n");
        prompt.append("- project_type: ").append(project.getType()).append('\n');
        appendModules(prompt, project.getModules());
        prompt.append('\n');
        prompt.append("# CONSTRAINTS\n");
        appendConstraints(prompt, constraints);
        prompt.append('\n');
        prompt.append("# OUTPUT FORMAT\n");
        prompt.append("Return only generated files using this exact format:\n");
        prompt.append("===FILE START===\n");
        prompt.append("relative/path/from/project/root\n");
        prompt.append("complete file content\n");
        prompt.append("===FILE END===");

        return new CompiledPrompt(name(), prompt.toString(), new ArrayList<>(sections), Instant.EPOCH);
    }

    private void appendModules(StringBuilder prompt, List<String> modules) {
        for (String module : modules) {
            prompt.append("- module: ").append(module).append('\n');
        }
    }

    private void appendConstraints(StringBuilder prompt, PromptConstraints constraints) {
        if (constraints == null || constraints.getDb() == null || constraints.getDb().isBlank()) {
            prompt.append("- db: not specified\n");
        } else {
            prompt.append("- db: ").append(constraints.getDb()).append('\n');
        }
        if (constraints != null && constraints.getExtensions() != null) {
            constraints.getExtensions().entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> prompt.append("- ")
                            .append(entry.getKey())
                            .append(": ")
                            .append(entry.getValue())
                            .append('\n'));
        }
    }
}
