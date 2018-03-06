package com.example.android.mobilesensingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;  //Document:  needed to add this to init SensingKit
import org.sensingkit.sensingkitlib.data.SKAccelerometerData;
import org.sensingkit.sensingkitlib.data.SKGyroscopeData;
import org.sensingkit.sensingkitlib.data.SKSensorData;
import org.sensingkit.sensingkitlib.SKSensorDataListener;

public class MainActivity extends AppCompatActivity {

    private SensingKitLibInterface mSensingKitLib;
    private long startTime = 0;
    private Graph3D accelerometerGraph;
    private Graph3D gyroscopeGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mSensingKitLib = SensingKitLib.getSensingKitLib(this);
        } catch (SKException e) {
            System.err.println("SensingKit Exception1");
        }

        try {
            mSensingKitLib.registerSensorModule(SKSensorModuleType.ACCELEROMETER);
            mSensingKitLib.registerSensorModule(SKSensorModuleType.GYROSCOPE);
        } catch (SKException e) {
            System.err.println("SensingKit Exception2");
        }

        accelerometerGraph = new Graph3D();
        accelerometerGraph.chart = findViewById(R.id.graph1);
        accelerometerGraph.initialiseValues(20, "Accelerometer");
        gyroscopeGraph = new Graph3D();
        gyroscopeGraph.chart = findViewById(R.id.graph2);
        gyroscopeGraph.initialiseValues(6, "Gyroscope");

        int red = getResources().getColor(R.color.lineDarkPink);
        int blue = getResources().getColor(R.color.lineBlue);
        int green = getResources().getColor(R.color.lineTeal);

        accelerometerGraph.setLineColors(red, blue, green);
        gyroscopeGraph.setLineColors(red, blue, green);
    }

    public void buttonOnClick(View v) {
        try {
            if (mSensingKitLib.isSensorModuleSensing(SKSensorModuleType.ACCELEROMETER)) {
                mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
                mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
//                startTime = 0;
            } else {
                subscribe();
                mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
                mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.GYROSCOPE);
            }
        } catch (SKException e) {
            System.err.println("SensingKit Exception3");
        }
    }

    private void subscribe() {
        try {
            mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, new SKSensorDataListener() {
                @Override
                public void onDataReceived(final SKSensorModuleType moduleType, final SKSensorData sensorData) {
                    System.out.println(sensorData.getDataInCSV());  // Print data in CSV format
                    SKAccelerometerData accelerometerDataPoint = (SKAccelerometerData) sensorData;
                    if (startTime == 0) {
                        startTime = accelerometerDataPoint.getTimestamp();
                    }
                    accelerometerGraph.updateGraph(sensorData, moduleType, startTime);
                }
            });
            mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.GYROSCOPE, new SKSensorDataListener() {
                @Override
                public void onDataReceived(final SKSensorModuleType moduleType, final SKSensorData sensorData) {
                    System.out.println(sensorData.getDataInCSV());  // Print data in CSV format
                    SKGyroscopeData gyroscopeDataPoint = (SKGyroscopeData) sensorData;
                    if (startTime == 0) {
                        startTime = gyroscopeDataPoint.getTimestamp();
                    }
                    gyroscopeGraph.updateGraph(sensorData, moduleType, startTime);
                }
            });
        } catch (SKException e) {
            System.err.println("SensingKit Exception4");
        }
    }
}