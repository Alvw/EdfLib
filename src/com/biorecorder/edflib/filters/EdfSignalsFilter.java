package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.EdfWriter;
import com.biorecorder.edflib.HeaderConfig;
import com.biorecorder.edflib.filters.digital_filters.MovingAverageFilter;
import com.biorecorder.edflib.filters.digital_filters.SignalFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Permit to  realize some kind of
 * transformation (digital filtering) with the signal data (add some digital filters to the signal).
 *
 * @see SignalFilter
 * @see MovingAverageFilter
 */
public class EdfSignalsFilter extends EdfFilter {
    private Map<Integer, List<SignalFilter>> filters = new HashMap<Integer, List<SignalFilter>>();

    public EdfSignalsFilter(EdfWriter out) {
        super(out);
    }

    /**
     * Indicate that the given filter should be applied to the samples
     * of the given signal in DataRecords
     *
     * @param signalFilter signal filter that will be applied to the samples of given channel number
     * @param signalNumber number of channel (signal) in the input DataRecord
     *                     the filter should be applied to
     */
    public void addSignalFilter(int signalNumber, SignalFilter signalFilter) {
        List<SignalFilter> signalFilters = filters.get(signalNumber);
        if(signalFilters == null) {
            signalFilters = new ArrayList<SignalFilter>();
            filters.put(signalNumber, signalFilters);
        }
        signalFilters.add(signalFilter);
    }

    /**
     * Add filters names to «prefiltering» field of the channels
     * @return Header config with filter names
     */
    protected HeaderConfig createOutputRecordingConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig);
        for (int signalNumber = 0; signalNumber < headerConfig.getNumberOfSignals(); signalNumber++) {
            List<SignalFilter> signalFilters = filters.get(signalNumber);
            if (signalFilters != null) {
                StringBuilder prefiltering = new StringBuilder(outHeaderConfig.getPrefiltering(signalNumber));
                for (SignalFilter filter : signalFilters) {
                    prefiltering.append(" ").append(filter.getName());
                }
                outHeaderConfig.setPrefiltering(signalNumber, prefiltering.toString());
            }
        }
        return  outHeaderConfig;
    }

    /**
     * Apply filters specified for the channels and
     * create resultant output array of samples
     *
     * @param digitalSamples input array of digital samples
     * @return resultant array of digital samples
     */
    private int[] createResultantSamples(int[] digitalSamples) {
        for (int i = 0; i < digitalSamples.length; i++) {
            int signalNumber = headerConfig.signalNumber(sampleCounter + 1);
            List<SignalFilter> signalFilters = filters.get(signalNumber);
            if(signalFilters != null) {
                for (SignalFilter filter : signalFilters) {
                    digitalSamples[i] = filter.getFilteredValue(digitalSamples[i]);
                }
            }
            sampleCounter++;
        }

        return digitalSamples;
    }


    @Override
    public void writeDigitalSamples(int[] digitalSamples) throws IOException {
        out.writeDigitalSamples(createResultantSamples(digitalSamples));
    }
}
