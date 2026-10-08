package com.edabip.billing.exceptionHandler;

public record ApiResponse<T>(boolean success, T data, ApiError error) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> failure(String code, String message, Object details) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details));
    }

    public record ApiError(String code, String message, Object details) {}
}
