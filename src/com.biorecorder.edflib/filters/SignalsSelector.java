package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.HeaderConfig;
import com.biorecorder.edflib.SignalConfig;

import java.io.IOException;

/**
 *
 */
public class SignalsSelector extends DataRecordsFilter {
    private boolean[] signalsMask;

    public SignalsSelector(DataRecordsWriter out, boolean[] signalsMask) {
        super(out);
        this.signalsMask = signalsMask;
    }

    @Override
    public void setHeaderConfig(HeaderConfig headerConfig) {
        if(signalsMask.length < headerConfig.getNumberOfSignals()) {
            boolean[]  newSignalsMask = new boolean[headerConfig.getNumberOfSignals()];
            System.arraycopy(signalsMask, 0, newSignalsMask, 0, signalsMask.length);
            signalsMask = newSignalsMask;
        }
        super.setHeaderConfig(headerConfig);
    }

    @Override
    protected HeaderConfig createOutHeaderConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig);
        outHeaderConfig.removeAllSignalConfig();
        for (int i = 0; i < headerConfig.getNumberOfSignals();  i++) {
            if(signalsMask[i]) {
                outHeaderConfig.addSignalConfig(new SignalConfig(headerConfig.getSignalConfig(i)));
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
