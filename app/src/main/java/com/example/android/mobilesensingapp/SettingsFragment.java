package com.example.android.mobilesensingapp;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import java.util.Map;

/**
 * Class defining the contents of the settings menu
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        // Resource does not specify menu items as have chosen to populate it programmatically below
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen screen = this.getPreferenceScreen();
        PreferenceCategory category = new PreferenceCategory(screen.getContext());
        category.setTitle("Enabled Sensors");
        screen.addPreference(category);

        SharedPreferenceManager preferenceManager = new SharedPreferenceManager();

        // Populate the settings menu with list of all sensors supported by the app
        for (final Map.Entry<String, ?> entry : preferenceManager.getAllSensors().entrySet()) {
            // Create check box for each sensor
            CheckBoxPreference preference = new CheckBoxPreference(screen.getContext());
            preference.setKey(entry.getKey());
            preference.setTitle(entry.getKey());

            // Disable checkbox for sensors that are not supported, with explanatory message
            if (!preferenceManager.getCompatibleSensors(getActivity()).containsKey(entry.getKey())) {
                preference.setEnabled(false);
                preference.setSummary("This sensor is not available on your device");
            } else if (preferenceManager.getEnabledSensors(getActivity()).containsKey(entry.getKey())) {
                preference.setChecked(true);
            }
            category.addPreference(preference);

            // Set checkbox click behaviour
            preference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            SharedPreferenceManager preferenceManager = new SharedPreferenceManager();
                            for (Map.Entry<String, ?> existingEntry : preferenceManager.getEnabledSensors(getActivity()).entrySet()) {
                                if (existingEntry.getKey().equals(entry.getKey())) {
                                    preferenceManager.changeSensorStatus(entry.getKey(), getActivity());
                                }
                            }
                            return true;
                        }
                    }
            );
        }
    }
}