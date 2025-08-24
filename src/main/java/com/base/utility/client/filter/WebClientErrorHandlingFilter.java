package com.base.utility.client.filter;

import com.base.utility.exception.type.*;
import com.base.utility.exception.utils.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * WebClient filter for error handling
 */
public class WebClientErrorHandlingFilter implements ExchangeFilterFunction {@Override
public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return next.exchange(request)
            .flatMap(this::processResponse);
}

    private Mono<ClientResponse> processResponse(ClientResponse response) {
        if (response.statusCode().isError()) {
            return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                            return Mono.error(new ResourceNotFoundException("Resource not found"));
                        } else if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                            return Mono.error(new UnauthorizedException("Unauthorized"));
                        } else if (response.statusCode() == HttpStatus.FORBIDDEN) {
                            return Mono.error(new ForbiddenException("Forbidden"));
                        } else if (response.statusCode().is4xxClientError()) {
                            return Mono.error(new BusinessException(ErrorCode.TECHNICAL_ERROR,"Client error: " + errorBody));
                        } else {
                            return Mono.error(new ExternalServiceException("HTTP", "Server error: " + errorBody));
                        }
                    });
        }
        return Mono.just(response);
    }

}
