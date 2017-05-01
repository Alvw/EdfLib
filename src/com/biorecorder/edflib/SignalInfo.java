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
class SignalInfo {
    private int numberOfSamplesInEachDataRecord;
    private String prefiltering = "None";
    private String transducerType = "Unknown";
    private String label = "";
    private int digitalMin = FileType.EDF_16BIT.getDigitalMin();
    private int digitalMax = FileType.EDF_16BIT.getDigitalMax();
    private double physicalMin = FileType.EDF_16BIT.getDigitalMin();
    private double physicalMax = FileType.EDF_16BIT.getDigitalMax();;
    private String physicalDimension = "";  // uV or Ohm


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

    public void setDigitalRange(int digitalMin, int digitalMax) {
        if(digitalMax <= digitalMin) {
            throw new RuntimeException("DigitalMax must be > digitalMin!. DigitalMax = "
                    + digitalMax + " DigitalMin = "+digitalMin);
        }
        this.digitalMin = digitalMin;
        this.digitalMax = digitalMax;
    }


    public void setPhysicalRange(double physicalMin, double physicalMax) {
        if(physicalMax <= physicalMin) {
            throw new RuntimeException("physicalMax must be > physicalMin!. PhysicalMax = "
                    + physicalMax+" PhysicalMin = " + physicalMin);
        }
        this.physicalMin = physicalMin;
        this.physicalMax = physicalMax;
    }

    public void setPhysicalDimension(String physicalDimension) {
        this.physicalDimension = physicalDimension;
    }


    public void setNumberOfSamplesInEachDataRecord(int numberOfSamplesInEachDataRecord) {
       if(numberOfSamplesInEachDataRecord <= 0) {
           throw new RuntimeException("Number of samples in each DataRecord = "+ numberOfSamplesInEachDataRecord
           + ".  Must be > 0!");

       }
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
        Long value =  Math.round((physValue - offset()) / gain());
        return value.intValue();

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
