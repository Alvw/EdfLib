package com.biorecorder.edflib;

import com.biorecorder.edflib.exceptions.InvalidEdfFileRuntimeException;
import com.biorecorder.edflib.exceptions.FileNotFoundRuntimeException;
import com.biorecorder.edflib.exceptions.IORuntimeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

/**
 * Permits to read data samples from EDF or BDF file. Also it
 * reads information from the file header and saves it in special {@link HeaderInfo} object, that we
 * can get by method {@link #getHeader()}
 * <p>
 * EDF/BDF files contains "row" digital (int) data but they can be converted to corresponding
 * real physical floating point data on the base of header information (physical maximum and minimum
 * and digital maximum and minimum specified for every channel (signal)).
 * So we can "read" both digital or physical values.
 * See: {@link #readDigitalSamples(int, int[], int, int)}, {@link #readPhysicalSamples(int, double[], int, int)}
 * {@link #readDigitalDataRecord(int[])}, {@link #readPhysicalDataRecord(double[])}.
 */
public class EdfFileReader {
    private HeaderInfo headerInfo;
    private FileInputStream fileInputStream;
    private File file;
    private long[] samplesPositionList;
    private int recordPosition = 0;

    /**
     * Creates EdfFileReader to read data from the file represented by the specified
     * File object. Before create EdfFileReader you can check if the file is valid EdF/Bdf file:
     * {@link #isReadableValidEdfFile(File)}
     *
     * @param file Edf or Bdf file to be opened for reading
     * @throws FileNotFoundRuntimeException   if the file does not exist,
     *                                        is a directory rather than a regular file,
     *                                        or for some other reason cannot be opened for reading.
     * @throws InvalidEdfFileRuntimeException if the the file is not valid EDF/BDF file
     *                                        due to some errors in its header record
     */
    public EdfFileReader(File file) throws FileNotFoundRuntimeException, InvalidEdfFileRuntimeException {
        this.file = file;
        try {
            fileInputStream = new FileInputStream(file);
            headerInfo = new HeaderInfo(file);
        } catch (InvalidEdfFileRuntimeException e) {
            throw e;
        } catch (Exception e) {
            String errMsg = MessageFormat.format("File: {0} can not be opened for reading", file);
            throw new FileNotFoundRuntimeException(errMsg, e);
        }
        samplesPositionList = new long[headerInfo.getNumberOfSignals()];
    }

