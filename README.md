# EDFLib

EDFlib is a programming library for Java to read/write EDF/BDF files. EDF+/BDF+ format at the moment is not supported.

<!---
##Installation
Download JAR and import it in your app. https://github.com/instasent/instasent-java-lib/releases/download/0.1.1/instasent-java-lib.jar.
--->

## License

This project is licensed under the terms of the MIT license. See license.txt.

## EDF Format

EDF means European Data Format. It is the popular medical time series storage fileformat.
EDF stores digital samples (mostly from an analog to digital converter) in two bytes, so the maximum
resolution is **16&nbsp;bit**.

BDF is the 24-bits version of EDF.  However, 24-bit ADC's are becoming more and more popular. The data produced by 24-bit ADC's can not be stored in EDF without losing information. BDF stores the datasamples in three bytes, giving it a resolution of **24&nbsp;bit**.


EDF/BDF is a simple and flexible format for the storage and exchange of multichannel biological signals. This means that it is used to store samples coming from multiple measuring channels, where every channel has its own measuring frequency which may differ from other channel frequencies.

Every EDF/BDF file consists of a **header record** and then all of the **data records** from the beginning to the end of the recording.

### Data Records

The data samples from different channels in EDF/BDF files are organized in DataRecords. A DataRecord accumulates the data samples of each channel during a specified period of time (usually 1 sec) where all data samples of channel 1 consecutive be saved. Then comes samples of channel 2 … until all channels are stored. This record will be followed by another data record of the same specified data recording time and so on.

So every DataRecord is a flat array of values, that  has a specified length and actually
represent the following two-dimensional data structure:
<br>[ n\_1 samples from channel\_1,
<br>  &nbsp; n\_2 samples from channel\_2,
<br>  &nbsp;&nbsp; ...
<br> &nbsp; n\_i samples from channel\_i  ]

Real physical data samples coming from measuring channels are generally floating point data, but they are scaled to fit into 16 bits integer (EDF format) or 24 bits integer (BDF format).   To do that a linear relationship is assumed between physical (floating point) and digital (integer) values.

For every channel (signal) **physical minimum** and **maximum**
and the corresponding **digital minimum** and **maximum** must be determined. That permits to calculate the scaling factor for every channel and to convert physical values to digital and vice versa:

<p>(physValue - physMin) / (digValue - digMin)  = constant [Gain] = (physMax - physMin) / (digMax - digMin)

<p>Thus every physical DataRecord (array of doubles) can be converted
to digital DataRecord (array of integers) and backwards.

### Header Record

Thus to correctly extract data from DataRecords we need to know their structure. Exactly for this purpose the file has the header record. The header record consists of two parts. The **first part** contains some significant information  about the experiment:

* patient identification
* recording identification
* start date and time of the recording
* number of data records in the file (-1 if unknown)
* duration of a data record (in seconds)
* number of channels (signals)  in data record
* ...

The  **second part** of the header contains significant information about every measuring channel (signal):

* channel label
* physical dimension (e.g. uV or degree C)
* physical minimum (e.g. -500 or 34)
* physical maximum (e.g. 500 or 40)
* digital minimum (e.g. -2048)
* digital maximum (e.g. 2047)
* number of samples in each data record
* ...

More detailed information about EDF/BDF format:
<br><a href="http://www.edfplus.info/specs/edf.html">European Data Format. Full specification of EDF</a>
<br><a href="http://www.teuniz.net/edfbrowser/edf%20format%20description.html">The EDF format</a>
<br><a href="http://www.teuniz.net/edfbrowser/bdfplus%20format%20description.html">The BDF format</a>
<br><a href="http://www.biosemi.com/faq/file_format.htm">EDF/BDF difference</a>
<br><a href="http://www.edfplus.info/specs/edffloat.html">EDF. How to store longintegers and floats</a>

## Library Usage and Javadoc

The library has 2 main files:  **EdfReader.java** to read EDF/BDF files, **EdfWriter.java**  to create and write EDF or BDF file.

