package com.biorecorder.edflib;

import com.biorecorder.edflib.util.HeaderUtility;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Class for writing DataRecords to BDF File.
 * Before writing DataRecord to the file it converts every digital value (every integer)
 * to 2 bytes, LITTLE_ENDIAN ordered.
 *
 * It creates and opens an new edf file if it does not exist. Already existing file with the same name
 * will be silently overwritten without advance warning!!
 */
public class EdfWriter extends DataRecordsFileWriter {

    /**
     * Creates EdfWriter to write DataRecords to the EDF file represented by the specified File object.
     * Every DataRecords will be written to the file at once, without buffering
     *
     * @param file the file to be opened for writing
     */
    public EdfWriter(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Create a {@link File} with the given filename and call the other constructor
     *
     * @param filename - the system-dependent filename
     *
     * @throws FileNotFoundException
     */

    public EdfWriter(String filename) throws FileNotFoundException {
        super(new File(filename));
    }

    @Override
    protected int getNumberOfBytesInSample() {
        return 2;
    }

    @Override
    protected byte[] createHeader()  {
        return HeaderUtility.createEdfHeader(recordConfig);

    }
}
