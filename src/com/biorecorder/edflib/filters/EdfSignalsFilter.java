package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DefaultRecordingInfo;
import com.biorecorder.edflib.base.RecordingInfo;
import com.biorecorder.edflib.base.EdfWriter;
import com.biorecorder.edflib.filters.signalfilters.MovingAverageFilter;
import com.biorecorder.edflib.filters.signalfilters.SignalFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Permits to  realize some kind of
 * transformation (digital filtering) with the data samples belonging to some signals
 * (add digital filters to some signals).
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
     * belonging to the given signal in DataRecords
     *
     * @param signalFilter digital filter that will be applied to the samples
     * @param signalNumber number of the channel (signal) to whose samples
     *                     the filter should be applied to. Numbering starts from 0.
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
    protected RecordingInfo createOutputConfig() {
        DefaultRecordingInfo outConfig = new DefaultRecordingInfo(config);
        for (int signalNumber = 0; signalNumber < config.getNumberOfSignals(); signalNumber++) {
            List<SignalFilter> signalFilters = filters.get(signalNumber);
            if (signalFilters != null) {
                String prefiltering = outConfig.getPrefiltering(signalNumber);
                StringBuilder prefilteringBuilder = new StringBuilder();
                if(!prefiltering.equalsIgnoreCase("none")) {
                    prefilteringBuilder.append(prefiltering);
                }
                for (SignalFilter filter : signalFilters) {
                    if(prefilteringBuilder.length() > 0) {
                        prefilteringBuilder.append(" ");
                    }
                    prefilteringBuilder.append(filter.getName());
                }
                outConfig.setPrefiltering(signalNumber, prefilteringBuilder.toString());
            }
        }
        return outConfig;
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
            int signalNumber = config.sampleNumberToSignalNumber(sampleCounter + 1);
            List<SignalFilter> signalFilters = filters.get(signalNumber);
            if(signalFilters != null) {
                for (SignalFilter filter : signalFilters) {
                    Long filteredVal = Math.round(filter.getFilteredValue(digitalSamples[i]));
                    digitalSamples[i] = filteredVal.intValue();
                }
            }
            sampleCounter++;
        }

        return digitalSamples;
    }


    @Override
    public void writeDigitalSamples(int[] digitalSamples) {
        out.writeDigitalSamples(createResultantSamples(digitalSamples));
    }
}
