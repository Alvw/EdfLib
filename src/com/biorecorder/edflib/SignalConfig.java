package com.biorecorder.edflib;

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
 */
class SignalConfig {
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

    public String getTransducer() {
        return transducerType;
    }

    public void setTransducer(String transducerType) {
        this.transducerType = transducerType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Calculate the gain calibration (adjust) factor on the base
     * of physical and digital maximums and minimums
     *
     * @return gain = (physMax - physMin) / (digMax - digMin)
     */
    public  double gain() {
        return (physicalMax - physicalMin) / (digitalMax - digitalMin);

    }

    /**
     * Calculate the offset calibration (adjust) factor on the base
     * of physical and digital maximums and minimums
     *
     * @return offset = (physMin - digMin * gain) where
     * gain = (physMax - physMin) / (digMax - digMin)
     */
    public double offset() {
        return physicalMin - digitalMin * gain();

    }

    /**
     * Convert physical value to digital on the base
     * of physical and digital maximums and minimums (gain and offset)
     *
     * @return digital value
     */
    public int physicalValueToDigital(double physValue) {
        return (int) ((physValue - offset()) / gain());

    }

    /**
     * Convert digital value to physical on the base
     * of physical and digital maximums and minimums (gain and offset)
     *
     * @return physical value
     */
    public  double digitalValueToPhysical(int digValue) {
        return digValue * gain() + offset();

    }

}
