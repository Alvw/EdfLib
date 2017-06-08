package com.biorecorder.edflib.base;


/**
 * This abstract class is the superclass of all classes representing an output stream of
 * data packages (DataRecords).
 * An output stream accepts samples belonging to DataRecord
 * and sends (writes) them to some sink.
 * <p>
 * To write data samples to the stream we must set
 * a {@link EdfConfig} object with the configuration information.
 * Only after that data samples could be written correctly.
 * <p>
 * We may write <b>digital</b> or <b>physical</b>  samples.
 * Every physical (floating point) sample
 * will be converted to the corresponding digital (int) one
 * using physical maximum, physical minimum, digital maximum and digital minimum of the signal.
 *
 */
public abstract class EdfWriter {
    protected volatile EdfConfig edfConfig;
    protected volatile long sampleCounter;


    /**
     * Sets the EdfConfig object describing the structure of
     * the data records (data packages) that will be written.
     * This function MUST be called before writing any data.
     *
     * @param recordingInfo - EdfConfig object describing DataRecords structure
     */
    public void setConfig(EdfConfig recordingInfo)  {
        this.edfConfig = recordingInfo;
    }

    /**
     * Writes the entire DataRecord (data pack) containing "raw" digital data from all signals
     * starting with n_0 samples of signal 0, n_1 samples of signal 1, n_2 samples of signal 2, etc.
     * <p>
     * Number of samples (length) for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     * This method do exactly the same as the call
     * writeDigitalSamples(digitalDataRecord, 0, dataRecordLength).
     *
     @param digitalDataRecord
     */
    public void writeDigitalRecord(int[] digitalDataRecord) {
        writeDigitalSamples(digitalDataRecord, 0, edfConfig.getDataRecordLength());
    }

    /**
     * Write "raw" digital (integer) samples from the given array to the stream/file.
     * This method do exactly the same as the call
     * writeDigitalSamples(digitalSamples, 0, digitalSamples.length).
     *
     * @param digitalSamples digital samples belonging to some signal or entire DataRecord
     */
    public final void writeDigitalSamples(int[] digitalSamples) {
        writeDigitalSamples(digitalSamples, 0, digitalSamples.length);
    }

    /**
     * Writes  «length» digital (integer) samples from the specified array
     * starting at offset «offset» to this EDF/BDF stream/file.
     * <p>
     * Call this method for every signal (channel) of the stream/file. The order is important!
     * When there are 4 signals,  the order of calling this method must be:
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br> ... etc.
     * <p>
     * Number of samples (length) for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     * The entire DataRecord (data pack) containing digital data from all signals also can be placed in one array
     * and written at once.
     *
     * @param digitalSamples data array with digital samples
     * @param offset the start offset in the data.
     * @param length the number of bytes to write.
     */
    public abstract void writeDigitalSamples(int[] digitalSamples, int offset, int length);


    /**
     * Write physical samples (uV, mA, Ohm) from the given array to the stream/file.
     * <p>
     * Call this method for every signal (channel) of the stream/file. The order is important!
     * When there are 4 signals,  the order of calling this method must be:
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br> ... etc.
     * <p>
     * Number of samples (length) for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     * The entire DataRecord (data pack) containing digital data from all signals also can be placed in one array
     * and written at once.
     * <p>
     * The physical samples will be converted to digital samples using the
     * values of physical maximum, physical minimum, digital maximum and digital minimum of
     * of the signal.
     *
     * @param physicalSamples physical samples belonging to some signal or entire DataRecord
     * @throws IllegalStateException if EdfConfig was not set
     */
    public void writePhysicalSamples(double[] physicalSamples) throws IllegalStateException {
        if(edfConfig == null) {
            throw new IllegalStateException("Recording configuration info is not specified! EdfConfig = "+ edfConfig);
        }
        int[] digSamples = new int[physicalSamples.length];
        int signalNumber;
        for (int i = 0; i < physicalSamples.length; i++) {
            signalNumber = edfConfig.sampleNumberToSignalNumber(sampleCounter + i + 1);
            digSamples[i] = edfConfig.physicalValueToDigital(signalNumber, physicalSamples[i]);
        }

        writeDigitalSamples(digSamples);
    }

    /**
     * Gets the number of received data records (data packages).
     * @return number of received data records
     */
    public int getNumberOfReceivedDataRecords() {
        if(edfConfig == null || edfConfig.getDataRecordLength()== 0) {
            return 0;
        }
        return (int) (sampleCounter / edfConfig.getDataRecordLength());
    }


    /**
     * Closes this Edf/Bdf writer and releases any system resources associated with
     * it. This writer may no longer be used for writing DataRecords.
     * This method MUST be called after finishing writing DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     */
    public abstract void close();
}
