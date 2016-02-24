package com.meerkats.familyshopper.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.meerkats.familyshopper.MainController;
import com.meerkats.familyshopper.NotificationEvents;
import com.meerkats.familyshopper.R;

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
    public static final String Color_Theme_Name = "colorTheme";

    private static NotificationEvents userSelectedNotificationEvents = new NotificationEvents();
    private static String firebaseURL = "";
    private static boolean integrateFirebase = false;
    private static int notificationDelay = 0;
    private static int pushBatchDelay = 0;
    private static int loggingLevel = 1;
    private static String colorTheme="gray";

    private static boolean connectToFirebase = false;
    private static boolean reconnectToFirebase = false;
    private static boolean disconnectFromFirebase = false;


    public static void loadSettings(Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        String temp = settings.getString(Push_Batch_Time_Name, "0").trim();
        int pushBatchTime = temp==""?1:Integer.valueOf(temp);
        pushBatchDelay=1000*(pushBatchTime<1?1:pushBatchTime);
        temp = settings.getString(Notification_Frequency_Name, "0");
        notificationDelay = 1000*(temp==""?0:Integer.valueOf(temp));
//to do: check for permissions
        firebaseURL = formatFirebaseURL(settings.getString(Firebase_URL_Name, null));
        integrateFirebase = settings.getBoolean(Integrate_With_Firebase_Name, false);
        colorTheme = settings.getString(Color_Theme_Name, "gray");

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

        String loggingLevelString = settings.getString(logging_name, "error");
        switch (loggingLevelString){
            case "verbose":
                loggingLevel = 5;
                break;
            case "debug":
                loggingLevel = 4;
                break;
            case "info":
                loggingLevel = 3;
                break;
            case "warn":
                loggingLevel = 2;
                break;
            case "error":
                loggingLevel = 1;
                break;
        }

    }
    public static void clearSettings(Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }
    private static String formatFirebaseURL(String firebaseURL){
        if(firebaseURL == null) return "";

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
    public static int getColorTheme(){
        switch (colorTheme){
            case "red":
                return R.style.ThemeRed;
            case "blue":
                return R.style.ThemeBlue;
        }
        return R.style.ThemeGray;
    }
    public static int getDialogColorTheme(){
        switch (colorTheme){
            case "red":
                return R.style.DialogThemeRed;
            case "blue":
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
}
