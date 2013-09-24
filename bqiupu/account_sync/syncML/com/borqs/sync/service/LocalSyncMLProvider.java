/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.borqs.contacts.app.ApplicationGlobals;

/**
 * Date: 7/5/12
 * Time: 2:42 PM
 * Borqs project
 */
public class LocalSyncMLProvider {
    private static SyncMLProvider mProvider = SyncMLProvider.instance(ApplicationGlobals.getContext());
    public static int delete(Uri uri, String selection, String[] selectionArgs) {
        return mProvider.delete(uri, selection, selectionArgs);
    }

    public static Uri insert(Uri uri, ContentValues values) {
        return mProvider.insert(uri, values);
    }

    public static Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return mProvider.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return mProvider.update(uri, values, selection, selectionArgs);
    }

    public static void close(){

    }
}
