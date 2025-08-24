package com.base.utility.client.filter;

import com.base.utility.common.autoconfigure.BaseUtilityProperties;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

public class WebClientRetryFilter implements ExchangeFilterFunction {
    private final BaseUtilityProperties properties;

    public WebClientRetryFilter(BaseUtilityProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .retryWhen(Retry.backoff(
                        properties.getHttpClient().getMaxRetries(),
                        Duration.ofMillis(properties.getHttpClient().getRetryDelayMs())
                ).filter(this::isRetryableException));
    }

    private boolean isRetryableException(Throwable throwable) {
        return throwable instanceof java.net.ConnectException ||
                throwable instanceof java.net.SocketTimeoutException ||
                throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException;
    }
}
