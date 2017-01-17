package com.biorecorder.edflib.util;

import com.biorecorder.edflib.RecordingConfig;
import com.biorecorder.edflib.SignalConfig;

/**
 * This class contains methods to convert physical (floating point) value to digital (integer) and
 * physical DataRecord to digital DataRecord and vice versa.
 *
 * <p>EDF/BDF format assumes a linear relationship between physical and digital values,
 * and the scaling factor for every channel is calculated  on the on the base of its
 * <b> physical minimum and maximum </b> and the corresponding <b> digital minimum and maximum </b>
 * specified in {@link com.biorecorder.edflib.SignalConfig}. So:
 *
 * <p>(physValue - physMin) / (digValue - digMin)  = constant [Gain] = (physMax - physMin) / (digMax - digMin)
 *
 * <p>physValue = digValue * gain + offset
 * <br>digValue = (physValue - offset) / gain
 *
 *  <p>where:
 *  <br>gain = (physMax - physMin) / (digMax - digMin)
 *  <br>offset = (physMin - digMin * gain);
 *
 * <p>More detailed information about EDF/BDF format:
 * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
 * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <br><a href="http://www.edfplus.info/specs/edffloat.html">EDF. How to store longintegers and floats</a>
 *
 * @see RecordingConfig
 * @see SignalConfig
 */

public class PhysicalDigitalConverter {
    private RecordingConfig recordingConfig;

    /**
     *  Create PhysicalDigitalConverter to make physical-digital conversion of DataRecords
     *  on the base of information stored in the given {@link RecordingConfig}
     *
     * @param recordingConfig - contains information about DataRecords structure,
     *                        physical and digital minimums and maximums for all its channels
     */
    public PhysicalDigitalConverter(RecordingConfig recordingConfig) {
        this.recordingConfig = recordingConfig;
    }


    /**
     * Convert digital DataRecord to physical. Digital DataRecord are taken from digArray starting
     * at digArrayOffset position.
     *
     * @param digArray - array with digital data
     * @param digArrayOffset - offset within the array at which the digital DataRecord starts
     * @return physical DataRecord
     */
    public double[] digitalRecordToPhysical(int[] digArray, int digArrayOffset) {
        double[] physicalDataRecord = new double[recordingConfig.getRecordLength()];
        int counter = 0;
        for(int signalNumber = 0; signalNumber < recordingConfig.getNumberOfSignals(); signalNumber++) {
            for(int sampleNumber = 0; sampleNumber < recordingConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                physicalDataRecord[counter] = digitalValueToPhysical(digArray[digArrayOffset + counter], signalNumber);
                counter++;
            }
        }
        return physicalDataRecord;
    }


    /**
     * Convert physical DataRecord to digital. Physical DataRecord are taken from physArray starting
     * at physArrayOffset position.
     *
     * @param physArray - array with physical data
     * @param physArrayOffset - - offset within the array at which the physical DataRecord starts
     * @return digital DataRecord
     */
    public int[] physicalRecordToDigital(double[] physArray, int physArrayOffset) {
        int[] digitalDataRecord = new int[recordingConfig.getRecordLength()];
        int counter = 0;
        for(int signalNumber = 0; signalNumber < recordingConfig.getNumberOfSignals(); signalNumber++) {
            for(int sampleNumber = 0; sampleNumber < recordingConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                digitalDataRecord[counter]  = physicalValueToDigital(physArray[physArrayOffset + counter], signalNumber);
                counter++;
            }
        }
        return digitalDataRecord;
    }

    /**
     * Convert physical DataRecord to digital.
     *
     * @param physDataRecord - physical DataRecord
     * @return digital DataRecord
     * @exception IllegalArgumentException in the case of wrong length of  DataRecord
     */
    public int[] physicalRecordToDigital(double[] physDataRecord) {
        if(physDataRecord.length != recordingConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + physDataRecord.length + " DataRecord length = " + recordingConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        return physicalRecordToDigital(physDataRecord, 0);
    }

    /**
     * Convert digital DataRecord to physical.
     *
     * @param digDataRecord - digital DataRecord
     * @return physical DataRecord
     * @exception IllegalArgumentException in the case of wrong length of  DataRecord
     */
    public double[] digitalRecordToPhysical(int[] digDataRecord) {
        if(digDataRecord.length != recordingConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + digDataRecord.length + " DataRecord length = " + recordingConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        return digitalRecordToPhysical(digDataRecord, 0);
    }

    /**
     * Convert physical value from the given channel to digital
     *
     * @param physValue - physical value
     * @param signalNumber - number of the channel (signal) which this value belongs to
     * @return digital value
     */

    public int physicalValueToDigital(double physValue, int signalNumber) {
        return PhysicalDigitalConverter.physicalValueToDigital(physValue,
                recordingConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                recordingConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                recordingConfig.getSignalConfig(signalNumber).getDigitalMax(),
                recordingConfig.getSignalConfig(signalNumber).getDigitalMin());
    }

    /**
     * Convert digital value from the given channel to physical
     *
     * @param digValue - digital value
     * @param signalNumber - number of the channel (signal) which this value belongs to
     * @return physical value
     */

    public  double digitalValueToPhysical(int digValue, int signalNumber) {
        return PhysicalDigitalConverter.digitalValueToPhysical(digValue,
                recordingConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                recordingConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                recordingConfig.getSignalConfig(signalNumber).getDigitalMax(),
                recordingConfig.getSignalConfig(signalNumber).getDigitalMin());
    }

    /**
     * This static method converts physical value to digital on the base
     * of physical and digital maximums and minimums
     *
     * @param physValue - physical value
     * @param physMax - physical maximum
     * @param physMin - physical minimum
     * @param digMax - digital maximum
     * @param digMin - digital minimum
     * @return digital value
     */
    public static int physicalValueToDigital(double physValue, double physMax, double physMin, int digMax, int digMin) {
       return  (int) ((physValue - offset(physMax, physMin, digMax, digMin)) / gain(physMax, physMin, digMax, digMin));

    }

    /**
     * This static method converts digital value to physical on the base
     * of physical and digital maximums and minimums
     *
     * @param digValue - digital value
     * @param physMax - physical maximum
     * @param physMin - physical minimum
     * @param digMax - digital maximum
     * @param digMin - digital minimum
     * @return physical value
     */
    public static double digitalValueToPhysical(int digValue, double physMax, double physMin, int digMax, int digMin) {
        return digValue * gain(physMax, physMin, digMax, digMin) + offset(physMax, physMin, digMax, digMin);

    }


    /**
     * This static method calculate the gain on the base
     * of physical and digital maximums and minimums
     *
     * @param physMax - physical maximum
     * @param physMin - physical minimum
     * @param digMax - digital maximum
     * @param digMin - digital minimum
     * @return gain = (physMax - physMin) / (digMax - digMin)
     */
    public static double gain(double physMax, double physMin, int digMax, int digMin) {
        return (physMax - physMin) / (digMax - digMin);

    }

    /**
     * This static method calculate the offset on the base
     * of physical and digital maximums and minimums
     *
     * @param physMax - physical maximum
     * @param physMin - physical minimum
     * @param digMax - digital maximum
     * @param digMin - digital minimum
     * @return offset = (physMin - digMin * gain) where
     *  gain = (physMax - physMin) / (digMax - digMin)
     */
    public static double offset(double physMax, double physMin, int digMax, int digMin) {
        return physMin - digMin * gain(physMax, physMin, digMax, digMin);

    }
}
