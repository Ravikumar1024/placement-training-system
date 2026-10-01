package com.placement.dto;

import lombok.Getter;
import com.placement.util.ApiMessages;

@Getter
public class Result<T> {
    private final String code;
    private final String message;
    private final T data;

    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(String messageKey, T data) {
        String code = ApiMessages.get(messageKey + ".code");
        String message = ApiMessages.get(messageKey + ".message");
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> success(T data) {
        return success("api.success.read.generic", data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int httpStatus, String detail) {
        String code = ApiMessages.get("api.error.status." + httpStatus + ".code");
        String message = detail == null || detail.isBlank()
            ? ApiMessages.get("api.error." + code + ".message")
            : detail;
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> notFound(String detail) {
        return error(404, detail);
    }

    public static <T> Result<T> badRequest(String detail) {
        return error(400, detail);
    }

    public static <T> Result<T> unauthorized(String detail) {
        return error(401, detail);
    }

    public static <T> Result<T> forbidden(String detail) {
        return error(403, detail);
    }

    public static <T> Result<T> internalError(String detail) {
        return error(500, detail);
    }

}
