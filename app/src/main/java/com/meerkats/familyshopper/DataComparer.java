package com.meerkats.familyshopper;

import android.content.Context;

import com.firebase.client.DataSnapshot;
import com.meerkats.familyshopper.model.ShoppingList;

import java.util.HashMap;

/**
 * Created by Rez on 07/01/2016.
 */
public class DataComparer {


    Context context;

    public DataComparer(Context context){
        this.context = context;
    }

    public void dataChanged(DataSnapshot snapshot, ShoppingList shoppingList, ShoppingListAdapter shoppingListAdapter){
        String newData = ((HashMap<String,String>)snapshot.getValue()).get("masterList");
        String oldData = shoppingList.getJson();
        if(oldData.compareTo(newData) != 0) {
            shoppingList.saveShoppingListToFile(newData);
            shoppingList.loadShoppingListFromFile();
            if(shoppingListAdapter != null)
                shoppingListAdapter.notifyDataSetChanged();
        }
    }
}
