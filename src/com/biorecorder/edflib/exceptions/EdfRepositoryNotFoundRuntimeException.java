package com.biorecorder.edflib.exceptions;

/**
 * If Edf file or any other type of repository/storage
 * can not be found, created or accessible for some reasons
 */
public class EdfRepositoryNotFoundRuntimeException extends EdfRuntimeException {
    public EdfRepositoryNotFoundRuntimeException(String message) {
        super(message);
    }

    public EdfRepositoryNotFoundRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EdfRepositoryNotFoundRuntimeException(Throwable cause) {
        super(cause);
    }
}
