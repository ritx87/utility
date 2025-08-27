package com.base.utility.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "base-utility")
public record BaseUtilityProperties(
        boolean enabled,
        @NestedConfigurationProperty
        LoggingProperties logging,
        @NestedConfigurationProperty
        AuditProperties audit,
        @NestedConfigurationProperty
        WebClientProperties webClient,
        @NestedConfigurationProperty
        AsyncProperties async
) {
    public BaseUtilityProperties() {
        this(true,
                new LoggingProperties(),
                new AuditProperties(),
                new WebClientProperties(),
                new AsyncProperties());
    }
}
