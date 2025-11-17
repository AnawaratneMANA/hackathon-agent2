package com.hackathon.hackathon.agent2.exception;

import org.springframework.http.HttpStatus;

/**
 * Base application exception for domain errors.
 * Always unchecked (extends RuntimeException) so we don't force try/catch in services.
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;     // machine friendly code (e.g., ITEM_NOT_FOUND)
    private final String userMessage;   // optional human-friendly message for UI

    public AppException(HttpStatus status, String errorCode, String userMessage) {
        super(userMessage);
        this.status = status;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public AppException(HttpStatus status, String errorCode, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
    public String getUserMessage() { return userMessage; }
}

