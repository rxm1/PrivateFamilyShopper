package com.meerkats.familyshopper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.firebase.client.Firebase;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.Settings;

/**
 * Created by Rez on 10/01/2016.
 */
public class MainService extends Service {

    DataHelper dataHelper;
    int mStartMode = START_STICKY;
    IBinder mBinder;
    boolean mAllowRebind;
    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;
    SettingsChangedReceiver settingsChangedReceiver;
    public static final String service_tag = "meerkats_MainService";

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
            FSLog.verbose(service_tag, "ServiceHandler constructor");
        }

        @Override
        public void handleMessage(Message message) {
        }
    }
    public class SettingsChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            FSLog.verbose(service_tag, "SettingsChangedReceiver onReceive");

            dataHelper.removeFirebaseListeners();
            dataHelper.setMyFirebaseRefNull();
            Settings.loadSettings(context);
            mServiceHandler.post(new Runnable() {
                @Override
                public void run() {
                    dataHelper.instanciateFirebase(true);
                }
            });
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FSLog.verbose(service_tag, "MainService onCreate");

        // An Android handler thread internally operates on a looper.
        mHandlerThread = new HandlerThread("MainService.HandlerThread");
        mHandlerThread.start();
        // An Android service handler is a handler running on a specific background thread.
        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());

        Firebase.setAndroidContext(this);
        dataHelper = new DataHelper(this, mHandlerThread, service_tag);
        settingsChangedReceiver = new SettingsChangedReceiver();
        IntentFilter filter = new IntentFilter(MainController.settings_updated_action);
        LocalBroadcastManager.getInstance(this).registerReceiver(settingsChangedReceiver, filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FSLog.verbose(service_tag, "MainService onStartCommand");

        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                dataHelper.instanciateFirebase(true);
            }
        });

        return mStartMode;
    }

    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
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
        FSLog.verbose(service_tag, "MainService onDestroy");

        // Cleanup service before destruction
        mHandlerThread.quit();
        dataHelper.cleanUp();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(settingsChangedReceiver);
    }

}
