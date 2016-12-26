package com.biorecorder.edflib.util;

import com.biorecorder.edflib.HeaderConfig;

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
 */

public class PhysicalDigitalConverter {
    private HeaderConfig headerConfig;

    public PhysicalDigitalConverter(HeaderConfig headerConfig) {
        this.headerConfig = headerConfig;
    }


    public void digitalRecordToPhysical(int[] digArray, int digArrayOffset, double[] physArray, int physArrayOffset) {
        int counter = 0;
        for(int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            for(int sampleNumber = 0; sampleNumber < headerConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                physArray[physArrayOffset + counter] = digitalValueToPhysical(digArray[digArrayOffset + counter], signalNumber);
                counter++;
            }
        }
    }

    public void physicalRecordToDigital(double[] physArray, int physArrayOffset, int[] digArray, int digArrayOffset) {
        int counter = 0;
        for(int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            for(int sampleNumber = 0; sampleNumber < headerConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord(); sampleNumber++) {
                digArray[digArrayOffset + counter]  = physicalValueToDigital(physArray[physArrayOffset + counter], signalNumber);
                counter++;
            }
        }
    }

    public void digitalArrayToPhysical(int[] digArray, int digArrayOffset, double[] physArray, int physArrayOffset, int numberOfRecords) {
        int recordLength = headerConfig.getRecordLength();
        for(int i = 0; i < numberOfRecords; i++) {
            digitalRecordToPhysical(digArray, digArrayOffset + recordLength * i, physArray, physArrayOffset + recordLength * i);
        }
    }

    public void physicalArrayToDigital(double[] physArray, int physArrayOffset, int[] digArray, int digArrayOffset, int numberOfRecords) {
        int recordLength = headerConfig.getRecordLength();
        for(int i = 0; i < numberOfRecords; i++) {
            physicalRecordToDigital(physArray, physArrayOffset + recordLength * i, digArray, digArrayOffset + recordLength * i);
        }
    }


    public int[] physicalArrayToDigital(double[] physArray) {
        if(physArray.length % headerConfig.getRecordLength() != 0) {
            String errMsg = "The input array must contain an integer number of DataRecords. Input array length = "
                    + physArray.length + " DataRecord length = " + headerConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        int numberOfRecords = physArray.length / headerConfig.getRecordLength();
        int[] digArray = new int[physArray.length];
        physicalArrayToDigital(physArray, 0, digArray, 0, numberOfRecords);
        return digArray;
    }

    public double[] digitalArrayToPhysical(int[] digArray) {
        if(digArray.length % headerConfig.getRecordLength() != 0) {
            String errMsg = "The input array must contain an integer number of DataRecords. Input array length = "
                    + digArray.length + " DataRecord length = " + headerConfig.getRecordLength();
            throw new IllegalArgumentException(errMsg);
        }
        int numberOfRecords = digArray.length / headerConfig.getRecordLength();
        double[] physArray = new double[digArray.length];
        digitalArrayToPhysical(digArray, 0, physArray, 0,  numberOfRecords);
        return physArray;
    }



    public int physicalValueToDigital(double physValue, int signalNumber) {
        return PhysicalDigitalConverter.physicalValueToDigital(physValue,
                headerConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                headerConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                headerConfig.getSignalConfig(signalNumber).getDigitalMax(),
                headerConfig.getSignalConfig(signalNumber).getDigitalMin());
    }

    public  double digitalValueToPhysical(int digValue, int signalNumber) {
        return PhysicalDigitalConverter.digitalValueToPhysical(digValue,
                headerConfig.getSignalConfig(signalNumber).getPhysicalMax(),
                headerConfig.getSignalConfig(signalNumber).getPhysicalMin(),
                headerConfig.getSignalConfig(signalNumber).getDigitalMax(),
                headerConfig.getSignalConfig(signalNumber).getDigitalMin());
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
