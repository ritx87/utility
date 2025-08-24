package com.base.utility.client;

import com.base.utility.common.autoconfigure.BaseUtilityProperties;
import com.base.utility.exception.type.*;
import com.base.utility.exception.utils.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Complete HTTP Client Service supporting all HTTP methods and client types
 */
@Service
public class HttpClientService {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientService.class);

    @Autowired
    private WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private RestClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BaseUtilityProperties properties;

    @Autowired
    private HttpHeadersBuilder headersBuilder;

    // GET Methods
    public String getForString(String url) {
        return getForString(url, null, null);
    }

    public String getForString(String url, Map<String, String> queryParams, HttpHeaders headers) {
        try {
            String finalUrl = buildUrlWithParams(url, queryParams);

            return webClient.get()
                    .uri(finalUrl)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(properties.getHttpClient().getMaxRetries(),
                            Duration.ofMillis(properties.getHttpClient().getRetryDelayMs())))
                    .block();

        } catch (Exception e) {
            logger.error("GET request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "GET request failed: " + e.getMessage(), e);
        }
    }

    public <T> T getForObject(String url, Class<T> responseType) {
        return getForObject(url, responseType, null, null);
    }

    public <T> T getForObject(String url, Class<T> responseType, Map<String, String> queryParams, HttpHeaders headers) {
        try {
            String finalUrl = buildUrlWithParams(url, queryParams);

            return webClient.get()
                    .uri(finalUrl)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(responseType)
                    .retryWhen(Retry.backoff(properties.getHttpClient().getMaxRetries(),
                            Duration.ofMillis(properties.getHttpClient().getRetryDelayMs())))
                    .block();

        } catch (Exception e) {
            logger.error("GET request failed for URL: {} with response type: {}", url, responseType.getSimpleName(), e);
            throw new ExternalServiceException("HTTP", "GET request failed: " + e.getMessage(), e);
        }
    }

    public <T> T getForObject(String url, ParameterizedTypeReference<T> responseType, Map<String, String> queryParams, HttpHeaders headers) {
        try {
            String finalUrl = buildUrlWithParams(url, queryParams);
            HttpEntity<?> entity = new HttpEntity<>(headers != null ? headers : headersBuilder.createCommonHeaders());

            ResponseEntity<T> response = restTemplate.exchange(
                    finalUrl, HttpMethod.GET, entity, responseType);

            return response.getBody();

        } catch (Exception e) {
            logger.error("GET request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "GET request failed: " + e.getMessage(), e);
        }
    }

    // POST Methods
    public <T, R> R postForObject(String url, T requestBody, Class<R> responseType) {
        return postForObject(url, requestBody, responseType, null);
    }

    public <T, R> R postForObject(String url, T requestBody, Class<R> responseType, HttpHeaders headers) {
        try {
            return webClient.post()
                    .uri(url)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        } else {
                            httpHeaders.addAll(headersBuilder.createCommonHeaders());
                        }
                    })
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(responseType)
                    .retryWhen(Retry.backoff(properties.getHttpClient().getMaxRetries(),
                            Duration.ofMillis(properties.getHttpClient().getRetryDelayMs())))
                    .block();

        } catch (Exception e) {
            logger.error("POST request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "POST request failed: " + e.getMessage(), e);
        }
    }

    public <T, R> ResponseEntity<R> postWithHeaders(String url, T requestBody, Class<R> responseType, HttpHeaders headers) {
        try {
            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);
            return restTemplate.postForEntity(url, entity, responseType);

        } catch (Exception e) {
            logger.error("POST request with custom headers failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "POST request with custom headers failed: " + e.getMessage(), e);
        }
    }

    // PUT Methods
    public <T> void put(String url, T requestBody) {
        put(url, requestBody, null);
    }

    public <T> void put(String url, T requestBody, HttpHeaders headers) {
        try {
            webClient.put()
                    .uri(url)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        } else {
                            httpHeaders.addAll(headersBuilder.createCommonHeaders());
                        }
                    })
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            logger.error("PUT request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "PUT request failed: " + e.getMessage(), e);
        }
    }

    public <T, R> R putForObject(String url, T requestBody, Class<R> responseType, HttpHeaders headers) {
        try {
            return webClient.put()
                    .uri(url)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        } else {
                            httpHeaders.addAll(headersBuilder.createCommonHeaders());
                        }
                    })
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(responseType)
                    .block();

        } catch (Exception e) {
            logger.error("PUT request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "PUT request failed: " + e.getMessage(), e);
        }
    }

    // DELETE Methods
    public void delete(String url) {
        delete(url, null);
    }

    public void delete(String url, HttpHeaders headers) {
        try {
            webClient.delete()
                    .uri(url)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            logger.error("DELETE request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "DELETE request failed: " + e.getMessage(), e);
        }
    }

    public <T> T deleteForObject(String url, Class<T> responseType) {
        return deleteForObject(url, responseType, null);
    }

    public <T> T deleteForObject(String url, Class<T> responseType, HttpHeaders headers) {
        try {
            return webClient.delete()
                    .uri(url)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(responseType)
                    .block();

        } catch (Exception e) {
            logger.error("DELETE request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "DELETE request failed: " + e.getMessage(), e);
        }
    }

    // PATCH Methods
    public <T, R> R patchForObject(String url, T requestBody, Class<R> responseType) {
        return patchForObject(url, requestBody, responseType, null);
    }

    public <T, R> R patchForObject(String url, T requestBody, Class<R> responseType, HttpHeaders headers) {
        try {
            return webClient.patch()
                    .uri(url)
                    .headers(httpHeaders -> {
                        if (headers != null) {
                            httpHeaders.addAll(headers);
                        } else {
                            httpHeaders.addAll(headersBuilder.createCommonHeaders());
                        }
                    })
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                    .bodyToMono(responseType)
                    .block();

        } catch (Exception e) {
            logger.error("PATCH request failed for URL: {}", url, e);
            throw new ExternalServiceException("HTTP", "PATCH request failed: " + e.getMessage(), e);
        }
    }

    // Generic Exchange Method
    public <T, R> ResponseEntity<R> exchange(String url, HttpMethod method, T requestBody,
                                             Class<R> responseType, HttpHeaders headers) {
        try {
            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers != null ? headers : headersBuilder.createCommonHeaders());
            return restTemplate.exchange(url, method, entity, responseType);

        } catch (Exception e) {
            logger.error("Exchange request failed for URL: {} with method: {}", url, method, e);
            throw new ExternalServiceException("HTTP", "Exchange request failed: " + e.getMessage(), e);
        }
    }

    public <T, R> ResponseEntity<R> exchange(String url, HttpMethod method, T requestBody,
                                             ParameterizedTypeReference<R> responseType, HttpHeaders headers) {
        try {
            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers != null ? headers : headersBuilder.createCommonHeaders());
            return restTemplate.exchange(url, method, entity, responseType);

        } catch (Exception e) {
            logger.error("Exchange request failed for URL: {} with method: {}", url, method, e);
            throw new ExternalServiceException("HTTP", "Exchange request failed: " + e.getMessage(), e);
        }
    }

    // Async Methods
    public <T> Mono<T> getAsync(String url, Class<T> responseType) {
        return getAsync(url, responseType, null, null);
    }

    public <T> Mono<T> getAsync(String url, Class<T> responseType, Map<String, String> queryParams, HttpHeaders headers) {
        String finalUrl = buildUrlWithParams(url, queryParams);

        return webClient.get()
                .uri(finalUrl)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        httpHeaders.addAll(headers);
                    }
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(responseType);
    }

    public <T, R> Mono<R> postAsync(String url, T requestBody, Class<R> responseType, HttpHeaders headers) {
        return webClient.post()
                .uri(url)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        httpHeaders.addAll(headers);
                    } else {
                        httpHeaders.addAll(headersBuilder.createCommonHeaders());
                    }
                })
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(responseType);
    }

    // Helper Methods
    private String buildUrlWithParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        queryParams.forEach(builder::queryParam);
        return builder.toUriString();
    }

    private Mono<? extends Throwable> handleErrorResponse(org.springframework.web.reactive.function.client.ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    HttpStatusCode status = clientResponse.statusCode();
                    String errorMessage = String.format("HTTP %s: %s", status.value(), errorBody);

                    if (status.is4xxClientError()) {
                        if (status == HttpStatus.NOT_FOUND) {
                            return Mono.error(new ResourceNotFoundException("Resource not found"));
                        } else if (status == HttpStatus.UNAUTHORIZED) {
                            return Mono.error(new UnauthorizedException("Unauthorized access"));
                        } else if (status == HttpStatus.FORBIDDEN) {
                            return Mono.error(new ForbiddenException("Forbidden access"));
                        } else {
                            return Mono.error(new BusinessException(ErrorCode.TECHNICAL_ERROR,"Client error: " + errorMessage));
                        }
                    } else if (status.is5xxServerError()) {
                        return Mono.error(new ExternalServiceException("HTTP", "Server error: " + errorMessage));
                    } else {
                        return Mono.error(new TechnicalException("HTTP error: " + errorMessage));
                    }
                });
    }
}
