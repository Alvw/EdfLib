package com.biorecorder.edflib;

import java.io.IOException;

/**
 * Permits to join (piece together) given number of incoming DataRecords.
 * Resultant output DataRecords (that will be written to underlying EdfWriter)
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
     * DataRecords to the specified underlying EdfWriter
     *
     * @param numberOfRecordsToJoin number of DataRecords to join in one resultant DataRecord
     * @param out                   the underlying EdfWriter where resultant DataRecords will be written
     */
    public EdfJoiner(int numberOfRecordsToJoin, EdfWriter out) {
        super(out);
        this.numberOfRecordsToJoin = numberOfRecordsToJoin;
    }

    @Override
    protected HeaderInfo createOutputRecordingConfig() {
        HeaderInfo outHeaderInfo = new HeaderInfo(headerInfo); // copy header config
        outHeaderInfo.setDurationOfDataRecord(headerInfo.getDurationOfDataRecord() * numberOfRecordsToJoin);
        for (int i = 0; i < headerInfo.getNumberOfSignals(); i++) {
            outHeaderInfo.setNumberOfSamplesInEachDataRecord(i, headerInfo.getNumberOfSamplesInEachDataRecord(i) * numberOfRecordsToJoin);
        }
        return outHeaderInfo;
    }

    @Override
    public void setHeader(HeaderInfo headerInfo) throws IOException {
        super.setHeader(headerInfo);
        outDataRecord = new int[headerInfo.getDataRecordLength() * numberOfRecordsToJoin];
    }

    /**
     * Accumulate and join the specified number of incoming DataRecords into one resultant
     * DataRecord and when it is ready write it to the underlying EdfWriter
     * <p>
     * Call this method for every signal (channel) of the stream/file. The order is important!
     * When there are 4 signals,  the order of calling this method must be:
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br> ... etc.
     * <p>
     * Number of samples for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     * The entire DataRecord (data pack) containing digital data from all signals also can be placed in one array
     * and written at once.
     *
     * @param digitalSamples array with digital data samples
     * @throws IOException
     */
    @Override
    public void writeDigitalSamples(int[] digitalSamples) throws IOException {
        for (int sample : digitalSamples) {
            int samplePosition = (int) (sampleCounter % headerInfo.getDataRecordLength());
            int joinedRecords = getNumberOfWrittenDataRecords() % numberOfRecordsToJoin;
            int counter = 0;
            int channelNumber = 0;
            while (samplePosition >= counter + headerInfo.getNumberOfSamplesInEachDataRecord(channelNumber)) {
                counter += headerInfo.getNumberOfSamplesInEachDataRecord(channelNumber);
                 channelNumber++;
            }

            int outSamplePosition = counter * numberOfRecordsToJoin;
            outSamplePosition += joinedRecords * headerInfo.getNumberOfSamplesInEachDataRecord(channelNumber);
            outSamplePosition += samplePosition - counter;

            outDataRecord[outSamplePosition] = sample;
            sampleCounter ++;

            if(sampleCounter % headerInfo.getDataRecordLength() == 0 &&  getNumberOfWrittenDataRecords()%numberOfRecordsToJoin == 0) {
                out.writeDigitalSamples(outDataRecord);
            }
        }
    }
}
