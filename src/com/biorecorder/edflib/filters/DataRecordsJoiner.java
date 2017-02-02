package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DataRecordsWriter;
import com.biorecorder.edflib.base.HeaderConfig;

import java.io.IOException;

/**
 * Permits to join (piece together) given number of incoming DataRecords.
 * Resultant output DataRecords (that will be written to underlying DataRecordsWriter)
 * have the following structure:
 * <p>
 * [ resultant number of samples from channel_1 ,
 * <br>  resultant number of samples from channel_2,
 * <br>  ...
 * <br>  resultant number of samples from channel_i ]
 * <p>
 * Where:
 * <br>resultant number of samples from channel_i  =  number of samples from channel_i in original DataRecord * numberOfRecordsToJoin
 * <br>duration of resulting DataRecord = duration of original DataRecord * numberOfRecordsToJoin
 */
public class DataRecordsJoiner extends DataRecordsFilter {
    private int numberOfRecordsToJoin;
    private int recordsCounter;
    private int[] outDataRecord;

    /**
     * Creates a new DataRecordsJoiner to join given numbers of incoming DataRecords and write the resultant
     * DataRecords to the specified underlying DataRecordsWriter
     *
     * @param numberOfRecordsToJoin number of DataRecords to join in one resultant DataRecord
     * @param out                   the underlying DataRecords writer where resultant DataRecords are written
     */
    public DataRecordsJoiner(int numberOfRecordsToJoin, DataRecordsWriter out) {
        super(out);
        this.numberOfRecordsToJoin = numberOfRecordsToJoin;
    }

    @Override
    protected HeaderConfig createOutputRecordingConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig); // copy header config
        outHeaderConfig.setDurationOfDataRecord(headerConfig.getDurationOfDataRecord() * numberOfRecordsToJoin);
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            outHeaderConfig.getSignalConfig(i).setNumberOfSamplesInEachDataRecord(headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord() * numberOfRecordsToJoin);
        }
        return outHeaderConfig;
    }

    /**
     * Accumulate and join the specified number of incoming DataRecords into one resultant
     * DataRecord and when it is ready write it to the underlying DataRecordsWriter
     *
     * @param digitalData array with digital data
     * @param offset      offset within the array at which the DataRecord starts
     * @throws IOException
     */

    @Override
    public void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        if (recordsCounter == 0) {
            outDataRecord = new int[headerConfig.getRecordLength() * numberOfRecordsToJoin];
        }
        recordsCounter++;
        int signalPosition = 0;
        int outSignalPosition;
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            int numberOfSignalSamples = headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
            outSignalPosition = signalPosition * numberOfRecordsToJoin + (recordsCounter - 1) * numberOfSignalSamples;
            System.arraycopy(digitalData, offset + signalPosition, outDataRecord, outSignalPosition, numberOfSignalSamples);
            signalPosition += numberOfSignalSamples;

        }

        if (recordsCounter == numberOfRecordsToJoin) {  // when resulting data record is ready
            out.writeDigitalDataRecord(outDataRecord);
            recordsCounter = 0;
        }
    }

}
