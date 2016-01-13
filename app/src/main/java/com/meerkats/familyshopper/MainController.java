package com.meerkats.familyshopper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;


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
    HandlerThread handlerThread;
    SyncHandler syncHandler;
    Handler mainUIHandler;
    Handler mainControllerHandler;
    public static final String firebase_ui_updated_action = "com.meerkats.familyshopper.MainController.FirebaseUI";

    class SyncHandler extends Handler {
        public SyncHandler(Looper myLooper) {
            super(myLooper);
        }
        public void handleMessage(Message msg) {
            DataSnapshot snapshot = (DataSnapshot)msg.obj;
            String localData = shoppingList.getJson();
            String mergedData = dataHelper.merge(snapshot, localData);
            if (!mergedData.trim().isEmpty()) {
                dataHelper.saveShoppingListToStorage(mergedData);
                shoppingList.loadShoppingList(mergedData);
                mainUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainController.this.shoppingListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }


    public MainController(Activity mainActivity) {
        this.activity = mainActivity;
        Firebase.setAndroidContext(activity);
        shoppingList = new ShoppingList("mainList");
        dataHelper = new DataHelper(mainActivity);
    }

    public void init(){
        handlerThread = new HandlerThread("MainController.SyncThread");
        handlerThread.start();
        syncHandler = new SyncHandler(handlerThread.getLooper());
        mainUIHandler = new Handler(Looper.getMainLooper());
        mainControllerHandler = new Handler(handlerThread.getLooper());

        shoppingList = dataHelper.loadShoppingListFromLocalStorage();
        shoppingListAdapter = new ShoppingListAdapter(activity, shoppingList);

        mainControllerHandler.post(new Runnable() {
            @Override
            public void run() {
                dataHelper.instanciateFirebase(false);
                myFirebaseRef = dataHelper.getMyFirebaseRef();
            }
        });


        shoppingListAdapter.notifyDataSetChanged();

    }

    public void deleteShoppingListItem(int position){
        shoppingList.remove(position);
        sync(true, false);
    }
    public void editShoppingListItem(final AdapterView<?> parent, final View v, final int position, long id, Activity activity){
        final ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(position);
        EditShoppingItemDialog cdd=new EditShoppingItemDialog(activity, shoppingListItem.getShoppingListItem());

        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (((EditShoppingItemDialog) dialog).isCanceled())
                    return;

                String newData = ((EditShoppingItemDialog) dialog).getNewData();
                shoppingListItem.setShoppingListItem(newData);
                shoppingList.setShoppingListItemEdit(shoppingListItem, position);
                sync(true, false);
            }
        });
        cdd.show();
    }
    public void crossOffShoppingItem(int position){
        shoppingList.setItemCrossedOff(position);
        sync(true, false);
    }
    public void addItemToShoppingList(String item){
        shoppingList.add(item);
        sync(true, false);
    }
    public void clearShoppingList(){
        shoppingList.clear();
        sync(true, false);
    }

/*  Sync is between local shopping list object
    and remote saved storage.
    After merge, it updates all local object and file
    and remote storage
 */
    public void sync(boolean refresh, boolean showConnectionStatus){
        if(refresh){
            mainUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    shoppingListAdapter.notifyDataSetChanged();
                }
            });
        }
        if(myFirebaseRef != null && DataHelper.getIsValidFirebaseURL()) {
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
        else{
            if(showConnectionStatus)
                Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect(final Activity activity){
        ConnectUsingFirebaseDialog cdd=new ConnectUsingFirebaseDialog(activity);

        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (((ConnectUsingFirebaseDialog) dialog).isCanceled())
                    return;
                mainControllerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataHelper.instanciateFirebase(false);
                        myFirebaseRef = dataHelper.getMyFirebaseRef();
                        Intent intent = new Intent(firebase_ui_updated_action);
                        LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
                        sync(false, false);
                    }
                });

            }
        });
        cdd.show();
    }

    public ShoppingList getShoppingList(){ return shoppingList; }

    public ShoppingListAdapter getShoppingListAdapter(){return shoppingListAdapter;}

    public void cleanUp(){
        handlerThread.quit();
        dataHelper.cleanUp();
    }
}
