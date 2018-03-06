package com.example.android.mobilesensingapp;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
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

class Graph3D {
    LineChart chart;
    private LineDataSet xDataSet;
    private LineDataSet yDataSet;
    private LineDataSet zDataSet;
    private LineData chartData;

    void initialiseValues(int axisHeight, String desc) {
        YAxis y = chart.getAxisLeft();
        y.setAxisMaximum(axisHeight);
        YAxis rightY = chart.getAxisRight();
        rightY.setEnabled(false);
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        List<Entry> entriesX = new ArrayList<>();
        List<Entry> entriesY = new ArrayList<>();
        List<Entry> entriesZ = new ArrayList<>();
        entriesX.add(new Entry(0,0));
        entriesY.add(new Entry(0,0));
        entriesZ.add(new Entry(0,0));
        //noinspection SuspiciousNameCombination
        xDataSet = new LineDataSet(entriesX, "x");
        xDataSet.setDrawCircles(false);
        yDataSet = new LineDataSet(entriesY, "y");
        yDataSet.setDrawCircles(false);
        zDataSet = new LineDataSet(entriesZ, "z");
        zDataSet.setDrawCircles(false);

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

    void setLineColors(int red, int blue, int green) {
        xDataSet.setColor(red);
        yDataSet.setColor(blue);
        zDataSet.setColor(green);
    }

    void updateGraph(SKSensorData sensorData, SKSensorModuleType moduleType, long startTime) {

        long chartTime;

        if (moduleType == SKSensorModuleType.ACCELEROMETER) {
            SKAccelerometerData accelerometerData = (SKAccelerometerData) sensorData;
            chartTime = accelerometerData.getTimestamp() - startTime;
            xDataSet.addEntry(new Entry(chartTime, accelerometerData.getX()));
            yDataSet.addEntry(new Entry(chartTime, accelerometerData.getY()));
            zDataSet.addEntry(new Entry(chartTime, accelerometerData.getZ()));
        } else {
            SKGyroscopeData gyroscopeData = (SKGyroscopeData) sensorData;
            chartTime = gyroscopeData.getTimestamp() - startTime;
            xDataSet.addEntry(new Entry(chartTime, gyroscopeData.getX()));
            yDataSet.addEntry(new Entry(chartTime, gyroscopeData.getY()));
            zDataSet.addEntry(new Entry(chartTime, gyroscopeData.getZ()));
        }
        chartData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(10000);
        if (chartTime > 10000) {
            chart.moveViewToX(chartTime - 10000);
        } else {
            chart.invalidate();
        }
    }
}