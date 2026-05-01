package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.domain.CompiledPrompt;
import com.tutict.eip.promptcompiler.domain.TemplateContext;

public interface PromptTemplate {

    String name();

    CompiledPrompt render(TemplateContext context);
}
