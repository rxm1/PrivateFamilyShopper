package com.meerkats.familyshopper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.util.Diagnostics;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Rez on 10/01/2016.
 */
public class DataHelper {
    ValueEventListener firebaseListeners;
    Context context;
    private String localMasterFileName = "localShoppingListMasterFile.json";
    DataMerger dataMerger;
    Firebase myFirebaseRef;
    SharedPreferences settings;
    public static final String Last_Synced_Name = "LastSyncedName";
    public static final String service_updated_file_action = "com.meerkats.familyshopper.MainService.FileChanged";
    public static final int file_changed_notification_id = 123456;
    MainServiceDataChangedHandler mainServiceDataChangedHandler;
    HandlerThread handlerThread;
    Handler mainUIHandler;
    private static boolean isValidFirebaseURL = false;
    NotificationEvents occuredNotificationEvents = new NotificationEvents();
    Timer timer = new Timer();
    String logTag = "";

    class MainServiceDataChangedHandler extends Handler {
        public MainServiceDataChangedHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            HashMap<String, String> map = (HashMap<String, String>) ((DataSnapshot)msg.obj).getValue();
            ShoppingList remoteList = new ShoppingList();
            if (map != null) {
                remoteList.loadShoppingList(map.get("masterList"));
                Diagnostics.saveLastSyncedBy(context, remoteList);
            }
            ShoppingList localList = loadShoppingListFromLocalStorage();
            occuredNotificationEvents.setFalse();

