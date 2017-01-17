package com.biorecorder.edflib.util;

import com.biorecorder.edflib.RecordingConfig;
import com.biorecorder.edflib.SignalConfig;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a helper class that know how to work with EDF/BDF file header.
 * It has one static method to convert {@link RecordingConfig} to EDF or BDF file header
 * (byte array). And another static method to read a EDF/BDF file header and store its information
 * at RecordingConfig object.
 *
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
 * <p>Detailed information about EDF/BDF format:
 * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
 * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <br><a href="http://www.biosemi.com/faq/file_format.htm">EDF/BDF difference</a>
 *
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

    /**
     * Create EDF file header on the base of given RecordingConfig object
     *
     * @param recordingConfig - object containing the information required for EDF header
     * @return EDF file header as array of bytes
     */
    public static  byte[] createEdfHeader(RecordingConfig recordingConfig) {
        return createHeader(recordingConfig, false);

    }

    /**
     * Create BDF file header on the base of given RecordingConfig object
     *
     * @param recordingConfig - object containing the information required for BDF header
     * @return BDF file header as array of bytes
     */
    public static  byte[] createBdfHeader(RecordingConfig recordingConfig) {
        return createHeader(recordingConfig, true);
    }

    /**
     * Create the version (minus first byte) of the data format
     * for BDF or EDF file header respectively
     *
     * @param isBdf - true if the the version is for BDF file header
     *              and false if the the version is for EDF file Header
     * @return the version for the the BDF or EDF file header
     */
    private static String getVersion(boolean isBdf){
        if(isBdf){
            return "BIOSEMI"; //bdf
        }else {
            return ""; //edf
        }
    }

    /**
     * Create first byte for the BDF or EDF file header respectively
     *
     * @param isBdf - true if the firs byte is for BDF file header
     *              and false if the first byte is for EDF file Header
     * @return first byte for the the BDF or EDF file header
     */
    private static byte getFirstByte(boolean isBdf){
        if(isBdf){
            return (byte) 255; //bdf
        }else {
            String zeroString = "0";  //edf  "0" (ASCII)
            return zeroString.getBytes(ASCII)[0]; // or  return (int) '0';
        }
    }

    /**
     * Create the first reserved field for the BDF or EDF file header respectively
     *
     * @param isBdf - true if the first reserved field is for BDF file header
     *              and false if the first reserved field is for EDF file Header
     * @return the first reserved field for the the BDF or EDF file header
     */
    private static String getFirstReserved(boolean isBdf){
        if(isBdf){
            return "24BIT"; //bdf
        }else {
            return ""; //edf
        }
    }

    /**
     * This method actually performs the main part of the work common for creating
     * both EDF and BDF file headers.
     *
     * @param recordingConfig - object containing the information required for EDF and BDF file header
     *
     * @param isBdf - true if we need BDF file header
     *              false if we need EDF file header
     * @return BDF or EDF file header as array of bytes
     */

    private static  byte[] createHeader(RecordingConfig recordingConfig, boolean isBdf) {

        String startDateOfRecording = new SimpleDateFormat("dd.MM.yy").format(new Date(recordingConfig.getStartTime()));
        String startTimeOfRecording = new SimpleDateFormat("HH.mm.ss").format(new Date(recordingConfig.getStartTime()));

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(adjustLength(getVersion(isBdf), VERSION_LENGTH - 1));  // -1 because first non ascii byte (or "0" for edf) we will add later
        headerBuilder.append(adjustLength(recordingConfig.getPatientId(), PATIENT_LENGTH));
        headerBuilder.append(adjustLength(recordingConfig.getRecordingId(), RECORD_LENGTH));
        headerBuilder.append(startDateOfRecording);
        headerBuilder.append(startTimeOfRecording);
        headerBuilder.append(adjustLength(Integer.toString(recordingConfig.getNumberOfBytesInHeader()), NUMBER_OF_BYTES_IN_HEADER_LENGTH));
        headerBuilder.append(adjustLength(getFirstReserved(isBdf), FIRST_RESERVED_LENGTH));
        headerBuilder.append(adjustLength(Integer.toString(recordingConfig.getNumberOfDataRecords()), NUMBER_Of_DATARECORDS_LENGTH));
        headerBuilder.append(adjustLength(double2String(recordingConfig.getDurationOfDataRecord()), DURATION_OF_DATARECORD_LENGTH));
        headerBuilder.append(adjustLength(Integer.toString(recordingConfig.getNumberOfSignals()), NUMBER_OF_SIGNALS_LENGTH));


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

        for (int i = 0; i < recordingConfig.getNumberOfSignals(); i++) {
            SignalConfig signalConfig = recordingConfig.getSignalConfig(i);
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

    /**
     * Read the header of the given file and finds out if it is EDF or BDF file
     *
     * @param file file to read
     * @return true if the file is BDF file and false if the file is EDF file
     * @throws IOException if file can not be read
     * @throws HeaderParsingException if the file header is not valid EDF/BDF file header
     */
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

    /**
     * Read the header of the given EDF or BDF file
     *
     * @param file file to read
     * @return RecordingConfig object containing information from the file header
     * @throws IOException if the file can not be read
     * @throws HeaderParsingException if the file header is not valid EDF/BDF file header.
     */
    public static RecordingConfig readHeader(File file) throws IOException, HeaderParsingException {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ASCII));
        RecordingConfig recordingConfig = new RecordingConfig();

        char[] buffer;
        buffer = new char[VERSION_LENGTH];
        reader.read(buffer);

        buffer = new char[PATIENT_LENGTH];
        reader.read(buffer);
        String patientIdentification = new String(buffer).trim();
        recordingConfig.setPatientId(patientIdentification);

        buffer = new char[RECORD_LENGTH];
        reader.read(buffer);
        String recordIdentification = new String(buffer).trim();
        recordingConfig.setRecordingId(recordIdentification);

        buffer = new char[STARTDATE_LENGTH];
        reader.read(buffer);
        String startDateStr = new String(buffer);

        buffer = new char[STARTTIME_LENGTH];
        reader.read(buffer);
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
        recordingConfig.setStartTime(startTime);

        buffer = new char[NUMBER_OF_BYTES_IN_HEADER_LENGTH];
        reader.read(buffer);


        buffer = new char[FIRST_RESERVED_LENGTH];
        reader.read(buffer);

        buffer = new char[NUMBER_Of_DATARECORDS_LENGTH];
        reader.read(buffer);
        int numberOfDataRecords = stringToInt(new String(buffer));
        recordingConfig.setNumberOfDataRecords(numberOfDataRecords);

        buffer = new char[DURATION_OF_DATARECORD_LENGTH];
        reader.read(buffer);
        Double durationOfDataRecord = stringToDouble(new String(buffer));
        recordingConfig.setDurationOfDataRecord(durationOfDataRecord);

        buffer = new char[NUMBER_OF_SIGNALS_LENGTH];
        reader.read(buffer);
        int numberOfSignals =  stringToInt(new String(buffer));

        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
           recordingConfig.addSignalConfig(new SignalConfig());
        }


        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_LABEL_LENGTH];
            reader.read(buffer);
            recordingConfig.getSignalConfig(signalNumber).setLabel(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_TRANSDUCER_TYPE_LENGTH];
            reader.read(buffer);
            recordingConfig.getSignalConfig(signalNumber).setTransducerType(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_DIMENSION_LENGTH];
            reader.read(buffer);
            recordingConfig.getSignalConfig(signalNumber).setPhysicalDimension(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MIN_LENGTH];
            reader.read(buffer);
            int physicalMin =  stringToInt(new String(buffer));
            recordingConfig.getSignalConfig(signalNumber).setPhysicalMin(physicalMin);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MAX_LENGTH];
            reader.read(buffer);
            int physicalMax =  stringToInt(new String(buffer));
            recordingConfig.getSignalConfig(signalNumber).setPhysicalMax(physicalMax);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MIN_LENGTH];
            reader.read(buffer);
            int digitalMin =  stringToInt(new String(buffer));
            recordingConfig.getSignalConfig(signalNumber).setDigitalMin(digitalMin);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MAX_LENGTH];
            reader.read(buffer);
            int digitalMax =  stringToInt(new String(buffer));
            recordingConfig.getSignalConfig(signalNumber).setDigitalMax(digitalMax);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PREFILTERING_LENGTH];
            reader.read(buffer);
            recordingConfig.getSignalConfig(signalNumber).setPrefiltering(new String(buffer).trim());
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_NUMBER_OF_SAMPLES_LENGTH];
            reader.read(buffer);
            int numberOfSamplesInDataRecord =  stringToInt(new String(buffer));
            recordingConfig.getSignalConfig(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamplesInDataRecord);
        }
        for(int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_RESERVED_LENGTH];
            reader.read(buffer);
        }

        reader.close();
        return recordingConfig;

    }

    /**
     * Convert String to in
     *
     * @param str string to convert
     * @return resultant int
     * @throws HeaderParsingException if the string could not be converted to int
     */
    private static Integer stringToInt(String str) throws HeaderParsingException {
        try {
            str = str.trim();
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. Error while parsing Int: " + str);
        }
    }

    /**
     * Convert String to double
     *
     * @param str string to convert
     * @return resultant double
     * @throws HeaderParsingException if the string could not be converted to double
     */
    private static Double stringToDouble(String str) throws  HeaderParsingException {
        try {
            str = str.trim();
            return Double.valueOf(str);
        } catch (NumberFormatException e) {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. Error while parsing Double: " + str);
        }
    }



    /**
     * if the String.length() is more then the given length cut the String to the given length
     * if the String.length() is less then the given length append spaces to the end of the String
     *
     * @param text - string which length should be adjusted
     * @param length - desired length
     * @return resultant string with the given length
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

    /**
     * Convert double to the string with format valid for EDF and BDF header - "%.6f".
     *
     * @param value double that should be converted to the string
     * @return resultant string with format valid for EDF and BDF header
     */
    private static String double2String(double value ) {
        return String.format("%.6f", value).replace(",", ".");
    }


}
