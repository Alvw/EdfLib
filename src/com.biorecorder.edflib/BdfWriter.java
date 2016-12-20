package com.biorecorder.edflib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;


public class BdfWriter  {
    private static final Log LOG = LogFactory.getLog(BdfWriter.class);
    private final BdfHeader bdfHeader;
    private RandomAccessFile fileToSave;
    private long startRecordingTime;
    private long stopRecordingTime;
    private int numberOfDataRecords = 0;
    private volatile boolean isClosed = false; // после close() DataRecords не должны записываться
    private File file;

    public BdfWriter(File file,  BdfHeader bdfHeader) throws FileNotFoundException {
        this.bdfHeader = bdfHeader;
        this.file = file;
        fileToSave = new RandomAccessFile(file, "rw");
    }


    public synchronized void writeDataRecord(byte[] bdfDataRecord) throws IOException {
        if (!isClosed) {
            if (numberOfDataRecords == 0) {
                startRecordingTime = System.currentTimeMillis() - (long) bdfHeader.getDurationOfDataRecord()*1000; //1 second (1000 msec) duration of a data record
                // setStartTime делаем только если bdfHeader.getStartTime == -1
                // если например идет копирование данных из файла в файл и bdfHeader.getStartTime имеет нормальное значение то изменять его не нужно
                if(bdfHeader.getStartTime() < 0) {
                    bdfHeader.setStartTime(startRecordingTime);
                }
                bdfHeader.setNumberOfDataRecords(-1);
                BdfHeaderUtility.writeHeader(fileToSave, bdfHeader);
            }
            numberOfDataRecords++;
            stopRecordingTime = System.currentTimeMillis();
            fileToSave.write(bdfDataRecord);
        } else {
            String errMsg = "File: "+file+" is closed. Data could not be written";
            throw new IOException(errMsg);
        }
    }

    public synchronized void writeDataRecord(int[] bdfDataRecord) throws IOException {
        int numberOfBytesInDataFormat = bdfHeader.isBdf() ? 3 : 2;
        writeDataRecord(BdfParser.intArrayToByteArray(bdfDataRecord, numberOfBytesInDataFormat));
    }

    public synchronized void close(boolean isAdjustFrequency) throws IOException {
        if (isClosed) return;
        isClosed = true;
        bdfHeader.setNumberOfDataRecords(numberOfDataRecords);
        // calculate actualDurationOfDataRecord
        double actualDurationOfDataRecord = (stopRecordingTime - startRecordingTime) * 0.001 / numberOfDataRecords;
        if(isAdjustFrequency) {
            bdfHeader.setDurationOfDataRecord(actualDurationOfDataRecord);

        }
        BdfHeaderUtility.writeHeader(fileToSave, bdfHeader);
        fileToSave.close();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS");
        LOG.info("Start recording time = " + startRecordingTime + " (" + dateFormat.format(new Date(startRecordingTime)));
        LOG.info("Stop recording time = " + stopRecordingTime + " (" + dateFormat.format(new Date(stopRecordingTime)));
        LOG.info("Number of data records = " + numberOfDataRecords);
        LOG.info("Duration of a data record = " + actualDurationOfDataRecord);
    }
}
