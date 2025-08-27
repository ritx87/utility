package com.base.utility.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;
@ConfigurationProperties(prefix = "base-utility.audit")
public record AuditProperties(
        boolean enabled,
        boolean logRequestBody,
        boolean logResponseBody,
        Set<String> maskedFields,
        int maxBodyLength
) {
    public AuditProperties() {
        this(true, true, true,
                Set.of("password", "token", "authorization", "ssn", "creditCard"),
                1000);
    }
}
