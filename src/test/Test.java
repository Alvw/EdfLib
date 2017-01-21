package test;

import com.biorecorder.edflib.*;
import com.biorecorder.edflib.filters.DataRecordsJoiner;
import com.biorecorder.edflib.filters.DataRecordsSignalsManager;
import com.biorecorder.edflib.filters.SignalMovingAverageFilter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * Created by gala on 01/01/17.
 */
public class Test {
    public static void main(String[] args) {
        edfReaderWriterTest1();


    }

    public static void byteBufferTest() {
        File file =  new File(System.getProperty("user.dir"), "Testfile.txt");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            FileWriter fileWriter = new FileWriter(file);
            FileChannel inChannel = fileInputStream.getChannel();
            fileWriter.write("hello world my darling baby!");
            fileWriter.close();

            byte[] array = new byte[5];

            ByteBuffer byteBuffer = ByteBuffer.wrap(array);
            int n= 0;
            while((n=inChannel.read(byteBuffer)) > 0) {
                byteBuffer.flip();
                for(int i = 0; i < array.length; i++)
                    System.out.println(n +"  result  "+ (char) (array[i] & 0xFF));
                byteBuffer.clear();
            }
            System.out.println("end of file " + n);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void edfReaderWriterTest() {
        File originalFile = new File(System.getProperty("user.dir")+"/records", "01-01-2017_19-51.bdf");
        File copyFile = new File(System.getProperty("user.dir")+"/records", "copy.bdf");
        try {
            EdfBdfReader reader = new EdfBdfReader(originalFile);
            RecordingConfig recordingConfig = reader.getRecordingConfig();

            BdfWriter bdfWriter = new BdfWriter(copyFile);

            RecordingConfig recordingConfigNew = new RecordingConfig(recordingConfig);
            recordingConfigNew.removeSignalConfig(0);
            System.out.println("one signal");
            bdfWriter.open(recordingConfigNew);

            while(reader.availableSignalSamples(1) > 0) {

                bdfWriter.writePhysicalSamples(reader.readPhysicalSamples(1, 13));
            }

            System.out.println(bdfWriter.getWritingInfo());

            bdfWriter.close();
            reader.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void edfReaderWriterTest1() {
        File originalFile = new File(System.getProperty("user.dir")+"/records", "01-01-2017_19-51.bdf");
        File copyFile = new File(System.getProperty("user.dir")+"/records", "copy.bdf");
        try {
            EdfBdfReader reader = new EdfBdfReader(originalFile);
            RecordingConfig recordingConfig = reader.getRecordingConfig();

            BdfWriter bdfWriter = new BdfWriter(copyFile);

            DataRecordsJoiner joiner = new DataRecordsJoiner(10, bdfWriter);

            DataRecordsSignalsManager filteredWriter = new DataRecordsSignalsManager(joiner);
            filteredWriter.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));

            System.out.println("filtered copy + physical_digital conversion");
            filteredWriter.open(recordingConfig);
            reader.setRecordPosition(50);
            while(reader.availableDataRecords() > 0) {
                filteredWriter.writePhysicalDataRecord(reader.readPhysycalDataRecord());
            }

            System.out.println(bdfWriter.getWritingInfo());

            bdfWriter.close();
            reader.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
