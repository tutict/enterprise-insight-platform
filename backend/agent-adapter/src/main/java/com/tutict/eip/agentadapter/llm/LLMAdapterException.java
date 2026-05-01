package com.tutict.eip.agentadapter.llm;

public class LLMAdapterException extends RuntimeException {

    public LLMAdapterException(String message) {
        super(message);
    }

    public LLMAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
