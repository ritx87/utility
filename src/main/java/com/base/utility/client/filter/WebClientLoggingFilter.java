package com.base.utility.client.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

public class WebClientLoggingFilter implements ExchangeFilterFunction {
    private static final Logger logger = LoggerFactory.getLogger(WebClientLoggingFilter.class);

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        long startTime = System.currentTimeMillis();

        logger.info("WebClient Request: {} {}", request.method(), request.url());

        return next.exchange(request)
                .doOnNext(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.info("WebClient Response: {} {} - Status: {} - Duration: {}ms",
                            request.method(), request.url(), response.statusCode(), duration);
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.error("WebClient Error: {} {} - Duration: {}ms - Error: {}",
                            request.method(), request.url(), duration, error.getMessage());
                });
    }
}
