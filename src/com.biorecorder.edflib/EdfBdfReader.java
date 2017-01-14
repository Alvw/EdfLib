package com.biorecorder.edflib;

import com.biorecorder.edflib.util.EndianBitConverter;
import com.biorecorder.edflib.util.HeaderParsingException;
import com.biorecorder.edflib.util.HeaderUtility;

import java.io.*;

/**
 * Created by gala on 01/01/17.
 */
public class EdfBdfReader {
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private boolean isBdf;
    private RecordConfig recordConfig;

    public EdfBdfReader(File file) throws IOException, HeaderParsingException {
        isBdf = HeaderUtility.isBdf(file);
        recordConfig = HeaderUtility.readHeader(file);
        fileInputStream = new FileInputStream(file);
    }

    public RecordConfig getRecordConfig() {
        return recordConfig;
    }

    public boolean isBdf() {
        return isBdf;
    }

    private int getNumberOfBytesInDataSample() {
        if(isBdf) {
            return 3;

        } else {
            return 2;
        }
    }

    public int availableDataRecords() throws IOException {
        if(bufferedInputStream == null) {
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.skip(recordConfig.getNumberOfBytesInHeader());
        }
        return bufferedInputStream.available() / (recordConfig.getRecordLength() * getNumberOfBytesInDataSample());
    }

    /**
     * Read ONE data record from file
     * @return data record or null if the end of file has been reached or
     * the rest of the file contains insufficient data to form entire data record
     * @throws IOException
     */
    public int[] readDataRecord() throws IOException {
        if(bufferedInputStream == null) {
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.skip(recordConfig.getNumberOfBytesInHeader());
        }
        int rowLength = recordConfig.getRecordLength() * getNumberOfBytesInDataSample();
        byte[] rowData = new byte[rowLength];
        bufferedInputStream.mark(rowLength);
        if(bufferedInputStream.read(rowData) < rowLength) { // returns numOfBytesRead or -1 at EOF
            bufferedInputStream.reset();
            return null;
        }
        else{
            return EndianBitConverter.littleEndianByteArrayToIntArray(rowData, getNumberOfBytesInDataSample());

        }
    }



    public void close() throws IOException {
        if(bufferedInputStream != null) {
            bufferedInputStream.close();
        }
        else {
            fileInputStream.close();
        }

    }
}
