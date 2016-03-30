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
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.Storage.FirebaseConnection;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.util.Diagnostics;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.ISynchronizeInterface;
import com.meerkats.familyshopper.Settings.Settings;

import java.util.HashMap;

/**
 * Created by Rez on 10/01/2016.
 */
public class MainService extends Service implements ISynchronizeInterface {

    DataHelper dataHelper;
    FirebaseConnection firebaseConnection;
    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;
    public static final String service_log_tag = "meerkats_MainService";
    Firebase myFirebaseRef;
    ValueEventListener firebaseListeners;
    private final IBinder mBinder = new LocalBinder();
    private static Object batchDelayObject = new Object();
    private static Object pushNotificationsObject = new Object();
    public static final String service_updated_file_action = "com.meerkats.familyshopper.MainService.FileChanged";
    public static final String firebase_connected_action = "com.meerkats.familyshopper.MainService.FirebaseConnected";
    public static final int firebaseConnectionWhat = 7357;

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

    public static final int file_changed_notification_id = 123456;

    private void disconnect(){
        FSLog.verbose(service_log_tag, "MainService disconnect");

        firebaseConnection.removeFirebaseListeners(firebaseListeners);
        firebaseConnection.disconnect();
    }
    private void connect(){
        FSLog.verbose(service_log_tag, "MainService connect");

        if(!mServiceHandler.hasMessages(firebaseConnectionWhat)) {
            Message message = Message.obtain(mServiceHandler, new Runnable() {
                @Override
                public void run() {
                    myFirebaseRef = firebaseConnection.instanciateFirebase(getApplicationContext());
                    firebaseListeners = firebaseConnection.addFirebaseListeners(dataHelper, MainService.this, mServiceHandler);
                }
            });
            message.what = firebaseConnectionWhat;
            mServiceHandler.dispatchMessage(message);
        }

    }
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.loadSettings(this, service_log_tag);
        FSLog.verbose(service_log_tag, "MainService onCreate");

        mHandlerThread = new HandlerThread("MainService.HandlerThread");
        mHandlerThread.start();
        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());

        Firebase.setAndroidContext(this);
        dataHelper = new DataHelper(this, service_log_tag);
        firebaseConnection = new FirebaseConnection();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Settings.loadSettings(this, service_log_tag);
        FSLog.verbose(service_log_tag, "MainService onStartCommand");

        connect();

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void postSyncTaskFromActivity(final ShoppingList localList, final ISynchronizeInterface synchronizeInterface, final Activity activity){
        FSLog.verbose(service_log_tag, "MainService postSyncTaskFromActivity");

        if(!firebaseConnection.isValidFirebaseConnection(myFirebaseRef))
            return;

        if(Settings.getPushBatchDelay()>0) {
            mServiceHandler.removeCallbacksAndMessages(batchDelayObject);
        }
        Runnable uiRunnable = new Runnable() {
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
                                        final Synchronize synchronize = new Synchronize(activity, myFirebaseRef, MainActivity.activity_log_tag, dataHelper);
                                        if (map != null) {
                                            ShoppingList remoteList = new ShoppingList(MainController.master_shopping_list_name, map.get("masterList"), MainActivity.activity_log_tag);
                                            Diagnostics.saveLastSyncedBy(getApplicationContext(), remoteList);
                                            synchronize.doSynchronize(synchronizeInterface, localList, remoteList);
                                        }
                                    }
                                });
                            }
                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                FSLog.error(MainActivity.activity_log_tag, "MainService postTaskFromActivity", firebaseError.toException());
                                Handler uiHandler = new Handler(Looper.getMainLooper());
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity.getApplicationContext(), "Firebase not connected", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }

                        });
            }
        };
        mServiceHandler.postAtTime(uiRunnable, batchDelayObject, SystemClock.uptimeMillis() + Settings.getPushBatchDelay());
    }
    public void postConnectToFirebaseTaskFromActivity(){
        FSLog.verbose(service_log_tag, "MainService postConnectToFirebaseTaskFromActivity");

        if(!firebaseConnection.isValidFirebaseConnection(myFirebaseRef))
            connect();
    }
    public void postReconnectToFirebaseTaskFromActivity(Context context) {
        FSLog.verbose(service_log_tag, "MainService postReconnectToFirebaseTaskFromActivity");

        Settings.loadSettings(context, service_log_tag);
        disconnect();
        connect();
    }
    public void postDisconnectFromFirebaseTaskFromActivity(Context context) {
        FSLog.verbose(service_log_tag, "MainService postDisconnectFromFirebaseTaskFromActivity");

        Settings.loadSettings(context, service_log_tag);
        disconnect();
    }
    public void postLoadSettingsFromActivity(final Context context) {
        FSLog.verbose(service_log_tag, "MainService postLoadSettingsFromActivity");

        mServiceHandler.post(new Runnable() { //queued in the handler so it can run before the connect tasks
            @Override
            public void run() {
                Settings.loadSettings(context, service_log_tag);
            }
        });
    }
    public boolean postIsValidFirebaseConnection(){
        return firebaseConnection.isValidFirebaseConnection(myFirebaseRef);
    }

    public void notifyFileChanged(final NotificationEvents occuredNotifications, ShoppingList mergedList){
        FSLog.verbose(service_log_tag, "MainService notifyFileChanged");

        Intent intent = new Intent(service_updated_file_action);
        if (occuredNotifications.forService().isTrue()) {
            boolean recieversAvailable = LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            if (!recieversAvailable) {
                if(Settings.getNotificationDelay() > 0) {
                    mServiceHandler.removeCallbacksAndMessages(pushNotificationsObject);
                }
                Runnable fileChangedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        sendFileChangedNotification(occuredNotifications);
                    }
                };
                mServiceHandler.postAtTime(fileChangedRunnable, pushNotificationsObject, SystemClock.uptimeMillis() + Settings.getNotificationDelay());
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

    @Override
    public void onDestroy() {
        FSLog.verbose(service_log_tag, "MainService onDestroy");

        // Cleanup service before destruction
        mHandlerThread.quit();
        dataHelper.cleanUp();
    }

}
