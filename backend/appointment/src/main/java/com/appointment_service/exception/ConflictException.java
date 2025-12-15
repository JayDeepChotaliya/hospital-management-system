package com.appointment_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Throw for conflict situations, e.g., double-booking.
 */
public class ConflictException extends ApiException {

    public ConflictException(String message, String errorCode) {
        super(message,HttpStatus.CONFLICT, null);
    }

    public ConflictException(String message) {
        super(message,HttpStatus.CONFLICT, null);
    }
}
