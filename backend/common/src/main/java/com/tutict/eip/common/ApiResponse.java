package com.tutict.eip.common;

import java.time.Instant;

public class ApiResponse<T> {
    private final String code;
    private final boolean success;
    private final String message;
    private final T data;
    private final Instant timestamp;

    private ApiResponse(String code, boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCodes.OK, true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(ErrorCodes.OK, true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ErrorCodes.INTERNAL_ERROR, false, message, null);
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(code, false, message, null);
    }

    public String getCode() {
        return code;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
