package com.biorecorder.edflib.util;

import com.biorecorder.edflib.HeaderConfig;
import com.biorecorder.edflib.SignalConfig;

import java.io.*;
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
public class HeaderUtility {
    private static Charset ASCII = Charset.forName("US-ASCII");

    private static final int VERSION_LENGTH = 8;
    private static final int PATIENT_LENGTH = 80;
    private static final int RECORD_LENGTH = 80;
    private static final int STARTDATE_LENGTH = 8;
    private static final int STARTTIME_LENGTH = 8;
    private static final int NUMBER_OF_BYTES_IN_HEADER_LENGTH = 8;
    private static final int FIRST_RESERVED_LENGTH = 44;
    private static final int NUMBER_Of_DATARECORDS_LENGTH = 8;
    private static final int DURATION_OF_DATARECORD_LENGTH = 8;
    private static final int NUMBER_OF_SIGNALS_LENGTH = 4;

    private static final int SIGNAL_LABEL_LENGTH = 16;
    private static final int SIGNAL_TRANSDUCER_TYPE_LENGTH = 80;
    private static final int SIGNAL_PHYSICAL_DIMENSION_LENGTH = 8;
    private static final int SIGNAL_PHYSICAL_MIN_LENGTH = 8;
    private static final int SIGNAL_PHYSICAL_MAX_LENGTH = 8;
    private static final int SIGNAL_DIGITAL_MIN_LENGTH = 8;
    private static final int SIGNAL_DIGITAL_MAX_LENGTH = 8;
    private static final int SIGNAL_PREFILTERING_LENGTH = 80;
    private static final int SIGNAL_NUMBER_OF_SAMPLES_LENGTH = 8;
    private static final int SIGNAL_RESERVED_LENGTH = 32;

    public static  byte[] createEdfHeader(HeaderConfig headerConfig) {
        return createHeader(headerConfig, false);

    }

