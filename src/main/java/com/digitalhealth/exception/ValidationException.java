package com.digitalhealth.exception;

/**
 * Exception thrown for validation errors (invalid input data).
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
