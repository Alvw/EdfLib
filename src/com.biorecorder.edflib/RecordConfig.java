package com.biorecorder.edflib;

import java.util.ArrayList;

/**
 *  Class (data-structure) that allows to store information required to create EDF/BDF file header and
 *  correctly extract data from DataRecords.
 *  It describes DataRecords structure (duration of DataRecords,
 *  number of channels or signals,  signals configuration) and other
 *  significant data  about the experiment (patient info, startTime and so on) and has
 *  getter and setter methods to set and get that information
 *
 * <p>Detailed information about EDF/BDF file header:
 * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
 * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 *
 * @see SignalConfig
 */
public class RecordConfig {
    private String patientId = "Default patient";
    private String recordingId = "Default record";
    private long startTime = -1;
    private int numberOfDataRecords = -1;
    private double durationOfDataRecord;
    private ArrayList<SignalConfig> signals = new ArrayList<SignalConfig>();

    public RecordConfig() {

    }

    /**
     * Constructor to make a copy of given RecordConfig instance
     *
     * @param recordConfig RecordConfig instance that will be copied
     */
    public RecordConfig(RecordConfig recordConfig) {
        patientId = recordConfig.getPatientId();
        recordingId = recordConfig.getRecordingId();
        startTime = recordConfig.getStartTime();
        durationOfDataRecord = recordConfig.getDurationOfDataRecord();
        numberOfDataRecords = recordConfig.getNumberOfDataRecords();
        for(int i = 0; i < recordConfig.getNumberOfSignals(); i++) {
            signals.add(new SignalConfig(recordConfig.getSignalConfig(i)));
        }
    }


    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = recordingId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
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
     *
     * RecordConfig must include SignalConfigs describing all measuring channels (signals).
     * And we have to add them successively.
     *
     * @param signalConfig  SignalConfig instance describing parameters of the measuring channel
     *
     */
    public void addSignalConfig(SignalConfig signalConfig) {
        signals.add(signalConfig);
    }

    public void removeAllSignalConfig() {
        signals = new ArrayList<SignalConfig>();
    }



    public SignalConfig getSignalConfig(int number) {
        return signals.get(number);
    }


    public int getNumberOfSignals() {
        return signals.size();
    }


    /**
     *  calculate total number of samples from all channels (signals) in each data record
     *
     *  @return  sum of samples from all channels
     */
    public int getRecordLength() {
        int totalNumberOfSamplesInRecord = 0;
        for(int i = 0; i < signals.size(); i++) {
            totalNumberOfSamplesInRecord += signals.get(i).getNumberOfSamplesInEachDataRecord();
        }
        return totalNumberOfSamplesInRecord;
    }

    /**
     * calculate number of bytes in EDF/BDF header that will be created on the base of this RecordConfig
     *
     * @return number of bytes in EDF/BDF header = (number of signals + 1) * 256
     */
    public int getNumberOfBytesInHeader() {
        return 256 + (getNumberOfSignals() * 256);
    }
}
