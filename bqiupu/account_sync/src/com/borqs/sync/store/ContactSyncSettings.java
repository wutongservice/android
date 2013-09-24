/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.store;

import android.content.Context;
import android.text.TextUtils;
import com.borqs.common.account.AccountAdapter;

/**
 * Date: 7/20/12
 * Time: 11:41 AM
 * Borqs project
 */
public class ContactSyncSettings implements IStore {
    private static final String KEY_PREFIX = "Contact_";
    
    private Context mContext;

    public ContactSyncSettings(Context context){
        mContext = context;
    }

    @Override
    public String get(String key, String defaultValue) {
        String result =  AccountAdapter.getUserData(mContext, KEY_PREFIX + key);
        if(TextUtils.isEmpty(result)){
            result =  defaultValue;
        }
        return result;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String result =  AccountAdapter.getUserData(mContext, KEY_PREFIX + key);
        if(TextUtils.isEmpty(result)){
            result =  String.valueOf(defaultValue);
        }
        return Integer.valueOf(result);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String result =  AccountAdapter.getUserData(mContext, KEY_PREFIX + key);
        if(TextUtils.isEmpty(result)){
            result =  String.valueOf(defaultValue);
        }
        return Long.valueOf(result);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String result =  AccountAdapter.getUserData(mContext, KEY_PREFIX + key);
        if(TextUtils.isEmpty(result)){
            result =  String.valueOf(defaultValue);
        }
        return Boolean.valueOf(result);
    }

    @Override
    public void put(String key, String value) {
        AccountAdapter.setUserData(mContext, KEY_PREFIX + key, value);
    }

    @Override
    public void putInt(String key, int value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void putLong(String key, long value) {
        put(key, String.valueOf(value));
    }

    @Override
    public void putBoolean(String key, boolean value) {
        put(key, String.valueOf(value));
    }
}
