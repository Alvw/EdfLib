package com.biorecorder.edflib.filters;


import java.util.ArrayList;
import java.util.List;

public class SignalAveragingFilter implements SignalFilter{

    private List<Integer> buffer = new ArrayList<Integer>();
    private int bufferSize;

    public SignalAveragingFilter(int bufferSize) {
            this.bufferSize = bufferSize;
    }

    @Override
    public int getFilteredValue(int value) {
        buffer.add(value);
        if (buffer.size() < bufferSize) {
                 return  0;
        }
        if (buffer.size() == bufferSize + 1) {
            buffer.remove(0);
        }
        long bufferSum = 0;
        for (int i = 0; i < bufferSize; i++) {
            bufferSum += buffer.get(i);
        }
        return  (int)(bufferSum/bufferSize);
    }
}
