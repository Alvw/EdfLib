package com.biorecorder.edflib;

import com.biorecorder.edflib.base.HeaderConfig;
import com.biorecorder.edflib.base.SignalConfig;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a helper class that know how to work with EDF/BDF file header.
 * It has one static method to convert {@link HeaderConfig} to EDF or BDF file header
 * (byte array). And another static method to read a EDF/BDF file header and store its information
 * at a HeaderConfig object.
 *
 * <p>HEADER RECORD
 * <br>8 ascii : version of this data format (0)
 * <br>80 ascii : local patient identification (mind item 3 of the additional EDF+ specs)
 * <br>80 ascii : local recording identification (mind item 4 of the additional EDF+ specs)
 * <br>8 ascii : startdate of recording (dd.mm.yy) (mind item 2 of the additional EDF+ specs)
 * <br>8 ascii : starttime of recording (hh.mm.ss)
 * <br>8 ascii : number of bytes in header record (The header record contains 256 + (ns * 256) bytes)
 * <br>44 ascii : reserved
 * <br>8 ascii : number of data records (-1 if unknown, obey item 10 of the additional EDF+ specs)
 * <br>8 ascii : duration of a data record, in seconds
 * <br>4 ascii : number of signals (ns) in data record
 * <br>ns * 16 ascii : ns * label (e.g. EEG Fpz-Cz or Body temp) (mind item 9 of the additional EDF+ specs)
 * <br>ns * 80 ascii : ns * transducer type (e.g. AgAgCl electrode)
 * <br>ns * 8 ascii : ns * physical dimension (e.g. uV or degreeC)
 * <br>ns * 8 ascii : ns * physical minimum (e.g. -500 or 34)
 * <br>ns * 8 ascii : ns * physical maximum (e.g. 500 or 40)
 * <br>ns * 8 ascii : ns * digital minimum (e.g. -2048)
 * <br>ns * 8 ascii : ns * digital maximum (e.g. 2047)
 * <br>ns * 80 ascii : ns * prefiltering (e.g. HP:0.1Hz LP:75Hz)
 * <br>ns * 8 ascii : ns * nr of samples in each data record
 * <br>ns * 32 ascii : ns * reserved
 *
 * <p>Detailed information about EDF/BDF format:
 * <a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
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
     * Create EDF or BDF file header on the base of the given HeaderConfig object
     * and the given file type.
     *
     * @param headerConfig - object containing the information required for EDF and BDF file header
     * @param fileType - EDF or BDF
     * @return BDF or EDF file header as array of bytes
     */

    public static byte[] createHeader(HeaderConfig headerConfig, FileType fileType) {

        String startDateOfRecording = new SimpleDateFormat("dd.MM.yy").format(new Date(headerConfig.getRecordingStartTime()));
        String startTimeOfRecording = new SimpleDateFormat("HH.mm.ss").format(new Date(headerConfig.getRecordingStartTime()));

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(adjustLength(fileType.getVersion(), VERSION_LENGTH - 1));  // -1 because first non ascii byte (or "0" for edf) we will add later
        headerBuilder.append(adjustLength(headerConfig.getPatientIdentification(), PATIENT_LENGTH));
        headerBuilder.append(adjustLength(headerConfig.getRecordingIdentification(), RECORD_LENGTH));
        headerBuilder.append(startDateOfRecording);
        headerBuilder.append(startTimeOfRecording);
        headerBuilder.append(adjustLength(Integer.toString(headerConfig.getNumberOfBytesInHeaderRecord()), NUMBER_OF_BYTES_IN_HEADER_LENGTH));
        headerBuilder.append(adjustLength(fileType.getFirstReserved(), FIRST_RESERVED_LENGTH));
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
            physicalMinimums.append(adjustLength(double2String(signalConfig.getPhysicalMin()), SIGNAL_PHYSICAL_MIN_LENGTH));
            physicalMaximums.append(adjustLength(double2String(signalConfig.getPhysicalMax()), SIGNAL_PHYSICAL_MAX_LENGTH));
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
        byteBuffer.put(fileType.getFirstByte());
        byteBuffer.put(headerBuilder.toString().getBytes(ASCII));
        return byteBuffer.array();
    }

    /**
     * Read the header of the given file and finds out if it is EDF or BDF file
     *
     * @param file file to read
     * @return true if the file is BDF file and false if the file is EDF file
     * @throws IOException            if file can not be read
     * @throws HeaderParsingException if the file header is not valid EDF/BDF file header
     */
    public static FileType getFileType(File file) throws IOException, HeaderParsingException {
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

        if (buffer[0] == (byte) 255) {
            return FileType.BDF_24BIT;

        } else if ((char) buffer[0] == '0') {
            return FileType.EDF_16BIT;
        }
        throw new HeaderParsingException("Invalid Edf/Bdf file header. First byte should be equal '0' or 255");
    }

    /**
     * Read the header of the given EDF or BDF file
     *
     * @param file file to read
     * @return HeaderConfig object containing information from the file header
     * @throws IOException            if the file can not be read
     * @throws HeaderParsingException if the file header is not valid EDF/BDF file header.
     */
    public static HeaderConfig readHeader(File file) throws IOException, HeaderParsingException {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ASCII));
        HeaderConfig headerConfig = new HeaderConfig();

        char[] buffer;
        buffer = new char[VERSION_LENGTH];
        reader.read(buffer);

        buffer = new char[PATIENT_LENGTH];
        reader.read(buffer);
        String patientIdentification = new String(buffer).trim();
        headerConfig.setPatientIdentification(patientIdentification);

        buffer = new char[RECORD_LENGTH];
        reader.read(buffer);
        String recordIdentification = new String(buffer).trim();
        headerConfig.setRecordingIdentification(recordIdentification);

        buffer = new char[STARTDATE_LENGTH];
        reader.read(buffer);
        String startDateStr = new String(buffer);

        buffer = new char[STARTTIME_LENGTH];
        reader.read(buffer);
        String startTimeStr = new String(buffer);
        String dateFormat = "dd.MM.yy HH.mm.ss";
        String startDateTimeStr = startDateStr + " " + startTimeStr;
        long startTime;
        try {
            Date date = new SimpleDateFormat(dateFormat).parse(startDateTimeStr);
            startTime = date.getTime();
        } catch (Exception e) {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. Error while parsing header Date-Time: " + startDateTimeStr);
        }
        headerConfig.setRecordingStartTime(startTime);

        buffer = new char[NUMBER_OF_BYTES_IN_HEADER_LENGTH];
        reader.read(buffer);


        buffer = new char[FIRST_RESERVED_LENGTH];
        reader.read(buffer);

        buffer = new char[NUMBER_Of_DATARECORDS_LENGTH];
        reader.read(buffer);
        int numberOfDataRecords = stringToInt(new String(buffer));
        headerConfig.setNumberOfDataRecords(numberOfDataRecords);

        buffer = new char[DURATION_OF_DATARECORD_LENGTH];
        reader.read(buffer);
        Double durationOfDataRecord = stringToDouble(new String(buffer));
        headerConfig.setDurationOfDataRecord(durationOfDataRecord);

        buffer = new char[NUMBER_OF_SIGNALS_LENGTH];
        reader.read(buffer);
        int numberOfSignals = stringToInt(new String(buffer));

        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            headerConfig.addSignalConfig(new SignalConfig());
        }


        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_LABEL_LENGTH];
            reader.read(buffer);
            headerConfig.getSignalConfig(signalNumber).setLabel(new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_TRANSDUCER_TYPE_LENGTH];
            reader.read(buffer);
            headerConfig.getSignalConfig(signalNumber).setTransducerType(new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_DIMENSION_LENGTH];
            reader.read(buffer);
            headerConfig.getSignalConfig(signalNumber).setPhysicalDimension(new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MIN_LENGTH];
            reader.read(buffer);
            double physicalMin = stringToDouble(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setPhysicalMin(physicalMin);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MAX_LENGTH];
            reader.read(buffer);
            double physicalMax = stringToDouble(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setPhysicalMax(physicalMax);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MIN_LENGTH];
            reader.read(buffer);
            int digitalMin = stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setDigitalMin(digitalMin);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MAX_LENGTH];
            reader.read(buffer);
            int digitalMax = stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setDigitalMax(digitalMax);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PREFILTERING_LENGTH];
            reader.read(buffer);
            headerConfig.getSignalConfig(signalNumber).setPrefiltering(new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_NUMBER_OF_SAMPLES_LENGTH];
            reader.read(buffer);
            int numberOfSamplesInDataRecord = stringToInt(new String(buffer));
            headerConfig.getSignalConfig(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamplesInDataRecord);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_RESERVED_LENGTH];
            reader.read(buffer);
        }

        reader.close();
        return headerConfig;

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
    private static Double stringToDouble(String str) throws HeaderParsingException {
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
     * @param text   - string which length should be adjusted
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
    private static String double2String(double value) {
        return String.format("%.6f", value).replace(",", ".");
    }


}
