package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DataRecordsWriter;
import com.biorecorder.edflib.base.HeaderConfig;

import java.io.IOException;

/**
 * A base decorator class (pattern Decorator) that is actually just a wrapper around some other
 * DataRecordsWriter object. It implements the same interface but permits to add some additional
 * functionality to the underlying class.
 * <p>
 * DataRecordsFilter itself simply overrides all methods of DataRecordsWriter
 * with versions that pass all requests to the underlying DataRecordsWriter. The only purpose of this class
 * is to be a superclass for real data DataRecord filters which normally modify incoming
 * DataRecords before pass (write) them to underlying writer.
 * <p>
 * Full analog of {@link java.io.FilterOutputStream}
 */
public class DataRecordsFilter extends DataRecordsWriter {
    protected DataRecordsWriter out;

    /**
     * Creates an DataRecordsFilter built on top of the specified underlying DataRecords writer.
     *
     * @param out the underlying DataRecords writer, to be assigned to the field this.out for later use
     */

    public DataRecordsFilter(DataRecordsWriter out) {
        this.out = out;
    }


    /**
     * Create HeaderConfig describing the structure of resulting output DataRecords
     * (after filtering transformations). In the base class it simply returns the copy
     * of the configuration set for input DataRecords.
     * This method should be overwritten in all subclasses.
     *
     * @return resulting output DataRecords configuration
     */
    protected HeaderConfig createOutputRecordingConfig() {
        return new HeaderConfig(headerConfig);

    }


    /**
     * The open method of DataRecordsFilter calls the same method of its
     * underlying DataRecordsWriter but with the new HeaderConfig
     * corresponding to the structure of resulting output DataRecords
     *
     * @param headerConfig object with the information about input DataRecords structure
     * @throws IOException
     */
    @Override
    public void open(HeaderConfig headerConfig) throws IOException {
        super.open(headerConfig);
        out.open(createOutputRecordingConfig());
    }


    /**
     * Calls the same method of its underlying DataRecordsWriter.
     *
     * @param digitalData array with digital data
     * @param offset      offset within the array at which the DataRecord starts
     * @throws IOException
     */
    @Override
    public void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        out.writeDigitalDataRecord(digitalData, offset);
    }

    /**
     * Calls the same method of its
     * underlying DataRecordsWriter.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        out.close();
    }
}
