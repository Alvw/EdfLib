package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.HeaderConfig;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gala on 26/12/16.
 */
public class FrequencyAdjuster extends DataRecordsFilter {
    private long startTime;
    private long stopTime;
    double actualDurationOfDataRecord;
    private int dataRecordsCounter = 0;

    public FrequencyAdjuster(DataRecordsWriter out) {
        super(out);
    }

    @Override
    protected void writeOneDataRecord(int[] data, int offset) throws IOException {
        if (dataRecordsCounter == 0) {
            // 1 second = 1000 msec
            startTime = System.currentTimeMillis() - (long) headerConfig.getDurationOfDataRecord() * 1000;
        }
        stopTime = System.currentTimeMillis();
        out.writeDigitalDataRecords(data, offset, 1);
        dataRecordsCounter++;
    }

    @Override
    public void close() throws IOException {
        // calculate actualDurationOfDataRecord
        actualDurationOfDataRecord = (stopTime - startTime) * 0.001 / dataRecordsCounter;
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig); // copy header config
        outHeaderConfig.setDurationOfDataRecord(actualDurationOfDataRecord);
        outHeaderConfig.setStartTime(startTime);
        out.setHeaderConfig(outHeaderConfig);
        out.close();
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Start recording time = " + startTime + " (" + dateFormat.format(new Date(startTime)) + "\n");
        stringBuilder.append("Stop recording time = " + stopTime + " (" + dateFormat.format(new Date(stopTime)) + "\n");
        stringBuilder.append("Number of data records = " + dataRecordsCounter + "\n");
        stringBuilder.append("Duration of a data record = " + actualDurationOfDataRecord);
        return stringBuilder.toString();
    }
}