            ShoppingList mergedList = merge(remoteList, localList, occuredNotificationEvents);
            if (occuredNotificationEvents.isTrue() && mergedList != null) {
                saveShoppingListToStorage(mergedList);
                Intent intent = new Intent(service_updated_file_action);
                if (occuredNotificationEvents.forService().isTrue()) {
                    boolean recieversAvailable =
                            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                    if (!recieversAvailable) {
                        sendFileChangedNotification();
                    }
                }
            }
        }
    }

    public DataHelper(Context context, HandlerThread handlerThread, String logTag) {
        FSLog.verbose(logTag, "DataHelper constructor");

        this.logTag = logTag;
        this.context = context;
        dataMerger = new DataMerger();
        this.handlerThread = handlerThread;
        mainServiceDataChangedHandler = new MainServiceDataChangedHandler(handlerThread.getLooper());
        mainUIHandler = new Handler(Looper.getMainLooper());

        settings = PreferenceManager.getDefaultSharedPreferences(context);
        if (settings.contains(Last_Synced_Name)) {
            settings.getLong(Last_Synced_Name, System.currentTimeMillis());
            dataMerger.setLastSynced(settings.getLong(Last_Synced_Name, 0));
        }
    }

    public synchronized void instanciateFirebase(boolean fromService) {
        FSLog.verbose(logTag, "DataHelper instanciateFirebase");

        try {
            String firebaseURL = Settings.getFirebaseURL();
            Boolean integrateFirebase = Settings.isIntegrateFirebase();
            if (integrateFirebase && firebaseURL != null && !firebaseURL.trim().isEmpty()) {
                myFirebaseRef = new Firebase(firebaseURL);
                if (myFirebaseRef != null)
                    showToast("Connecting to Firebase...", fromService);
                checkFirebaseURL(fromService);
            }
            else {
                myFirebaseRef = null;

            if (myFirebaseRef == null)
                showToast("Firebase not connected.", fromService);
            }
        }
        catch (Exception ex){
            FSLog.error(logTag, "DataHelper instanciateFirebase", ex);
            showToast("Firebase not connected.", fromService);
        }
    }

    private void showToast(final String toastMessage, boolean fromService){
        if(!fromService) {
            mainUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
    private synchronized void checkFirebaseURL(final boolean fromService) {
        FSLog.verbose(logTag, "DataHelper checkFirebaseURL");

        isValidFirebaseURL = false;
        if (myFirebaseRef != null) {
            try {
                myFirebaseRef.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                isValidFirebaseURL = true;
                                showToast("Firebase connected.", fromService);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {}

                        });
            } catch (Exception e) {
                FSLog.error(logTag, "DataHelper checkFirebaseURL", e);
                throw e;
            }
        }
        else{
            showToast("Firebase not connected.", fromService);
        }
    }

    /*  Sync is between saved local file
    and remote saved storage.
    After merge, it updates local file
    and remote storage
    */
    public synchronized void addFirebaseListeners(){
        FSLog.verbose(logTag, "DataHelper addFirebaseListeners");

        if(myFirebaseRef != null) {
            firebaseListeners = myFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(Settings.isIntegrateFirebase()) {
                        Message message = mainServiceDataChangedHandler.obtainMessage();
                        message.obj = snapshot;
                        mainServiceDataChangedHandler.sendMessage(message);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    //Toast.makeText(context, "The read failed: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
    public synchronized void removeFirebaseListeners() {
        FSLog.verbose(logTag, "DataHelper removeFirebaseListeners");

        if (myFirebaseRef == null){
            if (firebaseListeners != null) {
                firebaseListeners = null;
            }
        }
        else {
            if (firebaseListeners != null) {
                myFirebaseRef.removeEventListener(firebaseListeners);
            }
        }
    }

    private synchronized void sendFileChangedNotification(){
        FSLog.verbose(logTag, "DataHelper sendFileChangedNotification");

        NotificationEvents userSelectedNotifications = Settings.getUserSelectedNotificationEvents();
        NotificationEvents mergedNotifications = new NotificationEvents();
        String notificationDescription = "Items have been ";
        if(userSelectedNotifications.remoteAdditions){
            mergedNotifications.remoteAdditions = occuredNotificationEvents.remoteAdditions;
            if(occuredNotificationEvents.remoteAdditions) notificationDescription += "added, ";
        }
        if(userSelectedNotifications.modifications){
            mergedNotifications.modifications = occuredNotificationEvents.modifications;
            if(occuredNotificationEvents.modifications) notificationDescription += "modified, ";
        }
        if(userSelectedNotifications.deletions){
            mergedNotifications.deletions = occuredNotificationEvents.deletions;
            if(occuredNotificationEvents.deletions) notificationDescription += "deleted, ";
        }
        notificationDescription = notificationDescription.substring(0, notificationDescription.length()-2);
        notificationDescription += ".";
        final String finalNotificationDescription = notificationDescription;
        if(mergedNotifications.isTrue()) {
            if (Settings.getNotificationDelay() > 0) {
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        sendNotification(finalNotificationDescription);
                    }
                };
                timer.cancel();
                timer.purge();
                timer = new Timer();
                timer.schedule(timerTask, Settings.getNotificationDelay());
            }
            else
                sendNotification(finalNotificationDescription);
        }
    }

    private synchronized void sendNotification(String notificationDescription){
        FSLog.verbose(logTag, "DataHelper sendNotification");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(com.meerkats.familyshopper.R.mipmap.ic_launcher)
                        .setContentTitle("Family Shopper")
                        .setContentText(notificationDescription);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(file_changed_notification_id, mBuilder.build());
    }

    public synchronized ShoppingList merge(ShoppingList remoteList, ShoppingList localList, NotificationEvents occuredNotificationEvents){
        FSLog.verbose(logTag, "DataHelper merge");

        ShoppingList mergedList = new ShoppingList();

        if (localList != null && remoteList != null) {
            mergedList = dataMerger.merge(localList, remoteList, occuredNotificationEvents);
        }

        SharedPreferences.Editor editor = settings.edit();
        long lastSynced = (new Date()).getTime();
        editor.putLong(Last_Synced_Name, lastSynced);
        editor.commit();
        dataMerger.setLastSynced(lastSynced);
        return mergedList;
    }
    public synchronized ShoppingList loadShoppingListFromLocalStorage(){
        FSLog.verbose(logTag, "DataHelper loadShoppingListFromLocalStorage");

        ShoppingList shoppingList = null;
        String gson = "";
        try {
            gson = loadGsonFromLocalStorage();

            if(!gson.trim().isEmpty()) {
                shoppingList = new ShoppingList();
                shoppingList.loadShoppingList(gson);
            }
        }
        catch (Exception e){
            FSLog.error(logTag, "DataHelper loadShoppingListFromLocalStorage", e);
        }
        return shoppingList;
    }
    public synchronized String loadGsonFromLocalStorage(){
        FSLog.verbose(logTag, "DataHelper loadGsonFromLocalStorage");

        StringBuilder text = new StringBuilder();
        File file = new File(context.getFilesDir(), localMasterFileName);
        if(!file.exists())
            return "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        }
        catch (IOException e) {
            FSLog.error(logTag, "DataHelper loadGsonFromLocalStorage", e);
            return "";
        }
    }
    public synchronized boolean saveShoppingListToStorage(ShoppingList shoppingList){
        FSLog.verbose(logTag, "DataHelper saveShoppingListToStorage");

        shoppingList.setLastSyncedBy(android.provider.Settings.Secure.getString(
                context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
        shoppingList.setLastSyncedBySeen(new Date().getTime());
        String jsonData = shoppingList.getJson();

        if(saveShoppingListToLocalStorage(jsonData)){
            if(myFirebaseRef != null)
                myFirebaseRef.child("masterList").setValue(jsonData);
        }
        else
            return false;

        return true;
    }
    public synchronized boolean saveShoppingListToLocalStorage(String jsonData){
        FSLog.verbose(logTag, "DataHelper saveShoppingListToLocalStorage");

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(localMasterFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            FSLog.error(logTag, "DataHelper saveShoppingListToLocalStorage", e);
            return false;
        }

        return true;
    }
    public synchronized static boolean getIsValidFirebaseURL(){return isValidFirebaseURL;}
    public synchronized void clearShoppingListFromLocalStorage(){
        FSLog.verbose(logTag, "DataHelper clearShoppingListFromLocalStorage");

        File file = new File(context.getFilesDir(), localMasterFileName);
        if(file.exists())
            saveShoppingListToLocalStorage(new ShoppingList(MainController.master_shopping_list_name).getJson());
    }

    public synchronized Firebase getMyFirebaseRef(){return myFirebaseRef;}
    public synchronized void setMyFirebaseRefNull(){
        FSLog.verbose(logTag, "DataHelper setMyFirebaseRefNull");

        myFirebaseRef=null;
    }
    public synchronized void cleanUp(){
        FSLog.verbose(logTag, "DataHelper cleanUp");

        handlerThread.quit();
    }
}
