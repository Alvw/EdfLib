package com.biorecorder.edflib;

import com.biorecorder.edflib.util.BdfParser;

import java.io.*;

/**
 *
 */
abstract class DataRecordsFileWriter extends DataRecordsWriter {

    private long startTime;
    private int numberOfDataRecords = 0;
    protected FileOutputStream fileStream;

    public DataRecordsFileWriter(File file) throws FileNotFoundException  {
        fileStream = new FileOutputStream(file);
    }


    protected abstract int getNumberOfBytesInSample();

    protected abstract void writeHeader() throws IOException;

    @Override
    protected synchronized void write(int[] data) throws IOException {
        if (numberOfDataRecords == 0) {
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
        fileStream.write(BdfParser.intArrayToByteArray(data, getNumberOfBytesInSample()));
        numberOfDataRecords += data.length / headerConfig.getRecordLength();
    }

    @Override
    public synchronized void close() throws IOException {
        headerConfig.setNumberOfDataRecords(numberOfDataRecords);
        writeHeader();
        fileStream.close();
    }
}
