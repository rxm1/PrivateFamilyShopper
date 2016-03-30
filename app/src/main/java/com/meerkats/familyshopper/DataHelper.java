package com.meerkats.familyshopper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.meerkats.familyshopper.util.FSLog;
import com.meerkats.familyshopper.Settings.Settings;

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
    String log_tag = "";
    public String localMasterFileName = "localShoppingListMasterFile.json";

    public DataHelper(Context context, String logTag) {
        FSLog.verbose(logTag, "DataHelper constructor");

        this.log_tag = logTag;
        this.context = context;
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

    public synchronized void cleanUp(){
        FSLog.verbose(log_tag, "DataHelper cleanUp");
    }
}
