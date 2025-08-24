package com.base.utility.client;

import com.base.utility.client.filter.WebClientErrorHandlingFilter;
import com.base.utility.client.filter.WebClientLoggingFilter;
import com.base.utility.client.filter.WebClientRetryFilter;
import com.base.utility.client.handler.CustomErrorHandler;
import com.base.utility.client.interceptor.LoggingInterceptor;
import com.base.utility.client.interceptor.RetryInterceptor;
import com.base.utility.common.autoconfigure.BaseUtilityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Complete HTTP Client Configuration supporting WebClient, RestTemplate, and RestClient
 */
@RequiredArgsConstructor
public class HttpClientConfig {
    private final BaseUtilityProperties properties;
    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "common.utils.http-client",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient()))
                .exchangeStrategies(exchangeStrategies())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Common-Utils-WebClient/1.0")
                .filter(webClientLoggingFilter())
                .filter(webClientRetryFilter())
                .filter(webClientErrorHandlingFilter())
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(1024 * 1024);
                })
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(loggingInterceptor());
        restTemplate.getInterceptors().add(retryInterceptor());
        restTemplate.setErrorHandler(new CustomErrorHandler());
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(RestClient.class)
    @ConditionalOnProperty(
            prefix = "common.utils.http-client",
            name = "rest-client.enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public RestClient restClient() {
        return RestClient.builder()
                .requestInterceptor(loggingInterceptor())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(properties.getHttpClient().getMaxConnections())
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        return HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getHttpClient().getConnectionTimeout())
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(properties.getHttpClient().getReadTimeout(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(properties.getHttpClient().getReadTimeout(), TimeUnit.MILLISECONDS)))
                .compress(properties.getHttpClient().isCompressionEnabled())
                .followRedirect(true);
    }

    @Bean
    public ExchangeStrategies exchangeStrategies() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return ExchangeStrategies.builder()
                .codecs(clientCodecConfigurer -> {
                    clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                    clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(1024 * 1024);
                })
                .build();
    }

    @Bean
    public WebClientLoggingFilter webClientLoggingFilter() {
        return new WebClientLoggingFilter();
    }

    @Bean
    public WebClientRetryFilter webClientRetryFilter() {
        return new WebClientRetryFilter(properties);
    }

    @Bean
    public WebClientErrorHandlingFilter webClientErrorHandlingFilter() {
        return new WebClientErrorHandlingFilter();
    }

    @Bean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

    @Bean
    public RetryInterceptor retryInterceptor() {
        return new RetryInterceptor(properties);
    }
}
