package com.biorecorder.edflib.filters;

/**
 * Stream signal filter
 */
interface SignalFilter {
    int getFilteredValue(int value);
}
