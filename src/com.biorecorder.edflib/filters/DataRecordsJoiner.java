package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.HeaderConfig;
import java.io.IOException;

/**
 * Created by gala on 25/12/16.
 */
public class DataRecordsJoiner extends DataRecordsFilter {
    private int numberOfRecordsToJoin;
    private int recordsCounter;
    private int[] resultingDataRecord;

    public DataRecordsJoiner(int numberOfRecordsToJoin, DataRecordsWriter out) {
        super(out);
        this.numberOfRecordsToJoin = numberOfRecordsToJoin;
    }

    @Override
    protected HeaderConfig createOutHeaderConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig); // copy header config
        outHeaderConfig.setDurationOfDataRecord(headerConfig.getDurationOfDataRecord() * numberOfRecordsToJoin);
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            outHeaderConfig.getSignalConfig(i).setNumberOfSamplesInEachDataRecord(headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord() * numberOfRecordsToJoin);
        }
        return outHeaderConfig;
    }


    @Override
    protected void write(int[] data) throws IOException {
        int numberOfDataRecords = data.length / headerConfig.getRecordLength();
        for(int i = 0; i < numberOfDataRecords; i++) {
            addDataRecord(data, i * headerConfig.getRecordLength());
        }

    }

    private void addDataRecord(int[] data, int offset) throws IOException {
        if(recordsCounter == 0) {
            resultingDataRecord = new int[headerConfig.getRecordLength() * numberOfRecordsToJoin];
        }
        recordsCounter++;
        int signalPosition = 0;
        int resultingSignalPosition;
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            int numberOfSignalSamples = headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
            resultingSignalPosition = signalPosition * numberOfRecordsToJoin + (recordsCounter - 1) * numberOfSignalSamples;
            System.arraycopy(data, offset + signalPosition, resultingDataRecord, resultingSignalPosition, numberOfSignalSamples);
            signalPosition += numberOfSignalSamples;

        }

        if (recordsCounter == numberOfRecordsToJoin) {  // when resulting data record is ready
            out.writeDigitalDataRecords(resultingDataRecord);
            recordsCounter = 0;
        }
    }

}
