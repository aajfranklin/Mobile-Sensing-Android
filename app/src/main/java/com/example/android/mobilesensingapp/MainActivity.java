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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private enum SensingStateValues {SENSING, PAUSED, STOPPED}
    private SensingStateValues sensingState;

    /**
     * Sets content view for main user activity
     * Sets available sensors
     * Makes write to storage permission request if necessary
     * Sets toggle switch position and text on app start/resume
     * @param savedInstanceState Bundle: activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                this.startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        preferenceManager = new SharedPreferenceManager();
        // Must provide context as 'getApplicationContext()' rather than 'this' to
        // prevent rare leaked intent receiver crash. If permissions dialogue box is opened on resume,
        // battery sensor does not properly unregister its broadcast receiver if context is
        // provided as 'this'
        preferenceManager.setDefaultSensors(getApplicationContext());

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
        super.onStop();

        if (sensorServiceIsRunning()) {
            if (bound) {
                unbindService(connection);
                bound = false;
            }
        }
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sService = binder.getService();
            bound = true;
            if (sService.isSensing()) {
                sensingState = SensingStateValues.SENSING;
                updateButtonStates(sensingState);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    /**
     * Determines behaviour of toggle switch on click
     * Starts sensor service if it is not currently active
     * Stops sensor service if it is currently active
     * @param v The View clicked
     */
    public void onClick(View v) {

        // An Intent is a passive data structure holding a a description of an action to be performed
        Intent intent = new Intent(this, SensorService.class);

        switch (v.getId()) {
            case R.id.stop_button:
                unbindService(connection);
                bound = false;
                stopService(intent);
                sensingState = SensingStateValues.STOPPED;
                break;
            case R.id.pause_button:
                sService.pauseSensing();
                sensingState = SensingStateValues.PAUSED;
                break;
            case R.id.start_button:
                if (sensorServiceIsRunning()) {
                    sService.resumeSensing();
                } else {
                    startService(intent);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                }
                sensingState = SensingStateValues.SENSING;
                break;
        }
        updateButtonStates(sensingState);
    }

    public void updateButtonStates(SensingStateValues state) {
        switch (state) {
            case STOPPED:
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                pauseButton.setEnabled(false);
                break;
            case PAUSED:
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                pauseButton.setEnabled(false);
                break;
            case SENSING:
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                pauseButton.setEnabled(true);
                break;
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