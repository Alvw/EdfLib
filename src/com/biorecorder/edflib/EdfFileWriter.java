package com.biorecorder.edflib;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Class for writing DataRecords to the EDF or BDF File.
 * It creates a file if it does not exist.
 * Already existing file with the same name
 * will be silently overwritten without advance warning!!
 * <p>
 * To make it possible to write DataRecords to the file, we must open
 * the EdfFileWriter first and pass a
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
 * @see EdfWriter
 * @see EdfFileWriter
 */
public class EdfFileWriter extends EdfWriter {

    private long startTime;
    private long stopTime;
    private File file;
    private double durationOfDataRecord;
    private boolean isDurationOfDataRecordsComputable;
    private FileOutputStream fileOutputStream;


    /**
     * Creates EdfWriter to write DataRecords to the file represented by
     * the specified File object.  EDF or BDF file will be created depending on the
     * given file type.
     *
     * @param file the file to be opened for writing
     * @throws IOException
     */
    public EdfFileWriter(File file) throws IOException {
        this.file = file;
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.close();
    }

    /**
     * Create a {@link File} with the given filename and call the other constructor
     * {@link #EdfFileWriter(File)}
     *
     * @param filename the system-dependent filename
     * @throws IOException
     */

    public EdfFileWriter(String filename) throws IOException {
        this(new File(filename));
    }

    @Override
    public synchronized void open(HeaderConfig headerConfig) throws IOException {
        super.open(headerConfig);
        sampleCounter = 0;
        fileOutputStream = new FileOutputStream(file);
    }


    /**
     * If true the average duration of DataRecords during writing process will be calculated
     * and the result will be written to the file header.
     * <p>
     * Average duration of DataRecords = (time of coming last DataRecord - time of coming first DataRecord) / total number of DataRecords
     *
     * @param isComputable - if true duration of DataRecords will be calculated
     */
    public void setDurationOfDataRecordsComputable(boolean isComputable) {
        this.isDurationOfDataRecordsComputable = isComputable;
    }



    /**
     * Write digital samples to the file.
     * Every int is converted to LITTLE_ENDIAN ordered bytes (2 bytes for EDF files and 3 bytes for BDF files).
     *
     * @param digitalSamples array with digital samples
     * @throws IOException
     */
    @Override
    public synchronized void writeDigitalSamples(int[] digitalSamples) throws IOException {
        if (sampleCounter == 0) {
            // 1 second = 1000 msec
            startTime = System.currentTimeMillis() - (long) headerConfig.getDurationOfDataRecord() * 1000;
            // setRecordingStartTime делаем только если bdfHeader.getRecordingStartTime == -1
            // если например идет копирование данных из файла в файл и
            // bdfHeader.getRecordingStartTime имеет нормальное значение то изменять его не нужно
            if (headerConfig.getRecordingStartTime() < 0) {
                headerConfig.setRecordingStartTime(startTime);
            }
            headerConfig.setNumberOfDataRecords(-1);
            fileOutputStream.write(headerConfig.createFileHeader());
        }
        fileOutputStream.write(EndianBitConverter.intArrayToLittleEndianByteArray(digitalSamples, headerConfig.getFileType().getNumberOfBytesPerSample()));
        stopTime = System.currentTimeMillis();
        durationOfDataRecord = (stopTime - startTime) * 0.001 / countRecords();
        sampleCounter += digitalSamples.length;
    }


    @Override
    public synchronized void close() throws IOException {
        if (headerConfig.getNumberOfDataRecords() == -1) {
            headerConfig.setNumberOfDataRecords(countRecords());
        }
        if (isDurationOfDataRecordsComputable) {
            headerConfig.setDurationOfDataRecord(durationOfDataRecord);
        }
        FileChannel channel = fileOutputStream.getChannel();
        channel.position(0);
        fileOutputStream.write(headerConfig.createFileHeader());
        fileOutputStream.close();
    }

    /**
     * Get info about writing process: start recording time, stop recording time,
     * number of written DataRecords, average duration of DataRecords.
     *
     * @return string with info about writing process
     */
    public String getWritingInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append("Start recording time = " + startTime + " (" + dateFormat.format(new Date(startTime)) + ") \n");
        stringBuilder.append("Stop recording time = " + stopTime + " (" + dateFormat.format(new Date(stopTime)) + ") \n");
        stringBuilder.append("Number of data records = " + countRecords() + "\n");
        stringBuilder.append("Duration of a data record = " + durationOfDataRecord);
        return stringBuilder.toString();
    }
    /**
     * Unit Test. Usage Example.
     * <p>
     * Create the file: current_project_dir/records/test.edf
     * and write to it 10 data records. Then print some file header info
     * and writing info.
     *<p>
     * Data records has the following structure:
     * <br>duration of data records = 1 sec (default)
     * <br>number of channels = 2;
     * <br>number of samples from channel 0 in each data record (data package) = 50 (sample frequency 50Hz);
     * <br>number of samples from channel 1 in each data record (data package) = 5 (sample frequency 5 Hz);
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        File recordsDir = new File(System.getProperty("user.dir"), "records");
        // create file
        File file = new File(recordsDir, "test.edf");
        try {
            // create EdfFileWriter to write edf data to that file
            EdfFileWriter fileWriter = new EdfFileWriter(file);

            // create header info for the file describing data records structure
            HeaderConfig headerConfig = new HeaderConfig(2, FileType.EDF_16BIT);
            headerConfig.setLabel(0, "first channel");
            headerConfig.setNumberOfSamplesInEachDataRecord(0, 50);
            headerConfig.setLabel(0, "second channel");
            headerConfig.setNumberOfSamplesInEachDataRecord(1, 5);

            // open EdfFileWriter giving it the header info.
            // Now it is ready to write data records
            fileWriter.open(headerConfig);

            // create and write samples
            int channel0Frequency = 50; // Hz
            int channel1Frequency = 5; // Hz
            int[] samplesFromChannel0 = new int[channel0Frequency];
            int[] samplesFromChannel1 = new int[channel1Frequency];
            Random rand = new Random();
            for(int i = 0; i < 10; i++) {
                // create random samples for channel 0
                for(int j = 0; j < samplesFromChannel0.length; j++) {
                    samplesFromChannel0[j] = rand.nextInt(10000);
                }

                // create random samples for channel 1
                for(int j = 0; j < samplesFromChannel1.length; j++) {
                    samplesFromChannel1[j] = rand.nextInt(10000);
                }

                // write samples from both channels to the edf file
                fileWriter.writeDigitalSamples(samplesFromChannel0);
                fileWriter.writeDigitalSamples(samplesFromChannel1);
            }
            // close EdfFileWriter. Always must be done after finishing to write data records
            fileWriter.close();

            // print some header info
            System.out.println(fileWriter.getHeaderInfo().headerToString());
            // print some writing info
            System.out.println(fileWriter.getWritingInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
