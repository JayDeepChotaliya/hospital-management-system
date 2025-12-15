package com.appointment_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Throw when an entity/resource is not found.
 */
public class NotFoundException extends ApiException {



    public NotFoundException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, null);
    }
}
