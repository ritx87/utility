package com.base.utility.properties;

import lombok.Builder;
import lombok.With;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
@ConfigurationProperties(prefix = "base-utility.webclient")
public record WebClientProperties(
        Duration connectTimeout,
        Duration responseTimeout,
        Duration readTimeout,
        int maxRetries,
        Duration retryDelay,
        int maxInMemorySize
) {
    public WebClientProperties() {
        this(Duration.ofSeconds(5), Duration.ofSeconds(30), Duration.ofSeconds(30),
                3, Duration.ofMillis(1000), 1024 * 1024);
    }
}
