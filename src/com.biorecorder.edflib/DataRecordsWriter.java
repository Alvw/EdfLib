package com.biorecorder.edflib;

import com.biorecorder.edflib.util.PhysicalDigitalConverter;
import com.biorecorder.edflib.filters.DataRecordsFilter;
import java.io.IOException;

/**
 * This abstract class is the superclass of all classes representing an output stream of DataRecords.
 * An output stream accepts a digital DataRecord and send (write) it to some sink.
 *
 * <p>In the case if DataRecord contains not digital (int) but real physical (floating point) values
 * they should be converted to ints first.  The physical samples will be converted to digital samples
 * using the values of physical maximum, physical minimum, digital maximum and digital minimum
 * for every signal. See {@link PhysicalDigitalConverter}.
 *
 * <p>The special {@link RecordingConfig} object contains all information about
 * DataRecords structure and its signals configurations and permits correctly extract
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
    protected int[] digitalDataRecord = new int[0];
    protected int digitalDataRecordOffset;
    protected double[] physicalDataRecord = new double[0];
    protected int physicalDataRecordOffset;

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
     * Write digital samples from the given array to the inner digital DataRecord.
     * The input array should contain n samples belonging to one signal
     * where n = (samplefrequency of that signal) * (duration of DataRecord).
     *
     * <p>Call this function for every signal in the file. The order is important!
     * When there are 4 signals in the file,  the order of calling this function
     * must be:
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br> ... etc.
     *
     * Every time when the inner digital DataRecord is formed the method
     * writeDigitalDataRecord(DataRecord) will be called.
     *
     * @param digitalSamples digital samples belonging to one signal obtained during
     *                       the time = duration of DataRecord (usually 1 sec)
     * @throws IOException
     */

    public void writeDigitalSamples(int[] digitalSamples) throws IOException {
        if(digitalDataRecord.length == 0) {
            digitalDataRecord = new int[recordingConfig.getRecordLength()];
        }
        System.arraycopy(digitalSamples, 0, digitalDataRecord, digitalDataRecordOffset, digitalSamples.length);
        digitalDataRecordOffset += digitalSamples.length;
        if(digitalDataRecordOffset >= recordingConfig.getRecordLength()) {
            writeDigitalDataRecord(digitalDataRecord);
            digitalDataRecordOffset = 0;
        }
    }

    /**
     * Write physical samples from the given array to the inner physical DataRecord.
     * The input array should contain n samples belonging to one signal
     * where n = (samplefrequency of that signal) * (duration of DataRecord).
     *
     * <p>Call this function for every signal in the file. The order is important!
     * When there are 4 signals in the file,  the order of calling this function
     * must be:
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br> ... etc.
     *
     * Every time when the inner physical DataRecord is formed the method
     * writePhysicalDataRecord(DataRecord) will be called.
     *
     * @param physicalSamples
     * @throws IOException
     */
    public void writePhysicalSamples(double[] physicalSamples) throws IOException {
        if(physicalDataRecord.length == 0) {
            physicalDataRecord= new double[recordingConfig.getRecordLength()];
        }
        System.arraycopy(physicalSamples, 0, physicalDataRecord, physicalDataRecordOffset, physicalSamples.length);
        digitalDataRecordOffset += physicalSamples.length;
        if(digitalDataRecordOffset >= recordingConfig.getRecordLength()) {
            writePhysicalDataRecord(physicalDataRecord);
            digitalDataRecordOffset = 0;
        }
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
     * Convert ONE physical DataRecord to digital and write it. That is, it performs
     * writeDigitalDataRecord(physicalDigitalConverter.physicalRecordToDigital(physData, offset)).
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
