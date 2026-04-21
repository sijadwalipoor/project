package com.euro.db2performance.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private Meta meta;
    private ApiError error;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().data(data).error(null).build();
    }

    public static <T> ApiResponse<T> ok(T data, Meta meta) {
        return ApiResponse.<T>builder().data(data).meta(meta).error(null).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .data(null)
                .error(ApiError.builder().code(code).message(message).build())
                .build();
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return error("UNAUTHORIZED", message);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return error("BAD_REQUEST", message);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return error("NOT_FOUND", message);
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return error("INTERNAL_ERROR", message);
    }
}
