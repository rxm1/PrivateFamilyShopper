package com.meerkats.familyshopper.util;

import android.util.Log;

/**
 * Created by Rez on 24/01/2016.
 */
public class FSLog {
    public static void verbose(String tag, String message){
        if(Settings.getLoggingLevel()==5)
            Log.v(tag, message);
    }
    public static void debug(String tag, String message){
        if(Settings.getLoggingLevel()>=4)
            Log.d(tag, message);
    }
    public static void info(String tag, String message){
        if(Settings.getLoggingLevel()>=3)
            Log.i(tag, message);
    }
    public static void warn(String tag, String message){
        if(Settings.getLoggingLevel()>=2)
            Log.w(tag, message);
    }
    public static void error(String tag, String message, Exception e){
        if(Settings.getLoggingLevel()>=1)
            Log.e(tag, message + ": " + e.getMessage() + " " + e.getStackTrace());
    }
}
