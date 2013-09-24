package com.borqs.common.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.QiupuORM;
//
//
///**
// * Created with IntelliJ IDEA.
// * User: b608
// * Date: 12-9-17
// * Time: 下午3:21
// * To change this template use File | Settings | File Templates.
// */
public class PushingServiceAgent {
//    private static final String TAG = "PushingServiceAgent";
//
//    /// enable via this code line or disable via next 2 code line.
////    private static final boolean SOURCE_CODE_INT = true;
//
//    private static final boolean SOURCE_CODE_INT = false;
//    private static com.borqs.notification.Launcher launcher;
//    private static ServiceConnection serviceConnection = new ServiceConnection() {
//        public void onServiceDisconnected(ComponentName name) {
//            Log.d(TAG, "onServiceDisconnected name:" + name);
//            launcher = null;
//        }
//
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.d(TAG, "onServiceConnected name:" + name);
//        }
//    };
//    private static void bindServiceEx(final Context context) {
//        // enable code if SOURCE_CODE_INT = false.
//
//        if (launcher == null) {
//            launcher = com.borqs.notification.Launcher.getInstance(context.getApplicationContext());
//        } else {
//            Log.i(TAG, "bindServiceEx, skip while it is existing.");
//            return;
//        }
//
//        QiupuORM.sWorker.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Intent intent = new Intent();
//                    ComponentName component = launcher.launchService(0);
//                    intent.setComponent(component);
//                    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//                } catch (Exception e) {
//                    Log.e(TAG, " bindNotificationService: " + e.getMessage());
//                }
//            }
//        });
//    }
//
//    private static void stopServiceEx() {
//        // enable code if SOURCE_CODE_INT = false.
//        if (null != launcher) {
//            launcher.stopService(0);
//            launcher = null;
//        }
//    }
//
//    private static Uri getContentUriStringEx(Context context) {
//        // enable code if SOURCE_CODE_INT = false.
//        return Uri.parse(com.borqs.notification.Launcher.getInstance(context).getContentUriString());
////        return null; // only use to make compiler happy while SOURCE_CODE_INT, or use above code.
//    }

    private static final ComponentName pushService =
            new ComponentName(QiupuConfig.APP_PACKAGE_NAME, "com.borqs.notification.NotificationService");
    private static final ComponentName pushReceiver =
            new ComponentName(QiupuConfig.APP_PACKAGE_NAME, "com.borqs.notification.NotificationReceiver");
    public static void enablePushServiceComponent(Context context, boolean enabled) {
        PackageManager packageManager = context.getPackageManager();
        final int flag = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        packageManager.setComponentEnabledSetting(pushService, flag,
                PackageManager.DONT_KILL_APP);
        packageManager.setComponentEnabledSetting(pushReceiver, flag,
                PackageManager.DONT_KILL_APP);
    }

    public static void bindNotificationService(final Context context) {
//        Log.d(TAG, "start  bind NotificationService !!");
//        if (SOURCE_CODE_INT) {
//            enablePushServiceComponent(context, true);
//        } else {
//            bindServiceEx(context);
//        }
    }
//
    public static void stopNotificationService(final Context context) {
//        if (SOURCE_CODE_INT) {
//            enablePushServiceComponent(context, false);
//        } else {
//            stopServiceEx();
//        }
    }
//
//    private static Uri getContentUriString(Context context) {
//        if (SOURCE_CODE_INT) {
//            return Uri.parse("content://com.borqs.notification/user");
//        } else {
//            return getContentUriStringEx(context);
//        }
//    }
//
//    private static String[] contentProject = new String[]{"borqs_id", "status"};
//    private static String[] contentProjectEx = new String[]{"borqs_id", "status", "ip", "capability"};
//
//    private static String[] getContentProject() {
//        return SOURCE_CODE_INT ? contentProject : contentProjectEx;
//    }
//
//    public static boolean queryOnlineUser(Context context, String uid) {
//        boolean ret = false;
//        Cursor cursor = null;
//        try {
//            Uri contentUri = getContentUriString(context);
//            cursor = context.getContentResolver()
//                    .query(contentUri,
//                            getContentProject(), "borqs_id=" + uid,
//                            null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                Log.e(TAG, "borqs_id is:" + uid + "status is:" + cursor.getString(cursor
//                        .getColumnIndex("status")));
//                if ("1".equals(cursor.getString(cursor.getColumnIndex("status")))) {
//                    ret = true;
//                }
//            }
//        } catch (Exception ex) {
//            Log.e(TAG, "Exception:" + ex.toString());
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return ret;
//    }
}
