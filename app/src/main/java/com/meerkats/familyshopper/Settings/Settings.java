package com.meerkats.familyshopper.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.NotificationEvents;
import com.meerkats.familyshopper.R;
import com.meerkats.familyshopper.util.FSLog;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Rez on 24/01/2016.
 */
public class Settings {
    public static final String screen_orientation_name = "screenOrientation";
    public static final String sort_by_name = "sortBy";
    public static final String crossed_off_items_at_bottom_name = "crossedOffItems";
    private static final String logging_name = "logging";
    public static final String Firebase_URL_Name = "FirebaseURLName";
    public static final String Integrate_With_Firebase_Name = "IntegrateFirebase";
    public static final String Notification_Frequency_Name = "notificationFrequency";
    public static final String Notification_Events_Name = "notificationEvents";
    public static final String Push_Batch_Time_Name = "pushBatchTime";
    public static final String Color_Theme_Name = "colorTheme";
    public static final String Last_Synced_Name = "LastSyncedName";

    private static NotificationEvents userSelectedNotificationEvents = new NotificationEvents();
    private static String firebaseURL = "";
    private static boolean integrateFirebase = false;
    private static boolean portraitOrientation = false;
    private static boolean crossedOffItemsAtBottom = true;
    private static int notificationDelay = 0;
    private static int pushBatchDelay = 0;
    private static int loggingLevel = 1;
    private static String colorTheme="1";
    private static long lastSynced = 0;
    private static String sortBy="gray";

    private static boolean connectToFirebase = false;
    private static boolean reconnectToFirebase = false;
    private static boolean disconnectFromFirebase = false;
    private static boolean restartActivity = false;


    public static void loadSettings(final Context context, String logTag){
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

            String temp = settings.getString(Push_Batch_Time_Name, "0").trim();
            int pushBatchTime = temp == "" ? 1 : Integer.valueOf(temp);
            pushBatchDelay = 1000 * (pushBatchTime < 1 ? 1 : pushBatchTime);
            temp = settings.getString(Notification_Frequency_Name, "0");
            notificationDelay = 1000 * (temp == "" ? 0 : Integer.valueOf(temp));
            //to do: check for permissions
            firebaseURL = formatFirebaseURL(settings.getString(Firebase_URL_Name, null), logTag);
            integrateFirebase = settings.getBoolean(Integrate_With_Firebase_Name, false);
            crossedOffItemsAtBottom = settings.getBoolean(crossed_off_items_at_bottom_name, false);
            portraitOrientation = settings.getBoolean(screen_orientation_name, false);
            colorTheme = settings.getString(Color_Theme_Name, "1");
            sortBy = settings.getString(sort_by_name, "1");

            Set<String> notificationEventsSettings = settings.getStringSet(Notification_Events_Name, new HashSet<String>());
            for (String events : notificationEventsSettings) {
                switch (events) {
                    case "additions":
                        userSelectedNotificationEvents.remoteAdditions = true;
                        break;
                    case "modifications":
                        userSelectedNotificationEvents.modifications = true;
                        break;
                    case "deletions":
                        userSelectedNotificationEvents.deletions = true;
                        break;
                }
            }

            loggingLevel = Integer.parseInt(settings.getString(logging_name, "1"));
        }catch (Exception e){
            FSLog.error(logTag, "Settings loadSettings", e);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                             @Override
                             public void run() {
                                 Toast.makeText(context, "Please clear and update settings.", Toast.LENGTH_SHORT).show();
                             }
                         }
            );
        }
    }
    public static void clearSettings(Context context, String logTag){
        FSLog.verbose(logTag, "Settings clearSettings");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }
    private static String formatFirebaseURL(String firebaseURL, String logTag){
        try {
            if (firebaseURL == null) return "";


            if (!firebaseURL.startsWith("https://"))
                firebaseURL = "https://" + firebaseURL;
            if (!firebaseURL.endsWith(".com"))
                firebaseURL += ".com";
        }catch (Exception e) {
            FSLog.error(logTag, "Settings formatFirebaseURL", e);
        }
        return firebaseURL;
    }

    public static void setLastSynced(long lastSyncedTime, Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Last_Synced_Name, lastSynced);
        editor.commit();
        lastSynced = lastSyncedTime;
    }
    public static long getLastSynced(){return lastSynced;}
    public static NotificationEvents getUserSelectedNotificationEvents(){return userSelectedNotificationEvents;}
    public static int getNotificationDelay(){return notificationDelay;}
    public static boolean isIntegrateFirebase(){return integrateFirebase;}
    public static boolean crossedOffItemsAtBottom(){return crossedOffItemsAtBottom;}
    public static boolean isPortraitOrientation(){return portraitOrientation;}
    public static String getFirebaseURL(){return firebaseURL;}
    public static int getPushBatchDelay(){return pushBatchDelay;}
    public static String getSortBy(){
        return sortBy;
    }
    public static int getColorTheme(){
        switch (colorTheme){
            case "3":
                return R.style.ThemeRed;
            case "2":
                return R.style.ThemeBlue;
        }
        return R.style.ThemeGray;
    }
    public static String getColorThemeString(){return colorTheme;}
    public static int getDialogColorTheme(){
        switch (colorTheme){
            case "3":
                return R.style.DialogThemeRed;
            case "2":
                return R.style.DialogThemeBlue;
        }
        return R.style.DialogThemeGray;
    }
    public static int getLoggingLevel(){return loggingLevel;}

    public static boolean connectToFirebase(){
        return connectToFirebase;
    }
    public static void setConnectToFirebase(boolean connectFirebase){
        connectToFirebase = connectFirebase;
    }
    public static boolean reconnectToFirebase(){
        return reconnectToFirebase;
    }
    public static void setReconnectToFirebase(boolean connectToFirebase){
        reconnectToFirebase = connectToFirebase;
    }
    public static boolean disconnectFromFirbase(){
        return disconnectFromFirebase;
    }
    public static void setDisconnectFromFirebase(boolean disconnectFirebase){
        disconnectFromFirebase = disconnectFirebase;
    }
    public static boolean restartActivity(){
        return restartActivity;
    }
    public static void setRestartActivity(boolean restartActivitySet){
        restartActivity = restartActivitySet;
    }
}
