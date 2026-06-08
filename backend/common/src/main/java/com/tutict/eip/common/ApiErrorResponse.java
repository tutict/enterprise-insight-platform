package com.tutict.eip.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;

public final class ApiErrorResponse {

    private ApiErrorResponse() {
    }

    public static ResponseEntity<ApiResponse<ProblemDetail>> from(ApiException ex) {
        return body(ex.getStatusCode(), ex.getCode(), ex.getBody());
    }

    public static ResponseEntity<ApiResponse<ProblemDetail>> from(ErrorResponse ex, String code) {
        return body(ex.getStatusCode(), code, withCode(ex.getBody(), code));
    }

    public static ResponseEntity<ApiResponse<ProblemDetail>> badRequest(String message) {
        return from(ApiException.badRequest(message));
    }

    public static ResponseEntity<ApiResponse<ProblemDetail>> forbidden(String message, String code) {
        return body(HttpStatus.FORBIDDEN, code, problem(HttpStatus.FORBIDDEN, code, message));
    }

    public static ResponseEntity<ApiResponse<ProblemDetail>> internalError() {
        return from(ApiException.internal("Internal server error"));
    }

    private static ResponseEntity<ApiResponse<ProblemDetail>> body(
            HttpStatusCode status,
            String code,
            ProblemDetail problem
    ) {
        String message = problem.getDetail() == null || problem.getDetail().isBlank()
                ? problem.getTitle()
                : problem.getDetail();
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(message, code, problem));
    }

    private static ProblemDetail withCode(ProblemDetail problem, String code) {
        problem.setProperty("code", code);
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String code, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        return problem;
    }
}
