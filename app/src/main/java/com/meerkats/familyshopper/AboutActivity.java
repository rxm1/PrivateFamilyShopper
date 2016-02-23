package com.meerkats.familyshopper;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.meerkats.familyshopper.util.*;

/**
 * Created by Rez on 23/02/2016.
 */
public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTheme(Settings.getColorTheme());
        setContentView(R.layout.about);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(com.meerkats.familyshopper.R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("About");

        TextView versionCodeTextView = (TextView) findViewById(R.id.version_code);
        TextView versionNameTextView = (TextView) findViewById(R.id.version_name);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCodeTextView.setText("Version Code: " + pInfo.versionCode);
            versionNameTextView.setText("Version Name: " + pInfo.versionName);
        }
        catch (Exception e){
            FSLog.error("about", "AboutActivity onCreate", e);
        }
    }


    public void diagnosticsClick(View view){
        Intent intent = new Intent(this, DiagnosticsActivity.class);
        startActivity(intent);
    }
}
