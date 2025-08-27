package com.base.utility.exception.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Generic errors (1000-1999)
    INTERNAL_SERVER_ERROR("ERR_1000", "An unexpected error occurred. Please try again later."),
    INVALID_REQUEST("ERR_1001", "Invalid request format or parameters"),
    UNAUTHORIZED("ERR_1002", "Authentication required"),
    FORBIDDEN("ERR_1003", "Access denied"),
    METHOD_NOT_ALLOWED("ERR_1004", "HTTP method not allowed"),
    UNSUPPORTED_MEDIA_TYPE("ERR_1005", "Unsupported media type"),
    TOO_MANY_REQUESTS("ERR_1006", "Too many requests. Please try again later"),

    // Validation errors (2000-2999)
    VALIDATION_FAILED("ERR_2000", "Request validation failed"),
    MISSING_REQUIRED_FIELD("ERR_2001", "Required field is missing"),
    INVALID_FIELD_FORMAT("ERR_2002", "Field format is invalid"),
    FIELD_TOO_LONG("ERR_2003", "Field value exceeds maximum length"),
    FIELD_TOO_SHORT("ERR_2004", "Field value is below minimum length"),
    INVALID_EMAIL_FORMAT("ERR_2005", "Invalid email address format"),
    INVALID_DATE_FORMAT("ERR_2006", "Invalid date format"),

    // Resource errors (3000-3999)
    RESOURCE_NOT_FOUND("ERR_3000", "Requested resource not found"),
    USER_NOT_FOUND("ERR_3001", "User not found"),
    DUPLICATE_RESOURCE("ERR_3002", "Resource already exists"),
    RESOURCE_CONFLICT("ERR_3003", "Resource conflict"),
    RESOURCE_LOCKED("ERR_3004", "Resource is currently locked"),

    // Business logic errors (4000-4999)
    DUPLICATE_EMAIL("ERR_4000", "Email address already exists"),
    INVALID_USER_STATUS("ERR_4001", "Invalid user status"),
    USER_ALREADY_ACTIVE("ERR_4002", "User is already active"),
    USER_ALREADY_INACTIVE("ERR_4003", "User is already inactive"),
    OPERATION_NOT_ALLOWED("ERR_4004", "Operation not allowed for current user status"),

    // Database errors (5000-5999)
    DATABASE_CONNECTION_ERROR("ERR_5000", "Database connection failed"),
    DATABASE_TIMEOUT("ERR_5001", "Database operation timeout"),
    CONSTRAINT_VIOLATION("ERR_5002", "Database constraint violation"),

    // External service errors (6000-6999)
    EXTERNAL_SERVICE_UNAVAILABLE("ERR_6000", "External service is unavailable"),
    EXTERNAL_SERVICE_TIMEOUT("ERR_6001", "External service timeout"),
    INVALID_EXTERNAL_RESPONSE("ERR_6002", "Invalid response from external service"),
    DATABASE_ERROR("ERR_6002", "Database operation failed" );

    private final String code;
    private final String message;
}
