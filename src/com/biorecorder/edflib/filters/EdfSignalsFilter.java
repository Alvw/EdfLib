package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.EdfWriter;
import com.biorecorder.edflib.HeaderInfo;
import com.biorecorder.edflib.filters.digital_filters.MovingAverageFilter;
import com.biorecorder.edflib.filters.digital_filters.SignalFilter;

import java.io.IOException;
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
    protected HeaderInfo createOutputRecordingConfig() {
        HeaderInfo outHeaderInfo = new HeaderInfo(headerInfo);
        for (int signalNumber = 0; signalNumber < headerInfo.getNumberOfSignals(); signalNumber++) {
            List<SignalFilter> signalFilters = filters.get(signalNumber);
            if (signalFilters != null) {
                String prefiltering = outHeaderInfo.getPrefiltering(signalNumber);
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
                outHeaderInfo.setPrefiltering(signalNumber, prefilteringBuilder.toString());
            }
        }
        return outHeaderInfo;
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
            int signalNumber = headerInfo.sampleNumberToSignalNumber(sampleCounter + 1);
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
