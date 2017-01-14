package test;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.AggregateFilter;
import com.biorecorder.edflib.filters.SignalsRemoval;
import com.biorecorder.edflib.filters.signal_filters.SignalAveragingFilter;

import java.io.File;


/**
 * Created by gala on 01/01/17.
 */
public class Test {
    public static void main(String[] args) {
        File file = new File(System.getProperty("user.dir")+"/records", "30-12-2016_12-17.bdf");
        try {



            EdfBdfReader reader = new EdfBdfReader(file);
            BdfWriter writer = new BdfWriter(new File(System.getProperty("user.dir")+"/records", "copy.bdf"));
            RecordConfig recordConfig = reader.getRecordConfig();

            AggregateFilter filteredWriter = new AggregateFilter(writer);
            filteredWriter.addSignalFilter(0, new SignalAveragingFilter(10));
            filteredWriter.open(recordConfig);
            System.out.println(recordConfig.getNumberOfDataRecords()+"  records number "+reader.availableDataRecords());
            int numberOfRecords = 0;
            long startTime = System.currentTimeMillis();
            for(int i = 0; i < recordConfig.getNumberOfDataRecords(); i++) {
               // filteredWriter.writeDigitalDataRecord(reader.readDataRecord());
                numberOfRecords++;
            }
            long endTime = System.currentTimeMillis();
            System.out.println(endTime-startTime+"ms,  records number "+numberOfRecords);
            writer.close();
            reader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
