package com.biorecorder.edflib;

import com.biorecorder.edflib.base.EdfConfig;
import com.biorecorder.edflib.base.EdfWriter;
import com.biorecorder.edflib.exceptions.FileNotFoundRuntimeException;
import com.biorecorder.edflib.exceptions.EdfRuntimeException;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
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

    private File file;
    private FileType fileType;
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
     * We may do that in the constructor or by method {@link EdfWriter#setConfig(EdfConfig)}.
     *
     * @param file       the file to be opened for writing
     * @param headerInfo object containing all necessary information for the header record
     * @throws FileNotFoundRuntimeException if the file exists but is a directory rather
     * than a regular file, does not exist but cannot be created,
     * or cannot be opened for any other reason
     */
    public EdfFileWriter(File file, HeaderInfo headerInfo) throws FileNotFoundRuntimeException {
        this(file, headerInfo.getFileType());
        this.config = headerInfo;
    }

    /**
     * Creates EdfWriter to write data samples to the file represented by
     * the specified File object.  A HeaderInfo object specifying the type of the file
     * (EDF_16BIT or BDF_24BIT) and providing all necessary information for the file header record
     * must be passed to the EdfFileWriter before writing any data samples.
     * Use the method {@link EdfWriter#setConfig(EdfConfig)}.
     *
     * @param file the file to be opened for writing
     *  @param fileType    EDF_16BIT or BDF_24BIT
     * @throws FileNotFoundRuntimeException if the file exists but is a directory rather
     * than a regular file, does not exist but cannot be created,
     * or cannot be opened for any other reason
     */

    public EdfFileWriter(File file, FileType fileType) throws FileNotFoundRuntimeException {
        try {
            this.file = file;
            this.fileType = fileType;
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fileOutputStream = new FileOutputStream(file);
        } catch (Exception e) {
            String errMsg = MessageFormat.format("Writable file: {0} can not be created", file);
            throw new FileNotFoundRuntimeException(errMsg, e);
        }
    }

    @Override
    public HeaderInfo getConfig() {
        return (HeaderInfo) config;
    }

    @Override
    public void setConfig(EdfConfig edfConfig) {
       config = new HeaderInfo(edfConfig, fileType);
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
     *
     * @param physicalSamples physical samples belonging to some signal or entire DataRecord
     * @throws EdfRuntimeException  if an I/O  occurs while writing data to the file
     */
    @Override
    public void writePhysicalSamples(double[] physicalSamples) throws EdfRuntimeException {
        super.writePhysicalSamples(physicalSamples);
    }

    /**
     *
     * @param digitalSamples digital samples belonging to some signal or entire DataRecord
     * @throws EdfRuntimeException  if an I/O  occurs while writing data to the file
     */
    @Override
    public synchronized void writeDigitalSamples(int[] digitalSamples) throws EdfRuntimeException {
        try {
            HeaderInfo config = (HeaderInfo) this.config;
            if (sampleCounter == 0) {
                // 1 second = 1000 msec
                startTime = System.currentTimeMillis() - (long) config.getDurationOfDataRecord() * 1000;
                // setRecordingStartDateTimeMs делаем только если bdfHeader.getRecordingStartDateTimeMs == -1
                // если например идет копирование данных из файла в файл и
                // bdfHeader.getRecordingStartDateTimeMs имеет нормальное значение то изменять его не нужно
                if (config.getRecordingStartDateTimeMs() < 0) {
                    config.setRecordingStartDateTimeMs(startTime);
                }
                config.setNumberOfDataRecords(-1);
                fileOutputStream.write(config.createFileHeader());
            }
            fileOutputStream.write(EndianBitConverter.intArrayToLittleEndianByteArray(digitalSamples, config.getFileType().getNumberOfBytesPerSample()));
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Error while writing data to the file: {0}. Check available HD space.", file);
            throw new EdfRuntimeException(errMsg, e);
        }
        stopTime = System.currentTimeMillis();
        if (getNumberOfWrittenDataRecords() > 0) {
            durationOfDataRecord = (stopTime - startTime) * 0.001 / getNumberOfWrittenDataRecords();
        }
        sampleCounter += digitalSamples.length;
    }

    /**
     *
     * @throws EdfRuntimeException  if an I/O  occurs while closing the file writer
     */
    @Override
    public synchronized void close() throws EdfRuntimeException {
        HeaderInfo config = (HeaderInfo) this.config;
        if (config.getNumberOfDataRecords() == -1) {
            config.setNumberOfDataRecords(getNumberOfWrittenDataRecords());
        }
        if (isDurationOfDataRecordsComputable && durationOfDataRecord > 0) {
            config.setDurationOfDataRecord(durationOfDataRecord);
        }
        FileChannel channel = fileOutputStream.getChannel();

        try {
            channel.position(0);
            fileOutputStream.write(config.createFileHeader());
            fileOutputStream.close();
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Error while closing the file: {0}.", file);
            new EdfRuntimeException(errMsg, e);
        }
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
        stringBuilder.append("Number of data records = " + getNumberOfWrittenDataRecords() + "\n");
        stringBuilder.append("Actual duration of a data record = " + durationOfDataRecord);
        return stringBuilder.toString();
    }

    /**
     * Unit Test. Usage Example.
     * <p>
     * Create the file: current_project_dir/records/test.edf
     * and write to it 10 data records. Then print some file header info
     * and writing info.
     * <p>
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

        // create EdfFileWriter to write edf data to that file
        EdfFileWriter fileWriter = new EdfFileWriter(file, headerInfo);

        // create and write samples
        int[] samplesFromChannel0 = new int[channel0Frequency];
        int[] samplesFromChannel1 = new int[channel1Frequency];
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            // create random samples for channel 0
            for (int j = 0; j < samplesFromChannel0.length; j++) {
                samplesFromChannel0[j] = rand.nextInt(10000);
            }

            // create random samples for channel 1
            for (int j = 0; j < samplesFromChannel1.length; j++) {
                samplesFromChannel1[j] = rand.nextInt(1000);
            }

            // write samples from both channels to the edf file
            fileWriter.writeDigitalSamples(samplesFromChannel0);
            fileWriter.writeDigitalSamples(samplesFromChannel1);
        }

        // close EdfFileWriter. Always must be called after finishing writing DataRecords.
        fileWriter.close();

        // print some header info
        System.out.println(fileWriter.getConfig());
        // print some writing info
        System.out.println(fileWriter.getWritingInfo());

    }

}
