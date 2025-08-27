package com.base.utility.audit;

import com.base.utility.logging.StructuredLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditEventPublisher {
    private static final StructuredLogger logger = StructuredLogger.getLogger(AuditEventPublisher.class);

    public void publish(AuditEvent auditEvent) {
        logger.info("API audit event")
                .field("correlationId", auditEvent.correlationId())
                .field("method", auditEvent.method())
                .field("uri", auditEvent.uri())
                .field("executionTimeMs", auditEvent.executionTimeMs())
                .field("success", auditEvent.success())
                .field("remoteAddr", auditEvent.remoteAddr())
                .field("userAgent", auditEvent.userAgent())
                .field("requestBody", auditEvent.requestBody())
                .field("responseBody", auditEvent.responseBody())
                .field("errorMessage", auditEvent.errorMessage())
                .log();
    }
}
