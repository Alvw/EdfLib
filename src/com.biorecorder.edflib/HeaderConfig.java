package com.biorecorder.edflib;

import java.util.ArrayList;

/**
 * Created by gala on 21/12/16.
 */
public class HeaderConfig {
    private String patientId = "Default patient";
    private String recordingId = "Default record";
    private long startTime = -1;
    private int numberOfDataRecords = -1;
    private double durationOfDataRecord;
    private ArrayList<SignalConfig> signals = new ArrayList<SignalConfig>();

    public HeaderConfig() {

    }

    /**
     * Constructor to make a copy of given HeaderConfig
     */
    public HeaderConfig(HeaderConfig headerConfig) {
        patientId = headerConfig.getPatientId();
        recordingId = headerConfig.getRecordingId();
        startTime = headerConfig.getStartTime();
        durationOfDataRecord = headerConfig.getDurationOfDataRecord();
        numberOfDataRecords = headerConfig.getNumberOfDataRecords();
        for(int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            signals.add(new SignalConfig(headerConfig.getSignalConfig(i)));
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

    public void addSignalConfig(SignalConfig signalConfig) {
        signals.add(signalConfig);
    }

    public SignalConfig getSignalConfig(int number) {
        return signals.get(number);
    }


    public int getNumberOfSignals() {
        return signals.size();
    }


    /**
     *  get number of samples in each data record
     */
    public int getRecordLength() {
        int totalNumberOfSamplesInRecord = 0;
        for(int i = 0; i < signals.size(); i++) {
            totalNumberOfSamplesInRecord += signals.get(i).getNumberOfSamplesInEachDataRecord();
        }
        return totalNumberOfSamplesInRecord;
    }

    public int getNumberOfBytesInHeader() {
        return 256 + (getNumberOfSignals() * 256);
    }
}
