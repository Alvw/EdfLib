package com.biorecorder.edflib.exceptions;

/**
 * Signals that an exception of some sort has occurred. Base class for all exceptions
 * happening in EdfLib
 */
public class EdfRuntimeException extends RuntimeException {
    public EdfRuntimeException(String message) {
        super(message);
    }

    public EdfRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EdfRuntimeException(Throwable cause) {
        super(cause);
    }
}
