package com.meerkats.familyshopper.Storage;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.DataHelper;
import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.MainService;
import com.meerkats.familyshopper.Settings.Settings;
import com.meerkats.familyshopper.Synchronize;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.util.Diagnostics;
import com.meerkats.familyshopper.util.FSLog;

import java.util.HashMap;

/**
 * Created by Rez on 29/03/2016.
 */
public class FirebaseConnection {
    private static boolean isValidFirebaseURL = false;
    private static boolean isFirebaseAuthenticated = false;
    private static Firebase myFirebaseRef = null;

    public synchronized Firebase instanciateFirebase(Context context) {
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection instanciateFirebase");

        try {
            String firebaseURL = Settings.getFirebaseURL();
            if (myFirebaseRef == null &&
                    Settings.isIntegrateFirebase() && firebaseURL != null && !firebaseURL.trim().isEmpty()) {

                myFirebaseRef = new Firebase(firebaseURL);
                checkFirebaseURL(context);
                authenticateFirebase(context);
            }
        }
        catch (Exception ex){
            FSLog.error(MainService.service_log_tag, "FirebaseConnection instanciateFirebase", ex);
        }

        return myFirebaseRef;
    }
    private synchronized void authenticateFirebase(final Context context){
        isFirebaseAuthenticated = false;
        myFirebaseRef.unauth();
        myFirebaseRef.authWithPassword("rez@rez.com", "rezwana", new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                isFirebaseAuthenticated = true;
                Intent intent = new Intent(MainService.firebase_connected_action);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                isFirebaseAuthenticated = true;
                FSLog.error(MainService.service_log_tag, "FirebaseConnection onAuthenticationError", firebaseError.toException());
            }
        });
    }
    private synchronized void checkFirebaseURL(final Context context) {
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection checkFirebaseURL");

        isValidFirebaseURL = false;
        if (myFirebaseRef != null) {
            try {
                myFirebaseRef.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                isValidFirebaseURL = true;
                                Intent intent = new Intent(MainService.firebase_connected_action);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {}

                        });
            } catch (Exception e) {
                FSLog.error(MainService.service_log_tag, "FirebaseConnection checkFirebaseURL", e);
                throw e;
            }
        }
    }
    public synchronized void removeFirebaseListeners(ValueEventListener firebaseListeners) {
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection removeFirebaseListeners");

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
    public synchronized ValueEventListener addFirebaseListeners(final DataHelper dataHelper, final MainService context, final Handler serviceHandler){
        FSLog.verbose(MainService.service_log_tag, "MainService addFirebaseListeners");
        ValueEventListener firebaseListeners = null;
        if(myFirebaseRef != null) {
            firebaseListeners = myFirebaseRef.addValueEventListener(new ValueEventListener() {
                Synchronize synchronize = new Synchronize(context, myFirebaseRef, MainService.service_log_tag, dataHelper);
                @Override
                public void onDataChange(final DataSnapshot snapshot) {
                    serviceHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(isValidFirebaseConnection(myFirebaseRef)) {
                                HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
                                if (map != null) {
                                    final ShoppingList remoteList = new ShoppingList(MainController.master_shopping_list_name, map.get("masterList"), MainService.service_log_tag);
                                    final ShoppingList localList = new ShoppingList(MainController.master_shopping_list_name, dataHelper.loadGsonFromLocalStorage(), MainService.service_log_tag);
                                    Diagnostics.saveLastSyncedBy(context, remoteList);
                                    synchronize.doSynchronize(context, localList, remoteList);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    FSLog.error(MainService.service_log_tag, "MainService addFirebaseListeners", firebaseError.toException());
                }
            });
        }
        return firebaseListeners;
    }

    public static void disconnect(){
        myFirebaseRef = null;
        isFirebaseAuthenticated = false;
        isValidFirebaseURL = false;
    }

    public synchronized boolean isValidFirebaseConnection(Firebase myFirebaseRef){
        return Settings.isIntegrateFirebase() && myFirebaseRef != null && isValidFirebaseURL && isFirebaseAuthenticated;
    }


}
