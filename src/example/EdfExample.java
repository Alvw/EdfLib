package example;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.EdfJoiner;
import com.biorecorder.edflib.filters.EdfSignalsManager;
import com.biorecorder.edflib.filters.SignalMovingAverageFilter;
import com.biorecorder.edflib.HeaderConfig;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This example program opens the EDF-file records/ekg.edf
 * (that contains data from two measuring channels - cardiogram and accelerometer) and
 * <ul>
 * <li>reads its records (one by one) and writes them to the new file ekgcopy1.edf as it is</li>
 * <li>reads data by samples (from both channels) and writes them to the new file ekgcopy2.edf as it is</li>
 * <li>does some filtering
 * (that joins 10 data records first and then omits data from the first channel and averages samples
 * from the second channel reducing the 50Hz noise)
 * and writes the resultant records to ekgcopy3.edf</li>
 * </ul>
 */
public class EdfExample {
    public static void main(String[] args) {
        File recordsDir = new File(System.getProperty("user.dir"), "records");
        File fileToRead = new File(recordsDir, "ekg.edf");
        try {
            EdfFileReader edfFileReader = new EdfFileReader(fileToRead);
            HeaderConfig headerConfig = edfFileReader.getHeaderInfo();
            // Print some header info from original file
            System.out.println("\nHeader info of the original Edf-file:");
            printHeaderInfo(headerConfig);

            // read DataRecords one by one and write them to the new file ekgcopy1.edf as it is
            File fileToWrite1 = new File(recordsDir, "ekgcopy1.edf");
            EdfFileWriter fileWriter1 = new EdfFileWriter(fileToWrite1);
            fileWriter1.open(headerConfig);
            while (edfFileReader.availableDataRecords() > 0) {
                fileWriter1.writeDigitalSamples(edfFileReader.readDigitalDataRecord());
            }
            fileWriter1.close();

            // read data by samples (from both channels) and write them to the new file ekgcopy2.edf as it is
            File fileToWrite2 = new File(recordsDir, "ekgcopy2.edf");
            EdfFileWriter fileWriter2 = new EdfFileWriter(fileToWrite2);
            fileWriter2.open(headerConfig);
            while (edfFileReader.availableSamples(0) > 0) {
                fileWriter2.writePhysicalSamples(edfFileReader.readPhysicalSamples(0, headerConfig.getNumberOfSamplesInEachDataRecord(0)));
                fileWriter2.writePhysicalSamples(edfFileReader.readPhysicalSamples(1, headerConfig.getNumberOfSamplesInEachDataRecord(1)));
            }
            fileWriter2.close();

            /*
             *  do some filtering
             * (that joins 10 data records first and then omits data from the first channel and averages samples
             * from the second channel reducing the 50Hz noise)
             * and write the resultant records to ekgcopy3.edf
             */
            File fileToWrite3 = new File(recordsDir, "ekgcopy3.edf");
            EdfFileWriter fileWriter3 = new EdfFileWriter(fileToWrite3);
            EdfJoiner joiner = new EdfJoiner(10, fileWriter3);

         /*   EdfSignalsManager signalsManager = new EdfSignalsManager(joiner);
            signalsManager.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));
            signalsManager.removeSignal(1);
            signalsManager.open(headerConfig);*/

            joiner.open(headerConfig);
            edfFileReader.setDataRecordPosition(0);
            while (edfFileReader.availableDataRecords() > 0) {
                joiner.writeDigitalSamples(edfFileReader.readDigitalDataRecord());
            }
            fileWriter3.close();
            edfFileReader.close();

            // Print some header info from resultant file after filtering
            System.out.println("\nHeader info of the resultant filtered Edf-file:");
            printHeaderInfo(fileWriter3.getHeaderInfo());


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void printHeaderInfo(HeaderConfig headerConfig) {
        System.out.println("file type " + headerConfig.getFileType());
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String timeStamp = dateFormat.format(new Date(headerConfig.getRecordingStartTime()));
        System.out.println("Start time "+timeStamp);
        System.out.println("Duration of DataRecords = " + headerConfig.getDurationOfDataRecord());
        System.out.println("Number of signals = " + headerConfig.getNumberOfSignals());
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            System.out.println(i + ": label = " + headerConfig.getLabel(i)
                    + "; number of samples in data records = " + headerConfig.getNumberOfSamplesInEachDataRecord(i)
                    + "; prefiltering = " + headerConfig.getPrefiltering(i));
        }

    }
}