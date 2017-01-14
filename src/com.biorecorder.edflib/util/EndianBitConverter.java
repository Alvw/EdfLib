package com.biorecorder.edflib.util;


/**
 * Contains several useful static methods to convert Java integer (32-bit, signed, BIG_ENDIAN)
 * to an array of bytes (BIG_ENDIAN or LITTLE_ENDIAN ordered) and vice versa
 */
public class EndianBitConverter {


    /**
     * convert BIG_ENDIAN java integer to BIG_ENDIAN ordered 4 byte array.
     *
     * @param value the value to be converted to byte array, standard 32-bit signed java int (BIG_ENDIAN)
     *
     * @return resultant 4 byte array (BIG_ENDIAN ordered)
     */

    public static  byte[] intToBytes(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};

    }

    /**
     * convert BIG_ENDIAN java integer to LITTLE_ENDIAN ordered 4 byte array.
     *
     * @param value the value to be converted to byte array, standard 32-bit signed java int (BIG_ENDIAN)
     *
     * @return resultant 4 byte array (LITTLE_ENDIAN ordered)
     */
    public static  byte[] intToLittleEndianBytes(int value) {
        return new byte[]{
                (byte) value,
                (byte) (value >>> 8),
                (byte) (value >>> 16),
                (byte) (value >>> 24) };
    }

    /**
     * Convert BIG_ENDIAN java int to LITTLE_ENDIAN ordered byte array.
     * Resultant array will contain specified number of bytes: 4, 3, 2 or 1.
     *
     * <p>3 bytes LITTLE_ENDIAN data format is used to write/read BDF files
     * <br>2 bytes LITTLE_ENDIAN data format is used to write/read EDF files
     *
     * @param value the value to be converted to byte array, standard 32-bit signed java int (BIG_ENDIAN)
     * @param resultantNumberOfBytes  - number of bytes in resultant array. Can be: 4, 3, 2 or 1.
     *
     * @return resultant byte array (LITTLE_ENDIAN ordered)
     */

    public static byte[] intToLittleEndianBytes(int value, int resultantNumberOfBytes) {
        switch (resultantNumberOfBytes) {
            case 4:
                return new byte[]{
                        (byte) value,
                        (byte) (value >>> 8),
                        (byte) (value >>> 16),
                        (byte) (value >>> 24) };
            case 3:
                return new byte[]{
                        (byte) value,
                        (byte) (value >>> 8),
                        (byte) (value >>> 16) };
            case 2:
                return new byte[]{
                        (byte) value,
                        (byte) (value >>> 8)};
            case 1:
                return new byte[]{
                        (byte) value};
            default:
                String errMsg = "Available number of resultant bytes: 4, 3, 2 or 1.   " +
                        "Specified number of bytes = "+resultantNumberOfBytes;
                throw new IllegalArgumentException(errMsg);
        }

    }


    /**
     * Convert given LITTLE_ENDIAN ordered bytes to BIG_ENDIAN java integer.
     * Available number of input bytes: 4, 3, 2 or 1.
     *
     * @param bytes 4, 3, 2 or 1 bytes (LITTLE_ENDIAN ordered) to be converted to int
     *
     * @return standard 32-bit signed java int (BIG_ENDIAN)
     */
    public static int littleEndianBytesToInt(byte... bytes) {
        switch (bytes.length) {
            case 1:
                return bytes[0];
            case 2:
                return (bytes[1] << 8) | (bytes[0] & 0xFF);
            case 3:
                return (bytes[2] << 16) | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            case 4:
                return (bytes[3] << 24) | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            default:
                String errMsg = "Available number of input bytes: 4, 3, 2 or 1.   " +
                        "Specified number of bytes = "+bytes.length;
                throw new IllegalArgumentException(errMsg);
        }
    }

    /**
     * Convert specified number of bytes (4, 3, 2 or 1) of the LITTLE_ENDIAN ordered byte array
     * to BIG_ENDIAN java integer.
     * Сonversion begins from the specified offset position.
     *
     *
     * @param byteArray  byte array (LITTLE_ENDIAN ordered) from which 4, 3, 2 or 1 bytes
     *                   are taken to be converted to int
     * @param numberOfBytesPerInt  number of bytes that should be converted to int. Can be: 4, 3, 2 or 1.
     * @param offset the offset within the array of the first byte to be converted
     *
     * @return standard 32-bit signed java int (BIG_ENDIAN)
     */
    public static int littleEndianBytesToInt(byte[] byteArray, int offset, int numberOfBytesPerInt) {
        switch (numberOfBytesPerInt) {
            case 1:
                return byteArray[offset];
            case 2:
                return (byteArray[offset + 1] << 8) | (byteArray[offset] & 0xFF);
            case 3:
                return (byteArray[offset + 2] << 16) | (byteArray[offset + 1] & 0xFF) << 8 | (byteArray[offset] & 0xFF);
            case 4:
                return (byteArray[offset + 3] << 24) | (byteArray[offset + 2] & 0xFF) << 16 | (byteArray[offset + 1] & 0xFF) << 8 | (byteArray[offset] & 0xFF);
            default:
                String errMsg = "Available «number of bytes per int»: 4, 3, 2 or 1.   " +
                        "Specified «number of bytes per int» = "+numberOfBytesPerInt;
                throw new IllegalArgumentException(errMsg);
        }
    }

    /**
     * Convert LITTLE_ENDIAN ordered byte array to int array.
     *
     * @param byteArray - byte array (LITTLE_ENDIAN ordered) to be converted to int array
     * @param numberOfBytesPerInt  - number of bytes that will be converted to int. Can be: 4, 3, 2 or 1.
     *
     * @return resultant array of int
     */
    public static int[] littleEndianByteArrayToIntArray(byte[] byteArray, int numberOfBytesPerInt) {
        int[] result = new int[byteArray.length / numberOfBytesPerInt];
        for (int index = 0; index < result.length; index++) {
            result[index] = littleEndianBytesToInt(byteArray, index * numberOfBytesPerInt, numberOfBytesPerInt);
        }
        return result;
    }

    /**
     * Convert specified number of elements from int array to LITTLE_ENDIAN ordered byte array.
     * Сonversion begins from the specified offset position.
     *
     * @param intArray - int array to be converted to byte array
     * @param numberOfBytesPerInt - the number of bytes that we will get as a result of conversion of each int
     * @param offset -  the offset within the array of the first int to be converted
     * @param length - number of int elements that will be converted to bytes
     *
     * @return  resultant array of byte (LITTLE_ENDIAN ordered)
     */

    public static byte[] intArrayToLittleEndianByteArray(int[] intArray, int offset, int length, int numberOfBytesPerInt) {
        byte[] result = new byte[length * numberOfBytesPerInt];
        for (int i = 0; i < length; i++) {
            System.arraycopy(intToLittleEndianBytes(intArray[offset + i], numberOfBytesPerInt), 0, result, i * numberOfBytesPerInt, numberOfBytesPerInt);
        }
        return result;
    }



    /**
     * Convert given LITTLE_ENDIAN ordered bytes to BIG_ENDIAN 32-bit UNSIGNED int.
     * Available number of input bytes: 4, 3, 2 or 1.
     *
     * @param bytes - 4, 3, 2 or 1 bytes (LITTLE_ENDIAN ordered) to be converted to int
     * @return 32-bit UNSIGNED int (BIG_ENDIAN)
     */

    public static int littleEndianBytesToUnsignedInt(byte... bytes) {
        switch (bytes.length) {
            case 1:
                return (bytes[0] & 0xFF);
            case 2:
                return (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            case 3:
                return (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            case 4:
                return (bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            default:
                String errMsg = "Available number of input bytes: 4, 3, 2 or 1.   " +
                        "Specified number of bytes = "+bytes.length;
                throw new IllegalArgumentException(errMsg);
        }
    }
}
