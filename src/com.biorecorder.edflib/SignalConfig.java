package com.biorecorder.edflib;

/**
 *  Class (data-structure) that allows to store information about measuring channel (signal)
 *  required to create EDF/BDF file header and correctly extract data from DataRecords.
 *  It contains number of samples from the channel in each data record, phisical and digital max and min
 *  (to convert physical values into digital and backward),
 *  physical dimension (uV or Ohm), signal label and so on and has
 *  getter and setter methods to set and get that information
 *
 * @see RecordingConfig
 */
public class SignalConfig {
    private  int numberOfSamplesInEachDataRecord;
    private String prefiltering = "None";
    private String transducerType = "Unknown";
    private String label = "";
    private int digitalMin = -8388608;
    private int digitalMax = 8388607;
    private int physicalMin = -8388608;
    private int physicalMax = 8388607;
    private String physicalDimension = "";  // uV or Ohm

    public SignalConfig() {
    }

    /**
     * Constructor to make a copy of given SignalConfig instance
     */
    public SignalConfig(SignalConfig signalConfig) {
        this.numberOfSamplesInEachDataRecord = signalConfig.getNumberOfSamplesInEachDataRecord();
        this.prefiltering = signalConfig.getPrefiltering();
        this.transducerType = signalConfig.getTransducerType();
        this.label = signalConfig.getLabel();
        this.digitalMin = signalConfig.getDigitalMin();
        this.digitalMax = signalConfig.getDigitalMax();
        this.physicalMin = signalConfig.getPhysicalMin();
        this.physicalMax = signalConfig.getPhysicalMax();
        this.physicalDimension = signalConfig.getPhysicalDimension();
    }

    public int getDigitalMin() {
        return digitalMin;
    }

    public int getDigitalMax() {
        return digitalMax;
    }

    public int getPhysicalMin() {
        return physicalMin;
    }

    public int getPhysicalMax() {
        return physicalMax;
    }

    public String getPhysicalDimension() {
        return physicalDimension;
    }

    public int getNumberOfSamplesInEachDataRecord() {
        return numberOfSamplesInEachDataRecord;
    }

    public void setDigitalMin(int digitalMin) {
        this.digitalMin = digitalMin;
    }

    public void setDigitalMax(int digitalMax) {
        this.digitalMax = digitalMax;
    }

    public void setPhysicalMin(int physicalMin) {
        this.physicalMin = physicalMin;
    }

    public void setPhysicalMax(int physicalMax) {
        this.physicalMax = physicalMax;
    }

    public void setPhysicalDimension(String physicalDimension) {
        this.physicalDimension = physicalDimension;
    }


    public void setNumberOfSamplesInEachDataRecord(int numberOfSamplesInEachDataRecord) {
        this.numberOfSamplesInEachDataRecord = numberOfSamplesInEachDataRecord;
    }

    public String getPrefiltering() {
        return prefiltering;
    }

    public void setPrefiltering(String prefiltering) {
        this.prefiltering = prefiltering;
    }

    public String getTransducerType() {
        return transducerType;
    }

    public void setTransducerType(String transducerType) {
        this.transducerType = transducerType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
