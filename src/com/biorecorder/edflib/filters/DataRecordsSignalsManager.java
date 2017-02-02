package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DataRecordsWriter;
import com.biorecorder.edflib.base.HeaderConfig;
import com.biorecorder.edflib.base.SignalConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Permit to omit samples from some channels (delete signal) or realize some kind of
 * transformation with the signal data (add some filter to the signal).
 * <p>
 * EdfExample:
 * <pre>{@code
 *  DataRecordsSignalsManager signalsManager = new DataRecordsSignalsManager(new EdfWriter("filename", FileType.BDF_24BIT));
 *  signalsManager.removeSignal(0);
 *  signalsManager.removeSignal(2);
 *  signalsManager.addSignalPrefiltering(1, new SignalMovingAverageFilter(10));
 * }
 * </pre>
 *
 * @see SignalFilter
 * @see SignalMovingAverageFilter
 */
public class DataRecordsSignalsManager extends DataRecordsFilter {
    private Map<Integer, SignalFilter> filters = new HashMap<Integer, SignalFilter>();
    private List<Boolean> signalsMask = new ArrayList<Boolean>();

    public DataRecordsSignalsManager(DataRecordsWriter out) {
        super(out);
    }

    /**
     * Indicate that the samples from given signal should be omitted in resultant DataRecord
     *
     * @param signalNumber number of channel (signal) in the original (incoming) DataRecord
     *                     which samples should be omitted
     */
    public void removeSignal(int signalNumber) {
        for (int i = signalsMask.size(); i <= signalNumber; i++) {
            signalsMask.add(true);
        }
        signalsMask.set(signalNumber, false);
    }


    /**
     * Indicate that the given filter should be applied to the samples
     * of given signal in DataRecords
     *
     * @param signalFilter signal filter that will be applied to the samples of given channel number
     * @param signalNumber number of channel (signal) in the input DataRecord
     *                     the filter should be applied to
     */
    // TODO add the possibility to apply not one but several filters to the same channel
    public void addSignalPrefiltering(int signalNumber, SignalFilter signalFilter) {
        filters.put(signalNumber, signalFilter);
    }


    protected HeaderConfig createOutputRecordingConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig);
        outHeaderConfig.removeAllSignalConfigs();
        for (int i = signalsMask.size(); i < headerConfig.getNumberOfSignals(); i++) {
            signalsMask.add(true);
        }
        for (int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            if (signalsMask.get(signalNumber)) {
                SignalConfig signalConfig = new SignalConfig(headerConfig.getSignalConfig(signalNumber));
                SignalFilter signalFilter = filters.get(signalNumber);
                if (signalFilter != null) {
                    String prefiltering = signalConfig.getPrefiltering();
                    if(prefiltering == null || prefiltering.isEmpty()) {
                        prefiltering = signalFilter.getFilterName();
                    }
                    else {
                        prefiltering = prefiltering + "; "+ signalFilter.getFilterName();
                    }
                    signalConfig.setPrefiltering(prefiltering);

                }
                outHeaderConfig.addSignalConfig(signalConfig);
            }

        }
        return outHeaderConfig;

    }

    /**
     * Apply filters specified for the channels, omit data from the "deleted" channels and
     * create resultant output DataRecord
     *
     * @param digitalData
     * @param offset
     * @return
     */
    private int[] filterDataRecord(int[] digitalData, int offset) {
        int[] filteredDataRecord = new int[createOutputRecordingConfig().getRecordLength()];
        int signalPosition = 0;
        int filteredSignalPosition = 0;
        for (int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            int numberOfSamples = headerConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord();
            if (signalsMask.get(signalNumber)) {
                SignalFilter signalFilter = filters.get(signalNumber);
                if (signalFilter != null) {
                    for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
                        filteredDataRecord[filteredSignalPosition + sampleNumber] = signalFilter.getFilteredValue(digitalData[offset + signalPosition + sampleNumber]);
                    }
                } else {
                    System.arraycopy(digitalData, offset + signalPosition, filteredDataRecord, filteredSignalPosition, numberOfSamples);
                }
                filteredSignalPosition += numberOfSamples;
            }
            signalPosition += numberOfSamples;

        }
        return filteredDataRecord;
    }


    @Override
    public void open(HeaderConfig headerConfig) throws IOException {
        super.open(headerConfig);
    }

    /**
     * Fulfills all indicated transformation with the signals samples and write
     * resultant DataRecord to the underlying DataRecordsWriter
     *
     * @param digitalData - array with digital data
     * @param offset      - offset within the array at which the DataRecord starts
     * @throws IOException
     */
    @Override
    public void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        out.writeDigitalDataRecord(filterDataRecord(digitalData, offset));
    }

}
