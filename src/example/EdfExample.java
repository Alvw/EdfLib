package example;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.DataRecordsJoiner;
import com.biorecorder.edflib.filters.DataRecordsSignalsManager;
import com.biorecorder.edflib.filters.SignalMovingAverageFilter;
import com.biorecorder.edflib.base.HeaderConfig;

import java.io.*;

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
            EdfReader edfFileReader = new EdfReader(fileToRead);
            HeaderConfig headerConfig = edfFileReader.getHeaderInfo();
            // Print some header info from original file
            System.out.println("\nHeader info of the original Edf-file:");
            System.out.println("file type " + edfFileReader.getFileType());
            printHeaderInfo(headerConfig);

            // read DataRecords one by one and write them to the new file ekgcopy1.edf as it is
            File fileToWrite1 = new File(recordsDir, "ekgcopy1.edf");
            EdfWriter fileWriter1 = new EdfWriter(fileToWrite1, FileType.EDF_16BIT);
            fileWriter1.open(headerConfig);
            while (edfFileReader.availableDataRecords() > 0) {
                fileWriter1.writeDigitalDataRecord(edfFileReader.readDigitalDataRecord());
            }
            fileWriter1.close();

            // read data by samples (from both channels) and write them to the new file ekgcopy2.edf as it is
            File fileToWrite2 = new File(recordsDir, "ekgcopy2.edf");
            EdfWriter fileWriter2 = new EdfWriter(fileToWrite2, FileType.EDF_16BIT);
            fileWriter2.open(headerConfig);
            while (edfFileReader.availableSamples(0) > 0) {
                fileWriter2.writeDigitalSamples(edfFileReader.readDigitalSamples(0, headerConfig.getSignalConfig(0).getNumberOfSamplesInEachDataRecord()));
                fileWriter2.writeDigitalSamples(edfFileReader.readDigitalSamples(1, headerConfig.getSignalConfig(1).getNumberOfSamplesInEachDataRecord()));
            }
            fileWriter2.close();

            /*
             *  do the filtering
             * (that joins 10 data records first and then omits data from the first channel and averages samples
             * from the second channel reducing the 50Hz noise)
             * and write the resultant records to ekgcopy3.edf
             */
            File fileToWrite3 = new File(recordsDir, "ekgcopy3.edf");
            EdfWriter fileWriter3 = new EdfWriter(fileToWrite3, FileType.EDF_16BIT);
            DataRecordsJoiner joiner = new DataRecordsJoiner(10, fileWriter3);

            DataRecordsSignalsManager signalsManager = new DataRecordsSignalsManager(joiner);
            signalsManager.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));
            signalsManager.removeSignal(1);
            signalsManager.open(headerConfig);

            edfFileReader.setDataRecordPosition(0);
            while (edfFileReader.availableDataRecords() > 0) {
                signalsManager.writeDigitalDataRecord(edfFileReader.readDigitalDataRecord());
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
        System.out.println("Duration of DataRecords = " + headerConfig.getDurationOfDataRecord());
        System.out.println("Number of signals = " + headerConfig.getNumberOfSignals());
        for (int i = 0; i < headerConfig.getNumberOfSignals(); i++) {
            System.out.println(i + ": label = " + headerConfig.getSignalConfig(i).getLabel()
                   + "; number of samples in data records = " + headerConfig.getSignalConfig(i).getNumberOfSamplesInEachDataRecord()
                   + "; prefiltering = "+headerConfig.getSignalConfig(0).getPrefiltering());
        }

    }
}
