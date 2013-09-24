/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.sync.service;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SyncResult;
import android.content.SyncStats;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.login.util.BLog;
import com.borqs.common.account.AccountAdapter;
import com.borqs.common.contact.ContactSyncHelper;
import com.borqs.profile.ProfileSyncService;
import com.borqs.sync.client.common.ContactLock;
import com.borqs.sync.client.common.ContactSyncOperator;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.common.SyncHelper;
import com.borqs.sync.client.receiver.ScheduleSyncService;
import com.borqs.sync.ds.config.SyncProfile;
import com.borqs.syncml.ds.imp.tag.AlertCode;

public class ContactsSyncAdapterService extends Service {
    private static final String TAG = "BMS ContactsSyncAdapterService";
    
    //we can start sync by ContentResolver.requestSync(),and we can put extra.
    //now the sync_type can be put to sync extras for slow,fast sync or many other types
    public static final String SYNC_TYPE_EXTAR = "sync_type";

    private static SyncAdapterImpl sSyncAdapter = null;

    private static final Object sSyncAdapterLock = new Object();

    private static final long SYNC_LOCK_TIMEOUT = 100 * 60 * 1000L;

    public ContactsSyncAdapterService() {
        super();
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;

        private static final Object mSyncLock = new Object();

        private static ISyncMLService mService;
        
        private long time;
        
        private boolean mSyncError;

        public SyncAdapterImpl(Context context) {
            super(context, true /* autoInitialize */);
            mContext = context;
            mContext.bindService(new Intent(mContext, SyncMLService.class), mConnection, 0);
        }
        
        private boolean lockSyncStatus(){
            
            mContext.sendBroadcast(new Intent(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN)); 
            boolean isSucc = ContactLock.lockStatus(ContactLock.TOKEN_SYNC);
            if (!isSucc) {
            	Logger.logD(TAG, ContactLock.getErrorMsg(mContext));
            }else{
                Logger.logD(TAG, "can lock sync status,will start sync");
            }
            return isSucc;
        }
        
        private void unlockSyncStatus(){
            ContactLock.unLockStatus(ContactLock.TOKEN_SYNC);
            mContext.sendBroadcast(new Intent(SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END)); 
        }
        
        private boolean isInvalidAccount() {
            return AccountAdapter.getLoginID(mContext) == null
                    || TextUtils.isEmpty(AccountAdapter.getUserData(mContext,
                    AccountAdapter.BORQS_ACCOUNT_OPTIONS_KEY_SESSION));
        }
        
        @Override
        public void onPerformSync(Account account, Bundle extras, String authority,
                ContentProviderClient provider, SyncResult syncResult) {
            Logger.logD(TAG, "===========onPerformSync, extras: " + extras.toString());
            boolean supportUpload = extras.getBoolean("upload");//if the intent is contact changing
            Logger.logD(TAG, "===========supportUpload : " + supportUpload);
            //set manual sync
            SyncHelper.setManualSync(extras.getBoolean("ignore_backoff")
                    && extras.getBoolean("ignore_settings"));
            
            if (!lockSyncStatus()) {
                return;
            }
            //check if Borqs contact change
            if(supportUpload && !clientChange()){
                Logger.logD(TAG, "the contact change,but not Borqs contacts");
                unlockSyncStatus();
                return ;
            }
            
            if(isInvalidAccount()){
                Logger.logE(TAG, "invalid account info,account or session is null,do not sync");
                unlockSyncStatus();
                return;
            }
            
           
            if (!SyncHelper.isBackgroundEnable(mContext)) {
                Log.d(TAG, "onPerformSync() exit for background data is disabled!");
                unlockSyncStatus();
                return;
            }

            try {
                //init sync failed
                mSyncError = false;
                //execute sync
                long start = System.currentTimeMillis();
                ScheduleSyncService.cancelScheduledSync(mContext);
                ContactsSyncAdapterService.performSync(mContext, account, extras, authority,
                        provider, syncResult);
                waitForComplete(mSyncLock);
                unlockSyncStatus();
                //use databaseError to report the fail
                Logger.logD(TAG,"mSyncError:" + mSyncError);
                syncResult.databaseError = mSyncError;
                long end = System.currentTimeMillis();
                Log.i(TAG, "===================time spent to sync:" + (end-start) + "ms");
            } catch (OperationCanceledException e) {
            }

            ProfileSyncService.actionSyncProfile(mContext);
        }
        
        @Override
        public void onSyncCanceled() {
            super.onSyncCanceled();
            try {
                if(mService != null){
                    mService.stop();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void waitForComplete(Object lockObject) {
            synchronized (lockObject) {
                Logger.logD(TAG, "onPerformSync() wait for complete....................");
                try {
                    lockObject.wait(SYNC_LOCK_TIMEOUT);
                    Logger.logD(TAG, "onPerformSync() completed....................");
                } catch (InterruptedException e) {
                }
            }
        }

        private void complete(Object lockObject) {
            synchronized (lockObject) {
                lockObject.notify();
            }
        }

        // connection for SyncMLService
        private ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                mService = ISyncMLService.Stub.asInterface((IBinder) service);
                try {
                    mService.setInBackground(false);
                    mService.registerCallBack(mCallBack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                mService = null;
                mContext.bindService(new Intent(mContext, SyncMLService.class), mConnection, 0);
            }
        };

        // CallBack for SyncMLService,we can get the sync status by it
        private ISyncMLCallBack.Stub mCallBack = new ISyncMLCallBack.Stub() {

            public void syncBegin() throws RemoteException {
            }

            public void syncEnd(int syncResult,int excpetionCode,int exceptionCategory) throws RemoteException {
                Logger.logD(TAG,"sync result:" + syncResult + ",excpetionCode is:" + excpetionCode);
                mSyncError = (syncResult == SyncHelper.SYNC_FAIL);
                SyncHelper.setLastSyncResult(mContext, syncResult,excpetionCode,exceptionCategory);
                
                complete(mSyncLock);
                ContactSyncOperator.onSyncEnd(mContext);
            }

            @Override
            public void handleRegisterCallBack(String msg, boolean showRegisterDialog)
                    throws RemoteException {
            }

            @Override
            public void updateSyncItemStatus(int item, int status) throws RemoteException {
            }

            @Override
            public void handleSyncPhase(int phase) throws RemoteException {
            }

            @Override
            public void handleAlertMsg(String msg) throws RemoteException {
                Logger.logD(TAG,"sync alert msg:" + msg);
            }

            @Override
            public void handleCallBack(String msg) throws RemoteException {
                Logger.logD(TAG,"sync msg:" + msg);
            }

        };
        
        private boolean clientChange(){
            return ContactSyncHelper.getChangedContactsCount(mContext) > 0;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapterImpl(getApplicationContext());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    /**
     * start the SyncMLService to perform sync. the Sync item is
     * Contacts:"new int[]{0}" sync mode is "fast sync".//TODO use other sync
     * mode
     * 
     * @param context
     * @param account
     * @param extras
     * @param authority
     * @param provider
     * @param syncResult
     * @throws OperationCanceledException
     */
    private static void performSync(Context context, Account account, Bundle extras,
            String authority, ContentProviderClient provider, SyncResult syncResult)
            throws OperationCanceledException {
        int syncType = extras.getInt(SYNC_TYPE_EXTAR);
        if(syncType <=0){
            syncType = AlertCode.ALERT_CODE_FAST;
        }
        long defaultProfile = SyncProfile.defaultProfile(context);
        Intent intent = new Intent(context, SyncMLService.class);
        intent.putExtra(Define.EXTRA_NAME_PROFILE, defaultProfile);
        intent.putExtra(Define.EXTRA_NAME_SYNC_MODE, syncType);
        intent.putExtra(Define.EXTRA_NAME_SYNC_ITEM, new int[] {0});
        context.startService(intent);
    }
    
}