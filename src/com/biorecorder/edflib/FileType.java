package com.biorecorder.edflib;

import java.nio.charset.Charset;

/**
 * Defines file types that can be read or written by
 * {@link EdfFileReader} and {@link EdfFileWriter}
 *
 */
public enum FileType {
    /**
     * EDF file with 16 bits integers
     */
    EDF_16BIT {
        private final int DEFAULT_DIG_MIN_16 = -32768;
        private final int DEFAULT_DIG_MAX_16 = 32767;
        @Override
        String getVersion() {
            return "";
        }

        @Override
        byte getFirstByte() {
            final Charset ASCII = Charset.forName("US-ASCII");
            String zeroString = "0";  // "0" (ASCII)
            return zeroString.getBytes(ASCII)[0]; // or  return (int) '0';
        }

        @Override
        String getFirstReserved() {
            return "";
        }

        @Override
        int getNumberOfBytesPerSample() {
            return 2;
        }

        @Override
        int getDigitalMax() {
            return DEFAULT_DIG_MAX_16;
        }

        @Override
        int getDigitalMin() {
            return DEFAULT_DIG_MIN_16;
        }
    },
    /**
     * BDF file with 24 bits integers
     */
    BDF_24BIT {
        private final int DEFAULT_DIG_MIN_24 = -8388608;
        private final int DEFAULT_DIG_MAX_24 = 8388607;

        @Override
        String getVersion() {
            return "BIOSEMI";
        }

        @Override
        byte getFirstByte() {
            return (byte) 255;
        }

        @Override
        String getFirstReserved() {
            return "24BIT";
        }

        @Override
        int getNumberOfBytesPerSample() {
           return 3;
       }

        @Override
        int getDigitalMax() {
            return DEFAULT_DIG_MAX_24;
        }

        @Override
        int getDigitalMin() {
            return DEFAULT_DIG_MIN_24;
        }

    };

    /**
     * Tell how many bytes every sample in the file occupies
     *
     * @return number of bytes per data sample in the file: 2 for EDF files and 3 for BDF files
     */
    abstract int getNumberOfBytesPerSample();


    /**
     * Create the version (minus first byte) of the data format
     * for BDF or EDF file header respectively
     *
     * @return the version for the the BDF or EDF file header
     */
    abstract String getVersion();

    /**
     * Create first byte for the BDF or EDF file header respectively
     *
     * @return first byte for the the BDF or EDF file header
     */
    abstract byte getFirstByte();

    /**
     * Create the first reserved field for the BDF or EDF file header respectively
     *
     * @return the first reserved field for the the BDF or EDF file header
     */
    abstract String getFirstReserved();


    abstract int getDigitalMax();

    abstract int getDigitalMin();

}

