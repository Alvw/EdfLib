package com.biorecorder.edflib.exceptions;

/**
 * If Edf file or any other type of repository/storage
 * can not be found, created or accessible for some reasons
 */
public class FileNotFoundRuntimeException extends EdfRuntimeException {
    public FileNotFoundRuntimeException(String message) {
        super(message);
    }

    public FileNotFoundRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundRuntimeException(Throwable cause) {
        super(cause);
    }
}
