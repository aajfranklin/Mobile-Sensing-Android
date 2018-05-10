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
     * Sets content view for main user activity and identifies the action bar
     * @param savedInstanceState Bundle: activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Called between onCreate() and onResume()
     * Populates the action bar menu with buttons defined in res.menu folder
     * Ensures the settings menu button displays
     * @param menu The action bar menu, passed in automatically
     * @return boolean: return true for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    /**
     * Starts the settings activity on settings button click
     * @param item the button clicked
     * @return boolean: return true to display the selected options menu
     */
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

    /**
     * Sets sensors available on the device, if not already set
     * Requests write to storage and record audio permissions
     * Assigns button views to toggle button objects
     * Binds sensor service to main activity if running
     */
    @Override
    protected void onResume() {
        super.onResume();

        preferenceManager = new SharedPreferenceManager();
        // Must provide context as 'getApplicationContext()' rather than 'this' to
        // prevent rare leaked intent receiver crash. If permissions dialogue box is opened on resume,
        // battery sensor does not properly unregister its broadcast receiver if context is
        // provided as 'this'
        preferenceManager.setDefaultSensors(getApplicationContext());

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

        if(hasAudioPermission) {
            preferenceManager.setPermissionSensors(this);
        }

        // Set button status based on SensorService status
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);
        pauseButton= findViewById(R.id.pause_button);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);

        Intent intent = new Intent(this, SensorService.class);

        // Bind the service if it is running
        // Also serves to set button states based on sensor service status on bind
        if (sensorServiceIsRunning()) {
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Unbind sensor service on app close
     */
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

    /**
     * Interface for monitoring the state of the sensor service
     * Facilitates access to sensor service methods required for pause button functionality
     */
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
     * Determines behaviour of start, stop, play buttons on click
     * @param v The View clicked
     */
    public void onClick(View v) {

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

    /**
     * Update button state based on the current sensing state
     * @param state The current sensing state: SENSING, STOPPED, PAUSED
     */
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
     * Displays toasts to the user noting reduced app functionality when permission requests are denied
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