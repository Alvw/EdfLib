package com.biorecorder.edflib;

import com.biorecorder.edflib.util.EndianBitConverter;
import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base abstract class for writing DataRecords to EDF or BDF File.
 * It creates and opens a new file if it does not exist. Already existing file with the same name
 * will be silently overwritten without advance warning!!
 */
public abstract class DataRecordsFileWriter extends DataRecordsWriter {

    private long startTime;
    private long stopTime;
    private double durationOfDataRecord;
    private int dataRecordsCounter = 0;
    private File file;
    private boolean isDurationOfDataRecordsComputable;
    protected FileOutputStream fileOutputStream;

    /**
     * Creates DataRecordsFileWriter to write DataRecords to the file represented
     * by the specified File object.
     *
     * @param file the file to be opened for writing
     */
    public DataRecordsFileWriter(File file) throws IOException {
        this.file = file;
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.close();
    }


    /**
     * If true the average duration of DataRecords during writing process will be calculated
     * and the result will be written to the file header.
     *
     * <p>average duration of DataRecords = (time of coming last DataRecord - time of coming first DataRecord) / total number of DataRecords
     *
     * @param isComputable - if true duration of DataRecords will be calculated
     */
    public void setDurationOfDataRecordsComputable(boolean isComputable) {
        this.isDurationOfDataRecordsComputable = isComputable;
    }


    @Override
    public synchronized void open(RecordingConfig recordingConfig) throws IOException {
        super.open(recordingConfig);
        dataRecordsCounter = 0;
        fileOutputStream = new FileOutputStream(file);
    }

    /**
     * To be written in EDF/BDF file every int must be converted to 2 or 3 LITTLE_ENDIAN ordered bytes
     *
     * @return number of bytes per integer value (2 for EDF files and 3 for BDF files)
     */
    protected abstract int getNumberOfBytesPerSample();

    /**
     * Create file header on the base of data stored in the {@link RecordingConfig}.
     * <p>More detailed information about EDF/BDF header:
     * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
     * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
     *
     * @return EDF/BDF file header as an array of bytes
     */
    protected abstract byte[] createHeader();

    /**
     * Write ONE digital DataRecord to the file (or buffer if this file writer is buffered).
     * Take data from digitalData array starting at offset position.
     * Every int is converted to LITTLE_ENDIAN ordered bytes (2 bytes for EDF files and 3 bytes for BDF files).
     *
     * @param digitalData array with digital data
     * @param offset offset within the array at which the DataRecord starts
     *
     * @throws IOException
     */
    @Override
    public synchronized void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException {
        if (dataRecordsCounter == 0) {
            // 1 second = 1000 msec
            startTime = System.currentTimeMillis() - (long) recordingConfig.getDurationOfDataRecord()*1000;
            // setStartTime делаем только если bdfHeader.getStartTime == -1
            // если например идет копирование данных из файла в файл и
            // bdfHeader.getStartTime имеет нормальное значение то изменять его не нужно
            if(recordingConfig.getStartTime() < 0) {
                recordingConfig.setStartTime(startTime);
            }
            recordingConfig.setNumberOfDataRecords(-1);
            fileOutputStream.write(createHeader());
        }
        fileOutputStream.write(EndianBitConverter.intArrayToLittleEndianByteArray(digitalData, offset, recordingConfig.getRecordLength(), getNumberOfBytesPerSample()));
        stopTime = System.currentTimeMillis();
        durationOfDataRecord = (stopTime - startTime) * 0.001 / dataRecordsCounter;
        dataRecordsCounter++;
    }


    @Override
    public synchronized void close() throws IOException {
        if(recordingConfig.getNumberOfDataRecords() == -1) {
            recordingConfig.setNumberOfDataRecords(dataRecordsCounter);
        }
        if(isDurationOfDataRecordsComputable) {
            recordingConfig.setDurationOfDataRecord(durationOfDataRecord);
        }
        FileChannel channel = fileOutputStream.getChannel();
        channel.position(0);
        fileOutputStream.write(createHeader());
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
