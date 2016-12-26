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
    protected void writeHeader() throws IOException {
        FileChannel fileChannel = fileStream.getChannel();
        fileChannel.position(0);
        fileStream.write(HeaderUtility.createBdfHeader(headerConfig));
    }
}
