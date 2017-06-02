package com.biorecorder.edflib;

import com.biorecorder.edflib.base.DefaultEdfRecordingInfo;
import com.biorecorder.edflib.base.EdfRecordingInfo;
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

public class HeaderInfo extends DefaultEdfRecordingInfo {
    private static final String ERR_MSG_START = "Header error! ";
    private int numberOfDataRecords = -1;
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
     *  @param fileType EDF_16BIT or BDF_24BIT
     */
    public HeaderInfo(FileType fileType) {
        this.fileType = fileType;
    }


    /**
     * This constructor creates a HeaderInfo instance of the given type (EDF_16BIT or BDF_24BIT)
     * with the given number of channels (signals)
     *
     * @param numberOfSignals number of signals in data records
     * @param fileType  EDF_16BIT or BDF_24BIT
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
     * @param recordingInfo HeaderInfo instance that will be copied
     */
    public HeaderInfo(EdfRecordingInfo recordingInfo, FileType fileType) {
        this(recordingInfo.getNumberOfSignals(), fileType);
        setPatientIdentification(recordingInfo.getPatientIdentification());
        setRecordingIdentification(recordingInfo.getRecordingIdentification());
        setRecordingStartDateTimeMs(recordingInfo.getRecordingStartDateTimeMs());
        setDurationOfDataRecord(recordingInfo.getDurationOfDataRecord());
        for (int i = 0; i < recordingInfo.getNumberOfSignals(); i++) {
            setNumberOfSamplesInEachDataRecord(i, recordingInfo.getNumberOfSamplesInEachDataRecord(i));
            setPrefiltering(i, recordingInfo.getPrefiltering(i));
            setTransducer(i, recordingInfo.getTransducer(i));
            setLabel(i, recordingInfo.getLabel(i));
            setDigitalRange(i, recordingInfo.getDigitalMin(i), recordingInfo.getDigitalMax(i));
            setPhysicalRange(i, recordingInfo.getPhysicalMin(i), recordingInfo.getPhysicalMax(i));
            setPhysicalDimension(i, recordingInfo.getPhysicalDimension(i));
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
            setPatientIdentification(new String(buffer).trim());

            buffer = new char[RECORD_LENGTH];
            readBuffer(reader, buffer);
            setRecordingIdentification(new String(buffer).trim());

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
                setRecordingStartDateTimeMs(dateTimeFormat.parse(dateTimeString).getTime());
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
                setDurationOfDataRecord(stringToDouble(recordDurationString));
            } catch (NumberFormatException e) {
                String errMsg = MessageFormat.format("{0}Record duration field is invalid: \"{1}\". Expected: {2}", ERR_MSG_START, recordDurationString, "double");
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.RECORD_DURATION_NAN, errMsg, e);
                ex.setValue(recordDurationString);
                ex.setExpectedValue("double");
                throw ex;
            }
            if (getDurationOfDataRecord() <= 0) {
                String errMsg = MessageFormat.format("{0}Record duration is invalid: {1}. Expected {2}", ERR_MSG_START, Double.toString(getDurationOfDataRecord()), ">0");
                EdfHeaderRuntimeException ex = new EdfHeaderRuntimeException(ExceptionType.RECORD_DURATION_NONPOSITIVE, errMsg);
                ex.setValue(String.valueOf(getDurationOfDataRecord()));
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
                addSignal();
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
                setPhysicalRange(signalNumber, physMinList[signalNumber], physMaxList[signalNumber]);

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
                setDigitalRange(signalNumber, digMinList[signalNumber],digMaxList[signalNumber]);
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
                    setNumberOfSamplesInEachDataRecord(signalNumber, numberOfSamples);

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
        if (getNumberOfSignals() == 0) {
            String errMsg = MessageFormat.format("{0}Number of Signals = 0. Expected: > 0", ERR_MSG_START);
            EdfHeaderRuntimeException ex =  new EdfHeaderRuntimeException(ExceptionType.NUMBER_OF_SIGNALS_NONPOSITIVE, errMsg);
            ex.setValue("0");
            ex.setExpectedValue(">0");
            throw ex;
        }

        for (int signalNumber = 0; signalNumber < getNumberOfSignals(); signalNumber++) {
            int numberOfSamples = getNumberOfSamplesInEachDataRecord(signalNumber);
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
        String startDateOfRecording = new SimpleDateFormat("dd.MM.yy").format(new Date(getRecordingStartDateTimeMs()));
        String startTimeOfRecording = new SimpleDateFormat("HH.mm.ss").format(new Date(getRecordingStartDateTimeMs()));

        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(adjustLength(fileType.getVersion(), VERSION_LENGTH - 1));  // -1 because first non ascii byte (or "0" for edf) we will add later
        headerBuilder.append(adjustLength(getPatientIdentification(), PATIENT_LENGTH));
        headerBuilder.append(adjustLength(getRecordingIdentification(), RECORD_LENGTH));
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
     * Get the number of bytes in the EDF/BDF header record (when we will create it on the base of this HeaderInfo)
     *
     * @return number of bytes in EDF/BDF header = (number of signals + 1) * 256
     */
    public int getNumberOfBytesInHeaderRecord() {
        return 256 + (getNumberOfSignals() * 256);
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
    @Override
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
        super.setDigitalRange(signalNumber, digitalMin, digitalMax);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
      //  sb.append(super.toString());
        sb.append("file type = " + getFileType());
        sb.append("\nNumber of DataRecords = " + getNumberOfDataRecords());
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        String timeStamp = dateFormat.format(new Date(getRecordingStartDateTimeMs()));
        sb.append("\nStart date and time = " + timeStamp + " (" + getRecordingStartDateTimeMs() + " ms)");
        sb.append("\nPatient identification = " + getPatientIdentification());
        sb.append("\nRecording identification = " + getRecordingIdentification());
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
        return sb.toString();
    }

    /**
     * Convert String to int
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



