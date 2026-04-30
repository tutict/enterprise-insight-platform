package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.domain.CompiledPrompt;
import com.tutict.eip.promptcompiler.domain.DslDocument;
import com.tutict.eip.promptcompiler.domain.PromptCompileRequest;
import com.tutict.eip.promptcompiler.domain.PromptCompileResponse;
import com.tutict.eip.promptcompiler.domain.TemplateContext;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DefaultPromptCompilerService implements PromptCompilerService {

    private final DslParser dslParser;
    private final PromptTemplateRegistry promptTemplateRegistry;

    public DefaultPromptCompilerService(DslParser dslParser, PromptTemplateRegistry promptTemplateRegistry) {
        this.dslParser = dslParser;
        this.promptTemplateRegistry = promptTemplateRegistry;
    }

    @Override
    public PromptCompileResponse compile(PromptCompileRequest request) {
        DslDocument dslDocument = dslParser.parse(request.getDsl());
        PromptTemplate template = promptTemplateRegistry.resolve(request.getTemplateName());
        CompiledPrompt compiledPrompt = template.render(buildContext(dslDocument));
        return new PromptCompileResponse(dslDocument, compiledPrompt);
    }

    private TemplateContext buildContext(DslDocument dslDocument) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("projectType", dslDocument.getProject().getType());
        variables.put("modules", dslDocument.getProject().getModules());
        if (dslDocument.getConstraints() != null) {
            variables.put("db", dslDocument.getConstraints().getDb());
            variables.put("constraints", dslDocument.getConstraints().getExtensions());
        }
        return new TemplateContext(dslDocument, variables);
    }
}
