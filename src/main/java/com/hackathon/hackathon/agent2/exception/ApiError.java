package com.hackathon.hackathon.agent2.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public class ApiError {
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    private OffsetDateTime timestamp = OffsetDateTime.now();
    private Map<String, Object> details;

    public ApiError(int status, String error, String errorCode, String message, String path) {
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
