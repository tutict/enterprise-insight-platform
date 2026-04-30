package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.exception.PromptCompilerException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SpringPromptTemplateRegistry implements PromptTemplateRegistry {

    private final Map<String, PromptTemplate> templates;

    public SpringPromptTemplateRegistry(List<PromptTemplate> templates) {
        this.templates = new LinkedHashMap<>();
        for (PromptTemplate template : templates) {
            this.templates.put(template.name(), template);
        }
    }

    @Override
    public PromptTemplate resolve(String templateName) {
        String resolvedName = templateName == null || templateName.isBlank()
                ? HarnessPromptTemplate.TEMPLATE_NAME
                : templateName;
        PromptTemplate template = templates.get(resolvedName);
        if (template == null) {
            throw new PromptCompilerException("Prompt template not found: " + resolvedName);
        }
        return template;
    }
}
