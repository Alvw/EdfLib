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
    protected EdfConfig config;
    protected long sampleCounter;


    /**
     * Sets a EdfConfig object with the configuration information.
     * This function MUST be called before writing any data.
     *
     * @param edfConfig - HeaderInfo object describing DataRecords structure
     */
    public void setConfig(EdfConfig edfConfig)  {
        this.config = edfConfig;
    }


    /**
     * Gets the EdfConfig object with the configuration information.
     *
     * @return the object containing EDF/BDF header information
     */
    public EdfConfig getConfig(){
        return new DefaultEdfConfig(config);
    }



    /**
     * Write "raw" digital (integer) samples from the given array to the stream/file.
     * <p>
     * The entire DataRecord (data pack) containing digital data from all signals
     * can be placed in one array and written at once.
     * <p>
     * Or this method can be called for every signal (channel) to write the samples belonging
     * to that signal. In this case the order is important!
     * When there are 4 signals,  the order of calling this method must be:
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br> ... etc.
     * <p>
     * Number of samples for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     *
     * @param digitalSamples digital samples belonging to some signal or entire DataRecord
     */
    public abstract void writeDigitalSamples(int[] digitalSamples);



    /**
     * Write physical samples (uV, mA, Ohm) from the given array to the stream/file.
     * <p>
     * The entire DataRecord (data pack) containing physical data from all signals
     * can be placed in one array and written at once.
     * <p>
     * * r this method can be called for every signal (channel) to write the samples belonging
     * to that signal. In this case the order is important!
     * When there are 4 signals,  the order of calling this method must be:
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br>samples belonging to signal 0, samples belonging to signal 1, samples belonging to signal 2, samples belonging to  signal 3,
     * <br> ... etc.
     * <p>
     * Number of samples for every signal: n_i = (sample frequency of the signal_i) * (duration of DataRecord).
     * <p>
     * The physical samples will be converted to digital samples using the
     * values of physical maximum, physical minimum, digital maximum and digital minimum of
     * of the signal.
     *
     * @param physicalSamples physical samples belonging to some signal or entire DataRecord
     */
    public void writePhysicalSamples(double[] physicalSamples)  {
        int[] digSamples = new int[physicalSamples.length];
        int signalNumber;
        for (int i = 0; i < physicalSamples.length; i++) {
            signalNumber = config.sampleNumberToSignalNumber(sampleCounter + i + 1);
            digSamples[i] = config.physicalValueToDigital(signalNumber, physicalSamples[i]);
        }

        writeDigitalSamples(digSamples);
    }

    /**
     * Gets the number of actually written data records (data packages).
     * @return number of  written data records
     */
    public int getNumberOfWrittenDataRecords() {
        if(config == null || config.getDataRecordLength()== 0) {
            return 0;
        }
        return (int) (sampleCounter / config.getDataRecordLength());
    }


    /**
     * Closes this Edf/Bdf writer and releases any system resources associated with
     * it. This writer may no longer be used for writing DataRecords.
     * This method MUST be called after finishing writing DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     */
    public abstract void close();
}