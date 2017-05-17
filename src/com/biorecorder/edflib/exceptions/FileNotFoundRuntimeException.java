package com.biorecorder.edflib.exceptions;

/**
 * Runtime wrapper for FileNotFoundException
 */
public class FileNotFoundRuntimeException extends IORuntimeException {
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
