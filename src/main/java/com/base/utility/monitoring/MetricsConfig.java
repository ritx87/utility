package com.base.utility.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(MeterRegistry.class)
@RequiredArgsConstructor
public class MetricsConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config()
                    .commonTags("application", "base-utility-service")
                    .commonTags("version", "1.0.0")
                    .meterFilter(MeterFilter.deny(id -> {
                        String uri = id.getTag("uri");
                        return uri != null && (
                                uri.startsWith("/actuator") ||
                                        uri.startsWith("/swagger") ||
                                        uri.startsWith("/v3/api-docs")
                        );
                    }));
        };
    }

    @Bean
    public CustomMetrics customMetrics(MeterRegistry meterRegistry) {
        return new CustomMetrics(meterRegistry);
    }
}
