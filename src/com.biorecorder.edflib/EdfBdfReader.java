package com.biorecorder.edflib;

import com.biorecorder.edflib.util.EndianBitConverter;
import com.biorecorder.edflib.util.HeaderParsingException;
import com.biorecorder.edflib.util.HeaderUtility;
import com.biorecorder.edflib.util.PhysicalDigitalConverter;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
    private PhysicalDigitalConverter physicalDigitalConverter;
    private boolean isBdf;
    private RecordingConfig recordingConfig;
    private FileInputStream fileInputStream;
    private File file;
    private List<Long> samplesPositionList = new ArrayList<Long>();
    private int recordPosition = 0;

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
        physicalDigitalConverter = new PhysicalDigitalConverter(recordingConfig);
        this.file = file;
        fileInputStream = new FileInputStream(file);
        for(int i = 0; i < recordingConfig.getNumberOfSignals(); i++) {
            samplesPositionList.add(new Long(0));
        }
    }

    public void setRecordPosition(int position) {
        recordPosition = position;
    }

    public void setSignalSamplePosition(int signalNumber, long position) {
        samplesPositionList.set(signalNumber, position);
    }



    /**
     * Read ONE digital DataRecord from file
     *
     * @return data record or null if the end of file has been reached or
     * the rest of the file contains insufficient data to form entire data record
     * @throws IOException if the file could not be read
     */

    public int[] readDigitalDataRecord() throws IOException {

        long realPosition = recordingConfig.getNumberOfBytesInHeader() +
                recordingConfig.getRecordLength() * recordPosition * getNumberOfBytesPerSample();
        if(fileInputStream.getChannel().position() != realPosition) {
            fileInputStream.getChannel().position(realPosition);
        }

        int rowLength = recordingConfig.getRecordLength() * getNumberOfBytesPerSample();
        byte[] rowData = new byte[rowLength];

        int numberOfReadBytes = fileInputStream.getChannel().read(ByteBuffer.wrap(rowData));
          if(numberOfReadBytes == rowLength) {
              recordPosition ++;
            return EndianBitConverter.littleEndianByteArrayToIntArray(rowData, getNumberOfBytesPerSample());
          } else {
              return null;
          }
    }

    public double[] readPhysycalDataRecord() throws IOException {
        return physicalDigitalConverter.digitalRecordToPhysical(readDigitalDataRecord());
    }

    private int readSamplesFromRecord(int signalNumber, long recordNumber, ByteBuffer buffer) throws IOException {
        long signalPosition = recordingConfig.getRecordLength() * recordNumber;
        for(int i = 0; i < signalNumber; i++) {
            signalPosition += recordingConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord();
        }
        signalPosition = signalPosition * getNumberOfBytesPerSample() + recordingConfig.getNumberOfBytesInHeader();
        return fileInputStream.getChannel().read(buffer, signalPosition);
    }


    public double[] readPhysicalSamples (int signalNumber, int numberOfSamples) throws IOException {
        int[] digSamples = readDigitalSamples(signalNumber, numberOfSamples);
        double[] physSamples = new double[digSamples.length];
        for(int i = 0; i < digSamples.length; i++) {
           physSamples[i] = physicalDigitalConverter.digitalValueToPhysical(digSamples[i], signalNumber);
        }
        return physSamples;

    }

    public int[] readDigitalSamples(int signalNumber, int numberOfSamples) throws IOException {
        if(numberOfSamples > availableSignalSamples(signalNumber)) {
            numberOfSamples = (int)availableSignalSamples(signalNumber);
        }
        int[] digSamples = new int[numberOfSamples];
        readDigitalSamples(signalNumber, digSamples, 0, numberOfSamples) ;
        return digSamples;
    }

    public int readDigitalSamples(int signalNumber, int[] digArray, int offset, int numberOfSamples) throws IOException {
        int readTotal = 0;
        int samplesPerRecord = recordingConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord();
        byte[] rowData = new byte[samplesPerRecord * getNumberOfBytesPerSample()];
        ByteBuffer buffer = ByteBuffer.wrap(rowData);
        long recordNumber = samplesPositionList.get(signalNumber) / samplesPerRecord;
        int positionInRecord = (int) (samplesPositionList.get(signalNumber) % samplesPerRecord);

        while (readTotal < numberOfSamples) {
            if(readSamplesFromRecord(signalNumber, recordNumber, buffer) < buffer.capacity()) {
                break;
            }
            int readInRecord = Math.min(numberOfSamples - readTotal, samplesPerRecord - positionInRecord);
            EndianBitConverter.littleEndianByteArrayToIntArray(rowData, positionInRecord * getNumberOfBytesPerSample(), digArray, offset + readTotal, readInRecord, getNumberOfBytesPerSample());
            readTotal += readInRecord;
            buffer.clear();
            recordNumber++;
            positionInRecord = 0;
        }
        samplesPositionList.set(signalNumber, samplesPositionList.get(signalNumber) + readTotal);
        return readTotal;
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
     * Calculate and get the total number of  DataRecords in the file
     *
     * @return total number of DataRecords in the file
     * @throws IOException if file could not be read
     */
    public int getNumberOfDataRecords() throws IOException {
        return (int)(file.length() - recordingConfig.getNumberOfBytesInHeader()) / (recordingConfig.getRecordLength() * getNumberOfBytesPerSample());
    }

    /**
     * Get number of DataRecords available for reading (from the current DataRecord position)
     *
     * @return number of available for reading DataRecords
     * @throws IOException if file could not be read
     */
    public int availableDataRecords() throws IOException {
        return getNumberOfDataRecords() - recordPosition;
    }

    /**
     * Get the number of samples of the given channel (signal) available for reading
     * (from the current sample position set for that signal)
     *
     * @return number of samples of the given signal available for reading
     * @throws IOException if file could not be read
     */
    public long availableSignalSamples(int signalNumber) throws IOException {
        long totalNumberOfSamples = (long)getNumberOfDataRecords() * (long)recordingConfig.getSignalConfig(signalNumber).getNumberOfSamplesInEachDataRecord();
        return totalNumberOfSamples - samplesPositionList.get(signalNumber);
    }



    /**
     * Close this EdfBdfReader and releases any system resources associated with
     * it. This method MUST be called after finishing reading DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     *
     * @throws IOException if the reader can not be closed correctly
     */
    public void close() throws IOException {
        fileInputStream.close();
    }
}
