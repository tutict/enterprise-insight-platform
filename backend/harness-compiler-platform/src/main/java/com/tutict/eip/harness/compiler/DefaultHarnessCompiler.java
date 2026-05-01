package com.tutict.eip.harness.compiler;

import com.tutict.eip.harness.domain.CompiledHarnessPrompt;
import com.tutict.eip.harness.domain.DslDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.StringJoiner;

@Component
public class DefaultHarnessCompiler implements HarnessCompiler {

    @Override
    public CompiledHarnessPrompt compile(DslDocument dslDocument) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("# ROLE\n");
        prompt.append("You are an AI coding agent.\n\n");
        prompt.append("# PROJECT\n");
        prompt.append("- type: ").append(dslDocument.getProjectType()).append('\n');
        prompt.append("- modules: ").append(joinModules(dslDocument)).append("\n\n");
        prompt.append("# CONSTRAINTS\n");
        prompt.append(dslDocument.getConstraints()).append("\n\n");
        prompt.append("# TASK\n");
        prompt.append("Generate production-ready code according to the DSL.");
        return new CompiledHarnessPrompt(prompt.toString(), Instant.now());
    }

    private String joinModules(DslDocument dslDocument) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String module : dslDocument.getModules()) {
            joiner.add(module);
        }
        return joiner.toString();
    }
}
