package com.biorecorder.edflib;

import com.biorecorder.edflib.exceptions.EdfHeaderRuntimeException;
import com.biorecorder.edflib.exceptions.ExceptionType;
import com.biorecorder.edflib.exceptions.EdfRuntimeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This class permits to store the information required for the header record
 * of EDF/BDF file and then correctly extract data from the file. It has the following fields
 * (and their corresponding getters and setters):
 * <ul>
 * <li>patient identification</li>
 * <li>recording identification</li>
 * <li>recording recordingStartTime</li>
 * <li>number of data records</li>
 * <li>duration of a data record (in seconds)</li>
 * </ul>
 * <p>
 * Also it permits to configure the measuring channels (signals) and has
 * getters and setters for the fields describing every channel:
 * <ul>
 * <li>signal label</li>
 * <li>transducer type (e.g. AgAgCI electrode)</li>
 * <li>physical dimension(e.g. uV or degree C)</li>
 * <li>physical minimum (e.g. -500 or 34)</li>
 * <li>physical maximum (e.g. 500 or 40)</li>
 * <li>digital minimum (e.g. -2048)</li>
 * <li>digital maximum (e.g. 2047)</li>
 * <li>prefiltering (e.g. HP:0.1Hz LP:75Hz)</li>
 * <li>number of samples in each data record</li>
 * </ul>
 * <p>
 * EDF/BDF format assumes a linear relationship between physical and digital values,
 * and the scaling factors (gain and offset) for every channel are calculated
 * on the base of its <b> physical minimum and maximum </b> and the corresponding
 * <b> digital minimum and maximum </b>. So for every channel:
 * <p>
 * (physValue - physMin) / (digValue - digMin)  = constant [Gain] = (physMax - physMin) / (digMax - digMin)
 * <p>
 * physValue = digValue * gain + offset
 * <br>digValue = (physValue - offset) / gain
 * <p>
 * where:
 * <br>gain = (physMax - physMin) / (digMax - digMin)
 * <br>offset = (physMin - digMin * gain);
 * <p>
 * In general "gain" refers to multiplication of a signal
 * and "offset"  refer to addition to a signal, i.e. out = in * gain + offset
 * <p>
 * To get the number of the signals in data record use method {@link #getNumberOfSignals()}
 * <p>
 * This class also contains helper methods to convert physical (floating point) values
 * to digital (integer) ones and vice versa.
 * <p>
 * Detailed information about EDF/BDF format:
 * <a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <a href="https://www.biosemi.com/faq/file_format.htm">BioSemi or BDF file format</a>
 * <p>
 * BDF HEADER RECORD
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
 * <p>
 */

public class HeaderInfo {
    private static final String ERR_MSG_START = "Header error! ";
    private String patientIdentification = "Default patient";
    private String recordingIdentification = "Default record";
    private long recordingStartTime = -1;
    private int numberOfDataRecords = -1;
    private double durationOfDataRecord = 1; // sec
    private ArrayList<SignalInfo> signals = new ArrayList<SignalInfo>();
    private FileType fileType = FileType.EDF_16BIT;

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
     * Default constructor that creates a HeaderInfo instance
     * with 0 channels (signals). So all channels should be added as necessary.
     * <p>
     * See method: {@link #addSignal()}
     */
    public HeaderInfo(FileType fileType) {
        this.fileType = fileType;
    }


    /**
     * This constructor creates a HeaderInfo instance of the given type (EDF_16BIT or BDF_24BIT)
     * with the given number of channels (signals)
     *
     * @param numberOfSignals number of signals in data records
     * @param fileType        EDF_16BIT or BDF_24BIT
     * @throws IllegalArgumentException if numberOfSignals <= 0
     */
    public HeaderInfo(int numberOfSignals, FileType fileType) throws IllegalArgumentException {
        if (numberOfSignals <= 0) {
            String errMsg =  MessageFormat.format("Number of signals is invalid: {0}. Expected {1}",  numberOfSignals, ">0");
            throw new IllegalArgumentException(errMsg);
        }
        this.fileType = fileType;
        for (int i = 0; i < numberOfSignals; i++) {
            addSignal();
        }
    }

    /**
     * Constructor to make a copy of the given HeaderInfo instance
     *
     * @param headerInfo HeaderInfo instance that will be copied
     */
    public HeaderInfo(HeaderInfo headerInfo) {
        this(headerInfo.getNumberOfSignals(), headerInfo.getFileType());
        patientIdentification = headerInfo.getPatientIdentification();
        recordingIdentification = headerInfo.getRecordingIdentification();
        recordingStartTime = headerInfo.getRecordingStartDateTimeMs();
        durationOfDataRecord = headerInfo.getDurationOfDataRecord();
        numberOfDataRecords = headerInfo.getNumberOfDataRecords();
        for (int i = 0; i < headerInfo.getNumberOfSignals(); i++) {
            setNumberOfSamplesInEachDataRecord(i, headerInfo.getNumberOfSamplesInEachDataRecord(i));
            setPrefiltering(i, headerInfo.getPrefiltering(i));
            setTransducer(i, headerInfo.getTransducer(i));
            setLabel(i, headerInfo.getLabel(i));
            setDigitalRange(i, headerInfo.getDigitalMin(i), headerInfo.getDigitalMax(i));
            setPhysicalRange(i, headerInfo.getPhysicalMin(i), headerInfo.getPhysicalMax(i));
            setPhysicalDimension(i, headerInfo.getPhysicalDimension(i));
        }
    }

    private void readBuffer(Reader reader, char[] buffer) throws IOException {
        String errMsg = MessageFormat.format("{0}Header does not contain the required information.", ERR_MSG_START);
        EdfHeaderRuntimeException headerNotFullException = new EdfHeaderRuntimeException(ExceptionType.HEADER_NOT_FULL, errMsg);
        int numberCharactersRead = reader.read(buffer);
        if(numberCharactersRead < buffer.length) {
            throw headerNotFullException;
        }
    }


    /**
     * Create HeaderInfo object on the base of header record of the given EDF or BDF file
     *
     * @param file Edf/Bdf file to read
     * @throws EdfRuntimeException  if the file can not be read
     * @throws EdfHeaderRuntimeException  if the header record has some errors.
     */
    HeaderInfo(File file) throws EdfRuntimeException, EdfHeaderRuntimeException {
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ASCII));
            char[] buffer;
            buffer = new char[VERSION_LENGTH];
            readBuffer(reader, buffer);
            char firstChar = buffer[0];
            if ((Character.getNumericValue(firstChar) & 0xFF) == 255) { // BDF
                String version = new String(buffer, 1, VERSION_LENGTH - 1);
                String expectedVersion = "BIOSEMI";
                if(version.equals(expectedVersion)) {
                    fileType = FileType.BDF_24BIT;
                } else {
                    String errMsg = MessageFormat.format("{0}Header has unknown version: \"{1}\". Expected: \"{2}\"", ERR_MSG_START, version, expectedVersion);
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.VERSION_FORMAT_INVALID, errMsg);
                    ex.setValue(version);
                    ex.setExpectedValue(expectedVersion);
                    throw ex;
                }
            } else { // EDF
                String version = new String(buffer);
                String expectedVersion = adjustLength("0", VERSION_LENGTH);
                if(version.equals(expectedVersion)) {
                    fileType = FileType.EDF_16BIT;
                } else {
                    String errMsg = MessageFormat.format("{0}Header has unknown version: \"{1}\". Expected: \"{2}\"", ERR_MSG_START, version, expectedVersion);
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.VERSION_FORMAT_INVALID, errMsg);
                    ex.setValue(version);
                    ex.setExpectedValue(expectedVersion);
                    throw ex;
                }
            }
