package com.thehub.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

public class ApiError {

    private int status;
    private String message;
    private String detail;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Map<String, String> errors;

    public ApiError() {}

    public ApiError(int status, String message, String detail) {
        this.status = status;
        this.message = message;
        this.detail = detail != null ? detail : message;
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.detail = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
}
