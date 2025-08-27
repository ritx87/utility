package com.base.utility.database;

import com.base.utility.exception.type.BusinessException;
import com.base.utility.exception.utils.ErrorCode;
import com.base.utility.logging.StructuredLogger;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseExceptionTranslator {
    private static final StructuredLogger logger = StructuredLogger.getLogger(DatabaseExceptionTranslator.class);

    public BusinessException translate(Exception ex) {
        logger.error("Database exception occurred", ex)
                .field("exceptionType", ex.getClass().getSimpleName())
                .field("message", ex.getMessage())
                .log();

        return switch (ex) {
            case EntityNotFoundException enfe -> new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "The requested resource was not found",
                    enfe
            );
            case EmptyResultDataAccessException erdae -> new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "No data found for the given criteria",
                    erdae
            );
            case DuplicateKeyException dke -> new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "Duplicate entry found: " + extractConstraintName(dke),
                    dke
            );
            case DataIntegrityViolationException dive -> new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "Data integrity violation: " + extractConstraintMessage(dive),
                    dive
            );
            case DataAccessException dae -> new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "Database operation failed: " + dae.getMostSpecificCause().getMessage(),
                    dae
            );
            case PersistenceException pe -> new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "Persistence error: " + pe.getMessage(),
                    pe
            );
            case SQLException sqle -> new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "SQL error [" + sqle.getSQLState() + "]: " + sqle.getMessage(),
                    sqle
            );
            default -> new BusinessException(
                    ErrorCode.DATABASE_ERROR,
                    "Unexpected database error: " + ex.getMessage(),
                    ex
            );
        };
    }

    private String extractConstraintName(DuplicateKeyException ex) {
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("constraint")) {
                int start = message.indexOf("constraint") + 10;
                int end = message.indexOf(" ", start);
                if (end == -1) end = message.length();
                return message.substring(start, end).trim();
            }
        }
        return "unknown constraint";
    }

    private String extractConstraintMessage(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof SQLException sqlEx) {
            String sqlState = sqlEx.getSQLState();
            return switch (sqlState) {
                case "23000" -> "Integrity constraint violation";
                case "23001" -> "Restrict violation";
                case "23502" -> "Not null violation";
                case "23503" -> "Foreign key violation";
                case "23505" -> "Unique violation";
                case "23514" -> "Check violation";
                default -> sqlEx.getMessage();
            };
        }
        return cause.getMessage();
    }
//    public RuntimeException translateException(DataAccessException ex) {
//        log.error("Database exception occurred", ex);
//
//        return switch (ex) {
//            case DataIntegrityViolationException dive ->
//                    new BusinessException(ErrorCode.VALIDATION_FAILED, "Data integrity violation: " + dive.getMessage());
//            case EmptyResultDataAccessException erdae ->
//                    new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Resource not found");
//            case ObjectOptimisticLockingFailureException oolfe ->
//                    new BusinessException(ErrorCode.VALIDATION_FAILED, "Resource was modified by another user");
//            default -> new BusinessException(ErrorCode.DATABASE_ERROR, "Database operation failed: " + ex.getMessage());
//        };
//    }
//
//    public RuntimeException translateException(SQLException ex) {
//        log.error("SQL exception occurred", ex);
//
//        return switch (ex.getSQLState()) {
//            case "23000", "23001", "23502", "23503", "23505", "23514" ->
//                    new BusinessException(ErrorCode.VALIDATION_FAILED, "Data constraint violation");
//            case "42000", "42S02", "42S22" ->
//                    new BusinessException(ErrorCode.DATABASE_ERROR, "SQL syntax or schema error");
//            default -> new BusinessException(ErrorCode.DATABASE_ERROR, "Database error: " + ex.getMessage());
//        };
//    }
}
