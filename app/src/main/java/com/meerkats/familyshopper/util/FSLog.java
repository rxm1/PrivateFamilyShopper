package com.meerkats.familyshopper.util;

import android.util.Log;

/**
 * Created by Rez on 24/01/2016.
 */
public class FSLog {
    public static void verbose(String tag, String message){
        if(Settings.isDebugVerbose())
            Log.v(tag, message);
    }
    public static void debug(String tag, String message){
        if(Settings.isDebugDebug())
            Log.d(tag, message);
    }
    public static void info(String tag, String message){
        if(Settings.isDebugInfo())
            Log.i(tag, message);
    }
    public static void warn(String tag, String message){
        if(Settings.isDebugWarn())
            Log.w(tag, message);
    }
    public static void error(String tag, String message, Exception e){
        if(Settings.isDebugError())
            Log.e(tag, message + ": " + e.getMessage() + " " + e.getStackTrace());
    }
}
