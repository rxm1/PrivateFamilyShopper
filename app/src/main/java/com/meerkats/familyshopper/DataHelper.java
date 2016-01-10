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
import java.util.HashMap;

/**
 * Created by Rez on 10/01/2016.
 */
public class DataHelper {
    ValueEventListener firebaseListeners;
    Context context;
    private String localMasterFileName = "localShoppingListMasterFile.json";
    DataMerger dataMerger = new DataMerger();
    Firebase myFirebaseRef;

    public void DataHelper(Context context){
            this.context = context;
    }
    

    public void instanciateFirebase(boolean fromService) {
        try {
            SharedPreferences settings = context.getSharedPreferences(MainController.PREFS_NAME, context.MODE_PRIVATE);
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
            //Toast.makeText(activity.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
        }
    }
    private void addFirebaseListeners(){
        if(myFirebaseRef != null) {
            firebaseListeners = myFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String newData = ((HashMap<String, String>) snapshot.getValue()).get("masterList");
                    String oldData = "";
                    if(loadGsonFromLocalStorage(oldData) && !oldData.trim().isEmpty())
                    if (dataMerger.hasDataChanged(newData, oldData)) {
                        String mergedData = "";
                        if(dataMerger.mergeData(mergedData))
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

    public boolean loadShoppingListFromLocalStorage(ShoppingList shoppingList){
        String gson = "";
        if(loadGsonFromLocalStorage(gson) && !gson.trim().isEmpty()) {
            shoppingList = new ShoppingList();
            shoppingList.loadShoppingList(gson);
        }
        else
            return false;

        return true;
    }
    private boolean loadGsonFromLocalStorage(String oldData){
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
            oldData = text.toString();
        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
            return false;
        }
        return true;
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


}
