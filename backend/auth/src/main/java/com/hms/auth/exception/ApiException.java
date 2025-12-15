package com.hms.auth.exception;

public class ApiException extends RuntimeException {

    private final String errorCode;

    public ApiException(String massage, String errorCode)
    {
        super(massage);
        this.errorCode = errorCode;
    }
    public ApiException(String massage, String errorCode, Throwable cause)
    {
            super(massage,cause);
            this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
