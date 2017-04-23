package example;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.EdfJoiner;
import com.biorecorder.edflib.filters.EdfSignalsManager;
import com.biorecorder.edflib.filters.SignalMovingAverageFilter;
import com.biorecorder.edflib.HeaderConfig;

import java.io.*;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
        File originalFile = new File(recordsDir, "ekg.edf");
        try {
            EdfFileReader originalFileReader = new EdfFileReader(originalFile);
            HeaderConfig headerConfig = originalFileReader.getHeaderInfo();
            // Print some header info from original file
            System.out.println("\nHeader info of the original Edf-file:");
            printHeaderInfo(headerConfig);

            //headerConfig.test(originalFileReader.readDigitalDataRecord());

         /*   int[] digRecord = originalFileReader.readDigitalDataRecord();
            int[] digRecord1 = originalFileReader.readDigitalDataRecord();
            int[] digRecord3 = originalFileReader.readDigitalDataRecord();
            originalFileReader.reset();
            double[] physRecord = originalFileReader.readPhysicalDataRecord();
            double[] physRecord1 = headerConfig.convertDig(digRecord);
             System.out.println("is arrays equal? "+ Arrays.equals(physRecord, physRecord1));
            for (int i = 0; i < physRecord.length; i++) {
              // System.out.println(i+ " "+physRecord[i]+ " "+ physRecord1[i]);
            }
            for (int i = 0; i < physRecord.length; i++) {
               // System.out.println(i+ " "+digRecord1[i]);
            }
            System.out.println("offset "+headerConfig.offset(0));*/


/*****************************************************************************************
 *    Read «DIGITAL» DataRecords one by one and write them to the new file ekgcopy1.edf as it is
 *****************************************************************************************/
            File resultantFile1 = new File(recordsDir, "ekgcopy1.edf");
            EdfFileWriter fileWriter1 = new EdfFileWriter(resultantFile1);
            fileWriter1.open(headerConfig);
            while (originalFileReader.availableDataRecords() > 0) {
                fileWriter1.writeDigitalSamples(originalFileReader.readDigitalDataRecord());
            }
            fileWriter1.close();

            System.out.println("Test1: simple copy file record by record.");
            EdfFileReader resultantFileReader = new EdfFileReader(resultantFile1);
            originalFileReader.reset();
            int[] arr1;
            int[] arr2;
            int i = 0;
            while(originalFileReader.availableDataRecords() > 0) {
                arr1 = originalFileReader.readDigitalDataRecord();
                arr2 = resultantFileReader.readDigitalDataRecord();
                i++;
                if(!Arrays.equals(arr1, arr2)) {
                    System.out.println("original and resultant files are not equals. Record: "+i);
                }
            }
            System.out.println("Test1 done! \n");


/*****************************************************************************************
 *     Read «PHYSICAL» DataRecords one by one and write them to the new file ekgcopy2.edf
 *     Test Physical-Digital converter.
 *****************************************************************************************/
            File resultantFile2 = new File(recordsDir, "ekgcopy2.edf");
            EdfFileWriter fileWriter2 = new EdfFileWriter(resultantFile2);
            fileWriter2.open(headerConfig);
            originalFileReader.reset();
            while (originalFileReader.availableDataRecords() > 0) {
                fileWriter2.writePhysicalSamples(originalFileReader.readPhysicalDataRecord());
            }
            fileWriter2.close();

            System.out.println("Test2: copy file with physical-digital conversion.");
            resultantFileReader = new EdfFileReader(resultantFile2);
            originalFileReader.reset();
            i = 0;
            while(originalFileReader.availableDataRecords() > 0) {
                arr1 = originalFileReader.readDigitalDataRecord();
                arr2 = resultantFileReader.readDigitalDataRecord();
                i++;
                if(!Arrays.equals(arr1, arr2)) {
                    System.out.println("original and resultant files are not equals. Record: "+i);
                }
            }
            System.out.println("Test2 done! \n");

/*****************************************************************************************
 *     Read data by samples (from both channels) and
 *     write them to the new file ekgcopy3.edf
 *****************************************************************************************/
            File resultantFile3 = new File(recordsDir, "ekgcopy3.edf");
            EdfFileWriter fileWriter3 = new EdfFileWriter(resultantFile3);
            fileWriter3.open(headerConfig);
            originalFileReader.reset();
            while (originalFileReader.availableSamples(0) > 0) {
                fileWriter3.writeDigitalSamples(originalFileReader.readDigitalSamples(0, headerConfig.getNumberOfSamplesInEachDataRecord(0)));
                fileWriter3.writeDigitalSamples(originalFileReader.readDigitalSamples(1, headerConfig.getNumberOfSamplesInEachDataRecord(1)));
            }
            fileWriter3.close();

            System.out.println("Test3: read data by samples (from both channels) and write them to new file");
            resultantFileReader = new EdfFileReader(resultantFile3);
            originalFileReader.reset();
            i = 0;
            while(originalFileReader.availableDataRecords() > 0) {
                arr1 = originalFileReader.readDigitalDataRecord();
                arr2 = resultantFileReader.readDigitalDataRecord();
                i++;
                if(!Arrays.equals(arr1, arr2)) {
                    System.out.println("original and resultant files are not equals "+i);
                    for(int j = 0; j < arr1.length; j++) {
                        if(arr1[j] != arr2[j]) {
                             System.out.println(j+ " "+arr1[j]+ "   " +arr2[j]);
                        }
                    }
                }
            }
            System.out.println("Test3 done! \n");


/*****************************************************************************************
 *     Test EdfJoiner. Read data, joins 10 data records and write the resultant records
 *     to ekgcopy4.edf
 *****************************************************************************************/

            /*
             *  do some filtering
             * (that joins 10 data records first and then omits data from the first channel and averages samples
             * from the second channel reducing the 50Hz noise)
             * and write the resultant records to ekgcopy3.edf
             */
            File fileToWrite4 = new File(recordsDir, "ekgcopy4.edf");
            EdfFileWriter fileWriter4 = new EdfFileWriter(fileToWrite4);
            EdfJoiner joiner = new EdfJoiner(10, fileWriter3);

         /*   EdfSignalsManager signalsManager = new EdfSignalsManager(joiner);
            signalsManager.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));
            signalsManager.removeSignal(1);
            signalsManager.open(headerConfig);*/

            joiner.open(headerConfig);
            originalFileReader.setDataRecordPosition(0);
            while (originalFileReader.availableDataRecords() > 0) {
                joiner.writeDigitalSamples(originalFileReader.readDigitalDataRecord());
            }
            fileWriter4.close();
            originalFileReader.close();

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
        System.out.println();

    }
}