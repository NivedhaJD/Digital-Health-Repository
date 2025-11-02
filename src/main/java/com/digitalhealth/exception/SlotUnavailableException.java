package com.digitalhealth.exception;

/**
 * Exception thrown when an appointment slot is not available.
 */
public class SlotUnavailableException extends Exception {
    public SlotUnavailableException(String message) {
        super(message);
    }
}
