package com.meerkats.familyshopper.Settings;


import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.meerkats.familyshopper.MainActivity;
import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.util.FSLog;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.loadSettings(this, MainActivity.activity_log_tag);
        FSLog.verbose(MainActivity.activity_log_tag, "SettingsActivity onCreate");

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
            FSLog.verbose(MainActivity.activity_log_tag, "MyPreferenceFragment onCreate");
            super.onCreate(savedInstanceState);

            try {
                addPreferencesFromResource(com.meerkats.familyshopper.R.xml.settings);

                bindPreferenceSummaryToValue(findPreference(Settings.Integrate_With_Firebase_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Firebase_URL_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.firebase_authentication_name));
                bindPreferenceSummaryToValue(findPreference(Settings.firebase_email_name));
                bindPreferenceSummaryToValue(findPreference(Settings.firebase_password_name));

                bindPreferenceSummaryToValue(findPreference(Settings.Notification_Frequency_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Notification_Events_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.Push_Batch_Time_Name));

                bindPreferenceSummaryToValue(findPreference(Settings.sort_by_name));
                bindPreferenceSummaryToValue(findPreference(Settings.crossed_off_items_at_bottom_name));
                bindPreferenceSummaryToValue(findPreference(Settings.screen_orientation_name));
                bindPreferenceSummaryToValue(findPreference(Settings.Color_Theme_Name));
                bindPreferenceSummaryToValue(findPreference(Settings.vibration_name));

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

            // Trigger the listener immediately with the preference's current value
            onPreferenceChange(preference,
                    Settings.getPreferenceKey(preference));
        }

        @Override
        public boolean onPreferenceChange (Preference preference, Object newValue){
            FSLog.verbose(MainActivity.activity_log_tag, "MyPreferenceFragment onPreferenceChange");
            if(preference.getKey().equals(Settings.Integrate_With_Firebase_Name) ||
                    preference.getKey().equals(Settings.Firebase_URL_Name) ||
                    preference.getKey().equals(Settings.Color_Theme_Name) ||
                    preference.getKey().equals(Settings.firebase_authentication_name) ||
                    preference.getKey().equals(Settings.screen_orientation_name) ||
                    preference.getKey().equals(Settings.firebase_email_name) ||
                    preference.getKey().equals(Settings.firebase_password_name))
                setConnectionSettings(preference, newValue);

            if(preference.getKey().equals(Settings.Integrate_With_Firebase_Name) ||
                    preference.getKey().equals(Settings.crossed_off_items_at_bottom_name) ||
                    preference.getKey().equals(Settings.screen_orientation_name))
                return true;

            if(preference.getKey().equals(Settings.firebase_password_name))
                newValue = newValue.equals("") ? "Please enter the Firebase email password.":"Password entered";
            if(preference.getKey().equals(Settings.Firebase_URL_Name))
                newValue = newValue.equals("https://.com")||newValue.equals("") ? "Please enter the Firebase URL.":newValue;
            if(preference.getKey().equals(Settings.firebase_email_name))
                newValue = newValue .equals("") ? "Please enter the Firebase email.":newValue;
            if(preference.getKey().equals(Settings.Push_Batch_Time_Name))
                newValue=newValue.toString()+"s. Batches up changes to push. Increase delay to reduce data usage.";
            if(preference.getKey().equals(Settings.Notification_Frequency_Name))
                newValue=newValue.toString()+"s. Increase value to reduce notification frequency.";

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in the preference's 'entries' list (since they have separate labels/values).

                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(newValue.toString());
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(newValue.toString());
            }

            return true;
        }

        private void setConnectionSettings(Preference preference, Object newValue){
            FSLog.verbose(MainActivity.activity_log_tag, "MyPreferenceFragment setConnectionSettings");

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
            if(preference.getKey().equals(Settings.Color_Theme_Name)) {
                Settings.setRestartActivity(!Settings.getColorThemeString().equals(stringValue));
            }
            if(preference.getKey().equals(Settings.screen_orientation_name)) {
                if (!Settings.isPortraitOrientation() && stringValue.equals("true")
                        || Settings.isPortraitOrientation() && stringValue.equals("false")) {
                    Settings.setRestartActivity(true);
                }
            }
            if(preference.getKey().equals(Settings.firebase_authentication_name)) {
                Settings.setReconnectToFirebase(Settings.getFirebaseAuthentication() != (Settings.FirebaseAuthentication.values()[Integer.parseInt(stringValue)]));
            }
            if(preference.getKey().equals(Settings.firebase_email_name)) {
                Settings.setReconnectToFirebase(!Settings.getFirebaseEmail().equals(stringValue));
            }
            if(preference.getKey().equals(Settings.firebase_password_name)) {
                Settings.setReconnectToFirebase(!Settings.getFirebasePassword().equals(stringValue));
            }
        }
        @Override
        public void onPause(){
            super.onPause();
        }

    }



}