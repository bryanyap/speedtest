package me.bryanyap.speedtest.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import me.bryanyap.speedtest.R;
import me.bryanyap.speedtest.constants.ApplicationConstants;

public class AppPreference extends PreferenceActivity implements ApplicationConstants {
    private static final String TAG = "AppPreference";
    private String initialFrequency = "";
    SharedPreferences prefs = null;
    SharedPreferences.OnSharedPreferenceChangeListener listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(FREQUENCY)) {
                    Intent resultIntent = new Intent();
                    if (!sharedPreferences.getString(FREQUENCY, "10").equals(initialFrequency)) {
                        Log.v(TAG, "frequency changed");
                        resultIntent.putExtra(CHANGED, true);
                    } else {
                        Log.v(TAG, "frequency not changed");
                        resultIntent.putExtra(CHANGED, false);
                    }
                    setResult(PreferenceActivity.RESULT_OK, resultIntent);
                }

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        initialFrequency = prefs.getString(FREQUENCY, "10");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.v(TAG, "onBackPressed()");
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

}
