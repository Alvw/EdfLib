package example;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.EdfJoiner;
import com.biorecorder.edflib.EdfSignalsFilter;
import com.biorecorder.edflib.EdfSignalsRemover;
import com.biorecorder.edflib.HeaderInfo;
import com.biorecorder.edflib.filters.HighPassFilter;
import com.biorecorder.edflib.filters.MovingAverageFilter;

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
            HeaderInfo headerInfo = originalFileReader.getHeader();
            // Print some header info from original file
            System.out.println(headerInfo);

/*****************************************************************************************
 *    Read «DIGITAL» DataRecords one by one and write them to the new file ekgcopy1.edf as it is
 *****************************************************************************************/
            File resultantFile1 = new File(recordsDir, "ekgcopy1.edf");
            EdfFileWriter fileWriter1 = new EdfFileWriter(resultantFile1, headerInfo);

            int originalDataRecordLength = headerInfo.getDataRecordLength();
            int[] intBuffer1 = new int[originalDataRecordLength];
            while (originalFileReader.availableDataRecords() > 0) {
                // read digital DataRecord from the original file
                originalFileReader.readDigitalDataRecord(intBuffer1);
                // write digital DataRecord to the new file
                fileWriter1.writeDigitalSamples(intBuffer1);
            }
            fileWriter1.close();

            System.out.println("Test1: simple copy file record by record.");
            EdfFileReader resultantFileReader = new EdfFileReader(resultantFile1);

            // set DataRecord and signals positions to 0;
            originalFileReader.reset();
            int[] intBuffer2 = new int[originalDataRecordLength];
            int i = 0;
            while(originalFileReader.availableDataRecords() > 0) {
                // read digital DataRecord from the original file
                originalFileReader.readDigitalDataRecord(intBuffer1);
                // read digital DataRecord from the resultant copy file
                resultantFileReader.readDigitalDataRecord(intBuffer2);
                i++;
                if(!Arrays.equals(intBuffer1, intBuffer2)) {
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
            EdfFileWriter fileWriter2 = new EdfFileWriter(resultantFile2, headerInfo);

            // set DataRecord and signals positions to 0;
            originalFileReader.reset();
            double[] doubleBuffer1 = new double[originalDataRecordLength];
            while (originalFileReader.availableDataRecords() > 0) {
                // read physical DataRecord from the original file
                originalFileReader.readPhysicalDataRecord(doubleBuffer1);
                // write physical DataRecord to the new file
                fileWriter2.writePhysicalSamples(doubleBuffer1);
            }
            fileWriter2.close();

            System.out.println("Test2: copy file with physical-digital conversion.");
            resultantFileReader = new EdfFileReader(resultantFile2);
            // set DataRecord and signals positions to 0;
            originalFileReader.reset();
            i = 0;
            while(originalFileReader.availableDataRecords() > 0) {
                // read digital DataRecord from the original file
                originalFileReader.readDigitalDataRecord(intBuffer1);
                // read digital DataRecord from the resultant copy file
                resultantFileReader.readDigitalDataRecord(intBuffer2);
                i++;
                if(!Arrays.equals(intBuffer1, intBuffer2)) {
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
            EdfFileWriter fileWriter3 = new EdfFileWriter(resultantFile3, headerInfo);
            // set DataRecord and signals positions to 0;
            originalFileReader.reset();
            doubleBuffer1 = new double[headerInfo.getNumberOfSamplesInEachDataRecord(0)];
            double[] doubleBuffer2 = new double[headerInfo.getNumberOfSamplesInEachDataRecord(1)];

            while (originalFileReader.availableSamples(0) > 0) {
                // read physical samples belonging to signal 0 from the original file
                originalFileReader.readPhysicalSamples(0, doubleBuffer1);
                // read physical samples belonging to signal 1 from the original file
                originalFileReader.readPhysicalSamples(1, doubleBuffer2);
                // write physical samples to the new file
                fileWriter3.writePhysicalSamples(doubleBuffer1);
                fileWriter3.writePhysicalSamples(doubleBuffer2);

            }
            fileWriter3.close();

            System.out.println("Test3: read data by samples (from both channels) and write them to new file");
            resultantFileReader = new EdfFileReader(resultantFile3);
            originalFileReader.reset();
            i = 0;
            intBuffer1 = new int[originalDataRecordLength];
            intBuffer2 = new int[originalDataRecordLength];
            while(originalFileReader.availableDataRecords() > 0) {
                // read digital DataRecord from the original file
                originalFileReader.readDigitalDataRecord(intBuffer1);
                // read digital DataRecord from the resultant copy file
                resultantFileReader.readDigitalDataRecord(intBuffer2);
                i++;
                if(!Arrays.equals(intBuffer1, intBuffer2)) {
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
            joiner.setHeader(headerInfo);
            // set DataRecord and signals positions to 0;
            originalFileReader.reset();
            doubleBuffer1 = new double[originalDataRecordLength];
            while (originalFileReader.availableDataRecords() > 0) {
                // read physical DataRecord from the original file
                originalFileReader.readPhysicalDataRecord(doubleBuffer1);
                // write DataRecord to the EdfJoiner. It joined it and write the resultant
                // records to the new file
                joiner.writePhysicalSamples(doubleBuffer1);
            }
            joiner.close();

            System.out.println("Test4: copy file with joining every 5 data records");
            resultantFileReader = new EdfFileReader(resultantFile4);
            originalFileReader.reset();
            i = 0;
            int numberOfSamples = 150;
            intBuffer1 = new int[numberOfSamples];
            intBuffer2 = new int[numberOfSamples];
            while(resultantFileReader.availableSamples(0) > 0) {
                // read samples belonging to signal 0 from the original file
                int numberOfReadSamples1 = originalFileReader.readDigitalSamples(0, intBuffer1);
                // read samples belonging to signal 0 from the resultant joined file
                int numberOfReadSamples2 = resultantFileReader.readDigitalSamples(0, intBuffer2);
                i++;
                if(numberOfReadSamples1 == numberOfReadSamples2 && !Arrays.equals(intBuffer1, intBuffer2)) {
                    throw new RuntimeException("Test4: original and resultant files are not equals. Record: "+i);

                }
            }
            i = 0;
            numberOfSamples = 15;
            intBuffer1 = new int[numberOfSamples];
            intBuffer2 = new int[numberOfSamples];
            while(resultantFileReader.availableSamples(0) > 0) {
                // read samples belonging to signal 1 from the original file
                int numberOfReadSamples1 = originalFileReader.readDigitalSamples(1, intBuffer1);
                // read samples belonging to signal 1 from the resultant joined file
                int numberOfReadSamples2 = resultantFileReader.readDigitalSamples(1, intBuffer2);
                i++;
                if(numberOfReadSamples1 == numberOfReadSamples2 && !Arrays.equals(intBuffer1, intBuffer2)) {
                    throw new RuntimeException("Test4: original and resultant files are not equals. Record: "+i);

                }
            }

            resultantFileReader.close();
            System.out.println("Test4 done! \n");

            // Print some header info from resultant file
            System.out.println("Header info of the resultant joined Edf-file:");
            System.out.println(fileWriter4.getHeader());


/*****************************************************************************************
 *     Test EdfSignalsRemover. Reads data records from original file,
 *     removes samples belonging to channel 0 and write the resultant records
 *     to ekgcopy5.edf
 *****************************************************************************************/

            File resultantFile5 = new File(recordsDir, "ekgcopy5.edf");
            EdfFileWriter fileWriter5 = new EdfFileWriter(resultantFile5);
            EdfSignalsRemover signalsRemover = new EdfSignalsRemover(fileWriter5);
            signalsRemover.removeSignal(0);
            signalsRemover.setHeader(headerInfo);
            doubleBuffer1 = new double[originalDataRecordLength];
            // set DataRecord and signals positions to 0;
            originalFileReader.reset();
            while (originalFileReader.availableDataRecords() > 0) {
                // read physical DataRecord from the original file
                originalFileReader.readPhysicalDataRecord(doubleBuffer1);
                // write physical DataRecord to the EdfSignalRemover. It removes samples belonging to signal 0 and write the resultant
                // records to the new file
                signalsRemover.writePhysicalSamples(doubleBuffer1);
            }
            signalsRemover.close();

            System.out.println("Test5: copy file with removing data from channel 0");
            resultantFileReader = new EdfFileReader(resultantFile5);
            originalFileReader.reset();
            i = 0;
            intBuffer1 = new int[headerInfo.getNumberOfSamplesInEachDataRecord(1)];
            intBuffer2 = new int[resultantFileReader.getHeader().getDataRecordLength()];
            while(resultantFileReader.availableDataRecords() > 0) {
                // read samples belonging to signal 0 from the original file
                originalFileReader.readDigitalSamples(1, intBuffer1);
                // read DataRecord from the resultant file with removed signal 0
                resultantFileReader.readDigitalDataRecord(intBuffer2);
                i++;
                if(!Arrays.equals(intBuffer1, intBuffer2)) {
                    throw new RuntimeException("Test5: original and resultant files are not equals. Record: "+i);
                }
            }

            resultantFileReader.close();
            System.out.println("Test5 done! \n");

            // Print some header info from resultant file
            System.out.println("Header info of the resultant Edf-file with removed channel:");
            System.out.println(fileWriter5.getHeader());

/*****************************************************************************************
 *     EdfSignalsFilter usage example. Read data, apply some filtering to
 *     samples from channel 0 and write the resultant records
 *     to ekgcopy6.edf
 *****************************************************************************************/
            File resultantFile6 = new File(recordsDir, "ekgcopy6.edf");
            EdfFileWriter fileWriter6 = new EdfFileWriter(resultantFile6);
            EdfSignalsFilter signalsFilter = new EdfSignalsFilter(fileWriter6);
            signalsFilter.addSignalFilter(0, new MovingAverageFilter(10));
            signalsFilter.addSignalFilter(0, new HighPassFilter(1, headerInfo.getSampleFrequency(0)));
            signalsFilter.setHeader(headerInfo);

            // set DataRecord and signals positions to 0;
            originalFileReader.reset();
            intBuffer1 = new int[originalDataRecordLength];
            while (originalFileReader.availableDataRecords() > 0) {
                // read digital DataRecord from the original file
                originalFileReader.readDigitalDataRecord(intBuffer1);
                // write digital DataRecord to the EdfSignalsFilter. It applies MovingAverageFilter filter to
                // the samples belonging to signal 0 and write the resultant
                // records to the new file
                signalsFilter.writeDigitalSamples(intBuffer1);
            }
            signalsFilter.close();

            // Print some header info from resultant file after filtering
            System.out.println("Header info of the resultant filtered Edf-file:");
            System.out.println(fileWriter6.getHeader());

            originalFileReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}