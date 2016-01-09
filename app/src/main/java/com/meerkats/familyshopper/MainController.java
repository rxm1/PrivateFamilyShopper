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
import java.util.HashMap;

/**
 * Created by Rez on 07/01/2016.
 */
public class MainController {
    Firebase myFirebaseRef;
    Activity activity;
    DataComparer dataComparer;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    private String localMasterFileName = "localShoppingListMasterFile.json";
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String Firebase_URL_Name = "FirebaseURLName";

    public MainController(Activity mainActivity) {
        this.activity = mainActivity;
        Firebase.setAndroidContext(activity);
        dataComparer = new DataComparer();
        shoppingList = new ShoppingList("mainList");

        instanciateFirebase();
    }

    private void instanciateFirebase() {
        try {
            SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
            if (settings.contains(Firebase_URL_Name)) {
                String firebaseURL = "";
                firebaseURL = settings.getString(Firebase_URL_Name, null);
                if (firebaseURL != null && !firebaseURL.trim().isEmpty()) {
                    myFirebaseRef = new Firebase(firebaseURL);
                }
                else {
                    myFirebaseRef = null;
                }
                if (myFirebaseRef != null)
                    Toast.makeText(activity.getApplicationContext(), "Firebase connected.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(activity.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();

            }
        }
        catch (Exception ex){
            Toast.makeText(activity.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
        }
    }
    public void init(){
        loadShoppingListFromStorage();

        if(myFirebaseRef != null) {
            myFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String newData = ((HashMap<String, String>) snapshot.getValue()).get("masterList");
                    if (dataComparer.hasDataChanged(newData, shoppingList)) {
                        saveShoppingListToStorage(newData);
                        shoppingList.loadShoppingList(newData);
                        shoppingListAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Toast.makeText(activity, "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void deleteShoppingListItem(int position){
        shoppingList.remove(position);
        saveShoppingListToStorage();
        shoppingListAdapter.notifyDataSetChanged();
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
                saveShoppingListToStorage();
                shoppingListAdapter.notifyDataSetChanged();

            }
        });
        cdd.show();
    }

    public void crossOffShoppingItem(int position){
        shoppingList.setItemCrossedOff(position);
        saveShoppingListToStorage();
        shoppingListAdapter.notifyDataSetChanged();
    }

    public void addItemToShoppingList(String item){
        shoppingList.add(item);
        saveShoppingListToStorage();
        shoppingListAdapter.notifyDataSetChanged();
    }

    public void saveShoppingListToStorage(){
        saveShoppingListToStorage(shoppingList.getJson());
    }
    public void saveShoppingListToStorage(String jsonData){
        saveShoppingListToFile(jsonData);
        if(myFirebaseRef != null)
            myFirebaseRef.child("masterList").setValue(jsonData);
    }
    public void saveShoppingListToFile(String jsonData){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    activity.openFileOutput(localMasterFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void loadShoppingListFromStorage(){
        File file = new File(activity.getFilesDir(), localMasterFileName);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            if(!text.toString().trim().isEmpty())
                shoppingList.loadShoppingList(text.toString());

        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }
    }

    public void clearShoppingList(){
        shoppingList.clear();
        saveShoppingListToStorage();
        shoppingListAdapter.notifyDataSetChanged();
    }

    public void sync(boolean fromConnect){
        if(myFirebaseRef != null) {
            myFirebaseRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot snapshot) {
                             String newData = ((HashMap<String, String>) snapshot.getValue()).get("masterList");
                             saveShoppingListToFile(newData);
                             shoppingList.loadShoppingList(newData);
                             shoppingListAdapter.notifyDataSetChanged();
                         }

                         @Override
                         public void onCancelled(FirebaseError firebaseError) {
                             Toast.makeText(activity.getApplicationContext(), "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     }
            );
        }
        else {
            if(!fromConnect)
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
                instanciateFirebase();
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
