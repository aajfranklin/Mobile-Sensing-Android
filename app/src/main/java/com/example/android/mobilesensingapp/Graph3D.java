package com.example.android.mobilesensingapp;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.sensingkit.sensingkitlib.SKSensorModuleType;
import org.sensingkit.sensingkitlib.data.SKAccelerometerData;
import org.sensingkit.sensingkitlib.data.SKGyroscopeData;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

class Graph3D {
    LineChart chart;
    private LineDataSet xDataSet;
    private LineDataSet yDataSet;
    private LineDataSet zDataSet;
    private LineData chartData;

    void initialiseValues(int axisHeight, String desc) {
        YAxis y = chart.getAxisLeft();
        y.setAxisMaximum(axisHeight);

        List<Entry> entriesX = new ArrayList<>();
        List<Entry> entriesY = new ArrayList<>();
        List<Entry> entriesZ = new ArrayList<>();
        entriesX.add(new Entry(0,0));
        entriesY.add(new Entry(0,0));
        entriesZ.add(new Entry(0,0));
        //noinspection SuspiciousNameCombination
        xDataSet = new LineDataSet(entriesX, "x");
        xDataSet.setDrawCircles(false);
        xDataSet.setColor(RED);
        xDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        yDataSet = new LineDataSet(entriesY, "y");
        yDataSet.setDrawCircles(false);
        yDataSet.setColor(BLUE);
        yDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        zDataSet = new LineDataSet(entriesZ, "z");
        zDataSet.setDrawCircles(false);
        zDataSet.setColor(GREEN);
        zDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        ArrayList<ILineDataSet> lines = new ArrayList<>();
        lines.add(xDataSet);
        lines.add(yDataSet);
        lines.add(zDataSet);

        chartData = new LineData(lines);
        chartData.setDrawValues(false);
        chart.setData(chartData);
        chart.getDescription().setText(desc);
        chart.invalidate();
    }

    void updateGraph(SKSensorData sensorData, SKSensorModuleType moduleType, long startTime) {

        if (moduleType == SKSensorModuleType.ACCELEROMETER) {
            SKAccelerometerData accelerometerData = (SKAccelerometerData) sensorData;
            xDataSet.addEntry(new Entry((accelerometerData.getTimestamp() - startTime), accelerometerData.getX()));
            yDataSet.addEntry(new Entry((accelerometerData.getTimestamp() - startTime), accelerometerData.getY()));
            zDataSet.addEntry(new Entry((accelerometerData.getTimestamp() - startTime), accelerometerData.getZ()));
        } else {
            SKGyroscopeData gyroscopeData = (SKGyroscopeData) sensorData;
            xDataSet.addEntry(new Entry((gyroscopeData.getTimestamp() - startTime), gyroscopeData.getX()));
            yDataSet.addEntry(new Entry((gyroscopeData.getTimestamp() - startTime), gyroscopeData.getY()));
            zDataSet.addEntry(new Entry((gyroscopeData.getTimestamp() - startTime), gyroscopeData.getZ()));
        }
        xDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        yDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        zDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        chartData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
}