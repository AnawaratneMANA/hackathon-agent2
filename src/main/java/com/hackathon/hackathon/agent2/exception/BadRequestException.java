package com.hackathon.hackathon.agent2.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException {
    public BadRequestException(String errorCode, String userMessage) {
        super(HttpStatus.BAD_REQUEST, errorCode, userMessage);
    }
    public BadRequestException(String errorCode, String userMessage, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, errorCode, userMessage, cause);
    }
}
