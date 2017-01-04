package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.filters.signal_filters.SignalFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gala on 29/12/16.
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
    protected void writeOneDataRecord(int[] data, int offset) throws IOException {
        int[] filteredDataRecord = new int[headerConfig.getRecordLength()];
        int signalPosition = 0;
        for (int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            int numberOfSamples = headerConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord();
            SignalFilter signalFilter = filters.get(signalNumber);
            if(signalFilter != null) {
                for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
                    filteredDataRecord[signalPosition + sampleNumber] = signalFilter.getFilteredValue(data[offset + signalPosition + sampleNumber]);
                }
            } else {
                System.arraycopy(data, offset + signalPosition, filteredDataRecord, signalPosition, numberOfSamples);
            }
            signalPosition += numberOfSamples;
        }
        out.writeDigitalDataRecord(filteredDataRecord);
    }

}
