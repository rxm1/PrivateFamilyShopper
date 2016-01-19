package com.meerkats.familyshopper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

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
    public static final String Firebase_URL_Name = "FirebaseURLName";
    public static final String Integrate_With_Firebase_Name = "IntegrateFirebase";
    public static final String Notification_Frequency_Name = "notificationFrequency";
    public static final String Notification_Events_Name = "notificationEvents";
    public static final String Push_Batch_Time_Name = "pushBatchTime";
    public static final String master_shopping_list_name = "Master_List";

    HandlerThread handlerThread;
    SyncHandler syncHandler;
    Handler mainUIHandler;
    Handler mainControllerHandler;
    public static final String settings_updated_action = "com.meerkats.familyshopper.MainController.SettingsUpdated";
    Timer timer = new Timer();
    SharedPreferences settings;

    class SyncHandler extends Handler {
        public SyncHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            DataSnapshot snapshot = (DataSnapshot)msg.obj;
            NotificationEvents occuredNoticifications = new NotificationEvents();

            ShoppingList mergedList = dataHelper.merge(snapshot, shoppingList, occuredNoticifications);
            if(occuredNoticifications.isTrue()) {
                String mergedData = mergedList.getJson();
                dataHelper.saveShoppingListToStorage(mergedData);
                shoppingList.loadShoppingList(mergedData);
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
        this.activity = mainActivity;
        Firebase.setAndroidContext(activity);
        shoppingList = new ShoppingList("mainList");
        dataHelper = new DataHelper(mainActivity, handlerThread);
        this.handlerThread = handlerThread;
    }

    public void init(){
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
        shoppingList.markAsDeleted(position);
        sync(true, false, true);
    }
    public void editShoppingListItem(final AdapterView<?> parent, final View v, final int position, long id, final Activity activity){
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
            }
        });
        cdd.show();
    }
    public void crossOffShoppingItem(int position){
        shoppingList.setItemCrossedOff(position);
        sync(true, false, true);
    }
    public void addItemToShoppingList(String item){
        shoppingList.add(item);
        sync(true, false, true);
    }
    public void clearShoppingList(){
        shoppingList.deleteAll();
        sync(true, false, true);
    }
    public void clearCrossedOffShoppingList(){
        shoppingList.clearCrossedOff();
        sync(true, false, true);
    }

/*  Sync is between local shopping list object
    and remote saved storage.
    After merge, it updates all local object and file
    and remote storage
 */
    public synchronized void sync(boolean refresh, boolean showConnectionStatus, boolean withTimer){
        if(refresh){
            shoppingListAdapter.notifyDataSetChanged();
        }
        if(myFirebaseRef != null && DataHelper.getIsValidFirebaseURL()) {
            if (withTimer) {
                String temp = settings.getString(Push_Batch_Time_Name, "0").trim();
                int pushBatchTime = temp==""?1:Integer.valueOf(temp);
                pushBatchTime=pushBatchTime<1?1:pushBatchTime;
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        addListenerForSingleValueEvent();
                    }
                };
                timer.cancel();
                timer.purge();
                timer = new Timer();
                timer.schedule(timerTask, pushBatchTime * 1000);
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
            Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect(){
        dataHelper.setMyFirebaseRefNull();
        mainControllerHandler.post(new Runnable() {
            @Override
            public void run() {
                dataHelper.instanciateFirebase(false);
                myFirebaseRef = dataHelper.getMyFirebaseRef();
                try {
                    Thread.sleep(2*1000);

                    sync(false, false, false);
                }
                catch (Exception e){
                    Log.e("Exception", "Sync failed: " + e.toString());
                }

            }
        });
    }

    public ShoppingList getShoppingList(){ return shoppingList; }
    public ShoppingListAdapter getShoppingListAdapter(){return shoppingListAdapter;}
    public void clearShoppingListFromLocalStorage(){
        dataHelper.clearShoppingListFromLocalStorage();
    }
    public void cleanUp(){
        handlerThread.quit();
        dataHelper.cleanUp();
    }
}
