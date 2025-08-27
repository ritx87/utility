package com.base.utility.properties;

import lombok.Builder;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "base-utility.monitoring")
public record MonitoringProperties(
        @DefaultValue("true")
        boolean enabled,

        @DefaultValue("true")
        boolean metricsEnabled,

        @DefaultValue("true")
        boolean prometheusEnabled,

        @DefaultValue("base-utility-service")
        String applicationName,

        @DefaultValue("1.0.0")
        String applicationVersion
) {
}
