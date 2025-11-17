package com.hackathon.hackathon.agent2.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {
    public ConflictException(String errorCode, String userMessage) {
        super(HttpStatus.CONFLICT, errorCode, userMessage);
    }
    public ConflictException(String errorCode, String userMessage, Throwable cause) {
        super(HttpStatus.CONFLICT, errorCode, userMessage, cause);
    }
}
