package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.RecordConfig;
import com.biorecorder.edflib.SignalConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This filter permits to omit samples from the specified channels (signals).
 *
 *<p>Example:
 *<pre>{@code
 *    SignalsRemoval dataRecordsWriter = new SignalsRemoval(new EdfWriter("filename"));
 *    dataRecordsWriter.removeSignal(0);
 *    dataRecordsWriter.removeSignal(2);
 * }
 * </pre>
 *
 */
public class SignalsRemoval extends DataRecordsFilter {
    private List<Boolean> signalsMask = new ArrayList<Boolean>();

    public SignalsRemoval(DataRecordsWriter out) {
        super(out);
    }

    /**
     * indicate that the samples from given signal should be omitted in resultant DataRecord
     *
     * @param signalNumber - number of channel (signal) in the original (incoming) DataRecord
     *                     which samples should be omitted
     */
    public void removeSignal(int signalNumber) {
        for(int i = signalsMask.size(); i <= signalNumber; i++) {
            signalsMask.add(true);
        }
        signalsMask.set(signalNumber, false);
    }

    @Override
    public void open(RecordConfig recordConfig) throws IOException {
        super.open(recordConfig);
        for(int i = signalsMask.size(); i < recordConfig.getNumberOfSignals(); i++) {
            signalsMask.add(true);
        }
    }

    @Override
    protected RecordConfig createOutDataRecordConfig() {
        RecordConfig outRecordConfig = new RecordConfig(recordConfig);
        outRecordConfig.removeAllSignalConfig();
        for (int i = 0; i < recordConfig.getNumberOfSignals(); i++) {
            if(signalsMask.get(i)) {
                outRecordConfig.addSignalConfig(new SignalConfig(recordConfig.getSignalConfig(i)));
            }
        }
        return outRecordConfig;
    }

    /**
     * create resultant DataRecord without samples from the specified channels
     * and write it to the underlying DataRecordsWriter
     *
     * @param digitalData - array with digital data
     * @param offset - offset within the array at which the DataRecord starts
     *
     * @throws IOException
     */

    @Override
    public void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        int[] outDataRecord = new int[createOutDataRecordConfig().getRecordLength()];
        int signalPosition = 0;
        int outSignalPosition = 0;
        for (int i = 0; i <  recordConfig.getNumberOfSignals(); i++) {
            if(signalsMask.get(i)) {
                System.arraycopy(digitalData, offset + signalPosition, outDataRecord, outSignalPosition,
                        recordConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord());
                outSignalPosition += recordConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
            }
            signalPosition += recordConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
        }
        out.writeDigitalDataRecord(outDataRecord);
    }
}
