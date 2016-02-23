package com.meerkats.familyshopper.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.util.Settings;

/**
 * Created by Rez on 23/02/2016.
 */
public class DiagnosticsActivity extends AppCompatActivity {
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
}
