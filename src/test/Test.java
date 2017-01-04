package test;

import com.biorecorder.edflib.BdfWriter;
import com.biorecorder.edflib.EdfBdfReader;
import com.biorecorder.edflib.HeaderConfig;

import java.io.*;


/**
 * Created by gala on 01/01/17.
 */
public class Test {
    public static void main(String[] args) {
        File file = new File(System.getProperty("user.dir")+"/records", "30-12-2016_12-17.bdf");
        try {
            FileInputStream fileStream = new FileInputStream(file);
            byte[] buf = new byte[8];
            fileStream.read(buf);
            System.out.print(buf[0]+" buf: "+ new String(buf));
            EdfBdfReader reader = new EdfBdfReader(file);
            BdfWriter writer = new BdfWriter(new File(System.getProperty("user.dir")+"/records", "copy.bdf"));
            HeaderConfig headerConfig = reader.getHeaderConfig();
            writer.setHeaderConfig(headerConfig);
            int numberOfRecords = 0;
            for(int i = 0; i < headerConfig.getNumberOfDataRecords(); i++) {
                writer.writeDigitalDataRecord(reader.readDataRecord());
                numberOfRecords++;
            }
            System.out.println("number of records "+ numberOfRecords);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
