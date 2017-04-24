package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.EdfWriter;
import com.biorecorder.edflib.HeaderConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Permit to omit samples from some channels (delete signals)
 */
public class EdfSignalsRemover extends EdfFilter{
    private List<Integer> signalsToRemove = new ArrayList<Integer>();

    public EdfSignalsRemover(EdfWriter out) {
        super(out);
    }

    /**
     * Indicate that the samples from the given signal should be omitted in resultant DataRecord
     *
     * @param signalNumber number of channel (signal) in the original (incoming) DataRecord
     *                     which samples should be omitted
     */
    public void removeSignal(int signalNumber) {
        signalsToRemove.add(signalNumber);
    }

    protected HeaderConfig createOutputRecordingConfig() {
        HeaderConfig outHeaderConfig = new HeaderConfig(headerConfig);
        for (int signalNamber = headerConfig.getNumberOfSignals() - 1; signalNamber >=0 ; signalNamber--) {
            if(signalsToRemove.contains(Integer.valueOf(signalNamber))) {
                outHeaderConfig.removeSignal(signalNamber);
            }
        }
        return outHeaderConfig;
    }

    /**
     * Omit data from the "deleted" channels and
     * create resultant array of samples
     *
     * @param digitalSamples input array of digital samples
     * @return resultant array of digital samples
     */
    private int[] createResultantSamples(int[] digitalSamples) {
       List<Integer> resultantSamples = new ArrayList<Integer>();
       for (int sample : digitalSamples) {
           int signalNumber = headerConfig.signalNumber(sampleCounter + 1);
           if(!signalsToRemove.contains(signalNumber)) {
               resultantSamples.add(sample);
           }
           sampleCounter++;
       }
       int[] resultantArr = new int[resultantSamples.size()];
       for(int i = 0; i < resultantSamples.size(); i++) {
           resultantArr[i] = resultantSamples.get(i);
       }
       return resultantArr;

    }


    @Override
    public void writeDigitalSamples(int[] digitalSamples) throws IOException {
        out.writeDigitalSamples(createResultantSamples(digitalSamples));
    }
}
