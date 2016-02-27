package com.meerkats.familyshopper;

import android.content.Context;

import com.firebase.client.Firebase;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.ISynchronizeInterface;

import java.util.Date;

/**
 * Created by Rez on 25/02/2016.
 */
public class Synchronize {
    private Context context;
    private Firebase firebaseRef;
    String log_tag = "";
    private String localMasterFileName = "localShoppingListMasterFile.json";
    public static final int file_changed_notification_id = 123456;
    public static final String service_updated_file_action = "com.meerkats.familyshopper.MainService.FileChanged";
    private DataHelper dataHelper;

    public Synchronize(Context context, Firebase firebaseRef, String logTag, DataHelper dataHelper){
        FSLog.verbose(logTag, "Synchronize Synchronize");

        this.context = context;
        this.firebaseRef = firebaseRef;
        this.log_tag = logTag;
        this.dataHelper = dataHelper;
    }

    public synchronized void doSynchronize(ISynchronizeInterface synchronizeInterface, ShoppingList localList, ShoppingList remoteList){
        FSLog.verbose(log_tag, "Synchronize doSynchronize");

        NotificationEvents occuredNotificationEvents = new NotificationEvents();
        ShoppingList mergedList = merge(localList, remoteList, occuredNotificationEvents);
        if (occuredNotificationEvents.isTrue() && mergedList != null) { //really need mergedList != null???
            save(mergedList);
            synchronizeInterface.notifyFileChanged(occuredNotificationEvents, mergedList);
        }
        synchronizeInterface.postSynchronize();
    }

    private synchronized ShoppingList merge(ShoppingList localList, ShoppingList remoteList, NotificationEvents occuredNotificationEvents){
        FSLog.verbose(log_tag, "Synchronize merge");

        DataMerger dataMerger = new DataMerger(log_tag);

        if (localList != null && remoteList != null) {
            return dataMerger.merge(localList, remoteList, occuredNotificationEvents);
        }

        return new ShoppingList();
    }


    private synchronized void save(ShoppingList mergedList){
        FSLog.verbose(log_tag, "Synchronize save");

        mergedList.setLastSyncedBy(android.provider.Settings.Secure.getString(
                context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
        mergedList.setLastSyncedBySeen(new Date().getTime());
        String jsonData = mergedList.getJson();

        if(dataHelper.saveGsonToLocalStorage(jsonData)){
            if(firebaseRef != null)
                firebaseRef.child("masterList").setValue(jsonData);
        }
    }

}
