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
    protected void writeHeader() throws IOException {
        FileChannel fileChannel = fileStream.getChannel();
        fileChannel.position(0);
        fileStream.write(HeaderUtility.createEdfHeader(headerConfig));
    }
}
