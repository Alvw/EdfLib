package com.biorecorder.edflib.base;

import java.io.IOException;

/**
 * This abstract class is the superclass of all classes representing an output stream of DataRecords.
 * An output stream accepts a digital DataRecord and send (write) it to some sink.
 * <p>
 * To write DataRecords to the stream we must open this stream first and pass
 * a {@link HeaderConfig} object with the configuration information.
 * That is, call the method {@link #open(HeaderConfig)}.
 * Only after that DataRecords and samples could be written correctly.
 * <p>
 * We may write <b>digital</b> or <b>physical</b> DataRecords and samples. Every physical (floating point) value
 * will be converted to the corresponding digital (int) value
 * using physical maximum, physical minimum, digital maximum and digital minimum of the signal.
 * <p>
 * Every subclass of DataRecordsWriter must implement the
 * method that writes one digital DataRecord - {@link #writeDigitalDataRecord(int[], int)}
 *
 */
public abstract class DataRecordsWriter {
    protected HeaderConfig headerConfig = new HeaderConfig();
    protected PhysicalDigitalConverter physicalDigitalConverter = new PhysicalDigitalConverter(headerConfig);
    protected int[] dataRecord = new int[0];
    protected int dataRecordOffset;


    /**
     * Prepare the DataRecords writer for correct work.
     * This function MUST be called before writing any data.
     * After opening the configuration of the writer SHOULD NOT be changed.
     *
     * @param headerConfig - object with the information about DataRecords structure
     * @throws IOException
     */
    public void open(HeaderConfig headerConfig) throws IOException {
        this.headerConfig = new HeaderConfig(headerConfig);
        physicalDigitalConverter = new PhysicalDigitalConverter(headerConfig);
    }

    /**
     * Return the information from the file header stored in the HeaderConfig object
     *
     * @return the object containing EDF/BDF header information
     */
    public HeaderConfig getHeaderInfo(){
        return new HeaderConfig(headerConfig);
    }


    /**
     * Write the given digital samples to the inner DataRecord.
     * The input array should contain n samples belonging to one signal
     * where n = (samplefrequency of that signal) * (duration of DataRecord).
     * <p>
     * Call this function for every signal (channel) in the file. The order is important!
     * When there are 4 signals in the file,  the order of calling this function
     * must be:
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br> ... etc.
     * <p>
     * Every time when the inner digital DataRecord is completely formed the method
     * {@link #writeDigitalDataRecord(int[])} is called.
     *
     * @param digitalSamples digital samples belonging to one signal obtained during
     *                       the time = duration of DataRecord (usually 1 sec)
     * @throws IOException
     */

    public void writeDigitalSamples(int[] digitalSamples) throws IOException {
        if (dataRecord.length == 0) {
            dataRecord = new int[headerConfig.getRecordLength()];
        }
        int length = Math.min(headerConfig.getRecordLength() - dataRecordOffset, digitalSamples.length);
        System.arraycopy(digitalSamples, 0, dataRecord, dataRecordOffset, length);
        dataRecordOffset += digitalSamples.length;
        if (dataRecordOffset >= headerConfig.getRecordLength()) {
            writeDigitalDataRecord(dataRecord);
            dataRecordOffset = 0;
        }
    }

    /**
     * Convert the given physical samples to digital
     * and call the method {@link #writeDigitalSamples(int[])}.
     * <p>
     * The input array should contain n samples belonging to one signal
     * where n = (samplefrequency of that signal) * (duration of DataRecord).
     * <p>
     * Call this function for every signal (channel) in the file. The order is important!
     * When there are 4 signals in the file,  the order of calling this function
     * must be:
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br> ... etc.
     * <p>
     * Every time when the inner digital DataRecord is completely formed the method
     * {@link #writeDigitalDataRecord(int[])} is called.
     *
     * @param physicalSamples array with physical samples to write
     * @throws IOException
     */
    public void writePhysicalSamples(double[] physicalSamples) throws IOException {
        int signalNumber = headerConfig.getSampleSignal(dataRecordOffset);
        writeDigitalSamples(physicalDigitalConverter.signalPhysicalValuesToDigital(signalNumber, physicalSamples));
    }

    /**
     * Write ONE digital DataRecord.
     * Take data from digitalData array starting at offset position.
     *
     * @param digitalData array with digital data
     * @param offset      offset within the array at which the DataRecord starts
     * @throws IOException
     */
    public abstract void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException;


    /**
     * Convert ONE physical DataRecord to digital one and write it. That is, it performs
     * writeDigitalDataRecord(physicalDigitalConverter.physicalRecordToDigital(physData, offset)).
     * Take data from physData array starting at offset position.
     *
     * @param physData array with physical data
     * @param offset   offset within the array at which the DataRecord starts
     * @throws IOException
     */
    public void writePhysicalDataRecord(double[] physData, int offset) throws IOException {
        writeDigitalDataRecord(physicalDigitalConverter.physicalRecordToDigital(physData, offset));
    }

    /**
     * Write the given digital DataRecord.
     * This method do exactly the same as the call
     * {@link #writeDigitalDataRecord(int[], int) writeDigitalDataRecord(dataRecord, 0)} method.
     *
     * @param digitalDataRecord digital DataRecord to be written to the stream
     * @throws IOException
     */
    public void writeDigitalDataRecord(int[] digitalDataRecord) throws IOException {
        if (digitalDataRecord == null) {
            return;
        }
        if (digitalDataRecord.length != headerConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + digitalDataRecord.length + " DataRecord length = " + headerConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        writeDigitalDataRecord(digitalDataRecord, 0);
    }

    /**
     * Convert the given physical DataRecord to digital one and write it.
     * This method do exactly the same as the call
     * {@link #writePhysicalDataRecord(double[], int) writePhysicalDataRecord(physDataRecord, 0)}  method.
     *
     * @param physDataRecord physical DataRecord to be converted to digital one and written to the stream
     * @throws IOException
     */
    public void writePhysicalDataRecord(double[] physDataRecord) throws IOException {
        if (physDataRecord == null) {
            return;
        }
        if (physDataRecord.length != headerConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + physDataRecord.length + " DataRecord length = " + headerConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
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
