package com.tutict.eip.harness.compiler;

import com.tutict.eip.harness.domain.DSLModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.StringJoiner;

@Service
public class DefaultPromptCompiler implements PromptCompiler {

    private static final Logger log = LoggerFactory.getLogger(DefaultPromptCompiler.class);

    @Override
    public String compile(DSLModel dslModel) {
        if (dslModel == null) {
            throw new IllegalArgumentException("dslModel must not be null");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("ROLE\n");
        prompt.append("You are a senior Java architect and AI coding agent.\n\n");
        prompt.append("GOAL\n");
        prompt.append("Build code for this requirement: ").append(dslModel.getRequirement()).append("\n\n");
        prompt.append("MODULES\n");
        prompt.append(joinModules(dslModel)).append("\n\n");
        prompt.append("CONSTRAINTS\n");
        for (Map.Entry<String, String> entry : dslModel.getConstraints().entrySet()) {
            prompt.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        prompt.append("\nOUTPUT FORMAT\n");
        prompt.append(dslModel.getOutputFormat()).append('\n');

        String result = prompt.toString();
        log.info("Compiled DSL into harness prompt promptChars={} modules={}", result.length(), dslModel.getModules().size());
        return result;
    }

    private String joinModules(DSLModel dslModel) {
        StringJoiner joiner = new StringJoiner("\n");
        for (String module : dslModel.getModules()) {
            joiner.add("- " + module);
        }
        return joiner.toString();
    }
}
