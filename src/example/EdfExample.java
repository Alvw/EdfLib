package example;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.EdfJoiner;
import com.biorecorder.edflib.filters.EdfSignalsFilter;
import com.biorecorder.edflib.filters.EdfSignalsRemover;
import com.biorecorder.edflib.HeaderConfig;
import com.biorecorder.edflib.filters.digital_filters.MovingAverageFilter;

import java.io.*;
import java.util.Arrays;

/**
 * This example program opens the EDF-file records/ekg.edf
 * (that contains data from two measuring channels - cardiogram and accelerometer) and
 * copy its data to new files as it is or with some transformations
 */
public class EdfExample {
    public static void main(String[] args) {
        File recordsDir = new File(System.getProperty("user.dir"), "records");
        File originalFile = new File(recordsDir, "ekg.edf");
        try {
            EdfFileReader originalFileReader = new EdfFileReader(originalFile);
            HeaderConfig headerConfig = originalFileReader.getHeaderInfo();
            // Print some header info from original file
            System.out.println("Header info of the original Edf-file:");
            System.out.println(headerConfig.headerToString());

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
                    throw new RuntimeException("Test1: original and resultant files are not equals. Record: "+i);
                }
            }

            resultantFileReader.close();
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
                    throw new RuntimeException("Test2: original and resultant files are not equals. Record: "+i);
                }
            }

            resultantFileReader.close();
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
                fileWriter3.writePhysicalSamples(originalFileReader.readPhysicalSamples(0, headerConfig.getNumberOfSamplesInEachDataRecord(0)));
                fileWriter3.writePhysicalSamples(originalFileReader.readPhysicalSamples(1, headerConfig.getNumberOfSamplesInEachDataRecord(1)));
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
                    throw new RuntimeException("Test3: original and resultant files are not equals. Record: "+i);
                }
            }

            resultantFileReader.close();
            System.out.println("Test3 done! \n");

/*****************************************************************************************
 *     Test EdfJoiner. Read data, joins 5 data records and write the resultant records
 *     to ekgcopy4.edf
 *****************************************************************************************/

            File resultantFile4 = new File(recordsDir, "ekgcopy4.edf");
            EdfFileWriter fileWriter4 = new EdfFileWriter(resultantFile4);
            int numberOfRecordsToJoin = 5;
            EdfJoiner joiner = new EdfJoiner(numberOfRecordsToJoin, fileWriter4);

            joiner.open(headerConfig);
            originalFileReader.reset();
            while (originalFileReader.availableDataRecords() > 0) {
                joiner.writePhysicalSamples(originalFileReader.readPhysicalDataRecord());
            }
            joiner.close();

            System.out.println("Test4: copy file with joining every 5 data records");
            resultantFileReader = new EdfFileReader(resultantFile4);
            originalFileReader.reset();
            i = 0;
            int numberOfSamples = 150;
            while(resultantFileReader.availableSamples(0) > 0) {
                arr1 = originalFileReader.readDigitalSamples(0, numberOfSamples);
                arr2 = resultantFileReader.readDigitalSamples(0, numberOfSamples);
                i++;
                if(arr1.length == arr2.length && !Arrays.equals(arr1, arr2)) {
                    throw new RuntimeException("Test4: original and resultant files are not equals. Record: "+i);

                }
            }
            i = 0;
            numberOfSamples = 15;
            while(resultantFileReader.availableSamples(1) > 0) {
                arr1 = originalFileReader.readDigitalSamples(1, numberOfSamples);
                arr2 = resultantFileReader.readDigitalSamples(1, numberOfSamples);
                i++;
                if(arr1.length == arr2.length && !Arrays.equals(arr1, arr2)) {
                    throw new RuntimeException("Test4: original and resultant files are not equals. Record: "+i);
                }
            }

            resultantFileReader.close();
            System.out.println("Test4 done! \n");

            // Print some header info from resultant file
            System.out.println("Header info of the resultant joined Edf-file:");
            System.out.println(fileWriter4.getHeaderInfo().headerToString());


/*****************************************************************************************
 *     Test EdfSignalsRemover. Reads data records from original file,
 *     removes samples belonging to channel 0 and write the resultant records
 *     to ekgcopy5.edf
 *****************************************************************************************/

            File resultantFile5 = new File(recordsDir, "ekgcopy5.edf");
            EdfFileWriter fileWriter5 = new EdfFileWriter(resultantFile5);
            EdfSignalsRemover signalsRemover = new EdfSignalsRemover(fileWriter5);
            signalsRemover.removeSignal(0);
            signalsRemover.open(headerConfig);

            originalFileReader.reset();
            while (originalFileReader.availableDataRecords() > 0) {
                signalsRemover.writePhysicalSamples(originalFileReader.readPhysicalDataRecord());
            }
            signalsRemover.close();

            System.out.println("Test5: copy file with removing data from channel 0");
            resultantFileReader = new EdfFileReader(resultantFile5);
            originalFileReader.reset();
            i = 0;
            while(resultantFileReader.availableDataRecords() > 0) {
                arr1 = originalFileReader.readDigitalSamples(1, headerConfig.getNumberOfSamplesInEachDataRecord(1));
                arr2 = resultantFileReader.readDigitalDataRecord();
                i++;
                if(!Arrays.equals(arr1, arr2)) {
                    throw new RuntimeException("Test5: original and resultant files are not equals. Record: "+i);
                }
            }

            resultantFileReader.close();
            System.out.println("Test5 done! \n");

            // Print some header info from resultant file
            System.out.println("Header info of the resultant Edf-file with removed channel:");
            System.out.println(fileWriter5.getHeaderInfo().headerToString());

/*****************************************************************************************
 *     EdfSignalsFilter usage example. Read data, apply some filtering to
 *     samples from channel 0 and write the resultant records
 *     to ekgcopy6.edf
 *****************************************************************************************/
            File resultantFile6 = new File(recordsDir, "ekgcopy6.edf");
            EdfFileWriter fileWriter6 = new EdfFileWriter(resultantFile6);
            EdfSignalsFilter signalsFilter = new EdfSignalsFilter(fileWriter6);
            signalsFilter.addSignalFilter(0, new MovingAverageFilter(10));
            signalsFilter.open(headerConfig);

            originalFileReader.reset();
            while (originalFileReader.availableDataRecords() > 0) {
                signalsFilter.writeDigitalSamples(originalFileReader.readDigitalDataRecord());
            }
            signalsFilter.close();

            // Print some header info from resultant file after filtering
            System.out.println("Header info of the resultant filtered Edf-file:");
            System.out.println(fileWriter6.getHeaderInfo().headerToString());

            originalFileReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}