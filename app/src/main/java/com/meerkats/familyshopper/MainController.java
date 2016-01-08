package com.meerkats.familyshopper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
    Context context;
    DataComparer dataComparer;
    ShoppingList shoppingList;
    ShoppingListAdapter shoppingListAdapter;
    private String localMasterFileName = "localShoppingListMasterFile.json";

    public MainController(Context mainContext) {
        this.context = mainContext;
        Firebase.setAndroidContext(context);
        dataComparer = new DataComparer();
        myFirebaseRef = new Firebase("https://familyshopper.firebaseio.com/");
        shoppingList = new ShoppingList("mainList");

    }

    public void init(){
        loadShoppingListFromStorage();

        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String newData = ((HashMap<String,String>)snapshot.getValue()).get("masterList");
                if(dataComparer.hasDataChanged(newData, shoppingList)){
                    saveShoppingListToStorage(newData);
                    loadShoppingListFromStorage();
                    shoppingListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(context, "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setShoppingListItemDelete(int position){
        shoppingList.remove(position);
        saveShoppingListToStorage();
        shoppingListAdapter.notifyDataSetChanged();
    }

    public void setShoppingListItemEdit(final AdapterView<?> parent, final View v, final int position, long id, Activity activity){
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

    public void setShoppingItemCrossedOff(int position){
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
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(localMasterFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonData);
            outputStreamWriter.close();
            myFirebaseRef.child("masterList").setValue(jsonData);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void loadShoppingListFromStorage(){
        File file = new File(context.getFilesDir(), localMasterFileName);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
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

    public ShoppingList getShoppingList(){ return shoppingList; }

    public void setShoppingListAdapter(ShoppingListAdapter shoppingListAdapter){
        this.shoppingListAdapter = shoppingListAdapter;
    }
}
