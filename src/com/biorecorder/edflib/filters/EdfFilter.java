package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DefaultRecordingInfo;
import com.biorecorder.edflib.base.RecordingInfo;
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
    protected RecordingInfo createOutputConfig() {
        return recordingInfo == null? null : new DefaultRecordingInfo(recordingInfo);
    }


    /**
     * The setRecordingInfo method of EdfFilter create the configuration object describing the
     * structure of resultant DataRecords and pass it to underlying EdfWriter
     *
     * @param recordingInfo HeaderInfo object with the information describing input DataRecords structure
     */
    @Override
    public void setRecordingInfo(RecordingInfo recordingInfo)  {
        super.setRecordingInfo(recordingInfo);
        out.setRecordingInfo(createOutputConfig());
    }

    /**
     * Gets the RecordingInfo object describing the structure of
     * RESULTANT data records (data packages)
     *
     * @return the object containing EDF/BDF header information
     */
    @Override
    public RecordingInfo getRecordingInfo(){
        return out.getRecordingInfo();
    }

    /**
     * Gets the number of  written RESULTANT data records (data packages).
     * @return number of written RESULTANT data records
     */
    @Override
    public int getNumberOfWrittenDataRecords() {
        return out.getNumberOfWrittenDataRecords();
    }

    /**
     * Calls the same method of its underlying EdfWriter and pass to it data samples.
     *
     * @param digitalSamples array with digital data samples
     * @throws IllegalStateException if RecordingInfo was not set
     */
    @Override
    public void writeDigitalSamples(int[] digitalSamples)  {
        if(recordingInfo == null) {
            throw new IllegalStateException("Recording configuration info is not specified! RecordingInfo = "+ recordingInfo);
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
