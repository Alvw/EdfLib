package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsFileWriter;
import com.biorecorder.edflib.RecordConfig;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Calculate the average duration of incoming DataRecords during writing process
 * and write the result to the header of the underlying EDF or BDF file writer when we call close method.
 *
 * <p>duration of DataRecords = (time of coming last DataRecord - time of coming first DataRecord) / total number of DataRecords
 *
 * <p> All incoming DataRecords are written to the underlying DataRecordsFileWriter in its original form.
 */
public class RecordsFrequencyCalculator extends DataRecordsFilter {
    private long startTime;
    private long stopTime;
    double actualDurationOfDataRecord;
    private int dataRecordsCounter = 0;

    public RecordsFrequencyCalculator(DataRecordsFileWriter out) {
        super(out);
    }

    @Override
    protected RecordConfig createOutDataRecordConfig() {
        RecordConfig outRecordConfig = new RecordConfig(recordConfig); // copy header config
        if(dataRecordsCounter > 0) {
            // calculate actualDurationOfDataRecord
            actualDurationOfDataRecord = (stopTime - startTime) * 0.001 / dataRecordsCounter;
            outRecordConfig.setDurationOfDataRecord(actualDurationOfDataRecord);
            outRecordConfig.setStartTime(startTime);
            outRecordConfig.setNumberOfDataRecords(dataRecordsCounter);
        }
        return outRecordConfig;
    }

    @Override
    public void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        if (dataRecordsCounter == 0) {
            // 1 second = 1000 msec
            startTime = System.currentTimeMillis() - (long) recordConfig.getDurationOfDataRecord() * 1000;
        }
        stopTime = System.currentTimeMillis();
        out.writeDigitalDataRecord(digitalData, offset);
        dataRecordsCounter++;
    }


    /**
     * calculate average duration of incoming DataRecords, write the result to the header of
     * the underlying EDF or BDF file writer and calls the close method of the
     * underlying file writer
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        out.close();
        DataRecordsFileWriter dataRecordsFileWriter = (DataRecordsFileWriter) out;
        dataRecordsFileWriter.rewriteHeader(createOutDataRecordConfig());
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS");
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append("Start recording time = " + startTime + " (" + dateFormat.format(new Date(startTime)) + ") \n");
        stringBuilder.append("Stop recording time = " + stopTime + " (" + dateFormat.format(new Date(stopTime)) + ") \n");
        stringBuilder.append("Number of data records = " + dataRecordsCounter + "\n");
        stringBuilder.append("Duration of a data record = " + actualDurationOfDataRecord);
        return stringBuilder.toString();
    }
}
