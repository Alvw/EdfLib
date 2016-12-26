package com.biorecorder.edflib;

import com.biorecorder.edflib.util.PhysicalDigitalConverter;

import java.io.IOException;

/**
 * Created by gala on 21/12/16.
 */
public abstract class DataRecordsWriter {
    protected HeaderConfig headerConfig;
    protected PhysicalDigitalConverter physicalDigitalConverter;

    public void setHeaderConfig (HeaderConfig headerConfig)  {
        this.headerConfig = headerConfig;
        physicalDigitalConverter = new PhysicalDigitalConverter(headerConfig);
    }

    public void writePhysicalDataRecords(double[] physData) throws IOException  {
        writeDigitalDataRecords(physicalDigitalConverter.physicalArrayToDigital(physData));
    }

    public void writeDigitalDataRecords(int[] data) throws IOException {
        if(headerConfig == null) {
            return;
        }
        if(data.length % headerConfig.getRecordLength() != 0) {
            String errMsg = "The input array must contain an integer number of DataRecords. Input array length = "
                    + data.length + " DataRecord length = " + headerConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        write(data);
    }

    protected abstract void write(int[] data) throws IOException;

    public abstract void close() throws IOException;
}
