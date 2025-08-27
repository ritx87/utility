package com.base.utility.exception.utils;


import com.base.utility.exception.response.ApiResponse;
import com.base.utility.exception.type.BusinessException;
import com.base.utility.exception.type.DuplicateResourceException;
import com.base.utility.exception.type.ResourceNotFoundException;
import com.base.utility.exception.type.ValidationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import static com.base.utility.utils.AppConstant.CORRELATION_ID;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {} - Code: {}", ex.getMessage(), ex.getErrorCodeString());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCodeString(),
                ex.getMessage(),
                ex.getDetails(),
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {} - Code: {}", ex.getMessage(), ex.getErrorCodeString());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCodeString(),
                ex.getMessage(),
                ex.getDetails(),
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {} - Code: {}", ex.getMessage(), ex.getErrorCodeString());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCodeString(),
                ex.getMessage(),
                ex.getDetails(),
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {} - Code: {}", ex.getMessage(), ex.getErrorCodeString());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getErrorCodeString(),
                ex.getMessage(),
                ex.getDetails(),
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Request validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing // Keep first error message if multiple errors for same field
                ));

        // Get rejected values for each field that has errors
        Map<String, Object> rejectedValues = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getRejectedValue() != null ? error.getRejectedValue() : "null",
                        (existing, replacement) -> existing
                ));

        Map<String, Object> errorDetails = Map.of(
                "fieldErrors", fieldErrors,
                "rejectedValues", rejectedValues,
                "objectName", ex.getBindingResult().getObjectName()
        );

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.VALIDATION_FAILED.getCode(),
                "Request validation failed. Please check the field errors.",
                errorDetails,
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.VALIDATION_FAILED.getCode(),
                "Validation constraint violation",
                Map.of("violations", violations),
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INVALID_REQUEST.getCode(),
                "Malformed JSON request. Please check your request body format.",
                null,
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch for parameter {}: {}", ex.getName(), ex.getMessage());

        Map<String, Object> errorDetails = Map.of(
                "parameter", ex.getName(),
                "providedValue", ex.getValue(),
                "expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INVALID_FIELD_FORMAT.getCode(),
                String.format("Invalid value for parameter '%s'. Expected %s but got '%s'",
                        ex.getName(),
                        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                        ex.getValue()),
                errorDetails,
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing required parameter: {}", ex.getParameterName());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.MISSING_REQUIRED_FIELD.getCode(),
                String.format("Required parameter '%s' is missing", ex.getParameterName()),
                Map.of("parameter", ex.getParameterName(), "type", ex.getParameterType()),
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {} for this endpoint", ex.getMethod());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()),
                Map.of("supportedMethods", ex.getSupportedMethods()),
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Database constraint violation", ex);

        String message = "Data integrity constraint violation";
        ErrorCode errorCode = ErrorCode.CONSTRAINT_VIOLATION;

        // Check for common constraint violations
        if (ex.getMessage() != null) {
            String lowerMessage = ex.getMessage().toLowerCase();
            if (lowerMessage.contains("unique") || lowerMessage.contains("duplicate")) {
                message = "Duplicate entry. This record already exists.";
                errorCode = ErrorCode.DUPLICATE_RESOURCE;
            } else if (lowerMessage.contains("foreign key")) {
                message = "Referenced record does not exist or is being used by another record.";
            }
        }

        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getCode(),
                message,
                null,
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLException(SQLException ex) {
        log.error("Database error occurred", ex);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.DATABASE_CONNECTION_ERROR.getCode(),
                ErrorCode.DATABASE_CONNECTION_ERROR.getMessage(),
                null,
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                null,
                MDC.get(CORRELATION_ID)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
