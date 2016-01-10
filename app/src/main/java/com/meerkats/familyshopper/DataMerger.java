package com.meerkats.familyshopper;

import com.firebase.client.DataSnapshot;
import com.meerkats.familyshopper.model.ShoppingList;

import java.util.HashMap;

/**
 * Created by Rez on 07/01/2016.
 */
public class DataMerger {

    public DataMerger(){

    }

    public boolean hasDataChanged(String newData, ShoppingList shoppingList){
        String oldData = shoppingList.getJson();
        if(oldData.compareTo(newData) != 0) {
            return true;
        }
        return false;
    }
    public boolean hasDataChanged(String newData, String oldData){
        if(oldData.compareTo(newData) != 0) {
            return true;
        }
        return false;
    }

    public boolean mergeData(String mergedData){
        return true;
    }
}
