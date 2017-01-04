package com.biorecorder.edflib.filters.signal_filters;

/**
 * Stream signal filter
 */
public interface SignalFilter {
    int getFilteredValue(int value);
}
