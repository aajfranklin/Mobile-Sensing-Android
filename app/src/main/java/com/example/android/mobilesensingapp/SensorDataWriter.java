/*
 *  Alex Franklin, aajfranklin@gmail.com
 *
 *  This class is part of a continuous sensing application for Android
 *  For more information and the latest version, visit:
 *  https://github.com/aajfranklin/Mobile-Sensing-Android
 *
 *  This class has been modified from the open source continuous sensing
 *  application CrowdSense (2017). The original author of CrowdSense is
 *  Kleomenis Katevas, k.katevas@qmul.ac.uk
 *
 *  For the CrowdSense source code, visit:
 *  https://github.com/SensingKit/CrowdSense-Android
 *
 *  Permission to modify CrowdSense is granted under the terms of version
 *  3 of the GNU Lesser General Public License as published by the Free
 *  Software Foundation.
 *
 */

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

/**
 * Class responsible for writing out incoming data from each active sensor
 */
public class SensorDataWriter implements SKSensorDataListener {

    // Debug Tag for use logging debug output to LogCat
    private static final String TAG = "SensorDataWriter";
    @SuppressWarnings("FieldCanBeLocal UnusedDeclaration")
    private final SKSensorModuleType moduleType;
    @SuppressWarnings("FieldCanBeLocal")
    private File file;
    private BufferedOutputStream fileBuffer;

    /**
     * Constructor
     * Establishes module type, creates file to save data to, starts buffered output stream
     * @param moduleType The type of sensor module this sensor data writer will listen to and save data from
     * @param sessionFolder File: The folder to save data to for the current sensor session
     * @param filename String: The name of the file to save data to
     */
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

    /**
     * Writes out any buffered data
     * flush() and close() implemented separately as some JDK implementations swallow exceptions thrown by flush when closing
     * Best practice to make a habit of calling flush() before close() for this reason, even if not required here, so as not to be
     * caught out in future
     */
    void flush() throws SKException {

        try {
            fileBuffer.flush();
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    /**
     * Closes the buffered output stream
     */
    void close() throws SKException {

        try {
            fileBuffer.close();
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    /**
     * Creates file to save data to
     * @param sessionFolder File: The folder to save data to for the current sensor session
     * @param filename String: The name of the file to save data to
     * @return file: the file to save data to
     */
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

    /**
     * Writes buffered data out as a CSV
     * @param moduleType The type of sensor module this sensor data writer will listen to and save data from
     * @param moduleData The incoming sensor data
     */
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