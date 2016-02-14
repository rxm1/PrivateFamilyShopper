package com.meerkats.familyshopper;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.meerkats.familyshopper.util.Settings;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.getColorTheme());
        setContentView(R.layout.settings_with_toolbar);

        Toolbar myToolbar = (Toolbar) findViewById(com.meerkats.familyshopper.R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(com.meerkats.familyshopper.R.mipmap.ic_launcher);

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
            addPreferencesFromResource(com.meerkats.familyshopper.R.xml.settings);

            bindPreferenceSummaryToValue(findPreference(Settings.Firebase_URL_Name));
            bindPreferenceSummaryToValue(findPreference(Settings.Notification_Frequency_Name));
            bindPreferenceSummaryToValue(findPreference(Settings.Push_Batch_Time_Name));
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
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        @Override
        public boolean onPreferenceChange (Preference preference, Object newValue){
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
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
            }

            return true;
        }

        @Override
        public void onPause(){
            super.onPause();
        }

    }



}