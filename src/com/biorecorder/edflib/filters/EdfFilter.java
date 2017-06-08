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
     * Create HeaderConfig describing the structure of resulting output DataRecords
     * (after corresponding transformations).
     * This method should be overwritten in all subclasses.
     *
     * @return HeaderConfig object describing resultant output DataRecords configuration
     */
    protected EdfConfig createOutputConfig() {
        return edfConfig == null? null : new DefaultEdfConfig(edfConfig);
    }


    @Override
    public void setConfig(EdfConfig recordingInfo)  {
        super.setConfig(recordingInfo);
        out.setConfig(createOutputConfig());
    }

    /**
     * Gets the EdfConfig object describing the structure of
     * RESULTANT data records (data packages)
     *
     * @return EdfConfig object describing the structure of
     * RESULTANT data records
     */
    public EdfConfig getResultantConfig(){
        if( out instanceof EdfFilter) {
            return ((EdfFilter) out).getResultantConfig();
        }
        return createOutputConfig();
    }

    /**
     * Gets the number of written RESULTANT data records (data packages).
     * @return number of written RESULTANT data records
     */
    public int getNumberOfWrittenDataRecords() {
        if( out instanceof EdfFilter) {
            return ((EdfFilter) out).getNumberOfWrittenDataRecords();
        }
        return out.getNumberOfReceivedDataRecords();
    }



    /**
     * Calls the same method of its underlying EdfWriter and pass to it data samples.
     *
     * @param digitalSamples data array with digital samples
     * @param offset the start offset in the data.
     * @param length the number of bytes to write.
     * @throws IllegalStateException if EdfConfig was not set
     */
    @Override
    public void writeDigitalSamples(int[] digitalSamples, int offset, int length)  {
        if(edfConfig == null) {
            throw new IllegalStateException("Recording configuration info is not specified! EdfConfig = "+ edfConfig);
        }
        out.writeDigitalSamples(digitalSamples, offset, length);
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
