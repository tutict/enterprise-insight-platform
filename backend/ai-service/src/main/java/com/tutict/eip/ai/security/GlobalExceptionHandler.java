package com.tutict.eip.ai.security;

import com.tutict.eip.common.ApiErrorResponse;
import com.tutict.eip.common.ApiException;
import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.security.AccessDeniedException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleAccessDenied(AccessDeniedException ex) {
        return ApiErrorResponse.from(ex);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleApiException(ApiException ex) {
        return ApiErrorResponse.from(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleGeneric(Exception ex) {
        return ApiErrorResponse.internalError();
    }
}
