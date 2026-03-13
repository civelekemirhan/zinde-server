package com.wexec.zinde_server.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }

    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder()
                .success(true)
                .data(null)
                .error(null)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .error(ApiError.builder()
                        .code(code)
                        .message(message)
                        .build())
                .build();
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .error(error)
                .build();
    }
}
