package com.meerkats.familyshopper.util;

import android.util.Log;

/**
 * Created by Rez on 24/01/2016.
 */
public class FSLog {
    public static void verbose(String tag, String message){
        if(Settings.getLoggingLevel()==5)
            log(5, tag, message, null);
    }
    public static void debug(String tag, String message){
        if(Settings.getLoggingLevel()>=4)
            log(4, tag, message, null);
    }
    public static void info(String tag, String message){
        if(Settings.getLoggingLevel()>=3)
            log(3, tag, message, null);
    }
    public static void warn(String tag, String message){
        if(Settings.getLoggingLevel()>=2)
            log(2, tag, message, null);
    }
    public static void error(String tag, String message, Exception e){
        if(Settings.getLoggingLevel()>=1)
            log(1, tag, message, e);
    }

    // for async threading
    private static void log(int logLevel, String tag, String message, Exception e){
        switch (logLevel){
            case 1:
                Log.e(tag, message + ": " + e.getMessage() + " " + e.getStackTrace());
                break;
            case 2:
                Log.w(tag, message);
                break;
            case 3:
                Log.i(tag, message);
                break;
            case 4:
                Log.d(tag, message);
                break;
            case 5:
                //Log.v(tag, message);
                break;
        }
    }
}
