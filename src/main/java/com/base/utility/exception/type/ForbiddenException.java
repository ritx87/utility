package com.base.utility.exception.type;

import com.base.utility.exception.utils.ErrorCode;

import java.util.Map;

public class ForbiddenException extends BusinessException{
    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN, "Access forbidden", Map.of());
    }

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message, Map.of());
    }

    public ForbiddenException(String message, Map<String, Object> details) {
        super(ErrorCode.FORBIDDEN, message, details);
    }

    public ForbiddenException(String resource, String action) {
        super(ErrorCode.FORBIDDEN,
                String.format("Access forbidden for %s operation on %s", action, resource),
                Map.of("resource", resource, "action", action));
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message, Map.of());
    }

    public ForbiddenException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
