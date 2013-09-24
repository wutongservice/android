/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.receiver;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import com.borqs.common.account.AccountAdapter;
import com.borqs.common.util.BLog;
import com.borqs.contacts.app.ContactsApp;
import com.borqs.sync.provider.SyncMLDb;
import com.borqs.sync.service.LocalSyncMLProvider;

public class AccountUpdateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
        BLog.d("Receive message: " + intent.getAction());
        if (AccountAdapter.INTENT_ACTION_ACCOUNT_LOGIN.equals(intent.getAction())) {
            onAccountLogin(context);
            ScheduleSyncService.scheduleFirstSync(context);
        }

        if (AccountAdapter.INTENT_ACTION_ACCOUNT_LOGOUT.equals(intent.getAction())) {
            onAccountLogout(context);
            ScheduleSyncService.scheduleFirstSync(context);
        }

        if("com.borqs.action.OVERRIDE_CONTACTS_SERVICE".equals(intent.getAction())){
            BLog.d("disabled contact service in pacage '" + context.getPackageName() +"'");
            new ContactsApp().disableContactService(context);
        }
	}

    private void onAccountLogin(Context context){
        //1.set account visible, then sync
        Account account = AccountAdapter.getBorqsAccount(context);
        if (account != null) {
            // set account visible
            setAccountVisible(context, account);
            // start sync.
            // sync after sync account setup finished.
            ContentResolver.setIsSyncable(account,
                    ContactsContract.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account,
                    ContactsContract.AUTHORITY, true);
        }

        try{
            LocalSyncMLProvider.delete(SyncMLDb.Functions.CLEAR_SYNC_INFO_URI, null, null);
        }finally {
            LocalSyncMLProvider.close();
        }
    }

    private void onAccountLogout(Context context){
        try{
            LocalSyncMLProvider.delete(SyncMLDb.Functions.CLEAR_SYNC_INFO_URI, null, null);
        }finally {
            LocalSyncMLProvider.close();
        }
    }

    private void setAccountVisible(Context context, Account account) {
        // Make sure ungrouped contacts for borqs account are defaultly visible
        ContentProviderClient client = context.getContentResolver()
                .acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
        ContentValues cv = new ContentValues();
        cv.put(ContactsContract.Settings.ACCOUNT_NAME, account.name);
        cv.put(ContactsContract.Settings.ACCOUNT_TYPE, account.type);
        cv.put(ContactsContract.Settings.UNGROUPED_VISIBLE, true);
        try {
            if (!hasBorqsSettings(context, account)) {
                client.insert(
                        addCallerIsSyncAdapterParameter(ContactsContract.Settings.CONTENT_URI),
                        cv);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean hasBorqsSettings(Context context, Account account) {
        ContentProviderClient client = context.getContentResolver()
                .acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
        try {
            Cursor cursor = client.query(
                    addCallerIsSyncAdapterParameter(ContactsContract.Settings.CONTENT_URI),
                    null, ContactsContract.Settings.ACCOUNT_TYPE + "=? AND "
                    + ContactsContract.Settings.ACCOUNT_NAME + "=?", new String[] {
                    account.type, account.name }, null);
            if (cursor != null) {
                try {
                    return cursor.getCount() > 0;
                } finally {
                    cursor.close();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri
                .buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                        "true").build();
    }

}
