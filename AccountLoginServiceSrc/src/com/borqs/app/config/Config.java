/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.app.config;

import android.content.Context;
import android.text.TextUtils;

import com.borqs.account.login.provider.AccountProvider;

/**
 * Date: 8/10/12
 * Time: 9:41 AM
 * Borqs project
 */
public class Config {
    private static String TAG = "Borqs_Account_host_config";

    public static final String KEY_DEBUG_MODE = "app_config_debug_mode";
    public static final String KEY_SERVER_MODE = "app_config_server_mode";
    public static final String KEY_SERVER_URL = "app_config_server_url";
    
    public static final String HOST_API_BASE = "api_host";
    public static final String HOST_SYNC_SYNCML = "sync_syncml_host";
    public static final String HOST_SYNC_WEB_API = "sync_webagent_host";
    public static final String HOST_MUSIC = "music_host";
    public static final String HOST_BOOK = "book_uri";
    public static final String HOST_BOOK_ADMIN = "book_admin_uri";
    public static final String HOST_XDEVICE = "xdevice_host";
    public static final String HOST_NOTIFICATION = "notification_uri";

    public static final int SERVER_MODE_RELEASE = 0;
    public static final int SERVER_MODE_DEV = 1;
    public static final int SERVER_MODE_PRE_RELEASE = 2;
    public static final int SERVER_MODE_TEST = 3;
    

    private Context mContext;
    private AccountProvider mProvider;

    public Config(Context context){
        mContext =context;
        mProvider = new AccountProvider(mContext);
    }

    public String getData(String key){
        return mProvider.getAccountData(key);
    }
    
    void setData(String key, String data){
        mProvider.setAccountData(key, data);
    }
    
    void setDebugMode(boolean debug){
        setData(KEY_DEBUG_MODE, String.valueOf(debug));
    }    

    void setServerMode(int mode){
        setData(KEY_SERVER_MODE, String.valueOf(mode));
    }

    int getServerMode(){
        int mode = SERVER_MODE_RELEASE;
        String value = getData(KEY_SERVER_MODE);
        if (!TextUtils.isEmpty(value)){
            mode = Integer.valueOf(value);
        }
        return mode;
    }
    
    public void initDefaultHostIfNecessary(){
        if (TextUtils.isEmpty(getData(HOST_API_BASE))
             ||TextUtils.isEmpty(getData(HOST_SYNC_SYNCML))
             ||TextUtils.isEmpty(getData(HOST_SYNC_WEB_API))){
            setData(HOST_API_BASE, "http://api.borqs.com");
            setData(HOST_SYNC_SYNCML, "http://api.borqs.com/sync/syncml");
            setData(HOST_SYNC_WEB_API, "http://api.borqs.com/sync/webagent");
            setData(HOST_MUSIC, "http://proxy.borqs.com/music");
            setData(HOST_BOOK, "http://api.borqs.com/brook");
            setData(HOST_BOOK_ADMIN, "http://api.borqs.com/brook");
            setData(HOST_XDEVICE, "push.borqs.com");
            setData(HOST_NOTIFICATION, "http://api.borqs.com/bmb");
        }
    }
}