    /**
     * Checks if the given file is a readable and valid Edf or Bdf file.
     *
     * @return true if the file exist, can be read and is valid Edf/Bdf file
     */
    public static boolean isReadableValidEdfFile(File file) {
        try {
            new HeaderInfo(file);
            FileInputStream stream = new FileInputStream(file);
            stream.close();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    /**
     * Set the DataRecords position indicator to the given new position.
     * The position is measured in DataRecords. Methods {@link #readDigitalDataRecord(int[])} and
     * {@link #readPhysicalDataRecord(double[])} will start reading from the specified position.
     *
     * @param newPosition the new position, a non-negative integer counting
     *                    the number of DataRecords from the beginning of the file
     */
    public void setDataRecordPosition(int newPosition) {
        recordPosition = newPosition;
    }

    /**
     * Set the sample position indicator of the given channel (signal)
     * to the given new position. The position is measured in samples.
     * <p>
     * Note that every signal has it's own independent sample position indicator and
     * setSamplePosition() affects only one of them.
     * Methods {@link #readDigitalSamples(int, int[], int, int)} and
     * {@link #readPhysicalSamples(int, double[], int, int)} will start reading
     * samples belonging to a channel from the specified for that channel position.
     *
     * @param signalNumber channel (signal) number whose sample position we change. Numbering starts from 0!
     * @param newPosition  the new sample position, a non-negative integer counting
     *                     the number of samples belonging to the specified
     *                     channel from the beginning of the file
     */
    public void setSamplePosition(int signalNumber, long newPosition) {
        samplesPositionList[signalNumber] = newPosition;
    }

    /**
     * Puts DataRecord position indicator and sample position indicators of all signals to 0.
     */
    public void reset() {
        recordPosition = 0;
        for (int i = 0; i < samplesPositionList.length; i++) {
            samplesPositionList[i] = 0;
        }
    }

    /**
     * Return the current DataRecord position.
     * The position is measured in DataRecords.
     *
     * @return current DataRecord position, a non-negative integer counting
     * the number of DataRecords from the beginning of the file
     */
    public int getDataRecordPosition() {
        return recordPosition;
    }

    /**
     * Return the current sample position  of the given channel (signal).
     * The position is measured in samples.
     *
     * @param signalNumber channel (signal) number whose position we want to get. Numbering starts from 0!
     * @return current sample position, a non-negative integer counting
     * the number of samples belonging to the given
     * channel from the beginning of the file
     */
    public long getSamplePosition(int signalNumber) {
        return samplesPositionList[signalNumber];
    }

    /**
     * Reads the "raw" digital (integer) values corresponding to ONE DataRecord
     * and store them in the given buffer array.
     * Reading starts from the specified DataRecord position.
     * The DataRecords position indicator will be increased with 1.
     *
     * @param buffer array where read samples will be stored
     * @return the amount of read samples (this can be less than buffer.length or zero!)
     * @throws IORuntimeException if data could not be read
     */
    public int readDigitalDataRecord(int[] buffer) throws IORuntimeException {
        int numberOfBytesPerSample = headerInfo.getFileType().getNumberOfBytesPerSample();
        long realPosition = headerInfo.getNumberOfBytesInHeaderRecord() +
                headerInfo.getDataRecordLength() * recordPosition * numberOfBytesPerSample;
        try {
            if (fileInputStream.getChannel().position() != realPosition) {
                fileInputStream.getChannel().position(realPosition);
            }
            int recordLength = headerInfo.getDataRecordLength();
            int rowLength = recordLength * numberOfBytesPerSample;
            byte[] rowData = new byte[rowLength];

            int numberOfReadBytes = fileInputStream.getChannel().read(ByteBuffer.wrap(rowData));
            if (numberOfReadBytes == rowLength) {
                recordPosition++;
                EndianBitConverter.littleEndianByteArrayToIntArray(rowData, 0, buffer, 0, recordLength, numberOfBytesPerSample);
                return recordLength;
            }
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Error while reading data from the file: {0}.", file);
            throw new IORuntimeException(errMsg, e);
        }
        return 0;
    }


    /**
     * Reads the samples corresponding to ONE DataRecord, converts
     * them to their physical values (e.g. microVolts, beats per minute, etc)
     * and store the resultant physical values in the given buffer array.
     * Reading starts from the specified DataRecord position.
     * The DataRecords position indicator will be increased with 1.
     *
     * @param buffer array where resultant physical samples will be stored
     * @return the amount of read samples (this can be less than buffer.length or zero!)
     * @throws IORuntimeException if data could not be read
     */
    public int readPhysicalDataRecord(double[] buffer) throws IORuntimeException {
        int recordLength = headerInfo.getDataRecordLength();
        int[] digRecord = new int[recordLength];
        if (readDigitalDataRecord(digRecord) == recordLength) {
            headerInfo.digitalDataRecordToPhysical(digRecord, buffer);
            return recordLength;
        }
        return 0;
    }

    /**
     * Helper method that reads only samples belonging to the given channel within the DataRecord at the given position.
     * Read samples will be saved in the specified ByteBuffer.
     * The values are the "raw" digital (integer) values.
     * Note the this method does note affect the DataRecord position used by the methods
     * {@link #readDigitalSamples(int, int[], int, int)} and
     * {@link #readPhysicalSamples(int, double[], int, int)} (int, int)}
     *
     * @param signalNumber   channel (signal) number whose samples must be read within the given DataRecord
     * @param recordPosition position of the DataRecord
     * @param buffer         buffer where read samples will be saved
     * @return amount of read samples
     * @throws IORuntimeException if data can not be read
     */
    private int readSamplesFromRecord(int signalNumber, int recordPosition, ByteBuffer buffer) throws IORuntimeException {
        long signalPosition = headerInfo.getDataRecordLength() * recordPosition;
        for (int i = 0; i < signalNumber; i++) {
            signalPosition += headerInfo.getNumberOfSamplesInEachDataRecord(i);
        }
        signalPosition = signalPosition * headerInfo.getFileType().getNumberOfBytesPerSample() + headerInfo.getNumberOfBytesInHeaderRecord();
        int readByteNumber = 0;
        try {
            readByteNumber = fileInputStream.getChannel().read(buffer, signalPosition);
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Error while reading data from the file: {0}.", file);
            throw new IORuntimeException(errMsg, e);
        }
        return readByteNumber;
    }


    /**
     * Read the given number of samples belonging to the  channel
     * starting from the current sample position indicator.
     * The values are the "raw" digital (integer) values.
     * The sample position indicator of that channel will be increased with the amount of samples read.
     * Read samples are saved in the specified array starting at the specified offset.
     * Return the amount of read samples (this can be less than given numberOfSamples or zero!)
     *
     * @param signalNumber    channel (signal) number whose samples must be read. Numbering starts from 0!
     * @param buffer          buffer where read samples are saved
     * @param offset          offset within the buffer array at which saving starts
     * @param numberOfSamples number of samples to read
     * @return the amount of read samples (this can be less than given numberOfSamples or zero!)
     * @throws IORuntimeException if data can not be read
     */
    public int readDigitalSamples(int signalNumber, int[] buffer, int offset, int numberOfSamples) throws IORuntimeException {
        int readTotal = 0;
        int samplesPerRecord = headerInfo.getNumberOfSamplesInEachDataRecord(signalNumber);
        byte[] rowData = new byte[samplesPerRecord * headerInfo.getFileType().getNumberOfBytesPerSample()];
        ByteBuffer byteBuffer = ByteBuffer.wrap(rowData);
        int recordNumber = (int) (samplesPositionList[signalNumber] / samplesPerRecord);
        int positionInRecord = (int) (samplesPositionList[signalNumber] % samplesPerRecord);

        while (readTotal < numberOfSamples) {
            if (readSamplesFromRecord(signalNumber, recordNumber, byteBuffer) < byteBuffer.capacity()) {
                break;
            }
            int readInRecord = Math.min(numberOfSamples - readTotal, samplesPerRecord - positionInRecord);
            EndianBitConverter.littleEndianByteArrayToIntArray(rowData, positionInRecord * headerInfo.getFileType().getNumberOfBytesPerSample(), buffer, offset + readTotal, readInRecord, headerInfo.getFileType().getNumberOfBytesPerSample());
            readTotal += readInRecord;
            byteBuffer.clear();
            recordNumber++;
            positionInRecord = 0;
        }
        samplesPositionList[signalNumber] += readTotal;
        return readTotal;
    }

    /**
     * Read the samples belonging to the  channel
     * starting from the current sample position indicator. Number of samples to read = buffer.length.
     * Do the same as readDigitalSamples(signalNumber, buffer, 0, buffer.length);
     * <p>
     * The values are the "raw" digital (integer) values.
     * The sample position indicator of that channel will be increased with the amount of samples read.
     * Read samples are saved in the specified array starting at the specified offset.
     * Return the amount of read samples (this can be less than given numberOfSamples or zero!)
     *
     * @param signalNumber channel (signal) number whose samples must be read. Numbering starts from 0!
     * @param buffer       buffer where read samples are saved
     * @return the amount of read samples (this can be less than buffer.length or zero!)
     * @throws IORuntimeException if data can not be read
     */
    public int readDigitalSamples(int signalNumber, int[] buffer) throws IORuntimeException {
        return readDigitalSamples(signalNumber, buffer, 0, buffer.length);
    }


    /**
     * Read the given number of samples belonging to the channel
     * starting from the current sample position indicator. Converts the read samples
     * to their physical values (e.g. microVolts, beats per minute, etc) and
     * saves them in the specified buffer array starting at the specified offset.
     * The sample position indicator of that channel will be increased with the
     * amount of samples read.
     * Return the amount of read samples (this can be less than given numberOfSamples or zero!)
     *
     * @param signalNumber    channel (signal) number whose samples must be read. Numbering starts from 0!
     * @param buffer          buffer where resultant values are saved
     * @param offset          offset within the buffer array at which saving starts
     * @param numberOfSamples number of samples to read
     * @return the amount of read samples (this can be less than given numberOfSamples or zero!)
     * @throws IORuntimeException if data can not be read
     */
    public int readPhysicalSamples(int signalNumber, double[] buffer, int offset, int numberOfSamples) throws IORuntimeException {
        int[] digSamples = new int[numberOfSamples];
        int numberOfReadSamples = readDigitalSamples(signalNumber, digSamples, 0, numberOfSamples);
        for (int i = 0; i < numberOfReadSamples; i++) {
            buffer[i + offset] = headerInfo.digitalValueToPhysical(signalNumber, digSamples[i]);
        }
        return numberOfReadSamples;
    }

    /**
     * Read the  samples belonging to the channel
     * starting from the current sample position indicator. Number of samples to read = buffer.length.
     * Do the same as readPhysicalSamples(signalNumber, buffer, 0, buffer.length);
     * <p>
     * Converts the read samples
     * to their physical values (e.g. microVolts, beats per minute, etc) and
     * saves them in the specified buffer array starting at the specified offset.
     * The sample position indicator of that channel will be increased with the
     * amount of samples read.
     * Return the amount of read samples (this can be less than given numberOfSamples or zero!)
     *
     * @param signalNumber channel (signal) number whose samples must be read. Numbering starts from 0!
     * @param buffer       buffer where resultant values are saved
     * @return the amount of read samples (this can be less than buffer.length or zero!)
     */
    public int readPhysicalSamples(int signalNumber, double[] buffer) throws IORuntimeException {
        return readPhysicalSamples(signalNumber, buffer, 0, buffer.length);
    }


    /**
     * Return the information from the file header stored in the HeaderInfo object
     *
     * @return the object containing EDF/BDF header information
     */
    public HeaderInfo getHeader() {
        return headerInfo;
    }

    /**
     * On the base of information from the given config object
     * a new header will be created and rewritten to the file.
     * Note that the number of channels (signals) can not be changed!
     *
     * @param newHeaderInfo config object containing info for the new header
     * @throws IllegalArgumentException if the number of channels in the new HeaderInfo object
     *                                  does not equal the number of channels in the existent one
     * @throws IORuntimeException       if the header record failed to be re-written
     */
    public void rewriteHeader(HeaderInfo newHeaderInfo) throws IORuntimeException {
        if (newHeaderInfo.getNumberOfSignals() != headerInfo.getNumberOfSignals()) {
            String errMsg = MessageFormat.format("The number of signals {0} can not be changed! New number of signals: {1}", headerInfo.getNumberOfSignals(), newHeaderInfo.getNumberOfSignals());
            throw new IllegalArgumentException(errMsg);
        }
        byte[] header = newHeaderInfo.createFileHeader();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(header);
            fileOutputStream.close();
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Error while re-writing header record to the file: {0}.", file);
            throw new IORuntimeException(errMsg, e);
        }
        headerInfo = newHeaderInfo;
    }


    /**
     * Get the number of DataRecords available for reading (from the current DataRecord position).
     * <br>availableDataRecords() = getNumberOfDataRecords() - getDataRecordPosition();
     *
     * @return number of available for reading DataRecords
     */
    public int availableDataRecords() {
        return getNumberOfDataRecords() - recordPosition;
    }

    /**
     * Get the number of samples of the given channel (signal) available for reading
     * (from the current sample position set for that signal)
     * <br>availableSamples(sampleNumberToSignalNumber) = getNumberOfSamples(sampleNumberToSignalNumber) - getSamplePosition(sampleNumberToSignalNumber);
     *
     * @return number of samples of the given signal available for reading
     */
    public long availableSamples(int signalNumber) {
        return getNumberOfSamples(signalNumber) - samplesPositionList[signalNumber];
    }


    /**
     * Calculate and get the total number of  DataRecords in the file.
     * <br>getNumberOfDataRecords() = availableDataRecords() + getDataRecordPosition();
     *
     * @return total number of DataRecords in the file
     */
    public int getNumberOfDataRecords() {
        return (int) (file.length() - headerInfo.getNumberOfBytesInHeaderRecord()) / (headerInfo.getDataRecordLength() * headerInfo.getFileType().getNumberOfBytesPerSample());
    }

    /**
     * Calculate and get the total number of samples of the given channel (signal)
     * in the file.
     * <br>getNumberOfSamples(sampleNumberToSignalNumber) = availableSamples(sampleNumberToSignalNumber) + getSamplePosition(sampleNumberToSignalNumber);
     *
     * @return total number of samples of the given signal in the file
     */
    public long getNumberOfSamples(int signalNumber) {
        return (long) getNumberOfDataRecords() * (long) headerInfo.getNumberOfSamplesInEachDataRecord(signalNumber);
    }


    /**
     * Close this EdfFileReader and releases any system resources associated with
     * it. This method MUST be called after finishing reading DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     *
     * @throws IORuntimeException if an I/O  occurs while closing the file reader
     */
    public void close() throws IORuntimeException {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Error while closing the file: {0}.", file);
            new IORuntimeException(errMsg, e);
        }
    }
}
