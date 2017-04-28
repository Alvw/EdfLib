package com.biorecorder.edflib;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * This class allows to store the information required to create EDF/BDF file header
 * and correctly extract data from Data Records. It has the following fields
 * (and their corresponding getters and setters):
 * <ul>
 *     <li>patient identification</li>
 *     <li>recording identification</li>
 *     <li>recording recordingStartTime</li>
 *     <li>number of data records</li>
 *     <li>duration of a data record (in seconds)</li>
 * </ul>
 * <p>
 * Also it permits to configure the measuring channels (signals) and has
 * getters and setters for the fields describing every channel:
 *  <ul>
 *     <li>signal label</li>
 *     <li>transducer type (e.g. AgAgCI electrode)</li>
 *     <li>physical dimension(e.g. uV or degree C)</li>
 *     <li>physical minimum (e.g. -500 or 34)</li>
 *     <li>physical maximum (e.g. 500 or 40)</li>
 *     <li>digital minimum (e.g. -2048)</li>
 *     <li>digital maximum (e.g. 2047)</li>
 *     <li>prefiltering (e.g. HP:0.1Hz LP:75Hz)</li>
 *     <li>number of samples in each data record</li>
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
 * <p>BDF HEADER RECORD
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

public class HeaderConfig {
    private String patientIdentification = "Default patient";
    private String recordingIdentification = "Default record";
    private long recordingStartTime = -1;
    private int numberOfDataRecords = -1;
    private double durationOfDataRecord = 1; // sec
    private ArrayList<SignalConfig> signals = new ArrayList<SignalConfig>();
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
     * Default constructor that creates a HeaderConfig instance
     * with 0 channels (signals). So all channels should be added as necessary.
     * <p>
     * See method: {@link #addSignal()}
     */
    public HeaderConfig(FileType fileType) {
        this.fileType = fileType;
    }


    /**
     * This constructor creates a HeaderConfig instance of the given type (EDF_16BIT or BDF_24BIT)
     * with the given number of channels (signals)
     *
     * @param numberOfSignals number of signals in data records
     * @param fileType EDF_16BIT or BDF_24BIT
     */
    public HeaderConfig(int numberOfSignals, FileType fileType) {
        this.fileType = fileType;
        for (int i = 0; i < numberOfSignals; i++) {
            signals.add(new SignalConfig());
        }
    }

    /**
     * Create HeaderConfig on the base of header info of the given EDF or BDF file
     *
     * @param file file to read
     * @throws IOException            if the file can not be read
     * @throws HeaderParsingException if the file header is not valid EDF/BDF file header.
     */
    public HeaderConfig (File file) throws IOException, HeaderParsingException {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), ASCII));

        char[] buffer;
        buffer = new char[VERSION_LENGTH];
        reader.read(buffer);

        char firstChar = buffer[0];
        if(firstChar == '0') {
            fileType = FileType.EDF_16BIT;

        } else if((Character.getNumericValue(firstChar) & 0xFF) ==  255) {
            fileType = FileType.BDF_24BIT;
        } else {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. First byte should be equal '0' or 255");
        }

        buffer = new char[PATIENT_LENGTH];
        reader.read(buffer);
        patientIdentification = new String(buffer).trim();


        buffer = new char[RECORD_LENGTH];
        reader.read(buffer);
        recordingIdentification = new String(buffer).trim();

        buffer = new char[STARTDATE_LENGTH];
        reader.read(buffer);
        String startDateStr = new String(buffer);

        buffer = new char[STARTTIME_LENGTH];
        reader.read(buffer);
        String startTimeStr = new String(buffer);
        String dateFormat = "dd.MM.yy HH.mm.ss";
        String startDateTimeStr = startDateStr + " " + startTimeStr;

        try {
            Date date = new SimpleDateFormat(dateFormat).parse(startDateTimeStr);
            recordingStartTime = date.getTime();
        } catch (Exception e) {
            throw new HeaderParsingException("Invalid Edf/Bdf file header. Error while parsing header Date-Time: " + startDateTimeStr);
        }

        buffer = new char[NUMBER_OF_BYTES_IN_HEADER_LENGTH];
        reader.read(buffer);


        buffer = new char[FIRST_RESERVED_LENGTH];
        reader.read(buffer);

        buffer = new char[NUMBER_Of_DATARECORDS_LENGTH];
        reader.read(buffer);
        numberOfDataRecords = stringToInt(new String(buffer));

        buffer = new char[DURATION_OF_DATARECORD_LENGTH];
        reader.read(buffer);
        durationOfDataRecord = stringToDouble(new String(buffer));

        buffer = new char[NUMBER_OF_SIGNALS_LENGTH];
        reader.read(buffer);
        int numberOfSignals = stringToInt(new String(buffer));

        signals = new ArrayList<SignalConfig>(numberOfSignals);
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            signals.add(new SignalConfig());
        }

        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_LABEL_LENGTH];
            reader.read(buffer);
            setLabel(signalNumber, new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_TRANSDUCER_TYPE_LENGTH];
            reader.read(buffer);
            setTransducer(signalNumber, new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_DIMENSION_LENGTH];
            reader.read(buffer);
            setPhysicalDimension(signalNumber, new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MIN_LENGTH];
            reader.read(buffer);
            double physicalMin = stringToDouble(new String(buffer));
            setPhysicalMin(signalNumber, physicalMin);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PHYSICAL_MAX_LENGTH];
            reader.read(buffer);
            double physicalMax = stringToDouble(new String(buffer));
            setPhysicalMax(signalNumber, physicalMax);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MIN_LENGTH];
            reader.read(buffer);
            int digitalMin = stringToInt(new String(buffer));
            setDigitalMin(signalNumber, digitalMin);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_DIGITAL_MAX_LENGTH];
            reader.read(buffer);
            int digitalMax = stringToInt(new String(buffer));
            setDigitalMax(signalNumber, digitalMax);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_PREFILTERING_LENGTH];
            reader.read(buffer);
            setPrefiltering(signalNumber, new String(buffer).trim());
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_NUMBER_OF_SAMPLES_LENGTH];
            reader.read(buffer);
            int numberOfSamplesInDataRecord = stringToInt(new String(buffer));
            setNumberOfSamplesInEachDataRecord(signalNumber, numberOfSamplesInDataRecord);
        }
        for (int signalNumber = 0; signalNumber < numberOfSignals; signalNumber++) {
            buffer = new char[SIGNAL_RESERVED_LENGTH];
            reader.read(buffer);
        }
        reader.close();
    }


    /**
     * Constructor to make a copy of the given HeaderConfig instance
     *
     * @param headerConfig HeaderConfig instance that will be copied
     */
    public HeaderConfig(HeaderConfig headerConfig) {
        this(headerConfig.getNumberOfSignals(), headerConfig.getFileType());
        patientIdentification = headerConfig.getPatientIdentification();
        recordingIdentification = headerConfig.getRecordingIdentification();
        recordingStartTime = headerConfig.getRecordingStartTime();
        durationOfDataRecord = headerConfig.getDurationOfDataRecord();
        numberOfDataRecords = headerConfig.getNumberOfDataRecords();
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            setNumberOfSamplesInEachDataRecord(i, headerConfig.getNumberOfSamplesInEachDataRecord(i));
            setPrefiltering(i, headerConfig.getPrefiltering(i));
            setTransducer(i, headerConfig.getTransducer(i));
            setLabel(i, headerConfig.getLabel(i));
            setDigitalMin(i, headerConfig.getDigitalMin(i));
            setDigitalMax(i, headerConfig.getDigitalMax(i));
            setPhysicalMin(i, headerConfig.getPhysicalMin(i));
            setPhysicalMax(i, headerConfig.getPhysicalMax(i));
            setPhysicalDimension(i, headerConfig.getPhysicalDimension(i));
        }
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getPatientIdentification() {
        return patientIdentification;
    }

    public void setPatientIdentification(String patientIdentification) {
        this.patientIdentification = patientIdentification;
    }

    public String getRecordingIdentification() {
        return recordingIdentification;
    }

    public void setRecordingIdentification(String recordingIdentification) {
        this.recordingIdentification = recordingIdentification;
    }

    public long getRecordingStartTime() {
        return recordingStartTime;
    }

    public void setRecordingStartTime(long recordingStartTime) {
        this.recordingStartTime = recordingStartTime;
    }

    public int getNumberOfDataRecords() {
        return numberOfDataRecords;
    }

    public void setNumberOfDataRecords(int numberOfDataRecords) {
        this.numberOfDataRecords = numberOfDataRecords;
    }

    public double getDurationOfDataRecord() {
        return durationOfDataRecord;
    }

    public void setDurationOfDataRecord(double durationOfDataRecord) {
        this.durationOfDataRecord = durationOfDataRecord;
    }


    public void setDigitalMin(int signalNumber, int digitalMin) {
        signals.get(signalNumber).setDigitalMin(digitalMin);
    }

    public void setDigitalMax(int signalNumber, int digitalMax) {
        signals.get(signalNumber).setDigitalMax(digitalMax);
    }

    public void setPhysicalMin(int signalNumber, double physicalMin) {
        signals.get(signalNumber).setPhysicalMin(physicalMin);
    }

    public void setPhysicalMax(int signalNumber, double physicalMax) {
        signals.get(signalNumber).setPhysicalMax(physicalMax);
    }

    public void setPhysicalDimension(int signalNumber, String physicalDimension) {
        signals.get(signalNumber).setPhysicalDimension(physicalDimension);
    }

    public void setPrefiltering(int signalNumber, String prefiltering) {
        signals.get(signalNumber).setPrefiltering(prefiltering);
    }

    public void setTransducer(int signalNumber, String transducer) {
        signals.get(signalNumber).setTransducer(transducer);
    }

    public void setLabel(int signalNumber, String label) {
        signals.get(signalNumber).setLabel(label);
    }

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

    public int getNumberOfSamplesInEachDataRecord(int signalNumber) {
        return signals.get(signalNumber).getNumberOfSamplesInEachDataRecord();
    }

    public void setNumberOfSamplesInEachDataRecord(int signalNumber, int numberOfSamplesInEachDataRecord) {
        signals.get(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamplesInEachDataRecord);
    }


    public double getSampleRate(int signalNumber) {
        return signals.get(signalNumber).getNumberOfSamplesInEachDataRecord() / durationOfDataRecord;
    }

    public void setSampleRate(int signalNumber, int sampleRate) {
        int numberOfSamplesInEachDataRecord = (int)(sampleRate * durationOfDataRecord);
        setNumberOfSamplesInEachDataRecord(signalNumber, numberOfSamplesInEachDataRecord );

    }


    /**
     * HeaderConfig must describe all its measuring channels (signals).
     * And we have to add them successively in the same order in which the samples belonging to the
     * channels will be placed (saved) in DataRecords
     */
    public void addSignal() {
        signals.add(new SignalConfig());
    }

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
    public int getRecordLength() {
        int totalNumberOfSamplesInRecord = 0;
        for (int i = 0; i < signals.size(); i++) {
            totalNumberOfSamplesInRecord += signals.get(i).getNumberOfSamplesInEachDataRecord();
        }
        return totalNumberOfSamplesInRecord;
    }

    /**
     * Helper method. Give the  number of signal corresponding to the given
     * sample number calculating from the beginning of recording
     *
     * @param sampleNumber sample number
     * @return signal number corresponding to the given sample position
     */
    public int signalNumber(long sampleNumber) {
        if (sampleNumber < 1) {
            throw new RuntimeException("Sample number could not be < 1!");
        }

        int recordLength = getRecordLength();
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
     * Helper method. Convert the physical value belonging to the given signal to digital one
     *
     * @param physValue physical value that has to be converted to digital one
     * @param signalNumber  number of the signal the physical value belongs to
     * @return resultant digital value
     */
    public int physicalValueToDigital(int signalNumber, double physValue) {
        return signals.get(signalNumber).physicalValueToDigital(physValue);

    }

    /**
     * Helper method. Convert the digital value belonging to the given signal to physical one.
     *
     * @param digValue digital value that has to be converted to physical one
     * @param signalNumber  number of the signal the digital value belongs to
     * @return resultant physical value
     */
    public  double digitalValueToPhysical(int signalNumber, int digValue) {
        return signals.get(signalNumber).digitalValueToPhysical(digValue);
    }


    /**
     * Get the number of bytes in the EDF/BDF header record (when we will create it on the base of this HeaderConfig)
     *
     * @return number of bytes in EDF/BDF header = (number of signals + 1) * 256
     */
    public int getNumberOfBytesInHeaderRecord() {
        return 256 + (getNumberOfSignals() * 256);
    }

    public byte[] createFileHeader() {
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

    public double getSampleFrequency(int signalNumber) {
        return signals.get(signalNumber).getNumberOfSamplesInEachDataRecord() / getDurationOfDataRecord();
    }

    /**
     * Get some header info as a String
     * @return some header info as a String
     */
    public String headerToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("file type = " + getFileType());
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        String timeStamp = dateFormat.format(new Date(getRecordingStartTime()));
        sb.append("\nStart date and time = "+timeStamp);
        sb.append("\nDuration of DataRecords = " + getDurationOfDataRecord());
        sb.append("\nNumber of signals = " + getNumberOfSignals());
        for (int i = 0; i < getNumberOfSignals(); i++) {
            sb.append("\n "+i + ": label = " + getLabel(i)
                    + "; number of samples in data records = " + getNumberOfSamplesInEachDataRecord(i)
                    + "; frequency = "+ Math.round(getSampleFrequency(i))
                    + "; prefiltering = " + getPrefiltering(i));
        }
        sb.append("\n");
        return sb.toString();
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



