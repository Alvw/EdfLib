package com.biorecorder.edflib;

import com.biorecorder.edflib.util.BdfParser;

import java.io.*;

/**
 *
 */
abstract class DataRecordsFileWriter extends DataRecordsWriter {

    private long startTime;
    private int dataRecordsCounter = 0;
    protected FileOutputStream fileOutputStream;
    protected OutputStream outputStream;


    public DataRecordsFileWriter(File file) throws FileNotFoundException  {
         fileOutputStream = new FileOutputStream(file);
         outputStream = fileOutputStream;
    }

    public void setBuffered(boolean isBuffered) {
        if(isBuffered) {
            outputStream = new BufferedOutputStream(fileOutputStream);
        }
        else {
            outputStream = fileOutputStream;
        }
    }


    protected abstract int getNumberOfBytesInSample();

    protected abstract void writeHeader() throws IOException;

    @Override
    protected synchronized void writeOneDataRecord(int[] data, int offset) throws IOException {
        if (dataRecordsCounter == 0) {
            // 1 second = 1000 msec
            startTime = System.currentTimeMillis() - (long) headerConfig.getDurationOfDataRecord()*1000;
            // setStartTime делаем только если bdfHeader.getStartTime == -1
            // если например идет копирование данных из файла в файл и bdfHeader.getStartTime имеет нормальное значение то изменять его не нужно
            if(headerConfig.getStartTime() < 0) {
                headerConfig.setStartTime(startTime);
            }
            headerConfig.setNumberOfDataRecords(-1);
            writeHeader();
        }

        outputStream.write(BdfParser.intArrayToByteArray(data, offset, headerConfig.getRecordLength(), getNumberOfBytesInSample()));
        dataRecordsCounter++;

    }


    @Override
    public synchronized void close() throws IOException {
        headerConfig.setNumberOfDataRecords(dataRecordsCounter);
        writeHeader();
        outputStream.close();
    }
}
