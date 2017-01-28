package com.biorecorder.edflib;

import java.nio.charset.Charset;

/**
 * Defines file types that can be read or written by
 * {@link EdfReader} and {@link EdfWriter}
 *
 */
public enum FileType {
    /**
     * EDF file with 16 bits integers
     */
    EDF_16BIT {
        public String getVersion() {
            return "";
        }

        public byte getFirstByte() {
            final Charset ASCII = Charset.forName("US-ASCII");
            String zeroString = "0";  // "0" (ASCII)
            return zeroString.getBytes(ASCII)[0]; // or  return (int) '0';
        }

        public String getFirstReserved() {
            return "";
        }

        public int getNumberOfBytesPerSample() {
            return 2;
        }

    },
    /**
     * BDF file with 24 bits integers
     */
    BDF_24BIT {
        public String getVersion() {
            return "BIOSEMI";
        }

        public byte getFirstByte() {
            return (byte) 255;
        }

        public String getFirstReserved() {
            return "24BIT";
        }

       public int getNumberOfBytesPerSample() {
           return 3;
       }
    };

    /**
     * Tell how many bytes every sample in the file occupies
     *
     * @return number of bytes per data sample in the file: 2 for EDF files and 3 for BDF files
     */
    public abstract int getNumberOfBytesPerSample();


    /**
     * Create the version (minus first byte) of the data format
     * for BDF or EDF file header respectively
     *
     * @return the version for the the BDF or EDF file header
     */
    public abstract String getVersion();

    /**
     * Create first byte for the BDF or EDF file header respectively
     *
     * @return first byte for the the BDF or EDF file header
     */
    public abstract byte getFirstByte();

    /**
     * Create the first reserved field for the BDF or EDF file header respectively
     *
     * @return the first reserved field for the the BDF or EDF file header
     */
    public abstract String getFirstReserved();

    }
