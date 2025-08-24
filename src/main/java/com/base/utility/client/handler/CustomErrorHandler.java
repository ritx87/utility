package com.base.utility.client.handler;

import com.base.utility.exception.type.*;
import com.base.utility.exception.utils.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomErrorHandler extends DefaultResponseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = (HttpStatus) response.getStatusCode();
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

        logger.error("HTTP Error - Status: {}, Body: {}", statusCode, responseBody);

        switch (statusCode) {
            case NOT_FOUND:
                throw new ResourceNotFoundException("Resource not found");
            case UNAUTHORIZED:
                throw new UnauthorizedException("Unauthorized access");
            case FORBIDDEN:
                throw new ForbiddenException("Forbidden access");
            case CONFLICT:
                throw new ConflictException("Resource conflict: " + responseBody);
            case BAD_REQUEST:
                throw new ValidationException("Bad request: " + responseBody);
            default:
                if (statusCode.is4xxClientError()) {
                    throw new BusinessException(ErrorCode.TECHNICAL_ERROR,"Client error: " + responseBody);
                } else if (statusCode.is5xxServerError()) {
                    throw new ExternalServiceException("HTTP", "Server error: " + responseBody);
                } else {
                    throw new TechnicalException("HTTP error: " + responseBody);
                }
        }
    }
}
