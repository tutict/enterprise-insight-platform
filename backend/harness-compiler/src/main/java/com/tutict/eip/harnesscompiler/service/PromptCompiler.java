package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.DslModel;

public interface PromptCompiler {

    String compile(DslModel dslModel);
}
