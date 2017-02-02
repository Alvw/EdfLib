package com.biorecorder.edflib.base;

/**
 * Class (data-structure) that allows to store information about measuring channels (signals).
 * It has the following fields (and their corresponding getters and setters):
 *  <ul>
 *     <li>signal label</li>
 *     <li>transducer type (e.g. AgAgCI electrode)</li>
 *     <li>physical dimension(e.g. uV or degree C)</li>
 *     <li>physical minimum (e.g. -500 or 34)</li>
 *     <li>physical maximum (e.g. 500 or 40)</li>
 *     <li>digital minimum (e.g. -2048)</li>
 *     <li>digital maximum (e.g. 2047)</li>
 *     <li>prefiltering (e.g. HP:0.1Hz LP:75Hz)</li>
 *     <li>number of samples in each data record</li>
 * </ul>
 *
 */
public class SignalConfig {
    private int numberOfSamplesInEachDataRecord;
    private String prefiltering = "None";
    private String transducerType = "Unknown";
    private String label = "";
    private int digitalMin = -8388608;
    private int digitalMax = 8388607;
    private double physicalMin = -8388608;
    private double physicalMax = 8388607;
    private String physicalDimension = "";  // uV or Ohm

    public SignalConfig() {
    }

    /**
     * Constructor to make a copy of given SignalConfig instance
     *
     * @param signalConfig SignalConfig instance that will be copied
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
}
