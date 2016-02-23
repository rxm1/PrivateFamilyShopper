package com.meerkats.familyshopper.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.meerkats.familyshopper.MainActivity;
import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.util.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

/**
 * Created by Rez on 15/02/2016.
 */
public class ContextMenuDialog extends Dialog implements android.view.View.OnClickListener {

    private boolean isCrossedOff = false;
    Activity activity;
    MainController mainController;
    int itemOriginalPosition;
    ListView shoppingListView;

    public ContextMenuDialog(Activity activity, boolean isCrossedOff, MainController mainController, int itemOriginalPosition, ListView shoppingListView){
        super(activity, Settings.getDialogColorTheme());

        this.isCrossedOff = isCrossedOff;
        this.activity = activity;
        this.mainController = mainController;
        this.itemOriginalPosition = itemOriginalPosition;
        this.shoppingListView = shoppingListView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.context_menu);

        String[] data = new String[]{"Delete"};
        if(!isCrossedOff)
            data = new String[]{"Delete", "Edit"};

        ArrayAdapter arrayAdapter = new ArrayAdapter(activity, R.layout.context_menu_row, R.id.contextMenuTextView, data);
        ListView listView = (ListView)findViewById(R.id.context_menu_listView);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mainController.deleteShoppingListItem(itemOriginalPosition);
                        break;
                    case 1:
                        mainController.editShoppingListItem(itemOriginalPosition, activity);
                        break;
                }
                dismiss();
            }
        });
    }


    @Override
    public void onClick(View view){

    }
}