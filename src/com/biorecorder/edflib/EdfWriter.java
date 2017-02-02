package com.biorecorder.edflib;

import com.biorecorder.edflib.base.DataRecordsWriter;
import com.biorecorder.edflib.base.EndianBitConverter;
import com.biorecorder.edflib.base.HeaderConfig;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class for writing DataRecords to the EDF or BDF File.
 * It creates a file if it does not exist.
 * Already existing file with the same name
 * will be silently overwritten without advance warning!!
 *<p>
 * To make it possible to write DataRecords to the file, we must open
 * the EdfWriter first and pass a
 * {@link HeaderConfig} object with the configuration information for the file header record.
 * That is, call the method {@link #open(HeaderConfig)}.
 * Only after that DataRecords and samples could be written correctly.
 * <p>
 * We may write <b>digital</b> or <b>physical</b> DataRecords and samples. Every physical (floating point) value
 * will be converted to the corresponding digital (int) value
 * using physical maximum, physical minimum, digital maximum and digital minimum
 * of the signal.
 * <p>
 * Every digital (int) value is converted
 * to 2 LITTLE_ENDIAN ordered bytes (16 bits) for EDF files or
 * to 3 LITTLE_ENDIAN ordered bytes (24 bits) for BDF files
 * and in this form written to the file.
 *
 * @see DataRecordsWriter
 * @see EdfWriter
 */
public class EdfWriter extends DataRecordsWriter {

    private long startTime;
    private long stopTime;
    private double durationOfDataRecord;
    private int dataRecordsCounter = 0;
    private File file;
    private boolean isDurationOfDataRecordsComputable;
    private FileOutputStream fileOutputStream;
    private FileType fileType;


    /**
     * Creates EdfWriter to write DataRecords to the file represented by
     * the specified File object.  EDF or BDF file will be created depending on the
     * given file type.
     *
     * @param file the file to be opened for writing
     * @param fileType type of the file: EDF (16 bits) or BDF (24 bits)
     * @throws IOException
     */
    public EdfWriter(File file, FileType fileType) throws IOException {
        this.file = file;
        this.fileType = fileType;
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.close();
    }

    /**
     * Create a {@link File} with the given filename and call the other constructor
     * {@link #EdfWriter(File, FileType)}
     *
     * @param filename the system-dependent filename
     * @param fileType type of the file: EDF (16 bits) or BDF (24 bits)
     * @throws IOException
     */

    public EdfWriter(String filename, FileType fileType) throws IOException {
        this(new File(filename), fileType);
    }


    /**
     * If true the average duration of DataRecords during writing process will be calculated
     * and the result will be written to the file header.
     * <p>
     * <p>average duration of DataRecords = (time of coming last DataRecord - time of coming first DataRecord) / total number of DataRecords
     *
     * @param isComputable - if true duration of DataRecords will be calculated
     */
    public void setDurationOfDataRecordsComputable(boolean isComputable) {
        this.isDurationOfDataRecordsComputable = isComputable;
    }


    @Override
    public synchronized void open(HeaderConfig headerConfig) throws IOException {
        super.open(headerConfig);
        dataRecordsCounter = 0;
        fileOutputStream = new FileOutputStream(file);
    }

    /**
     * Write ONE digital DataRecord to the file.
     * Take data from digitalData array starting at offset position.
     * Every int is converted to LITTLE_ENDIAN ordered bytes (2 bytes for EDF files and 3 bytes for BDF files).
     *
     * @param digitalData array with digital data
     * @param offset      offset within the array at which the DataRecord starts
     * @throws IOException
     */
    @Override
    public synchronized void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        if (dataRecordsCounter == 0) {
            // 1 second = 1000 msec
            startTime = System.currentTimeMillis() - (long) headerConfig.getDurationOfDataRecord() * 1000;
            // setRecordingStartTime делаем только если bdfHeader.getRecordingStartTime == -1
            // если например идет копирование данных из файла в файл и
            // bdfHeader.getRecordingStartTime имеет нормальное значение то изменять его не нужно
            if (headerConfig.getRecordingStartTime() < 0) {
                headerConfig.setRecordingStartTime(startTime);
            }
            headerConfig.setNumberOfDataRecords(-1);
            fileOutputStream.write(HeaderUtility.createHeader(headerConfig, fileType));
        }
        int numberOfBytesPerSample = 0;
        switch (fileType) {
            case BDF_24BIT: numberOfBytesPerSample = 3; break;
            case EDF_16BIT: numberOfBytesPerSample = 2; break;
            default:
        }
        fileOutputStream.write(EndianBitConverter.intArrayToLittleEndianByteArray(digitalData, offset, headerConfig.getRecordLength(), numberOfBytesPerSample));
        stopTime = System.currentTimeMillis();
        durationOfDataRecord = (stopTime - startTime) * 0.001 / dataRecordsCounter;
        dataRecordsCounter++;
    }


    @Override
    public synchronized void close() throws IOException {
        if (headerConfig.getNumberOfDataRecords() == -1) {
            headerConfig.setNumberOfDataRecords(dataRecordsCounter);
        }
        if (isDurationOfDataRecordsComputable) {
            headerConfig.setDurationOfDataRecord(durationOfDataRecord);
        }
        FileChannel channel = fileOutputStream.getChannel();
        channel.position(0);
        fileOutputStream.write(HeaderUtility.createHeader(headerConfig, fileType));
        fileOutputStream.close();
    }

    /**
     * Get info about writing process: start recording time, stop recording time,
     * number of written DataRecords, average duration of DataRecords.
     *
     * @return string with info about writing process
     */
    public String getWritingInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS");
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append("Start recording time = " + startTime + " (" + dateFormat.format(new Date(startTime)) + ") \n");
        stringBuilder.append("Stop recording time = " + stopTime + " (" + dateFormat.format(new Date(stopTime)) + ") \n");
        stringBuilder.append("Number of data records = " + dataRecordsCounter + "\n");
        stringBuilder.append("Duration of a data record = " + durationOfDataRecord);
        return stringBuilder.toString();
    }

}
