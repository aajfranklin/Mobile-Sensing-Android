package com.example.android.mobilesensingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;

import java.util.LinkedHashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class SharedPreferenceManager {
    Map<String, SKSensorModuleType> sensors;
    private final String AVAILABLE_SENSORS = "AVAILABLE_SENSORS";

    SharedPreferenceManager() {
        sensors = new LinkedHashMap<>();
        sensors.put("Accelerometer", SKSensorModuleType.ACCELEROMETER);
        sensors.put("Ambient Temperature", SKSensorModuleType.AMBIENT_TEMPERATURE);
        sensors.put("Audio Level", SKSensorModuleType.AUDIO_LEVEL);
        sensors.put("Battery", SKSensorModuleType.BATTERY);
        sensors.put("Gravity", SKSensorModuleType.GRAVITY);
        sensors.put("Gyroscope", SKSensorModuleType.GYROSCOPE);
        sensors.put("Light", SKSensorModuleType.LIGHT);
        sensors.put("Linear Acceleration", SKSensorModuleType.LINEAR_ACCELERATION);
        sensors.put("Magnetometer", SKSensorModuleType.MAGNETOMETER);
        sensors.put("Rotation", SKSensorModuleType.ROTATION);
        sensors.put("Step Counter", SKSensorModuleType.STEP_COUNTER);
        sensors.put("Step Detector", SKSensorModuleType.STEP_DETECTOR);
    }

    boolean sensorsAreSet(Context context) {
        return getAvailableSensors(context).getBoolean("Set", false);
    }

    SharedPreferences getAvailableSensors(Context context) {
        return context.getSharedPreferences(AVAILABLE_SENSORS, Context.MODE_PRIVATE);
    }

    boolean sensorIsAvailable(Context context, String sensor) {
        return getAvailableSensors(context).getBoolean(sensor, false);
    }

    void setAvailableSensors(Context context) {
        if (!sensorsAreSet(context)) {
            final SharedPreferences.Editor editor = getAvailableSensors(context).edit();

            for (Map.Entry<String, SKSensorModuleType> entry : sensors.entrySet()) {
                SKSensorModuleType sensor = entry.getValue();
                try {
                    SensingKitLibInterface mSensingKitLib = SensingKitLib.getSensingKitLib(context);
                    mSensingKitLib.registerSensorModule(sensor);
                    mSensingKitLib.startContinuousSensingWithSensor(sensor);
                    mSensingKitLib.stopContinuousSensingWithSensor(sensor);
                    mSensingKitLib.deregisterSensorModule(sensor);
                    editor.putBoolean(entry.getKey(), true);
                    Log.d(TAG, entry.getKey() + " Sensor Available");
                } catch (SKException e) {
                    editor.putBoolean(entry.getKey(), false);
                    Log.e(TAG, e.getMessage());
                }
            }

            editor.putBoolean("Set", true);
            editor.apply();
            Log.d(TAG, "Sensors needed setting");
        } else {
            Log.d(TAG, "Sensors were set");
        }
    }
}