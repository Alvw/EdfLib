package com.biorecorder.edflib.util;

/**
 * Created by gala on 02/01/17.
 */
public class HeaderParsingException extends Exception {
    public HeaderParsingException() { super(); }
    public HeaderParsingException(String message) { super(message); }
    public HeaderParsingException(String message, Throwable cause) { super(message, cause); }
    public HeaderParsingException(Throwable cause) { super(cause); }
}
