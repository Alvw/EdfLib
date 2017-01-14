package com.biorecorder.edflib;

import com.biorecorder.edflib.util.PhysicalDigitalConverter;

import java.io.IOException;

/**
 * This abstract class is the superclass of all classes representing an output stream of DataRecords.
 * An output stream accepts output DataRecord and send it to some sink.
 * Applications that need to define a subclass of OutputStream must always provide at
 * least a method that writes one digital DataRecord of output - writeDigitalDataRecord.
 *
 * <p>DataRecord is a flat array of values. It has a specified length and actually represent a
 * two-dimensional data structure of samples coming from multiple meter channels
 * during a specified period of time (usually 1 sec).
 *
 * <p>A DataRecord consists of the data samples of each channel
 * where all data samples of channel 1 consecutive be saved. Then comes samples of channel 2 ...
 * until all channels are stored. So every DataRecord has the following structure:
 *
 * <p>[ n_1 samples from channel_1, n_2 samples from channel_2, ..., n_i samples from channel_i ]
 *
 *<p> Real physical data samples are generally floating point data, but they can be scaled to
 * fit into  2byte integer (EDF format) or 3byte integer (BDF format). A linear relationship is assumed
 * between physical (floating point) and digital (integer) values.
 * For every channel (signal) <b> physical minimum and maximum </b>
 * and the corresponding <b> digital minimum and maximum </b> are determined
 * that permit to calculate the scaling factors and to convert physical values to digital and vice versa:
 *
 * <p>(physValue - physMin) / (digValue - digMin)  = constant [Gain] = (physMax - physMin) / (digMax - digMin)
 *
 * <p>Thus every physical DataRecord (array of doubles) can be converted
 * to digital DataRecord (array of integers) and backwards.
 *
 * <p>The information about DataRecords structure such as:
 * number of data channels (or signals), duration of DataRecord (usually 1 sec),
 * numbers of samples for every channel,
 * digital and physical minimum and maximum for every channel and so on are stored
 * in the special object {@link RecordConfig}.
 * On the base of this information EDF/BDF file header will be created when  finally data
 * will be saved to EDF/BDF file.
 *
 * <p>More detailed information about EDF/BDF format:
 * <br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
 * <br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
 * <br><a href="http://www.edfplus.info/specs/edffloat.html">EDF. How to store longintegers and floats</a>
 *
 * @see DataRecordsFileWriter
 * @see com.biorecorder.edflib.filters.DataRecordsFilter
 *
 *
 */
public abstract class DataRecordsWriter {
    protected RecordConfig recordConfig;
    protected PhysicalDigitalConverter physicalDigitalConverter;

    /**
     * Prepare this DataRecordWriter for correct work.
     * This function MUST be called before writing any data.
     * After opening the configuration of the writer SHOULD NOT be changed.
     *
     * @param recordConfig object with the information about DataRecords structure
     *
     * @throws IOException
     */
    public void open(RecordConfig recordConfig) throws IOException {
        this.recordConfig = new RecordConfig(recordConfig);
        physicalDigitalConverter = new PhysicalDigitalConverter(recordConfig);
    }


    /**
     * Write ONE digital DataRecord.
     * Take data from digitalData array starting at offset position.
     *
     * @param digitalData array with digital data
     * @param offset offset within the array at which the DataRecord starts
     *
     * @throws IOException
     */
    public abstract void writeDigitalDataRecord(int[] digitalData, int offset) throws IOException;


    /**
     * Convert ONE physical DataRecord to digital and write it.
     * Take data from physData array starting at offset position.
     *
     * @param physData array with physical data
     * @param offset offset within the array at which the DataRecord starts
     *
     * @throws IOException
     */
    public void writePhysicalDataRecord(double[] physData, int offset) throws IOException  {
        int[] digData = new int[recordConfig.getRecordLength()];
        physicalDigitalConverter.physicalRecordToDigital(physData, offset, digData, 0);
        writeDigitalDataRecord(digData);
    }

    /**
     * Write given digital DataRecord.
     * This method do exactly the same as the call writeDigitalDataRecord(digitalDataRecord, 0)
     *
     * @param digitalDataRecord digital DataRecord to be written to the stream
     *
     * @throws IOException
     */
    public void writeDigitalDataRecord(int[] digitalDataRecord) throws IOException {
        writeDigitalDataRecord(digitalDataRecord, 0);
    }

    /**
     * Convert given physical DataRecord to digital and write it.
     * This method do exactly the same as the call writePhysicalDataRecord(physDataRecord, 0)
     *
     * @param physDataRecord physical DataRecord to be converted to digital and written to the stream
     *
     * @throws IOException
     */
    public void writePhysicalDataRecord(double[] physDataRecord) throws IOException  {
       writePhysicalDataRecord(physDataRecord, 0);
    }

    /**
     * Close this DataRecordsWriter and releases resources.
     * This method MUST be called after finishing writing data.
     * Failing to do so will cause unnessesary memory usage and corrupted and incomplete data writing.
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;
}
