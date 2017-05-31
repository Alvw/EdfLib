package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DefaultEdfConfig;
import com.biorecorder.edflib.base.EdfConfig;
import com.biorecorder.edflib.base.EdfWriter;

/**
 * A base decorator class (pattern Decorator) that is actually just a wrapper around some other
 * EdfWriter object. It implements the same interface but permits to add some additional
 * functionality to the underlying class.
 * <p>
 * EdfFilter itself simply overrides all methods of EdfWriter
 * with versions that pass all requests to the underlying EdfWriter. The only purpose of this class
 * is to be a superclass for real realisation of EdfFilter which normally modify incoming
 * DataRecords before pass (write) them to underlying writer.
 * <p>
 * Full analog of {@link java.io.FilterOutputStream}
 */
public class EdfFilter extends EdfWriter {
    protected EdfWriter out;

    /**
     * Creates an EdfFilter built on top of the specified underlying EdfWriter.
     *
     * @param out the underlying EdfWriter
     */

    public EdfFilter(EdfWriter out) {
        this.out = out;
    }


    /**
     * Create HeaderInfo describing the structure of resulting output DataRecords
     * (after corresponding transformations).
     * This method should be overwritten in all subclasses.
     *
     * @return HeaderInfo object describing resultant output DataRecords configuration
     */
    protected EdfConfig createOutputConfig() {
        return new DefaultEdfConfig(config);

    }


    /**
     * The setConfig method of EdfFilter create HeaderInfo object describing the
     * structure of resultant DataRecords and pass it to underlying EdfWriter
     *
     * @param edfConfig HeaderInfo object with the information describing input DataRecords structure
     */
    @Override
    public void setConfig(EdfConfig edfConfig)  {
        super.setConfig(edfConfig);
        out.setConfig(createOutputConfig());
    }


    /**
     * Calls the same method of its underlying EdfWriter and pass to it data samples.
     *
     * @param digitalSamples array with digital data samples
     */
    @Override
    public void writeDigitalSamples(int[] digitalSamples)  {
        if(config == null) {
            throw new RuntimeException("File header is not specified! HeaderInfo = "+ config);
        }
        out.writeDigitalSamples(digitalSamples);
    }

    /**
     * Calls the same method of its
     * underlying EdfWriter.
     */
    @Override
    public void close() {
        out.close();
    }
}
