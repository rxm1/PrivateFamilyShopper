package com.meerkats.familyshopper.model;

import android.content.Context;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Rez on 17/12/2015.
 */
public class ShoppingList extends ArrayList<String>{
    private Context context;
    private String localMasterFileName = "localShoppingListMasterFile.json";
    Gson gson = new Gson();
    Type gsonType = new TypeToken<InnerShoppingList>() {}.getType();
    Firebase myFirebaseRef;

    private InnerShoppingList innerShoppingList;

    public ShoppingList(String name){
        init(name, null, null);
    }
    public ShoppingList(String name, Context context, Firebase myFirebaseRef){
        init(name, context, myFirebaseRef);
    }
    private void init(String name, Context context, Firebase myFirebaseRef){
        innerShoppingList = new InnerShoppingList(name);
        this.context = context;
        this.myFirebaseRef = myFirebaseRef;
    }

    public String getShoppingListName() {
        return innerShoppingList.shoppingListName;
    }

    @Override
    public boolean add(String item){
        super.add(item);
        innerShoppingList.add(new ShoppingListItem(item));
        saveShoppingListToFile();
        return true;
    }
    @Override
    public String remove(int position){
        String item = super.remove(position);
        innerShoppingList.remove(position);
        saveShoppingListToFile();
        return item;
    }
    @Override
    public void clear(){
        super.clear();
        innerShoppingList.clear();
        saveShoppingListToFile();
    }

    public void setItemCrossedOff(int position){
        ShoppingListItem shoppingListItem = innerShoppingList.getShoppingListItems().get(position);
        shoppingListItem.setIsCrossedOff(!shoppingListItem.isCrossedOff());
        innerShoppingList.setShoppingListItem(position, shoppingListItem);
        saveShoppingListToFile();
    }


    public void getSavedShoppingList(){
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
            setShoppingList(text.toString());

        }
        catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }
    }
    public void setShoppingList(String newGson){
        super.clear();
        innerShoppingList = gson.fromJson(newGson, gsonType);
        for (int i = 0; i < innerShoppingList.shoppingListItems.size(); i++){
            this.add(i, innerShoppingList.shoppingListItems.get(i).getShoppingListItem());
        }
    }
    public String getJson(){
        return gson.toJson(innerShoppingList, gsonType);
    }

    public void saveShoppingListToFile(){
        saveShoppingListToFile(getJson());
    }
    public void saveShoppingListToFile(String jsonData){
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

    public ShoppingListItem getShoppingListItem(int index){
        return innerShoppingList.shoppingListItems.get(index);
    }
    public void setShoppingListItem(int index, ShoppingListItem shoppingListItem){
        innerShoppingList.shoppingListItems.set(index, shoppingListItem);
        this.set(index, shoppingListItem.toString());
    }

    private class InnerShoppingList
    {
        private String shoppingListName;
        private ArrayList<ShoppingListItem> shoppingListItems;

        private InnerShoppingList(String shoppingListName)
        {
            shoppingListItems = new ArrayList<ShoppingListItem>();
            this.shoppingListName = shoppingListName;
        }

        private void add(ShoppingListItem shoppingListItem){
            shoppingListItems.add(shoppingListItem);
        }

        private void remove(int postion){
            shoppingListItems.remove(postion);
        }

        private void clear(){
            shoppingListItems.clear();
        }

        private void setShoppingListItem(int position, ShoppingListItem shoppingListItem){
            shoppingListItems.set(position, shoppingListItem);
        }
        public String getShoppingListName(){return shoppingListName;}
        public void setShoppingListName(String shoppingListName){this.shoppingListName=shoppingListName;}
        public ArrayList<ShoppingListItem> getShoppingListItems(){return shoppingListItems;}
        public void setShoppingListItems(ArrayList<ShoppingListItem> shoppingListItems){this.shoppingListItems=shoppingListItems;}

        @Override
        public String toString(){
            String items = "";
            for (int i=0; i < shoppingListItems.size(); i++) {
                items += shoppingListItems.get(i).toString();
            }
            return "ShoppingList [shopping_list_name=" + shoppingListName + ", " + items
                    + "]";
        }
    }
}
