package com.example.android.mobilesensingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import org.sensingkit.sensingkitlib.SKException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SensorService extends Service {

    private final IBinder binder = new LocalBinder();
    private PowerManager.WakeLock wakeLock;
    private SensorSession sSession;
    private NotificationManager notificationManager;
    private boolean active;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        stopSensing();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    class LocalBinder extends Binder {

        SensorService getService() {
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

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            String CHANNEL_ID = "mobile_sensing_01";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(description);

            // Register the channel with the system
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Mobile Sensing")
                    .setContentText("Collecting sensor data...")
                    // Placeholder Icon, update or give credit: https://visualpharm.com/free-icons/sensor-595b40b85ba036ed117dba5a
                    .setSmallIcon(R.drawable.download)
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(true);

            notificationManager.notify(1, builder.build());
        } else {

            Notification notification = new Notification.Builder(this)
                    .setContentTitle("Mobile Sensing")
                    .setContentText("Collecting sensor data...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(contentIntent)
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(true)
                    .build();

            startForeground(1, notification);
        }
    }

    private void hideNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancel(1);
        } else {
            stopForeground(true);
        }
    }

    private void acquireWakeLock() {
        if ((wakeLock == null) || (!wakeLock.isHeld())) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLock");
            wakeLock.acquire();
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
        active = true;
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
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}