package com.base.utility.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "base-utility.logging")
public record LoggingProperties(
        boolean enabled,
        String level,
        boolean jsonFormat,
        boolean correlationId,
        String correlationIdHeader
) {
    public LoggingProperties() {
        this(true, "INFO", true, true, "X-Correlation-ID");
    }
}
