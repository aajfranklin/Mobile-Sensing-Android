package com.example.android.mobilesensingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import com.github.mikephil.charting.charts.LineChart;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;  //Document:  needed to add this to init SensingKit
import org.sensingkit.sensingkitlib.data.SKAccelerometerData;
import org.sensingkit.sensingkitlib.data.SKSensorData;
import org.sensingkit.sensingkitlib.SKSensorDataListener;

public class MainActivity extends AppCompatActivity {

    SensingKitLibInterface mSensingKitLib;
    SKAccelerometerData accelerometerDataPoint;
    ArrayList<SKAccelerometerData> accelerometerDataList = new ArrayList<SKAccelerometerData>();
    long startTime = 0;
    Graph3D accelerometerGraph;

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
        } catch (SKException e) {
            System.err.println("SensingKit Exception2");
        }

        accelerometerGraph = new Graph3D();
        accelerometerGraph.chart = (LineChart) findViewById(R.id.graph);
        accelerometerGraph.initialiseValues();
    }

    public void buttonOnClick(View v) {
        try {
            if (mSensingKitLib.isSensorModuleSensing(SKSensorModuleType.ACCELEROMETER)) {
                mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
                accelerometerDataList.clear();
//                startTime = 0;
            } else {
                subscribe();
                mSensingKitLib.startContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
            }
        } catch (SKException e) {
            System.err.println("SensingKit Exception3");
        }
    }

    public void subscribe() {
        try {
            mSensingKitLib.subscribeSensorDataListener(SKSensorModuleType.ACCELEROMETER, new SKSensorDataListener() {
                @Override
                public void onDataReceived(final SKSensorModuleType moduleType, final SKSensorData sensorData) {
                    System.out.println(sensorData.getDataInCSV());  // Print data in CSV format
                    accelerometerDataPoint = (SKAccelerometerData) sensorData;
                    if (startTime == 0) {
                        startTime = accelerometerDataPoint.getTimestamp();
                    }
                    accelerometerGraph.updateGraph(sensorData, startTime);
                }
            });
        } catch (SKException e) {
            System.err.println("SensingKit Exception4");
        }
    }
}