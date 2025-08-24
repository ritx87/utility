package com.base.utility.aspect;

import com.base.utility.common.autoconfigure.BaseUtilityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static com.base.utility.utils.AppConstant.MDC_KEY;

/**
 * Aspect for API auditing and request/response logging
 */
@Aspect
public class ApiAuditingAspect {
    private static final Logger auditLogger = LoggerFactory.getLogger("API_AUDIT");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BaseUtilityProperties properties;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller)")
    public void controllerMethods() {}

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceMethods() {}

    @Around("controllerMethods()")
    public Object auditApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.getAspect().getAuditing().isEnabled() ||
                !properties.getAspect().getAuditing().isAuditControllers()) {
            return joinPoint.proceed();
        }

        String requestId = generateRequestId();
        MDC.put("requestId", requestId);

        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getCurrentHttpRequest();

        try {
            auditRequest(joinPoint, request, requestId);
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            auditResponse(joinPoint, result, duration, requestId, "SUCCESS");
            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            auditResponse(joinPoint, null, duration, requestId, "ERROR: " + e.getMessage());
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }

    @Around("serviceMethods()")
    public Object auditServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.getAspect().getAuditing().isEnabled() ||
                !properties.getAspect().getAuditing().isAuditServices()) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        try {
            auditLogger.info("Service Call - Class: {}, Method: {}, Started: {}",
                    className, methodName, LocalDateTime.now());

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            auditLogger.info("Service Call - Class: {}, Method: {}, Duration: {}ms, Status: SUCCESS",
                    className, methodName, duration);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            auditLogger.error("Service Call - Class: {}, Method: {}, Duration: {}ms, Status: ERROR, Error: {}",
                    className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    private void auditRequest(JoinPoint joinPoint, HttpServletRequest request, String requestId) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
            String uri = request != null ? request.getRequestURI() : "UNKNOWN";
            String clientIp = getClientIpAddress(request);

            StringBuilder auditMessage = new StringBuilder();
            auditMessage.append("API Request - RequestId: ").append(requestId)
                    .append(", Method: ").append(httpMethod)
                    .append(", URI: ").append(uri)
                    .append(", Controller: ").append(className)
                    .append(", Action: ").append(methodName)
                    .append(", ClientIP: ").append(clientIp)
                    .append(", Timestamp: ").append(LocalDateTime.now());

            if (properties.getAspect().getAuditing().isIncludeRequestBody() && joinPoint.getArgs().length > 0) {
                String requestBody = objectMapper.writeValueAsString(joinPoint.getArgs());
                auditMessage.append(", RequestBody: ").append(requestBody);
            }

            auditLogger.info(auditMessage.toString());

        } catch (Exception e) {
            auditLogger.error("Error auditing request: {}", e.getMessage());
        }
    }

    private void auditResponse(JoinPoint joinPoint, Object result, long duration,
                               String requestId, String status) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            StringBuilder auditMessage = new StringBuilder();
            auditMessage.append("API Response - RequestId: ").append(requestId)
                    .append(", Controller: ").append(className)
                    .append(", Action: ").append(methodName)
                    .append(", Duration: ").append(duration).append("ms")
                    .append(", Status: ").append(status)
                    .append(", Timestamp: ").append(LocalDateTime.now());

            if (properties.getAspect().getAuditing().isIncludeResponseBody() && result != null) {
                String responseBody = objectMapper.writeValueAsString(result);
                auditMessage.append(", ResponseBody: ").append(responseBody);
            }

            auditLogger.info(auditMessage.toString());

        } catch (Exception e) {
            auditLogger.error("Error auditing response: {}", e.getMessage());
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};

        return Arrays.stream(headerNames)
                .map(request::getHeader)
                .filter(Objects::nonNull)
                .map(String::trim)
                .findFirst()
                .orElse(null);
    }

    private String generateRequestId() {
        return "REQ-" + MDC.get(MDC_KEY);
        //return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
