package com.base.utility.audit;

import com.base.utility.properties.AuditProperties;
import com.base.utility.utils.JsonUtils;
import com.base.utility.utils.MaskingUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "base-utility.audit", name = "enabled", havingValue = "true")
public class ApiAuditingAspect {
    private final AuditEventPublisher auditEventPublisher;
    private final AuditProperties auditProperties;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object auditApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getCurrentRequest();

        if (request == null) {
            return joinPoint.proceed();
        }

        AuditEvent auditEvent = createAuditEvent(joinPoint, request, startTime);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            auditEvent = auditEvent.withResponse(result, executionTime, true);
            auditEventPublisher.publish(auditEvent);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            auditEvent = auditEvent.withError(e, executionTime);
            auditEventPublisher.publish(auditEvent);

            throw e;
        }
    }

    private AuditEvent createAuditEvent(ProceedingJoinPoint joinPoint, HttpServletRequest request, long startTime) {
        String requestBody = null;
        if (auditProperties.logRequestBody() && joinPoint.getArgs().length > 0) {
            requestBody = JsonUtils.toJson(Arrays.asList(joinPoint.getArgs()));
            requestBody = MaskingUtils.maskSensitiveData(requestBody, auditProperties.maskedFields());
            if (requestBody.length() > auditProperties.maxBodyLength()) {
                requestBody = requestBody.substring(0, auditProperties.maxBodyLength()) + "...";
            }
        }

        return AuditEvent.builder()
                .correlationId(MDC.get("correlationId"))
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .userAgent(request.getHeader("User-Agent"))
                .remoteAddr(getClientIpAddress(request))
                .requestBody(requestBody)
                .headers(getHeaders(request))
                .methodName(joinPoint.getSignature().getName())
                .className(joinPoint.getTarget().getClass().getSimpleName())
                .build();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(StringBuilder::new,
                        (sb, name) -> sb.append(name).append(":").append(request.getHeader(name)).append(";"),
                        StringBuilder::append)
                .toString();
    }
}
