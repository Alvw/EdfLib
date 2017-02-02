package com.biorecorder.edflib;

/**
 * Signals that during parsing of EDF/BDF file header some sort of error has occurred
 * and appropriate data can not be extracted from the header
 */
public class HeaderParsingException extends Exception {
    public HeaderParsingException() {
        super();
    }

    public HeaderParsingException(String message) {
        super(message);
    }

    public HeaderParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeaderParsingException(Throwable cause) {
        super(cause);
    }
}
