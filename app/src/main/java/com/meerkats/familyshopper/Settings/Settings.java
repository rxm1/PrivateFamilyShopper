package com.meerkats.familyshopper.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
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
    public enum FirebaseAuthentication{
        Empty(0),
        None(1),
        Anonymous(2),
        EmailAndPassword(3),
        FirebaseSecret(4);

        private final int value;
        private FirebaseAuthentication(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    public enum Vibration{
        Empty(0),
        SystemDefault(1),
        Never(2);

        private final int value;
        private Vibration(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static final String screen_orientation_name = "screenOrientation";
    public static final String sort_by_name = "sortBy";
    public static final String crossed_off_items_at_bottom_name = "crossedOffItems";
    public static final String Firebase_URL_Name = "FirebaseURLName";
    public static final String Integrate_With_Firebase_Name = "IntegrateFirebase";
    public static final String Notification_Frequency_Name = "notificationFrequency";
    public static final String Notification_Events_Name = "notificationEvents";
    public static final String Push_Batch_Time_Name = "pushBatchTime";
    public static final String Color_Theme_Name = "colorTheme";
    public static final String Last_Synced_Name = "LastSyncedName";
    public static final String logging_name = "logging";
    public static final String firebase_authentication_name = "authentication";
    public static final String firebase_email_name = "FirebaseEmail";
    public static final String firebase_password_name = "FirebaseEmailPassword";
    public static final String firebase_secret_name = "FirebaseSecret";
    public static final String vibration_name = "vibration";

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
    private static int sortBy=1;
    private static int firebaseAuthentication = 1;
    private static int vibration = 1;
    private static String firebaseEmail="";
    private static String firebasePassword="";
    private static String firebaseSecret="";
    private static Set<String> notificationEventsSettings;

    private static boolean connectToFirebase = false;
    private static boolean reconnectToFirebase = false;
    private static boolean disconnectFromFirebase = false;
    private static boolean restartActivity = false;

    private static Context settingsContext = null;

    public static void loadSettings(final Context context, String logTag){
        try {
            settingsContext = context;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

            String temp = settings.getString(Push_Batch_Time_Name, "3").trim();
            int pushBatchTime = temp == "" ? 0 : Integer.valueOf(temp);
            pushBatchDelay = pushBatchTime;
            temp = settings.getString(Notification_Frequency_Name, "60");
            notificationDelay = temp == "" ? 0 : Integer.valueOf(temp);
            //to do: check for permissions
            firebaseURL = formatFirebaseURL(settings.getString(Firebase_URL_Name, null), logTag);
            integrateFirebase = settings.getBoolean(Integrate_With_Firebase_Name, false);
            crossedOffItemsAtBottom = settings.getBoolean(crossed_off_items_at_bottom_name, false);
            portraitOrientation = settings.getBoolean(screen_orientation_name, false);
            colorTheme = settings.getString(Color_Theme_Name, "1");
            sortBy = Integer.parseInt(settings.getString(sort_by_name, "1"));

            HashSet<String> defaultEvents = new HashSet<String>();
            defaultEvents.add("Additions");
            defaultEvents.add("Modifications");
            defaultEvents.add("Deletions");
            notificationEventsSettings = settings.getStringSet(Notification_Events_Name, defaultEvents);
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
            vibration = Integer.parseInt(settings.getString(vibration_name, "1"));
            firebaseAuthentication = Integer.parseInt(settings.getString(firebase_authentication_name, "1"));
            firebaseEmail = settings.getString(firebase_email_name, "").trim();
            firebasePassword = settings.getString(firebase_password_name, "").trim();
            firebaseSecret = settings.getString(firebase_secret_name, "").trim();

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
            if (firebaseURL == null || firebaseURL.equals("")) return "";


            if (!firebaseURL.startsWith("https://"))
                firebaseURL = "https://" + firebaseURL;
            if (!firebaseURL.endsWith(".com"))
                firebaseURL += ".com";

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(settingsContext);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Firebase_URL_Name, firebaseURL);
            editor.commit();
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
    public static int getNotificationDelay(){return notificationDelay*1000;}
    public static boolean isIntegrateFirebase(){return integrateFirebase;}
    public static boolean crossedOffItemsAtBottom(){return crossedOffItemsAtBottom;}
    public static boolean isPortraitOrientation(){return portraitOrientation;}
    public static String getFirebaseURL(){return firebaseURL;}
    public static int getPushBatchDelay(){return pushBatchDelay*1000;}
    public static String getSortBy(){
        switch (sortBy){
            case 1:
                return "AlphabetAsc";
            case 2:
                return "AlphabetDesc";
            case 3:
                return "DateEnteredAsc";
            case 4:
                return "DateEnteredDesc";
            default:
                return "AlphabetAsc";
        }
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
    public static Settings.FirebaseAuthentication getFirebaseAuthentication(){
        return (FirebaseAuthentication.values())[firebaseAuthentication];
    }
    public static Settings.Vibration getVibration(){
        return (Vibration.values())[vibration];
    }

    public static String getFirebaseEmail()
    {
        return firebaseEmail;
    }
    public static String getFirebasePassword()
    {
        return firebasePassword;
    }
    public static String getFirebaseSecret()
    {
        return firebaseSecret;
    }

    public static Object getPreferenceKey(Preference preference){
        switch (preference.getKey()){
            case Integrate_With_Firebase_Name:
                return integrateFirebase;
            case Firebase_URL_Name:
                return firebaseURL;
            case firebase_authentication_name:
                return firebaseAuthentication;
            case firebase_email_name:
                return firebaseEmail;
            case firebase_password_name:
                return firebasePassword;
            case Notification_Frequency_Name:
                return notificationDelay;
            case Notification_Events_Name:
                return notificationEventsSettings;
            case Push_Batch_Time_Name:
                return pushBatchDelay;
            case sort_by_name:
                return sortBy;
            case crossed_off_items_at_bottom_name:
                return crossedOffItemsAtBottom;
            case screen_orientation_name:
                return portraitOrientation;
            case Color_Theme_Name:
                return colorTheme;
            case vibration_name:
                return vibration;
            default:
                return "";
        }
    }
}
