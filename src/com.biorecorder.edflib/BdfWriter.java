package com.biorecorder.edflib;

import com.biorecorder.edflib.util.HeaderUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 */
public class BdfWriter extends  DataRecordsFileWriter {

    public BdfWriter(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    protected int getNumberOfBytesInSample() {
        return 3;
    }

    @Override
    protected synchronized void writeHeader() throws IOException {
        outputStream.flush();
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.position(0);
        fileOutputStream.write(HeaderUtility.createBdfHeader(headerConfig));
    }
}
