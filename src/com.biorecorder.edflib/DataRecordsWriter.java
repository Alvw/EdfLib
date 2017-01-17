package com.biorecorder.edflib;

import com.biorecorder.edflib.util.PhysicalDigitalConverter;
import com.biorecorder.edflib.filters.DataRecordsFilter;
import java.io.IOException;

/**
 * This abstract class is the superclass of all classes representing an output stream of DataRecords.
 * An output stream accepts a digital DataRecord and send (write) it to some sink.
 *
 * <p>In the case if DataRecord contains not digital (int) but real physical (floating point) values
 * they should be converted to ints first. See {@link PhysicalDigitalConverter}.
 * The special {@link RecordingConfig} object contains all necessary information about
 * DataRecords structure and permits correctly extract
 * data from DataRecords and convert every physical DataRecord to digital DataRecord and vice versa.
 *
 * <p>Every subclass of DataRecordsWriter must implement the
 * method that writes one digital DataRecord - writeDigitalDataRecord(int[] digitalData, int offset).
 *
 * @see DataRecordsFileWriter
 * @see DataRecordsFilter
 */
public abstract class DataRecordsWriter {
    protected RecordingConfig recordingConfig = new RecordingConfig();
    protected PhysicalDigitalConverter physicalDigitalConverter;

    /**
     * Prepare the DataRecords writer for correct work.
     * This function MUST be called before writing any data.
     * After opening the configuration of the writer SHOULD NOT be changed.
     *
     * @param recordingConfig - object with the information about DataRecords structure
     *
     * @throws IOException
     */
    public void open(RecordingConfig recordingConfig) throws IOException {
        this.recordingConfig = new RecordingConfig(recordingConfig);
        physicalDigitalConverter = new PhysicalDigitalConverter(recordingConfig);
    }


    /**
     * Write ONE digital DataRecord.
     * Take data from digitalData array starting at offset position.
     *
     * @param digitalData - array with digital data
     * @param offset - offset within the array at which the DataRecord starts
     *
     * @throws IOException
     */
    public abstract void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException;


    /**
     * Convert ONE physical DataRecord to digital and write it.
     * Take data from physData array starting at offset position.
     *
     * @param physData - array with physical data
     * @param offset - offset within the array at which the DataRecord starts
     *
     * @throws IOException
     */
    public void writePhysicalDataRecord(double[] physData, int offset) throws IOException  {
        writeDigitalDataRecord(physicalDigitalConverter.physicalRecordToDigital(physData, offset));
    }

    /**
     * Write given digital DataRecord.
     * This method do exactly the same as the call writeDigitalDataRecord(digitalDataRecord, 0)
     *
     * @param digitalDataRecord - digital DataRecord to be written to the stream
     *
     * @throws IOException
     */
    public void writeDigitalDataRecord(int[] digitalDataRecord) throws IOException {
        writeDigitalDataRecord(digitalDataRecord, 0);
    }

    /**
     * Convert given physical DataRecord to digital and write it.
     * This method do exactly the same as the call writePhysicalDataRecord(physDataRecord, 0)
     *
     * @param physDataRecord - physical DataRecord to be converted to digital and written to the stream
     *
     * @throws IOException
     */
    public void writePhysicalDataRecord(double[] physDataRecord) throws IOException  {
       writePhysicalDataRecord(physDataRecord, 0);
    }

    /**
     * Closes this writer and releases any system resources associated with
     * it. This writer may no longer be used for writing DataRecords.
     * This method MUST be called after finishing writing DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;
}
