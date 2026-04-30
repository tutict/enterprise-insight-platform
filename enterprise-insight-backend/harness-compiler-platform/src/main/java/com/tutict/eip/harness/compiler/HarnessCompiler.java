package com.tutict.eip.harness.compiler;

import com.tutict.eip.harness.domain.CompiledHarnessPrompt;
import com.tutict.eip.harness.domain.DslDocument;

public interface HarnessCompiler {

    CompiledHarnessPrompt compile(DslDocument dslDocument);
}
