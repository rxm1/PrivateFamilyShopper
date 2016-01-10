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

    @Override
    public void onCreate() {
        Firebase.setAndroidContext(this);
        dataHelper = new DataHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dataHelper.instanciateFirebase();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Nullable
    public IBinder onBind(Intent intent){
        return null;
    }

}
