package com.base.utility.exception.type;

import com.base.utility.exception.utils.ErrorCode;

import java.util.Map;

public class ExternalServiceException extends BusinessException {
    public ExternalServiceException() {
        super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, "External service error", Map.of());
    }

    public ExternalServiceException(String message) {
        super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, message, Map.of());
    }

    public ExternalServiceException(String message, Map<String, Object> details) {
        super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE, message, details);
    }

    public ExternalServiceException(String serviceName, String operation) {
        super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                String.format("External service '%s' failed during %s operation", serviceName, operation),
                Map.of("serviceName", serviceName, "operation", operation));
    }

    public ExternalServiceException(String serviceName, String operation, Throwable cause) {
        super(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                String.format("External service '%s' failed during %s operation", serviceName, operation),
                Map.of("serviceName", serviceName, "operation", operation, "cause", cause.getMessage()));
    }

    public ExternalServiceException(ErrorCode errorCode, String message) {
        super(errorCode, message, Map.of());
    }

    public ExternalServiceException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
