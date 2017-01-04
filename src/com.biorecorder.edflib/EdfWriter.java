package com.biorecorder.edflib;

import com.biorecorder.edflib.util.HeaderUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by gala on 25/12/16.
 */
public class EdfWriter extends DataRecordsFileWriter {
    public EdfWriter(File file) throws FileNotFoundException {
        super(file);
    }

    @Override
    protected int getNumberOfBytesInSample() {
        return 2;
    }

    @Override
    protected synchronized void writeHeader() throws IOException {
        outputStream.flush();
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.position(0);
        fileOutputStream.write(HeaderUtility.createEdfHeader(headerConfig));
    }
}
