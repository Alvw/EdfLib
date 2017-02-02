package com.biorecorder.edflib.base;

/**
 * This class contains methods to convert physical (floating point) value to digital (integer) and
 * physical DataRecord to digital DataRecord and vice versa.
 * <p>
 * EDF/BDF format assumes a linear relationship between physical and digital values,
 * and the scaling factor (gain) for every channel is calculated on the base of the
 * <b> physical minimum and maximum </b> and the corresponding <b> digital minimum and maximum </b>
 * specified for each channel (see {@link SignalConfig}). So:
 * <p>
 * (physValue - physMin) / (digValue - digMin)  = constant [Gain] = (physMax - physMin) / (digMax - digMin)
 * <p>
 * physValue = digValue * gain + offset
 * <br>digValue = (physValue - offset) / gain
 * <p>
 * where:
 * <br>gain = (physMax - physMin) / (digMax - digMin)
 * <br>offset = (physMin - digMin * gain);
 */

public class PhysicalDigitalConverter {
    private HeaderConfig headerConfig;

    /**
     * Create PhysicalDigitalConverter to make physical-digital conversion of DataRecords
     * on the base of information stored in the given {@link HeaderConfig}
     *
     * @param headerConfig object containing the information about DataRecords structure,
     *                        and physical and digital minimums and maximums for all channels
     */
    public PhysicalDigitalConverter(HeaderConfig headerConfig) {
        this.headerConfig = headerConfig;
    }


    /**
     * Convert digital DataRecord to physical. Digital DataRecord are taken from digArray starting
     * at digArrayOffset position.
     *
     * @param digArray        array with digital data
     * @param digArrayOffset  offset within the array at which the digital DataRecord starts
     * @return physical DataRecord
     */
    public double[] digitalRecordToPhysical(int[] digArray, int digArrayOffset) {
        double[] physicalDataRecord = new double[headerConfig.getRecordLength()];
        int counter = 0;
        for (int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            for (int sampleNumber = 0; sampleNumber < headerConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                physicalDataRecord[counter] = signalDigitalValuesToPhysical(signalNumber, digArray[digArrayOffset + counter])[0];
                counter++;
            }
        }
        return physicalDataRecord;
    }


    /**
     * Convert physical DataRecord to digital. Physical DataRecord are taken from physArray starting
     * at physArrayOffset position.
     *
     * @param physArray        array with physical data
     * @param physArrayOffset  offset within the array at which the physical DataRecord starts
     * @return digital DataRecord
     */
    public int[] physicalRecordToDigital(double[] physArray, int physArrayOffset) {
        int[] digitalDataRecord = new int[headerConfig.getRecordLength()];
        int counter = 0;
        for (int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            for (int sampleNumber = 0; sampleNumber < headerConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                digitalDataRecord[counter] = signalPhysicalValuesToDigital(signalNumber, physArray[physArrayOffset + counter])[0];
                counter++;
            }
        }
        return digitalDataRecord;
    }

    /**
     * Convert physical DataRecord to digital.
     *
     * @param physDataRecord  physical DataRecord
     * @return digital DataRecord
     * @throws IllegalArgumentException in the case of wrong length of DataRecord
     */
    public int[] physicalRecordToDigital(double[] physDataRecord) {
        if (physDataRecord.length != headerConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + physDataRecord.length + " DataRecord length = " + headerConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        return physicalRecordToDigital(physDataRecord, 0);
    }

    /**
     * Convert digital DataRecord to physical.
     *
     * @param digDataRecord  digital DataRecord
     * @return physical DataRecord
     * @throws IllegalArgumentException in the case of wrong length of  DataRecord
     */
    public double[] digitalRecordToPhysical(int[] digDataRecord) {
        if (digDataRecord.length != headerConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + digDataRecord.length + " DataRecord length = " + headerConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        return digitalRecordToPhysical(digDataRecord, 0);
    }

    /**
     * Convert physical values from given channel to digital values
     *
     * @param signalNumber channel (signal) number this value belongs to
     * @param physValues   physical values to be converted to digital values
     * @return array with corresponding digital values
     */

    public int[] signalPhysicalValuesToDigital(int signalNumber, double... physValues) {
        int[] digValues = new int[physValues.length];
        for (int i = 0; i < physValues.length; i++) {
            digValues[i] = PhysicalDigitalConverter.physicalValueToDigital(physValues[i],
                    headerConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                    headerConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                    headerConfig.getSignalConfig(signalNumber).getDigitalMax(),
                    headerConfig.getSignalConfig(signalNumber).getDigitalMin());
        }
        return digValues;
    }

    /**
     * Convert digital values from given channel to physical values
     *
     * @param signalNumber channel (signal) number this value belongs to
     * @param digValues    digital values to be converted to physical values
     * @return array with corresponding physical values
     */

    public double[] signalDigitalValuesToPhysical(int signalNumber, int... digValues) {
        double[] physValues = new double[digValues.length];
        for (int i = 0; i < digValues.length; i++) {
            physValues[i] = PhysicalDigitalConverter.digitalValueToPhysical(digValues[i],
                    headerConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                    headerConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                    headerConfig.getSignalConfig(signalNumber).getDigitalMax(),
                    headerConfig.getSignalConfig(signalNumber).getDigitalMin());
        }
        return physValues;
    }

    /**
     * This static method converts physical value to digital on the base
     * of physical and digital maximums and minimums
     *
     * @param physValue physical value
     * @param physMax   physical maximum
     * @param physMin   physical minimum
     * @param digMax    digital maximum
     * @param digMin    digital minimum
     * @return digital value
     */
    public static int physicalValueToDigital(double physValue, double physMax, double physMin, int digMax, int digMin) {
        return (int) ((physValue - offsetCalibrationFactor(physMax, physMin, digMax, digMin)) / gainCalibrationFactor(physMax, physMin, digMax, digMin));

    }

    /**
     * This static method converts digital value to physical on the base
     * of physical and digital maximums and minimums
     *
     * @param digValue digital value
     * @param physMax  physical maximum
     * @param physMin  physical minimum
     * @param digMax   digital maximum
     * @param digMin   digital minimum
     * @return physical value
     */
    public static double digitalValueToPhysical(int digValue, double physMax, double physMin, int digMax, int digMin) {
        return digValue * gainCalibrationFactor(physMax, physMin, digMax, digMin) + offsetCalibrationFactor(physMax, physMin, digMax, digMin);

    }


    /**
     * This static method calculate the gain calibration (adjust) factor on the base
     * of physical and digital maximums and minimums
     *
     * @param physMax physical maximum
     * @param physMin physical minimum
     * @param digMax  digital maximum
     * @param digMin  digital minimum
     * @return gain = (physMax - physMin) / (digMax - digMin)
     */
    public static double gainCalibrationFactor(double physMax, double physMin, int digMax, int digMin) {
        return (physMax - physMin) / (digMax - digMin);

    }

    /**
     * This static method calculate the offset calibration (adjust) factor on the base
     * of physical and digital maximums and minimums
     *
     * @param physMax physical maximum
     * @param physMin physical minimum
     * @param digMax  digital maximum
     * @param digMin  digital minimum
     * @return offset = (physMin - digMin * gain) where
     * gain = (physMax - physMin) / (digMax - digMin)
     */
    public static double offsetCalibrationFactor(double physMax, double physMin, int digMax, int digMin) {
        return physMin - digMin * gainCalibrationFactor(physMax, physMin, digMax, digMin);

    }
}
