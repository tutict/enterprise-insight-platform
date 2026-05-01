package com.tutict.eip.agentadapter.exception;

public class AgentAdapterException extends RuntimeException {

    public AgentAdapterException(String message) {
        super(message);
    }

    public AgentAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
