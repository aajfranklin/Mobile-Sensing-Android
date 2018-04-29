/*
 *  Alex Franklin, aajfranklin@gmail.com
 *
 *  This class is part of a continuous sensing application for Android
 *  For more information, visit https://github.com/aajfranklin/Mobile-Sensing-Android
 *
 */

package com.example.android.mobilesensingapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Controls interactive elements of the application home screen
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Constant for use in request for permission to write to storage
    private static final int PERMISSION_REQUESTS = 1;
    // UI elements
    private ToggleButton startButton;
    private ToggleButton stopButton;
    private ToggleButton pauseButton;

    private SharedPreferenceManager preferenceManager;
    private SensorService sService;
    private boolean bound;

    /**
     * Sets content view for main user activity
     * Sets available sensors
     * Makes write to storage permission request if necessary
     * Sets toggle switch position and text on app start/resume
     * @param savedInstanceState Bundle: activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check permissions and request if necessary
        boolean hasWritePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean hasAudioPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if (!(hasWritePermission && hasAudioPermission)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUESTS);
        }
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sService = binder.getService();
            bound = true;
            System.out.println("onServiceConnected()");
            if (sService.isSensing()) {
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                pauseButton.setEnabled(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onResume() {
        System.out.println("onResume()");
        super.onResume();

        preferenceManager = new SharedPreferenceManager();
        preferenceManager.setAvailableSensors(this);

        // Ensures audio level sensor is made available if user changed permission from
        // OS app info page, rather than in app
        boolean hasAudioPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        if(hasAudioPermission) {
            preferenceManager.setPermissionSensors(this);
        }

        // Set toggle switch position and text based on SensorService status
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        pauseButton= findViewById(R.id.pause_button);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);

        Intent intent = new Intent(this, SensorService.class);

        if (sensorServiceIsRunning()) {
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        System.out.println("onStop()");
        super.onStop();

        if (sensorServiceIsRunning()) {
            if (bound) {
                unbindService(connection);
                bound = false;
            }
        }
    }

    /**
     * Determines behaviour of toggle switch on click
     * Starts sensor service if it is not currently active
     * Stops sensor service if it is currently active
     * @param v The View clicked
     */
    public void onClick(View v) {

        // An Intent is a passive data structure holding a a description of an action to be performed
        Intent intent = new Intent(this, SensorService.class);

        if (v == stopButton) {
            unbindService(connection);
            bound = false;
            stopService(intent);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);
        } else if (v == startButton) {
            if (sensorServiceIsRunning()) {
                sService.resumeSensing();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                pauseButton.setEnabled(true);
            } else {
                startService(intent);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
            }
        } else if (v == pauseButton) {
            sService.pauseSensing();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);
        }
    }

    /**
     * Checks sensor service active status
     * @return boolean: true for active service, false for inactive
     */
    private boolean sensorServiceIsRunning() {

        ActivityManager manager = (ActivityManager) getSystemService (Context.ACTIVITY_SERVICE);

        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (SensorService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Displays toast to the user noting that app will not request properly if write storage permission is denied
     * Called on receipt of permission request results
     * @param requestCode int: request code passed to the original permission request
     * @param permissions String: the requested permissions
     * @param grantResults int: the grant results, either PERMISSION_GRANTED or PERMISSION_DENIED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int granted = PackageManager.PERMISSION_GRANTED;
        int writeStorage = grantResults[0];
        int recordAudio = grantResults[1];

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUESTS: {
                if (!(grantResults.length > 0 && writeStorage == granted && recordAudio == granted)) {
                    if (!(writeStorage == granted)) Toast.makeText(this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                    if (!(recordAudio == granted)) Toast.makeText(this, getString(R.string.audio_permission_denied), Toast.LENGTH_LONG).show();
                } else {
                    preferenceManager.setPermissionSensors(this);
                }
            }
        }
    }
}