package com.meerkats.familyshopper.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.SettingsLoggingActivity;
import com.meerkats.familyshopper.util.Settings;

/**
 * Created by Rez on 23/02/2016.
 */
public class DiagnosticsActivity extends AppCompatActivity {
    private static final int DIAGONOSTICS_SETTINGS_RESULT = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.getColorTheme());
        setContentView(R.layout.diagnostics);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.diagnostics_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(com.meerkats.familyshopper.R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("Diagnostics");

        TextView currentDeviceIDTextView = (TextView) findViewById(R.id.current_device_ID_textview);
        currentDeviceIDTextView.setText("Current Device ID: " +
                android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));

        ListView listView = (ListView) findViewById(R.id.diagnostics_listView);
        // create the grid item mapping
        String[] from = new String[] {"Device ID", "Last Seen"};
        int[] to = new int[] { R.id.shoppingListMemberName, R.id.shoppingListMemberLastSeen };
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, Diagnostics.getShoppingListMembers(this), R.layout.diagnostics_list_member, from, to);
        listView.setAdapter(simpleAdapter);

    }

    public void diagnosticsSettingsClick(View view){
        Intent intent = new Intent(this, SettingsLoggingActivity.class);
        startActivityForResult(intent, DIAGONOSTICS_SETTINGS_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case DIAGONOSTICS_SETTINGS_RESULT:
                Settings.loadSettings(this);
                //notify service that settings have changed
                Intent intent = new Intent(MainController.settings_changed_action);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }


}
