package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.HeaderConfig;

import java.io.IOException;

/**
 * Created by gala on 26/12/16.
 */
public class DataRecordsFilter extends DataRecordsWriter {
    protected DataRecordsWriter out;

    public DataRecordsFilter(DataRecordsWriter out) {
        this.out = out;
    }

    protected HeaderConfig createOutHeaderConfig() {
        return new HeaderConfig(headerConfig);

    }

    @Override
    public void setHeaderConfig(HeaderConfig headerConfig) {
        super.setHeaderConfig(headerConfig);
        out.setHeaderConfig(createOutHeaderConfig());
    }

    @Override
    protected void write(int[] data) throws IOException {
        out.writeDigitalDataRecords(data);

    }

    @Override
    public void close() throws IOException {
        out.close();

    }
}
