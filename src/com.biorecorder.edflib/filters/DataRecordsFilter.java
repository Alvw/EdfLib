package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.DataRecordsWriter;
import com.biorecorder.edflib.RecordConfig;

import java.io.IOException;

/**
 * A base decorator class (pattern Decorator) that is actually just a wrapper around some other
 * DataRecordsWriter object. It implements the same interface but permits to add some additional
 * functionality to the underlying class.
 *
 * <p>DataRecordsFilter itself simply overrides all methods of DataRecordsWriter
 * with versions that pass all requests to the underlying DataRecordsWriter. The only purpose of this class
 * is to be a superclass for real data DataRecord filters which normally modify incoming
 * DataRecords before pass (write) them to underlying writer.
 *
 * <p>Full analog of {@link java.io.FilterOutputStream}
 *
 */
public class DataRecordsFilter extends DataRecordsWriter {
    protected DataRecordsWriter out;

    /**
     * Creates an DataRecords filter built on top of the specified underlying DataRecords writer.
     *
     * @param out - the underlying DataRecords writer, to be assigned to the field this.out for later use
     */

    public DataRecordsFilter(DataRecordsWriter out) {
        this.out = out;
    }


    /**
     * Create RecordConfig describing the structure of resulting DataRecords
     * (after filtering transformations).
     *
     * @return resulting DataRecords configuration
     */
    protected RecordConfig createOutDataRecordConfig() {
        return new RecordConfig(recordConfig);

    }

    /**
     * The open method of DataRecordsFilter calls the same method of its
     * underlying DataRecordsWriter. That is, it performs out.open(recordConfig).
     *
     * @param recordConfig object with the information about DataRecords structure
     *
     * @throws IOException
     */
    @Override
    public void open(RecordConfig recordConfig) throws IOException {
        super.open(recordConfig);
        out.open(createOutDataRecordConfig());
    }


    /**
     * The writeDigitalDataRecord method of DataRecordsFilter calls the same method of its
     * underlying DataRecordsWriter. That is, it performs out.writeDigitalDataRecord(dataRecord, offset).
     *
     * @param digitalData - array with digital data
     * @param offset - offset within the array at which the DataRecord starts
     *
     * @throws IOException
     */
    @Override
    public void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        out.writeDigitalDataRecord(digitalData, offset);
    }

    /**
     * The close method of DataRecordsFilter calls the same method of its
     * underlying DataRecordsWriter. That is, it performs out.close().
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        out.close();
    }
}
