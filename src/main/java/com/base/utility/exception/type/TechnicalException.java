package com.base.utility.exception.type;

import com.base.utility.exception.utils.ErrorCode;

import java.util.Map;

public class TechnicalException extends BusinessException{
    public TechnicalException() {
        super(ErrorCode.TECHNICAL_ERROR, "Technical error occurred", Map.of());
    }

    public TechnicalException(String message) {
        super(ErrorCode.TECHNICAL_ERROR, message, Map.of());
    }

    public TechnicalException(String message, Map<String, Object> details) {
        super(ErrorCode.TECHNICAL_ERROR, message, details);
    }

    public TechnicalException(String operation, Throwable cause) {
        super(ErrorCode.TECHNICAL_ERROR,
                String.format("Technical error occurred during %s", operation),
                Map.of("operation", operation, "cause", cause.getMessage()));
    }

    public TechnicalException(ErrorCode errorCode, String message) {
        super(errorCode, message, Map.of());
    }

    public TechnicalException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
