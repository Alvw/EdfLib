package com.biorecorder.edflib.exceptions;

/**
 * This exception is a <b>Runtime Exception!</b> and signals that during parsing of EDF/BDF file header some sort of error has occurred
 * and appropriate data can not be extracted from the header.
 * <p>
 * It has multiple types. Idea is that appropriate
 * exception message for the user could be generated on any app level only on the base
 * of exception type and some additional info from the exception itself (value, expectedValue, signalNumber, range).
 * <p>
 * Message string of the exception serves only for developers, logging and debugging.
 * It should not be shown to final users
 * <p>
 * Идея заключается в том чтобы в идеала соответсвующее сообщение об ошибке
 * могло генериться на любом уровне лишь на основании типа исключения и содержащихся
 * в исключении параметров. Message string служит лишь информацией для разработчиков и не должен
 * выводиться клиенты
 *
 * @see HeaderExceptionType
 */
public class EdfHeaderParsingRuntimeException extends IORuntimeException {
    HeaderExceptionType exceptionType;
    String value;
    String expectedValue;
    Double min;
    Double max;
    int signalNumber = -1;


    public EdfHeaderParsingRuntimeException(HeaderExceptionType exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }
    public EdfHeaderParsingRuntimeException(HeaderExceptionType exceptionType, String message, Throwable cause) {
        super(message, cause);
        this.exceptionType = exceptionType;
    }

    public HeaderExceptionType getExceptionType() {
        return exceptionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public int getSignalNumber() {
        return signalNumber;
    }

    public void setSignalNumber(int signalNumber) {
        this.signalNumber = signalNumber;
    }

    public double getMin() {return min;}

    public double getMax() {return max;}

    public void setRange(double min, double max) {
        this.min = min;
        this.max = max;
    }
}
