package com.meerkats.familyshopper;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.meerkats.familyshopper.util.Settings;
import com.meerkats.familyshopper.model.ShoppingList;

public class MainActivity extends AppCompatActivity {

    EditText enterItemEditTxt;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    ListView shoppingListView;
    MainController mainController;
    DataChangedReceiver dataChangedReceiver;
    Handler mainActivityHandler;
    Handler mainUIHandler;
    HandlerThread handlerThread;
    private static final int SETTINGS_RESULT = 1;
    private boolean isEditing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.meerkats.familyshopper.R.layout.activity_main);
        Settings.loadSettings(this);

        handlerThread = new HandlerThread("MainActivity.HandlerThread");
        handlerThread.start();
        mainActivityHandler = new Handler(handlerThread.getLooper());
        mainUIHandler = new Handler(Looper.getMainLooper());

        mainController = new MainController(this, handlerThread);
        mainController.init();
        shoppingList = mainController.getShoppingList();
        shoppingListAdapter = mainController.getShoppingListAdapter();
        dataChangedReceiver = new DataChangedReceiver();


        Toolbar myToolbar = (Toolbar) findViewById(com.meerkats.familyshopper.R.id.family_shopper_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(com.meerkats.familyshopper.R.mipmap.ic_launcher);

        enterItemEditTxt = (EditText)findViewById(com.meerkats.familyshopper.R.id.enterItemTxt);
        shoppingListView = (ListView)findViewById(com.meerkats.familyshopper.R.id.shoppingListView);
        shoppingListView.setAdapter(shoppingListAdapter);

        setShoppingListOnItemClick();
        setShoppingListOnItemLongClick();

        Intent intent = new Intent(this, MainService.class);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(com.meerkats.familyshopper.R.menu.shopping_list_action_items, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.meerkats.familyshopper.R.id.sync:
                mainController.sync(false, true, false);
                return true;
            case com.meerkats.familyshopper.R.id.clear_list:
                mainController.clearShoppingList();
                return true;
            case com.meerkats.familyshopper.R.id.clear_crossed_off:
                mainController.clearCrossedOffShoppingList();
                return true;
            case com.meerkats.familyshopper.R.id.settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, SETTINGS_RESULT);
                return true;
            case com.meerkats.familyshopper.R.id.about:
                mainController.about();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETTINGS_RESULT:
                Settings.loadSettings(this);
                mainController.clearShoppingListFromLocalStorage();
                mainController.connect();

                //notify service that settings have changed
                Intent intent = new Intent(MainController.settings_updated_action);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                break;
        }
    }

    private void setShoppingListOnItemLongClick(){

        shoppingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, final long id) {
                setIsEditing(true);
                int contextMenuID = ((int) com.meerkats.familyshopper.R.array.shoppingListContextMenuValues);
                if (shoppingList.getShoppingListItem(position).isCrossedOff())
                    contextMenuID = ((int) com.meerkats.familyshopper.R.array.shoppingListContextMenuValuesDeleteOnly);

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, com.meerkats.familyshopper.R.style.ListContextMenu));
                builder.setTitle(shoppingList.getShoppingListItem(position).getShoppingListItem()).setCancelable(true).setItems(contextMenuID,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int index) {
                                switch (index) {
                                    case 0:
                                        mainController.deleteShoppingListItem(position);
                                        break;
                                    case 1:
                                        mainController.editShoppingListItem(parent, v, position, id, MainActivity.this);
                                        shoppingListView.setSelection(position);
                                        break;
                                }
                            }
                        }
                ).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        loadLocalShoppingList();
                        setIsEditing(false);
                    }
                }).show();
                return true;
            }
        });

    }

    private void setShoppingListOnItemClick(){
        shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mainController.crossOffShoppingItem(position);
            }
        });
    }

    public void addBtnClick(View view){
        if (enterItemEditTxt.getText().toString().trim().length() > 0)
            mainController.addItemToShoppingList(enterItemEditTxt.getText().toString().trim());
        shoppingListView.setSelection(shoppingListAdapter.getCount() - 1);
        enterItemEditTxt.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(DataHelper.service_updated_file_action);
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangedReceiver, filter);

        loadLocalShoppingList();
    }
    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataChangedReceiver);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

        mainController.cleanUp();
    }

    // Define the callback for what to do when message is received
    public class DataChangedReceiver extends BroadcastReceiver {
          @Override
        public void onReceive(Context context, Intent intent) {
              loadLocalShoppingList();
        }
    }

    public void loadLocalShoppingList(){
        mainActivityHandler.post(new Runnable() {
            @Override
            public void run() {
                if(isEditing())
                    return;

                String localFile = mainController.dataHelper.loadGsonFromLocalStorage().trim();
                if(!localFile.isEmpty())
                    shoppingList.loadShoppingList(localFile);
                mainUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.shoppingListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public void setIsEditing(boolean isEditing){ this.isEditing=isEditing; }
    public boolean isEditing(){return isEditing;}

    public void diagnosticsClick(View view){

    }
}
