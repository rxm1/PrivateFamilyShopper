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
    private static boolean isValidFirebaseURL = false;
    NotificationEvents occuredNotificationEvents = new NotificationEvents();
    Timer timer = new Timer();

    class MainServiceDataChangedHandler extends Handler {
        public MainServiceDataChangedHandler(Looper myLooper) {
            super(myLooper);
        }
        public void handleMessage(Message msg) {
            DataSnapshot snapshot = (DataSnapshot)msg.obj;
            ShoppingList localList = loadShoppingListFromLocalStorage();
            occuredNotificationEvents.setFalse();

            ShoppingList mergedList = merge(snapshot, localList, occuredNotificationEvents);
            if (occuredNotificationEvents.isTrue() && mergedList != null) {
                saveShoppingListToStorage(mergedList.getJson());
                Intent intent = new Intent(service_updated_file_action);
                if (occuredNotificationEvents.forService().isTrue()) {
                    boolean recieversAvailable = LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
                    if (!recieversAvailable) {
                        sendFileChangedNotification();
                    }
                }
            }
        }
    }

    public DataHelper(Context context, HandlerThread handlerThread) {
        this.context = context;
        dataMerger = new DataMerger();
        this.handlerThread = handlerThread;
        mainServiceDataChangedHandler = new MainServiceDataChangedHandler(handlerThread.getLooper());

        settings = PreferenceManager.getDefaultSharedPreferences(context);

      /*  SharedPreferences.Editor editor = settings.edit();
        editor.clearao();
        editor.commit();
*/
        if (settings.contains(Last_Synced_Name)) {
            settings.getLong(Last_Synced_Name, System.currentTimeMillis());
            dataMerger.setLastSynced(settings.getLong(Last_Synced_Name, 0));
        }
    }

    public synchronized void instanciateFirebase(boolean fromService) {
        try {
            if (settings.contains(MainController.Firebase_URL_Name)) {
                String firebaseURL = formatFirebaseURL(settings.getString(MainController.Firebase_URL_Name, null));
                Boolean integrateFirebase = settings.getBoolean(MainController.Integrate_With_Firebase_Name, false);
                if (integrateFirebase && firebaseURL != null && !firebaseURL.trim().isEmpty()) {
                    myFirebaseRef = new Firebase(firebaseURL);
                    checkFirebaseURL(fromService);

                    if(fromService)
                        addFirebaseListeners();
                }
                else {
                    if(fromService)
                        removeFirebaseListeners();
                    myFirebaseRef = null;
                }
                if(!fromService) {
                    if (myFirebaseRef != null)
                        Toast.makeText(context.getApplicationContext(), "Connecting to Firebase...", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception ex){
            if(!fromService) {
                Toast.makeText(context.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private synchronized void checkFirebaseURL(final boolean fromService) {
        isValidFirebaseURL = false;
        if (myFirebaseRef != null) {
            try {
                myFirebaseRef.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                isValidFirebaseURL = true;
                                if (!fromService) {
                                    Toast.makeText(context.getApplicationContext(), "Firebase connected.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }

                        });
            } catch (Exception e) {
                if (!fromService) {
                    Toast.makeText(context.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            Toast.makeText(context.getApplicationContext(), "Firebase not connected.", Toast.LENGTH_SHORT).show();
        }
    }
    public synchronized String formatFirebaseURL(String firebaseURL){
        if(!firebaseURL.startsWith("https://"))
            firebaseURL = "https://" + firebaseURL;
        if(!firebaseURL.endsWith(".com"))
            firebaseURL += ".com";

        return firebaseURL;
    }

    /*  Sync is between saved local file
    and remote saved storage.
    After merge, it updates local file
    and remote storage
    */
    private synchronized void addFirebaseListeners(){
        if(myFirebaseRef != null) {
            firebaseListeners = myFirebaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Boolean integrateFirebase = settings.getBoolean(MainController.Integrate_With_Firebase_Name, false);
                    if(integrateFirebase) {
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
    private synchronized void removeFirebaseListeners() {
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
        String temp = settings.getString(MainController.Notification_Frequency_Name, "0");
        int notificationFrequency = temp==""?0:Integer.valueOf(temp);

        NotificationEvents userSelectedNotifications = userSelectedNotificationEvents();
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
            if (notificationFrequency > 0) {
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        sendNotification(finalNotificationDescription);
                    }
                };
                timer.cancel();
                timer.purge();
                timer = new Timer();
                timer.schedule(timerTask, notificationFrequency * 1000);
            }
            else
                sendNotification(finalNotificationDescription);
        }
    }

    private synchronized void sendNotification(String notificationDescription){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
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
    private synchronized NotificationEvents userSelectedNotificationEvents(){
        Set<String> notificationEventsSettings = settings.getStringSet(MainController.Notification_Events_Name, new HashSet<String>());
        NotificationEvents tempNotifications = new NotificationEvents();
        for (String events : notificationEventsSettings) {
            switch (events){
                case "additions":
                    tempNotifications.remoteAdditions = true;
                    break;
                case "modifications":
                    tempNotifications.modifications = true;
                    break;
                case "deletions":
                    tempNotifications.deletions = true;
                    break;
            }
        }
        return tempNotifications;
    }

    public synchronized ShoppingList merge(DataSnapshot snapshot, ShoppingList localList, NotificationEvents occuredNotificationEvents){
        HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
        ShoppingList mergedList = new ShoppingList();
        ShoppingList remoteList = new ShoppingList();
        if (map != null) {
            remoteList.loadShoppingList(map.get("masterList"));
            if (localList != null && !localList.equals(remoteList)) {
                mergedList = dataMerger.merge(localList, remoteList, occuredNotificationEvents);
            }
        }
        else{
            if(localList != null)
                mergedList = localList;
            occuredNotificationEvents.localAdditions=true;
        }

        SharedPreferences.Editor editor = settings.edit();
        long lastSynced = (new Date()).getTime();
        editor.putLong(Last_Synced_Name, lastSynced);
        editor.commit();
        dataMerger.setLastSynced(lastSynced);
        return mergedList;
    }
    public synchronized ShoppingList loadShoppingListFromLocalStorage(){
        ShoppingList shoppingList = null;
        String gson = loadGsonFromLocalStorage();
        if(!gson.trim().isEmpty()) {
            shoppingList = new ShoppingList();
            shoppingList.loadShoppingList(gson);
        }

        return shoppingList;
    }
    public synchronized String loadGsonFromLocalStorage(){
        StringBuilder text = new StringBuilder();
        File file = new File(context.getFilesDir(), localMasterFileName);
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
            Log.e("Exception", "File read failed: " + e.toString());
            return "";
        }
    }
    public synchronized boolean saveShoppingListToStorage(String jsonData){
        if(saveShoppingListToLocalStorage(jsonData)){
            if(myFirebaseRef != null)
                myFirebaseRef.child("masterList").setValue(jsonData);
        }
        else
            return false;

        return true;
    }
    public synchronized boolean saveShoppingListToLocalStorage(String jsonData){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(localMasterFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }

        return true;
    }
    public synchronized static boolean getIsValidFirebaseURL(){return isValidFirebaseURL;}


    public synchronized Firebase getMyFirebaseRef(){return myFirebaseRef;}
    public synchronized void cleanUp(){
        handlerThread.quit();
    }
}
