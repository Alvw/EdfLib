package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.HeaderConfig;
import com.biorecorder.edflib.SignalConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SignalsSelector extends DataRecordsFilter {
    private List<Boolean> signalsMask = new ArrayList<Boolean>();

    public SignalsSelector(DataRecordsWriter out) {
        super(out);
    }

    public void excludeSignal(int signalNumber) {
        for(int i = signalsMask.size(); i <= signalNumber; i++) {
            signalsMask.add(true);
        }
        signalsMask.set(signalNumber, false);
    }

    @Override
    public void setHeaderConfig(HeaderConfig headerConfig) {
        super.setHeaderConfig(headerConfig);
        for(int i = signalsMask.size(); i < headerConfig.getNumberOfSignals(); i++) {
            signalsMask.add(true);
        }
    }

    @Override
    protected HeaderConfig createOutHeaderConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig);
        outHeaderConfig.removeAllSignalConfig();
        for (int i = 0; i < headerConfig.getNumberOfSignals();  i++) {
            if(signalsMask.get(i)) {
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
            if(signalsMask.get(i)) {
                System.arraycopy(data, offset + signalPosition, outDataRecord, outSignalPosition,
                        headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord());
                outSignalPosition += headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
            }
            signalPosition += headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
        }
        out.writeDigitalDataRecord(outDataRecord);
    }
}
