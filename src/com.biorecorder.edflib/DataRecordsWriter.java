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
        this.headerConfig = new HeaderConfig(headerConfig);
        physicalDigitalConverter = new PhysicalDigitalConverter(headerConfig);
    }

    public void writeDigitalDataRecord(int[] data, int offset) throws IOException {
        if(headerConfig == null ||  headerConfig.getRecordLength() == 0) {
            return;
        }
        writeOneDataRecord(data, offset);
    }

    public void writePhysicalDataRecord(double[] physData, int offset) throws IOException  {
        if(headerConfig == null ||  headerConfig.getRecordLength() == 0) {
            return;
        }
        writeDigitalDataRecord(physicalDigitalConverter.physicalArrayToDigital(physData), offset);
    }

    public void writeDigitalDataRecord(int[] data) throws IOException {
        writeDigitalDataRecord(data, 0);
    }

    public void writePhysicalDataRecord(double[] physData) throws IOException  {
       writePhysicalDataRecord(physData, 0);
    }

    protected abstract void writeOneDataRecord(int[] data, int offset) throws IOException;



    public abstract void close() throws IOException;
}
