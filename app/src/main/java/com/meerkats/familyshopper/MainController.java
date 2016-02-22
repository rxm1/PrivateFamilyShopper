package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.dialogs.EditShoppingItemDialog;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;
import com.meerkats.familyshopper.util.Diagnostics;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.Settings;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Rez on 07/01/2016.
 */
public class MainController {
    Firebase myFirebaseRef;
    Activity activity;
    DataHelper dataHelper;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String master_shopping_list_name = "Master_List";
    public static final String activity_tag = "meerkats_MainActivity";


    HandlerThread handlerThread;
    SyncHandler syncHandler;
    Handler mainUIHandler;
    Handler mainControllerHandler;
    public static final String settings_changed_action = "com.meerkats.familyshopper.MainController.SettingsUpdated";
    public static final String reconnect_to_firebase_action = "com.meerkats.familyshopper.MainController.ReconnectToFirebaseAction";
    public static final String disconnect_from_firebase_action = "com.meerkats.familyshopper.MainController.DisconnectFromFirebaseAction";
    Timer timer = new Timer();
    SharedPreferences settings;

    class SyncHandler extends Handler {
        public SyncHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            NotificationEvents occuredNoticifications = new NotificationEvents();
            HashMap<String, String> map = (HashMap<String, String>) ((DataSnapshot)msg.obj).getValue();
            ShoppingList remoteList = new ShoppingList();
            if (map != null) {
                remoteList.loadShoppingList(map.get("masterList"));
                Diagnostics.saveLastSyncedBy(activity, remoteList);
            }

            ShoppingList mergedList = dataHelper.merge(remoteList, shoppingList, occuredNoticifications);
            if(occuredNoticifications.isTrue()) {
                dataHelper.saveShoppingListToStorage(mergedList);
                shoppingList.loadShoppingList(mergedList.getJson());
                mainUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainController.this.shoppingListAdapter.notifyDataSetChanged();
                        ((MainActivity)activity).setIsEditing(false);
                    }
                });
            }
            else {
                ((MainActivity)activity).setIsEditing(false);
                ((MainActivity)activity).loadLocalShoppingList();
            }

        }
    }



    public MainController(Activity mainActivity, HandlerThread handlerThread) {
        FSLog.verbose(activity_tag, "MainController constructor");

        this.activity = mainActivity;
        Firebase.setAndroidContext(activity);
        shoppingList = new ShoppingList("mainList");
        dataHelper = new DataHelper(mainActivity, handlerThread, activity_tag);
        this.handlerThread = handlerThread;
    }

    public void init(){
        FSLog.verbose(activity_tag, "MainController init");

        syncHandler = new SyncHandler(handlerThread.getLooper());
        mainUIHandler = new Handler(Looper.getMainLooper());
        mainControllerHandler = new Handler(handlerThread.getLooper());
        settings = PreferenceManager.getDefaultSharedPreferences(activity);

        shoppingList = new ShoppingList(master_shopping_list_name);
        shoppingListAdapter = new ShoppingListAdapter(activity, shoppingList);
        shoppingListAdapter.notifyDataSetChanged();
        mainControllerHandler.post(new Runnable() {
            @Override
            public void run() {
                dataHelper.instanciateFirebase(false);
                myFirebaseRef = dataHelper.getMyFirebaseRef();
                sync(false, false, false);
            }
        });
    }

    public void deleteShoppingListItem(int position){
        FSLog.verbose(activity_tag, "MainController deleteShoppingListItem");

        shoppingList.markAsDeleted(position);
        sync(true, false, true);
        ((MainActivity) activity).setIsEditing(false);
    }
    public void editShoppingListItem(final int position, final Activity activity){
        FSLog.verbose(activity_tag, "MainController editShoppingListItem");

        final ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);
        EditShoppingItemDialog cdd=new EditShoppingItemDialog(activity, shoppingListItem.getShoppingListItem());

        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (((EditShoppingItemDialog) dialog).isCanceled()) {
                    mainUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) activity).setIsEditing(false);
                            ((MainActivity) activity).loadLocalShoppingList();
                        }
                    });
                    return;
                }


                String newData = ((EditShoppingItemDialog) dialog).getNewData();
                shoppingListItem.setShoppingListItem(newData);
                shoppingList.setShoppingListItemEdit(shoppingListItem, position);

                sync(true, false, true);

                mainUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) activity).setIsEditing(false);
                        ((MainActivity) activity).shoppingListView.setSelection(position);
                    }
                });

            }
        });
        cdd.show();
    }
    public void crossOffShoppingItem(int position){
        FSLog.verbose(activity_tag, "MainController crossOffShoppingItem");

        shoppingList.setItemCrossedOff(position);
        sync(true, false, true);
    }
    public void addItemToShoppingList(String item){
        FSLog.verbose(activity_tag, "MainController addItemToShoppingList");

        shoppingList.add(item);
        sync(true, false, true);
    }
    public void clearShoppingList(){
        FSLog.verbose(activity_tag, "MainController clearShoppingList");

        shoppingList.deleteAll();
        sync(true, false, true);
    }
    public void clearCrossedOffShoppingList(){
        FSLog.verbose(activity_tag, "MainController clearCrossedOffShoppingList");

        shoppingList.clearCrossedOff();
        sync(true, false, true);
    }

