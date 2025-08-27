package com.base.utility.webclient;

import com.base.utility.exception.type.BusinessException;
import com.base.utility.exception.utils.ErrorCode;
import com.base.utility.logging.StructuredLogger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Component
public class WebClientResponseHandler {
    private static final StructuredLogger logger = StructuredLogger.getLogger(WebClientResponseHandler.class);

    public Mono<ClientResponse> handleResponse(ClientResponse response) {
        HttpStatus statusCode = (HttpStatus) response.statusCode();

        if (statusCode.isError()) {
            return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        logger.error("WebClient error response received")
                                .field("statusCode", statusCode.value())
                                .field("errorBody", errorBody)
                                .log();

                        ErrorCode errorCode = mapToErrorCode(statusCode);
                        return Mono.error(new BusinessException(errorCode, errorBody));
                    });
        }

        return Mono.just(response);
    }

    private ErrorCode mapToErrorCode(HttpStatus statusCode) {
        return switch (statusCode.series()) {
            case CLIENT_ERROR -> {
                if (statusCode == HttpStatus.UNAUTHORIZED) {
                    yield ErrorCode.UNAUTHORIZED;
                } else if (statusCode == HttpStatus.FORBIDDEN) {
                    yield ErrorCode.FORBIDDEN;
                } else if (statusCode == HttpStatus.NOT_FOUND) {
                    yield ErrorCode.RESOURCE_NOT_FOUND;
                } else {
                    yield ErrorCode.VALIDATION_FAILED;
                }
            }
            case SERVER_ERROR -> ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE;
            default -> ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE;
        };
    }
}
