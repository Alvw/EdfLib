package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.EdfWriter;
import com.biorecorder.edflib.HeaderConfig;

import java.io.IOException;

/**
 * A base decorator class (pattern Decorator) that is actually just a wrapper around some other
 * DataRecordsWriter object. It implements the same interface but permits to add some additional
 * functionality to the underlying class.
 * <p>
 * EdfFilter itself simply overrides all methods of DataRecordsWriter
 * with versions that pass all requests to the underlying DataRecordsWriter. The only purpose of this class
 * is to be a superclass for real data DataRecord filters which normally modify incoming
 * DataRecords before pass (write) them to underlying writer.
 * <p>
 * Full analog of {@link java.io.FilterOutputStream}
 */
public class EdfFilter extends EdfWriter {
    protected EdfWriter out;

    /**
     * Creates an EdfFilter built on top of the specified underlying DataRecords writer.
     *
     * @param out the underlying DataRecords writer, to be assigned to the field this.out for later use
     */

    public EdfFilter(EdfWriter out) {
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
        return new HeaderConfig(new HeaderConfig(headerConfig));

    }


    /**
     * The open method of EdfFilter calls the same method of its
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
     * @param digitalSamples array with digital data
     * @throws IOException
     */
    @Override
    public void writeDigitalSamples(int[] digitalSamples) throws IOException {
        out.writeDigitalSamples(digitalSamples);
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
