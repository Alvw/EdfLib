package com.biorecorder.edflib.util;

/**
 * Created by mac on 06/11/14.
 */
public class BdfParser {


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

    public static byte[] intArrayToByteArray(int[] intData, int offset, int length, int numberOfBytesPerInt) {
        if(numberOfBytesPerInt > 4) {
            numberOfBytesPerInt = 4;
        }
        byte[] result = new byte[length * numberOfBytesPerInt];
        for (int i = 0; i < length; i++) {
            byte[] intBytes = intToLittleEndianByteArray(intData[i + offset]);
            for(int byteNumber = 0; byteNumber < numberOfBytesPerInt; byteNumber++) {
                result[i*numberOfBytesPerInt + byteNumber] = intBytes[byteNumber];
            }
        }
        return result;
    }

    public static byte[] intArrayToByteArray(int[] intData, int numberOfBytesPerInt) {
       return intArrayToByteArray(intData, 0, intData.length, numberOfBytesPerInt);
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

    /**
     * convert Big_endian int (java)  to Little_endian  byte array (for bdf)
     */
    public static  byte[] intToLittleEndianByteArray(int value) {
        return new byte[]{
                (byte) value,
                (byte) (value >>> 8),
                (byte) (value >>> 16),
                (byte) (value >>> 24) };
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

}
