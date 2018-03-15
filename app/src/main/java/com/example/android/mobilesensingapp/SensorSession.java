package com.example.android.mobilesensingapp;

import android.content.Context;
import android.os.Environment;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;

import java.io.File;

/**
 * Created by Alex on 15/03/2018.
 */

class SensorSession {

    @SuppressWarnings("unused")
    private static final String TAG = "SensingSession";

    private SensingKitLibInterface mSensingKitLib;
    private boolean isSensing = false;
    private SensorDataWriter accelerometerWriter;
    private SensorDataWriter gyroscopeWriter;

    SensorSession(final Context context, final String folderName) throws SKException {
        mSensingKitLib = SensingKitLib.getSensingKitLib(context);

        File sessionFolder = createFolder(folderName);

        accelerometerWriter = new SensorDataWriter(SKSensorModuleType.ACCELEROMETER, sessionFolder, "Accelerometer");
        gyroscopeWriter = new SensorDataWriter(SKSensorModuleType.GYROSCOPE, sessionFolder, "Gyroscope");

        mSensingKitLib.registerSensorModule(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.GYROSCOPE);

        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, accelerometerWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.GYROSCOPE, gyroscopeWriter);
    }

    void startSession() throws SKException {
        this.isSensing = true;
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
    }

    void stopSession() throws SKException {
        this.isSensing = false;
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
    }

    void close() throws SKException {
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, accelerometerWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.GYROSCOPE, gyroscopeWriter);

        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.GYROSCOPE);

        accelerometerWriter.close();
        gyroscopeWriter.close();
    }

    boolean isSensing(){
        return this.isSensing;
    }

    private File createFolder(final String folderName) throws SKException {
        File appFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MobileSensing/");

        if (!appFolder.exists()) {
            if (!appFolder.mkdir()) {
                throw new SKException(TAG, "Folder could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }

        // Create session folder
        File folder = new File(appFolder, folderName);

        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new SKException(TAG, "Folder could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }

        return folder;
    }
}