/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.contact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import com.borqs.common.account.AccountAdapter;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.sync.ds.datastore.contacts.ContactsSrcOperator;
import com.borqs.syncml.ds.imp.common.Constant;
import com.borqs.syncml.ds.protocol.IPimInterface2;

/**
 * Date: 4/24/12
 * Time: 6:23 PM
 * Borqs project
 */
public class ContactSyncHelper {
    public static Account getBorqsAccount(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();
        if (accounts != null) {
            for (Account account : accounts) {
                if (Constant.BORQS_ACCOUNT_TYPE.equals(account.type)) {
                    return account;
                }
            }
        }
        return null;
    }

    public static void requestContactsSyncOnAccount(Account a){
        if(a == null || !AccountAdapter.BORQS_ACCOUNT_TYPE.equals(a.type)){
            return;
        }
        // start sync.
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL,
                true);
        ContentResolver.requestSync(a,
                ContactsContract.AUTHORITY, extras);
        Context context = ApplicationGlobals.getContext();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(ApplicationGlobals.NOTIFICATION_ID_NEED_SYNC);
    }

    public static int getChangedContactsCount(Context context){
        /*if(AccountAdapter.getAccountId(context) == null){
            return 0;
        }

        ChangeLogCollection clc = new ChangeLogCollection(context.getContentResolver());
        // get the contacts change_log
        PimSyncmlInterface<?> contact = new ContactsVCardOperator(true);
        IPimInterface contactPim = new ContactsInterface(context.getContentResolver(),
                (PimSyncmlInterface<Object>) contact, "text/x-vcard",
                Constant.PREFIX_CONTACTS, ContactsContract.RawContacts.CONTENT_URI);
        SyncProfile profile = new SyncProfile(SyncProfile.defaultProfile(context), context);
        long syncSourceId = profile.getSyncSourceId(Define.SYNC_ITEMS_INT_CONTACTS);
        OrderedObjectSelection selection = clc.executeChangeCollection(contactPim, syncSourceId);
        return selection.getChangedSize();*/
        int count = 0;
        Account borqsAccount = AccountAdapter.getBorqsAccount(context);
        if (borqsAccount != null){
            IPimInterface2 contact = new ContactsSrcOperator(context, borqsAccount);
            count = contact.getChangedItemCount();
        }
        
        return count;
    }
    
    //copy from SyncML engine
    public static boolean canAutoSyncContacts(Context context){
        Account borqsAccount = ContactSyncHelper.getBorqsAccount(context);
        boolean contactsSyncEnalbed = ContentResolver.getSyncAutomatically(borqsAccount, ContactsContract.AUTHORITY);
        boolean systemSyncEnabled = ContentResolver.getMasterSyncAutomatically();
        int isSyncAble = ContentResolver.getIsSyncable(borqsAccount, ContactsContract.AUTHORITY);
        
        return isSyncAble > 0 && systemSyncEnabled && contactsSyncEnalbed;
    }

    public static boolean isContactsInSyncing(Context context){
        Account borqsAccount = ContactSyncHelper.getBorqsAccount(context);
        return ContentResolver.isSyncActive(borqsAccount, ContactsContract.AUTHORITY);
    }
    
    public static boolean isSDK4_0Available() {
        boolean isAvailable = false;
        try {
            int sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
            isAvailable = sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        } catch (NumberFormatException e) {
        }
        return isAvailable;
    }
}
