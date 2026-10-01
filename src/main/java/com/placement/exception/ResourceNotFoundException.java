package com.placement.exception;

import com.placement.util.ApiMessages;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String messageKey, Object... arguments) {
        super(ApiMessages.get(messageKey, arguments));
    }
}
