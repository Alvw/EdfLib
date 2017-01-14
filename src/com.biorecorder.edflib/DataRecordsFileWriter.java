package com.biorecorder.edflib;

import com.biorecorder.edflib.util.EndianBitConverter;
import java.io.*;

/**
 * Base abstract class for writing DataRecords to EDF or BDF File.
 * It creates and opens a new file if it does not exist. Already existing file with the same name
 * will be silently overwritten without advance warning!!
 */
public abstract class DataRecordsFileWriter extends DataRecordsWriter {

    private long startTime;
    private int dataRecordsCounter = 0;
    private File file;
 //   private boolean isBuffered;
    protected OutputStream outputStream;

    /**
     * Creates DataRecordsFileWriter to write DataRecords to the file represented by the specified File object.
     * Every DataRecords will be written to the file at once, without buffering
     *
     * @param file the file to be opened for writing
     */
    public DataRecordsFileWriter(File file)   {
        this.file = file;
    }

    /**
     * If isBuffered = true, a buffered DataRecordsFileWriter will be created to write data to the
     * file represented by the specified File object.
     *
     * //@param file the file to be opened for writing
     * //@param isBuffered if true, then coming DataRecords will be buffered before writing to the file.
     */
  /*  public DataRecordsFileWriter(File file, boolean isBuffered) {
        this.file = file;
        this.isBuffered = isBuffered;
    } */


    @Override
    public synchronized void open(RecordConfig recordConfig) throws IOException {
        super.open(recordConfig);
        outputStream = new FileOutputStream(file);
        dataRecordsCounter = 0;

   /*     if(isBuffered) {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        }
        else {
            outputStream = new FileOutputStream(file);
        } */

    }

    /**
     * To be written in EDF/BDF file every int must be converted to 2 or 3 LITTLE_ENDIAN ordered bytes
     *
     * @return number of bytes storing integer value (2 for EDF files and 3 for BDF files)
     */
    protected abstract int getNumberOfBytesInSample();

    /**
     * Create file header on the base of data stored in the {@link RecordConfig}.
     * <p>More detailed information about EDF/BDF header:
     * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
     * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
     *
     * @return EDF/BDF file header as an array of bytes
     */
    protected abstract byte[] createHeader();

    /**
     * Write ONE digital DataRecord to the file (or buffer if this DataRecordsFileWriter is buffered).
     * Take data from digitalData array starting at offset position.
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
            startTime = System.currentTimeMillis() - (long) recordConfig.getDurationOfDataRecord()*1000;
            // setStartTime делаем только если bdfHeader.getStartTime == -1
            // если например идет копирование данных из файла в файл и
            // bdfHeader.getStartTime имеет нормальное значение то изменять его не нужно
            if(recordConfig.getStartTime() < 0) {
                recordConfig.setStartTime(startTime);
            }
            recordConfig.setNumberOfDataRecords(-1);
            outputStream.write(createHeader());
        }
        int recordLength = recordConfig.getRecordLength();
        outputStream.write(EndianBitConverter.intArrayToLittleEndianByteArray(digitalData, offset, recordLength, getNumberOfBytesInSample()));
        dataRecordsCounter++;

    }

    /**
     * Closes this DataRecordsFileWriter and releases any system resources associated with
     * this stream. This DataRecordsFileWriter may no longer be used for writing DataRecords.
     *
     * @throws IOException
     */
    @Override
    public synchronized void close() throws IOException {
        if(recordConfig.getNumberOfDataRecords() == -1) {
            recordConfig.setNumberOfDataRecords(dataRecordsCounter);
        }
        outputStream.close();
        rewriteHeader(recordConfig);
    }

    /**
     * Create a file header on the base of the data stored in the given {@link RecordConfig}
     * and then write resultant byte array to the beginning of the file. Stream position to write
     * DataRecords are not changed.
     *
     * @param recordConfig object with the data required to form EDF/BDF file header
     *
     * @throws IOException
     */
    public synchronized void rewriteHeader(RecordConfig recordConfig) throws IOException {
        this.recordConfig = recordConfig;
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.write(createHeader());
    }

}
