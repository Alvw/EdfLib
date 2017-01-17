package com.biorecorder.edflib;

import com.biorecorder.edflib.util.HeaderUtility;
import com.biorecorder.edflib.util.PhysicalDigitalConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class for writing DataRecords to BDF File.
 * Before writing DataRecord to the file it converts every digital (int) value
 * to 2 bytes, LITTLE_ENDIAN ordered.
 *
 * <p>In the case if DataRecord contains not digital (int) but real physical (floating point) values
 * they should be converted to ints first. See {@link PhysicalDigitalConverter}.
 * The special {@link RecordingConfig} object contains all necessary information about
 * DataRecords structure and permits correctly extract
 * data from DataRecords and convert every physical DataRecord to digital DataRecord and vice versa.
 * Also on the base of that information the file header is created (see {@link HeaderUtility}).
 *
 * <p>Detailed information about EDF/BDF format:
 * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
 * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <br><a href="http://www.edfplus.info/specs/edffloat.html">EDF. How to store longintegers and floats</a>
 *
 * <p>EdfWriter creates and opens an new edf file if it does not exist. Already existing file with the same name
 * will be silently overwritten without advance warning!!
 *
 * @see DataRecordsWriter
 * @see DataRecordsFileWriter
 */
public class EdfWriter extends DataRecordsFileWriter {

    /**
     * Creates EdfWriter to write DataRecords to the EDF file represented by the specified File object.
     * Every DataRecords will be written to the file at once, without buffering
     *
     * @param file the file to be opened for writing
     */
    public EdfWriter(File file) throws IOException {
        super(file);
    }

    /**
     * Create a {@link File} with the given filename and call the other constructor
     *
     * @param filename - the system-dependent filename
     *
     * @throws FileNotFoundException
     */

    public EdfWriter(String filename)  throws IOException {
        this(new File(filename));
    }

    @Override
    protected int getNumberOfBytesPerSample() {
        return 2;
    }

    @Override
    protected byte[] createHeader()  {
        return HeaderUtility.createEdfHeader(recordingConfig);

    }
}
