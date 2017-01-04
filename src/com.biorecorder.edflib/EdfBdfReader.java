package com.biorecorder.edflib;

import com.biorecorder.edflib.util.BdfParser;
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
    private HeaderConfig headerConfig;

    public EdfBdfReader(File file) throws IOException, HeaderParsingException {
        isBdf = HeaderUtility.isBdf(file);
        headerConfig = HeaderUtility.readHeader(file);
        fileInputStream = new FileInputStream(file);
    }

    public HeaderConfig getHeaderConfig() {
        return headerConfig;
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
            bufferedInputStream.skip(headerConfig.getNumberOfBytesInHeader());
        }
        return bufferedInputStream.available() / (headerConfig.getRecordLength() * getNumberOfBytesInDataSample());
    }


    public int[] readDataRecord() throws IOException {
        if(bufferedInputStream == null) {
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.skip(headerConfig.getNumberOfBytesInHeader());
        }
        int rowLength = headerConfig.getRecordLength() * getNumberOfBytesInDataSample();
        byte[] rowData = new byte[rowLength];
        bufferedInputStream.mark(rowLength);
        if(bufferedInputStream.read(rowData) < rowLength) { // returns numOfBytesRead or -1 at EOF
            bufferedInputStream.reset();
            return null;
        }
        else{
            return BdfParser.byteArrayToIntArray(rowData, getNumberOfBytesInDataSample());

        }
    }

    public void close() throws IOException {
        bufferedInputStream.close();
    }
}
