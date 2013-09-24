/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Date: 7/11/12
 * Time: 4:28 PM
 * Borqs project
 */
class PreferenceStore implements IStore {
    private static final String CONTACT_STORE_PREFIX = "_borqs_contacts_";
    public static final String DEFAULT_STORE = "default";

    private String mName;
    private Context mContext;

    PreferenceStore(Context context, String name) {
        if(TextUtils.isEmpty(name)){
            name = DEFAULT_STORE;
        }
        mName = CONTACT_STORE_PREFIX + name;
        mContext = context;
    }

    @Override
    public String get(String key, String defaultValue) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        return share.getString(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        return share.getInt(key, defaultValue);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        return share.getLong(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        return share.getBoolean(key, defaultValue);
    }

    @Override
    public void put(String key, String value) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        share.edit().putString(key,value).commit();
    }

    @Override
    public void putInt(String key, int value) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        share.edit().putInt(key,value).commit();
    }

    @Override
    public void putLong(String key, long value) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        share.edit().putLong(key,value).commit();
    }

    @Override
    public void putBoolean(String key, boolean value) {
        SharedPreferences share = mContext.getSharedPreferences(mName,
                Context.MODE_PRIVATE);
        share.edit().putBoolean(key,value).commit();
    }
}
