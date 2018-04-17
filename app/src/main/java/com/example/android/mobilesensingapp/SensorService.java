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

import org.sensingkit.sensingkitlib.SKException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorService extends Service {

    private PowerManager.WakeLock wakeLock;
    private SensorSession sSession;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startSensing();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopSensing();
        super.onDestroy();
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

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String CHANNEL_ID = getString(R.string.channel_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("Post-Oreo");

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

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.download)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    private void hideNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            stopForeground(true);
        }
    }

    private void acquireWakeLock() {
        if ((wakeLock == null) || (!wakeLock.isHeld())) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
            }
            wakeLock.acquire(1800000);
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

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