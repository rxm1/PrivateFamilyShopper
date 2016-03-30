package com.meerkats.familyshopper.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.meerkats.familyshopper.MainActivity;
import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.Settings.Settings;

/**
 * Created by Rez on 15/02/2016.
 */
public class ContextMenuDialog extends Dialog {

    private boolean isCrossedOff = false;
    Activity activity;
    MainController mainController;
    int itemOriginalPosition;
    ListView shoppingListView;

    public ContextMenuDialog(Activity activity, boolean isCrossedOff, MainController mainController, int itemOriginalPosition, ListView shoppingListView){
        super(activity, Settings.getDialogColorTheme());
        FSLog.verbose(MainActivity.activity_log_tag, "ContextMenuDialog ContextMenuDialog");

        this.isCrossedOff = isCrossedOff;
        this.activity = activity;
        this.mainController = mainController;
        this.itemOriginalPosition = itemOriginalPosition;
        this.shoppingListView = shoppingListView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        FSLog.verbose(MainActivity.activity_log_tag, "ContextMenuDialog onCreate");

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
}
