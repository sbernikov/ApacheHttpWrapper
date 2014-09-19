package com.sbtools.apachehttpwrapper.log;

import android.util.Log;

public final class ApacheHttpWrapperLog {
    
    private static final String TAG = "ApacheHttpWrapper";
    
    public static void e(String msg) {
        log(Log.ERROR, msg);
    }
    
    private static void log(int priority, String msg) {
        Log.println(priority, TAG, msg);
    }
}