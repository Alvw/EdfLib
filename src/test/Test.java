package test;

import com.biorecorder.edflib.BdfWriter;
import com.biorecorder.edflib.EdfBdfReader;
import com.biorecorder.edflib.HeaderConfig;
import com.biorecorder.edflib.filters.AggregateFilter;
import com.biorecorder.edflib.filters.signal_filters.SignalAveragingFilter;

import java.io.File;


/**
 * Created by gala on 01/01/17.
 */
public class Test {
    public static void main(String[] args) {
        File file = new File(System.getProperty("user.dir")+"/records", "01-01-2017_19-51.bdf");
        try {

            EdfBdfReader reader = new EdfBdfReader(file);
            BdfWriter writer = new BdfWriter(new File(System.getProperty("user.dir")+"/records", "copy.bdf"));
            writer.setBuffered(true);
            HeaderConfig headerConfig = reader.getHeaderConfig();

            AggregateFilter filteredWriter = new AggregateFilter(writer);
            filteredWriter.addSignalFilter(0, new SignalAveragingFilter(10));
            filteredWriter.setHeaderConfig(headerConfig);
            int numberOfRecords = 0;
            long startTime = System.currentTimeMillis();
            for(int i = 0; i < headerConfig.getNumberOfDataRecords(); i++) {
                filteredWriter.writeDigitalDataRecord(reader.readDataRecord());
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
