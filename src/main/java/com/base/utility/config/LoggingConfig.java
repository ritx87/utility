package com.base.utility.config;

import com.base.utility.logging.CorrelationIdFilter;
import com.base.utility.logging.LoggingAspect;
import com.base.utility.properties.BaseUtilityProperties;
import com.base.utility.properties.LoggingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@ConditionalOnProperty(prefix = "base-utility.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class LoggingConfig {
    private final BaseUtilityProperties properties;

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect(properties.logging());
    }
}
