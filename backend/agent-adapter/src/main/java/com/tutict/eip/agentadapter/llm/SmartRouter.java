package com.tutict.eip.agentadapter.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SmartRouter implements LLMAdapter {

    private static final Logger log = LoggerFactory.getLogger(SmartRouter.class);
    private static final int OPENAI_THRESHOLD = 500;

    private final LLMAdapter localAdapter;
    private final LLMAdapter remoteAdapter;

    public SmartRouter(LLMAdapter localAdapter, LLMAdapter remoteAdapter) {
        this.localAdapter = Objects.requireNonNull(localAdapter, "localAdapter must not be null");
        this.remoteAdapter = Objects.requireNonNull(remoteAdapter, "remoteAdapter must not be null");
    }

    @Override
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new LLMAdapterException("Prompt must not be blank");
        }
        int promptLength = prompt.length();
        if (promptLength < OPENAI_THRESHOLD) {
            log.info("SmartRouter selected local Ollama promptLength={} threshold={}", promptLength, OPENAI_THRESHOLD);
            return localAdapter.generate(prompt);
        }
        log.info("SmartRouter selected remote OpenAI promptLength={} threshold={}", promptLength, OPENAI_THRESHOLD);
        return remoteAdapter.generate(prompt);
    }
}
