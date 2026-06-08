package com.tutict.eip.common.security;

import com.tutict.eip.common.ApiException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApiException {
    public AccessDeniedException(String message, String code) {
        super(HttpStatus.FORBIDDEN, code, message);
    }
}
