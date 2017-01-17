package com.biorecorder.edflib;

import com.biorecorder.edflib.util.EndianBitConverter;
import com.biorecorder.edflib.util.HeaderParsingException;
import com.biorecorder.edflib.util.HeaderUtility;
import com.biorecorder.edflib.util.PhysicalDigitalConverter;

import java.io.*;

/**
 * Permit to read DataRecords from EDF and BDF files. The structure of DataRecords is described
 * in the file header. EdfBdfReader reads  and recognizes the information from the file header and
 * save it in special {@link RecordingConfig} object. After that it can correctly read data
 * from the file.
 *
 * <p>EDF/BDF files contains digital (int) data but they can be converted to corresponding
 * real physical floating point data on the base of header information.
 * See {@link PhysicalDigitalConverter}
 *
 * <p>Detailed information about EDF/BDF format:
 * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
 * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <br><a href="http://www.edfplus.info/specs/edffloat.html">EDF. How to store longintegers and floats</a>
 */
public class EdfBdfReader {
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private boolean isBdf;
    private RecordingConfig recordingConfig;

    /**
     * Creates EdfBdfReader to write data from the file represented by the specified File object.
     *
     * @param file the file to be opened for reading
     * @throws IOException if the file header can not be read
     * @throws HeaderParsingException  if the file header is not valid EDF/BDF file header
     */
    public EdfBdfReader(File file) throws IOException, HeaderParsingException {
        isBdf = HeaderUtility.isBdf(file);
        recordingConfig = HeaderUtility.readHeader(file);
        fileInputStream = new FileInputStream(file);
    }

    /**
     * Give the information from the file header stored in the RecordingConfig object
     *
     * @return the object containing EDF/BDF header information
     */
    public RecordingConfig getRecordingConfig() {
        return recordingConfig;
    }

    /**
     * On the base of the file header information determines whether the file open for reading
     * is EDF or BDF file
     *
     * @return true in the case of BDF file and false in the case of EDF file
     */
    public boolean isBdf() {
        return isBdf;
    }

    /**
     * Tell how many bytes every sample in the file occupies
     *
     * @return number of bytes per data sample in the file: 2 for EDF files and 3 for BDF files
     */
    private int getNumberOfBytesPerSample() {
        if(isBdf) {
            return 3;

        } else {
            return 2;
        }
    }

    /**
     * Get number of DataRecords in the file available for reading
     *
     * @return number of available for reading DataRecords
     * @throws IOException if file could not be read
     */
    public int availableDataRecords() throws IOException {
        if(bufferedInputStream == null) {
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.skip(recordingConfig.getNumberOfBytesInHeader());
        }
        return bufferedInputStream.available() / (recordingConfig.getRecordLength() * getNumberOfBytesPerSample());
    }

    /**
     * Read ONE digital DataRecord from file
     *
     * @return data record or null if the end of file has been reached or
     * the rest of the file contains insufficient data to form entire data record
     * @throws IOException if the file could not be read
     */
    public int[] readDigitalDataRecord() throws IOException {
        if(bufferedInputStream == null) {
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.skip(recordingConfig.getNumberOfBytesInHeader());
        }
        int rowLength = recordingConfig.getRecordLength() * getNumberOfBytesPerSample();
        byte[] rowData = new byte[rowLength];
        bufferedInputStream.mark(rowLength);
        if(bufferedInputStream.read(rowData) < rowLength) { // returns numOfBytesRead or -1 at EOF
            bufferedInputStream.reset();
            return null;
        }
        else{
            return EndianBitConverter.littleEndianByteArrayToIntArray(rowData, getNumberOfBytesPerSample());

        }
    }


    /**
     * Close this EdfBdfReader and releases any system resources associated with
     * it. This method MUST be called after finishing reading DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     *
     * @throws IOException if the reader can not be closed correctly
     */
    public void close() throws IOException {
        if(bufferedInputStream != null) {
            bufferedInputStream.close();
        }
        else {
            fileInputStream.close();
        }

    }
}
