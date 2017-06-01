package com.biorecorder.edflib.filters;

import com.biorecorder.edflib.base.DefaultRecordingInfo;
import com.biorecorder.edflib.base.RecordingInfo;
import com.biorecorder.edflib.base.EdfWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Permit to omit samples from some channels (delete signals)
 */
public class EdfSignalsRemover extends EdfFilter {
    private List<Integer> signalsToRemove = new ArrayList<Integer>();

    public EdfSignalsRemover(EdfWriter out) {
        super(out);
    }

    /**
     * Indicate that the samples from the given signal should be omitted in resultant DataRecords
     *
     * @param signalNumber number of the signal
     *                     whose samples should be omitted. Numbering starts from 0.
     */
    public void removeSignal(int signalNumber) {
        signalsToRemove.add(signalNumber);
    }

    protected RecordingInfo createOutputConfig() {
        DefaultRecordingInfo outConfig = new DefaultRecordingInfo(recordingInfo);
        for (int signalNamber = recordingInfo.getNumberOfSignals() - 1; signalNamber >=0 ; signalNamber--) {
            if(signalsToRemove.contains(Integer.valueOf(signalNamber))) {
                outConfig.removeSignal(signalNamber);
            }
        }
        return outConfig;
    }

    /**
     * Omits data from the "deleted" channels and
     * create resultant array of samples
     *
     * @param digitalSamples input array of digital samples
     * @return resultant array of digital samples
     */
    private int[] createResultantSamples(int[] digitalSamples) {
       List<Integer> resultantSamples = new ArrayList<Integer>();
       for (int sample : digitalSamples) {
           int signalNumber = recordingInfo.sampleNumberToSignalNumber(sampleCounter + 1);
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
    public void writeDigitalSamples(int[] digitalSamples) {
        out.writeDigitalSamples(createResultantSamples(digitalSamples));
    }
}
