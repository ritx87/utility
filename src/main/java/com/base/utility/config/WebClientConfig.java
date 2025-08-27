package com.base.utility.config;

import com.base.utility.properties.BaseUtilityProperties;
import com.base.utility.properties.WebClientProperties;
import com.base.utility.webclient.AsyncWebClientService;
import com.base.utility.webclient.WebClientHelper;
import com.base.utility.webclient.WebClientResponseHandler;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Map;

@Configuration
@ConditionalOnClass(WebClient.class)
@RequiredArgsConstructor
public class WebClientConfig {
    private final BaseUtilityProperties properties;

    @Bean
    public WebClient webClient(WebClientProperties webClientProperties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) webClientProperties.connectTimeout().toMillis())
                .responseTimeout(webClientProperties.responseTimeout())
                .compress(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(mdcExchangeFilter())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(webClientProperties.maxInMemorySize()))
                .build();
    }

    @Bean
    public WebClientResponseHandler webClientResponseHandler() {
        return new WebClientResponseHandler();
    }

    @Bean
    public WebClientHelper webClientHelper(WebClient webClient,
                                           WebClientProperties webClientProperties,
                                           WebClientResponseHandler responseHandler) {
        return new WebClientHelper(webClient, webClientProperties, responseHandler);
    }

    @Bean
    public AsyncWebClientService asyncWebClientService(WebClient webClient) {
        return new AsyncWebClientService(webClient);
    }
    // MDC propagation filter for WebClient
    private ExchangeFilterFunction mdcExchangeFilter() {
        return (request, next) -> {
            // Capture MDC context from current thread
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            return next.exchange(request)
                    .doOnNext(clientResponse -> {
                        // Restore MDC context in reactive thread
                        if (contextMap != null) {
                            MDC.setContextMap(contextMap);
                        }
                    })
                    .doOnError(throwable -> {
                        // Restore context even on error
                        if (contextMap != null) {
                            MDC.setContextMap(contextMap);
                        }
                    });
        };
    }
}
