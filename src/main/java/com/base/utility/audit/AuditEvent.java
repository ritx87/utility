package com.base.utility.audit;

import com.base.utility.utils.JsonUtils;
import com.base.utility.utils.MaskingUtils;
import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;
@Builder
@With
public record AuditEvent(
        String correlationId,
        LocalDateTime timestamp,
        String method,
        String uri,
        String userAgent,
        String remoteAddr,
        String requestBody,
        String responseBody,
        String headers,
        String methodName,
        String className,
        long executionTimeMs,
        boolean success,
        String errorMessage
) {
    public AuditEvent withResponse(Object response, long executionTime, boolean success) {
        String responseBody = null;
        if (response != null) {
            responseBody = JsonUtils.toJson(response);
            responseBody = MaskingUtils.maskSensitiveData(responseBody, java.util.Set.of("password", "token"));
        }

        return new AuditEvent(
                correlationId, timestamp, method, uri, userAgent, remoteAddr,
                requestBody, responseBody, headers, methodName, className,
                executionTime, success, null
        );
    }

    public AuditEvent withError(Exception error, long executionTime) {
        return new AuditEvent(
                correlationId, timestamp, method, uri, userAgent, remoteAddr,
                requestBody, null, headers, methodName, className,
                executionTime, false, error.getMessage()
        );
    }
}
