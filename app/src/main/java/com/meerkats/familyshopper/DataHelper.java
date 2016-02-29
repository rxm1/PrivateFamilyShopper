package com.meerkats.familyshopper;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.util.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Rez on 10/01/2016.
 */
public class DataHelper {

    Context context;
    DataMerger dataMerger;

    private static boolean isValidFirebaseURL = false;
    String log_tag = "";
    public String localMasterFileName = "localShoppingListMasterFile.json";
    public static final String service_updated_file_action = "com.meerkats.familyshopper.MainService.FileChanged";

    public DataHelper(Context context, String logTag) {
        FSLog.verbose(logTag, "DataHelper constructor");

        this.log_tag = logTag;
        this.context = context;
        dataMerger = new DataMerger(log_tag);

    }

    public synchronized Firebase instanciateFirebase(boolean fromService) {
        FSLog.verbose(log_tag, "DataHelper instanciateFirebase");

        Firebase myFirebaseRef = null;
        try {
            String firebaseURL = Settings.getFirebaseURL();
            if (Settings.isIntegrateFirebase() && firebaseURL != null && !firebaseURL.trim().isEmpty()) {
                myFirebaseRef = new Firebase(firebaseURL);
                if (myFirebaseRef != null)
                    showToast("Connecting to Firebase...", fromService);
                checkFirebaseURL(fromService, myFirebaseRef);
            }
            else {
                myFirebaseRef = null;

            if (myFirebaseRef == null)
                showToast("Firebase not connected.", fromService);
            }
        }
        catch (Exception ex){
            FSLog.error(log_tag, "DataHelper instanciateFirebase", ex);
            showToast("Firebase not connected.", fromService);
        }

        return myFirebaseRef;
    }
    private synchronized void checkFirebaseURL(final boolean fromService, Firebase myFirebaseRef) {
        FSLog.verbose(log_tag, "DataHelper checkFirebaseURL");

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
                FSLog.error(log_tag, "DataHelper checkFirebaseURL", e);
                throw e;
            }
        }
        else{
            showToast("Firebase not connected.", fromService);
        }
    }
    private void showToast(final String toastMessage, boolean fromService){
        FSLog.verbose(log_tag, "DataHelper showToast");

        if(!fromService) {
            Handler mainUIHandler = new Handler(Looper.getMainLooper());
            mainUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public synchronized String loadGsonFromLocalStorage(){
        FSLog.verbose(log_tag, "DataHelper loadGsonFromLocalStorage");

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
            FSLog.error(log_tag, "DataHelper loadGsonFromLocalStorage", e);
            return "";
        }
    }
    public synchronized boolean saveGsonToLocalStorage(String jsonData){
        FSLog.verbose(log_tag, "DataHelper saveGsonToLocalStorage");

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(localMasterFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(jsonData);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            FSLog.error(log_tag, "Synchronize saveShoppingListToLocalStorage", e);
            return false;
        }

        return true;
    }


    public synchronized static boolean getIsValidFirebaseURL(){
        return isValidFirebaseURL;
    }


    public synchronized void cleanUp(){
        FSLog.verbose(log_tag, "DataHelper cleanUp");
    }
}
