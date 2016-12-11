package com.biorecorder.edflib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 *   8 ascii : number of bytes in header record
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
public class BdfWriter  {
    private static final Log LOG = LogFactory.getLog(BdfWriter.class);

    private final BdfHeader bdfHeader;
    private RandomAccessFile fileToSave;
    private long startRecordingTime;
    private long stopRecordingTime;
    private int numberOfDataRecords;
    private boolean stopWritingRequest = false; // после stopWriting() не должны записываться dataRecord-ы

// TODO: 11/12/16  create correct exceptions (file cant be created and so on)
    public BdfWriter(File file,  BdfHeader bdfHeader) throws IOException  {
        this.bdfHeader = bdfHeader;
        file.createNewFile();
        fileToSave = new RandomAccessFile(file, "rw");
        fileToSave.write(createHeader());
    }


    private byte getFirstByte(){
        byte firstByte = (byte) 255;

        if (!bdfHeader.isBdf()) {   // edf
            String zeroString = "0";
            firstByte = zeroString.getBytes()[0];

        }
        return firstByte;
    }


    private String getFirstReserved(){
        String versionOfDataFormat = "24BIT"; //bdf
        if (!bdfHeader.isBdf()) {   // edf
            versionOfDataFormat = "";
        }
        return versionOfDataFormat;
    }


    private  byte[] createHeader() {
        Charset characterSet = Charset.forName("US-ASCII");
        SignalConfig[] signalList = bdfHeader.getSignals();
        int numberOfSignals = signalList.length;
        String startDateOfRecording = new SimpleDateFormat("dd.MM.yy").format(new Date(bdfHeader.getStartTime()));
        String startTimeOfRecording = new SimpleDateFormat("HH.mm.ss").format(new Date(bdfHeader.getStartTime()));
        int numberOfBytesInHeaderRecord = 256 * (1 + numberOfSignals);

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(adjustLength("", 7));  //7 not 8 because first non ascii byte (or "0" for edf) we will add later
        headerBuilder.append(adjustLength(bdfHeader.getPatientId(), 80));
        headerBuilder.append(adjustLength(bdfHeader.getRecordingId(), 80));
        headerBuilder.append(startDateOfRecording);
        headerBuilder.append(startTimeOfRecording);
        headerBuilder.append(adjustLength(Integer.toString(numberOfBytesInHeaderRecord), 8));
        headerBuilder.append(adjustLength(getFirstReserved(), 44));
        headerBuilder.append(adjustLength(Integer.toString(numberOfDataRecords), 8));
        headerBuilder.append(adjustLength(double2String(bdfHeader.getDurationOfDataRecord()), 8));
        headerBuilder.append(adjustLength(Integer.toString(numberOfSignals), 4));


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

        for (int i = 0; i < numberOfSignals; i++) {
            SignalConfig signalConfig = signalList[i];
            labels.append(adjustLength(signalConfig.getLabel(), 16));
            transducerTypes.append(adjustLength(signalConfig.getTransducerType(), 80));
            physicalDimensions.append(adjustLength(signalConfig.getPhysicalDimension(), 8));
            physicalMinimums.append(adjustLength(double2String(signalConfig.getPhysicalMin()), 8));
            physicalMaximums.append(adjustLength(double2String(signalConfig.getPhysicalMax()), 8));
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
        byteBuffer.put(getFirstByte());
        byteBuffer.put(headerBuilder.toString().getBytes(characterSet));
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
        return String.format("%f", value).replace(",", ".");
    }

    // TODO: 11/12/16 setStartTime должно делаться только когда bdfHeader.getStartTime == -1
    public synchronized void writeDataRecord(byte[] bdfDataRecord) {
        if (!stopWritingRequest) {
            if (numberOfDataRecords == 0) {
                startRecordingTime = System.currentTimeMillis() - (long) bdfHeader.getDurationOfDataRecord()*1000; //1 second (1000 msec) duration of a data record
                bdfHeader.setStartTime(startRecordingTime);
                bdfHeader.setNumberOfDataRecords(-1);
                try {
                    fileToSave.write(createHeader());
                } catch (IOException e) {
                    LOG.error(e);
                    throw new RuntimeException(e);
                }
            }
            numberOfDataRecords++;
            stopRecordingTime = System.currentTimeMillis();
            try {
                fileToSave.write(bdfDataRecord);
            } catch (IOException e) {
                LOG.error(e);
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void stopWriting(boolean isAdjustFrequency) {
        if (stopWritingRequest) return;
        stopWritingRequest = true;
        bdfHeader.setStartTime(startRecordingTime);
        bdfHeader.setNumberOfDataRecords(numberOfDataRecords);
        // if BdfProvide(device) don't have quartz we should calculate actualDurationOfDataRecord
        double actualDurationOfDataRecord = (stopRecordingTime - startRecordingTime) * 0.001 / numberOfDataRecords;
        try {
            fileToSave.seek(0);
            if(isAdjustFrequency) {
                bdfHeader.setDurationOfDataRecord(actualDurationOfDataRecord);
                fileToSave.write(createHeader());
            }
            else{
                fileToSave.write(createHeader());
            }
            fileToSave.close();
        } catch (IOException e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS");
        LOG.info("Start recording time = " + startRecordingTime + " (" + dateFormat.format(new Date(startRecordingTime)));
        LOG.info("Stop recording time = " + stopRecordingTime + " (" + dateFormat.format(new Date(stopRecordingTime)));
        LOG.info("Number of data records = " + numberOfDataRecords);
        LOG.info("Duration of a data record = " + actualDurationOfDataRecord);
    }
}
