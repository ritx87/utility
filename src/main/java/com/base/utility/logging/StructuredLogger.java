package com.base.utility.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.base.utility.utils.AppConstant.CORRELATION_ID;


public class StructuredLogger {
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final Map<String, Object> fields;

    // ✅ Private constructor - no Spring injection needed
    private StructuredLogger(Logger logger) {
        this.logger = logger;
        this.objectMapper = createObjectMapper();
        this.fields = new HashMap<>();
    }

    // ✅ Static factory method - this is how you get instances
    public static StructuredLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(LoggerFactory.getLogger(clazz));
    }

    public StructuredLogger field(String key, Object value) {
        fields.put(key, value);
        return this;
    }

    public StructuredLogger info(String message) {
        return logMessage("INFO", message, null);
    }

    public StructuredLogger error(String message) {
        return logMessage("ERROR", message, null);
    }

    public StructuredLogger error(String message, Throwable throwable) {
        return logMessage("ERROR", message, throwable);
    }

    public StructuredLogger warn(String message) {
        return logMessage("WARN", message, null);
    }

    public StructuredLogger debug(String message) {
        return logMessage("DEBUG", message, null);
    }

    private StructuredLogger logMessage(String level, String message, Throwable throwable) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("level", level);
        logEntry.put("message", message);
        logEntry.put("correlationId", MDC.get("correlationId"));
        logEntry.putAll(fields);

        if (throwable != null) {
            logEntry.put("exception", throwable.getClass().getSimpleName());
            logEntry.put("stackTrace", throwable.getMessage());
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(logEntry);
            switch (level) {
                case "INFO" -> logger.info(jsonMessage);
                case "ERROR" -> {
                    if (throwable != null) {
                        logger.error(jsonMessage, throwable);
                    } else {
                        logger.error(jsonMessage);
                    }
                }
                case "WARN" -> logger.warn(jsonMessage);
                case "DEBUG" -> logger.debug(jsonMessage);
            }
        } catch (Exception e) {
            // ✅ Fallback to simple logging if JSON fails
            logger.error("Failed to log structured message: {} - Error: {}", message, e.getMessage());
        }

        return this;
    }

    // ✅ Create ObjectMapper with Java 8 time support
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    public void log() {
        fields.clear();
    }
}
