package com.base.utility.exception.type;

import com.base.utility.exception.utils.ErrorCode;

import java.util.Map;

public class UnauthorizedException extends BusinessException{
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED, "Unauthorized access", Map.of());
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message, Map.of());
    }

    public UnauthorizedException(String message, Map<String, Object> details) {
        super(ErrorCode.UNAUTHORIZED, message, details);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message, Map.of());
    }

    public UnauthorizedException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }

}
