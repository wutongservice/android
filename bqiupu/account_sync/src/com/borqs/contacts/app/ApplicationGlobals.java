package com.borqs.contacts.app;

import android.content.Context;
import com.borqs.common.util.BLog;

public class ApplicationGlobals {

private static Context context;
    public static int NOTIFICATION_ID_NEED_SYNC = 201;
    public static int NOTIFICATION_ID_NEED_MERGE = 202;
    public static int NOTIFICATION_ID_NEED_IMPORT = 203;

	public static Context getContext() {
        if(context == null){
            new Throwable().printStackTrace();
        }
		return context;
	}

	public static void setContext(Context context) {
		if(context != null) {
			ApplicationGlobals.context = context;
		}
	}

    private static void dumpEnv(String id){
        StringBuilder builder = new StringBuilder();
        builder.append("Context: " + context)
                .append(", PID: " + android.os.Process.myPid())
                .append(", TID: " + android.os.Process.myTid())
                .append(", UID: " + android.os.Process.myUid());
        BLog.d("Dump env (" + id +"): " + builder.toString());
    }
}
