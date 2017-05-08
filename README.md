# EDFLib

EDFlib is a programming library for Java to read/write EDF/BDF files. EDF+/BDF+ format at the moment is not supported.

<!---
##Installation
Download JAR and import it in your app. https://github.com/instasent/instasent-java-lib/releases/download/0.1.1/instasent-java-lib.jar.
--->

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

The library has 2 main files:  **EdfFileReader.java** to read EDF/BDF files, **EdfFileWriter.java**  to create and write EDF or BDF file.

Also the class **HeaderInfo.java** is necessary to store the information from the file header or to provide it.

For more detailed info see [javadoc](http://biorecorder.com/api/edflib/javadoc) .

## Examples

### EdfWriter

To save data to a EDF/BDF file we have to create EdfWriter and give it a HeaderInfo object with the configuration information for the file header record.

Suppose that we have a two-channels measuring device. One channel  with the frequency 50 Hz, and the other with the frequency 5 Hz. Lets create appropriate HeaderInfo object and EdfFileWriter:

```java
int numberOfChannels = 2;
int channel0Frequency = 50; // Hz
int channel1Frequency = 5; // Hz

// create header info for the file describing data records structure
HeaderInfo headerInfo = new HeaderInfo(numberOfChannels, FileType.EDF_16BIT);
// Signal numbering starts from 0!

// configure signal (channel) number 0
headerInfo.setSampleFrequency(0, channel0Frequency);
headerInfo.setLabel(0, "first channel");
headerInfo.setPhysicalRange(0, -500, 500);
headerInfo.setDigitalRange(0, -2048, -2047);
headerInfo.setPhysicalDimension(0, "uV");

// configure signal (channel) number 1
headerInfo.setSampleFrequency(1, channel1Frequency);
headerInfo.setLabel(1, "second channel");
headerInfo.setPhysicalRange(1, 100, 300);


// create EdfFileWriter
File file = new File("filename.edf");
EdfFileWriter edfFileWriter = new EdfFileWriter(file, headerInfo);
```

Now we may write data samples to the EdfFileWriter. Lets write to the file 10 data records:

```java
// create and write samples
int[] samplesFromChannel0 = new int[channel0Frequency];
int[] samplesFromChannel1 = new int[channel1Frequency];
Random rand = new Random();
for(int i = 0; i < 10; i++) {
    // create random samples for channel 0
    for(int j = 0; j < samplesFromChannel0.length; j++) {
        samplesFromChannel0[j] = rand.nextInt(10000);
    }

    // create random samples for channel 1
    for(int j = 0; j < samplesFromChannel1.length; j++) {
        samplesFromChannel1[j] = rand.nextInt(1000);
    }

    // write samples from both channels to the edf file
    edfFileWriter.writeDigitalSamples(samplesFromChannel0);
    edfFileWriter.writeDigitalSamples(samplesFromChannel1);
}

```

The same way we may write not digital but physical values.

When we finish to work with EdfWriter we must close it:

```java
edfFileWriter.close();
```
### EdfReader

To read  a EDF/BDF file, we have to create EdfReader:

```java
EdfFileReader edfFileReader = new EdfFileReader(new File("filename.edf"));
```

Now we can read data samples (digital or  physical) belonging to any channel:

```java
int signalNumber = 0;

// read digital values
int[] digSampleBuffer = new int[100] ;

edfFileReader.setSamplePosition(signalNumber, 0);

while (edfFileReader.availableSamples(signalNumber) > 0) {
    edfFileReader.readDigitalSamples(signalNumber, digSampleBuffer);
    // do smth with samples stored in digSampleBuffer
}
```
or

```java
// read physical values
double[] physSampleBuffer = new double[50];

edfFileReader.setSamplePosition(signalNumber, 0);

while (edfFileReader.availableSamples(signalNumber) > 0) {
    edfFileReader.readPhysicalSamples(signalNumber, physSampleBuffer);
    // do smth with samples stored in physSampleBuffer
}
```
Note that every signal has it's own independent sample position indicator. That permits us to read samples from different signals independently.

Every time we read samples belonging to some signal the corresponding sample position indicator will be increased with the amount of samples read.

But we may set the signal sample position indicator to the position we want:

```java
long samplePosition = 5000;
edfFileReader.setSamplePosition(signalNumber, samplePosition);
```

Also we can get the header record of the file and for example print some header information:

```java
System.out.println(edfFileReader.getHeader());
```

When we finish to work with EdfReader we must close it:

```java
edfFileReader.close();
```


### DataRecords filtering

It is possible to do some kind of DataRecords transformation before actually write them to the file.

Class **EdfJoiner.java** combines a few short DataRecords into one:

```java
HeaderInfo headerInfor;
// create and configure headerInfo
// ....

// create edf file writer
EdfFileWriter edfFileWriter = new EdfFileWriter(new File("filename.edf"));

// create edf joiner
int numberOfRecordsToJoin = 5;
EdfJoiner joiner = new EdfJoiner(numberOfRecordsToJoin, edfFileWriter);

// set headerInfo
joiner.setHeader(headerInfo);

// write digital or physical samples
joiner.writeDigitalSamples(intArray);
joiner.writePhysicalSamples(doubleArray);
```

In this example if input DataRecord has duration = 1 sec, then resultant DataRecords actually written to the file will have duration = 5 seconds.

Class **EdfSignalsFilter.java** permits to realize some kind of transformation with the signal data (add some digital filter to the signal). At the moment only MovingAverage and HighPass filters are available.


```java
HeaderInfo headerInfor;
// create and configure headerInfo
// ....

// create edf file writer
EdfFileWriter edfFileWriter = new EdfFileWriter(new File("filename.edf"));

// create EdfSignalsFilter
EdfSignalsFilter signalsFilter = new EdfSignalsFilter(edfFileWriter);
// set MovingAvg filter for signal number 0
signalsFilter.addSignalFilter(0, new MovingAverageFilter(10));

// set headerInfo
signalsFilter.setHeader(headerInfo);

// write digital or physical samples
signalsFilter.writeDigitalSamples(intArray);
signalsFilter.writePhysicalSamples(doubleArray);
```

An example program is available in the 'examples/EdfExample.java' file.

Use [EDFbrowser](http://www.teuniz.net/edfbrowser/ "EDFbrowser") to view EDF/BDF-files.


