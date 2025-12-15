package com.hms.auth.exception;

import java.time.Instant;

public class ErrorResponse {

    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String errorCode;
    private String path;
    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String errorCode, String path, String message) {
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.path = path;
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
