/*
 *  Alex Franklin, aajfranklin@gmail.com
 *
 *  This class is part of a continuous sensing application for Android
 *  For more information, visit https://github.com/aajfranklin/Mobile-Sensing-Android
 *
 */

package com.example.android.mobilesensingapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Class to register sensor modules, subscribe sensor data listeners, and start
 * SensorDataWriters when a SensorService is started
 */
class SensorSession {

    // Debug Tag for use logging debug output to LogCat
    private static final String TAG = "SensingSession";
    private SensingKitLibInterface mSensingKitLib;
    private boolean isSensing = false;
    private Map<String, SKSensorModuleType> sensors;
    private ArrayList<SensorDataWriter> dataWriters;
    private SharedPreferenceManager preferenceManager;


    /**
     * Constructor
     * Creates folder for sensor data to be saved to
     * Starts sensor data writer, registers sensor module, and subscribes sensor data listener for each sensor
     * @param context Sensor service context links sensor session to the service
     * @param folderName String: name of folder where data will be saved
     */
    SensorSession(final Context context, final String folderName) throws SKException {
        mSensingKitLib = SensingKitLib.getSensingKitLib(context);
        preferenceManager = new SharedPreferenceManager();
        sensors = preferenceManager.sensors;

        File sessionFolder = createFolder(folderName);

        dataWriters = new ArrayList<>();

        // start sensor data writers
        for (Map.Entry<String, SKSensorModuleType> entry : sensors.entrySet()) {
            SKSensorModuleType sensorType = entry.getValue();
            String sensorName = entry.getKey();
            if(preferenceManager.sensorIsAvailable(context, entry.getKey())) {
                SensorDataWriter writer = new SensorDataWriter(sensorType, sessionFolder, sensorName);
                mSensingKitLib.registerSensorModule(sensorType);
                mSensingKitLib.subscribeSensorDataListener(sensorType, writer);
                dataWriters.add(writer);
            }
        }
    }

    /**
     * Starts continuous sensing with all sensors
     */
    void startSession(Context context) throws SKException {
        this.isSensing = true;

        for (Map.Entry<String, SKSensorModuleType> entry : sensors.entrySet()) {
            SKSensorModuleType sensorType = entry.getValue();
            if(preferenceManager.sensorIsAvailable(context, entry.getKey())) {
                mSensingKitLib.startContinuousSensingWithSensor(sensorType);
            }
        }
    }

    /**
     * Stops continuous sensing with all sensors
     */
    void stopSession(Context context) throws SKException {
        this.isSensing = false;

        for (Map.Entry<String, SKSensorModuleType> entry : sensors.entrySet()) {
            SKSensorModuleType sensorType = entry.getValue();
            if(preferenceManager.sensorIsAvailable(context, entry.getKey())) {
                mSensingKitLib.stopContinuousSensingWithSensor(sensorType);
            }
        }

        for (SensorDataWriter writer : dataWriters) {
            writer.flush();
        }
    }

    /**
     * Unsubscribes sensor data writers, deregisters sensor modules, closes data writer output streams
     */
    void close(Context context) throws SKException {

        int i = 0;

        for (Map.Entry<String, SKSensorModuleType> entry : sensors.entrySet()) {

            SKSensorModuleType sensorType = entry.getValue();
            if(preferenceManager.sensorIsAvailable(context, entry.getKey())) {
                mSensingKitLib.unsubscribeSensorDataListener(sensorType, dataWriters.get(i));
                mSensingKitLib.deregisterSensorModule(sensorType);
                i++;
            }
        }

        for (SensorDataWriter writer : dataWriters) {
            writer.close();
        }
    }

    /**
     * Checks sensor session active status
     * @return boolean: true if sensing, false if not
     */
    boolean isSensing(){
        return this.isSensing;
    }

    /**
     * Creates folder for sensor data to be saved to
     * @param folderName String: name of the folder, constructed on creation of sensor service
     * @return folder: the folder created
     */
    private File createFolder(final String folderName) throws SKException {
        File appFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileSensing/");

        if (!appFolder.exists()) {
            if (!appFolder.mkdir()) {
                throw new SKException(TAG, "Folder could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }

        Log.d(TAG, appFolder.getAbsolutePath());
        File folder = new File(appFolder, folderName);

        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new SKException(TAG, "Folder could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }

        return folder;
    }
}