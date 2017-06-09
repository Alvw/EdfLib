package com.biorecorder.edflib.base;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DefaultEdfConfig extends EdfConfig {
    private String patientIdentification = "Default patient";
    private String recordingIdentification = "Default record";
    private double durationOfDataRecord = 1; // sec
    private long recordingStartTime = -1;
    private ArrayList<SignalConfig> signals = new ArrayList<SignalConfig>();


    /**
     * Default constructor that creates a DefaultEdfConfig instance
     * with 0 channels (signals). So all channels should be added as necessary.
     * <p>
     * See method: {@link #addSignal()}
     */
    public DefaultEdfConfig() {

    }

    /**
     * This constructor creates a DefaultEdfConfig instance of the given type (EDF_16BIT or BDF_24BIT)
     * with the given number of channels (signals)
     *
     * @param numberOfSignals number of signals in data records
     * @throws IllegalArgumentException if numberOfSignals <= 0
     */
    public DefaultEdfConfig(int numberOfSignals) throws IllegalArgumentException {
        if (numberOfSignals <= 0) {
            String errMsg = MessageFormat.format("Number of signals is invalid: {0}. Expected {1}", numberOfSignals, ">0");
            throw new IllegalArgumentException(errMsg);
        }
        for (int i = 0; i < numberOfSignals; i++) {
            addSignal();
        }
    }

    /**
     * Constructor to make a copy of the given DefaultEdfConfig instance
     *
     * @param recordingInfo DefaultEdfConfig instance that will be copied
     */
    public DefaultEdfConfig(EdfConfig recordingInfo) {
        this(recordingInfo.getNumberOfSignals());
        durationOfDataRecord = recordingInfo.getDurationOfDataRecord();
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


    @Override
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

    @Override
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

    @Override
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


    @Override
    public double getDurationOfDataRecord() {
        return durationOfDataRecord;
    }


    /**
     * Sets duration of DataRecords (data packages) in seconds.
     * Default value = 1 sec.
     *
     * @param durationOfDataRecord duration of DataRecords in seconds
     * @throws IllegalArgumentException if durationOfDataRecord <= 0.
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
     * @throws IllegalArgumentException if digitalMin >= digitalMax,
     **/
    public void setDigitalRange(int signalNumber, int digitalMin, int digitalMax) throws IllegalArgumentException {
        if (digitalMax <= digitalMin) {
            String errMsg = MessageFormat.format("Digital min/max range of signal {0} is invalid. Min = {1}, Max = {2}. Expected: {3}", signalNumber, Integer.toString(digitalMin), Integer.toString(digitalMax), "max > min");
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
     * @throws IllegalArgumentException if physicalMin >= physicalMax
     */
    public void setPhysicalRange(int signalNumber, double physicalMin, double physicalMax) throws IllegalArgumentException {
        if (physicalMax <= physicalMin) {
            String errMsg = MessageFormat.format("Physical min/max range of signal {0} is invalid. Min = {1}, Max = {2}. Expected: {3}", signalNumber, Double.toString(physicalMin), Double.toString(physicalMax), "max > min");
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

    @Override
    public String getLabel(int signalNumber) {
        return signals.get(signalNumber).getLabel();
    }

    public String getTransducer(int signalNumber) {
        return signals.get(signalNumber).getTransducer();
    }

    @Override
    public String getPrefiltering(int signalNumber) {
        return signals.get(signalNumber).getPrefiltering();
    }
    @Override
    public int getDigitalMin(int signalNumber) {
        return signals.get(signalNumber).getDigitalMin();
    }
    @Override
    public int getDigitalMax(int signalNumber) {
        return signals.get(signalNumber).getDigitalMax();
    }
    @Override
    public double getPhysicalMin(int signalNumber) {
        return signals.get(signalNumber).getPhysicalMin();
    }
    @Override
    public double getPhysicalMax(int signalNumber) {
        return signals.get(signalNumber).getPhysicalMax();
    }
    @Override
    public String getPhysicalDimension(int signalNumber) {
        return signals.get(signalNumber).getPhysicalDimension();
    }



    @Override
    public int getNumberOfSamplesInEachDataRecord(int signalNumber) {
        return signals.get(signalNumber).getNumberOfSamplesInEachDataRecord();
    }


    /**
     * Sets the number of samples belonging to the signal
     * in each DataRecord (data package).
     * <p>
     * When duration of DataRecords = 1 sec (default):
     * NumberOfSamplesInEachDataRecord = sampleFrequency
     * <p>
     * SampleFrequency o NumberOfSamplesInEachDataRecord must be set for every signal!!!
     *
     * @param signalNumber                    number of the signal(channel). Numeration starts from 0
     * @param numberOfSamplesInEachDataRecord number of samples belonging to the signal with the given sampleNumberToSignalNumber
     *                                        in each DataRecord
     * @throws IllegalArgumentException if the given numberOfSamplesInEachDataRecord <= 0
     */
    public void setNumberOfSamplesInEachDataRecord(int signalNumber, int numberOfSamplesInEachDataRecord) throws IllegalArgumentException {
        if (numberOfSamplesInEachDataRecord <= 0) {
            String errMsg = MessageFormat.format("Number of samples in datarecord of signal {0} is invalid: {1}. Expected {2}", signalNumber, Integer.toString(numberOfSamplesInEachDataRecord), ">0");
            throw new IllegalArgumentException(errMsg);
        }
        signals.get(signalNumber).setNumberOfSamplesInEachDataRecord(numberOfSamplesInEachDataRecord);
    }


    /**
     * Add new signal to the inner "signals list".
     */
    public void addSignal() {
        SignalConfig signalInfo = new SignalConfig();
        signalInfo.setLabel("Channel_" + signals.size());
        signals.add(signalInfo);

    }

    /**
     * Removes the signal from the inner "signals list".
     *
     * @param signalNumber number of the signal(channel) to remove. Numeration starts from 0
     */
    public void removeSignal(int signalNumber) {
        signals.remove(signalNumber);
    }


    @Override
    public int getNumberOfSignals() {
        return signals.size();
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
     * @throws IllegalArgumentException if the given sampleFrequency <= 0
     */
    public void setSampleFrequency(int signalNumber, int sampleFrequency) throws IllegalArgumentException {
        if (sampleFrequency <= 0) {
            String errMsg = MessageFormat.format("Sample frequency of signal {0} is invalid: {1}. Expected {2}", signalNumber, Double.toString(sampleFrequency), ">0");
            throw new IllegalArgumentException(errMsg);
        }
        Long numberOfSamplesInEachDataRecord = Math.round(sampleFrequency * durationOfDataRecord);
        setNumberOfSamplesInEachDataRecord(signalNumber, numberOfSamplesInEachDataRecord.intValue());
    }

    public String toString_() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\nPatient identification = " + getPatientIdentification());
        sb.append("\nRecording identification = " + getRecordingIdentification());
        sb.append("\nDuration of DataRecords = " + getDurationOfDataRecord());
        sb.append("\nNumber of signals = " + getNumberOfSignals());
        for (int i = 0; i < getNumberOfSignals(); i++) {
            sb.append("\n  " + i + " label: " + getLabel(i)
                    + "; number of samples: " + getNumberOfSamplesInEachDataRecord(i)
                    + "; frequency: "+  Math.round(getSampleFrequency(i))
                    + "; dig min: " + getDigitalMin(i) + "; dig max: " + getDigitalMax(i)
                    + "; phys min: " + getPhysicalMin(i) + "; phys max: " + getPhysicalMax(i)
                    + "; prefiltering: " + getPrefiltering(i)
                    + "; transducer: " + getTransducer(i)
                    + "; dimension: " + getPhysicalDimension(i));
        }
        sb.append("\n");
        return sb.toString();
    }

}
