package com.biorecorder.edflib.util;

import com.biorecorder.edflib.RecordConfig;

/**
 *  EDF format assumes that:
 * (physValue - physMin) / (digValue - digMin)  = constant [Gain] = (physMax - physMin) / (digMax - digMin)
 *
 *  physValue = digValue * gain + offset
 *  digValue = (physValue - offset) / gain
 *
 *  where:
 *  gain = (physMax - physMin) / (digMax - digMin)
 *  offset = (physMin - digMin * gain);
 *
 *  @see <a href="http://www.edfplus.info/specs/edffloat.html">EDF. How to store longintegers and floats</a>
 *
 */

public class PhysicalDigitalConverter {
    private RecordConfig recordConfig;

    public PhysicalDigitalConverter(RecordConfig recordConfig) {
        this.recordConfig = recordConfig;
    }


    public void digitalRecordToPhysical(int[] digArray, int digArrayOffset, double[] physArray, int physArrayOffset) {
        int counter = 0;
        for(int signalNumber = 0; signalNumber < recordConfig.getNumberOfSignals(); signalNumber++) {
            for(int sampleNumber = 0; sampleNumber < recordConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                physArray[physArrayOffset + counter] = digitalValueToPhysical(digArray[digArrayOffset + counter], signalNumber);
                counter++;
            }
        }
    }

    public void physicalRecordToDigital(double[] physArray, int physArrayOffset, int[] digArray, int digArrayOffset) {
        int counter = 0;
        for(int signalNumber = 0; signalNumber < recordConfig.getNumberOfSignals(); signalNumber++) {
            for(int sampleNumber = 0; sampleNumber < recordConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                digArray[digArrayOffset + counter]  = physicalValueToDigital(physArray[physArrayOffset + counter], signalNumber);
                counter++;
            }
        }
    }

    public int[] physicalRecordToDigital(double[] physArray) {
        if(physArray.length != recordConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + physArray.length + " DataRecord length = " + recordConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        int[] digArray = new int[physArray.length];
        physicalRecordToDigital(physArray, 0, digArray, 0);
        return digArray;
    }

    public double[] digitalRecordToPhysical(int[] digArray) {
        if(digArray.length != recordConfig.getRecordLength()) {
            String errMsg = "The input array length must be equal DataRecord length. Input array length = "
                    + digArray.length + " DataRecord length = " + recordConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        double[] physArray = new double[digArray.length];
        digitalRecordToPhysical(digArray, 0, physArray, 0);
        return physArray;
    }



    public int physicalValueToDigital(double physValue, int signalNumber) {
        return PhysicalDigitalConverter.physicalValueToDigital(physValue,
                recordConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                recordConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                recordConfig.getSignalConfig(signalNumber).getDigitalMax(),
                recordConfig.getSignalConfig(signalNumber).getDigitalMin());
    }

    public  double digitalValueToPhysical(int digValue, int signalNumber) {
        return PhysicalDigitalConverter.digitalValueToPhysical(digValue,
                recordConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                recordConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                recordConfig.getSignalConfig(signalNumber).getDigitalMax(),
                recordConfig.getSignalConfig(signalNumber).getDigitalMin());
    }

    public static int physicalValueToDigital(double physValue, double physMax, double physMin, int digMax, int digMin) {
       return  (int) ((physValue - offset(physMax, physMin, digMax, digMin)) / gain(physMax, physMin, digMax, digMin));

    }

    public static double digitalValueToPhysical(int digValue, double physMax, double physMin, int digMax, int digMin) {
        return digValue * gain(physMax, physMin, digMax, digMin) + offset(physMax, physMin, digMax, digMin);

    }

    private static double gain(double physMax, double physMin, int digMax, int digMin) {
        return (physMax - physMin) / (digMax - digMin);

    }

    private static double offset(double physMax, double physMin, int digMax, int digMin) {
        return physMin - digMin * gain(physMax, physMin, digMax, digMin);

    }
}
