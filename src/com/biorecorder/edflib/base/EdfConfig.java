package com.biorecorder.edflib.base;

import java.text.MessageFormat;

/**
 * Provides the the following base info:
 * <ul>
 * <li>patient identification</li>
 * <li>recording identification</li>
 * <li>recording start date and time</li>
 * <li>duration of  data records (in seconds)</li>
 * </ul>
 * And the information about every measuring channel (signal). Such as:
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
 * </p>
 * Describes the structure of edf data records or packages.
 * Each data record contains data from multiple signals
 * and has the following structure:
 * <br>samples belonging to signal 0,
 * <br>samples belonging to signal 1,
 * <br>...
 * <br>samples belonging to  signal n
 * <p>
 * Where number of samples for every signal:
 * <br>n_i = (sample frequency of the signal_i) * (duration of DataRecord).
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
 * Contains helper methods to convert physical (floating point) values
 * to digital (integer) ones and vice versa.
 * <p>
 * Detailed information about EDF/BDF format:
 * <a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <a href="https://www.biosemi.com/faq/file_format.htm">BioSemi or BDF file format</a>
 *
 */
public abstract class EdfConfig {

    /**
     * Return the number of measuring channels (signals).
     *
     * @return the number of measuring channels
     */
    public abstract int getNumberOfSignals();

    /**
     * Gets duration of DataRecords (data packages).
     *
     * @return duration of DataRecords in seconds
     */
    public abstract double getDurationOfDataRecord();

    /**
     * Gets recording start date and time measured in milliseconds,
     * since midnight, January 1, 1970 UTC.
     *
     * @return the difference, measured in milliseconds,
     * between the recording start time
     * and midnight, January 1, 1970 UTC.
     */
    public abstract long getRecordingStartDateTimeMs();

    /**
     * Gets the patient identification string (name, surname, etc).
     *
     * @return patient identification string
     */
    public abstract String getPatientIdentification();

    /**
     * Gets the recording identification string.
     *
     * @return recording (experiment) identification string
     */
    public abstract String getRecordingIdentification();


    /**
     * Gets the label of the signal
     *
     * @param signalNumber number of the signal (channel). Numeration starts from 0
     * @return label of the signal
     */
    public abstract String getLabel(int signalNumber);

    public abstract String getTransducer(int signalNumber);

    public abstract String getPrefiltering(int signalNumber);

    public abstract int getDigitalMin(int signalNumber);

    public abstract int getDigitalMax(int signalNumber);

    public abstract double getPhysicalMin(int signalNumber);

    public abstract double getPhysicalMax(int signalNumber);

    public abstract String getPhysicalDimension(int signalNumber);

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
    public abstract int getNumberOfSamplesInEachDataRecord(int signalNumber);


    /**
     * Get the frequency of the samples belonging to the signal.
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return frequency of the samples (number of samples per second) belonging to the signal with the given number
     */
    public double getSampleFrequency(int signalNumber) {
        return getNumberOfSamplesInEachDataRecord(signalNumber) / getDurationOfDataRecord();
    }


    /**
     * Helper method. Calculate total number of samples from all channels (signals) in each data record
     *
     * @return sum of samples from all channels
     */
    public int getDataRecordLength() {
        int totalNumberOfSamplesInRecord = 0;
        for (int i = 0; i < getNumberOfSignals(); i++) {
            totalNumberOfSamplesInRecord += getNumberOfSamplesInEachDataRecord(i);
        }
        return totalNumberOfSamplesInRecord;
    }

    /**
     * Calculate the gain calibration (adjust) factor of the signal on the base
     * of its physical and digital maximums and minimums
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return gain = (physMax - physMin) / (digMax - digMin)
     */
    public double gain(int signalNumber) {
        return (getPhysicalMax(signalNumber) - getPhysicalMin(signalNumber)) / (getDigitalMax(signalNumber) - getDigitalMin(signalNumber));

    }

    /**
     * Calculate the offset calibration (adjust) factor of the signal on the base
     * of its physical and digital maximums and minimums
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return offset = (physMin - digMin * gain) where
     * gain = (physMax - physMin) / (digMax - digMin)
     */
    public double offset(int signalNumber) {
        return getPhysicalMin(signalNumber) - getDigitalMin(signalNumber) * gain(signalNumber);

    }

    /**
     * Convert physical value of the signal to digital one on the base
     * of its physical and digital maximums and minimums (gain and offset)
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return digital value
     */
    public int physicalValueToDigital(int signalNumber, double physValue) {
        Long value =  Math.round((physValue - offset(signalNumber)) / gain(signalNumber));
        return value.intValue();

    }

    /**
     * Convert digital value of the signal to physical one  on the base
     * of its physical and digital maximums and minimums (gain and offset)
     *
     * @param signalNumber number of the signal(channel). Numeration starts from 0
     * @return physical value
     */
    public  double digitalValueToPhysical(int signalNumber, int digValue) {
        return digValue * gain(signalNumber) + offset(signalNumber);

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
     * Helper method. Converts the given physical DataRecord to digital DataRecord
     * @param physRecord array with physical values from all signals (physical DataRecord)
     * @param digRecord array where resultant digital values will be stored
     */
    public void physicalDataRecordToDigital(double[] physRecord, int[] digRecord) {
        int sampleCounter = 0;
        for(int signalNumber = 0; signalNumber < getNumberOfSignals(); signalNumber++) {
            int numberOfSamples = getNumberOfSamplesInEachDataRecord(signalNumber);
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
            int numberOfSamples = getNumberOfSamplesInEachDataRecord(signalNumber);
            for(int i = 0; i < numberOfSamples; i++) {
                physRecord[sampleCounter] = digitalValueToPhysical(signalNumber, digRecord[sampleCounter]);
                sampleCounter++;
            }
        }
    }



}
