package com.biorecorder.edflib;

/**
 *
 */
public class SignalConfig {
    private  int numberOfSamplesInEachDataRecord;
    private String prefiltering = "None";
    private String transducerType = "Unknown";
    private String label = "";
    private int digitalMin = Integer.MIN_VALUE;
    private int digitalMax = Integer.MAX_VALUE;
    private double physicalMin = Integer.MIN_VALUE;
    private double physicalMax = Integer.MAX_VALUE;
    private String physicalDimension = "";  // uV or degreeC


    public int getDigitalMin() {
        return digitalMin;
    }

    public int getDigitalMax() {
        return digitalMax;
    }

    public double getPhysicalMin() {
        return physicalMin;
    }

    public double getPhysicalMax() {
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

    public void setPhysicalMin(double physicalMin) {
        this.physicalMin = physicalMin;
    }

    public void setPhysicalMax(double physicalMax) {
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

    public SignalConfig copy() {
        SignalConfig resultingSignalConfig = new SignalConfig();
        resultingSignalConfig.setDigitalMax(getDigitalMax());
        resultingSignalConfig.setDigitalMin(getDigitalMin());
        resultingSignalConfig.setPhysicalMax(getPhysicalMax());
        resultingSignalConfig.setPhysicalMin(getPhysicalMin());
        resultingSignalConfig.setLabel(getLabel());
        resultingSignalConfig.setPrefiltering(getPrefiltering());
        resultingSignalConfig.setTransducerType(getTransducerType());
        resultingSignalConfig.setPhysicalDimension(getPhysicalDimension());
        resultingSignalConfig.setNumberOfSamplesInEachDataRecord(getNumberOfSamplesInEachDataRecord());
        return resultingSignalConfig;
    }
}
