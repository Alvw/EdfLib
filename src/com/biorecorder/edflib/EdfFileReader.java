package com.biorecorder.edflib;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Permit to read DataRecords from EDF or BDF file. The structure of DataRecords is described
 * in the file header. EdfFileReader reads  and recognizes the information from the file header and
 * save it in special {@link HeaderConfig} object. After that it can correctly read data
 * from the file.
 * <p>
 * <p>EDF/BDF files contains "row" digital (int) data but they can be converted to corresponding
 * real physical floating point data on the base of header information (physical maximum and minimum
 * and digital maximum and minimum specified for every channel (signal)). So we can "read" physical values if
 * we wish - {@link #readPhysicalDataRecord()}, {@link #readPhysicalSamples(int, int)}.
 */
public class EdfFileReader {
    private HeaderConfig headerConfig;
    private FileInputStream fileInputStream;
    private File file;
    private List<Long> samplesPositionList = new ArrayList<Long>();
    private int recordPosition = 0;

    /**
     * Creates EdfFileReader to read data from the file represented by the specified File object.
     *
     * @param file the file to be opened for reading
     * @throws IOException            if the file header can not be read
     * @throws HeaderParsingException if the file header is not valid EDF/BDF file header
     */
    public EdfFileReader(File file) throws IOException, HeaderParsingException {
        headerConfig = new HeaderConfig(file);
        this.file = file;
        fileInputStream = new FileInputStream(file);
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            samplesPositionList.add(new Long(0));
        }
    }

    /**
     * Set the DataRecords position indicator to the given new position.
     * The position is measured in DataRecords. Methods {@link #readDigitalDataRecord()} and
     * {@link #readPhysicalDataRecord()} will start reading from the specified position.
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
     * Methods {@link #readDigitalSamples(int, int)} and
     * {@link #readPhysicalSamples(int, int)} will start reading
     * samples belonging to a channel from the specified for that channel position.
     *
     * @param signalNumber channel (signal) number whose sample position we change
     * @param newPosition  the new sample position, a non-negative integer counting
     *                     the number of samples belonging to the specified
     *                     channel from the beginning of the file
     */
    public void setSamplePosition(int signalNumber, long newPosition) {
        samplesPositionList.set(signalNumber, newPosition);
    }

    public void reset() {
        recordPosition = 0;
        for(int i = 0; i < samplesPositionList.size(); i++) {
            samplesPositionList.set(i, Long.valueOf(0));
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
     * @param signalNumber channel (signal) number whose position we want to get
     * @return current sample position, a non-negative integer counting
     * the number of samples belonging to the given
     * channel from the beginning of the file
     */
    public long getSamplePosition(int signalNumber) {
        return samplesPositionList.get(signalNumber);
    }

    /**
     * Read ONE DataRecord from the file starting from the specified DataRecord position.
     * The DataRecords position indicator will be increased with 1.
     * Return the "raw" digital (integer) values.
     *
     * @return array of "raw" digital values belonging to ONE DataRecord  or null if the end of file
     * has been reached or the rest of the file contains insufficient data to form entire data record
     * @throws IOException if the file could not be read
     */

    public int[] readDigitalDataRecord() throws IOException {
        int numberOfBytesPerSample = headerConfig.getFileType().getNumberOfBytesPerSample();
        long realPosition = headerConfig.getNumberOfBytesInHeaderRecord() +
                headerConfig.getRecordLength() * recordPosition * numberOfBytesPerSample;
        if (fileInputStream.getChannel().position() != realPosition) {
            fileInputStream.getChannel().position(realPosition);
        }

        int rowLength = headerConfig.getRecordLength() * numberOfBytesPerSample;
        byte[] rowData = new byte[rowLength];

        int numberOfReadBytes = fileInputStream.getChannel().read(ByteBuffer.wrap(rowData));
        if (numberOfReadBytes == rowLength) {
            recordPosition++;
            return EndianBitConverter.littleEndianByteArrayToIntArray(rowData, numberOfBytesPerSample);
        } else {
            return null;
        }
    }


    /**
     * Read ONE DataRecord from the file starting from the specified DataRecord position.
     * The DataRecords position indicator will be increased with 1.
     * The values are converted to their physical (floating points) values e.g. microVolts, beats per minute, etc.
     *
     * @return array of real physical values belonging to ONE DataRecord or null if the end of file has been
     * reached or the rest of the file contains insufficient data to form entire data record
     * @throws IOException if the file could not be read
     */
    public double[] readPhysicalDataRecord() throws IOException {
        int[] digRecord = readDigitalDataRecord();
        double[] physRecord = new double[digRecord.length];
        for(int i = 0; i < digRecord.length; i++) {
            physRecord[i] = headerConfig.digitalValueToPhysical(headerConfig.signalNumber(i+1), digRecord[i]);

        }

       /* int counter = 0;
        for (int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            for (int sampleNumber = 0; sampleNumber < headerConfig.getNumberOfSamplesInEachDataRecord(signalNumber); sampleNumber++) {
                physRecord[counter] = headerConfig.digitalValueToPhysical(signalNumber, digRecord[counter]);
                counter++;
            }
        }*/
        return physRecord;
    }

    /**
     * Helper method that reads only samples belonging to the given channel within the DataRecord at the given position.
     * Read samples will be saved in the specified ByteBuffer.
     * The values are the "raw" digital (integer) values.
     * Note the this method does note affect the DataRecord position used by the methods
     * {@link #readDigitalSamples(int, int)} and
     * {@link #readPhysicalSamples(int, int)}
     *
     * @param signalNumber   channel (signal) number whose samples must be read within the given DataRecord
     * @param recordPosition position of the DataRecord
     * @param buffer         buffer where read samples will be saved
     * @return number of read samples
     * @throws IOException if the file could not be read
     */
    private int readSamplesFromRecord(int signalNumber, int recordPosition, ByteBuffer buffer) throws IOException {
        long signalPosition = headerConfig.getRecordLength() * recordPosition;
        for (int i = 0; i < signalNumber; i++) {
            signalPosition += headerConfig.getNumberOfSamplesInEachDataRecord(i);
        }
        signalPosition = signalPosition * headerConfig.getFileType().getNumberOfBytesPerSample() + headerConfig.getNumberOfBytesInHeaderRecord();
        return fileInputStream.getChannel().read(buffer, signalPosition);
    }


    /**
     * Read the given number of samples belonging to the given channel
     * starting from the current sample position indicator.
     * The values are the "raw" digital (integer) values.
     * The sample position indicator of that channel will be increased with the amount of samples read.
     * Read samples are saved in the specified array starting at the specified offset.
     * Return the amount of samples read (this can be less than given numberOfSamples or zero!)
     *
     * @param signalNumber    channel (signal) number whose samples must be read
     * @param digArray        int array where read samples are saved
     * @param offset          offset within the array at which saving starts
     * @param numberOfSamples number of samples to read
     * @return the amount of samples read (this can be less than given numberOfSamples or zero!)
     * @throws IOException
     */
    public int readDigitalSamples(int signalNumber, int[] digArray, int offset, int numberOfSamples) throws IOException {
        int readTotal = 0;
        int samplesPerRecord = headerConfig.getNumberOfSamplesInEachDataRecord(signalNumber);
        byte[] rowData = new byte[samplesPerRecord * headerConfig.getFileType().getNumberOfBytesPerSample()];
        ByteBuffer buffer = ByteBuffer.wrap(rowData);
        int recordNumber = (int) (samplesPositionList.get(signalNumber) / samplesPerRecord);
        int positionInRecord = (int) (samplesPositionList.get(signalNumber) % samplesPerRecord);

        while (readTotal < numberOfSamples) {
            if (readSamplesFromRecord(signalNumber, recordNumber, buffer) < buffer.capacity()) {
                break;
            }
            int readInRecord = Math.min(numberOfSamples - readTotal, samplesPerRecord - positionInRecord);
            EndianBitConverter.littleEndianByteArrayToIntArray(rowData, positionInRecord * headerConfig.getFileType().getNumberOfBytesPerSample(), digArray, offset + readTotal, readInRecord, headerConfig.getFileType().getNumberOfBytesPerSample());
            readTotal += readInRecord;
            buffer.clear();
            recordNumber++;
            positionInRecord = 0;
        }
        samplesPositionList.set(signalNumber, samplesPositionList.get(signalNumber) + readTotal);
        return readTotal;
    }

    /**
     * Read the given number of samples belonging to the given channel
     * starting from the current sample position indicator.
     * The values are the "raw" digital (integer) values.
     * The sample position indicator of that channel will be increased with the amount of samples read.
     * Array with read digital samples will be returned (array length can be less than given numberOfSamples or zero!)
     *
     * @param signalNumber    channel (signal) number whose samples must be read
     * @param numberOfSamples number of samples to read
     * @return Array with read digital samples
     * @throws IOException
     */
    public int[] readDigitalSamples(int signalNumber, int numberOfSamples) throws IOException {
        if (numberOfSamples > availableSamples(signalNumber)) {
            numberOfSamples = (int) availableSamples(signalNumber);
        }
        int[] digSamples = new int[numberOfSamples];
        readDigitalSamples(signalNumber, digSamples, 0, numberOfSamples);
        return digSamples;
    }


    /**
     * Read the given number of samples belonging to the given channel starting from the current sample position indicator.
     * The values are converted to their physical (floating points) values e.g. microVolts, beats per minute, etc..
     * The sample position indicator of that channel will be increased with the amount of samples read.
     * Array with read physical samples will be returned (array length can be less than given numberOfSamples or zero!)
     *
     * @param signalNumber    channel (signal) number whose samples must be read
     * @param numberOfSamples number of samples to read
     * @return Array with read physical samples
     * @throws IOException
     */
    public double[] readPhysicalSamples(int signalNumber, int numberOfSamples) throws IOException {
        int[] digSamples = readDigitalSamples(signalNumber, numberOfSamples);
        double[] physSamples = new double[digSamples.length];
        for(int i = 0; i < numberOfSamples; i++) {
            physSamples[i] = headerConfig.digitalValueToPhysical(signalNumber, digSamples[i]);
        }
        return physSamples;
    }


    /**
     * Return the information from the file header stored in the HeaderConfig object
     *
     * @return the object containing EDF/BDF header information
     */
    public HeaderConfig getHeaderInfo() {
        return new HeaderConfig(headerConfig);
    }

    /**
     * On the base of information from the given config object
     * a new header will be created and rewritten to the file.
     * Note that the number of channels (signals) could not be changed!
     *
     * @param headerConfig config object containing info for the new header
     */
    public void rewriteHeader(HeaderConfig headerConfig) throws IOException {
        if(headerConfig.getNumberOfSignals() != this.headerConfig.getNumberOfSignals()) {
            String errMsg = "The number of signals could not be changed!  Current number of signals = "
                    + this.headerConfig.getNumberOfSignals() + " New number of signals = " + headerConfig.getNumberOfSignals();
            throw new IllegalArgumentException(errMsg);
        }

        byte[] header = headerConfig.createFileHeader();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(header);
        fileOutputStream.close();
        this.headerConfig = new HeaderConfig(headerConfig);
    }


    /**
     * Get the number of DataRecords available for reading (from the current DataRecord position).
     * <br>availableDataRecords() = getNumberOfDataRecords() - getDataRecordPosition();
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
     * <br>availableSamples(signalNumber) = getNumberOfSamples(signalNumber) - getSamplePosition(signalNumber);
     *
     * @return number of samples of the given signal available for reading
     * @throws IOException if file could not be read
     */
    public long availableSamples(int signalNumber) throws IOException {
        return getNumberOfSamples(signalNumber) - samplesPositionList.get(signalNumber);
    }


    /**
     * Calculate and get the total number of  DataRecords in the file.
     * <br>getNumberOfDataRecords() = availableDataRecords() + getDataRecordPosition();
     *
     * @return total number of DataRecords in the file
     * @throws IOException if file could not be read
     */
    public int getNumberOfDataRecords() throws IOException {
        return (int) (file.length() - headerConfig.getNumberOfBytesInHeaderRecord()) / (headerConfig.getRecordLength() * headerConfig.getFileType().getNumberOfBytesPerSample());
    }

    /**
     * Calculate and get the total number of samples of the given channel (signal)
     * in the file.
     * <br>getNumberOfSamples(signalNumber) = availableSamples(signalNumber) + getSamplePosition(signalNumber);
     *
     * @return total number of samples of the given signal in the file
     * @throws IOException if file could not be read
     */
    public long getNumberOfSamples(int signalNumber) throws IOException {
        return  (long) getNumberOfDataRecords() * (long) headerConfig.getNumberOfSamplesInEachDataRecord(signalNumber);
    }


    /**
     * Close this EdfFileReader and releases any system resources associated with
     * it. This method MUST be called after finishing reading DataRecords.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     *
     * @throws IOException if the reader can not be closed correctly
     */
    public void close() throws IOException {
        fileInputStream.close();
    }
}