    public static  byte[] createBdfHeader(HeaderConfig headerConfig) {
        return createHeader(headerConfig, true);
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

    private static  byte[] createHeader(HeaderConfig headerConfig, boolean isBdf) {

        String startDateOfRecording = new SimpleDateFormat("dd.MM.yy").format(new Date(headerConfig.getStartTime()));
        String startTimeOfRecording = new SimpleDateFormat("HH.mm.ss").format(new Date(headerConfig.getStartTime()));

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(adjustLength(getVersion(isBdf), VERSION_LENGTH - 1));  // -1 because first non ascii byte (or "0" for edf) we will add later
        headerBuilder.append(adjustLength(headerConfig.getPatientId(), PATIENT_LENGTH));
        headerBuilder.append(adjustLength(headerConfig.getRecordingId(), RECORD_LENGTH));
        headerBuilder.append(startDateOfRecording);
        headerBuilder.append(startTimeOfRecording);
        headerBuilder.append(adjustLength(Integer.toString(headerConfig.getNumberOfBytesInHeader()), NUMBER_OF_BYTES_IN_HEADER_LENGTH));
        headerBuilder.append(adjustLength(getFirstReserved(isBdf), FIRST_RESERVED_LENGTH));
        headerBuilder.append(adjustLength(Integer.toString(headerConfig.getNumberOfDataRecords()), NUMBER_Of_DATARECORDS_LENGTH));
        headerBuilder.append(adjustLength(double2String(headerConfig.getDurationOfDataRecord()), DURATION_OF_DATARECORD_LENGTH));
        headerBuilder.append(adjustLength(Integer.toString(headerConfig.getNumberOfSignals()), NUMBER_OF_SIGNALS_LENGTH));


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

        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            SignalConfig signalConfig = headerConfig.getSignalConfig(i);
            labels.append(adjustLength(signalConfig.getLabel(), SIGNAL_LABEL_LENGTH));
            transducerTypes.append(adjustLength(signalConfig.getTransducerType(), SIGNAL_TRANSDUCER_TYPE_LENGTH));
            physicalDimensions.append(adjustLength(signalConfig.getPhysicalDimension(), SIGNAL_PHYSICAL_DIMENSION_LENGTH));
            physicalMinimums.append(adjustLength(String.valueOf(signalConfig.getPhysicalMin()), SIGNAL_PHYSICAL_MIN_LENGTH));
            physicalMaximums.append(adjustLength(String.valueOf(signalConfig.getPhysicalMax()), SIGNAL_PHYSICAL_MAX_LENGTH));
            digitalMinimums.append(adjustLength(String.valueOf(signalConfig.getDigitalMin()), SIGNAL_DIGITAL_MIN_LENGTH));
            digitalMaximums.append(adjustLength(String.valueOf(signalConfig.getDigitalMax()), SIGNAL_DIGITAL_MAX_LENGTH));
            preFilterings.append(adjustLength(signalConfig.getPrefiltering(), SIGNAL_PREFILTERING_LENGTH));
            samplesNumbers.append(adjustLength(Integer.toString(signalConfig.getNumberOfSamplesInEachDataRecord()), SIGNAL_NUMBER_OF_SAMPLES_LENGTH));
            reservedForChannels.append(adjustLength("", SIGNAL_RESERVED_LENGTH));
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
        byteBuffer.put(getFirstByte(isBdf));
        byteBuffer.put(headerBuilder.toString().getBytes(ASCII));
        return byteBuffer.array();
    }


    public static boolean isBdf(File file) throws IOException, HeaderParsingException {
    /*    Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ASCII));
        int length = VERSION_LENGTH + PATIENT_LENGTH + RECORD_LENGTH + STARTDATE_LENGTH +
                STARTTIME_LENGTH +  NUMBER_OF_BYTES_IN_HEADER_LENGTH + FIRST_RESERVED_LENGTH;
        char[] buffer = new char[length];
        reader.read(buffer, 0, length);
        reader.close();

        // just if we will want to check if firstReserved equals 'BIOSEMI' or "24BIT"
        String firstReserved = new String(buffer, length - FIRST_RESERVED_LENGTH, FIRST_RESERVED_LENGTH);

        char firstChar = buffer[0];
        if(firstChar == '0') {
            return false;

        }else if((byte) firstChar == 255) {
            return true;
        }*/
        InputStream input = new FileInputStream(file);
        byte[] buffer = new byte[1];
        input.read(buffer);
        input.close();

        if(buffer[0] == (byte)255)  {
            return true;

        }else if((char)buffer[0] == '0') {
            return true;
        }
        throw new HeaderParsingException("Invalid Edf/Bdf file header. First byte should be equal '0' or 255");
    }

    public static HeaderConfig readHeader(File file) throws IOException, HeaderParsingException {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ASCII));
        HeaderConfig headerConfig = new HeaderConfig();

        char[] buffer;
        buffer = new char[VERSION_LENGTH];
        reader.read(buffer, 0, VERSION_LENGTH);

        buffer = new char[PATIENT_LENGTH];
        reader.read(buffer, 0, PATIENT_LENGTH);
        String patientIdentification = new String(buffer).trim();
        headerConfig.setPatientId(patientIdentification);

        buffer = new char[RECORD_LENGTH];
        reader.read(buffer, 0, RECORD_LENGTH);
        String recordIdentification = new String(buffer).trim();
        headerConfig.setRecordingId(recordIdentification);

        buffer = new char[STARTDATE_LENGTH];
        reader.read(buffer, 0, STARTDATE_LENGTH);
        String startDateStr = new String(buffer);

        buffer = new char[STARTTIME_LENGTH];
        reader.read(buffer, 0, STARTTIME_LENGTH);
        String startTimeStr = new String(buffer);
        String dateFormat = "dd.MM.yy HH.mm.ss";
        String startDateTimeStr = startDateStr + " " + startTimeStr;
        long startTime;
        try{
            Date date = new SimpleDateFormat(dateFormat).parse(startDateTimeStr);
            startTime = date.getTime();
        } catch (Exception e) {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. Error while parsing header Date-Time: " + startDateTimeStr);
        }
        headerConfig.setStartTime(startTime);

