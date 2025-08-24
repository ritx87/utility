package com.base.utility.exception.type;

import com.base.utility.exception.utils.ErrorCode;

import java.util.Map;

public class ConflictException extends BusinessException{
    public ConflictException() {
        super(ErrorCode.RESOURCE_CONFLICT, "Resource conflict", Map.of());
    }

    public ConflictException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, message, Map.of());
    }

    public ConflictException(String message, Map<String, Object> details) {
        super(ErrorCode.RESOURCE_CONFLICT, message, details);
    }

    public ConflictException(String resource, String identifier) {
        super(ErrorCode.RESOURCE_CONFLICT,
                String.format("%s with identifier '%s' already exists", resource, identifier),
                Map.of("resource", resource, "identifier", identifier));
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message, Map.of());
    }

    public ConflictException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
