/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.common;

import android.util.Log;

public class Logger {
    
    private static final boolean DEBUG = /*Config.DEBUG*/true;
    private static final boolean ERROR = /*Config.DEBUG*/true;
    private static final boolean WARNING = /*Config.DEBUG*/true;
    private static final boolean XML = /*Config.DEBUG*/true;

    public static void logD(String tag, String... msgs) {
        if (DEBUG) {
            for (String msg : msgs) {
                Log.d(tag, msg);
            }
        }
    }
    
    public static void logE(String tag, String... msgs) {
        if (ERROR) {
            for (String msg : msgs) {
                Log.e(tag, msg);
            }
        }
    }    

    public static void logW(String tag, String... msgs) {
        if (WARNING) {
            for (String msg : msgs) {
                Log.w(tag, msg);
            }
        }
    }    
    
    public static void logXML(String tag, String... msgs) {
        if (XML) {
            for (String msg : msgs) {
                Log.w(tag, msg);
            }
        }
    }    
}
