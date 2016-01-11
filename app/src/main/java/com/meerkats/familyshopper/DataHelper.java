package com.meerkats.familyshopper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.model.ShoppingList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Rez on 10/01/2016.
 */
public class DataHelper {
    ValueEventListener firebaseListeners;
    Context context;
    private String localMasterFileName = "localShoppingListMasterFile.json";
    DataMerger dataMerger;
    Firebase myFirebaseRef;
    SharedPreferences settings;
    public static final String Last_Synced_Name = "LastSyncedName";

    public DataHelper(Context context) {
        this.context = context;
        settings = context.getSharedPreferences(MainController.PREFS_NAME, context.MODE_PRIVATE);
        dataMerger = new DataMerger();
        if (settings.contains(Last_Synced_Name)) {
            settings.getLong(Last_Synced_Name, System.currentTimeMillis());
            dataMerger.setLastSynced(settings.getLong(Last_Synced_Name, 0));
        }
    }

    public void instanciateFirebase(boolean fromService) {
        try {
            if (settings.contains(MainController.Firebase_URL_Name)) {
                String firebaseURL = settings.getString(MainController.Firebase_URL_Name, null);
                if (firebaseURL != null && !firebaseURL.trim().isEmpty()) {
                    myFirebaseRef = new Firebase(firebaseURL);
                    if(fromService)
                        addFirebaseListeners();
                }
                else {
                    if(fromService)
                        removeFirebaseListeners();
                    myFirebaseRef = null;
                }
                if(!fromService) {
                    if (myFirebaseRef != null)
                        Toast.makeText(context.getApplicationContext(), "Firebase connected.", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception ex){
            if(!fromService) {
                Toast.makeText(context.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*  Sync is between saved local file
    and remote saved storage.
    After merge, it updates local file
    and remote storage
    */
    private void addFirebaseListeners(){
        if(myFirebaseRef != null) {
            firebaseListeners = myFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String localData = loadGsonFromLocalStorage();
                    if (!localData.trim().isEmpty()){
                        String mergedData = sync(snapshot, localData);
                        if (!mergedData.trim().isEmpty())
                            saveShoppingListToStorage(mergedData);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    //Toast.makeText(context, "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
    private void removeFirebaseListeners() {
        if (myFirebaseRef != null && firebaseListeners != null) {
            myFirebaseRef.removeEventListener(firebaseListeners);
        }
    }

    public String sync(DataSnapshot snapshot, String localData){
        HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
        String mergedData = "";
        if (map != null) {
            String remoteData = map.get("masterList");
            if (dataMerger.hasDataChanged(localData, remoteData)) {
                mergedData = dataMerger.mergeData(localData, remoteData);
            }
        }
        SharedPreferences.Editor editor = settings.edit();
        long lastSynced = (new Date()).getTime();
        editor.putLong(Last_Synced_Name, lastSynced);
        editor.commit();
        dataMerger.setLastSynced(lastSynced);
        return mergedData;
    }
    public ShoppingList loadShoppingListFromLocalStorage(){
        ShoppingList shoppingList = null;
        String gson = loadGsonFromLocalStorage();
        if(!gson.trim().isEmpty()) {
            shoppingList = new ShoppingList();
            shoppingList.loadShoppingList(gson);
        }

        return shoppingList;
    }
    private String loadGsonFromLocalStorage(){
        StringBuilder text = new StringBuilder();
        File file = new File(context.getFilesDir(), localMasterFileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
            return "";
        }
    }
    public void saveShoppingListToStorage(ShoppingList shoppingList){
        saveShoppingListToStorage(shoppingList.getJson());
    }
    public boolean saveShoppingListToStorage(String jsonData){
        if(saveShoppingListToLocalStorage(jsonData)){
            if(myFirebaseRef != null)
                myFirebaseRef.child("masterList").setValue(jsonData);
        }
        else
            return false;

        return true;
    }
    public boolean saveShoppingListToLocalStorage(String jsonData){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(localMasterFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }

        return true;
    }

    public Firebase getMyFirebaseRef(){return myFirebaseRef;}

}
