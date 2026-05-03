package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.CompileResponse;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CompileService {

    private static final Logger log = LoggerFactory.getLogger(CompileService.class);

    private final DslParser dslParser;
    private final PromptCompiler promptCompiler;

    public CompileService(DslParser dslParser, PromptCompiler promptCompiler) {
        this.dslParser = dslParser;
        this.promptCompiler = promptCompiler;
    }

    public CompileResponse compile(String requirement) {
        log.info("Compile request received requirementChars={}", requirement == null ? 0 : requirement.length());
        DslModel dslModel = dslParser.parse(requirement);
        String prompt = promptCompiler.compile(dslModel);
        return new CompileResponse(dslModel, prompt);
    }
}
