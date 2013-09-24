/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.borqs.qiupu.calendar;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.util.Log;

import com.borqs.account.login.service.ConstData;
import com.borqs.qiupu.util.CalendarMappingUtils;
import com.borqs.qiupu.util.ContactUtils;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
public class CalendarSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "CalendarSyncAdapter";
    private final AccountManager mAccountManager;
    private final Context mContext;

    public CalendarSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
        insertBorqsAccounts();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	
    	try {
//    		insertBorqsAccounts();
		} catch (Exception e) {
			Log.e(TAG, "insert borqs account exception");
		}
    }

    public void insertBorqsAccounts(){
    	
        Account[] accounts = mAccountManager.getAccounts();
        if(accounts != null) {
        	Account tmpAccount = null ;
        	final ContentResolver cr = mContext.getContentResolver();
			
        	final int tmpid = ContactUtils.getBorqsIdfromCalendar(mContext);
			if(tmpid > 0) {
				Log.i(TAG, "calendars already have this account" + tmpid);
				Intent intent = new Intent();
		        intent.setClass(mContext, CalendarSyncService.class);
		        mContext.stopService(intent);
			}else {
				for(int i=0; i<accounts.length; i++) {
	        		if(accounts[i].type != null && accounts[i].type.equals(ConstData.BORQS_ACCOUNT_TYPE)) {
	        			tmpAccount = accounts[i];
	        	        break;
	        		}
	        	}
				try {
					if(tmpAccount != null) {
						ContentValues cv = CalendarMappingUtils.crateCalendarCv(mContext, tmpAccount);
						Uri uri = cr.insert(
								asSyncAdapter(CalendarMappingUtils.CALENDAR_URL, tmpAccount.name,
										ConstData.BORQS_ACCOUNT_TYPE), cv);
						Log.d(TAG, "insert borqs account to calendars: " + ContentUris.parseId(uri));
						Intent intent = new Intent();
						intent.setClass(mContext, CalendarSyncService.class);
						mContext.stopService(intent);
					}else {
						Log.d(TAG, "have no borqsAccount");
					}
				} catch (Exception e) {
					Log.d(TAG, "insert account to calendar exception");
				}
			}
        }
    }
    
    static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,
                        "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }
}
