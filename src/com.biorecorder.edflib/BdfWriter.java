package com.biorecorder.edflib;

import com.biorecorder.edflib.util.HeaderUtility;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Class for writing DataRecords to BDF File.
 * Before writing DataRecord to the file it converts every digital value (every integer)
 * to 3 bytes, LITTLE_ENDIAN ordered.
 *
 * It creates and opens an new bdf file if it does not exist. Already existing file with the same name
 * will be silently overwritten without advance warning!!
 */
public class BdfWriter extends  DataRecordsFileWriter {

    /**
     * Creates BDFWriter to write DataRecords to the BDF file represented by the specified File object.
     * Every DataRecords will be written to the file at once, without buffering
     *
     * @param file the file to be opened for writing
     */
    public BdfWriter(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Create a {@link File} with the given filename and call the other constructor
     *
     * @param filename - the system-dependent filename
     *
     * @throws FileNotFoundException
     */

    public BdfWriter(String filename) throws FileNotFoundException {
        super(new File(filename));
    }

    @Override
    protected int getNumberOfBytesInSample() {
        return 3;
    }


    @Override
    protected byte[] createHeader()  {
        return HeaderUtility.createBdfHeader(recordConfig);

    }
}