/*******************************************************************************/
            buffer = new char[PATIENT_LENGTH];
            readBuffer(reader, buffer);
            patientIdentification = new String(buffer).trim();

            buffer = new char[RECORD_LENGTH];
            readBuffer(reader, buffer);
            recordingIdentification = new String(buffer).trim();

/******************** START DATE AND TIME *********************************************/
            buffer = new char[STARTDATE_LENGTH];
            readBuffer(reader, buffer);
            String dateString = new String(buffer);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(dateString);
            } catch (ParseException e) {
                String expectedDateString = "dd.mm.yy";
                String errMsg = MessageFormat.format("{0}Invalid date: \"{1}\". Expected: \"{2}\"", ERR_MSG_START, dateString, expectedDateString);
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.DATE_FORMAT_INVALID, errMsg, e);
                ex.setValue(dateString);
                ex.setExpectedValue(expectedDateString);
                throw ex;
            }

            buffer = new char[STARTTIME_LENGTH];
            readBuffer(reader, buffer);
            String timeString = new String(buffer);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm.ss");
            timeFormat.setLenient(false);
            try {
                timeFormat.parse(timeString);
            } catch (ParseException e) {
                String expectedTimeString = "hh.mm.ss";
                String errMsg = MessageFormat.format("{0}Invalid time: \"{1}\". Expected: \"{2}\"", ERR_MSG_START, timeString, expectedTimeString);
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.TIME_FORMAT_INVALID, errMsg, e);
                ex.setValue(timeString);
                ex.setExpectedValue(expectedTimeString);
                throw ex;
            }

            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yy HH.mm.ss");
            String dateTimeString = dateString + " " + timeString;
            try {
                recordingStartTime = dateTimeFormat.parse(dateTimeString).getTime();
            } catch (ParseException e) {
                // This situation never should take place. If happens it is an error
                // and we should detect it apart
                new RuntimeException("DateTime parsing failed: "+dateTimeString+ ". Expected: "+"dd.MM.yy HH.mm.ss");
            }
/*******************************************************************************/
            buffer = new char[NUMBER_OF_BYTES_IN_HEADER_LENGTH];
            readBuffer(reader, buffer);

            buffer = new char[FIRST_RESERVED_LENGTH];
            readBuffer(reader, buffer);

/********************* DATARECORD DURATION *************************************/
            buffer = new char[NUMBER_Of_DATARECORDS_LENGTH];
            readBuffer(reader, buffer);
            numberOfDataRecords = stringToInt(new String(buffer));

            buffer = new char[DURATION_OF_DATARECORD_LENGTH];
            readBuffer(reader, buffer);
            String recordDurationString = new String(buffer);
            try {
                durationOfDataRecord = (stringToDouble(recordDurationString));
            } catch (NumberFormatException e) {
                String errMsg = MessageFormat.format("{0}Record duration field is invalid: \"{1}\". Expected: {2}", ERR_MSG_START, recordDurationString, "double");
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.RECORD_DURATION_NAN, errMsg, e);
                ex.setValue(recordDurationString);
                ex.setExpectedValue("double");
                throw ex;
            }
            if (durationOfDataRecord <= 0) {
                String errMsg = MessageFormat.format("{0}Record duration is invalid: {1}. Expected {2}", ERR_MSG_START, Double.toString(durationOfDataRecord), ">0");
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.RECORD_DURATION_NONPOSITIVE, errMsg);
                ex.setValue(String.valueOf(durationOfDataRecord));
                ex.setExpectedValue(">0");
                throw ex;
            }