Also the class **HeaderConfig.java** is necessary to store the information from the file header or to provide it.

For more detailed info see [javadoc](http://biorecorder.com/api/edflib/) .

## Examples

### EdfWriter

To save data to a EDF/BDF file we have to create EdfWriter:

```java
EdfWriter edfFileWriter = new EdfWriter("filename");

```

But to make it possible to write data to the file, we must open the EdfWriter first and pass a
HeaderConfig object with the configuration information for the file header record.

Suppose that we have a two-channels measuring device. One channel measures the cardiogram with the frequency 500 Hz, and the other is accelerometer detecting movements with the frequency 50 Hz. Lets create appropriate HeaderConfig and open the EdfWriter:

```java

HeaderConfig headerInfo = new HeaderConfig(2);
headerInfo.setPatientIdentification("Some Patient");
headerInfo.setDurationOfDataRecord(1); // 1 second

headerInfo.setLabel(0,"EKG");
headerInfo.setDigitalMin(0,-32767);
headerInfo.setDigitalMax(0,32767);
headerInfo.setPhysicalMin(0,-3125);
headerInfo.setPhysicalMax(0,3125);
headerInfo.setPhysicalDimension(0,"uV");
headerInfo.setNumberOfSamplesInEachDataRecord(0,500);

headerInfo.setLabel(1,"Accelerometer");
headerInfo.setDigitalMin(1,-32767);
headerInfo.setDigitalMax(1, 32767);
headerInfo.setPhysicalMin(1,-16384);
headerInfo.setPhysicalMax(1,-16384);
headerInfo.setPhysicalDimension(1,"m/sec^3");

edfFileWriter.open(headerInfo);
```

Now EdfWriter is redy to write data. And we may write the samples from the channels directly to the EdfWriter:

```java
int[] signal0Samples = new int[500];
int[] signal1Samples = new int[50];

while (isRecording) {
    // fill signal0Samples with 500 samples from channel_0
    edfFileWriter.writeDigitalSamples(signal0Samples);
    // fill signal1Samples with 50 samples from channel_1
    edfFileWriter.writeDigitalSamples(signal1Samples);
}
```

The same way we may write not digital but physical values.


Or we may first prepare a DataRecord by filling it with data from both channels (recived during 1 second), and when the record will be ready write it to the file:

```java
boolean isRecording = true;

int[] dataRecord = new int[500 + 50];

while (isRecording) {
    // put 500 samples from channel_0 to the dataRecord channel_0
    // put 50 samples from channel_1 to the dataRecord
    edfFileWriter.writeDigitalDataRecord(dataRecord);
}

```

When we finish to work with EdfWriter we must  close it:
```java
edfFileWriter.close();
```
### EdfReader

To read  a EDF/BDF file, we have to create EdfReader:

```java
EdfReader edfFileReader = new EdfReader("filename");
```

Now we can get the header record of the file and for example print some header information:

```java
HeaderConfig headerInfo = edfFileReader.getHeaderInfo();

System.out.println("File type "+ headerInfo.getFileType());
System.out.println("Duration of DataRecords = "+headerInfo.getDurationOfDataRecord());
System.out.println("Number of signals = "+headerInfo.getNumberOfSignals());
for(int i = 0; i < headerInfo.getNumberOfSignals(); i++) {
      System.out.println(i+ ": label = "+ headerInfo.getLabel(i)
      + ", number of samples in data records = "
   +  headerInfo.getNumberOfSamplesInEachDataRecord(i));
}

```

We can change header information if we want. Lets for example change the patient name and the label of signal 0 (Note that the signals numbering starts from 0!):

```java
headerInfo.setPatientIdentification("John Smith");
headerInfo.setLabel(0,"EKG");
edfFileReader.rewriteHeader(headerInfo);
```

Data from the file we may read in DataRecords (digital o phycisal):

```java
int[] digitalDataRecord;
while (edfFileReader.availableDataRecords() > 0) {
    digitalDataRecord = edfFileReader.readDigitalDataRecord();
    // do smth
}

// or

edfFileReader.setDataRecordPosition(0);
double[] physicalDataRecord;
while (edfFileReader.availableDataRecords() > 0) {
    physicalDataRecord = edfFileReader.readPhysicalDataRecord();
    // do smth
}

```

Or we may read only samples belonging to some channel (also digital or  physical):

```java
int numberOfamples = 100;
int signalNumber = 0;
int[] digitalSamples;

 while (edfFileReader.availableSamples(signalNumber) > 0) {
    digitalSamples = edfFileReader.readDigitalSamples(signalNumber, numberOfamples);
  // do smth
}

// or

edfFileReader.setSamplePosition(signalNumber, 0);
double[] physicalSamples;
while (edfFileReader.availableSamples(signalNumber) > 0) {
    physicalSamples = edfFileReader.readPhysicalSamples(signalNumber, numberOfamples);
  // do smth
}
```

Note that every signal has it's own independent sample position indicator. That permits us to read samples from different signals independently.

Every time we read samples belonging to some signal the corresponding sample position indicator will be increased with the amount of samples read.

But we may set the signal sample position indicator to the position we want:

```java
long samplePosition = 5000;
edfFileReader.setSamplePosition(signalNumber, samplePosition);
```

When we finish to work with EdfReader we must  close it:

```java
edfFileReader.close();
```


### DataRecords filtering

It is possible to do some kind of DataRecords transformation before actually write them to the file.

Class **DataRecordsJoiner.java** combines a few short DataRecords into one:

```java
EdfWriter edfFileWriter = new EdfWriter("filename", FileType.EDF_16BIT);
int numberOfRecordsToJoin = 10;
DataRecordsJoiner joiner = new DataRecordsJoiner(numberOfRecordsToJoin, edfFileWriter);
joiner.open(headerInfo);

joiner.writeDigitalDataRecord(dataRecord);
```

In this example if input DataRecords have duration = 1 second then resultant DataRecords actually written to the file will have duration = 10 seconds.

Class **DataRecordsSignalsManager.java** permits to omit samples from some channels (delete signal) or realize some kind of transformation with the signal data (add some filter to the signal). At the moment only SignalMovingAverageFilter is available for signal transformation:

```java
EdfWriter edfFileWriter = new EdfWriter("filename", FileType.EDF_16BIT);
DataRecordsSignalsManager signalsManager = new DataRecordsSignalsManager(edfFileWriter);
signalsManager.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));
signalsManager.removeSignal(1);
signalsManager.open(headerInfo);

signalsManager.writeDigitalDataRecord(dataRecord);
```

In this example if the input DataRecords have samples from 2 channels then the resultant DataRecords (actually written to the file) will have samples only from one channel, and the samples values will be averaging... Averaging permits to reduce the 50 hz  noise. So the resultant signal will be much more "clean".

Filters can be connected together in a chain:

```java
EdfWriter edfFileWriter = new EdfWriter("filename", FileType.EDF_16BIT);
int numberOfRecordsToJoin = 10;
DataRecordsJoiner joiner = new DataRecordsJoiner(numberOfRecordsToJoin, edfFileWriter);

DataRecordsSignalsManager signalsManager = new DataRecordsSignalsManager(joiner);
signalsManager.addSignalPrefiltering(0, new SignalMovingAverageFilter(10));
signalsManager.removeSignal(1);
signalsManager.open(headerInfo);

signalsManager.writeDigitalDataRecord(dataRecord);
```

An example program is available in the 'examples/EdflibExample.java' file.  This example reads the EDF file ('records/ekg.edf') and copy its data to a new EDF file with the filtering described above.  The file ekg.edf  contains real data from two channels - the cardiogram and accelerometer.

Use [EDFbrowser](http://www.teuniz.net/edfbrowser/ "EDFbrowser") to view EDF/BDF-files.


