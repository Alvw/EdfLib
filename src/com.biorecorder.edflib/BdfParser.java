package com.biorecorder.edflib;

/**
 * Created by mac on 06/11/14.
 */
public class BdfParser {
    private int numberOfBytesInDataFormat;
    private int[] signalNumberOfSamplesInEachDataRecords;


    public BdfParser(int numberOfBytesInDataFormat, int[] signalNumberOfSamplesInEachDataRecords) {
        this.numberOfBytesInDataFormat = numberOfBytesInDataFormat;
        this.signalNumberOfSamplesInEachDataRecords = signalNumberOfSamplesInEachDataRecords;
    }

    public static byte[] intArrayToByteArray(int[] intData, int numberOfBytesPerInt) {
        if(numberOfBytesPerInt > 4) {
            numberOfBytesPerInt = 4;
        }
        byte[] result = new byte[intData.length * numberOfBytesPerInt];
        for (int i = 0; i < intData.length; i++) {
            byte[] intBytes = intToBytes(intData[i]);
            for(int byteNumber = 0; byteNumber < numberOfBytesPerInt; byteNumber++) {
                result[i*numberOfBytesPerInt + byteNumber] = intBytes[byteNumber];
            }
        }
        return result;
    }

    public static int[] byteArrayToIntArray(byte[] byteData, int numberOfBytesPerInt) {
        if(numberOfBytesPerInt > 4) {
            numberOfBytesPerInt = 4;
        }
        int[] result = new int[byteData.length / numberOfBytesPerInt];
        for (int index = 0; index < result.length; index++) {
            int i = index * numberOfBytesPerInt;
            switch (numberOfBytesPerInt) {
                case 1:
                    result[index] = bytesToSignedInt(byteData[i]);
                    break;
                case 2:
                    result[index] = bytesToSignedInt(byteData[i], byteData[i + 1]);
                    break;
                case 3:
                     result[index] = bytesToSignedInt(byteData[i], byteData[i + 1],  byteData[i + 2]);
                     break;
                default:
                    result[index] = bytesToSignedInt(byteData[i], byteData[i + 1],  byteData[i + 2], byteData[i + 3]);

            }
        }
        return result;
    }


    public int parseDataRecordSample(byte[] bdfDataRecord, int sampleNumber) {
        if (numberOfBytesInDataFormat == 3) {  //bdf format
            return bytesToSignedInt(bdfDataRecord[sampleNumber * 3],
                    bdfDataRecord[sampleNumber * 3 + 1], bdfDataRecord[sampleNumber * 3 + 2]);
        }
        if (numberOfBytesInDataFormat == 2) {   // edf format
            return bytesToSignedInt(bdfDataRecord[sampleNumber * 2], bdfDataRecord[sampleNumber * 2 + 1]);
        }
        return 0;
    }

    public int[][] parseDataRecord(byte[] bdfDataRecord) {
        int numberOfSignals = signalNumberOfSamplesInEachDataRecords.length;
        int[][] result = new int[numberOfSignals][];
        for (int i = 0; i < numberOfSignals; i++) {
            result[i] = parseDataRecordSignal(bdfDataRecord, i);
        }
        return result;
    }

    public int[] parseDataRecordSignal(byte[] bdfDataRecord, int signalNumber) {
        int numberOfSamples = signalNumberOfSamplesInEachDataRecords[signalNumber];
        int startIndex = getSignalStartIndexInDataRecord(signalNumber);
        int[] result = new int[numberOfSamples];
        for (int i = 0; i < numberOfSamples; i++) {
            result[i] = parseDataRecordSample(bdfDataRecord, startIndex + i);
        }
        return result;
    }

    private int getSignalStartIndexInDataRecord(int signalNumber) {
        int startIndex = 0;
        for (int i = 0; i < signalNumber; i++) {
            startIndex += signalNumberOfSamplesInEachDataRecords[i];
        }
        return startIndex;
    }


    private static byte[] addInt3ToByteArray(int value, byte[] array, int startIndex) {
        array[startIndex] = (byte) (value & 0xff);
        array[startIndex + 1] = (byte) (value >> 8 & 0xff);
        array[startIndex + 2] = (byte) (value >> 16);
        return array;
    }

    private static byte[] addInt2ToByteArray(int value, byte[] array, int startIndex) {
        array[startIndex] = (byte) (value & 0xff);
        array[startIndex + 1] = (byte) (value >> 8);
        return array;
    }


    /* Java int BIG_ENDIAN, Byte order: LITTLE_ENDIAN  */
    public static int bytesToSignedInt(byte... b) {
        switch (b.length) {
            case 1:
                return b[0];
            case 2:
                return (b[1] << 8) | (b[0] & 0xFF);
            case 3:
                return (b[2] << 16) | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
            default:
                return (b[3] << 24) | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
        }
    }

    public static int bytesToUnsignedInt(byte... b) {
        switch (b.length) {
            case 1:
                return (b[0] & 0xFF);
            case 2:
                return (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
            case 3:
                return (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
            default:
                return (b[3] & 0xFF) << 24 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
        }
    }

    public static  byte[] intToBytes(int value) {
        return new byte[]{
                (byte) value,
                (byte) (value >>> 8),
                (byte) (value >>> 16),
                (byte) (value >>> 24) };
    }

}
