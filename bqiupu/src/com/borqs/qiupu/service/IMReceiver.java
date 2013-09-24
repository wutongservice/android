package com.borqs.qiupu.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import twitter4j.ChatInfo;
import twitter4j.QiupuUser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.information.util.InformationUtils;
//import com.borqs.notification.INotificationService;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.db.UserProvider;
import com.borqs.qiupu.ui.bpc.IMComposeActivity;

public class IMReceiver extends BroadcastReceiver {

    private static final String TAG = "Qiupu.IMReceiver";

    private static final String ACTION_NOTIFICATION_NOTIFY = "com.borqs.notification.notify";
    private static final int RECEIVE_CHAT_MSG = 0;
    private IMHandler mIMHandler;
    private static IMReceiver mIMReceiver;
    private static final int FROM_TYPE = 0;

    public IMReceiver() {
        mIMHandler = new IMHandler();
    }

    static IMReceiver createInstance(QiupuService service) {
        mIMReceiver = new IMReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFICATION_NOTIFY);
        service.registerReceiver(mIMReceiver, intentFilter);
        Log.d(TAG, "bindService()");
        bindService(service);
        return mIMReceiver;
    }

    static void bindService(QiupuService service) {
        try {
            Intent intent = new Intent();
            intent.setAction("com.borqs.notification.StartService");
            if (!service.bindService(intent, mConnection, android.content.Context.BIND_AUTO_CREATE)) {
                Log.w(TAG, "bindService, failed.");
            }
        } catch (Exception ee) {
            Log.e(TAG, ee.getMessage());
        }
    }

    static void unregisterService(Context context) {
//        mNotificationService = null;
        context.unbindService(mConnection);
    }

//    static INotificationService mNotificationService;
    private static ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
//            mNotificationService = null;
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
//            mNotificationService = INotificationService.Stub.asInterface(arg1);
//            queryUserOnlineStatus();
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_NOTIFICATION_NOTIFY)) {
            Bundle bundle = intent.getExtras();
//            String app_id = bundle.getString("app_id");
            if (bundle != null && String.valueOf(IntentUtil.WUTONG_NOTIFICATION_ID).equals(bundle.getString("app_id"))) {
                Log.d(TAG, "Received appId = " + bundle.getString("app_id"));
                Log.d(TAG, "Received data = " +  bundle.getString("data"));
                Log.d(TAG, "From = " + bundle.getString("from"));

                boolean isMainRunning = InformationUtils.isActivityOnTop(context, "com.borqs.qiupu.ui.bpc.IMComposeActivity");
                Log.d(TAG, "isMainRunning = " + isMainRunning);
                if (isMainRunning) {
                    Message msg = mIMHandler.obtainMessage(RECEIVE_CHAT_MSG);
                    msg.getData().putString("app_id", bundle.getString("app_id"));// app_id = 16
                    msg.getData().putString("data", bundle.getString("data"));// chat message
                    msg.getData().putString("from", bundle.getString("from"));// borqs_id
                    msg.sendToTarget();
                } else {
                    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification n = new Notification(R.drawable.ic_bpc_launcher, context.getString(R.string.notification_msg), System.currentTimeMillis());
                    n.flags = Notification.FLAG_AUTO_CANCEL;
                    n.defaults = Notification.DEFAULT_SOUND;

                    Intent it = new Intent(context, IMComposeActivity.class);

                    String message = bundle.getString("data");

//                    ArrayList<ChatInfo> chatList = new ArrayList<ChatInfo>();
                    String to_url = QiupuORM.getInstance(context).getUserProfileImageUrl(AccountServiceUtils.getBorqsAccountID());
                    QiupuUser qiupuUser = QiupuUser.instance(QiupuORM.getInstance(context).querySimpleUserInfo(Long.valueOf(bundle.getString("from"))));
//                    ChatInfo chatItem = new ChatInfo(message, 0, qiupuUser.profile_image_url, to_url, false, true);
//                    chatList.add(chatItem);
//                    String im_file = QiupuHelper.getChatCacheFileName(bundle.getString("from"));
//                    shootSerializeIMRecord(context, chatList, im_file);

                    QiupuORM.getInstance(context).insertMessage(context, qiupuUser, message, FROM_TYPE);

                    it.putExtra("user", qiupuUser);
                    it.putExtra("to_url", to_url);

//                    it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP/*|Intent.FLAG_ACTIVITY_NEW_TASK*/);

                    PendingIntent contentIntent = PendingIntent.getActivity(
                            context, R.string.app_name, it, PendingIntent.FLAG_UPDATE_CURRENT);
                    n.setLatestEventInfo(context, context.getString(R.string.notification_msg), message, contentIntent);
                    nm.notify(R.string.app_name, n);
                }
            }
        }
    }

    private class IMHandler extends Handler {

        public IMHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECEIVE_CHAT_MSG:
                    if (imServiceListener != null && imServiceListener.get() != null) {
                        imServiceListener.get().updateUI(msg);
                    } else {
                        Log.d(TAG, "error, no listener.");
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public interface IMServiceListener {
        public void updateUI(Message msg);
    }

    private static WeakReference<IMServiceListener> imServiceListener;

    public static void setUpdateAbsentUIListener(IMServiceListener listener) {
        imServiceListener = new WeakReference<IMServiceListener>(listener);
    }

}
