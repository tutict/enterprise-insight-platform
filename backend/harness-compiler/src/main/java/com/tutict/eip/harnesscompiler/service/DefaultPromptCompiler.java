package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultPromptCompiler implements PromptCompiler {

    private static final Logger log = LoggerFactory.getLogger(DefaultPromptCompiler.class);

    private final DslToPromptCompiler dslToPromptCompiler;

    public DefaultPromptCompiler() {
        this(new DslToPromptCompiler(new PromptTemplateEngine()));
    }

    @Autowired
    public DefaultPromptCompiler(DslToPromptCompiler dslToPromptCompiler) {
        this.dslToPromptCompiler = dslToPromptCompiler;
    }

    @Override
    public String compile(DslModel dslModel) {
        if (dslModel == null) {
            throw new IllegalArgumentException("dslModel must not be null");
        }

        String result = dslToPromptCompiler.compile(dslModel);
        log.info("Compiled DSL into harness prompt promptChars={} modules={}", result.length(), dslModel.getModules().size());
        return result;
    }
}
