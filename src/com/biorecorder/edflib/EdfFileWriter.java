package com.biorecorder.edflib;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * This class permits to write digital or physical samples
 * from multiple measuring channel to  EDF or BDF File.
 * Every channel (signal) has its own sample frequency.
 * <p>
 * If the file does not exist it will be created.
 * Already existing file with the same name
 * will be silently overwritten without advance warning!!
 * <p>
 * To write data samples to the file we must provide it
 * a {@link HeaderInfo} object with the configuration information.
 * Only after that data samples could be written correctly.
 * <p>
 * We may write <b>digital</b> or <b>physical</b>  samples.
 * Every physical (floating point) sample
 * will be converted to the corresponding digital (int) one
 * using physical maximum, physical minimum, digital maximum and digital minimum of the signal.
 * <p>
 * Every digital (int) value will be converted
 * to 2 LITTLE_ENDIAN ordered bytes (16 bits) for EDF files or
 * to 3 LITTLE_ENDIAN ordered bytes (24 bits) for BDF files
 * and in this form written to the file.
 *
 * @see EdfWriter
 */
public class EdfFileWriter extends EdfWriter {

    private long startTime;
    private long stopTime;
    private double durationOfDataRecord;
    private boolean isDurationOfDataRecordsComputable;
    private FileOutputStream fileOutputStream;

    /**
     * Creates EdfFileWriter to write data samples to the file represented by
     * the specified File object. HeaderInfo object specifies the type of the file
     * (EDF_16BIT or BDF_24BIT) and provides all necessary information for the file header record.
     * A HeaderInfo object must be passed to the EdfFileWriter before writing any data samples.
     * We may do that in the constructor or by method {@link #setHeader(HeaderInfo)}.
     *
     *
     * @param file the file to be opened for writing
     * @param headerInfo object containing all necessary information for the header record
     * @throws IOException
     */
    public EdfFileWriter(File file, HeaderInfo headerInfo) throws IOException {
        this(file);
        this.headerInfo = headerInfo;
    }

    /**
     * Creates EdfWriter to write data samples to the file represented by
     * the specified File object.  A HeaderInfo object specifying the type of the file
     * (EDF_16BIT or BDF_24BIT) and providing all necessary information for the file header record
     * must be passed to the EdfFileWriter before writing any data samples.
     * Use the method {@link #setHeader(HeaderInfo)}.
     *
     * @param file the file to be opened for writing
     * @throws IOException
     */
    public EdfFileWriter(File file) throws IOException {
        File dir = file.getParentFile();
        if(!dir.exists()) {
            dir.mkdirs();
        }
        file.createNewFile();
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



    @Override
    public synchronized void writeDigitalSamples(int[] digitalSamples) throws IOException {
        if (sampleCounter == 0) {
            if(headerInfo == null) {
                throw new RuntimeException("File header is not specified! HeaderInfo = "+headerInfo);
            }
            // 1 second = 1000 msec
            startTime = System.currentTimeMillis() - (long) headerInfo.getDurationOfDataRecord() * 1000;
            // setRecordingStartDateTimeMs делаем только если bdfHeader.getRecordingStartDateTimeMs == -1
            // если например идет копирование данных из файла в файл и
            // bdfHeader.getRecordingStartDateTimeMs имеет нормальное значение то изменять его не нужно
            if (headerInfo.getRecordingStartDateTimeMs() < 0) {
                headerInfo.setRecordingStartDateTimeMs(startTime);
            }
            headerInfo.setNumberOfDataRecords(-1);
            fileOutputStream.write(headerInfo.createFileHeader());
        }
        fileOutputStream.write(EndianBitConverter.intArrayToLittleEndianByteArray(digitalSamples, headerInfo.getFileType().getNumberOfBytesPerSample()));
        stopTime = System.currentTimeMillis();
        if(countRecords() > 0) {
            durationOfDataRecord = (stopTime - startTime) * 0.001 / countRecords();
        }
        sampleCounter += digitalSamples.length;
    }


    @Override
    public synchronized void close() throws IOException {
        if (headerInfo.getNumberOfDataRecords() == -1) {
            headerInfo.setNumberOfDataRecords(countRecords());
        }
        if (isDurationOfDataRecordsComputable && durationOfDataRecord > 0) {
            headerInfo.setDurationOfDataRecord(durationOfDataRecord);
        }
        FileChannel channel = fileOutputStream.getChannel();
        channel.position(0);
        fileOutputStream.write(headerInfo.createFileHeader());
        fileOutputStream.close();
    }

    /**
     * Gets some info about file writing process: start recording time, stop recording time,
     * number of written DataRecords, average duration of DataRecords.
     *
     * @return string with some info about writing process
     */
    public String getWritingInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append("Start recording time = " + startTime + " (" + dateFormat.format(new Date(startTime)) + ") \n");
        stringBuilder.append("Stop recording time = " + stopTime + " (" + dateFormat.format(new Date(stopTime)) + ") \n");
        stringBuilder.append("Number of data records = " + countRecords() + "\n");
        stringBuilder.append("Actual duration of a data record = " + durationOfDataRecord);
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
        int channel0Frequency = 50; // Hz
        int channel1Frequency = 5; // Hz

        // create header info for the file describing data records structure
        HeaderInfo headerInfo = new HeaderInfo(2, FileType.EDF_16BIT);
        // Signal numbering starts from 0!
        // configure signal (channel) number 0
        headerInfo.setSampleFrequency(0, channel0Frequency);
        headerInfo.setLabel(0, "first channel");
        headerInfo.setPhysicalRange(0, -500, 500);
        headerInfo.setDigitalRange(0, -2048, -2047);
        headerInfo.setPhysicalDimension(0, "uV");

        // configure signal (channel) number 1
        headerInfo.setSampleFrequency(1, channel1Frequency);
        headerInfo.setLabel(1, "second channel");
        headerInfo.setPhysicalRange(1, 100, 300);


        // create file
        File recordsDir = new File(System.getProperty("user.dir"), "records");
        File file = new File(recordsDir, "test.edf");

        try {
            // create EdfFileWriter to write edf data to that file
            EdfFileWriter fileWriter = new EdfFileWriter(file, headerInfo);

            // create and write samples
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
                    samplesFromChannel1[j] = rand.nextInt(1000);
                }

                // write samples from both channels to the edf file
                fileWriter.writeDigitalSamples(samplesFromChannel0);
                fileWriter.writeDigitalSamples(samplesFromChannel1);
            }

            // close EdfFileWriter. Always must be called after finishing writing DataRecords.
            fileWriter.close();

            // print some header info
            System.out.println(fileWriter.getHeader());
            // print some writing info
            System.out.println(fileWriter.getWritingInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
