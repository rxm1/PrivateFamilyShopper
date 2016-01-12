package com.meerkats.familyshopper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

/**
 * Created by Rez on 10/01/2016.
 */
public class MainService extends Service {

    Firebase myFirebaseRef;
    DataHelper dataHelper;
    DataMerger dataMerger;
    int mStartMode = START_STICKY;
    IBinder mBinder;
    boolean mAllowRebind;


    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;

    // Define how the handler will process messages
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // Define how to handle any incoming messages here
        @Override
        public void handleMessage(Message message) {
            // ...
            // When needed, stop the service with
            // stopSelf();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // An Android handler thread internally operates on a looper.
        mHandlerThread = new HandlerThread("MainService.HandlerThread");
        mHandlerThread.start();
        // An Android service handler is a handler running on a specific background thread.
        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());

        Firebase.setAndroidContext(this);
        dataHelper = new DataHelper(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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
        // Cleanup service before destruction
        mHandlerThread.quit();
    }

}
