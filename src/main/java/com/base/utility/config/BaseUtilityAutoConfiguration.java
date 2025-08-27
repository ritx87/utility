package com.base.utility.config;

import com.base.utility.audit.ApiAuditingAspect;
import com.base.utility.audit.AuditEventPublisher;
import com.base.utility.database.AuditingConfig;
import com.base.utility.database.DatabaseExceptionTranslator;
import com.base.utility.exception.utils.GlobalExceptionHandler;
import com.base.utility.logging.CorrelationIdFilter;
import com.base.utility.logging.LoggingAspect;
import com.base.utility.monitoring.CustomMetrics;
import com.base.utility.monitoring.MetricsConfig;
import com.base.utility.properties.*;
import com.base.utility.security.CurrentUserUtil;
import com.base.utility.security.JwtTokenHelper;
import com.base.utility.security.RoleCheckUtil;
import com.base.utility.utils.DateTimeUtils;
import com.base.utility.webclient.AsyncWebClientService;
import com.base.utility.webclient.WebClientHelper;
import com.base.utility.webclient.WebClientResponseHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Simple Auto-configuration class for Base Utility Starter
 * <p>
 * This class imports all necessary configurations and enables component scanning
 * for automatic bean discovery and registration.
 *
 * @author Development Team
 * @version 1.0.0
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties({
        BaseUtilityProperties.class,
        LoggingProperties.class,
        AuditProperties.class,
        WebClientProperties.class,
        MonitoringProperties.class})
@ConditionalOnProperty(
        prefix = "base-utility",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@AutoConfigureAfter({
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@ComponentScan(basePackages = "com.base.utility")
@Import({
        // Core Configurations
        LoggingConfig.class,
        DatabaseConfig.class,
        WebClientConfig.class,
        SecurityConfig.class,

        // Optional Configurations
        OpenApiConfig.class,
        ResilienceConfig.class,
        MetricsConfig.class,
        JacksonConfig.class,
        AsyncConfig.class
})
public class BaseUtilityAutoConfiguration {
    private final BaseUtilityProperties baseUtilityProperties;

    private final BaseUtilityProperties properties;

    @PostConstruct
    public void logStartup() {
        log.info("🚀 Base Utility Auto-Configuration started successfully");
        log.debug("Base Utility Properties: {}", properties);
        log.info("📦 Available features: Logging, Database, WebClient, Security, Monitoring, API Documentation");
    }

}
