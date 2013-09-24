/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.common;

import android.content.Context;
import android.content.Intent;

import com.borqs.common.transport.SimpleHttpClient;
import com.borqs.common.util.BLog;
import com.borqs.sync.client.vdata.IContactServerInfoStatus;
import com.borqs.sync.client.vdata.card.ContactServerInfoOperator;

public class ContactSyncOperator {

    public static final String KEY_INTENT_ACTION_BORQS_ID_READY = "ready";

    public static final String INTENT_ACTION_BORQS_ID_READY = "com.borqs.intent.action.BORQSID_READY";

    public static void onSyncEnd(Context context) {
        // Contact server info operator,like sourceid,GBorqsID
        ContactServerInfoOperator.onSyncEnd(context, SimpleHttpClient.get(),
                new ContactSyncOperator().new ContactServerInfoReady());
    }

    class ContactServerInfoReady implements IContactServerInfoStatus {

        @Override
        public void onSourceIDReady(Context context) {
        }

        @Override
        public void onGBorqsIDReady(Context context) {
            broadcastBorqsIDReady(context, true);
            setBorqsPlus(context);
        }

        @Override
        public void onSourceIDReadyError(Context context) {
            BLog.e("update sourceid error,may be network issue");
        }

        @Override
        public void onGBorqsIDReadyError(Context context) {
            broadcastBorqsIDReady(context, false);
            BLog.e("update global borqsid(sync3) error,may be network issue");
        }
    }

    private void broadcastBorqsIDReady(Context context, boolean ready) {
         BLog.d("send borqsId ready broadcast");
         Intent intent = new Intent(INTENT_ACTION_BORQS_ID_READY);
         intent.putExtra(KEY_INTENT_ACTION_BORQS_ID_READY, ready);
         context.sendBroadcast(intent);
    }

    public void setBorqsPlus(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                BorqsPlusManagent bpm = new BorqsPlusManagent(context);
                try {
                    bpm.setBorqsPlus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
