package com.hackathon.hackathon.agent2.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Primary handler for domain exceptions
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiError> handleAppException(AppException ex, WebRequest req) {
        HttpStatus status = ex.getStatus();
        ApiError err = new ApiError(status.value(), status.getReasonPhrase(),
                ex.getErrorCode(), ex.getUserMessage(), req.getDescription(false).replace("uri=", ""));

        // You might include some structured details for some errors:
        // For example, EXTERNAL_SERVICE failures include cause message
        if (ex.getCause() != null) {
            err.setDetails(Map.of("cause", ex.getCause().getMessage()));
        }
        // Log according to severity (for demo use printStackTrace for non-4xx)
        if (status.is5xxServerError()) {
            // use logger in real project
            log.error(ex.getMessage(), ex);
        }
        return new ResponseEntity<>(err, status);
    }

    // Validation errors (Bean validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiError err = new ApiError(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "VALIDATION_FAILED", details, req.getDescription(false)
                .replace("uri=", ""));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // Common legacy mapping (optional): map NoSuchElementException -> NotFound
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(NoSuchElementException ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.NOT_FOUND.value(), "Not Found",
                "ITEM_NOT_FOUND", ex.getMessage(), req.getDescription(false)
                .replace("uri=", ""));
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    // Catch-all fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest req) {
        // Log stacktrace
        ex.printStackTrace();

        ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "INTERNAL_ERROR", "An unexpected error occurred", req.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
