package com.biorecorder.edflib;

import java.util.ArrayList;
import com.biorecorder.edflib.util.PhysicalDigitalConverter;

/**
 *  Class (data-structure) that allows to store information required to create EDF/BDF file header
 *  and correctly extract data from DataRecords.
 *
 * <p><b>DataRecord</b> is a flat array of values. It has a specified length and actually
 * represent a two-dimensional data structure of samples coming from multiple meter channels
 * during a specified period of time (usually 1 sec).
 *
 * <p>A DataRecord consists of the data samples of each channel
 * where all data samples of channel 1 consecutive be saved. Then comes samples of channel 2 ...
 * until all channels are stored. So every DataRecord has the following structure:
 *
 * <p>[ n_1 samples from channel_1, n_2 samples from channel_2, ..., n_i samples from channel_i ]
 *
 *<p> Real physical data samples coming from measuring channels are generally floating point data,
 * but they can be scaled to fit into  2byte integer (EDF format) or 3byte integer (BDF format).
 * A linear relationship is assumed between physical (floating point) and digital (integer) values.
 * For every channel (signal) <b> physical minimum and maximum </b>
 * and the corresponding <b> digital minimum and maximum </b> are determined
 * that permit to calculate the scaling factors and to convert physical values t
 * o digital and vice versa:
 *
 * <p>(physValue - physMin) / (digValue - digMin)  = constant [Gain] = (physMax - physMin) / (digMax - digMin)
 *
 * <p>Thus every physical DataRecord (array of doubles) can be converted
 * to digital DataRecord (array of integers) and backwards.

 *  <p>RecordingConfig contains all necessary information about DataRecords structure such as:
 *  duration of DataRecords (usually 1 sec), number of channels or signals,
 *  configuration of every signal... and other
 *  significant data  about the experiment (patient info, startTime and so on) and has
 *  getter and setter methods to set and get that information.
 *
 *  <p>Configuration of every channel is a special  object -
 *  {@link SignalConfig}, containing all important information about meter channel
 *  (number of sample coming from that channel, its digital and physical minimum and maximum
 *  physical dimension (uV or Ohm) and so on).
 *
 * <p>On the base of this information EDF/BDF file header are created when data
 * are saved to EDF/BDF file.
 *
 * <p>More detailed information about EDF/BDF format:
 * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
 * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <br><a href="http://www.edfplus.info/specs/edffloat.html">EDF. How to store longintegers and floats</a>
 *
 * @see SignalConfig
 * @see PhysicalDigitalConverter
 */

public class RecordingConfig {
    private String patientId = "Default patient";
    private String recordingId = "Default record";
    private long startTime = -1;
    private int numberOfDataRecords = -1;
    private double durationOfDataRecord;
    private ArrayList<SignalConfig> signals = new ArrayList<SignalConfig>();

    public RecordingConfig() {

    }

    /**
     * Constructor to make a copy of given RecordingConfig instance
     *
     * @param recordingConfig RecordingConfig instance that will be copied
     */
    public RecordingConfig(RecordingConfig recordingConfig) {
        patientId = recordingConfig.getPatientId();
        recordingId = recordingConfig.getRecordingId();
        startTime = recordingConfig.getStartTime();
        durationOfDataRecord = recordingConfig.getDurationOfDataRecord();
        numberOfDataRecords = recordingConfig.getNumberOfDataRecords();
        for(int i = 0; i < recordingConfig.getNumberOfSignals(); i++) {
            signals.add(new SignalConfig(recordingConfig.getSignalConfig(i)));
        }
    }


    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = recordingId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
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

    /**
     *
     * RecordingConfig must include SignalConfigs describing all measuring channels (signals).
     * And we have to add them successively.
     *
     * @param signalConfig  SignalConfig instance describing parameters of the measuring channel
     *
     */
    public void addSignalConfig(SignalConfig signalConfig) {
        signals.add(signalConfig);
    }

    public void removeAllSignalConfig() {
        signals = new ArrayList<SignalConfig>();
    }



    public SignalConfig getSignalConfig(int number) {
        return signals.get(number);
    }


    public int getNumberOfSignals() {
        return signals.size();
    }


    /**
     *  calculate total number of samples from all channels (signals) in each data record
     *
     *  @return  sum of samples from all channels
     */
    public int getRecordLength() {
        int totalNumberOfSamplesInRecord = 0;
        for(int i = 0; i < signals.size(); i++) {
            totalNumberOfSamplesInRecord += signals.get(i).getNumberOfSamplesInEachDataRecord();
        }
        return totalNumberOfSamplesInRecord;
    }

    /**
     * calculate number of bytes in EDF/BDF header that will be created on the base of this RecordingConfig
     *
     * @return number of bytes in EDF/BDF header = (number of signals + 1) * 256
     */
    public int getNumberOfBytesInHeader() {
        return 256 + (getNumberOfSignals() * 256);
    }
}
