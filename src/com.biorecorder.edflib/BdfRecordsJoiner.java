package com.biorecorder.edflib;



/**
 * Created by mac on 02/12/14.
 */
public class BdfRecordsJoiner {
    private BdfHeader bdfHeader;
    private int numberOfRecordsToJoin;
    private int recordsCounter;
    private byte[] resultingDataRecord;
    private int numberOfBytesInDataFormat;
    private int resultingDataRecordLength;

    public BdfRecordsJoiner(BdfHeader bdfHeader, int numberOfRecordsToJoin) {
        this.bdfHeader = bdfHeader;
        this.numberOfRecordsToJoin = numberOfRecordsToJoin;
        numberOfBytesInDataFormat = bdfHeader.isBdf() ? 3 : 2;

        for (SignalConfig signalConfig : bdfHeader.getSignals()) {
            resultingDataRecordLength += signalConfig.getNumberOfSamplesInEachDataRecord();
        }
        resultingDataRecordLength = resultingDataRecordLength * numberOfRecordsToJoin * numberOfBytesInDataFormat;
        resultingDataRecord = new byte[resultingDataRecordLength];
    }


    public boolean addDataRecord(byte[] bdfDataRecord) {
        if(recordsCounter == 0) {
            resultingDataRecord = new byte[resultingDataRecordLength];
        }
        recordsCounter++;
        int pointer = 0;
        SignalConfig[] signalConfigs = bdfHeader.getSignals();
        for (int i = 0; i < signalConfigs.length; i++) {
            int numberOfSamples = signalConfigs[i].getNumberOfSamplesInEachDataRecord();
            int toIndex = (pointer * numberOfRecordsToJoin + numberOfSamples * (recordsCounter - 1)) * numberOfBytesInDataFormat;
            int fromIndex = pointer * numberOfBytesInDataFormat;
            System.arraycopy(bdfDataRecord, fromIndex, resultingDataRecord, toIndex, numberOfSamples * numberOfBytesInDataFormat);
            pointer += numberOfSamples;
        }
        if (recordsCounter == numberOfRecordsToJoin) {
            recordsCounter = 0;
            return true;
        } else {
            return false;
        }
    }

    public boolean addDataRecord(int[] bdfDataRecord) {
        int numberOfBytesInDataFormat = bdfHeader.isBdf() ? 3 : 2;
        return addDataRecord(BdfParser.intArrayToByteArray(bdfDataRecord, numberOfBytesInDataFormat));
    }

    public byte[] getResultingDataRecord() {
        return resultingDataRecord;
    }

    public BdfHeader getResultingBdfHeader() {
        BdfHeader resultingBdfHeader = bdfHeader.copy();
        resultingBdfHeader.setDurationOfDataRecord(bdfHeader.getDurationOfDataRecord() * numberOfRecordsToJoin);
        SignalConfig[] originalSignalConfigs =  bdfHeader.getSignals();
        int length = originalSignalConfigs.length;
        SignalConfig[] signalsConfigs = resultingBdfHeader.getSignals();
        for(int i = 0; i < length; i++) {
            int resultingNumberOfSamples = numberOfRecordsToJoin * signalsConfigs[i].getNumberOfSamplesInEachDataRecord();
            signalsConfigs[i].setNumberOfSamplesInEachDataRecord(resultingNumberOfSamples);
        }
        return resultingBdfHeader;

    }

}
