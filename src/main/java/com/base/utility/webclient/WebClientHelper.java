package com.base.utility.webclient;

import com.base.utility.exception.type.BusinessException;
import com.base.utility.exception.utils.ErrorCode;
import com.base.utility.logging.StructuredLogger;
import com.base.utility.properties.WebClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientHelper {
    private static final StructuredLogger logger = StructuredLogger.getLogger(WebClientHelper.class);

    private final WebClient webClient;
    private final WebClientProperties properties;
    private final WebClientResponseHandler responseHandler;

    // Synchronous GET
    public <T> T get(String uri, Class<T> responseType) {
        return get(uri, responseType, null);
    }

    public <T> T get(String uri, Class<T> responseType, Map<String, String> headers) {
        return executeSync(() ->
                buildRequest(HttpMethod.GET, uri, null, headers)
                        .retrieve()
                        .bodyToMono(responseType)
        );
    }

    public <T> T get(String uri, ParameterizedTypeReference<T> responseType) {
        return get(uri, responseType, null);
    }

    public <T> T get(String uri, ParameterizedTypeReference<T> responseType, Map<String, String> headers) {
        return executeSync(() ->
                buildRequest(HttpMethod.GET, uri, null, headers)
                        .retrieve()
                        .bodyToMono(responseType)
        );
    }

    // Asynchronous GET
    public <T> Mono<T> getAsync(String uri, Class<T> responseType) {
        return getAsync(uri, responseType, null);
    }

    public <T> Mono<T> getAsync(String uri, Class<T> responseType, Map<String, String> headers) {
        return executeAsync(() ->
                buildRequest(HttpMethod.GET, uri, null, headers)
                        .retrieve()
                        .bodyToMono(responseType)
        );
    }

    // Synchronous POST
    public <T, R> R post(String uri, T requestBody, Class<R> responseType) {
        return post(uri, requestBody, responseType, null);
    }

    public <T, R> R post(String uri, T requestBody, Class<R> responseType, Map<String, String> headers) {
        return executeSync(() ->
                buildRequest(HttpMethod.POST, uri, requestBody, headers)
                        .retrieve()
                        .bodyToMono(responseType)
        );
    }

    // Asynchronous POST
    public <T, R> Mono<R> postAsync(String uri, T requestBody, Class<R> responseType) {
        return postAsync(uri, requestBody, responseType, null);
    }

    public <T, R> Mono<R> postAsync(String uri, T requestBody, Class<R> responseType, Map<String, String> headers) {
        return executeAsync(() ->
                buildRequest(HttpMethod.POST, uri, requestBody, headers)
                        .retrieve()
                        .bodyToMono(responseType)
        );
    }

    // Synchronous PUT
    public <T, R> R put(String uri, T requestBody, Class<R> responseType) {
        return put(uri, requestBody, responseType, null);
    }

    public <T, R> R put(String uri, T requestBody, Class<R> responseType, Map<String, String> headers) {
        return executeSync(() ->
                buildRequest(HttpMethod.PUT, uri, requestBody, headers)
                        .retrieve()
                        .bodyToMono(responseType)
        );
    }

    // Synchronous DELETE
    public void delete(String uri) {
        delete(uri, null);
    }

    public void delete(String uri, Map<String, String> headers) {
        executeSync(() ->
                buildRequest(HttpMethod.DELETE, uri, null, headers)
                        .retrieve()
                        .bodyToMono(Void.class)
        );
    }

    private WebClient.RequestBodySpec buildRequest(HttpMethod method, String uri, Object body, Map<String, String> headers) {
        WebClient.RequestBodyUriSpec uriSpec = webClient.method(method);
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(uri);

        if (headers != null) {
            headers.forEach(bodySpec::header);
        }

        bodySpec.contentType(MediaType.APPLICATION_JSON);

        if (body != null) {
            bodySpec.bodyValue(body);
        }

        return bodySpec;
    }

    private <T> T executeSync(java.util.function.Supplier<Mono<T>> monoSupplier) {
        long startTime = System.currentTimeMillis();

        try {
            return monoSupplier.get()
                    .retryWhen(Retry.backoff(properties.maxRetries(), properties.retryDelay()))
                    .doOnError(this::logError)
                    .onErrorMap(this::mapException)
                    .block(properties.responseTimeout());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("WebClient request completed")
                    .field("executionTimeMs", executionTime)
                    .log();
        }
    }

    private <T> Mono<T> executeAsync(java.util.function.Supplier<Mono<T>> monoSupplier) {
        long startTime = System.currentTimeMillis();

        return monoSupplier.get()
                .retryWhen(Retry.backoff(properties.maxRetries(), properties.retryDelay()))
                .doOnError(this::logError)
                .onErrorMap(this::mapException)
                .doFinally(signalType -> {
                    long executionTime = System.currentTimeMillis() - startTime;
                    logger.info("Async WebClient request completed")
                            .field("executionTimeMs", executionTime)
                            .field("signalType", signalType.toString())
                            .log();
                });
    }

    private void logError(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            logger.error("WebClient error response")
                    .field("statusCode", webClientError.getStatusCode().value())
                    .field("responseBody", webClientError.getResponseBodyAsString())
                    .log();
        } else {
            logger.error("WebClient error", error)
                    .log();
        }
    }

    private Throwable mapException(Throwable error) {
        if (error instanceof WebClientResponseException webClientError) {
            HttpStatus statusCode = (HttpStatus) webClientError.getStatusCode();
            return switch (statusCode.series()) {
                case CLIENT_ERROR -> new BusinessException(ErrorCode.VALIDATION_FAILED,
                        "Client error: " + webClientError.getResponseBodyAsString());
                case SERVER_ERROR -> new BusinessException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                        "External service error: " + webClientError.getResponseBodyAsString());
                default -> new BusinessException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                        "External service error: " + error.getMessage());
            };
        }
        return new BusinessException(ErrorCode.EXTERNAL_SERVICE_TIMEOUT,
                "External service communication failed: " + error.getMessage());
    }
}
