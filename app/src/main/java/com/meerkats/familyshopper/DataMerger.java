package com.meerkats.familyshopper;

import android.content.SharedPreferences;

import com.firebase.client.DataSnapshot;
import com.meerkats.familyshopper.model.ShoppingList;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Rez on 07/01/2016.
 */
public class DataMerger {

    //gmt in milliseconds
    private long lastSynced;

    public DataMerger(){
        lastSynced = 0;
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

    public String mergeData(String localData, String remoteData){
        ShoppingList localList = new ShoppingList();
        ShoppingList remoteList = new ShoppingList();
        localList.loadShoppingList(localData);
        remoteList.loadShoppingList(remoteData);
        return mergeData(localList, remoteList);
    }
    public String mergeData(ShoppingList localList, String remoteData){
        ShoppingList remoteList = new ShoppingList();
        remoteList.loadShoppingList(remoteData);
        return mergeData(localList, remoteList);
    }
    public String mergeData(ShoppingList localList, ShoppingList remoteList){
        long localLastUpdated = localList.getLastUpdated();
        long remoteLastUpdated = remoteList.getLastUpdated();

        if(localLastUpdated > remoteLastUpdated &&
                localLastUpdated > lastSynced &&
                remoteLastUpdated <= lastSynced) {
            return localList.getJson();
        }

        if (remoteLastUpdated > localLastUpdated &&
                remoteLastUpdated > lastSynced &&
                localLastUpdated <= lastSynced){
            return remoteList.getJson();

        }

        if (remoteLastUpdated > lastSynced &&
                localLastUpdated > lastSynced){
            return "";
        }

        return "";
    }

    public long getLastSynced(){return lastSynced;}
    public void setLastSynced(long lastSynced){this.lastSynced = lastSynced;}
}
