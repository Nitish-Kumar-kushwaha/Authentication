package com.security.authentication.util;

import com.security.authentication.dto.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

public final class ResponseFactory {
    private ResponseFactory() {}

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message, String path) {
        return build(true, HttpStatus.OK, message, data, null, path);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message, String path) {
        return build(true, HttpStatus.CREATED, message, data, null, path);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message, List<String> errors, String path) {
        return build(false, status, message, null, errors, path);
    }

    private static <T> ResponseEntity<ApiResponse<T>> build(boolean success,
                                                            HttpStatus status,
                                                            String message,
                                                            T data,
                                                            List<String> errors,
                                                            String path) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .success(success)
                .message(message)
                .data(data)
                .errors(errors)
                .status(status.value())
                .path(path)
                .timestamp(Instant.now().toString())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}