/***************** NUMBER OF SIGNALS IN HEADER *******************************/
            buffer = new char[NUMBER_OF_SIGNALS_LENGTH];
            readBuffer(reader, buffer);
            String numberOfSignalsString = new String(buffer);
            int numberOfSignals = 0;
            try {
                numberOfSignals = stringToInt(numberOfSignalsString);
            } catch (NumberFormatException e) {
                String errMsg = MessageFormat.format("{0}Number of signals field is invalid: \"{1}\". Expected: {2}", ERR_MSG_START, numberOfSignalsString, "integer");
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.NUMBER_OF_SIGNALS_NAN, errMsg, e);
                ex.setValue(numberOfSignalsString);
                ex.setExpectedValue("integer");
                throw ex;
            }

            if (numberOfSignals <= 0) {
                String errMsg = MessageFormat.format("{0}Number of signals is invalid: {1}. Expected {2}", ERR_MSG_START, Integer.toString(numberOfSignals), ">0");
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.NUMBER_OF_SIGNALS_NONPOSITIVE, errMsg);
                ex.setValue(Integer.toString(numberOfSignals));
                ex.setExpectedValue(">0");
                throw ex;
            }

/********************* SIGNALS IN THE HEADER *********************/
            int[] digMaxList = new int[numberOfSignals];
            int[] digMinList = new int[numberOfSignals];
            double[] physMaxList = new double[numberOfSignals];
            double[] physMinList = new double[numberOfSignals];
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                signals.add(new SignalInfo());
            }
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_LABEL_LENGTH];
                readBuffer(reader, buffer);
                setLabel(signalNumber, new String(buffer).trim());
            }
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_TRANSDUCER_TYPE_LENGTH];
                readBuffer(reader, buffer);
                setTransducer(signalNumber, new String(buffer).trim());
            }
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_PHYSICAL_DIMENSION_LENGTH];
                readBuffer(reader, buffer);
                setPhysicalDimension(signalNumber, new String(buffer).trim());
            }
/**************************** PHYSICAL MINIMUMS *************************************/
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_PHYSICAL_MIN_LENGTH];
                readBuffer(reader, buffer);
                String physMinString = new String(buffer);
                try {
                    physMinList[signalNumber] = stringToDouble(physMinString);
                } catch (NumberFormatException e) {
                    String errMsg = MessageFormat.format("{0}Physical minimum field of signal {1} is invalid: \"{2}\". Expected: {3}", ERR_MSG_START, signalNumber, physMinString, "double");
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_PHYSICAL_MIN_NAN, errMsg, e);
                    ex.setValue(physMinString);
                    ex.setExpectedValue("double");
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }
            }
/**************************** PHYSICAL MAXIMUMS *************************************/

            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_PHYSICAL_MAX_LENGTH];
                readBuffer(reader, buffer);
                String physMaxString = new String(buffer);
                try {
                    physMaxList[signalNumber] = stringToDouble(physMaxString);
                } catch (NumberFormatException e) {
                    String errMsg = MessageFormat.format("{0}Physical maximum field of signal {1} is invalid: \"{2}\". Expected: {3}", ERR_MSG_START, signalNumber, physMaxString, "double");
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_PHYSICAL_MAX_NAN, errMsg, e);
                    ex.setValue(physMaxString);
                    ex.setExpectedValue("double");
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }
            }
/**************************** DIGITAL MINIMUM *************************************/
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_DIGITAL_MIN_LENGTH];
                readBuffer(reader, buffer);
                String digMinString = new String(buffer);
                try {
                    digMinList[signalNumber] = stringToInt(digMinString);
                } catch (NumberFormatException e) {
                    String errMsg = MessageFormat.format("{0}Digital minimum field of signal {1} is invalid: \"{2}\". Expected: {3}", ERR_MSG_START, signalNumber, digMinString, "integer");
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_DIGITAL_MIN_NAN, errMsg, e);
                    ex.setValue(digMinString);
                    ex.setExpectedValue("integer");
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }

            }

/**************************** DIGITAL MAXIMUM *************************************/
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_DIGITAL_MAX_LENGTH];
                readBuffer(reader, buffer);
                String digMaxString = new String(buffer);
                try {
                    digMaxList[signalNumber] = stringToInt(digMaxString);
                } catch (NumberFormatException e) {
                    String errMsg = MessageFormat.format("{0}Digital maximum field of signal {1} is invalid: \"{2}\". Expected: {3}", ERR_MSG_START, signalNumber, digMaxString, "integer");
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_DIGITAL_MAX_NAN, errMsg, e);
                    ex.setValue(digMaxString);
                    ex.setExpectedValue("integer");
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }
            }

/**************************** PHYSICAL AND DIGITAL RANGES ***********************/

            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                if(physMaxList[signalNumber] <= physMinList[signalNumber]) {
                    String errMsg = MessageFormat.format("{0}Physical min/max range of signal {1} is invalid. Min = {2}, Max = {3}. Expected: {4}", ERR_MSG_START, signalNumber, Double.toString(physMinList[signalNumber]), Double.toString(physMaxList[signalNumber]), "min < max");
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_PHYSICAL_RANGE_INVALID, errMsg);
                    ex.setRange(physMinList[signalNumber], physMaxList[signalNumber]);
                    ex.setExpectedValue("min < max");
                    ex.setSignalNumber(signalNumber);
                    throw ex;

                }
                signals.get(signalNumber).setPhysicalRange(physMinList[signalNumber], physMaxList[signalNumber]);

                if(digMinList[signalNumber] < fileType.getDigitalMin() || digMinList[signalNumber] >= fileType.getDigitalMax()) {
                    String expected = fileType.getDigitalMin()+" <= digital min < "+fileType.getDigitalMax();
                    String errMsg = MessageFormat.format("{0}Digital min of signal {1} is invalid: {2}. Expected: {3}", ERR_MSG_START, signalNumber, Integer.toString(digMinList[signalNumber]), expected);
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_DIGITAL_MIN_OUT_OF_PERMITED_RANGE, errMsg);
                    ex.setRange(fileType.getDigitalMin(), fileType.getDigitalMax());
                    ex.setValue(String.valueOf(digMinList[signalNumber]));
                    ex.setExpectedValue(expected);
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }

                if(digMaxList[signalNumber] <= fileType.getDigitalMin() || digMaxList[signalNumber] > fileType.getDigitalMax()) {
                    String expected = fileType.getDigitalMin()+" < digital max <= "+fileType.getDigitalMax();
                    String errMsg = MessageFormat.format("{0}Digital max of signal {1} is invalid: {2}. Expected: {3}", ERR_MSG_START, signalNumber, Integer.toString(digMaxList[signalNumber]), expected);
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_DIGITAL_MAX_OUT_OF_PERMITED_RANGE, errMsg);
                    ex.setRange(fileType.getDigitalMin(), fileType.getDigitalMax());
                    ex.setValue(String.valueOf(digMaxList[signalNumber]));
                    ex.setExpectedValue(expected);
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }


                if(digMaxList[signalNumber] <= digMinList[signalNumber]) {
                    String errMsg = MessageFormat.format("{0}Digital min/max range of signal {1} is invalid. Min = {2}, Max = {3}. Expected: {4}", ERR_MSG_START, signalNumber, Integer.toString(digMinList[signalNumber]), Integer.toString(digMaxList[signalNumber]), "min < max");
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_DIGITAL_RANGE_INVALID, errMsg);
                    ex.setRange(digMinList[signalNumber], digMaxList[signalNumber]);
                    ex.setExpectedValue("min < max");
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }
                signals.get(signalNumber).setDigitalRange(digMinList[signalNumber],digMaxList[signalNumber]);
            }
