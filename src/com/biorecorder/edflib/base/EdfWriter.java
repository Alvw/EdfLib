package com.biorecorder.edflib.base;


/**
 * This abstract class is the superclass of all classes representing an output stream of
 * data packages (DataRecords).
 * An output stream accepts samples belonging to DataRecord
 * and sends (writes) them to some sink.
 * <p>
 * To write data samples to the stream we must set
 * a {@link RecordingInfo} object with the configuration information.
 * Only after that data samples could be written correctly.
 * <p>
 * We may write <b>digital</b> or <b>physical</b>  samples.
 * Every physical (floating point) sample
 * will be converted to the corresponding digital (int) one
 * using physical maximum, physical minimum, digital maximum and digital minimum of the signal.
 *
 */
public abstract class EdfWriter {
    protected volatile RecordingInfo recordingInfo;
    protected volatile long sampleCounter;


    /**
     * Sets the RecordingInfo object describing the structure of
     * the data records (data packages) that will be written.
     * This function MUST be called before writing any data.
     *
     * @param recordingInfo - RecordingInfo object describing DataRecords structure
     */
    public void setRecordingInfo(RecordingInfo recordingInfo)  {
        this.recordingInfo = recordingInfo;
    }

    /**
     * Get the RecordingInfo object with base info about recording process
     * @return RecordingInfo object
     */
    public RecordingInfo getRecordingInfo() {
        return recordingInfo;
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
     * @throws IllegalStateException if RecordingInfo was not set
     */
    public void writePhysicalSamples(double[] physicalSamples) throws IllegalStateException {
        if(recordingInfo == null) {
            throw new IllegalStateException("Recording configuration info is not specified! RecordingInfo = "+ recordingInfo);
        }
        int[] digSamples = new int[physicalSamples.length];
        int signalNumber;
        for (int i = 0; i < physicalSamples.length; i++) {
            signalNumber = recordingInfo.sampleNumberToSignalNumber(sampleCounter + i + 1);
            digSamples[i] = recordingInfo.physicalValueToDigital(signalNumber, physicalSamples[i]);
        }

        writeDigitalSamples(digSamples);
    }

    /**
     * Gets the number of  written data records (data packages).
     * @return number of  written data records
     */
    public int getNumberOfWrittenDataRecords() {
        if(recordingInfo == null || recordingInfo.getDataRecordLength()== 0) {
            return 0;
        }
        return (int) (sampleCounter / recordingInfo.getDataRecordLength());
    }


    /**
     * Closes this Edf/Bdf writer and releases any system resources associated with
     * it. This writer may no longer be used for writing DataRecords.
     * This method MUST be called after finishing writing DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     */
    public abstract void close();
}
