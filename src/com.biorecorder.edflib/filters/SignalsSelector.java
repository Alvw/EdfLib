package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.HeaderConfig;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 */
public class SignalsSelector extends DataRecordsFilter {
    private boolean[] signalsMask;

    public SignalsSelector(DataRecordsWriter out, boolean[] signalsMask) {
        super(out);
        this.signalsMask = new boolean[headerConfig.getNumberOfSignals()];
        for(int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            this.signalsMask[i] = true;
        }
        for(int i = 0; i < Math.min(headerConfig.getNumberOfSignals(), signalsMask.length); i++) {
            this.signalsMask[i] = signalsMask[i];
        }
    }

    @Override
    protected HeaderConfig createOutHeaderConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig);
        for (int i = 0; i < headerConfig.getNumberOfSignals();  i++) {
            if(! signalsMask[i]) {
                outHeaderConfig.removeSignalConfig(i);
            }
        }
        return outHeaderConfig;
    }

    @Override
    protected void writeOneDataRecord(int[] data, int offset) throws IOException {
        int[] outDataRecord = new int[createOutHeaderConfig().getRecordLength()];
        int signalPosition = 0;
        int outSignalPosition = 0;
        for (int i = 0; i <  headerConfig.getNumberOfSignals(); i++) {
            if(signalsMask[i]) {
                System.arraycopy(data, offset + signalPosition, outDataRecord, outSignalPosition,
                        headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord());
                outSignalPosition += headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
            }
            signalPosition += headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
        }
        out.writeDigitalDataRecords(outDataRecord);
    }
}
