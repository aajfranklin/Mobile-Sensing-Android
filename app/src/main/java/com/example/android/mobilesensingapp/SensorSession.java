package com.example.android.mobilesensingapp;

import android.content.Context;
import android.os.Environment;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;
import org.sensingkit.sensingkitlib.data.SKBatteryData;

import java.io.File;

class SensorSession {

    @SuppressWarnings("unused")
    private static final String TAG = "SensingSession";

    private SensingKitLibInterface mSensingKitLib;
    private boolean isSensing = false;
    private SensorDataWriter accelerometerWriter;
    private SensorDataWriter batteryWriter;
    private SensorDataWriter gravityWriter;
    private SensorDataWriter gyroscopeWriter;
    private SensorDataWriter linAccelWriter;
    private SensorDataWriter magnetWriter;
    private SensorDataWriter rotationWriter;
//    private SensorDataWriter stepCountWriter;
//    private SensorDataWriter stepDetectWriter;

    SensorSession(final Context context, final String folderName) throws SKException {
        mSensingKitLib = SensingKitLib.getSensingKitLib(context);

        File sessionFolder = createFolder(folderName);

        accelerometerWriter = new SensorDataWriter(SKSensorModuleType.ACCELEROMETER, sessionFolder, "Accelerometer");
        batteryWriter = new SensorDataWriter(SKSensorModuleType.BATTERY, sessionFolder, "Battery");
        gravityWriter = new SensorDataWriter(SKSensorModuleType.GRAVITY, sessionFolder, "Gravity");
        gyroscopeWriter = new SensorDataWriter(SKSensorModuleType.GYROSCOPE, sessionFolder, "Gyroscope");
        linAccelWriter = new SensorDataWriter(SKSensorModuleType.LINEAR_ACCELERATION, sessionFolder, "Linear Acceleration");
        magnetWriter = new SensorDataWriter(SKSensorModuleType.MAGNETOMETER, sessionFolder, "Magnetometer");
        rotationWriter = new SensorDataWriter(SKSensorModuleType.ROTATION, sessionFolder, "Rotation");
//        stepCountWriter = new SensorDataWriter(SKSensorModuleType.STEP_COUNTER, sessionFolder, "Step Counter");
//        stepDetectWriter = new SensorDataWriter(SKSensorModuleType.STEP_DETECTOR, sessionFolder, "Step Detector");

        mSensingKitLib.registerSensorModule(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.BATTERY);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.GRAVITY);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.registerSensorModule(SKSensorModuleType.ROTATION);
//        mSensingKitLib.registerSensorModule(SKSensorModuleType.STEP_COUNTER);
//        mSensingKitLib.registerSensorModule(SKSensorModuleType.STEP_DETECTOR);

        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, accelerometerWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.BATTERY, batteryWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.GRAVITY, gravityWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.GYROSCOPE, gyroscopeWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.LINEAR_ACCELERATION, linAccelWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.MAGNETOMETER, magnetWriter);
        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.ROTATION, rotationWriter);
//        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.STEP_COUNTER, stepCountWriter);
//        mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.STEP_DETECTOR, stepDetectWriter);
    }

    void startSession() throws SKException {
        this.isSensing = true;
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.BATTERY);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.GRAVITY);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.ROTATION);
//        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.STEP_COUNTER);
//        mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.STEP_DETECTOR);
    }

    void stopSession() throws SKException {
        this.isSensing = false;
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.BATTERY);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.GRAVITY);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ROTATION);
//        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.STEP_COUNTER);
//        mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.STEP_DETECTOR);

        accelerometerWriter.flush();
        batteryWriter.flush();
        gravityWriter.flush();
        gyroscopeWriter.flush();
        linAccelWriter.flush();
        magnetWriter.flush();
        rotationWriter.flush();
//        stepCountWriter.flush();
//        stepDetectWriter.flush();
    }

    void close() throws SKException {
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, accelerometerWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.BATTERY, batteryWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.GRAVITY, gravityWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.GYROSCOPE, gyroscopeWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.LINEAR_ACCELERATION, linAccelWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.MAGNETOMETER, magnetWriter);
        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.ROTATION, rotationWriter);
//        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.STEP_COUNTER, stepCountWriter);
//        mSensingKitLib.unsubscribeSensorDataListener(SKSensorModuleType.STEP_DETECTOR, stepDetectWriter);


        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.ACCELEROMETER);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.BATTERY);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.GRAVITY);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.GYROSCOPE);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.LINEAR_ACCELERATION);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.MAGNETOMETER);
        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.ROTATION);
//        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.STEP_COUNTER);
//        mSensingKitLib.deregisterSensorModule(SKSensorModuleType.STEP_DETECTOR);

        accelerometerWriter.close();
        batteryWriter.close();
        gravityWriter.close();
        gyroscopeWriter.close();
        linAccelWriter.close();
        magnetWriter.close();
        rotationWriter.close();
//        stepCountWriter.close();
//        stepDetectWriter.close();
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

        System.out.println(appFolder);
        File folder = new File(appFolder, folderName);

        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new SKException(TAG, "Folder could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }

        return folder;
    }
}