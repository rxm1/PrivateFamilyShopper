package com.meerkats.familyshopper.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.NotificationEvents;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Rez on 24/01/2016.
 */
public class Settings {
    private static final String logging_name = "logging";
    public static final String Firebase_URL_Name = "FirebaseURLName";
    public static final String Integrate_With_Firebase_Name = "IntegrateFirebase";
    public static final String Notification_Frequency_Name = "notificationFrequency";
    public static final String Notification_Events_Name = "notificationEvents";
    public static final String Push_Batch_Time_Name = "pushBatchTime";

    private static NotificationEvents userSelectedNotificationEvents = new NotificationEvents();
    private static String firebaseURL = "";
    private static boolean integrateFirebase = false;
    private static int notificationDelay = 0;
    private static int pushBatchDelay = 0;
    private static boolean debugVerbose=false;
    private static boolean debugDebug=false;
    private static boolean debugInfo=false;
    private static boolean debugWarn=false;
    private static boolean debugError=false;

    public static void loadSettings(Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String temp = settings.getString(Push_Batch_Time_Name, "0").trim();
        int pushBatchTime = temp==""?1:Integer.valueOf(temp);
        pushBatchDelay=1000*(pushBatchTime<1?1:pushBatchTime);
        temp = settings.getString(Notification_Frequency_Name, "0");
        notificationDelay = 1000*(temp==""?0:Integer.valueOf(temp));

        firebaseURL = formatFirebaseURL(settings.getString(Firebase_URL_Name, null));
        integrateFirebase = settings.getBoolean(Integrate_With_Firebase_Name, false);

        Set<String> notificationEventsSettings = settings.getStringSet(Notification_Events_Name, new HashSet<String>());
        for (String events : notificationEventsSettings) {
            switch (events){
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

        Set<String> logEventsSettings = settings.getStringSet(logging_name, new HashSet<String>());
        for (String events : logEventsSettings) {
            switch (events){
                case "verbose":
                    debugVerbose = true;
                    break;
                case "debug":
                    debugDebug = true;
                    break;
                case "info":
                    debugInfo = true;
                    break;
                case "warn":
                    debugWarn= true;
                    break;
                case "error":
                    debugError = true;
                    break;
            }
        }
    }
    public static void clearSettings(Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }
    private static String formatFirebaseURL(String firebaseURL){
        if(!firebaseURL.startsWith("https://"))
            firebaseURL = "https://" + firebaseURL;
        if(!firebaseURL.endsWith(".com"))
            firebaseURL += ".com";

        return firebaseURL;
    }

    public static NotificationEvents getUserSelectedNotificationEvents(){return userSelectedNotificationEvents;}
    public static int getNotificationDelay(){return notificationDelay;}
    public static boolean isIntegrateFirebase(){return integrateFirebase;}
    public static String getFirebaseURL(){return firebaseURL;}
    public static int getPushBatchDelay(){return pushBatchDelay;}
    public static boolean isDebugVerbose(){return debugVerbose;}
    public static boolean isDebugDebug(){return debugDebug;}
    public static boolean isDebugInfo(){return debugInfo;}
    public static boolean isDebugWarn(){return debugWarn;}
    public static boolean isDebugError(){return debugError;}
}
