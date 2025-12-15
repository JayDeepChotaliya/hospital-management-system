package com.doctor_service.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String errorCode;
    private String path;
    private LocalDateTime timestamp;
}