/*******************************************************************************/

            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_PREFILTERING_LENGTH];
                readBuffer(reader, buffer);
                setPrefiltering(signalNumber, new String(buffer).trim());
            }

/*********************** NR OF SAMPLES IN EACH DATARECORD ********************/
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_NUMBER_OF_SAMPLES_LENGTH];
                readBuffer(reader, buffer);
                String numberOfSamplesString = new String(buffer);
                try {
                    int numberOfSamples = stringToInt(numberOfSamplesString);
                    if (numberOfSamples <= 0) {
                        String errMsg = MessageFormat.format("{0}Number of samples in datarecord of signal {1} is invalid: {2}. Expected {3}", ERR_MSG_START, signalNumber, Integer.toString(numberOfSamples), ">0");
                        EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_NUMBER_OF_SAMPLES_IN_RECORD_NONPOSITIVE, errMsg);
                        ex.setValue(Integer.toString(numberOfSamples));
                        ex.setExpectedValue(">0");
                        ex.setSignalNumber(signalNumber);
                        throw ex;
                    }
                    signals.get(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamples);

                } catch (NumberFormatException e) {
                    String errMsg = MessageFormat.format("{0}Number of samples in datarecord field of signal {1} is invalid: \"{2}\". Expected: {3}", ERR_MSG_START, signalNumber, numberOfSamplesString, "integer");
                    EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_NUMBER_OF_SAMPLES_IN_RECORD_NAN, errMsg, e);
                    ex.setValue(numberOfSamplesString);
                    ex.setExpectedValue("integer");
                    ex.setSignalNumber(signalNumber);
                    throw ex;
                }


            }
