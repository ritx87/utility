package com.base.utility.aspect;

import com.base.utility.common.autoconfigure.BaseUtilityProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Aspect for logging method executions, parameters, return values, and execution time
 */
@Aspect
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Autowired
    private BaseUtilityProperties properties;

    @Pointcut("execution(public * *..*Service*.*(..)) || " +
            "execution(public * *..*Controller*.*(..))")
    public void serviceAndControllerMethods() {}

    @Around("serviceAndControllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.getAspect().getLogging().isEnabled()) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            if (properties.getAspect().getLogging().isLogParameters()) {
                logger.info("Entering method: {}.{}() with parameters: {}",
                        className, methodName, joinPoint.getArgs());
            } else {
                logger.info("Entering method: {}.{}()", className, methodName);
            }

            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            if (properties.getAspect().getLogging().isLogExecutionTime()) {
                logger.info("Exiting method: {}.{}() - Execution time: {}ms",
                        className, methodName, executionTime);
            }

            if (properties.getAspect().getLogging().isLogReturnValues() && result != null) {
                logger.info("Method: {}.{}() returned: {}", className, methodName, result);
            }

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Exception in method: {}.{}() after {}ms - {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @Before("serviceAndControllerMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        if (properties.getAspect().getLogging().isEnabled() &&
                !properties.getAspect().getLogging().isLogExecutionTime()) {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            logger.debug("Before method: {}.{}()", className, methodName);
        }
    }

    @AfterThrowing(pointcut = "serviceAndControllerMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        if (properties.getAspect().getLogging().isEnabled()) {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            logger.error("Exception in method: {}.{}() - {}: {}",
                    className, methodName, exception.getClass().getSimpleName(), exception.getMessage());
        }
    }
}
