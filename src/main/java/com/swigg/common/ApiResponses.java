package com.swigg.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message) {
        return ResponseEntity.ok(success(message, null, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(success(message, data, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data, int count) {
        return ResponseEntity.ok(success(message, data, count));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success(message, data, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                ApiResponse.<T>builder()
                        .success(false)
                        .message(message)
                        .build()
        );
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalError() {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private static <T> ApiResponse<T> success(String message, T data, Integer count) {
        ApiResponse.ApiResponseBuilder<T> builder = ApiResponse.<T>builder()
                .success(true)
                .message(message);
        if (data != null) {
            builder.data(data);
        }
        if (count != null) {
            builder.count(count);
        }
        return builder.build();
    }
}
