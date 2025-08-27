package com.base.utility.properties;

import lombok.Builder;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;


@ConfigurationProperties(prefix = "base-utility.async")
public record AsyncProperties(
        boolean enabled,
        int coreSize,
        int maxSize,
        int queueCapacity,
        String threadNamePrefix,
        int keepAliveSeconds
) {
    public AsyncProperties() {
        this(true, 5, 10, 100, "todo-async-", 60);
    }
}
