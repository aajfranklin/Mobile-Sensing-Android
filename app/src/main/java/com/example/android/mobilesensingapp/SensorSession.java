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

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;

import java.io.File;

/**
 * Class to register sensor modules, subscribe sensor data listeners, and start
 * SensorDataWriters when a SensorService is started
 */
class SensorSession {

    // Debug Tag for use logging debug output to LogCat
    private static final String TAG = "SensingSession";
    private SensingKitLibInterface mSensingKitLib;
    private boolean isSensing = false;
    // Data writers for all sensors
    private SensorDataWriter accelerometerWriter;
    private SensorDataWriter batteryWriter;
    private SensorDataWriter gravityWriter;
    private SensorDataWriter gyroscopeWriter;
    private SensorDataWriter linAccelWriter;
    private SensorDataWriter magnetWriter;
    private SensorDataWriter rotationWriter;

    /**
     * Constructor
     * Creates folder for sensor data to be saved to
     * Starts sensor data writer, registers sensor module, and subscribes sensor data listener for each sensor
     * @param context Sensor service context links sensor session to the service
     * @param folderName String: name of folder where data will be saved
     */
    SensorSession(final Context context, final String folderName) throws SKException {
        mSensingKitLib = SensingKitLib.getSensingKitLib(context);

        File sessionFolder = createFolder(folderName);

        // start sensor data writers
        accelerometerWriter = new SensorDataWriter(SKSensorModuleType.ACCELEROMETER, sessionFolder, "Accelerometer");
        batteryWriter = new SensorDataWriter(SKSensorModuleType.BATTERY, sessionFolder, "Battery");
        gravityWriter = new SensorDataWriter(SKSensorModuleType.GRAVITY, sessionFolder, "Gravity");
        gyroscopeWriter = new SensorDataWriter(SKSensorModuleType.GYROSCOPE, sessionFolder, "Gyroscope");
        linAccelWriter = new SensorDataWriter(SKSensorModuleType.LINEAR_ACCELERATION, sessionFolder, "Linear Acceleration");
        magnetWriter = new SensorDataWriter(SKSensorModuleType.MAGNETOMETER, sessionFolder, "Magnetometer");
        rotationWriter = new SensorDataWriter(SKSensorModuleType.ROTATION, sessionFolder, "Rotation");

        // register sensor modules
        mSensingKitLib.registerSensorModule(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.BATTERY);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.GRAVITY);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.ROTATION);

        // subscribe sensor data writers to listen to their appropriate sensors
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, accelerometerWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.BATTERY, batteryWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.GRAVITY, gravityWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.GYROSCOPE, gyroscopeWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.LINEAR_ACCELERATION, linAccelWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.MAGNETOMETER, magnetWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.ROTATION, rotationWriter);
    }

    /**
     * Starts continuous sensing with all sensors
     */
    void startSession() throws SKException {
        this.isSensing = true;
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.BATTERY);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.GRAVITY);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.ROTATION);
    }

    /**
     * Stops continuous sensing with all sensors
     */
    void stopSession() throws SKException {
        this.isSensing = false;
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.BATTERY);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.GRAVITY);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ROTATION);

        accelerometerWriter.flush();
        batteryWriter.flush();
        gravityWriter.flush();
        gyroscopeWriter.flush();
        linAccelWriter.flush();
        magnetWriter.flush();
        rotationWriter.flush();
    }

    /**
     * Unsubscribes sensor data writers, deregisters sensor modules, closes data writer output streams
     */
    void close() throws SKException {
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, accelerometerWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.BATTERY, batteryWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.GRAVITY, gravityWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.GYROSCOPE, gyroscopeWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.LINEAR_ACCELERATION, linAccelWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.MAGNETOMETER, magnetWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.ROTATION, rotationWriter);

        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.BATTERY);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.GRAVITY);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.ROTATION);

        accelerometerWriter.close();
        batteryWriter.close();
        gravityWriter.close();
        gyroscopeWriter.close();
        linAccelWriter.close();
        magnetWriter.close();
        rotationWriter.close();
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