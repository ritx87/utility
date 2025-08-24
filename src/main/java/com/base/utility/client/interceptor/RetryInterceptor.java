package com.base.utility.client.interceptor;

import com.base.utility.common.autoconfigure.BaseUtilityProperties;
import com.base.utility.exception.type.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * RestTemplate interceptor for implementing retry logic on failed HTTP requests
 */
@Component
public class RetryInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);

    private final BaseUtilityProperties properties;

    private static final Set<HttpStatus> RETRYABLE_STATUS_CODES = new HashSet<>(Arrays.asList(
            HttpStatus.REQUEST_TIMEOUT,
            HttpStatus.TOO_MANY_REQUESTS,
            HttpStatus.INTERNAL_SERVER_ERROR,
            HttpStatus.BAD_GATEWAY,
            HttpStatus.SERVICE_UNAVAILABLE,
            HttpStatus.GATEWAY_TIMEOUT
    ));

    private static final Set<Class<? extends Exception>> RETRYABLE_EXCEPTIONS = new HashSet<>(Arrays.asList(
            ConnectException.class,
            SocketTimeoutException.class,
            ResourceAccessException.class
    ));

    public RetryInterceptor(BaseUtilityProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        int maxRetries = getMaxRetries();
        long retryDelay = getRetryDelay();

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                if (attempt > 1) {
                    logger.info("HTTP Retry attempt {} of {} for {} {}",
                            attempt - 1, maxRetries, request.getMethod(), request.getURI());
                }

                ClientHttpResponse response = execution.execute(request, body);

                if (attempt <= maxRetries && shouldRetryForStatus(response)) {
                    logger.warn("HTTP Request returned retryable status {} for {} {}. Attempt {} of {}",
                            response.getStatusCode(), request.getMethod(), request.getURI(),
                            attempt, maxRetries + 1);

                    response.close();
                    waitBeforeRetry(retryDelay, attempt);
                    continue;
                }

                if (attempt > 1) {
                    logger.info("HTTP Request succeeded on retry attempt {} for {} {}",
                            attempt - 1, request.getMethod(), request.getURI());
                }

                return response;

            } catch (Exception e) {
                lastException = e;

                if (attempt > maxRetries || !shouldRetryForException(e)) {
                    logger.error("HTTP Request failed permanently after {} attempts for {} {}: {}",
                            attempt, request.getMethod(), request.getURI(), e.getMessage());
                    throw e;
                }

                logger.warn("HTTP Request failed on attempt {} of {} for {} {}: {}. Will retry.",
                        attempt, maxRetries + 1, request.getMethod(), request.getURI(), e.getMessage());

                waitBeforeRetry(retryDelay, attempt);
            }
        }

        if (lastException instanceof IOException) {
            throw (IOException) lastException;
        } else if (lastException instanceof RuntimeException) {
            throw (RuntimeException) lastException;
        } else {
            throw new TechnicalException("HTTP request failed after retries", lastException);
        }
    }

    private boolean shouldRetryForStatus(ClientHttpResponse response) {
        try {
            HttpStatus statusCode = HttpStatus.resolve(response.getStatusCode().value());
            return statusCode != null && RETRYABLE_STATUS_CODES.contains(statusCode);
        } catch (IOException e) {
            logger.debug("Could not determine status code for retry decision: {}", e.getMessage());
            return false;
        }
    }

    private boolean shouldRetryForException(Exception exception) {
        for (Class<? extends Exception> retryableType : RETRYABLE_EXCEPTIONS) {
            if (retryableType.isInstance(exception)) {
                return true;
            }
        }

        if (exception instanceof HttpServerErrorException) {
            HttpServerErrorException serverException = (HttpServerErrorException) exception;
            return RETRYABLE_STATUS_CODES.contains(serverException.getStatusCode());
        }

        if (exception instanceof HttpClientErrorException) {
            HttpClientErrorException clientException = (HttpClientErrorException) exception;
            return RETRYABLE_STATUS_CODES.contains(clientException.getStatusCode());
        }

        Throwable cause = exception.getCause();
        if (cause != null) {
            for (Class<? extends Exception> retryableType : RETRYABLE_EXCEPTIONS) {
                if (retryableType.isInstance(cause)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void waitBeforeRetry(long baseDelay, int attemptNumber) {
        try {
            long delay = baseDelay * (1L << (attemptNumber - 1));
            long maxDelay = 30000;
            delay = Math.min(delay, maxDelay);

            logger.debug("Waiting {}ms before retry attempt {}", delay, attemptNumber);
            Thread.sleep(delay);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TechnicalException("Retry interrupted", e);
        }
    }

    private int getMaxRetries() {
        if (properties != null && properties.getHttpClient() != null) {
            return properties.getHttpClient().getMaxRetries();
        }
        return 3;
    }

    private long getRetryDelay() {
        if (properties != null && properties.getHttpClient() != null) {
            return properties.getHttpClient().getRetryDelayMs();
        }
        return 1000;
    }
}
