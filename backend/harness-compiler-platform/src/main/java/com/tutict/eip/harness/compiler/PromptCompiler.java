package com.tutict.eip.harness.compiler;

import com.tutict.eip.harness.domain.DSLModel;

public interface PromptCompiler {

    String compile(DSLModel dslModel);
}
