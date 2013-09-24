package com.borqs.account.login.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.borqs.account.login.util.Configuration;

public class EnableComponentsReceiver  extends BroadcastReceiver {

    public static final String INTENT_ACTION_ACCOUNT_ENABLECOMPONENTS = "com.borqs.account.enablecomponents";
    public static final String EXTRA_PARAM_VERSION = "version";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(INTENT_ACTION_ACCOUNT_ENABLECOMPONENTS)){
            String version = intent.getStringExtra(EXTRA_PARAM_VERSION);
            String localversion = Configuration.getAccountServerVersion(context);
            //ServiceConnectionOperator.uninitComponents(false);
            if(!version.equals(localversion)){
              //TODO  AccountGlobal.doDisableComponents(context);
            }
            //ServiceConnectionOperator.uninitComponents(true);
        }
    }

}
