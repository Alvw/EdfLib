package com.biorecorder.edflib.exceptions;

/**
 * Signals that some sort of exception has occurred during file writing
 */
public class FileWritingRuntimeException extends  IORuntimeException {
    public FileWritingRuntimeException(String message) {
        super(message);
    }

    public FileWritingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileWritingRuntimeException(Throwable cause) {
        super(cause);
    }
}
