package com.tutict.eip.harnesscompiler.controller;

import com.tutict.eip.common.ApiErrorResponse;
import com.tutict.eip.common.ApiException;
import com.tutict.eip.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request");
        log.warn("Validation failed message={}", message);
        return ApiErrorResponse.badRequest(message);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleApiException(ApiException ex) {
        log.warn("API request failed code={} status={} message={}", ex.getCode(), ex.getStatusCode(), ex.getBody().getDetail());
        return ApiErrorResponse.from(ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleBadRequest(RuntimeException ex) {
        log.warn("Request failed message={}", ex.getMessage(), ex);
        return ApiErrorResponse.badRequest(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleUnexpected(Exception ex) {
        log.error("Unexpected server error message={}", ex.getMessage(), ex);
        return ApiErrorResponse.internalError();
    }
}