/*  Sync is between local shopping list object
    and remote saved storage.
    After merge, it updates all local object and file
    and remote storage
 */
    public synchronized void sync(boolean refresh, boolean showConnectionStatus, boolean withTimer){
        FSLog.verbose(activity_tag, "MainController sync");

        if(refresh){
            shoppingListAdapter.notifyDataSetChanged();
        }
        if(myFirebaseRef != null && DataHelper.getIsValidFirebaseURL()) {
            if (withTimer) {
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        addListenerForSingleValueEvent();
                    }
                };
                timer.cancel();
                timer.purge();
                timer = new Timer();
                timer.schedule(timerTask, Settings.getPushBatchDelay());
            }
            else {
                timer.cancel();
                timer.purge();
                addListenerForSingleValueEvent();
            }
        }
        else{
            if(showConnectionStatus)
                Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();
        }

    }
    public void addListenerForSingleValueEvent(){
        FSLog.verbose(activity_tag, "MainController addListenerForSingleValueEvent");

        try {
            myFirebaseRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Message message = syncHandler.obtainMessage();
                            message.obj = snapshot;
                            syncHandler.sendMessage(message);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();
                        }

                    });
        }
        catch (Exception e){
            FSLog.error(activity_tag, "MainController addListenerForSingleValueEvent", e);
            Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect(){
        FSLog.verbose(activity_tag, "MainController connect");

        disconnect();
        mainControllerHandler.post(new Runnable() {
            @Override
            public void run() {
                dataHelper.instanciateFirebase(false);
                myFirebaseRef = dataHelper.getMyFirebaseRef();
                try {
                    Thread.sleep(2 * 1000);

                    sync(false, false, false);
                } catch (Exception e) {
                    FSLog.error(activity_tag, "MainController connect", e);
                }

            }
        });
    }
    public void disconnect(){
        dataHelper.setMyFirebaseRefNull();
    }

    public ShoppingList getShoppingList(){ return shoppingList; }
    public ShoppingListAdapter getShoppingListAdapter(){return shoppingListAdapter;}
    public void clearShoppingListFromLocalStorage(){
        FSLog.verbose(activity_tag, "MainController clearShoppingListFromLocalStorage");
        dataHelper.clearShoppingListFromLocalStorage();
    }
    public void cleanUp(){
        FSLog.verbose(activity_tag, "MainController cleanUp");

        handlerThread.quit();
        dataHelper.cleanUp();
    }

    public void about(){
        // Inflate the about message contents
        View messageView = activity.getLayoutInflater().inflate(R.layout.about, null, false);

        // When linking text, force to always use default color. This works
        // around a pressed color state bug.
        //TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
        //int defaultColor = textView.getTextColors().getDefaultColor();
        //textView.setTextColor(defaultColor);
        TextView versionCodeTextView = (TextView) messageView.findViewById(R.id.version_code);
        TextView versionNameTextView = (TextView) messageView.findViewById(R.id.version_name);
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            versionCodeTextView.setText("Version Code: " + pInfo.versionCode);
            versionNameTextView.setText("Version Name: " + pInfo.versionName);
        }
        catch (Exception e){

        }


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }
}
