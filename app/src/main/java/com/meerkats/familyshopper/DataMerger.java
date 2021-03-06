package com.meerkats.familyshopper;

import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.model.ShoppingListItem;
import com.meerkats.familyshopper.util.FSLog;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Rez on 07/01/2016.
 */
public class DataMerger {
    String log_tag = "";
    public DataMerger(String log_tag){
        this.log_tag = log_tag;
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
        FSLog.verbose(log_tag, "DataMerger merge");

        ShoppingList mergedList = new ShoppingList();

        if(localList.equals(remoteList))
            return localList;

        HashMap<UUID, ShoppingListItem> remoteListHash = new HashMap<>(remoteList.size());
        for (ShoppingListItem i : remoteList.getShoppingListItems()) remoteListHash.put(i.getGuid(),i);

        for (ShoppingListItem localItem : localList.getShoppingListItems()){
            if(remoteListHash.containsKey(localItem.getGuid())) {
                ShoppingListItem remoteItem = remoteListHash.get(localItem.getGuid()); //item exists in both list
                if (!remoteItem.getIsDeleted()){ //do not add back in if its been deleted
                    if (remoteItem.getLastModified() > localItem.getLastModified()) {
                        mergedList.add(remoteItem);
                        FSLog.debug(log_tag, "Remote selected " + remoteItem.getLastModified() + " " + remoteItem.getShoppingListItem() + " " + remoteItem.isCrossedOff());
                    } else {
                        mergedList.add(localItem);
                        FSLog.debug(log_tag, "local selected : " + localItem.getLastModified() + " " + localItem.getShoppingListItem() + " " + localItem.isCrossedOff());
                    }

                    if(!remoteItem.equal(localItem))
                        notificationEvents.modifications = true;
                }
                else
                    notificationEvents.deletions = true;

                remoteListHash.remove(localItem.getGuid());
            }
            else {
                mergedList.add(localItem); //item exists only in local list
                notificationEvents.localAdditions = true;
            }
        }
        for (HashMap.Entry<UUID, ShoppingListItem> remoteItem : remoteListHash.entrySet()) //items that exist only in remote list
        {
            if (!remoteItem.getValue().getIsDeleted()) { //do not add back in if its been deleted
                notificationEvents.remoteAdditions = true;
                mergedList.add(remoteItem.getValue());
            }
        }

        return mergedList;
    }
}
