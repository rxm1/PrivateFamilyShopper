package com.meerkats.familyshopper;

import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;

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

    /*public synchronized ShoppingList mergeData(ShoppingList localList, ShoppingList remoteList, NotificationEvents notificationEvents){
        long localLastUpdated = localList.getLastUpdated();
        long remoteLastUpdated = remoteList.getLastUpdated();

        if(alwaysMerge) {
            return merge(localList, remoteList, notificationEvents);
        }

        if (localLastUpdated > remoteLastUpdated &&
                localLastUpdated > lastSynced &&
                remoteLastUpdated <= lastSynced) {
            return localList;
        }

        if (remoteLastUpdated > localLastUpdated &&
                remoteLastUpdated > lastSynced &&
                localLastUpdated <= lastSynced) {
            notificationEvents.modifications = true;
            return remoteList;

        }

        if (remoteLastUpdated > lastSynced &&
                localLastUpdated > lastSynced){
            return merge(localList, remoteList, notificationEvents);
        }

        return new ShoppingList();
    }*/

    public synchronized ShoppingList merge(ShoppingList localList, ShoppingList remoteList, NotificationEvents notificationEvents){
        ShoppingList mergedList = new ShoppingList();
        HashMap<UUID, ShoppingListItem> remoteListHash = new HashMap<>(remoteList.size());
        for (ShoppingListItem i : remoteList.getShoppingListItems()) remoteListHash.put(i.getGuid(),i);

        for (ShoppingListItem localItem : localList.getShoppingListItems()){
            if(remoteListHash.containsKey(localItem.getGuid())) {
                //item exists in both list
                ShoppingListItem remoteItem = remoteListHash.get(localItem.getGuid());
                if (!remoteItem.getIsDeleted()){ //do not add back in if its been deleted
                    if (remoteItem.getLastModified().getTime() > localItem.getLastModified().getTime()) {
                        mergedList.add(remoteItem);
                    } else
                        mergedList.add(localItem);

                    if(!remoteItem.equal(localItem))
                        notificationEvents.modifications = true;
                }
                else
                    notificationEvents.deletions = true;


                remoteListHash.remove(localItem.getGuid());
            }
            else {
                //item exists only in local list
                mergedList.add(localItem);
                notificationEvents.additions = true;
            }
        }
        //items that exist only in remote list
        for (HashMap.Entry<UUID, ShoppingListItem> remoteItem : remoteListHash.entrySet())
        {
            if (!remoteItem.getValue().getIsDeleted()) { //do not add back in if its been deleted
                notificationEvents.additions = true;
                mergedList.add(remoteItem.getValue());
            }
        }

        return mergedList;
    }

    public synchronized void setLastSynced(long lastSynced){this.lastSynced = lastSynced;}
}
