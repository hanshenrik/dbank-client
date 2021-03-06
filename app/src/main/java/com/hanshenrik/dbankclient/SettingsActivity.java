package com.hanshenrik.dbankclient;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.MenuItem;


import java.util.List;


public class SettingsActivity extends PreferenceActivity {
    private static final boolean ALWAYS_SIMPLE_PREFS = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == android.R.id.home || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        final SecurePreferences preferences = new SecurePreferences(getBaseContext(),
                MainActivity.DBANK_PREFERENCES, MainActivity.KEY, true);

        final EditTextPreference ipAddress = (EditTextPreference) findPreference(getString(R.string.pref_key_ip));
        final EditTextPreference portNumber = (EditTextPreference) findPreference(getString(R.string.pref_key_port));
        final EditTextPreference username = (EditTextPreference) findPreference(getString(R.string.pref_key_username));
        final EditTextPreference password = (EditTextPreference) findPreference(getString(R.string.pref_key_password));

        ipAddress.setSummary(preferences.getString(getString(R.string.pref_key_ip)));
        portNumber.setSummary(preferences.getString(getString(R.string.pref_key_port)));
        username.setSummary(preferences.getString(getString(R.string.pref_key_username)));
        password.setText(preferences.getString(getString(R.string.pref_key_password)));

        ipAddress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ipAddress.setSummary((String) newValue);
                preferences.put(getString(R.string.pref_key_ip), (String) newValue);
                return false;
            }
        });

        portNumber.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                portNumber.setSummary((String) newValue);
                preferences.put(getString(R.string.pref_key_port), (String) newValue);
                return false;
            }
        });

        username.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                username.setSummary((String) newValue);
                preferences.put(getString(R.string.pref_key_username), (String) newValue);
                return false;
            }
        });

        password.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preferences.put(getString(R.string.pref_key_password), (String) newValue);
                return false;
            }
        });
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }
}
