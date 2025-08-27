package com.base.utility.logging;

import com.base.utility.properties.LoggingProperties;
import com.base.utility.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "base-utility.logging", name = "enabled", havingValue = "true")
public class LoggingAspect {
    private static final StructuredLogger logger = StructuredLogger.getLogger(LoggingAspect.class);
    private final LoggingProperties loggingProperties;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "CONTROLLER");
    }

    @Around("@within(org.springframework.stereotype.Service)")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecution(joinPoint, "SERVICE");
    }

    private Object logExecution(ProceedingJoinPoint joinPoint, String type) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        logger.info("Method execution started")
                .field("type", type)
                .field("class", className)
                .field("method", methodName)
                .field("args", JsonUtils.toJson(joinPoint.getArgs()))
                .log();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("Method execution completed")
                    .field("type", type)
                    .field("class", className)
                    .field("method", methodName)
                    .field("executionTimeMs", executionTime)
                    .field("result", JsonUtils.toJson(result))
                    .log();

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error("Method execution failed", e)
                    .field("type", type)
                    .field("class", className)
                    .field("method", methodName)
                    .field("executionTimeMs", executionTime)
                    .log();

            throw e;
        }
    }
}
