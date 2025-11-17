package com.hackathon.hackathon.agent2.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {
    public NotFoundException(String errorCode, String userMessage) {
        super(HttpStatus.NOT_FOUND, errorCode, userMessage);
    }
    public NotFoundException(String errorCode, String userMessage, Throwable cause) {
        super(HttpStatus.NOT_FOUND, errorCode, userMessage, cause);
    }
}
