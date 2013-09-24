/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.push;

import android.accounts.Account;
import android.content.Context;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.common.SyncDeviceContext;
import com.borqs.sync.client.common.SyncHelper;

import java.util.List;

public class PushOperation {

    private static final String TAG = "PushOperation";

    public PushOperation() {
    }

    public void requestSync(Context context, PushData pushData) {
        Account account = SyncHelper.getBorqsAccount(context);
        if (account == null) {
            return;
        }
        if (SyncHelper.isBackgroundEnable(context) && SyncHelper.isContactAutoSync(account)
                && needSync(pushData, context)) {
            Logger.logD(TAG, "need sync personal contacts,request sync now.");
            SyncHelper.requestSyncPersonalContacts(context, !SyncHelper.isInitSyncSuccess(context));
        }
    }

    private boolean needSync(PushData pushData, Context context) {
        SyncDeviceContext device = new SyncDeviceContext(context);
        String deviceId = device.getDeviceId();
        Logger.logD(TAG, "current deviceId: " + deviceId);
        List<String> devices = pushData.getNeedSyncDevices();
        return devices.contains(deviceId);
    }

}
