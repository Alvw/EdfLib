package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DefaultRecordingInfo;
import com.biorecorder.edflib.base.RecordingInfo;
import com.biorecorder.edflib.base.EdfWriter;

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
    protected RecordingInfo createOutputConfig() {
        DefaultRecordingInfo outConfig = new DefaultRecordingInfo(recordingInfo); // copy header recordingInfo
        outConfig.setDurationOfDataRecord(recordingInfo.getDurationOfDataRecord() * numberOfRecordsToJoin);
        for (int i = 0; i < recordingInfo.getNumberOfSignals(); i++) {
            outConfig.setNumberOfSamplesInEachDataRecord(i, recordingInfo.getNumberOfSamplesInEachDataRecord(i) * numberOfRecordsToJoin);
        }
        return outConfig;
    }

    @Override
    public void setRecordingInfo(RecordingInfo recordingInfo) {
        super.setRecordingInfo(recordingInfo);
        outDataRecord = new int[recordingInfo.getDataRecordLength() * numberOfRecordsToJoin];
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
     */
    @Override
    public void writeDigitalSamples(int[] digitalSamples)  {
        for (int sample : digitalSamples) {
            int samplePosition = (int) (sampleCounter % recordingInfo.getDataRecordLength());
            int joinedRecords = getNumberOfReceivedDataRecords() % numberOfRecordsToJoin;
            int counter = 0;
            int channelNumber = 0;
            while (samplePosition >= counter + recordingInfo.getNumberOfSamplesInEachDataRecord(channelNumber)) {
                counter += recordingInfo.getNumberOfSamplesInEachDataRecord(channelNumber);
                 channelNumber++;
            }

            int outSamplePosition = counter * numberOfRecordsToJoin;
            outSamplePosition += joinedRecords * recordingInfo.getNumberOfSamplesInEachDataRecord(channelNumber);
            outSamplePosition += samplePosition - counter;

            outDataRecord[outSamplePosition] = sample;
            sampleCounter ++;

            if(sampleCounter % recordingInfo.getDataRecordLength() == 0 &&  getNumberOfReceivedDataRecords()%numberOfRecordsToJoin == 0) {
                out.writeDigitalSamples(outDataRecord);
            }
        }
    }
}
