package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.domain.PromptCompileRequest;
import com.tutict.eip.promptcompiler.domain.PromptCompileResponse;

public interface PromptCompilerService {

    PromptCompileResponse compile(PromptCompileRequest request);
}
