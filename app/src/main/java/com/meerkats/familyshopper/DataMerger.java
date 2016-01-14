package com.meerkats.familyshopper;

import android.content.SharedPreferences;

import com.firebase.client.DataSnapshot;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Rez on 07/01/2016.
 */
public class DataMerger {

    //gmt in milliseconds
    private long lastSynced;

    public DataMerger(){
        lastSynced = 0;
    }

    public boolean hasDataChanged(String newData, String oldData){
        if(oldData.compareTo(newData) != 0) {
            return true;
        }
        return false;
    }

    public String mergeData(String localData, String remoteData, NotificationEvents notificationEvents, boolean alwaysMerge){
        ShoppingList localList = new ShoppingList();
        ShoppingList remoteList = new ShoppingList();
        localList.loadShoppingList(localData);
        remoteList.loadShoppingList(remoteData);
        return mergeData(localList, remoteList, notificationEvents, alwaysMerge);
    }
    public String mergeData(ShoppingList localList, ShoppingList remoteList, NotificationEvents notificationEvents, boolean alwaysMerge){
        long localLastUpdated = localList.getLastUpdated();
        long remoteLastUpdated = remoteList.getLastUpdated();

        if(alwaysMerge) {
            return merge(localList, remoteList, notificationEvents);
        }

        if (localLastUpdated > remoteLastUpdated &&
                localLastUpdated > lastSynced &&
                remoteLastUpdated <= lastSynced) {
            return localList.getJson();
        }

        if (remoteLastUpdated > localLastUpdated &&
                remoteLastUpdated > lastSynced &&
                localLastUpdated <= lastSynced) {
            notificationEvents.modifications = true;
            return remoteList.getJson();

        }

        if (remoteLastUpdated > lastSynced &&
                localLastUpdated > lastSynced){
            return merge(localList, remoteList, notificationEvents);
        }

        return "";
    }

    private String merge(ShoppingList localList, ShoppingList remoteList, NotificationEvents notificationEvents){
        ShoppingList mergedList = new ShoppingList();
        HashMap<UUID, ShoppingListItem> remoteListHash = new HashMap<>(remoteList.size());
        for (ShoppingListItem i : remoteList.getShoppingListItems()) remoteListHash.put(i.getGuid(),i);

        for (ShoppingListItem localItem : localList.getShoppingListItems()){
            if(remoteListHash.containsKey(localItem.getGuid())){
                //item exists in both list
                ShoppingListItem remoteItem = remoteListHash.get(localItem.getGuid());
                if(remoteItem.getLastModified().getTime() > localItem.getLastModified().getTime()) {
                    mergedList.add(remoteItem);
                }
                else
                    mergedList.add(localItem);

                if(!remoteItem.equal(localItem))
                    notificationEvents.modifications = true;

                remoteListHash.remove(localItem.getGuid());
            }
            else {
                //item exists only in local list
                mergedList.add(localItem);
            }
        }
        //items that exist only in remote list
        for (HashMap.Entry<UUID, ShoppingListItem> remoteItem : remoteListHash.entrySet())
        {
            notificationEvents.additions = true;
            mergedList.add(remoteItem.getValue());
        }

        return mergedList.getJson();
    }

    public void setLastSynced(long lastSynced){this.lastSynced = lastSynced;}
}
