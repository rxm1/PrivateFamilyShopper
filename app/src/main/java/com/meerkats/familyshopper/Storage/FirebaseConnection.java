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
import com.firebase.security.token.TokenGenerator;
import com.meerkats.familyshopper.DataHelper;
import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.MainService;
import com.meerkats.familyshopper.Settings.Settings;
import com.meerkats.familyshopper.Synchronize;
import com.meerkats.familyshopper.model.ShoppingList;
import com.meerkats.familyshopper.util.Diagnostics;
import com.meerkats.familyshopper.util.FSLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rez on 29/03/2016.
 */
public class FirebaseConnection {
    private boolean isValidFirebaseURL = false;
    private boolean isFirebaseAuthenticated = false;
    private Firebase myFirebaseRef = null;
    ValueEventListener firebaseListeners;
    private boolean ignoreFirebaseListenerErrors = true;

    public synchronized Firebase instanciateFirebase(Context context, final DataHelper dataHelper, final MainService.ServiceHandler serviceHandler, final MainService mainService) {
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection instanciateFirebase");

        try {
            String firebaseURL = Settings.getFirebaseURL();
            if (myFirebaseRef == null &&
                    Settings.isIntegrateFirebase() && firebaseURL != null && !firebaseURL.trim().isEmpty()) {

                myFirebaseRef = new Firebase(firebaseURL);
                if(myFirebaseRef != null) {
                    ignoreFirebaseListenerErrors = true;
                    myFirebaseRef.unauth();
                }
                if(Settings.getFirebaseAuthentication() == Settings.FirebaseAuthentication.None){
                    checkFirebaseURL(context, dataHelper, serviceHandler, mainService);
                }
                else {
                    authenticateFirebase(context, dataHelper, serviceHandler, mainService);
                }
            }
        }
        catch (Exception ex){
            FSLog.error(MainService.service_log_tag, "FirebaseConnection instanciateFirebase", ex);
        }

        return myFirebaseRef;
    }
    private synchronized void authenticateFirebase(final Context context, final DataHelper dataHelper, final MainService.ServiceHandler serviceHandler, final MainService mainService){
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection authenticateFirebase");

        isFirebaseAuthenticated = false;
        isValidFirebaseURL = false;

        switch (Settings.getFirebaseAuthentication()) {
            case Anonymous:
                myFirebaseRef.authAnonymously(new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        addFirebaseListeners(dataHelper, mainService, serviceHandler, context);
                        authenticated(context, dataHelper, serviceHandler, mainService);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        FSLog.error(MainService.service_log_tag, "FirebaseConnection onAuthenticationError anonymous", firebaseError.toException());

                        sendToast(context, "Firebase Authentication Error");
                    }
                });
                break;
            case EmailAndPassword:
                myFirebaseRef.authWithPassword(Settings.getFirebaseEmail(), Settings.getFirebasePassword(), new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        addFirebaseListeners(dataHelper, mainService, serviceHandler, context);
                        authenticated(context, dataHelper, serviceHandler, mainService);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        FSLog.error(MainService.service_log_tag, "FirebaseConnection onAuthenticationError userAndEmail", firebaseError.toException());

                        sendToast(context, "Firebase Authentication Error");
                    }
                });
                break;
        }

    }
    private void authenticated(Context context, DataHelper dataHelper, MainService.ServiceHandler serviceHandler, MainService mainService){
        isFirebaseAuthenticated = true;
        isValidFirebaseURL = true;
        Intent intent = new Intent(MainService.firebase_connected_action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        //addFirebaseListeners(dataHelper, mainService, serviceHandler, context);
    }
    private void sendToast(Context context, String toast){
        Intent intent = new Intent(MainService.show_toast_action);
        intent.putExtra("Toast", toast);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    private synchronized void checkFirebaseURL(final Context context, final DataHelper dataHelper, final MainService.ServiceHandler serviceHandler, final MainService mainService) {
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection checkFirebaseURL");

        isValidFirebaseURL = false;
        if (myFirebaseRef != null) {
            try {
                myFirebaseRef.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                isValidFirebaseURL = true;
                                addFirebaseListeners(dataHelper, mainService, serviceHandler, context);
                                Intent intent = new Intent(MainService.firebase_connected_action);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                FSLog.error(MainService.service_log_tag, "checkFirebaseURL onCancelled", firebaseError.toException());

                                if(firebaseError.getCode() == FirebaseError.PERMISSION_DENIED)
                                    sendToast(context, "Firebase Authentication Error");
                                else
                                    sendToast(context, "Error connecting to Firebase URL");
                            }

                        });
            } catch (Exception e) {
                FSLog.error(MainService.service_log_tag, "FirebaseConnection checkFirebaseURL", e);
                throw e;
            }
        }
    }

    public synchronized void addFirebaseListeners(final DataHelper dataHelper, final MainService mainService, final Handler serviceHandler, final Context context){
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection addFirebaseListeners");

        ignoreFirebaseListenerErrors=false;
        if(myFirebaseRef != null) {
            firebaseListeners = myFirebaseRef.addValueEventListener(new ValueEventListener() {
                Synchronize synchronize = new Synchronize(mainService, myFirebaseRef, MainService.service_log_tag, dataHelper);
                @Override
                public void onDataChange(final DataSnapshot snapshot) {
                    serviceHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
                            if (map != null) {
                                final ShoppingList remoteList = new ShoppingList(MainController.master_shopping_list_name, map.get("masterList"), MainService.service_log_tag);
                                final ShoppingList localList = new ShoppingList(MainController.master_shopping_list_name, dataHelper.loadGsonFromLocalStorage(), MainService.service_log_tag);
                                Diagnostics.saveLastSyncedBy(context, remoteList);
                                synchronize.doSynchronize(mainService, localList, remoteList);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    FSLog.error(MainService.service_log_tag, "addFirebaseListeners onCancelled", firebaseError.toException());

                    if(ignoreFirebaseListenerErrors==false) {
                        if (firebaseError.getCode() == FirebaseError.PERMISSION_DENIED)
                            sendToast(context, "Firebase Authentication Error");
                        else
                            sendToast(context, "Error connecting to Firebase");
                        isValidFirebaseURL = false;
                        isFirebaseAuthenticated = false;
                    }
                }
            });
        }
    }

    public void disconnect(){
        FSLog.verbose(MainService.service_log_tag, "FirebaseConnection disconnect");

        myFirebaseRef = null;
        isFirebaseAuthenticated = false;
        isValidFirebaseURL = false;
        removeFirebaseListeners();
        firebaseListeners = null;
    }
    public synchronized void removeFirebaseListeners() {
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
    public synchronized boolean isValidFirebaseConnection(){
        return Settings.isIntegrateFirebase() && myFirebaseRef != null && isValidFirebaseURL && (isFirebaseAuthenticated || Settings.getFirebaseAuthentication()== Settings.FirebaseAuthentication.None);
    }


}
