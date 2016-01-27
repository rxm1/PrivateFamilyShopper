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

    private static boolean debugVerbose=false;
    private static boolean debugDebug=false;
    private static boolean debugInfo=false;
    private static boolean debugWarn=false;
    private static boolean debugError=false;

    public static void loadSettings(Context context){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> notificationEventsSettings = settings.getStringSet(logging_name, new HashSet<String>());
        for (String events : notificationEventsSettings) {
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

    public static boolean isDebugVerbose(){return debugVerbose;}
    public static boolean isDebugDebug(){return debugDebug;}
    public static boolean isDebugInfo(){return debugInfo;}
    public static boolean isDebugWarn(){return debugWarn;}
    public static boolean isDebugError(){return debugError;}
}
