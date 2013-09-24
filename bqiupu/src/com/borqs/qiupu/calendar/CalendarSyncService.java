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

import com.borqs.account.service.AccountListener;
import com.borqs.account.service.AccountObserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its
 * IBinder.
 */
public class CalendarSyncService extends Service implements AccountListener{

	private static final String TAG = "CalendarSyncService";

	private static final Object sSyncAdapterLock = new Object();
    private static CalendarSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
    	Log.d(TAG, "oncreate:");
    	AccountObserver.registerAccountListener(getClass().getName(), this);
    	
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new CalendarSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
    	super.onStart(intent, startId);
    	
    	Log.d(TAG, "onstart:");
    	onLogin();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
    
    @Override
    public void onDestroy() {
    	AccountObserver.unregisterAccountListener(getClass().getName());
    }

	@Override
	public void onLogin() {
		Log.d(TAG, "onLogin ");
		 if (sSyncAdapter == null) {
             sSyncAdapter = new CalendarSyncAdapter(getApplicationContext(), true);
         }else {
        	 sSyncAdapter.insertBorqsAccounts();
         }
	}

	@Override
	public void onLogout() {
		
	}

	@Override
	public void onCancelLogin() {
		
	}
}
