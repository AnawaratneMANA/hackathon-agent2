package com.hackathon.hackathon.agent2.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Primary handler for domain exceptions (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleAppException(AppException ex, WebRequest req) {
        HttpStatus status = ex.getStatus();
        ApiError err = new ApiError(status.value(), status.getReasonPhrase(),
                ex.getErrorCode(), ex.getUserMessage(), extractPath(req));

        // Log the cause for debugging (don't expose raw cause to client)
        if (ex.getCause() != null) {
            log.debug("AppException cause: {}", ex.getCause().getMessage(), ex.getCause());
            // optionally add sanitized message
            Map<String, Object> details = Map.of("info", "See server logs for cause id");
            err.setDetails(details);
        }

        if (status.is5xxServerError()) {
            log.error("AppException: {} - {}", ex.getErrorCode(), ex.getUserMessage(), ex);
        } else {
            log.info("AppException: {} - {}", ex.getErrorCode(), ex.getUserMessage());
        }

        return new ResponseEntity<>(err, status);
    }

    // Validation errors - request body (Bean validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (a, b) -> a + "; " + b
                ));

        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "VALIDATION_FAILED", "Request validation failed", extractPath(req));
        err.setDetails(Map.of("fields", fieldErrors));
        log.info("Validation failed: {}", fieldErrors);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // Malformed JSON / unreadable message
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "MALFORMED_JSON", "Malformed JSON request", extractPath(req));
        log.warn("Malformed JSON: {}", ex.getMessage());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // Missing request params
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "MISSING_PARAMETER", ex.getMessage(), extractPath(req));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // Method not allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method Not Allowed",
                "METHOD_NOT_ALLOWED", ex.getMessage(), extractPath(req));
        return new ResponseEntity<>(err, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // Authorization / access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.FORBIDDEN.value(), "Forbidden",
                "ACCESS_DENIED", "Access denied", extractPath(req));
        log.warn("Access denied: {}", ex.getMessage());
        return new ResponseEntity<>(err, HttpStatus.FORBIDDEN);
    }

    // Map generic NoSuchElementException -> Not Found (legacy)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(NoSuchElementException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found",
                "ITEM_NOT_FOUND", ex.getMessage(), extractPath(req));
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    // Data access exception -> service unavailable (DB issues)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable",
                "DATABASE_ERROR", "Database error, try again later", extractPath(req));
        log.error("Database error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(err, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest req) {
        // Log stacktrace with correlation id if present
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "INTERNAL_ERROR", "An unexpected error occurred", extractPath(req));

        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // helper: convert WebRequest description to path
    private String extractPath(WebRequest req) {
        return req.getDescription(false).replace("uri=", "");
    }
}
