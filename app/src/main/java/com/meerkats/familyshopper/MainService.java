package com.meerkats.familyshopper;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.ISynchronizeInterface;
import com.meerkats.familyshopper.util.Settings;

import java.util.HashMap;

/**
 * Created by Rez on 10/01/2016.
 */
public class MainService extends Service implements ISynchronizeInterface {

    DataHelper dataHelper;
    int mStartMode = START_STICKY;
    boolean mAllowRebind;
    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;
    ReconnectToFirebaseReceiver reconnectToFirebaseReceiver;
    DisconnectFromFirebaseReceiver disconnectFromFirebaseReceiver;
    SettingsChangedReceiver settingsChangedReceiver;
    public static final String service_log_tag = "meerkats_MainService";
    Firebase myFirebaseRef;
    ValueEventListener firebaseListeners;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        MainService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MainService.this;
        }
    }
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
            FSLog.verbose(service_log_tag, "ServiceHandler constructor");
        }

        @Override
        public void handleMessage(Message message) {
        }
    }
    public class ReconnectToFirebaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            FSLog.verbose(service_log_tag, "ReconnectToFirebaseReceiver onReceive");

            Settings.loadSettings(context);
            disconnect();
            mServiceHandler.post(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            });
        }
    }
    public class DisconnectFromFirebaseReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(final Context context, Intent intent){
            FSLog.verbose(service_log_tag, "DisconnectFromFirebaseReceiver onReceive");

            Settings.loadSettings(context);
            disconnect();
        }
    }
    public class SettingsChangedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(final Context context, Intent intent){
            FSLog.verbose(service_log_tag, "SettingsChangedReceiver onReceive");

            Settings.loadSettings(context);
        }
    }


    public static final int file_changed_notification_id = 123456;

    private void disconnect(){
        FSLog.verbose(service_log_tag, "MainService disconnect");

        removeFirebaseListeners();
        myFirebaseRef = null;
    }
    private void connect(){
        FSLog.verbose(service_log_tag, "MainService connect");

        myFirebaseRef = dataHelper.instanciateFirebase(true);
        addFirebaseListeners();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FSLog.verbose(service_log_tag, "MainService onCreate");

        // An Android handler thread internally operates on a looper.
        mHandlerThread = new HandlerThread("MainService.HandlerThread");
        mHandlerThread.start();
        // An Android service handler is a handler running on a specific background thread.
        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());

        Firebase.setAndroidContext(this);
        dataHelper = new DataHelper(this, mHandlerThread, service_log_tag);

        reconnectToFirebaseReceiver = new ReconnectToFirebaseReceiver();
        IntentFilter filter = new IntentFilter(MainController.reconnect_to_firebase_action);
        LocalBroadcastManager.getInstance(this).registerReceiver(reconnectToFirebaseReceiver, filter);
        disconnectFromFirebaseReceiver = new DisconnectFromFirebaseReceiver();
        filter = new IntentFilter(MainController.disconnect_from_firebase_action);
        LocalBroadcastManager.getInstance(this).registerReceiver(disconnectFromFirebaseReceiver, filter);
        settingsChangedReceiver = new SettingsChangedReceiver();
        filter = new IntentFilter(MainController.settings_changed_action);
        LocalBroadcastManager.getInstance(this).registerReceiver(settingsChangedReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FSLog.verbose(service_log_tag, "MainService onStartCommand");

        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        });

        return mStartMode;
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        FSLog.verbose(service_log_tag, "MainService onDestroy");

        // Cleanup service before destruction
        mHandlerThread.quit();
        dataHelper.cleanUp();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reconnectToFirebaseReceiver);
    }

    public synchronized void addFirebaseListeners(){
        FSLog.verbose(service_log_tag, "MainService addFirebaseListeners");

        if(myFirebaseRef != null) {
            firebaseListeners = myFirebaseRef.addValueEventListener(new ValueEventListener() {
                Synchronize synchronize = new Synchronize(getApplicationContext(), myFirebaseRef, service_log_tag, dataHelper);
                @Override
                public void onDataChange(final DataSnapshot snapshot) {
                    if(Settings.isIntegrateFirebase()) {
                        HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
                        if (map != null) {
                            final ShoppingList changedList = new ShoppingList(MainController.master_shopping_list_name, map.get("masterList"));
                            final ShoppingList currentList = new ShoppingList(MainController.master_shopping_list_name, dataHelper.loadGsonFromLocalStorage());
                            mServiceHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    synchronize.doSynchronize(MainService.this, changedList, currentList);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }
    }

    public void postTaskFromActivity(final ShoppingList changedList, final ISynchronizeInterface synchronizeInterface, final Activity activity){
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {

                myFirebaseRef.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                final HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
                                mServiceHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
                                        if(Settings.isIntegrateFirebase()) {
                                            final Synchronize synchronize = new Synchronize(activity, myFirebaseRef, service_log_tag, dataHelper);
                                            if (map != null) {
                                                ShoppingList currentList = new ShoppingList(MainController.master_shopping_list_name, map.get("masterList"));

                                                synchronize.doSynchronize(synchronizeInterface, changedList, currentList);
                                            }
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();
                            }

                        });
            }
        });
    }

    public synchronized void removeFirebaseListeners() {
        FSLog.verbose(service_log_tag, "MainService removeFirebaseListeners");

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

    public ShoppingList retrieveLatest(){
        FSLog.verbose(service_log_tag, "MainService retrieveLatest");

        ShoppingList shoppingList = null;
        String gson = "";
        try {
            gson = dataHelper.loadGsonFromLocalStorage();

            if(!gson.trim().isEmpty()) {
                shoppingList = new ShoppingList();
                shoppingList.loadShoppingList(gson);
            }
        }
        catch (Exception e){
            FSLog.error(service_log_tag, "DataHelper loadShoppingListFromLocalStorage", e);
        }
        return shoppingList;
    }

    public void notifyFileChanged(NotificationEvents occuredNotifications, ShoppingList mergedList){
        FSLog.verbose(service_log_tag, "MainService notifyFileChanged");

        Intent intent = new Intent(DataHelper.service_updated_file_action);
        if (occuredNotifications.forService().isTrue()) {
            boolean recieversAvailable =
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            if (!recieversAvailable) {
                sendFileChangedNotification(occuredNotifications);
            }
        }
    }
    private synchronized void sendFileChangedNotification(NotificationEvents occuredNotificationEvents){
        FSLog.verbose(service_log_tag, "MainService sendFileChangedNotification");

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
            sendNotification(finalNotificationDescription);
        }
    }
    private synchronized void sendNotification(String notificationDescription){
        FSLog.verbose(service_log_tag, "MainService sendNotification");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(com.meerkats.familyshopper.R.mipmap.ic_launcher)
                        .setContentTitle("Family Shopper")
                        .setContentText(notificationDescription);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
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
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(file_changed_notification_id, mBuilder.build());
    }

    public void postSynchronize(){}
}
