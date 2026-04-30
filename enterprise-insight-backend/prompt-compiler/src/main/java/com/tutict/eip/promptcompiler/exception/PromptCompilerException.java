package com.tutict.eip.promptcompiler.exception;

public class PromptCompilerException extends RuntimeException {

    public PromptCompilerException(String message) {
        super(message);
    }

    public PromptCompilerException(String message, Throwable cause) {
        super(message, cause);
    }
}
