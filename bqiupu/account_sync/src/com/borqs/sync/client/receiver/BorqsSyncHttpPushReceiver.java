
package com.borqs.sync.client.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.borqs.sync.client.common.AppConstant;
import com.borqs.sync.client.common.Logger;
import com.borqs.sync.client.push.PushData;
import com.borqs.sync.client.push.PushOperation;

public class BorqsSyncHttpPushReceiver extends BroadcastReceiver {

    private static final String TAG = "BorqsSyncHttpPushReceiver";
    private static final String INTENT_ACTION_NOTIFICATION_NOTIFY = "com.borqs.notification.notify";
    private static final String PUSH_DATA_ACTION_REQUEST_SYNC = "REQUEST_SYNC";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.logD(TAG, "===========receive action:" + intent.getAction());
        String appid = intent.getStringExtra("app_id");
        Logger.logD(TAG, "appid :" + appid);
        if (!AppConstant.BORQS_SYNC_APP_ID.equals(appid)) {
            Logger.logD(TAG, "not sync app,do nothing");
            return;
        }
        String action = intent.getAction();
        if (action.equals(INTENT_ACTION_NOTIFICATION_NOTIFY)) {
            String data = intent.getStringExtra("data");
            Logger.logD(TAG, "receive data :" + data);
            PushData pushData = PushData.parse(data);
            // request sync
            if (PUSH_DATA_ACTION_REQUEST_SYNC.equals(pushData.getDataAction())) {
                PushOperation pushOperation = new PushOperation();
                pushOperation.requestSync(context, pushData);
            }
        }
    }

}
