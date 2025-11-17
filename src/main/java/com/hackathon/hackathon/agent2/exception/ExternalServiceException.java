package com.hackathon.hackathon.agent2.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {
    public ExternalServiceException(String errorCode, String userMessage) {
        super(HttpStatus.SERVICE_UNAVAILABLE, errorCode, userMessage);
    }
    public ExternalServiceException(String errorCode, String userMessage, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, errorCode, userMessage, cause);
    }
}
