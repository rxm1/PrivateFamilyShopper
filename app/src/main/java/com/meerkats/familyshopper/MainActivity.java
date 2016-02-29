package com.meerkats.familyshopper;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.meerkats.familyshopper.dialogs.ContextMenuDialog;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.Settings;
import com.meerkats.familyshopper.model.ShoppingList;

public class MainActivity extends AppCompatActivity {

    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    ListView shoppingListView;
    MainController mainController;
    DataChangedReceiver dataChangedReceiver = new DataChangedReceiver();;
    HandlerThread handlerThread = new HandlerThread("MainActivity.HandlerThread");;
    private static final int SETTINGS_RESULT = 1;
    private boolean isEditing = false;
    boolean mBound = false;
    public static final String activity_log_tag = "meerkats_MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.loadSettings(this);
        FSLog.verbose(activity_log_tag, "MainActivity onCreate");
        setTheme(Settings.getColorTheme());
        setContentView(com.meerkats.familyshopper.R.layout.activity_main);

        handlerThread.start();

        mainController = new MainController(this, handlerThread, activity_log_tag);
        mainController.init();
        shoppingList = mainController.getShoppingList();
        shoppingListAdapter = mainController.getShoppingListAdapter();

        Toolbar myToolbar = (Toolbar) findViewById(com.meerkats.familyshopper.R.id.family_shopper_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(com.meerkats.familyshopper.R.mipmap.ic_launcher);

        shoppingListView = (ListView)findViewById(com.meerkats.familyshopper.R.id.shoppingListView);
        shoppingListView.setAdapter(shoppingListAdapter);

        setShoppingListOnItemClick();
        setShoppingListOnItemLongClick();

        startService(new Intent(this, MainService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        FSLog.verbose(activity_log_tag, "MainActivity onCreateOptionsMenu");

        MenuInflater mi = getMenuInflater();
        mi.inflate(com.meerkats.familyshopper.R.menu.shopping_list_action_items, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FSLog.verbose(activity_log_tag, "MainActivity onOptionsItemSelected");

        switch (item.getItemId()) {
            case com.meerkats.familyshopper.R.id.sync:
                mainController.sync(true, false);
                return true;
            case com.meerkats.familyshopper.R.id.clear_list:
                mainController.clearShoppingList();
                return true;
            case com.meerkats.familyshopper.R.id.clear_crossed_off:
                mainController.clearCrossedOffShoppingList();
                return true;
            case com.meerkats.familyshopper.R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, SETTINGS_RESULT);
                return true;
            case com.meerkats.familyshopper.R.id.about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FSLog.verbose(activity_log_tag, "MainActivity onActivityResult");

        switch (requestCode) {
            case SETTINGS_RESULT:
                Settings.loadSettings(this);

                if(Settings.disconnectFromFirbase()){
                    mainController.disconnect();

                    Intent intent = new Intent(MainController.disconnect_from_firebase_action);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
                else if (Settings.reconnectToFirebase() || (Settings.connectToFirebase())){
                    if (Settings.reconnectToFirebase()) {
                        mainController.clearShoppingListFromLocalStorage();
                    }

                    mainController.connect();
                    Intent intent = new Intent(MainController.reconnect_to_firebase_action);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
                else {
                    Intent intent = new Intent(MainController.settings_changed_action);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
                Settings.setDisconnectFromFirebase(false);
                Settings.setReconnectToFirebase(false);
                Settings.setConnectToFirebase(false);

                setTheme(Settings.getColorTheme());
                break;
        }
    }

    private void setShoppingListOnItemLongClick(){
        FSLog.verbose(activity_log_tag, "MainActivity setShoppingListOnItemLongClick");

        shoppingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, final long id) {
                setIsEditing(true);

                ContextMenuDialog contextMenuDialog = new ContextMenuDialog(MainActivity.this, shoppingList.getShoppingListItem(position).isCrossedOff(), mainController, position, shoppingListView);
                contextMenuDialog.setTitle(shoppingList.getShoppingListItem(position).getShoppingListItem());
                contextMenuDialog.setCancelable(true);
                contextMenuDialog.setCanceledOnTouchOutside(true);
                contextMenuDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setIsEditing(false);
                        mainController.loadLocalShoppingList();
                    }
                });
                contextMenuDialog.show();

                return true;
            }
        });

    }

    private void setShoppingListOnItemClick(){
        FSLog.verbose(activity_log_tag, "MainActivity setShoppingListOnItemClick");

        shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mainController.crossOffShoppingItem(position);
            }
        });
    }

    public void addBtnClick(View view){
        FSLog.verbose(activity_log_tag, "MainActivity addBtnClick");
        EditText enterItemEditTxt = (EditText)findViewById(com.meerkats.familyshopper.R.id.enterItemTxt);
        if (enterItemEditTxt.getText().toString().trim().length() > 0)
            mainController.addItemToShoppingList(enterItemEditTxt.getText().toString().trim());
        shoppingListView.setSelection(shoppingListAdapter.getCount() - 1);
        enterItemEditTxt.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        FSLog.verbose(activity_log_tag, "MainActivity onResume");

        IntentFilter filter = new IntentFilter(DataHelper.service_updated_file_action);
        LocalBroadcastManager.getInstance(this).registerReceiver(dataChangedReceiver, filter);

        mainController.loadLocalShoppingList();
    }
    @Override
    protected void onPause() {
        super.onPause();
        FSLog.verbose(activity_log_tag, "MainActivity onPause");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataChangedReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FSLog.verbose(activity_log_tag, "MainActivity onStart");

        Intent intent = new Intent(getApplicationContext(), MainService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            FSLog.verbose(activity_log_tag, "MainActivity onServiceConnected");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MainService.LocalBinder binder = (MainService.LocalBinder) service;
            MainService mainService = binder.getService();
            mBound = true;

            mainController.setMainService(mainService, mBound);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            FSLog.verbose(activity_log_tag, "MainActivity onServiceDisconnected");

            mBound = false;
            mainController.setMainService(null, mBound);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        FSLog.verbose(activity_log_tag, "MainActivity onStop");

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        FSLog.verbose(activity_log_tag, "MainActivity onDestroy");

        mainController.cleanUp();
    }

    // Define the callback for what to do when message is received
    public class DataChangedReceiver extends BroadcastReceiver {
          @Override
        public void onReceive(Context context, Intent intent) {
              FSLog.verbose(activity_log_tag, "MainActivity onReceive");

              mainController.loadLocalShoppingList();
        }
    }

    public void setIsEditing(boolean isEditing){
        FSLog.verbose(activity_log_tag, "MainActivity setIsEditing");

        this.isEditing=isEditing;
    }
    public boolean isEditing(){
        FSLog.verbose(activity_log_tag, "MainActivity isEditing");

        return isEditing;
    }

}
