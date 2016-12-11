package com.biorecorder.edflib;



public class BdfHeader  {

    private String patientId = "Default patient";
    private String recordingId = "Default record";
    private long startTime = -1;
    private int numberOfDataRecords = -1;
    private double durationOfDataRecord;
    private SignalConfig[]  signals;
    private boolean isBdf;

    public boolean isBdf() {
        return isBdf;
    }

    public void setBdf(boolean bdf) {
        isBdf = bdf;
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

    public SignalConfig[] getSignals() {
        return signals;
    }

    public void setSignals(SignalConfig[] signals) {
        this.signals = signals;
    }

    public BdfHeader copy() {
        BdfHeader resultingBdfHeader = new BdfHeader();
        resultingBdfHeader.setBdf(isBdf());
        resultingBdfHeader.setPatientId(getPatientId());
        resultingBdfHeader.setRecordingId(getRecordingId());
        resultingBdfHeader.setStartTime(getStartTime());
        resultingBdfHeader.setDurationOfDataRecord(getDurationOfDataRecord());
        SignalConfig[] originalSignalConfigs = getSignals();
        int length = originalSignalConfigs.length;
        SignalConfig[] resultingSignalsConfigs = new SignalConfig[length];
        for(int i = 0; i < length; i++) {
            resultingSignalsConfigs[i]  = originalSignalConfigs[i].copy();
        }
        resultingBdfHeader.setSignals(resultingSignalsConfigs);
        return resultingBdfHeader;
    }

}
