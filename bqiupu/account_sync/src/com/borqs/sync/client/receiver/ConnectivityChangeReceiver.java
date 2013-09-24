/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.receiver;

import java.util.Date;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.text.TextUtils;
import com.borqs.common.account.AccountAdapter;
import com.borqs.common.contact.ContactSyncHelper;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.common.SyncDeviceContext;
import com.borqs.sync.client.common.SyncHelper;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "ConnectivityChangeReceiver";
    private static final long HOUR_MILIS = 60 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            Logger.logD(TAG, "handle network changed start");
            handleNetwork(intent, context);
            Logger.logD(TAG, "handle network changed end");
        }
    }

    // ConnectivityManager.CONNECTIVITY_ACTION is and ordered intent,if spend
    // much time to handle the intent,
    // the other application can not receive the intent in time .so our
    // operation should be asynchronous
    private void handleNetwork(final Intent intent, final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Bundle b = intent.getExtras();
                if (b != null) {
                    NetworkInfo a = (NetworkInfo) b.get(ConnectivityManager.EXTRA_NETWORK_INFO);
                    String info = "Connectivity alert for " + a.getTypeName();
                    State state = a.getState();

                    if (state == State.CONNECTED) {
                        info += " CONNECTED,will check if sync";
                        Logger.logD(TAG, info);
                        // network is active,we start sync.
                        startSync(context);
                    } else if (state == State.DISCONNECTED) {
                        info += " DISCONNECTED,do nothing for sync";
                        Logger.logD(TAG, info);
                    }

                }
            }

        }).start();
    }

    private void startSync(Context context) {
        // save the check time
        long current = System.currentTimeMillis();
        SyncHelper.setSyncTimeCausedByNetworkChange(context, current);
        Logger.logD(TAG, " we check if sync after receiving network "
                + "connected notifcation and save the checkTime:" + (new Date(current).toString()));

        long synctime = SyncHelper.getSyncTimeCausedByNetworkChange(context);

        Account account = SyncHelper.getBorqsAccount(context);
        if (account == null) {
            return;
        }

        if (SyncHelper.isBackgroundEnable(context) && SyncHelper.isContactAutoSync(account)
                && current - synctime > 72 * HOUR_MILIS) {
            String accountId = AccountAdapter.getUserData(context,
                    AccountAdapter.BORQS_ACCOUNT_OPTIONS_KEY_UID);
            if (TextUtils.isEmpty(accountId)) {
                return;
            }

            SyncDeviceContext device = new SyncDeviceContext(context);
            boolean serverChanged = SyncHelper.needSyncPersonalServerContacts(accountId,
                    device.getDeviceId(), context);
            boolean clientChanged = ContactSyncHelper.getChangedContactsCount(context) > 0;
            Logger.logD(TAG, "server contacts change ? " + serverChanged + ",clientchange:" + clientChanged);
            if (serverChanged || clientChanged) {
                Logger.logD(TAG, "contacts(server or local) changed ,need sync");
                if (SyncHelper.requestSyncPersonalContacts(context, false)) {
                    Logger.logD(TAG, "all sync environment is ok,sync started");
                }
            } else {
                Logger.logD(TAG, "skip sync,because the server contacts no change");
            }
        } else {
            Logger.logD(TAG,
                    "skip sync,because the duration is less than 72 hours");
        }
    }

}
