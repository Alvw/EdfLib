package test;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.DataRecordsSignalsManager;
import com.biorecorder.edflib.filters.SignalMovingAverageFilter;

import java.io.File;


/**
 * Created by gala on 01/01/17.
 */
public class Test {
    public static void main(String[] args) {
        File file = new File(System.getProperty("user.dir")+"/records", "30-12-2016_12-17.bdf");
        try {

            BdfWriter writer = new BdfWriter(new File(System.getProperty("user.dir")+"/records", "copy.bdf"));
            System.out.println("writer = "+ writer);

            EdfBdfReader reader = new EdfBdfReader(file);
            RecordingConfig recordingConfig = reader.getRecordingConfig();
            DataRecordsSignalsManager filteredWriter = new DataRecordsSignalsManager(writer);
            filteredWriter.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));
            //filteredWriter.open(recordingConfig);

            System.out.println(recordingConfig.getNumberOfDataRecords()+"  records number "+reader.availableDataRecords());
            int numberOfRecords = 0;
            long startTime = System.currentTimeMillis();
            for(int i = 0; i < recordingConfig.getNumberOfDataRecords(); i++) {
                filteredWriter.writeDigitalDataRecord(reader.readDigitalDataRecord());
                numberOfRecords++;
            }
            long endTime = System.currentTimeMillis();
            System.out.println(endTime-startTime+"ms,  records number "+numberOfRecords);
            writer.close();
            reader.close();
            System.out.println(writer.getWritingInfo());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
