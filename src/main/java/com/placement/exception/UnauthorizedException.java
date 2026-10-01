package com.placement.exception;

import com.placement.util.ApiMessages;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when authentication fails.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String messageKey, Object... arguments) {
        super(ApiMessages.get(messageKey, arguments));
    }
}