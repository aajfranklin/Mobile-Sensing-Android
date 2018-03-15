package com.example.android.mobilesensingapp;

/**
 * Created by Alex on 15/03/2018.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import org.sensingkit.sensingkitlib.SKException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private PowerManager.WakeLock mWakeLock;
    private SensorSession sSession;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {

        try {
            sSession.stopSession();
            sSession.close();
            hideNotification();
        }
        catch (SKException ex) {
            ex.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // To ensure the service continues running until explicitly stopped
        return START_STICKY;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    class LocalBinder extends Binder {

        SensorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SensorService.this;
        }
    }

    private SensorSession createSensingSession() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.UK);
        String folderName = dateFormat.format(new Date());

        SensorSession session;

        try {
            session = new SensorSession(this, folderName);
        }
        catch (SKException ex) {
            System.err.println(ex.getMessage());
            session = null;
        }

        return session;
    }

    private void showNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build the notification
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Mobile Sensing")
                .setContentText("Collecting sensor data...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .build();

        // Show the notification
        startForeground(1, notification);
        System.out.println("Hello!");
    }

    private void hideNotification() {
        stopForeground(true);
    }

    private void acquireWakeLock() {
        if ((mWakeLock == null) || (!mWakeLock.isHeld())) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
            mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    // Public Interface

    public void startSensing() {

        if (sSession != null) {
            System.err.println("SensorSession already created.");
        }

        sSession = createSensingSession();

        try {
            acquireWakeLock();
            sSession.startSession();
        }
        catch (SKException ex) {
            ex.printStackTrace();
        }

        showNotification();
    }

    public void stopSensing() {

        try {

            if (sSession.isSensing()) {
                sSession.stopSession();
            }

            sSession.close();
        }
        catch (SKException ex) {
            ex.printStackTrace();
        }

        releaseWakeLock();
        hideNotification();

        sSession = null;
    }
}