package com.base.utility.client.handler;

import com.base.utility.exception.type.*;
import com.base.utility.exception.utils.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Enhanced error handler for RestTemplate with detailed error mapping
 */
public class RestTemplateErrorHandler implements ResponseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }


    private void handleClientError(HttpStatus statusCode, String errorMessage) {
        switch (statusCode) {
            case BAD_REQUEST:
                throw new ValidationException("Bad request: " + errorMessage);
            case UNAUTHORIZED:
                throw new UnauthorizedException("Authentication required: " + errorMessage);
            case FORBIDDEN:
                throw new ForbiddenException("Access denied: " + errorMessage);
            case NOT_FOUND:
                throw new ResourceNotFoundException("Resource not found: " + errorMessage);
            case CONFLICT:
                throw new ConflictException("Resource conflict: " + errorMessage);
            case UNPROCESSABLE_ENTITY:
                throw new ValidationException("Validation failed: " + errorMessage);
            case TOO_MANY_REQUESTS:
                throw new ExternalServiceException("HTTP", "Rate limit exceeded: " + errorMessage);
            default:
                throw new BusinessException(ErrorCode.TECHNICAL_ERROR,"Client error: " + errorMessage);
        }
    }

    private void handleServerError(HttpStatus statusCode, String errorMessage) {
        switch (statusCode) {
            case INTERNAL_SERVER_ERROR:
                throw new ExternalServiceException("HTTP", "Internal server error: " + errorMessage);
            case BAD_GATEWAY:
                throw new ExternalServiceException("HTTP", "Bad gateway: " + errorMessage);
            case SERVICE_UNAVAILABLE:
                throw new ExternalServiceException("HTTP", "Service unavailable: " + errorMessage);
            case GATEWAY_TIMEOUT:
                throw new ExternalServiceException("HTTP", "Gateway timeout: " + errorMessage);
            default:
                throw new ExternalServiceException("HTTP", "Server error: " + errorMessage);
        }
    }

    private String getResponseBody(ClientHttpResponse response) {
        try {
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.debug("Could not read response body: {}", e.getMessage());
            return "[Could not read response body]";
        }
    }
}
