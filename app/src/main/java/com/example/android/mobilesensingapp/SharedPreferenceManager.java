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

class SharedPreferenceManager {
    private Map<String, Integer> defaultSensors;
    private Map<String, Integer> permissionSensors;
    private final String AVAILABLE_SENSORS = "AVAILABLE_SENSORS";
    private final String SENSORS_SET = "SENSORS_SET";

    SharedPreferenceManager() {
        defaultSensors = new LinkedHashMap<>();
        defaultSensors.put("Accelerometer", 0);
        defaultSensors.put("Ambient Temperature", 6);
        defaultSensors.put("Battery", 12);
        defaultSensors.put("Gravity", 1);
        defaultSensors.put("Gyroscope", 3);
        defaultSensors.put("Light", 9);
        defaultSensors.put("Linear Acceleration", 2);
        defaultSensors.put("Magnetometer", 5);
        defaultSensors.put("Rotation", 4);
        defaultSensors.put("Step Counter", 7);
        defaultSensors.put("Step Detector", 8);

        permissionSensors = new LinkedHashMap<>();
        permissionSensors.put("Audio Level", 15);
    }

    Map<String, ?> getAvailableSensors(Context context) {
        return context.getSharedPreferences(AVAILABLE_SENSORS, Context.MODE_PRIVATE).getAll();
    }

    private boolean defaultSensorsAreSet(Context context) {
        return context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).getBoolean("Default Sensors Set", false);
    }

    private boolean permissionSensorsAreSet(Context context) {
        return context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).getBoolean("Permission Sensors Set", false);
    }

    void setAvailableSensors(Context context) {
        if (!defaultSensorsAreSet(context)) {
            SharedPreferences.Editor editor = context.getSharedPreferences(AVAILABLE_SENSORS, Context.MODE_PRIVATE).edit();

            for (Map.Entry<String, Integer> entry : defaultSensors.entrySet()) {
                SKSensorModuleType sensor = SKSensorModuleType.values()[entry.getValue()];
                if (checkSensor(context, sensor)) editor.putInt(entry.getKey(), entry.getValue());
            }
            editor.apply();
            editor = context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).edit();
            editor.putBoolean("Default Sensors Set", true);
            editor.apply();
        }
    }

    void setPermissionSensors(Context context) {
        if (!permissionSensorsAreSet(context)) {
            SharedPreferences.Editor editor = context.getSharedPreferences(AVAILABLE_SENSORS, Context.MODE_PRIVATE).edit();
            for (Map.Entry<String, Integer> entry : permissionSensors.entrySet()) {
                SKSensorModuleType sensor = SKSensorModuleType.values()[entry.getValue()];
                if (checkSensor(context, sensor)) editor.putInt(entry.getKey(), entry.getValue());
            }
            editor.apply();
            editor = context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).edit();
            editor.putBoolean("Permission Sensors Set", true);
            editor.apply();
        }
    }

    private boolean checkSensor(Context context, SKSensorModuleType sensor) {
        try {
            SensingKitLibInterface mSensingKitLib = SensingKitLib.getSensingKitLib(context);
            mSensingKitLib.registerSensorModule(sensor);
            mSensingKitLib.startContinuousSensingWithSensor(sensor);
            mSensingKitLib.stopContinuousSensingWithSensor(sensor);
            mSensingKitLib.deregisterSensorModule(sensor);
            return true;
        } catch (SKException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }
}