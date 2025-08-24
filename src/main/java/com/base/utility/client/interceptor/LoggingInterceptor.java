package com.base.utility.client.interceptor;

import com.base.utility.common.autoconfigure.BaseUtilityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.base.utility.utils.AppConstant.MDC_KEY;

/**
 * RestTemplate interceptor for comprehensive request/response logging
 */
@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final int MAX_BODY_SIZE = 1024 * 4;

    @Autowired(required = false)
    private BaseUtilityProperties properties;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        String requestId = generateRequestId();

        long startTime = System.currentTimeMillis();

        try {
            logRequest(request, body, requestId);

            ClientHttpResponse response = execution.execute(request, body);
            ClientHttpResponse responseWrapper = new BufferingClientHttpResponseWrapper(response);

            long duration = System.currentTimeMillis() - startTime;
            logResponse(request, responseWrapper, duration, requestId);

            return responseWrapper;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logError(request, e, duration, requestId);
            throw e;
        } finally {
            MDC.remove("httpRequestId");
        }
    }

    private void logRequest(HttpRequest request, byte[] body, String requestId) {
        if (!isLoggingEnabled()) {
            return;
        }

        try {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("HTTP Request - ")
                    .append("ID: ").append(requestId)
                    .append(", Method: ").append(request.getMethod())
                    .append(", URI: ").append(request.getURI())
                    .append(", Timestamp: ").append(LocalDateTime.now());

            if (shouldLogHeaders()) {
                logMessage.append(", Headers: ").append(sanitizeHeaders(request.getHeaders().toString()));
            }

            if (shouldLogRequestBody() && body != null && body.length > 0) {
                String bodyContent = new String(body, StandardCharsets.UTF_8);
                if (bodyContent.length() > MAX_BODY_SIZE) {
                    bodyContent = bodyContent.substring(0, MAX_BODY_SIZE) + "... [TRUNCATED]";
                }
                logMessage.append(", Body: ").append(sanitizeBody(bodyContent));
            }

            logger.info(logMessage.toString());

        } catch (Exception e) {
            logger.warn("Failed to log HTTP request: {}", e.getMessage());
        }
    }

    private void logResponse(HttpRequest request, ClientHttpResponse response, long duration, String requestId) {
        if (!isLoggingEnabled()) {
            return;
        }

        try {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("HTTP Response - ")
                    .append("ID: ").append(requestId)
                    .append(", Method: ").append(request.getMethod())
                    .append(", URI: ").append(request.getURI())
                    .append(", Status: ").append(response.getStatusCode())
                    .append(", Duration: ").append(duration).append("ms")
                    .append(", Timestamp: ").append(LocalDateTime.now());

            if (shouldLogHeaders()) {
                logMessage.append(", Headers: ").append(response.getHeaders().toString());
            }

            if (shouldLogResponseBody()) {
                String responseBody = getResponseBody(response);
                if (responseBody != null && !responseBody.isEmpty()) {
                    if (responseBody.length() > MAX_BODY_SIZE) {
                        responseBody = responseBody.substring(0, MAX_BODY_SIZE) + "... [TRUNCATED]";
                    }
                    logMessage.append(", Body: ").append(sanitizeBody(responseBody));
                }
            }

            logger.info(logMessage.toString());

        } catch (Exception e) {
            logger.warn("Failed to log HTTP response: {}", e.getMessage());
        }
    }

    private void logError(HttpRequest request, Exception exception, long duration, String requestId) {
        if (!isLoggingEnabled()) {
            return;
        }

        try {
            logger.error("HTTP Request Error - " +
                            "ID: {}, Method: {}, URI: {}, Duration: {}ms, " +
                            "Error: {}, Timestamp: {}",
                    requestId,
                    request.getMethod(),
                    request.getURI(),
                    duration,
                    exception.getMessage(),
                    LocalDateTime.now(),
                    exception);

        } catch (Exception e) {
            logger.warn("Failed to log HTTP error: {}", e.getMessage());
        }
    }

    private String getResponseBody(ClientHttpResponse response) {
        try {
            if (response instanceof BufferingClientHttpResponseWrapper) {
                return ((BufferingClientHttpResponseWrapper) response).getBodyAsString();
            }
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.debug("Could not read response body for logging: {}", e.getMessage());
            return "[Body could not be read]";
        }
    }

    private String sanitizeHeaders(String headers) {
        if (headers == null) return "";

        return headers.replaceAll("(?i)(authorization|cookie|x-api-key|x-auth-token)=[^,\\]]*", "$1=***")
                .replaceAll("(?i)(password|secret|key)=[^,\\]]*", "$1=***");
    }

    private String sanitizeBody(String body) {
        if (body == null) return "";

        return body.replaceAll("(?i)\"(password|secret|token|key|authorization)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***\"")
                .replaceAll("(?i)\"(password|secret|token|key|authorization)\"\\s*:\\s*[^,}\\]]*", "\"$1\":\"***\"");
    }

    private String generateRequestId() {
        return "REQ-" + MDC.get(MDC_KEY);
    }

    private boolean isLoggingEnabled() {
        return properties == null ||
                properties.getHttpClient() == null ||
                properties.getHttpClient().getLogging().isEnabled();
    }

    private boolean shouldLogHeaders() {
        return properties != null &&
                properties.getHttpClient() != null &&
                properties.getHttpClient().getLogging().isLogHeaders();
    }

    private boolean shouldLogRequestBody() {
        return properties != null &&
                properties.getHttpClient() != null &&
                properties.getHttpClient().getLogging().isLogRequestBody();
    }

    private boolean shouldLogResponseBody() {
        return properties != null &&
                properties.getHttpClient() != null &&
                properties.getHttpClient().getLogging().isLogResponseBody();
    }

    private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private byte[] body;

        public BufferingClientHttpResponseWrapper(ClientHttpResponse response) {
            this.response = response;
        }

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public java.io.InputStream getBody() throws IOException {
            if (body == null) {
                body = StreamUtils.copyToByteArray(response.getBody());
            }
            return new java.io.ByteArrayInputStream(body);
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        public String getBodyAsString() {
            try {
                if (body == null) {
                    body = StreamUtils.copyToByteArray(response.getBody());
                }
                return new String(body, StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "[Could not read body]";
            }
        }
    }
}
