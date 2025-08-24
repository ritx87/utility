package com.base.utility.common.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "common.utils")
public class BaseUtilityProperties {
    private ExceptionProperties exception = new ExceptionProperties();
    private AspectProperties aspect = new AspectProperties();
    private HttpClientProperties httpClient = new HttpClientProperties();
    private SecurityProperties security = new SecurityProperties();

    @Data
    public static class ExceptionProperties {
        private GlobalHandlerProperties globalHandler = new GlobalHandlerProperties();

        @Data
        public static class GlobalHandlerProperties {
            private boolean enabled = true;
            private boolean includeStackTrace = false;
            private boolean sanitizeMessages = true;
        }
    }

    @Data
    public static class AspectProperties {
        private LoggingProperties logging = new LoggingProperties();
        private AuditingProperties auditing = new AuditingProperties();

        @Data
        public static class LoggingProperties {
            private boolean enabled = true;
            private String level = "INFO";
            private boolean logExecutionTime = true;
            private boolean logParameters = false;
            private boolean logReturnValues = false;
        }

        @Data
        public static class AuditingProperties {
            private boolean enabled = true;
            private boolean auditControllers = true;
            private boolean auditServices = true;
            private boolean includeRequestBody = false;
            private boolean includeResponseBody = false;
        }
    }

    @Data
    public static class HttpClientProperties {
        private boolean enabled = true;
        private int connectionTimeout = 5000;
        private int readTimeout = 30000;
        private int maxRetries = 3;
        private long retryDelayMs = 1000;
        private int maxConnections = 100;
        private boolean compressionEnabled = true;
        private RetryProperties retry = new RetryProperties();
        private LoggingProperties logging = new LoggingProperties();

        @Data
        public static class RetryProperties {
            private boolean enabled = true;
            private int maxAttempts = 3;
            private long initialDelayMs = 1000;
            private double backoffMultiplier = 2.0;
            private long maxDelayMs = 30000;
        }

        @Data
        public static class LoggingProperties {
            private boolean enabled = true;
            private boolean logHeaders = false;
            private boolean logRequestBody = false;
            private boolean logResponseBody = false;
            private int maxBodySize = 4096;
        }
    }

    @Data
    public static class SecurityProperties {
        private boolean enabled = false;
        private String issuerUri = "http://localhost:8080/realms/master";
        private String jwkSetUri = "http://localhost:8080/realms/master/protocol/openid-connect/certs";
    }
}
