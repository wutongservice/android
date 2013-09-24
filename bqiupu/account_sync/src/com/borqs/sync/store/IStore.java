/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.store;

/**
 * Date: 7/11/12
 * Time: 4:29 PM
 * Borqs project
 */
public interface IStore {
    public String get(String key, String defaultValue);
    public int getInt(String key, int defaultValue);
    public long getLong(String key, long defaultValue);
    public boolean getBoolean(String key, boolean defaultValue);

    public void put(String key, String value);
    public void putInt(String key, int value);
    public void putLong(String key, long value);
    public void putBoolean(String key, boolean value);
}
