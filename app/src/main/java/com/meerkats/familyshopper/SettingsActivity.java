package com.meerkats.familyshopper;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.Settings;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Settings.getColorTheme());
        setContentView(R.layout.settings_with_toolbar);
        if(Settings.isPortraitOrientation())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        Toolbar myToolbar = (Toolbar) findViewById(com.meerkats.familyshopper.R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(com.meerkats.familyshopper.R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("Settings");

        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new MyPreferenceFragment()).commit();
        PreferenceManager.setDefaultValues(this, com.meerkats.familyshopper.R.xml.settings, false);

    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {

        theme.applyStyle(Settings.getColorTheme(), true);
    }


    public static class MyPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            try {
                addPreferencesFromResource(com.meerkats.familyshopper.R.xml.settings);

                bindPreferenceSummaryToValue(findPreference(Settings.Firebase_URL_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Notification_Frequency_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Push_Batch_Time_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Integrate_With_Firebase_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Notification_Frequency_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Notification_Events_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Color_Theme_Name));
            }catch (Exception e){
                FSLog.error(MainActivity.activity_log_tag, "MyPreferenceFragment onCreate", e);
                Settings.clearSettings(getActivity(), MainActivity.activity_log_tag);
                Toast.makeText(getActivity(), "Settings cleared.", Toast.LENGTH_SHORT).show();
            }
        }


        /**
         * Attaches a listener so the summary is always updated with the preference value.
         * Also fires the listener once, to initialize the summary (so it shows up before the value
         * is changed.)
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            // Trigger the listener immediately with the preference's
            // current value.
            //onPreferenceChange(preference,
              //      PreferenceManager
                //            .getDefaultSharedPreferences(preference.getContext())
                  //          .getString(preference.getKey(), ""));
        }

        @Override
        public boolean onPreferenceChange (Preference preference, Object newValue){
            String stringValue = newValue.toString();

            if(preference.getKey().equals(Settings.Integrate_With_Firebase_Name)) {
                Settings.setDisconnectFromFirebase(stringValue.equals("false")
                        && Settings.isIntegrateFirebase());

                Settings.setConnectToFirebase(stringValue.equals("true")
                        && !Settings.isIntegrateFirebase());

            }
            if(preference.getKey().equals(Settings.Firebase_URL_Name)) {
                Settings.setConnectToFirebase(Settings.getFirebaseURL().equals("")
                        && !stringValue.equals(""));
                Settings.setReconnectToFirebase(!Settings.getFirebaseURL().equals("")
                        && !Settings.getFirebaseURL().equals(stringValue));
            }

            /*if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }*/

            return true;
        }

        @Override
        public void onPause(){
            super.onPause();
        }

    }



}