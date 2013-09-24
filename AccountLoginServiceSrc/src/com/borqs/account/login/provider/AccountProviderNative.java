/*
* Copyright (C) 2007-2012 Borqs Ltd.
*  All rights reserved.
*/
package com.borqs.account.login.provider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.util.BLog;

public class AccountProviderNative extends ContentProvider {
    public static final String CONTENT_URI_ACCOUNT_PATH = "account";
    public static final String BASE_AUTHORITY_PREFIX = "com.borqs.account_service_";

    public static final String ARGUMENT = "arg";
    public static final String OP_GET_USER_DATA = "op_get_user_data";
    public static final String OP_SET_USER_DATA = "op_set_user_data";
    public static final String OP_DEL_USER_DATA = "op_del_user_data";
    public static final String OP_GET_ALL_USER_DATA = "op_get_all_user_data";

    private static final int ACCOUNT_BASE = 0;
    private static final int ACCOUNT_GET_USER_DATA = ACCOUNT_BASE+1;
    private static final int ACCOUNT_SET_USER_DATA = ACCOUNT_GET_USER_DATA+1;
    private static final int ACCOUNT_DEL_USER_DATA = ACCOUNT_SET_USER_DATA+1;
    private static final int ACCOUNT_GET_ALL_USER_DATA = ACCOUNT_DEL_USER_DATA+1;


    //URI matcher
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        int match = sURIMatcher.match(uri);
        switch (match) {  
            case ACCOUNT_GET_USER_DATA:{
                String value = getAccountUserData(uri.getQueryParameter(ARGUMENT));
                MatrixCursorExtras c = new MatrixCursorExtras(new String[]{"value"});
                if( !TextUtils.isEmpty(value) ){
                    c.newRow().add(value);
                }
                return c;
            }
            case ACCOUNT_GET_ALL_USER_DATA:{
                HashMap<String, String> results = getAllUserData(); 
                MatrixCursorExtras c = new MatrixCursorExtras(new String[]{"key", "value"});
                Iterator<String> it = results.keySet().iterator();
                
                while (it.hasNext()){
                    String key = it.next();
                    c.addRow(new String[]{key, results.get(key)});
                }
                return c;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sURIMatcher.match(uri);
        switch (match){
            case ACCOUNT_SET_USER_DATA:{
                Set<Map.Entry<String, Object>> set = values.valueSet();
                for(Map.Entry<String, Object> e : set){
                    setAccountUserData(e.getKey().toString(), e.getValue().toString());
                }
                return null;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private String getAccountUserData(String key){
        if(TextUtils.isEmpty(key)){
            throw new IllegalArgumentException();
        }
        FileStore dataStore = new FileStore(getContext());
        String value = dataStore.get(key, null);
        if (TextUtils.isEmpty(value)
            && isOldKey(key)){
            try{
                AccountManager am = AccountManager.get(getContext());
                Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
                if (accounts.length > 0){
                    value = am.getUserData(accounts[0], key);
                }
                BLog.d("apn  get data from account manager");
            }catch(Exception e){
                BLog.d("apn get data err:" +e.getMessage());
            } 
        }
        return value;
    }

    private boolean isOldKey(String key){
        boolean res = false;
        if (ConstData.ACCOUNT_USER_ID.equalsIgnoreCase(key)){
            res = true;
        } else if (ConstData.ACCOUNT_SESSION.equalsIgnoreCase(key)){
            res = true;
        } else if (ConstData.ACCOUNT_LOGIN_ID.equalsIgnoreCase(key)){
            res = true;
        } else if (ConstData.ACCOUNT_GUID.equalsIgnoreCase(key)){
            res = true;
        }
        
        return res;
    }
    
    private HashMap<String, String> getAllUserData(){
        FileStore dataStore = new FileStore(getContext());
        HashMap<String, String> result = null;
        result = dataStore.getAll();
        return result;
    }

    private void setAccountUserData(String key, String value){
        if(TextUtils.isEmpty(key)){
            throw new IllegalArgumentException();
        }
        FileStore dataStore = new FileStore(getContext());
        dataStore.set(key, value);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sURIMatcher.match(uri);
        switch (match){
            case ACCOUNT_DEL_USER_DATA:{
                FileStore dataStore = new FileStore(getContext());
                dataStore.clean();
                return 1;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/account-entry";
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return -1;
    }

    /**
     * A simple extension to MatrixCursor that supports extras
     */
    private static class MatrixCursorExtras extends MatrixCursor {

        private Bundle mExtras;

        public MatrixCursorExtras(String[] columnNames) {
            super(columnNames);
            mExtras = null;
        }

        /*public void setExtras(Bundle extras) {
            mExtras = extras;
        }*/

        @Override
        public Bundle getExtras() {
            return mExtras;
        }
    }

    static {
        UriMatcher matcher = sURIMatcher;
        String[] apps = new String[]{
            BorqsProductDefine.ACTIVITY, BorqsProductDefine.BROOK, BorqsProductDefine.OPENFACE,
            BorqsProductDefine.SECURITYCENTER, BorqsProductDefine.WUTONG, BorqsProductDefine.XMESSAGE,
            BorqsProductDefine.TOUCHDIALER,BorqsProductDefine.MUSIC,BorqsProductDefine.APPBOX,
            BorqsProductDefine.CONTACTSPLUS, BorqsProductDefine.BORQS_CARD,
            BorqsProductDefine.FILE_MANAGER, BorqsProductDefine.NOTES, BorqsProductDefine.DESK_CLOCK,
            BorqsProductDefine.BORQS_APP1, BorqsProductDefine.BORQS_APP2,BorqsProductDefine.BORQS_APP3,
        };

        //for each application, which define a unique Authority URI with it's name. Here, we just
        //register the all authorities.
        for(String app : apps){
            String authority = BASE_AUTHORITY_PREFIX + app;
            matcher.addURI(authority, CONTENT_URI_ACCOUNT_PATH + "/" + OP_GET_USER_DATA,
                ACCOUNT_GET_USER_DATA);
            matcher.addURI(authority, CONTENT_URI_ACCOUNT_PATH + "/" + OP_SET_USER_DATA,
                ACCOUNT_SET_USER_DATA);
            matcher.addURI(authority, CONTENT_URI_ACCOUNT_PATH + "/" + OP_DEL_USER_DATA,
                    ACCOUNT_DEL_USER_DATA);
            matcher.addURI(authority, CONTENT_URI_ACCOUNT_PATH + "/" + OP_GET_ALL_USER_DATA,
                    ACCOUNT_GET_ALL_USER_DATA);
        }
    }
}
