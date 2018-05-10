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

/**
 * Class responsible for managing persistent settings and preferences
 */
class SharedPreferenceManager {
    private Map<String, Integer> defaultSensors;
    private Map<String, Integer> permissionSensors;
    private final String COMPATIBLE_SENSORS = "COMPATIBLE_SENSORS";
    private final String SENSORS_SET = "SENSORS_SET";
    private final String ENABLED_SENSORS = "ENABLES_SENSORS";

    /**
     * Constructor
     * Initialise LinkedHashMaps of sensor names: constant values for SKSensorModuleTypes
     */
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

    /**
     * Accessor for compatible sensors
     * @param context Application context
     * @return LinkedHashMap with details of sensors both supported by the app and available on this device
     */
    Map<String, ?> getCompatibleSensors(Context context) {
        return context.getSharedPreferences(COMPATIBLE_SENSORS, Context.MODE_PRIVATE).getAll();
    }

    /**
     * Accessor for available sensors
     * @param context Application context
     * @return LinkedHashMap with details of sensors currently enabled
     */
    Map<String, ?> getEnabledSensors(Context context) {
        return context.getSharedPreferences(ENABLED_SENSORS, Context.MODE_PRIVATE).getAll();
    }

    /**
     * Accessor for all sensors supported by the app, used when populating settings fragment
     * @return LinkedHashMap with details of all sensors supported by the app, regardless of whether available on the device or enabled by user
     */
    Map<String, Integer> getAllSensors() {
        Map<String, Integer> allSensors = new LinkedHashMap<>();
        allSensors.putAll(defaultSensors);
        allSensors.putAll(permissionSensors);
        return allSensors;
    }

    /**
     * Check if availability of default sensors on this device has already been established
     * @param context Application context
     * @return boolean: true if default sensor availability has been set previously
     */
    private boolean defaultSensorsAreSet(Context context) {
        return context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).getBoolean("Compatible Default Sensors Set", false);
    }


    /**
     * Check if availability of sensors requiring permissions on this device has already been established
     * @param context Application context
     * @return boolean: true if permission sensor availability has been set previously
     */
    private boolean permissionSensorsAreSet(Context context) {
        return context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).getBoolean("Compatible Permission Sensors Set", false);
    }

    /**
     * Set shared preferences for which default sensors are available on the device
     * @param context Application context
     */
    void setDefaultSensors(Context context) {
        if (!defaultSensorsAreSet(context)) {
            SharedPreferences.Editor editor = context.getSharedPreferences(COMPATIBLE_SENSORS, Context.MODE_PRIVATE).edit();

            for (Map.Entry<String, Integer> entry : defaultSensors.entrySet()) {
                SKSensorModuleType sensor = SKSensorModuleType.values()[entry.getValue()];
                if (sensorIsCompatible(context, sensor)) editor.putInt(entry.getKey(), entry.getValue());
            }
            editor.apply();
            editor = context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).edit();
            editor.putBoolean("Compatible Default Sensors Set", true);
            editor.apply();
            this.setEnabledSensors(context);
        }
    }


    /**
     * Set shared preferences for which sensors requiring permission are available on the device
     * @param context Application context
     */
    void setPermissionSensors(Context context) {
        if (!permissionSensorsAreSet(context)) {
            SharedPreferences.Editor editor = context.getSharedPreferences(COMPATIBLE_SENSORS, Context.MODE_PRIVATE).edit();
            for (Map.Entry<String, Integer> entry : permissionSensors.entrySet()) {
                SKSensorModuleType sensor = SKSensorModuleType.values()[entry.getValue()];
                if (sensorIsCompatible(context, sensor)) editor.putInt(entry.getKey(), entry.getValue());
            }
            editor.apply();
            editor = context.getSharedPreferences(SENSORS_SET, Context.MODE_PRIVATE).edit();
            editor.putBoolean("Compatible Permission Sensors Set", true);
            editor.apply();
            editor = context.getSharedPreferences(ENABLED_SENSORS, Context.MODE_PRIVATE).edit();
            editor.putBoolean("Audio Level", true);
            editor.apply();        }
    }

    /**
     * Checks if a sensor is available on the device by attempting to register and start it
     * @param context Application context
     * @param sensor String: the sensor module type to check
     * @return boolean: true if compatible
     */
    private boolean sensorIsCompatible(Context context, SKSensorModuleType sensor) {
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

    /**
     * Check if individual sensor is currently enabled
     * @param context Application context
     * @param sensorName String: name of sensor to check, used to identify relevant key in shared preferences
     * @return boolean: true if available
     */
    boolean sensorIsEnabled(Context context, String sensorName) {
        Map<String, ?> enabledSensors = this.getEnabledSensors(context);
        return (Boolean) enabledSensors.get(sensorName);
    }

    /**
     * Set all all compatible sensors to enabled by default
     * @param context Application context
     */
    private void setEnabledSensors(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(ENABLED_SENSORS, Context.MODE_PRIVATE).edit();
        for (Map.Entry<String, ?> entry : this.getCompatibleSensors(context).entrySet()) {
            editor.putBoolean(entry.getKey(), true);
        }
        editor.apply();
    }

    /**
     * Change enabled status of single sensor on de/selection by user via settings screen
     * @param sensorName String: name of sensor to change status
     * @param context Application context
     */
    void changeSensorStatus(String sensorName, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(ENABLED_SENSORS, Context.MODE_PRIVATE).edit();
        Map<String, ?> enabledSensors = this.getEnabledSensors(context);
        if ((Boolean) enabledSensors.get(sensorName)) {
            editor.putBoolean(sensorName, false);
        } else {
            editor.putBoolean(sensorName, true);
        }
        editor.apply();
    }
}