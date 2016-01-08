package com.meerkats.familyshopper;

import com.firebase.client.DataSnapshot;
import com.meerkats.familyshopper.model.ShoppingList;

import java.util.HashMap;

/**
 * Created by Rez on 07/01/2016.
 */
public class DataComparer {

    public DataComparer(){

    }

    public boolean hasDataChanged(String newData, ShoppingList shoppingList){
        String oldData = shoppingList.getJson();
        if(oldData.compareTo(newData) != 0) {
            return true;
        }
        return false;
    }
}
