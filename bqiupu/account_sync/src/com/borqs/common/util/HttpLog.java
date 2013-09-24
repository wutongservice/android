/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.util;

import com.borqs.app.Env;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.sync.client.common.Logger;

/**
 * Date: 8/17/12
 * Time: 11:27 AM
 * Borqs project
 */
public class HttpLog {
    private static String TAG = "BorqsContactsService";
    private static boolean ACCOUNT_LOG_EABLED = Env.isDebugMode(ApplicationGlobals.getContext());

    public static void d(String msg){
        if(ACCOUNT_LOG_EABLED){
            Logger.logD(TAG, msg);
        }
    }

    public static void d(String tag, String msg){
        if(ACCOUNT_LOG_EABLED){
            Logger.logD(tag, msg);
        }
    }

    public static void w(String msg){
        if(ACCOUNT_LOG_EABLED){
            Logger.logW(TAG, msg);
        }
    }

    public static void e(String msg){
        if(ACCOUNT_LOG_EABLED){
            Logger.logE(TAG, msg);
        }
    }
}
