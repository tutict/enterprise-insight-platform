package com.tutict.eip.agentadapter.exception;

public class OllamaClientException extends AgentAdapterException {

    private final boolean retryable;

    public OllamaClientException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public OllamaClientException(String message, Throwable cause, boolean retryable) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
