/*
* Copyright (C) 2007-2012 Borqs Ltd.
*  All rights reserved.
*/
package com.borqs.account.login.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.util.BLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: b251
 * Date: 12-7-28
 * Time: 下午5:22
 * To change this template use File | Settings | File Templates.
 */
public class AccountProvider {   
    private Context mContext;
    //The content provider that is in this package
    private Uri mLocalContentURI;
    private Uri mShareContentURI = Uri.EMPTY;
    //The content providers that host in other packages
    private List<Uri> mGlobalProviders;

    public AccountProvider(Context context){
        mContext = context;
        mGlobalProviders = new ArrayList<Uri>();
        lookupProviders();
    }
    
    public void cleanAccountData(){        
        //local
        delete(Uri.withAppendedPath(mLocalContentURI, AccountProviderNative.OP_DEL_USER_DATA));
        //global
        for(Uri uri : mGlobalProviders){
            delete(Uri.withAppendedPath(uri, AccountProviderNative.OP_DEL_USER_DATA));
        }
    }
    
    public String getAccountData(String key){
        //1. query from local provider
        String value = queryLocal(key);
        if(TextUtils.isEmpty(value)){
            //2. try to get data from other providers
            value = queryGlobal(key);
            // remove to ProviderNative, see bug 0058381
            /*if (TextUtils.isEmpty(value)){
                AccountManager am = AccountManager.get(mContext);
                Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
                if (accounts.length > 0) {
                    try{
                        value = am.getUserData(accounts[0], key);
                        BLog.d("acp  get data from account manager");
                    }catch(Exception e){
                        BLog.d("acp get data err:" +e.getMessage());
                    } 
                }
            }*/
            if(!TextUtils.isEmpty(value)){
                //3.cache it to local
                //updateLocal(key, value);
                updateLocal();
            }
        }
        return value;
    }

    public void setAccountData(String key, String value){
        BLog.d("update acd:" + key + ", " + value);
        updateLocal(key, value);
        updateGlobal(key, value);
    }

    private String queryLocal(String key){
        Uri getUri = Uri.withAppendedPath(mLocalContentURI, AccountProviderNative.OP_GET_USER_DATA);
        return query(getUri, key);
    }

    private void updateLocal(String key, String value){
        Uri updateUri = Uri.withAppendedPath(mLocalContentURI, AccountProviderNative.OP_SET_USER_DATA);
        update(updateUri, key, value);
    }
    
    private void updateLocal(){
        if(Uri.EMPTY == mShareContentURI){
            BLog.d("No share content provider set");
            return;
        }
        try {
            ContentValues cvs = new ContentValues();
            Uri queryUri = Uri.withAppendedPath(mShareContentURI, AccountProviderNative.OP_GET_ALL_USER_DATA);
            Cursor cursor = mContext.getContentResolver().query(queryUri, null, null, null, null);
            if(cursor != null){
                try{
                    while(cursor.moveToNext()){
                        cvs.put(cursor.getString(0), cursor.getString(1));
                    }
                } finally {
                    cursor.close();
                }
            }        
            
            if (cvs.size() > 0){
                Uri updateUri = Uri.withAppendedPath(mLocalContentURI, AccountProviderNative.OP_SET_USER_DATA);
                mContext.getContentResolver().insert(updateUri,cvs);
            }
        } catch (IllegalArgumentException exp){
            BLog.d("update local exception, maybe you use old login service version that not support get all data");
        }
    }

    private String queryGlobal(String key){
        String res = null;
        try {
            for(Uri uri : mGlobalProviders){
                Uri getUri = Uri.withAppendedPath(uri, AccountProviderNative.OP_GET_USER_DATA);
                String value = query(getUri, key);
                if(!TextUtils.isEmpty(value)){
                    mShareContentURI = uri;
                    res = value;
                    break;
                }
            }
        } catch (IllegalArgumentException exp){
            BLog.d("queryGlobal exception, maybe you use old login service version that not support get data");
        }
        return res;
    }

    private void updateGlobal(String key, String value){
        for(Uri uri : mGlobalProviders){
            Uri updateUri = Uri.withAppendedPath(uri, AccountProviderNative.OP_SET_USER_DATA);
            update(updateUri, key, value);
        }
    }

    private String query(Uri contentUri, String key){
        try{
            Uri queryUri = contentUri.buildUpon()
                    .appendQueryParameter(AccountProviderNative.ARGUMENT, key)
                    .build();
            Cursor cursor = mContext.getContentResolver().query(queryUri, null, null, null, null);
            if(cursor != null){
                try{
                    if(cursor.moveToFirst()){
                        return cursor.getString(0);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (IllegalArgumentException exp){
            BLog.d("query exception, maybe you use old login service version that not support get data");
        }
        return null;
    }

    private void update(Uri contentUri, String key, String value){
        try{
            ContentValues cvs = new ContentValues();
            cvs.put(key, value);
            mContext.getContentResolver().insert(contentUri,cvs);
        } catch (IllegalArgumentException exp){
            BLog.d("update exception, maybe you use old login service version that not support set data");
        }
    }
    
    private void delete(Uri uri){
        try{
            mContext.getContentResolver().delete(uri, null, null);
        } catch (IllegalArgumentException exp){
            BLog.d("delete exception, maybe you use old login service version that not support delete");
        }
    }

    private void lookupProviders(){
        mGlobalProviders.clear();
        //emulate the borqs providers
        PackageManager pm = mContext.getPackageManager();
        List<ProviderInfo> providers = pm.queryContentProviders(null, 0, 0);
        if(providers != null){
            for(ProviderInfo info : providers){
                if(!AccountProviderNative.class.getName().equals(info.name)){
                    continue;
                }
                //if the provider is in this package
                BLog.d("Provider:" + info.authority + " is borqs provider.");
                Uri baseUri = Uri.parse("content://"+info.authority);
                Uri contentUri = Uri.withAppendedPath(baseUri, AccountProviderNative.CONTENT_URI_ACCOUNT_PATH);
                if(mContext.getPackageName().equals(info.packageName)){
                    mLocalContentURI = contentUri;
                } else {
                    mGlobalProviders.add(contentUri);
                }
            }
        }

        if(mLocalContentURI == null){
            throw new IllegalStateException("No provider is defined!");
        }
    }
}
