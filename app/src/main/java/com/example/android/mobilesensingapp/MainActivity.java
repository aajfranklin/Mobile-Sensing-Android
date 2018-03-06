package com.example.android.mobilesensingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;  //Document:  needed to add this to init SensingKit
import org.sensingkit.sensingkitlib.data.SKAccelerometerData;
import org.sensingkit.sensingkitlib.data.SKSensorData;
import org.sensingkit.sensingkitlib.SKSensorDataListener;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

public class MainActivity extends AppCompatActivity {

    SensingKitLibInterface mSensingKitLib;
    SKAccelerometerData accelerometerDataPoint;
    ArrayList<SKAccelerometerData> accelerometerDataList = new ArrayList<SKAccelerometerData>();
    LineChart chart;
    LineDataSet xDataSet;
    LineDataSet yDataSet;
    LineDataSet zDataSet;
    LineData chartData;
    long startTime = 0;

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

        chart = (LineChart) findViewById(R.id.chart);
        YAxis y = chart.getAxisLeft();
        y.setAxisMaximum(20);

        List<Entry> entriesX = new ArrayList<Entry>();
        List<Entry> entriesY = new ArrayList<Entry>();
        List<Entry> entriesZ = new ArrayList<Entry>();
        entriesX.add(new Entry(0,0));
        entriesY.add(new Entry(0,0));
        entriesZ.add(new Entry(0,0));
        xDataSet = new LineDataSet(entriesX, "x-axis acceleration");
        xDataSet.setDrawCircles(false);
        xDataSet.setColor(RED);
        xDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        yDataSet = new LineDataSet(entriesY, "y-axis acceleration");
        yDataSet.setDrawCircles(false);
        yDataSet.setColor(BLUE);
        yDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        zDataSet = new LineDataSet(entriesZ, "z-axis acceleration");
        zDataSet.setDrawCircles(false);
        zDataSet.setColor(GREEN);
        zDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        ArrayList<ILineDataSet> lines = new ArrayList<ILineDataSet>();
        lines.add(xDataSet);
        lines.add(yDataSet);
        lines.add(zDataSet);

        chartData = new LineData(lines);
        chartData.setDrawValues(false);
        chart.setData(chartData);
        chart.invalidate();
    }

    public void buttonOnClick(View v) {
        try {
            if (mSensingKitLib.isSensorModuleSensing(SKSensorModuleType.ACCELEROMETER)) {
                mSensingKitLib.stopContinuousSensingWithSensor(SKSensorModuleType.ACCELEROMETER);
                accelerometerDataList.clear();
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
                    updateGraph(accelerometerDataPoint);
                }
            });
        } catch (SKException e) {
            System.err.println("SensingKit Exception4");
        }
    }

    public void updateGraph(SKAccelerometerData newDataPoint) {
        xDataSet.addEntry(new Entry((newDataPoint.getTimestamp() - startTime), newDataPoint.getX()));
        yDataSet.addEntry(new Entry((newDataPoint.getTimestamp() - startTime), newDataPoint.getY()));
        zDataSet.addEntry(new Entry((newDataPoint.getTimestamp() - startTime), newDataPoint.getZ()));
        xDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        yDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        zDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        chartData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
}