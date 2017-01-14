package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.filters.signal_filters.SignalFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AggregateFilter extends DataRecordsFilter {
    private Map<Integer, SignalFilter> filters = new HashMap<Integer, SignalFilter>();

    public AggregateFilter(DataRecordsWriter out) {
        super(out);
    }

    public void addSignalFilter(int signalNumber, SignalFilter signalFilter) {
        filters.put(signalNumber, signalFilter);
    }

    @Override
    public void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        int[] filteredDataRecord = new int[recordConfig.getRecordLength()];
        int signalPosition = 0;
        for (int signalNumber = 0; signalNumber < recordConfig.getNumberOfSignals(); signalNumber++) {
            int numberOfSamples = recordConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord();
            SignalFilter signalFilter = filters.get(signalNumber);
            if(signalFilter != null) {
                for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
                    filteredDataRecord[signalPosition + sampleNumber] = signalFilter.getFilteredValue(digitalData[offset + signalPosition + sampleNumber]);
                }
            } else {
                System.arraycopy(digitalData, offset + signalPosition, filteredDataRecord, signalPosition, numberOfSamples);
            }
            signalPosition += numberOfSamples;
        }
        out.writeDigitalDataRecord(filteredDataRecord);
    }

}
