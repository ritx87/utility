package com.base.utility.client;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Utility class for building HTTP headers with various authentication and custom options
 */
@Component
public class HttpHeadersBuilder {

    public HttpHeaders createCommonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.USER_AGENT, "Common-Utils-Client/1.0");
        headers.set(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache");
        return headers;
    }

    public HttpHeaders createBearerAuthHeaders(String token) {
        HttpHeaders headers = createCommonHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    public HttpHeaders createAuthHeaders(String authScheme, String credentials) {
        HttpHeaders headers = createCommonHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authScheme + " " + credentials);
        return headers;
    }

    public HttpHeaders createBasicAuthHeaders(String username, String password) {
        HttpHeaders headers = createCommonHeaders();
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
        return headers;
    }

    public HttpHeaders createCustomHeader(String headerName, String headerValue) {
        HttpHeaders headers = createCommonHeaders();
        headers.set(headerName, headerValue);
        return headers;
    }

    public HttpHeaders createMultipleCustomHeaders(Map<String, String> customHeaders) {
        HttpHeaders headers = createCommonHeaders();
        customHeaders.forEach(headers::set);
        return headers;
    }

    public HttpHeaders createApiKeyHeaders(String apiKey) {
        return createApiKeyHeaders("X-API-Key", apiKey);
    }

    public HttpHeaders createApiKeyHeaders(String headerName, String apiKey) {
        HttpHeaders headers = createCommonHeaders();
        headers.set(headerName, apiKey);
        return headers;
    }

    public HttpHeaders createSessionHeaders(String sessionId) {
        HttpHeaders headers = createCommonHeaders();
        headers.set("X-Session-ID", sessionId);
        return headers;
    }

    public HttpHeaders createFileUploadHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(HttpHeaders.USER_AGENT, "Common-Utils-Client/1.0");
        return headers;
    }

    public HttpHeaders createXmlHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_XML));
        headers.set(HttpHeaders.USER_AGENT, "Common-Utils-Client/1.0");
        return headers;
    }

    public HttpHeaders createTracingHeaders(String correlationId) {
        HttpHeaders headers = createCommonHeaders();
        headers.set("X-Correlation-ID", correlationId);
        headers.set("X-Request-ID", java.util.UUID.randomUUID().toString());
        return headers;
    }

    public HttpHeaders createClientInfoHeaders(String clientId, String clientVersion) {
        HttpHeaders headers = createCommonHeaders();
        headers.set("X-Client-ID", clientId);
        headers.set("X-Client-Version", clientVersion);
        return headers;
    }

    public HttpHeaders mergeHeaders(HttpHeaders... headerSets) {
        HttpHeaders merged = new HttpHeaders();
        for (HttpHeaders headers : headerSets) {
            if (headers != null) {
                merged.addAll(headers);
            }
        }
        return merged;
    }

    public HttpHeaders createJwtHeaders(String jwtToken) {
        HttpHeaders headers = createCommonHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
        return headers;
    }

    public HttpHeaders createConditionalHeaders(String etag, boolean ifMatch) {
        HttpHeaders headers = createCommonHeaders();
        if (ifMatch) {
            headers.setIfMatch(etag);
        } else {
            headers.setIfNoneMatch(etag);
        }
        return headers;
    }

}
