/*
 *  Alex Franklin, aajfranklin@gmail.com
 *
 *  This class is part of a continuous sensing application for Android
 *  For more information, visit https://github.com/aajfranklin/Mobile-Sensing-Android
 *
 */

package com.example.android.mobilesensingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Controls persistent sensor service, explicitly started and stopped by the user
 * Ensures that the app only stops reading sensor data when explicitly stopped by the user
 * and otherwise continues to read sensor data when running in the background or closed
 */
public class SensorService extends Service {

    // Debug Tag for use logging debug output to LogCat
    private static final String TAG = "SensorService";
    private PowerManager.WakeLock wakeLock;
    private SensorSession sSession;

    /**
     * Starts sensor service.
     * @param intent Intent provided to startService(Intent) in MainActivity
     * @param flags int: additional data about this start request. 0, START_FLAG_REDELIVER or START_FLAG_RETRY
     * @param startId int: unique id representing each specific start attempt
     * @return START_STICKY int: constant specifying that this service is explicitly started and stopped as needed
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startSensing();
        return START_STICKY;
    }

    /**
     * Mandatory implementation of abstract method in Service interface
     * Returns interface for communication between a client and the service
     * Returns null as this service has no clients
     * @param intent Intent used to bind service
     * @return null: No interface provided as this service has no clients
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Properly stops sensor service if it is halted prematurely by OS memory management
     * Ensures that data gathered up to this point is properly saved
     */
    @Override
    public void onDestroy() {
        stopSensing();
        super.onDestroy();
    }

    /**
     * Creates a sensor session to record sensor data
     * @return session: the created sensor session
     */
    private SensorSession createSensingSession() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.UK);
        String folderName = dateFormat.format(new Date());

        SensorSession session;

        try {
            session = new SensorSession(this, folderName);
        }
        catch (SKException ex) {
            Log.e(TAG, ex.getMessage());
            session = null;
        }

        return session;
    }

    /**
     * Creates persistent foreground notification while sensor service is active
     * Ensures that the service persists when the app is minimised/closed
     * Creates notification channel for Android Oreo and above
     */
    private void showNotification() {

        // The pending intent to launch the application if the user selects the notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String CHANNEL_ID = getString(R.string.channel_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG,"Post-Oreo");

            // Create the NotificationChannel
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        // Build the notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .build();

        // Start the notification in foreground, to ensure the service persists when app is minimised/closed
        startForeground(1, notification);
    }

    /**
     * Hides notification, called when service is stopped
     */
    private void hideNotification() {
        stopForeground(true);
    }

    /**
     * Ensures the CPU does not become inactive (sleep mode) when the application is running with device screen off
     * Wake lock is set to time out after an hour
     */
    private void acquireWakeLock() {
        if ((wakeLock == null) || (!wakeLock.isHeld())) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
            }
            wakeLock.acquire(3600000);
        }
    }

    /**
     * Releases wake lock, called when service is stopped
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /**
     * Called on start
     * Starts sensor session, acquires wake lock, and displays notification
     */
    public void startSensing() {

        if (sSession != null) {
            Log.e(TAG,"SensorSession already created.");
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

    /**
     * Called on stop, whether by user or when service is stopped by OS memory management
     * Stops sensor session, releases wake lock, hides notification
     */
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