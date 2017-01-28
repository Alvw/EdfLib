package example;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.DataRecordsJoiner;
import com.biorecorder.edflib.filters.DataRecordsSignalsManager;
import com.biorecorder.edflib.filters.SignalMovingAverageFilter;

import java.io.*;

/**
 * This example program shows how to work with this EdfLib.
 * It reads data from the EDF file edfdata.edf and copy them to
 * the file BdfFileCopy.bdf
 */
public class EdfExample {
    public static void main(String[] args) {
        File originalFile = new File(System.getProperty("user.dir"),"edfdata.edf");
        File copyFile = new File(System.getProperty("user.dir"), "edfcopy.bdf");

        try {
            EdfReader reader = new EdfReader(originalFile);
            HeaderConfig headerConfig = reader.getHeaderInfo();

            System.out.println("Number of signals = "+headerConfig.getNumberOfSignals());
            System.out.println("Duration of DataRecords = "+headerConfig.getDurationOfDataRecord());
            for(int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
                System.out.println(i+ " "+ headerConfig.getSignalConfig(i).getLabel() +
                        ", number of samples in data records = "+ headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord());
            }


            reader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }




    public static void edfReaderWriterTest_2() {
        File originalFile = new File(System.getProperty("user.dir") + "/records", "01-01-2017_19-51.bdf");
        File copyFile = new File(System.getProperty("user.dir") + "/records", "copy.bdf");
        try {
            EdfReader reader = new EdfReader(originalFile);
            HeaderConfig headerConfig = reader.getHeaderInfo();

            EdfWriter bdfWriter = new EdfWriter(copyFile, FileType.BDF_24BIT);

            System.out.println("Samples coping. Physical-digital conversion");
            bdfWriter.open(headerConfig);
            System.out.println("channel 0 =" + headerConfig.getSignalConfig(0).getNumberOfSamplesInEachDataRecord());
            System.out.println("channel 1 =" + headerConfig.getSignalConfig(1).getNumberOfSamplesInEachDataRecord());

            while (reader.availableSignalSamples(0) < 0) {

                bdfWriter.writePhysicalSamples(reader.readPhysicalSamples(1, 13));
            }

            System.out.println(bdfWriter.getWritingInfo());

            bdfWriter.close();
            reader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void edfReaderWriterTest_1() {
        File originalFile = new File(System.getProperty("user.dir") + "/records", "30-12-2016_12-17.bdf");
       // File originalFile = new File(System.getProperty("user.dir") + "/records", "01-01-2017_19-51.bdf");
        File copyFile = new File(System.getProperty("user.dir") + "/records", "copy.bdf");
        try {
            EdfReader reader = new EdfReader(originalFile);
            HeaderConfig headerConfig = reader.getHeaderInfo();

            EdfWriter bdfWriter = new EdfWriter(copyFile, FileType.BDF_24BIT);

            DataRecordsJoiner joiner = new DataRecordsJoiner(10, bdfWriter);

            DataRecordsSignalsManager filteredWriter = new DataRecordsSignalsManager(joiner);
            filteredWriter.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));

            System.out.println("filtered DataRecords copy + physical_digital conversion");
            filteredWriter.open(headerConfig);
            reader.setDataRecordPosition(50);
            while (reader.availableDataRecords() > 0) {
                filteredWriter.writePhysicalDataRecord(reader.readPhysicalDataRecord());
            }

            System.out.println(bdfWriter.getWritingInfo());

            bdfWriter.close();
            reader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
