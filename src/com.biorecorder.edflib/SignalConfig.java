package com.biorecorder.edflib;

/**
 *
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