/*******************************************************************************/
            for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
                buffer = new char[SIGNAL_RESERVED_LENGTH];
                readBuffer(reader, buffer);
            }
            reader.close();
        } catch (IOException e) {
           throw new EdfRuntimeException(e);
        }
    }

    /**
     * Creates byte array with the header info ready to write in Edf/Bdf file
     *
     * @return byte array with the header info
     * @throws EdfHeaderRuntimeException if this HeaderInfo object is not correctly formed:
     * Number of signals = 0, or number of samples in data record
     * for some signal = 0.
     */
    public byte[] createFileHeader() throws EdfHeaderRuntimeException{
        // check if this HeaderInfo object was completely formed
        if (signals.size() == 0) {
            String errMsg = MessageFormat.format("{0}Number of Signals = 0. Expected: > 0", ERR_MSG_START);
            EdfHeaderRuntimeException ex =  new EdfHeaderRuntimeException(ExceptionType.NUMBER_OF_SIGNALS_NONPOSITIVE, errMsg);
            ex.setValue("0");
            ex.setExpectedValue(">0");
            throw ex;
        }

        for (int signalNumber = 0; signalNumber < signals.size(); signalNumber++) {
            int numberOfSamples = signals.get(signalNumber).getNumberOfSamplesInEachDataRecord();
            if (numberOfSamples <= 0) {
                String errMsg = MessageFormat.format("{0}Number of samples in datarecord of signal {1} is invalid: {2}. Expected >0", ERR_MSG_START, signalNumber, Integer.toString(numberOfSamples));
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.SIGNAL_NUMBER_OF_SAMPLES_IN_RECORD_NONPOSITIVE, errMsg);
                ex.setValue(String.valueOf(numberOfSamples));
                ex.setExpectedValue(">0");
                ex.setSignalNumber(signalNumber);
                throw ex;
            }
        }

        // convert this HeaderInfo object to byte array
        String startDateOfRecording = new SimpleDateFormat("dd.MM.yy").format(new Date(recordingStartTime));
        String startTimeOfRecording = new SimpleDateFormat("HH.mm.ss").format(new Date(recordingStartTime));

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(adjustLength(fileType.getVersion(), VERSION_LENGTH - 1));  // -1 because first non ascii byte (or "0" for edf) we will add later
        headerBuilder.append(adjustLength(patientIdentification, PATIENT_LENGTH));
        headerBuilder.append(adjustLength(recordingIdentification, RECORD_LENGTH));
        headerBuilder.append(startDateOfRecording);
        headerBuilder.append(startTimeOfRecording);
        headerBuilder.append(adjustLength(Integer.toString(getNumberOfBytesInHeaderRecord()), NUMBER_OF_BYTES_IN_HEADER_LENGTH));
        headerBuilder.append(adjustLength(fileType.getFirstReserved(), FIRST_RESERVED_LENGTH));
        headerBuilder.append(adjustLength(Integer.toString(getNumberOfDataRecords()), NUMBER_Of_DATARECORDS_LENGTH));
        headerBuilder.append(adjustLength(double2String(getDurationOfDataRecord()), DURATION_OF_DATARECORD_LENGTH));
        headerBuilder.append(adjustLength(Integer.toString(getNumberOfSignals()), NUMBER_OF_SIGNALS_LENGTH));


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

        for (int i = 0; i < getNumberOfSignals(); i++) {
            labels.append(adjustLength(getLabel(i), SIGNAL_LABEL_LENGTH));
            transducerTypes.append(adjustLength(getTransducer(i), SIGNAL_TRANSDUCER_TYPE_LENGTH));
            physicalDimensions.append(adjustLength(getPhysicalDimension(i), SIGNAL_PHYSICAL_DIMENSION_LENGTH));
            physicalMinimums.append(adjustLength(double2String(getPhysicalMin(i)), SIGNAL_PHYSICAL_MIN_LENGTH));
            physicalMaximums.append(adjustLength(double2String(getPhysicalMax(i)), SIGNAL_PHYSICAL_MAX_LENGTH));
            digitalMinimums.append(adjustLength(String.valueOf(getDigitalMin(i)), SIGNAL_DIGITAL_MIN_LENGTH));
            digitalMaximums.append(adjustLength(String.valueOf(getDigitalMax(i)), SIGNAL_DIGITAL_MAX_LENGTH));
            preFilterings.append(adjustLength(getPrefiltering(i), SIGNAL_PREFILTERING_LENGTH));
            samplesNumbers.append(adjustLength(Integer.toString(getNumberOfSamplesInEachDataRecord(i)), SIGNAL_NUMBER_OF_SAMPLES_LENGTH));
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
     * Gets the type of the file: EDF_16BIT or BDF_24BIT
     *
     * @return type of the file: EDF_16BIT or BDF_24BIT
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * Gets the patient identification string (name, surname, etc).
     *
     * @return patient identification string
     */
    public String getPatientIdentification() {
        return patientIdentification;
    }

    /**
     * Sets the patient identification string (name, surname, etc).
     * This method is optional
     *
     * @param patientIdentification patient identification string
     */
    public void setPatientIdentification(String patientIdentification) {
        this.patientIdentification = patientIdentification;
    }

    /**
     * Gets the recording identification string.
     *
     * @return recording (experiment) identification string
     */
    public String getRecordingIdentification() {
        return recordingIdentification;
    }

    /**
     * Sets the recording identification string.
     * This method is optional
     *
     * @param recordingIdentification recording (experiment) identification string
     */
    public void setRecordingIdentification(String recordingIdentification) {
        this.recordingIdentification = recordingIdentification;
    }

    /**
     * Gets recording start date and time measured in milliseconds,
     * since midnight, January 1, 1970 UTC.
     *
     * @return the difference, measured in milliseconds,
     * between the recording start time
     * and midnight, January 1, 1970 UTC.
     */
    public long getRecordingStartDateTimeMs() {
        return recordingStartTime;
    }


    /**
     * Sets recording start date and time.
     * If not called, EdfFileWriter will use the system date and time at runtime
     * since midnight, January 1, 1970 UTC.
     *
     * @param year   1970 - 3000
     * @param month  1 - 12
     * @param day    1 - 31
     * @param hour   0 - 23
     * @param minute 0 - 59
     * @param second 0 - 59
     */
    public void setRecordingStartDateTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        // in java month indexing from 0
        calendar.set(year, month - 1, day, hour, minute, second);
        this.recordingStartTime = calendar.getTimeInMillis();
    }


    /**
     * Sets recording start date and time measured in milliseconds,
     * since midnight, January 1, 1970 UTC.
     *
     * @param recordingStartTime the difference, measured in milliseconds,
     *                           between the recording start time
     *                           and midnight, January 1, 1970 UTC.
     */
    public void setRecordingStartDateTimeMs(long recordingStartTime) {
        this.recordingStartTime = recordingStartTime;
    }

    /**
     * Gets the number of DataRecords (data packages) in Edf/Bdf file.
     * The default value = -1 and real number of DataRecords is
     * set automatically when we finish to write data to the EdfWileWriter and close it
     *
     * @return number of DataRecords in the file or -1 if data writing is not finished
     */
    public int getNumberOfDataRecords() {
        return numberOfDataRecords;
    }

    /**
     * Sets the number of DataRecords (data packages) in Edf/Bdf file.
     * The default value = -1 means that file writing is not finished yet.
     * Normally EdfFileWriter calculate and sets the number of DataRecords automatically when
     * we finish to write data to the EdfWileWriter and close it
     *
     * @param numberOfDataRecords number of DataRecords (data packages) in Edf/Bdf file
     */
    public void setNumberOfDataRecords(int numberOfDataRecords) {
        this.numberOfDataRecords = numberOfDataRecords;
    }

    /**
     * Gets duration of DataRecords (data packages).
     *
     * @return duration of DataRecords in seconds
     */
    public double getDurationOfDataRecord() {
        return durationOfDataRecord;
    }


    /**
     * Sets duration of DataRecords (data packages) in seconds.
     * Default value = 1 sec.
     * @param durationOfDataRecord duration of DataRecords in seconds
     * @throws IllegalArgumentException  if durationOfDataRecord <= 0.
     */
    public void setDurationOfDataRecord(double durationOfDataRecord) throws IllegalArgumentException {
        if (durationOfDataRecord <= 0) {
            String errMsg = MessageFormat.format("Record duration is invalid: {0}. Expected {1}", Double.toString(durationOfDataRecord), ">0");
            throw new IllegalArgumentException(errMsg);
        }
        this.durationOfDataRecord = durationOfDataRecord;
    }

    /**
     * Sets the digital minimum and maximum values of the signal.
     * Usually it's the extreme output of the ADC.
     * <br>-32768 <= digitalMin <= digitalMax <= 32767 (EDF_16BIT  file format).
     * <br>-8388608 <= digitalMin <= digitalMax <= 8388607 (BDF_24BIT file format).
     * <p>
     * Digital min and max must be set for every signal!!!
     * <br>Default digitalMin = -32768,  digitalMax = 32767 (EDF_16BIT file format)
     * <br>Default digitalMin = -8388608,  digitalMax = 8388607 (BDF_24BIT file format)
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @param digitalMin   the minimum digital value of the signal
     * @param digitalMax   the maximum digital value of the signal
     * @throws IllegalArgumentException  if digitalMin >= digitalMax,
     *                            <br>if  32767 <= digitalMin  or digitalMin < -32768 (EDF_16BIT  file format).
     *                            <br>if  32767 < digitalMax  or digitalMax <= -32768 (EDF_16BIT  file format).
     *                            <br>if  8388607 <= digitalMin  or digitalMin < -8388608 (BDF_24BIT  file format).
     *                            <br>if  8388607 < digitalMax  or digitalMax <= -8388608 (BDF_24BIT  file format).
     */
    public void setDigitalRange(int signalNumber, int digitalMin, int digitalMax) throws IllegalArgumentException {
        if(digitalMin < fileType.getDigitalMin() || digitalMin >= fileType.getDigitalMax()) {
            String expected = fileType.getDigitalMin()+" <= digital min < "+fileType.getDigitalMax();
            String errMsg = MessageFormat.format("Digital min of signal {0} is invalid: {1}. Expected: {2}",  signalNumber, Integer.toString(digitalMin), expected);
            throw new IllegalArgumentException(errMsg);

        }

        if(digitalMax <= fileType.getDigitalMin() || digitalMax > fileType.getDigitalMax()) {
            String expected = fileType.getDigitalMin()+" < digital max <= "+fileType.getDigitalMax();
            String errMsg = MessageFormat.format("Digital max of signal {0} is invalid: {1}. Expected: {2}", signalNumber, Integer.toString(digitalMax), expected);
            throw new IllegalArgumentException(errMsg);
        }


        if(digitalMax <= digitalMin) {
            String errMsg = MessageFormat.format("Digital min/max range of signal {0} is invalid. Min = {1}, Max = {2}. Expected: {3}",signalNumber, Integer.toString(digitalMin), Integer.toString(digitalMax), "max > min");
            throw new IllegalArgumentException(errMsg);

        }
        signals.get(signalNumber).setDigitalRange(digitalMin, digitalMax);
    }

    /**
     * Sets the physical minimum and maximum values of the signal (the values of the input
     * of the ADC when the output equals the value of "digital minimum" and "digital maximum").
     * Usually physicalMin = - physicalMax.
     * <p>
     * Physical min and max must be set for every signal!!!
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @param physicalMin  the minimum physical value of the signal
     * @param physicalMax  the maximum physical value of the signal
     * @throws IllegalArgumentException  if physicalMin >= physicalMax
     */
    public void setPhysicalRange(int signalNumber, double physicalMin, double physicalMax) throws IllegalArgumentException {
        if(physicalMax <= physicalMin) {
            String errMsg = MessageFormat.format("Physical min/max range of signal {0} is invalid. Min = {1}, Max = {2}. Expected: {3}",  signalNumber, Double.toString(physicalMin), Double.toString(physicalMax), "max > min");
            throw new IllegalArgumentException(errMsg);
        }
        signals.get(signalNumber).setPhysicalRange(physicalMin, physicalMax);
    }


    /**
     * Sets the physical dimension of the signal ("uV", "BPM", "mA", "Degr.", etc.).
     * It is recommended to set physical dimension for every signal.
     *
     * @param signalNumber      number of the signal (channel). Numeration starts from 0
     * @param physicalDimension physical dimension of the signal ("uV", "BPM", "mA", "Degr.", etc.)
     */
    public void setPhysicalDimension(int signalNumber, String physicalDimension) {
        signals.get(signalNumber).setPhysicalDimension(physicalDimension);
    }

    /**
     * Sets the transducer of the signal ("AgAgCl cup electrodes", etc.).
     * This method is optional.
     *
     * @param signalNumber number of the signal (channel). Numeration starts from 0
     * @param transducer   string describing transducer (electrodes) used for measuring
     */
    public void setTransducer(int signalNumber, String transducer) {
        signals.get(signalNumber).setTransducer(transducer);
    }

    /**
     * Sets the prefilter of the signal ("HP:0.1Hz", "LP:75Hz N:50Hz", etc.).
     * This method is optional.
     *
     * @param signalNumber number of the signal (channel). Numeration starts from 0
     * @param prefiltering string describing filters that were applied to the signal
     */
    public void setPrefiltering(int signalNumber, String prefiltering) {
        signals.get(signalNumber).setPrefiltering(prefiltering);
    }


    /**
     * Sets the label (name) of signal.
     * It is recommended to set labels for every signal.
     *
     * @param signalNumber number of the signal (channel). Numeration starts from 0
     * @param label        label of the signal
     */
    public void setLabel(int signalNumber, String label) {
        signals.get(signalNumber).setLabel(label);
    }

    /**
     * Gets the label of the signal
     *
     * @param signalNumber number of the signal (channel). Numeration starts from 0
     * @return label of the signal
     */
    public String getLabel(int signalNumber) {
        return signals.get(signalNumber).getLabel();
    }

    public String getTransducer(int signalNumber) {
        return signals.get(signalNumber).getTransducer();
    }

    public String getPrefiltering(int signalNumber) {
        return signals.get(signalNumber).getPrefiltering();
    }

    public int getDigitalMin(int signalNumber) {
        return signals.get(signalNumber).getDigitalMin();
    }

    public int getDigitalMax(int signalNumber) {
        return signals.get(signalNumber).getDigitalMax();
    }

    public double getPhysicalMin(int signalNumber) {
        return signals.get(signalNumber).getPhysicalMin();
    }

    public double getPhysicalMax(int signalNumber) {
        return signals.get(signalNumber).getPhysicalMax();
    }

    public String getPhysicalDimension(int signalNumber) {
        return signals.get(signalNumber).getPhysicalDimension();
    }


    /**
     * Gets the number of samples belonging to the signal
     * in each DataRecord (data package).
     * See also {@link #getSampleFrequency(int)}.
     * <p>
     * When duration of DataRecords = 1 sec (default):
     * NumberOfSamplesInEachDataRecord = sampleFrequency
     *
     * @param signalNumber number of the signal (channel). Numeration starts from 0
     * @return number of samples belonging to the signal with the given sampleNumberToSignalNumber
     * in each DataRecord (data package)
     */
    public int getNumberOfSamplesInEachDataRecord(int signalNumber) {
        return signals.get(signalNumber).getNumberOfSamplesInEachDataRecord();
    }


    /**
     * Sets the number of samples belonging to the signal
     * in each DataRecord (data package).
     * See also {@link #setSampleFrequency(int, int)}.
     * <p>
     * When duration of DataRecords = 1 sec (default):
     * NumberOfSamplesInEachDataRecord = sampleFrequency
     * <p>
     * SampleFrequency o NumberOfSamplesInEachDataRecord must be set for every signal!!!
     *
     * @param signalNumber                    number of the signal(channel). Numeration starts from 0
     * @param numberOfSamplesInEachDataRecord number of samples belonging to the signal with the given sampleNumberToSignalNumber
     *                                        in each DataRecord
     * @throws IllegalArgumentException  if the given numberOfSamplesInEachDataRecord <= 0
     */
    public void setNumberOfSamplesInEachDataRecord(int signalNumber, int numberOfSamplesInEachDataRecord) throws IllegalArgumentException {
        if (numberOfSamplesInEachDataRecord <= 0) {
            String errMsg = MessageFormat.format("Number of samples in datarecord of signal {0} is invalid: {1}. Expected {2}", signalNumber, Integer.toString(numberOfSamplesInEachDataRecord), ">0");
            throw new IllegalArgumentException(errMsg);
        }
        signals.get(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamplesInEachDataRecord);
    }

    /**
     * Get the frequency of the samples belonging to the signal.
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return frequency of the samples (number of samples per second) belonging to the signal with the given number
     */
    public double getSampleFrequency(int signalNumber) {
        return signals.get(signalNumber).getNumberOfSamplesInEachDataRecord() / getDurationOfDataRecord();
    }


    /**
     * Sets the sample frequency of the signal.
     * This method is just a user friendly wrapper of the method
     * {@link #setNumberOfSamplesInEachDataRecord(int, int)}
     * <p>
     * When duration of DataRecords = 1 sec (default):
     * NumberOfSamplesInEachDataRecord = sampleFrequency
     * <p>
     * SampleFrequency o NumberOfSamplesInEachDataRecord must be set for every signal!!!
     *
     * @param signalNumber    number of the signal(channel). Numeration starts from 0
     * @param sampleFrequency frequency of the samples (number of samples per second) belonging to that channel
     * @throws IllegalArgumentException  if the given sampleFrequency <= 0
     */
    public void setSampleFrequency(int signalNumber, int sampleFrequency) throws IllegalArgumentException {
        if (sampleFrequency <= 0) {
            String errMsg =  MessageFormat.format("Sample frequency of signal {0} is invalid: {1}. Expected {2}", signalNumber, Double.toString(sampleFrequency), ">0");
            throw new IllegalArgumentException(errMsg);
        }
        Long numberOfSamplesInEachDataRecord = Math.round(sampleFrequency * durationOfDataRecord);
        setNumberOfSamplesInEachDataRecord(signalNumber, numberOfSamplesInEachDataRecord.intValue());
    }


    /**
     * Add new signal to the inner "signals list".
     */
    public void addSignal() {
        SignalInfo signalInfo = new SignalInfo();
        signalInfo.setLabel("Channel_" + signals.size());
        signalInfo.setDigitalRange(fileType.getDigitalMin(), fileType.getDigitalMax());
        signalInfo.setPhysicalRange(fileType.getDigitalMin(), fileType.getDigitalMax());
        signals.add(signalInfo);

    }

    /**
     * Removes the signal from the inner "signals list".
     * @param signalNumber number of the signal(channel) to remove. Numeration starts from 0
     */
    public void removeSignal(int signalNumber) {
        signals.remove(signalNumber);
    }


    /**
     * Return the number of measuring channels (signals).
     *
     * @return the number of measuring channels
     */
    public int getNumberOfSignals() {
        return signals.size();
    }


    /**
     * Helper method. Calculate total number of samples from all channels (signals) in each data record
     *
     * @return sum of samples from all channels
     */
    public int getDataRecordLength() {
        int totalNumberOfSamplesInRecord = 0;
        for (int i = 0; i < signals.size(); i++) {
            totalNumberOfSamplesInRecord += signals.get(i).getNumberOfSamplesInEachDataRecord();
        }
        return totalNumberOfSamplesInRecord;
    }

    /**
     * Helper method. Calculates  the signal to which the given sample belongs to.
     *
     * @param sampleNumber the number of the sample calculated from the beginning of recording
     * @return the signal number to which the given sample belongs to
     * @throws IllegalArgumentException if sampleNumber < 1
     */
    public int sampleNumberToSignalNumber(long sampleNumber) throws IllegalArgumentException{
        if (sampleNumber < 1) {
            String errMsg =  MessageFormat.format("Sample number is invalid: {0}. Expected {1}", sampleNumber, ">=1");
            throw new IllegalArgumentException(errMsg);
        }
        int recordLength = getDataRecordLength();
        sampleNumber = (sampleNumber % recordLength == 0) ? recordLength : sampleNumber % recordLength;

        int samplesCounter = 0;
        for (int signalNumber = 0; signalNumber < getNumberOfSignals(); signalNumber++) {
            samplesCounter += getNumberOfSamplesInEachDataRecord(signalNumber);
            if (sampleNumber <= samplesCounter) {
                return signalNumber;
            }
        }
        return 0;
    }


    /**
     * Helper method. Convert the physical value to digital one for the given signal.
     *
     * @param physValue    physical value that has to be converted to digital one
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return resultant digital value
     */
    public int physicalValueToDigital(int signalNumber, double physValue) {
        return signals.get(signalNumber).physicalValueToDigital(physValue);

    }

    /**
     * Helper method. Converts the digital value to physical one for the given signal.
     *
     * @param digValue     digital value that has to be converted to physical one
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return resultant physical value
     */
    public double digitalValueToPhysical(int signalNumber, int digValue) {
        return signals.get(signalNumber).digitalValueToPhysical(digValue);
    }

    /**
     * Helper method. Converts the given physical DataRecord to digital DataRecord
     * @param physRecord array with physical values from all signals (physical DataRecord)
     * @param digRecord array where resultant digital values will be stored
     */
    public void physicalDataRecordToDigital(double[] physRecord, int[] digRecord) {
        int sampleCounter = 0;
        for(int signalNumber = 0; signalNumber < getNumberOfSignals(); signalNumber++) {
            int numberOfSamples = signals.get(signalNumber).getNumberOfSamplesInEachDataRecord();
            for(int i = 0; i < numberOfSamples; i++) {
                digRecord[sampleCounter] = physicalValueToDigital(signalNumber, physRecord[sampleCounter]);
                sampleCounter++;
            }
        }
    }

    /**
     * Helper method. Converts the given digital DataRecord to physical DataRecord
     * @param digRecord array with digital values from all signals (digital DataRecord)
     * @param physRecord array where resultant physical values will be stored
     */
    public void digitalDataRecordToPhysical(int[] digRecord, double[] physRecord) {
        int sampleCounter = 0;
        for(int signalNumber = 0; signalNumber < getNumberOfSignals(); signalNumber++) {
            int numberOfSamples = signals.get(signalNumber).getNumberOfSamplesInEachDataRecord();
            for(int i = 0; i < numberOfSamples; i++) {
                physRecord[sampleCounter] = digitalValueToPhysical(signalNumber, digRecord[sampleCounter]);
                sampleCounter++;
            }
        }
    }


    /**
     * Get the number of bytes in the EDF/BDF header record (when we will create it on the base of this HeaderInfo)
     *
     * @return number of bytes in EDF/BDF header = (number of signals + 1) * 256
     */
    public int getNumberOfBytesInHeaderRecord() {
        return 256 + (getNumberOfSignals() * 256);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\nfile type = " + getFileType());
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        String timeStamp = dateFormat.format(new Date(getRecordingStartDateTimeMs()));
        sb.append("\nStart date and time = " + timeStamp + " (" + getRecordingStartDateTimeMs() + " ms)");
        sb.append("\nPatient identification = " + getPatientIdentification());
        sb.append("\nRecording identification = " + getRecordingIdentification());
        sb.append("\nNumber of DataRecords = " + getNumberOfDataRecords());
        sb.append("\nDuration of DataRecords = " + getDurationOfDataRecord());
        sb.append("\nNumber of signals = " + getNumberOfSignals());
        for (int i = 0; i < getNumberOfSignals(); i++) {
            sb.append("\n  " + i + " label: " + getLabel(i)
                    + "; number of samples: " + getNumberOfSamplesInEachDataRecord(i)
                    + "; frequency: " + Math.round(getSampleFrequency(i))
                    + "; dig min: " + getDigitalMin(i) + "; dig max: " + getDigitalMax(i)
                    + "; phys min: " + getPhysicalMin(i) + "; phys max: " + getPhysicalMax(i)
                    + "; prefiltering: " + getPrefiltering(i)
                    + "; transducer: " + getTransducer(i)
                    + "; dimension: " + getPhysicalDimension(i));
        }
        sb.append("\n");
        return sb.toString();
    }


    /**
     * Convert String to in
     *
     * @param str string to convert
     * @return resultant int
     * @throws NumberFormatException - if the string does not contain a parsable integer.
     */
    private static Integer stringToInt(String str) throws NumberFormatException {
        str = str.trim();
        return Integer.valueOf(str);
    }


    /**
     * Convert String to double
     *
     * @param str string to convert
     * @return resultant double
     * @throws NumberFormatException - if the string does not contain a parsable double.
     */
    private static Double stringToDouble(String str) throws NumberFormatException {
        str = str.trim();
        return Double.valueOf(str);
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

    /**
     * Unit Test. Usage Example.
     * <p>
     * Create and print default Edf and Bdf HeaderInfo
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        int numberOfSignals = 3;
        HeaderInfo headerInfoEdf = new HeaderInfo(numberOfSignals, FileType.EDF_16BIT);
        HeaderInfo headerInfoBdf = new HeaderInfo(numberOfSignals, FileType.BDF_24BIT);

        // set start date and time for Bdf HeaderInfo
        headerInfoBdf.setRecordingStartDateTime(1972, 6, 23, 23, 23, 50);
        // print header info
        System.out.println(headerInfoEdf);
        System.out.println(headerInfoBdf);
    }
}



