package com.biorecorder.edflib.filters;


import java.util.ArrayList;
import java.util.List;

/**
 * The simplest realisation of Moving Average filter that buffers given number (n) of incoming
 * samples and returns its average value
 * <p>
 * filteredValue_m = ( value_m + value_(m-1) + ... + value_(m-n) ) / n
 */

public class MovingAverageFilter implements SignalFilter {

    private List<Double> buffer = new ArrayList<>();
    private int bufferSize;

    /**
     * Create MovingAverageFilter that will buffer the given number of averaging points
     *
     * @param numberOfAveragingPoints the number of last input samples that will be buffered
     *                                to calculate the average value
     */
    public MovingAverageFilter(int numberOfAveragingPoints) {
        this.bufferSize = numberOfAveragingPoints;
    }

    /**
     * Calculate the average value of last n samples (buffered), where n = numberOfAveragingPoints
     * was specified in the constructor
     *
     * @param value input value
     * @return average value of last n samples
     */
    @Override
    public double getFilteredValue(double value) {
        buffer.add(value);
        if (buffer.size() < bufferSize) {
            return value;
        }
        if (buffer.size() == bufferSize + 1) {
            buffer.remove(0);
        }
        long bufferSum = 0;
        for (int i = 0; i < bufferSize; i++) {
            bufferSum += buffer.get(i);
        }
        return  bufferSum / bufferSize;
    }


    @Override
    public String getName() {
        return "MovAvg:" + bufferSize;
    }
}
