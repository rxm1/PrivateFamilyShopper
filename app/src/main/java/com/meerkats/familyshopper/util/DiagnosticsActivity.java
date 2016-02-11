package com.meerkats.familyshopper.util;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.model.ShoppingListMembers;
import com.meerkats.familyshopper.util.Diagnostics;

public class DiagnosticsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostics);

        ArrayAdapter<ShoppingListMembers.ShoppingListMember> arrayAdapter = new ArrayAdapter<ShoppingListMembers.ShoppingListMember>(this, R.layout.diagnostics_list_member, Diagnostics.getShoppingListMembers().getShoppingListMembers());
        ListView listView = (ListView)findViewById(R.id.diagnostics_list);
        listView.setAdapter(arrayAdapter);
    }
}
