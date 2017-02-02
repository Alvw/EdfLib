package com.biorecorder.edflib.base;

import java.util.ArrayList;

/**
 * Class (data-structure) that allows to store the information required to create EDF/BDF file header
 * and correctly extract data from the DataRecords. It has the following fields
 * (and their corresponding getters and setters):
 * <ul>
 *     <li>patient identification</li>
 *     <li>recording identification</li>
 *     <li>recording recordingStartTime</li>
 *     <li>number of data records</li>
 *     <li>duration of a data record (in seconds)</li>
 * </ul>
 * <p>
 * Configuration of every measuring channel is described by a separate
 * special class {@link SignalConfig}, that store all important information about the channel:
 * number of sample coming from that channel, its digital and physical minimum and maximum,
 * physical dimension (uV or Ohm) and so on.
 * <p>
 * HeaderConfig must contain a list of configurations for all measuring channels (signals) and
 * we have to add them successively in the same order in which the samples belonging to the
 * channels will be placed (saved) in DataRecords.
 * <p>
 * To add the signal (channel) configuration use the method
 * {@link #addSignalConfig(SignalConfig)}
 * <p>
 * To get the signal configuration ( ! <b>the numbering starts at 0</b> ! ) - {@link #getSignalConfig(int)}
 * <p>
 * To get the number of the signals in data record - {@link #getNumberOfSignals()}
 * <p>
 * To get the number of bytes in header record - {@link #getNumberOfBytesInHeaderRecord()}
 *
 * @see SignalConfig
 */

public class HeaderConfig {
    private String patientIdentification = "Default patient";
    private String recordingIdentification = "Default record";
    private long recordingStartTime = -1;
    private int numberOfDataRecords = -1;
    private double durationOfDataRecord;
    private ArrayList<SignalConfig> signals = new ArrayList<SignalConfig>();

    public HeaderConfig() {

    }

    /**
     * Constructor to make a copy of the given HeaderConfig instance
     *
     * @param headerConfig HeaderConfig instance that will be copied
     */
    public HeaderConfig(HeaderConfig headerConfig) {
        patientIdentification = headerConfig.getPatientIdentification();
        recordingIdentification = headerConfig.getRecordingIdentification();
        recordingStartTime = headerConfig.getRecordingStartTime();
        durationOfDataRecord = headerConfig.getDurationOfDataRecord();
        numberOfDataRecords = headerConfig.getNumberOfDataRecords();
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            signals.add(new SignalConfig(headerConfig.getSignalConfig(i)));
        }
    }


    public String getPatientIdentification() {
        return patientIdentification;
    }

    public void setPatientIdentification(String patientIdentification) {
        this.patientIdentification = patientIdentification;
    }

    public String getRecordingIdentification() {
        return recordingIdentification;
    }

    public void setRecordingIdentification(String recordingIdentification) {
        this.recordingIdentification = recordingIdentification;
    }

    public long getRecordingStartTime() {
        return recordingStartTime;
    }

    public void setRecordingStartTime(long recordingStartTime) {
        this.recordingStartTime = recordingStartTime;
    }

    public int getNumberOfDataRecords() {
        return numberOfDataRecords;
    }

    public void setNumberOfDataRecords(int numberOfDataRecords) {
        this.numberOfDataRecords = numberOfDataRecords;
    }

    public double getDurationOfDataRecord() {
        return durationOfDataRecord;
    }

    public void setDurationOfDataRecord(double durationOfDataRecord) {
        this.durationOfDataRecord = durationOfDataRecord;
    }

    /**
     * HeaderConfig must include SignalConfigs describing all measuring channels (signals).
     * And we have to add them successively in the same order in which the samples belonging to the
     * channels will be placed (saved) in DataRecords
     *
     * @param signalConfig object describing parameters of the measuring channel
     */
    public void addSignalConfig(SignalConfig signalConfig) {
        signals.add(signalConfig);
    }


    public void removeSignalConfig(int signalNumber) {
        signals.remove(signalNumber);
    }

    public void removeAllSignalConfigs() {
        signals = new ArrayList<SignalConfig>();
    }


    /**
     * Return the instance of SignalConfig describing the given measuring channel (signal).
     * Note that channels numbering starts from 0.
     *
     * @param signalNumber the channel number whose configuration we want to get
     * @return object containing information describing the given measuring channel (signal)
     */
    public SignalConfig getSignalConfig(int signalNumber) {
        return signals.get(signalNumber);
    }


    /**
     * Return the number of measuring channels.
     *
     * @return the number of measuring channels
     */
    public int getNumberOfSignals() {
        return signals.size();
    }


    /**
     * Helper method. cCalculate total number of samples from all channels (signals) in each data record
     *
     * @return sum of samples from all channels
     */
    public int getRecordLength() {
        int totalNumberOfSamplesInRecord = 0;
        for (int i = 0; i < signals.size(); i++) {
            totalNumberOfSamplesInRecord += signals.get(i).getNumberOfSamplesInEachDataRecord();
        }
        return totalNumberOfSamplesInRecord;
    }

    /**
     * Helper method. Give the signal number corresponding to the given sample position
     * inside a DataRecord
     *
     * @param samplePosition sample position within a DataRecord
     * @return signal number corresponding to the given sample position
     */
    public int getSampleSignal(int samplePosition) {
        if (samplePosition < 0) {
            samplePosition = 0;
        }
        if (samplePosition > getRecordLength()) {
            samplePosition = samplePosition % getRecordLength();
        }
        int samplesCounter = 0;
        for (int signalNumber = 0; signalNumber < getNumberOfSignals(); signalNumber++) {
            samplesCounter += getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord();
            if (samplePosition <= samplesCounter) {
                return signalNumber;
            }
        }
        return 0;
    }

    /**
     * Get the number of bytes in the EDF/BDF header record (when we will create it on the base of this HeaderConfig)
     *
     * @return number of bytes in EDF/BDF header = (number of signals + 1) * 256
     */
    public int getNumberOfBytesInHeaderRecord() {
        return 256 + (getNumberOfSignals() * 256);
    }
}
