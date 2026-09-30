package com.placement.dto;

import lombok.*;

import java.util.Map;

/**
 * Generic Result class for API responses.
 * Contains code, message, and data fields for standardized responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private Map<String, String> errors;

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
            .code(200)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(200)
            .message("Success")
            .data(data)
            .build();
    }

    public static <T> Result<T> success() {
        return Result.<T>builder()
            .code(200)
            .message("Success")
            .data(null)
            .build();
    }

    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
            .code(code)
            .message(message)
            .data(null)
            .build();
    }

    public static <T> Result<T> notFound(String message) {
        return Result.<T>builder()
            .code(404)
            .message(message)
            .data(null)
            .build();
    }

    public static <T> Result<T> badRequest(String message) {
        return Result.<T>builder()
            .code(400)
            .message(message)
            .data(null)
            .build();
    }

    public static <T> Result<T> badRequest(String message, Map<String, String> errors) {
        return Result.<T>builder()
            .code(400)
            .message(message)
            .data(null)
            .errors(errors)
            .build();
    }

    public static <T> Result<T> unauthorized(String message) {
        return Result.<T>builder()
            .code(401)
            .message(message)
            .data(null)
            .build();
    }

    public static <T> Result<T> forbidden(String message) {
        return Result.<T>builder()
            .code(403)
            .message(message)
            .data(null)
            .build();
    }

    public static <T> Result<T> internalError(String message) {
        return Result.<T>builder()
            .code(500)
            .message(message)
            .data(null)
            .build();
    }
}