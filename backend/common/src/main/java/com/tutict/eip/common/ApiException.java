package com.tutict.eip.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;

public class ApiException extends ErrorResponseException {

    private final String code;

    public ApiException(HttpStatusCode status, String code, String message) {
        this(status, code, message, null);
    }

    public ApiException(HttpStatusCode status, String code, String message, Throwable cause) {
        super(status, problem(status, code, message), cause);
        this.code = code;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.BAD_REQUEST, message);
    }

    public static ApiException unauthorized(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_REQUIRED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, message);
    }

    public static ApiException internal(String message) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR, message);
    }

    public static ApiException internal(String message, Throwable cause) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR, message, cause);
    }

    public String getCode() {
        return code;
    }

    private static ProblemDetail problem(HttpStatusCode status, String code, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title(status));
        detail.setType(URI.create("urn:eip:error:" + code));
        detail.setProperty("code", code);
        return detail;
    }

    private static String title(HttpStatusCode status) {
        if (status instanceof HttpStatus httpStatus) {
            return httpStatus.getReasonPhrase();
        }
        return "HTTP " + status.value();
    }
}
