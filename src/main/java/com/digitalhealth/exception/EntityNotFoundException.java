package com.digitalhealth.exception;

/**
 * Exception thrown when a requested entity is not found.
 */
public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
