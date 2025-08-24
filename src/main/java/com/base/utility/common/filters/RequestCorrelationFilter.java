package com.base.utility.common.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.base.utility.utils.AppConstant.MDC_KEY;
import static com.base.utility.utils.AppConstant.REQUEST_UID_HEADER;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request
            , HttpServletResponse response
            , FilterChain filterChain) throws ServletException, IOException {
        // Try to get the UID from the header, or generate a new one
        String requestUid = Optional.ofNullable(request.getHeader(REQUEST_UID_HEADER))
                .orElse(UUID.randomUUID().toString());

        // Put the UID into the MDC for logging
        MDC.put(MDC_KEY, requestUid);

        // Add the UID to the response header
        response.setHeader(REQUEST_UID_HEADER, requestUid);

        try {
            // Continue the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Crucial: Clean up the MDC after the request is complete
            MDC.remove(MDC_KEY);
        }
    }
}

