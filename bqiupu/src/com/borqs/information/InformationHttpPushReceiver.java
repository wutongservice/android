package com.borqs.information;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InformationHttpPushReceiver extends BroadcastReceiver {
	public static final int HTTPPUSH = 4391;

	@Override
	public void onReceive(Context context, Intent intent) {
		String app_id = intent.getStringExtra("app_id");
		if(!"101".equals(app_id)) {
			return;
		}
		context.startService(new Intent(context,InformationDownloadService.class));  
	}

}
