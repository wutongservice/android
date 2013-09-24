/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.app;

import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.login.util.BLog;
import com.borqs.app.config.Config;
import com.borqs.app.config.HostLoader;

/**
 * Date: 8/9/12
 * Time: 6:18 PM
 * Borqs project
 */
public final class Env {
    private static String TAG = "Borqs_Account_host_config";

    public static final String HOST_API_BASE = Config.HOST_API_BASE;
    public static final String HOST_SYNC_SYNCML = Config.HOST_SYNC_SYNCML;
    public static final String HOST_SYNC_WEB_API = Config.HOST_SYNC_WEB_API;
    public static final String HOST_MUSIC = Config.HOST_MUSIC;
    public static final String HOST_BOOK = Config.HOST_BOOK;
    public static final String HOST_BOOK_ADMIN = Config.HOST_BOOK_ADMIN;
    public static final String HOST_XDEVICE = Config.HOST_XDEVICE;
    public static final String HOST_NOTIFICATION = Config.HOST_NOTIFICATION;
    public static final String HOST_PUSH = Config.HOST_XDEVICE;

    public static final int SERVER_MODE_RELEASE = Config.SERVER_MODE_RELEASE;
    public static final int SERVER_MODE_DEV = Config.SERVER_MODE_DEV;
    public static final int SERVER_MODE_PRE_RELEASE = Config.SERVER_MODE_PRE_RELEASE;
    public static final int SERVER_MODE_TEST = Config.SERVER_MODE_TEST;

    private static Env _this;
    private Config mConfig;
    private Context mContext;
    private static HashMap<String,String> mDefaultHostMap;

    private Env(Context context){
        mContext = context.getApplicationContext();
        mConfig = new Config(mContext);
        mConfig.initDefaultHostIfNecessary();
        //init default
        mDefaultHostMap = new HashMap<String , String>();
        mDefaultHostMap.put(HOST_API_BASE, "http://api.borqs.com");
        mDefaultHostMap.put(HOST_SYNC_SYNCML, "http://api.borqs.com/sync/syncml");
        mDefaultHostMap.put(HOST_SYNC_WEB_API, "http://api.borqs.com/sync/webagent");
        mDefaultHostMap.put(HOST_MUSIC, "http://proxy.borqs.com/music");
        mDefaultHostMap.put(HOST_BOOK, "http://api.borqs.com/brook");
        mDefaultHostMap.put(HOST_BOOK_ADMIN, "http://api.borqs.com/brook");
        mDefaultHostMap.put(HOST_XDEVICE, "push.borqs.com");
        mDefaultHostMap.put(HOST_NOTIFICATION, "http://api.borqs.com/bmb");
    }

    public static void init(Context ctx){
        initIfNecessary(ctx);
    }
    /**
     * check if the debug mode is set
     * @param context
     * @return true if in debug mode, false otherwise.
     */
    public static final boolean isDebugMode(Context context){
        initIfNecessary(context);
        String debug = _this.mConfig.getData(Config.KEY_DEBUG_MODE);
        if(!TextUtils.isEmpty(debug)){
            return Boolean.valueOf(debug);
        }

        return false;
    }
    
    /**
     * enable or disable log
      * 
     * @param enable
     */
    public static final void setLogEnable(boolean enable){
        if(enable){
            BLog.enable();
        }else{
            BLog.disable();
        }
    }

    /**
     * get host url for the given feature.
     * @param context
     * @param key feature key
     * @return the host url like : http://api.borqs.com/
     */
    public static final String getHost(Context context, String key){
        initIfNecessary(context);
        String apiHost = _this.mConfig.getData(key);
        
        if(TextUtils.isEmpty(apiHost)){//if data is null,we will give a default value
            apiHost = mDefaultHostMap.get(key);
        }
        if(apiHost != null && !apiHost.endsWith("/")){
            apiHost += "/";
        }
        BLog.d("getHost: " + key + "='" + apiHost + "'");
        return apiHost;
    }

    /**
     * check server mode, {@link Env#SERVER_MODE_DEV}, {@link Env#SERVER_MODE_TEST},
     * {@link Env#SERVER_MODE_PRE_RELEASE}, {@link Env#SERVER_MODE_RELEASE} and so on
     * @param context Application context
     * @return mode of current settings
     */
    public static final int getServerMode(Context context){
        initIfNecessary(context);
        String value = _this.mConfig.getData(Config.KEY_SERVER_MODE);
        int server_mode = Env.SERVER_MODE_RELEASE;
        if(!TextUtils.isEmpty(value)){
            server_mode = Integer.valueOf(value);
        }
        BLog.d("getServerMode: " + server_mode);
        return server_mode;
    }

    private static void initIfNecessary(final Context context){
        if(null==_this){
            try {
                throw new Exception("env init");
            } catch (Exception e){
                BLog.d("env init:" + Log.getStackTraceString(e));
            }
            _this = new Env(context);
            //TODO: this has default for run in different thread
            new Thread(new Runnable(){
                public void run(){
                    new HostLoader(context).initIfNecessary();
                }
            }).start();
        }
    }
}
