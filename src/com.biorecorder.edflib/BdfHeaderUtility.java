package com.biorecorder.edflib;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *   HEADER RECORD (we suggest to also adopt the 12 simple additional EDF+ specs)
 *   8 ascii : version of this data format (0)
 *   80 ascii : local patient identification (mind item 3 of the additional EDF+ specs)
 *   80 ascii : local recording identification (mind item 4 of the additional EDF+ specs)
 *   8 ascii : startdate of recording (dd.mm.yy) (mind item 2 of the additional EDF+ specs)
 *   8 ascii : starttime of recording (hh.mm.ss)
 *   8 ascii : number of bytes in header record (The header record contains 256 + (ns * 256) bytes)
 *   44 ascii : reserved
 *   8 ascii : number of data records (-1 if unknown, obey item 10 of the additional EDF+ specs)
 *   8 ascii : duration of a data record, in seconds
 *   4 ascii : number of signals (ns) in data record
 *   ns * 16 ascii : ns * label (e.g. EEG Fpz-Cz or Body temp) (mind item 9 of the additional EDF+ specs)
 *   ns * 80 ascii : ns * transducer type (e.g. AgAgCl electrode)
 *   ns * 8 ascii : ns * physical dimension (e.g. uV or degreeC)
 *   ns * 8 ascii : ns * physical minimum (e.g. -500 or 34)
 *   ns * 8 ascii : ns * physical maximum (e.g. 500 or 40)
 *   ns * 8 ascii : ns * digital minimum (e.g. -2048)
 *   ns * 8 ascii : ns * digital maximum (e.g. 2047)
 *   ns * 80 ascii : ns * prefiltering (e.g. HP:0.1Hz LP:75Hz)
 *   ns * 8 ascii : ns * nr of samples in each data record
 *   ns * 32 ascii : ns * reserved
 *
 *  EDF specification   http://www.edfplus.info/specs/edf.html
 *  EDF/BDF difference  http://www.biosemi.com/faq/file_format.htm
 */
public class BdfHeaderUtility {
    private static Charset ASCII = Charset.forName("US-ASCII");

    public static void writeHeader(RandomAccessFile randomAccessFile, BdfHeader bdfHeader) throws IOException {
        randomAccessFile.seek(0);
        randomAccessFile.write(createHeader(bdfHeader));
    }

    public static void writeHeader(File file, BdfHeader bdfHeader) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "w");
        writeHeader(randomAccessFile, bdfHeader);
        randomAccessFile.close();
    }



    private static String getVersion(boolean isBdf){
        if(isBdf){
            return "BIOSEMI"; //bdf
        }else {
            return ""; //edf
        }
    }

    private static byte getFirstByte(boolean isBdf){
        if(isBdf){
            return (byte) 255; //bdf
        }else {
            String zeroString = "0";  //edf  "0" (ASCII)
            return zeroString.getBytes(ASCII)[0]; // or  return (int) '0';
        }
    }

   // Version of data format in BIOSEMI: http://www.biosemi.com/faq/file_format.htm
    private static String getFirstReserved(boolean isBdf){
        if(isBdf){
            return "24BIT"; //bdf
        }else {
            return ""; //edf
        }
    }

    private static  byte[] createHeader(BdfHeader bdfHeader) {
        SignalConfig[] signalList = bdfHeader.getSignals();

        String startDateOfRecording = new SimpleDateFormat("dd.MM.yy").format(new Date(bdfHeader.getStartTime()));
        String startTimeOfRecording = new SimpleDateFormat("HH.mm.ss").format(new Date(bdfHeader.getStartTime()));

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(adjustLength(getVersion(bdfHeader.isBdf()), 7));  //7 not 8 because first non ascii byte (or "0" for edf) we will add later
        headerBuilder.append(adjustLength(bdfHeader.getPatientId(), 80));
        headerBuilder.append(adjustLength(bdfHeader.getRecordingId(), 80));
        headerBuilder.append(startDateOfRecording);
        headerBuilder.append(startTimeOfRecording);
        headerBuilder.append(adjustLength(Integer.toString(bdfHeader.getNumberOfBytesInHeader()), 8));
        headerBuilder.append(adjustLength(getFirstReserved(bdfHeader.isBdf()), 44));
        headerBuilder.append(adjustLength(Integer.toString(bdfHeader.getNumberOfDataRecords()), 8));
        headerBuilder.append(adjustLength(double2String(bdfHeader.getDurationOfDataRecord()), 8));
        headerBuilder.append(adjustLength(Integer.toString(bdfHeader.getNumberOfSignals()), 4));


        StringBuilder labels = new StringBuilder();
        StringBuilder transducerTypes = new StringBuilder();
        StringBuilder physicalDimensions = new StringBuilder();
        StringBuilder physicalMinimums = new StringBuilder();
        StringBuilder physicalMaximums = new StringBuilder();
        StringBuilder digitalMinimums = new StringBuilder();
        StringBuilder digitalMaximums = new StringBuilder();
        StringBuilder preFilterings = new StringBuilder();
        StringBuilder samplesNumbers = new StringBuilder();
        StringBuilder reservedForChannels = new StringBuilder();

        for (int i = 0; i < bdfHeader.getNumberOfSignals(); i++) {
            SignalConfig signalConfig = signalList[i];
            labels.append(adjustLength(signalConfig.getLabel(), 16));
            transducerTypes.append(adjustLength(signalConfig.getTransducerType(), 80));
            physicalDimensions.append(adjustLength(signalConfig.getPhysicalDimension(), 8));
            physicalMinimums.append(adjustLength(String.valueOf(signalConfig.getPhysicalMin()), 8));
            physicalMaximums.append(adjustLength(String.valueOf(signalConfig.getPhysicalMax()), 8));
            digitalMinimums.append(adjustLength(String.valueOf(signalConfig.getDigitalMin()), 8));
            digitalMaximums.append(adjustLength(String.valueOf(signalConfig.getDigitalMax()), 8));
            preFilterings.append(adjustLength(signalConfig.getPrefiltering(), 80));
            samplesNumbers.append(adjustLength(Integer.toString(signalConfig.getNumberOfSamplesInEachDataRecord()), 8));
            reservedForChannels.append(adjustLength("", 32));
        }

        headerBuilder.append(labels);
        headerBuilder.append(transducerTypes);
        headerBuilder.append(physicalDimensions);
        headerBuilder.append(physicalMinimums);
        headerBuilder.append(physicalMaximums);
        headerBuilder.append(digitalMinimums);
        headerBuilder.append(digitalMaximums);
        headerBuilder.append(preFilterings);
        headerBuilder.append(samplesNumbers);
        headerBuilder.append(reservedForChannels);
        // reserve space for first byte
        ByteBuffer byteBuffer = ByteBuffer.allocate(headerBuilder.length() + 1);
        byteBuffer.put(getFirstByte(bdfHeader.isBdf()));
        byteBuffer.put(headerBuilder.toString().getBytes(ASCII));
        return byteBuffer.array();
    }


    /**
     * if the String.length() is more then the given length we cut the String
     * if the String.length() is less then the given length we append spaces to the end of the String
     */
    private static String adjustLength(String text, int length) {
        StringBuilder sB = new StringBuilder(text);
        if (text.length() > length) {
            sB.delete(length, text.length());
        } else {
            for (int i = text.length(); i < length; i++) {
                sB.append(" ");
            }
        }
        return sB.toString();
    }

    private static String double2String(double value ) {
        return String.format("%.6f", value).replace(",", ".");
    }


}
