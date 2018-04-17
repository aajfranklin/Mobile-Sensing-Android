package com.example.android.mobilesensingapp;

import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorDataListener;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SensorDataWriter implements SKSensorDataListener {

    private static final String TAG = "SensorDataWriter";
    @SuppressWarnings("FieldCanBeLocal UnusedDeclaration")
    private final SKSensorModuleType moduleType;
    @SuppressWarnings("FieldCanBeLocal")
    private File file;
    private BufferedOutputStream fileBuffer;

    SensorDataWriter (SKSensorModuleType moduleType, File sessionFolder, String filename) throws SKException {

        this.moduleType = moduleType;
        this.file = createFile(sessionFolder, filename);

        try {
            this.fileBuffer = new BufferedOutputStream(new FileOutputStream(file));
        }
        catch (FileNotFoundException ex) {
            throw new SKException(TAG, "File could not be found.", SKExceptionErrorCode.UNKNOWN_ERROR);
        }

    }

    // flush() and close() implemented separately as some JDK implementations swallow exceptions thrown by flush when closing
    // best practice to stay used to using flush() before a close for this reason, even if excessive here.
    void flush() throws SKException {

        try {
            fileBuffer.flush();
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    void close() throws SKException {

        try {
            fileBuffer.close();
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    private File createFile(File sessionFolder, String filename) throws SKException {

        File file = new File(sessionFolder, filename + ".csv");

        try {
            if (!file.createNewFile()) {
                throw new SKException(TAG, "File could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }

        return file;
    }

    @Override
    public void onDataReceived(SKSensorModuleType moduleType, SKSensorData moduleData) {

        if (fileBuffer != null) {

            String dataLine = moduleData.getDataInCSV() + "\n";

            try {
                fileBuffer.write(dataLine.getBytes());
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }
}