        buffer = new char[NUMBER_OF_BYTES_IN_HEADER_LENGTH];
        reader.read(buffer, 0, NUMBER_OF_BYTES_IN_HEADER_LENGTH);

        buffer = new char[FIRST_RESERVED_LENGTH];
        reader.read(buffer, 0, FIRST_RESERVED_LENGTH);

        buffer = new char[NUMBER_Of_DATARECORDS_LENGTH];
        reader.read(buffer, 0, NUMBER_Of_DATARECORDS_LENGTH);
        int numberOfDataRecords = stringToInt(new String(buffer));
        headerConfig.setNumberOfDataRecords(numberOfDataRecords);

        buffer = new char[DURATION_OF_DATARECORD_LENGTH];
        reader.read(buffer, 0, DURATION_OF_DATARECORD_LENGTH);
        Double durationOfDataRecord = stringToDouble(new String(buffer));
        headerConfig.setDurationOfDataRecord(durationOfDataRecord);

        buffer = new char[NUMBER_OF_SIGNALS_LENGTH];
        reader.read(buffer, 0, NUMBER_OF_SIGNALS_LENGTH);
        int numberOfSignals =  stringToInt(new String(buffer));

        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
           headerConfig.addSignalConfig(new SignalConfig());
        }


        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_LABEL_LENGTH];
            reader.read(buffer, 0, SIGNAL_LABEL_LENGTH);
            headerConfig.getSignalConfig(signalNumber).setLabel(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_TRANSDUCER_TYPE_LENGTH];
            reader.read(buffer, 0, SIGNAL_TRANSDUCER_TYPE_LENGTH);
            headerConfig.getSignalConfig(signalNumber).setTransducerType(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_DIMENSION_LENGTH];
            reader.read(buffer, 0, SIGNAL_PHYSICAL_DIMENSION_LENGTH);
            headerConfig.getSignalConfig(signalNumber).setPhysicalDimension(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MIN_LENGTH];
            reader.read(buffer, 0, SIGNAL_PHYSICAL_MIN_LENGTH);
            int physicalMin =  stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setPhysicalMin(physicalMin);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MAX_LENGTH];
            reader.read(buffer, 0, SIGNAL_PHYSICAL_MAX_LENGTH);
            int physicalMax =  stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setPhysicalMax(physicalMax);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MIN_LENGTH];
            reader.read(buffer, 0, SIGNAL_DIGITAL_MIN_LENGTH);
            int digitalMin =  stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setDigitalMin(digitalMin);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MAX_LENGTH];
            reader.read(buffer, 0, SIGNAL_DIGITAL_MAX_LENGTH);
            int digitalMax =  stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setDigitalMax(digitalMax);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PREFILTERING_LENGTH];
            reader.read(buffer, 0, SIGNAL_PREFILTERING_LENGTH);
            headerConfig.getSignalConfig(signalNumber).setPrefiltering(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_NUMBER_OF_SAMPLES_LENGTH];
            reader.read(buffer, 0, SIGNAL_NUMBER_OF_SAMPLES_LENGTH);
            int numberOfSamplesInDataRecord =  stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamplesInDataRecord);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_RESERVED_LENGTH];
            reader.read(buffer, 0, SIGNAL_RESERVED_LENGTH);
        }

        reader.close();
        return headerConfig;

    }


    private static Integer stringToInt(String str) throws HeaderParsingException {
        try {
            str = str.trim();
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. Error while parsing Int: " + str);
        }
    }

    private static Double stringToDouble(String str) throws  HeaderParsingException {
        try {
            str = str.trim();
            return Double.valueOf(str);
        } catch (NumberFormatException e) {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. Error while parsing Double: " + str);
        }
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
