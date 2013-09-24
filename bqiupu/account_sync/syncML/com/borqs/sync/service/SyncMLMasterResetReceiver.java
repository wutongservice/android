package com.borqs.sync.service;

import com.borqs.sync.client.common.Logger;
import com.borqs.sync.provider.SyncMLDb.Functions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class SyncMLMasterResetReceiver extends BroadcastReceiver {
	private static final String TAG="SyncMLRestReceiver";
	@Override
	public void onReceive(Context context, Intent arg1) {
		Logger.logD(TAG,"Receive master reset intent");
		context.getContentResolver().update(Functions.MASTER_RESET_URI, null, null, null);
		
	}

}
