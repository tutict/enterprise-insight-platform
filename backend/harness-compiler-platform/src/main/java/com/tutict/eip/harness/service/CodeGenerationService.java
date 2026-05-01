package com.tutict.eip.harness.service;

import com.tutict.eip.harness.agent.llm.AdapterFactory;
import com.tutict.eip.harness.agent.llm.LLMAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CodeGenerationService {

    private static final Logger log = LoggerFactory.getLogger(CodeGenerationService.class);

    private final AdapterFactory adapterFactory;

    public CodeGenerationService(AdapterFactory adapterFactory) {
        this.adapterFactory = adapterFactory;
    }

    public String generateCode(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt must not be blank");
        }
        log.info("Generate request received promptChars={}", prompt.length());
        LLMAdapter adapter = adapterFactory.create();
        String code = adapter.generate(prompt);
        log.info("Code generation completed codeChars={}", code == null ? 0 : code.length());
        return code;
    }
}
