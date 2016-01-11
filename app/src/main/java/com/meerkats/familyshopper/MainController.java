package com.meerkats.familyshopper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Rez on 07/01/2016.
 */
public class MainController {
    Firebase myFirebaseRef;
    Activity activity;
    DataMerger dataMerger;
    DataHelper dataHelper;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String Firebase_URL_Name = "FirebaseURLName";

    public MainController(Activity mainActivity) {
        this.activity = mainActivity;
        Firebase.setAndroidContext(activity);
        dataMerger = new DataMerger();
        shoppingList = new ShoppingList("mainList");
        dataHelper = new DataHelper(mainActivity);
    }

    public void init(){
        dataHelper.loadShoppingListFromLocalStorage(shoppingList);
        dataHelper.instanciateFirebase(false);
        myFirebaseRef = dataHelper.getMyFirebaseRef();
    }

    public void deleteShoppingListItem(int position){
        shoppingList.remove(position);
        sync(false);
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
                sync(false);

            }
        });
        cdd.show();
    }
    public void crossOffShoppingItem(int position){
        shoppingList.setItemCrossedOff(position);
        sync(false);
    }
    public void addItemToShoppingList(String item){
        shoppingList.add(item);
        sync(false);
    }
    public void clearShoppingList(){
        shoppingList.clear();
        sync(false);
    }

/*  Sync is between local shopping list object
    and remote saved storage.
    After merge, it updates all local object and file
    and remote storage
 */
    public void sync(boolean fromConnect){
        if(myFirebaseRef != null) {
            myFirebaseRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String localData = shoppingList.getJson();
                            String mergedData = dataHelper.sync(snapshot, localData);
                            if (!mergedData.trim().isEmpty()) {
                                dataHelper.saveShoppingListToStorage(mergedData);
                                shoppingList.loadShoppingList(mergedData);
                                shoppingListAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(activity.getApplicationContext(), "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    });
        }
        else{
            if (!fromConnect)
                Toast.makeText(activity, "Not Connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void connect(Activity activity){
        ConnectUsingFirebaseDialog cdd=new ConnectUsingFirebaseDialog(activity);

        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (((ConnectUsingFirebaseDialog) dialog).isCanceled())
                    return;
                dataHelper.instanciateFirebase(false);
                myFirebaseRef = dataHelper.getMyFirebaseRef();
                sync(true);
            }
        });
        cdd.show();
    }

    public ShoppingList getShoppingList(){ return shoppingList; }

    public void setShoppingListAdapter(ShoppingListAdapter shoppingListAdapter){
        this.shoppingListAdapter = shoppingListAdapter;
    }
}
