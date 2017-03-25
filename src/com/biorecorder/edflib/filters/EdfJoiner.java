package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.EdfWriter;
import com.biorecorder.edflib.HeaderConfig;

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
public class EdfJoiner extends EdfFilter {
    private int numberOfRecordsToJoin;
    private int[] outDataRecord;

    /**
     * Creates a new EdfJoiner to join given numbers of incoming DataRecords and write the resultant
     * DataRecords to the specified underlying DataRecordsWriter
     *
     * @param numberOfRecordsToJoin number of DataRecords to join in one resultant DataRecord
     * @param out                   the underlying DataRecords writer where resultant DataRecords are written
     */
    public EdfJoiner(int numberOfRecordsToJoin, EdfWriter out) {
        super(out);
        this.numberOfRecordsToJoin = numberOfRecordsToJoin;
    }

    @Override
    protected HeaderConfig createOutputRecordingConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig); // copy header config
        outHeaderConfig.setDurationOfDataRecord(headerConfig.getDurationOfDataRecord() * numberOfRecordsToJoin);
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            outHeaderConfig.setNumberOfSamplesInEachDataRecord(i, headerConfig.getNumberOfSamplesInEachDataRecord(i) * numberOfRecordsToJoin);
        }
        return outHeaderConfig;
    }

    @Override
    public void open(HeaderConfig headerConfig) throws IOException {
        super.open(headerConfig);
        outDataRecord = new int[headerConfig.getRecordLength() * numberOfRecordsToJoin];
    }

    /**
     * Accumulate and join the specified number of incoming DataRecords into one resultant
     * DataRecord and when it is ready write it to the underlying DataRecordsWriter
     *
     * @param digitalSamples array with digital data
     * @throws IOException
     */
    @Override
    public void writeDigitalSamples(int[] digitalSamples) throws IOException {
        for (int sample : digitalSamples) {
            int samplePosition = (int) (sampleCounter % headerConfig.getRecordLength());
            int joinedRecords = countRecords() % numberOfRecordsToJoin;
            int counter = 0;
            int channelNumber = 0;
            while (samplePosition > counter + headerConfig.getNumberOfSamplesInEachDataRecord(channelNumber)) {
                counter += headerConfig.getNumberOfSamplesInEachDataRecord(channelNumber);
                channelNumber++;
            }
            int outSamplePosition = counter * numberOfRecordsToJoin;
            outSamplePosition += joinedRecords * headerConfig.getNumberOfSamplesInEachDataRecord(channelNumber);
            outSamplePosition += samplePosition - counter;
            outDataRecord[outSamplePosition] = sample;
            sampleCounter ++;

            if(sampleCounter % headerConfig.getRecordLength() == 0 &&  countRecords()%numberOfRecordsToJoin == 0) {
                out.writeDigitalSamples(outDataRecord);
            }
        }
    }
}
