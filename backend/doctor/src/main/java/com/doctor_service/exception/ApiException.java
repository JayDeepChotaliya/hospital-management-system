package com.doctor_service.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException{
    private final String errorCode;

    public ApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiException(String message)
    {
        this(message, null);
    }

}
