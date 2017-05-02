package com.biorecorder.edflib.filters;

/**
 * The simplest realisation of HighPass filter that buffers n incoming
 * samples and returns
 * <p>
 * filteredValue_m = value_m - ( value_m + value_(m-1) + ... + value_(m-n) ) / n
 * <p>
 *  Where n =  cutOffFrequency / sampleFrequency
 */

public class HighPassFilter implements SignalFilter {
   private FifoQueue<Double> buffer;
   private int maxSize;
   private double sum;
   private int cutOffFrequency;

    /**
     * Create HighPassFilter for the samples with the given sampleFrequency, that
     * cuts frequencies less then the given cutOffFrequency.
     * @param cutOffFrequency frequencies < cutOffFrequency will be cut
     * @param sampleFrequency  frequency of filtered samples
     */
    public HighPassFilter(double cutOffFrequency, double sampleFrequency) {
        this.cutOffFrequency = (int)cutOffFrequency;
        maxSize = (int)(sampleFrequency / cutOffFrequency);
        buffer = new FifoQueue<>(maxSize);
    }

    @Override
    public double getFilteredValue(double value) {
        sum += value;
        if(buffer.size() < maxSize) {
            buffer.put(value);
            return value - (int)(sum / buffer.size());
        }
        else {
            sum -= buffer.pop();
            buffer.put(value);
            return value - (sum / maxSize);
        }
    }

    @Override
    public String getName() {
        return "HP:"+cutOffFrequency+"Hz";
    }

    /**
     * Unit Test. Usage Example.
     * <p>
     * Create and test HighPassFilter filter.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        HighPassFilter hp = new HighPassFilter(1, 3);
        System.out.println(hp.getName());
        int value = 101;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
        value = 104;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
        value = 102;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
        value = 99;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
        value = 100;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
        value = 98;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
        value = 102;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
        value = 105;
        System.out.println(value + " filtered value: "+ hp.getFilteredValue(value));
    }
}
