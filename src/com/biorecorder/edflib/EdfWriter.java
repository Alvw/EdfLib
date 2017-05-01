package com.biorecorder.edflib;

import java.io.IOException;

/**
 * This abstract class is the superclass of all classes representing an output stream of DataRecords.
 * An output stream accepts a digital DataRecord and send (write) it to some sink.
 * <p>
 * To write data samples to the stream we must set
 * a {@link HeaderInfo} object with the configuration information.
 * Only after that data samples could be written correctly.
 * <p>
 * We may write <b>digital</b> or <b>physical</b>  samples.
 * Every physical (floating point) sample
 * will be converted to the corresponding digital (int) one
 * using physical maximum, physical minimum, digital maximum and digital minimum of the signal.
 *
 */
public abstract class EdfWriter {
    protected HeaderInfo headerInfo;
    protected long sampleCounter;


    /**
     * Sets a HeaderInfo object with the configuration information.
     * This function MUST be called before writing any data.
     *
     * @param headerInfo - HeaderInfo object describing DataRecords structure
     * @throws IOException
     */
    public void setHeader(HeaderInfo headerInfo) throws IOException {
        this.headerInfo = headerInfo;
    }


    /**
     * Gets the HeaderInfo object with the configuration information.
     *
     * @return the object containing EDF/BDF header information
     */
    public HeaderInfo getHeader(){
        return new HeaderInfo(headerInfo);
    }



    /**
     * Write "raw" digital (integer) samples from the given array to the stream/file.
     * <p>
     * Call this method for every signal (channel) of the stream/file. The order is important!
     * When there are 4 signals,  the order of calling this method must be:
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br> ... etc.
     * <p>
     * Number of samples for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     * The entire DataRecord (data pack) containing digital data from all signals also can be placed in one array
     * and written at once.
     *
     * @param digitalSamples digital samples belonging to some signal or entire DataRecord
     * @throws IOException
     */
    public abstract void writeDigitalSamples(int[] digitalSamples) throws IOException;



    /**
     * Write physical samples (uV, mA, Ohm) from the given array to the stream/file.
     * <p>
     * Call this method for every signal (channel) of the stream/file. The order is important!
     * The physical samples will be converted to digital samples using the
     * values of physical maximum, physical minimum, digital maximum and digital minimum of
     * of the signal.
     * When there are 4 signals,  the order of calling this method must be:
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br> ... etc.
     * <p>
     * Number of samples for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     * The entire DataRecord (data pack) containing physical data from all signals also can be placed in one array
     * and written at once.
     *
     * @param physicalSamples physical samples belonging to some signal or entire DataRecord
     * @throws IOException
     */
    public void writePhysicalSamples(double[] physicalSamples) throws IOException {
        int[] digSamples = new int[physicalSamples.length];
        int signalNumber;
        for (int i = 0; i < physicalSamples.length; i++) {
            signalNumber = headerInfo.sampleNumberToSignalNumber(sampleCounter + i + 1);
            digSamples[i] = headerInfo.physicalValueToDigital(signalNumber, physicalSamples[i]);
        }

        writeDigitalSamples(digSamples);
    }

    protected int countRecords() {
        return  headerInfo.getRecordLength() == 0 ?  0 :  (int) (sampleCounter / headerInfo.getRecordLength());
    }



    /**
     * Closes this Edf/Bdf writer and releases any system resources associated with
     * it. This writer may no longer be used for writing DataRecords.
     * This method MUST be called after finishing writing DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;
}
