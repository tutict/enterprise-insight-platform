package com.tutict.eip.common.security;

public class AccessDeniedException extends RuntimeException {
    private final String code;

    public AccessDeniedException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
