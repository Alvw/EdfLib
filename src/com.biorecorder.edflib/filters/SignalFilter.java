package com.biorecorder.edflib.filters;

/**
 * Signal filter describes some kind of transformation with stream data coming from
 * some measuring channel.
 */
public interface SignalFilter {

    /**
     * Do filter transformation with input value and return the resultant value
     *
     * @param value input value
     * @return filtered value
     */
    int getFilteredValue(int value);

    /**
     * Give the name or short description of this filter.
     *
     * @return the name of this filter
     */
    String getFilterName();
}
