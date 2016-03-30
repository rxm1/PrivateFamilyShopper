package com.meerkats.familyshopper.Settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.meerkats.familyshopper.R;

public class SettingsLoggingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.getColorTheme());
        setContentView(R.layout.settings_with_toolbar);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("Settings");

        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new MyPreferenceFragment()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.settings_logging, false);

    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {

        theme.applyStyle(Settings.getColorTheme(), true);
    }


    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_logging);
        }

        @Override
        public void onPause(){
            super.onPause();
        }

    }



}