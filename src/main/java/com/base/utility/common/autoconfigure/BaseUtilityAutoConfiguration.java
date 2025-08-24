package com.base.utility.common.autoconfigure;

import com.base.utility.aspect.ApiAuditingAspect;
import com.base.utility.aspect.LoggingAspect;
import com.base.utility.client.HttpClientConfig;
import com.base.utility.client.HttpClientService;
import com.base.utility.client.HttpHeadersBuilder;
import com.base.utility.exception.utils.GlobalExceptionHandler;
import org.apache.catalina.security.SecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties({BaseUtilityProperties.class})
@Import({HttpClientConfig.class, SecurityConfig.class})
public class BaseUtilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    @ConditionalOnProperty(
            prefix = "common.utils.exception",
            name = "global-handler.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(LoggingAspect.class)
    @ConditionalOnProperty(
            prefix = "common.utils.aspect",
            name = "logging.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
    @ConditionalOnMissingBean(ApiAuditingAspect.class)
    @ConditionalOnProperty(
            prefix = "common.utils.aspect",
            name = "auditing.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public ApiAuditingAspect apiAuditingAspect() {
        return new ApiAuditingAspect();
    }

    @Bean
    @ConditionalOnMissingBean(HttpClientService.class)
    @ConditionalOnProperty(
            prefix = "common.utils.http-client",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public HttpClientService httpClientService() {
        return new HttpClientService();
    }

    @Bean
    @ConditionalOnMissingBean(HttpHeadersBuilder.class)
    public HttpHeadersBuilder httpHeadersBuilder() {
        return new HttpHeadersBuilder();
    }
}
