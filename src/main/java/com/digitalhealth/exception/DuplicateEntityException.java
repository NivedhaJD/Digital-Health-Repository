package com.digitalhealth.exception;

/**
 * Exception thrown when a duplicate entity is attempted to be created.
 */
public class DuplicateEntityException extends Exception {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
