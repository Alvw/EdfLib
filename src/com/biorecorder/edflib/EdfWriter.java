package com.biorecorder.edflib;

import java.io.IOException;

/**
 * This abstract class is the superclass of all classes representing an output stream of DataRecords.
 * An output stream accepts a digital DataRecord and send (write) it to some sink.
 * <p>
 * To write DataRecords to the stream we must open this stream first and pass
 * a {@link HeaderConfig} object with the configuration information.
 * Only after that DataRecords and samples could be written correctly.
 * <p>
 * We may write <b>digital</b> or <b>physical</b> DataRecords and samples. Every physical (floating point) value
 * will be converted to the corresponding digital (int) value
 * using physical maximum, physical minimum, digital maximum and digital minimum of the signal.
 * <p>
 *
 */
public abstract class EdfWriter {
    protected HeaderConfig headerConfig = new HeaderConfig(FileType.EDF_16BIT);
    protected long sampleCounter;


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
     * Write the given digital samples.
     *
     * The input array should contain n samples belonging to one signal
     * where n = (samplefrequency of that signal) * (duration of DataRecord).
     * <p>
     * Call this function for every signal (channel) in the file. The order is important!
     * When there are 4 signals in the file,  the order of calling this function
     * must be:
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br>signal 0, signal 1, signal 2, signal 3,
     * <br> ... etc.
     *
     * @param digitalSamples digital samples belonging to one signal obtained during
     *                       the time = duration of DataRecord (usually 1 sec)
     * @throws IOException
     */

    public abstract void writeDigitalSamples(int[] digitalSamples) throws IOException;


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
     *
     * @param physicalSamples array with physical samples to write
     * @throws IOException
     */
    public void writePhysicalSamples(double[] physicalSamples) throws IOException {
        int[] digSamples = new int[physicalSamples.length];
        int signalNumber = 0;
        for (int i = 0; i < physicalSamples.length; i++) {
            signalNumber = headerConfig.signalNumber(sampleCounter + i + 1);
            digSamples[i] = headerConfig.physicalValueToDigital(signalNumber, physicalSamples[i]);
        }

        writeDigitalSamples(digSamples);
    }

    protected int countRecords() {
        return  headerConfig.getRecordLength() == 0 ?  0 :  (int) (sampleCounter / headerConfig.getRecordLength());
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
