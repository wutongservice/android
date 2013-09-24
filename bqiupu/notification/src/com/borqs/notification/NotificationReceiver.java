
package com.borqs.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;



public class NotificationReceiver extends BroadcastReceiver {

    static String TAG = "NotificationReceiver";
    static final int NOTIFICATION_ID = 1;
    private ConnectivityManager mConnectivityManager;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Constants.LOGD_ENABLED) {
            Log.d(TAG, "intent =" + action
                +", data = " + (bundle==null? "" : bundle.getString("data")));
        }
        
        if (action.equals(Constants.BORQS_NOTIFICATION_INTENT)) {
            // Vibrate for debugging.
            if(false) {
                // vibrate. for debug only.
                long[] pattern = {200, 400};
                Vibrator vibrator = (Vibrator) context.getSystemService(
                        Context.VIBRATOR_SERVICE);
                vibrator.vibrate(pattern, -1);
            }

            if(true && bundle != null) {
                String content = (String) bundle.get("data");
                String appId = (String) bundle.get("app_id");
                String type = (String) bundle.getString("type");
                Log.e("NotificationReceiver", "from: " + (String) bundle.get("from"));

                int id = -1;
                try {
                    id = Integer.parseInt(appId);
                } catch(Exception ee) {
                    id = -1;
                }

                // send notification 
                if(false) {
                NotificationManager nm = (NotificationManager) 
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(NOTIFICATION_ID);
                
                Notification n = new Notification(
                        android.R.drawable.stat_notify_error, 
                        "Notification", 
                        System.currentTimeMillis());
                n.flags |= Notification.FLAG_AUTO_CANCEL;

                Intent newIntent = null;
                switch(id) {
                case Constants.APP_ID_BROWSER:
                    // Browser
                    newIntent = new Intent(Intent.ACTION_VIEW, null);
                    newIntent.setDataAndType(Uri.parse(content), "text/html");
                    break;
                case Constants.APP_ID_SELF:
                    // Browser
                    newIntent = new Intent(Intent.ACTION_VIEW, null);
                    newIntent.setDataAndType(Uri.parse(content), "dummy/msg");
                    break;
                default:
                    break;
                }
                
                if(newIntent != null) {
                    PendingIntent contentIntent = PendingIntent.getActivity(
                            context, 0, newIntent, 0);
    
                    n.setLatestEventInfo(context, "Httpush", content, contentIntent);
                    nm.notify(NOTIFICATION_ID, n);
                }
            }
            }

            String s = (bundle == null ? "" : bundle.getString("action"));
            if (null == s) {
                return;
            }
            if (s.equals("urlshare")) {
                Log.i(TAG, "content="+bundle.getString("content"));
                String content = bundle.getString("content");
                if (null != content && content.length() != 0) {
                    if (!content.toLowerCase().startsWith("http://") &&
                            !content.toLowerCase().startsWith("https://")) {
                        content = "http://"+content;
                    }
                    // send notification 
                    NotificationManager nm = (NotificationManager) 
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(NOTIFICATION_ID);
                    
                    Notification n = new Notification(
                            android.R.drawable.stat_notify_error, 
                            "Httpush", 
                            System.currentTimeMillis());
                    n.flags |= Notification.FLAG_AUTO_CANCEL;
                    
                    Uri uri = Uri.parse(content);
                    Intent newIntent = new Intent(Intent.ACTION_VIEW, uri);
                    PendingIntent contentIntent = PendingIntent.getActivity(
                            context, 0, newIntent, 0);
                    n.setLatestEventInfo(context, "Httpush", content, contentIntent);
                    nm.notify(NOTIFICATION_ID, n);
                }
            }
        }
       /* } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            int wifiStatePrev = intent.getIntExtra(
                    WifiManager.EXTRA_PREVIOUS_WIFI_STATE, -1);
            // Log.d(TAG, "WIFI state is changed wifiState="+wifiState+", wifiStatPrev="+wifiStatePrev);

            if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                //Log.d(TAG, "WIFI is disconnected, will start Linx service to disconnect");
                if (Constants.LOGV_ENABLED) {
                    Log.d(TAG, "Wifi disabled.");
                }
                Intent newIntent = new Intent(context, NotificationService.class);
                newIntent.putExtra("service_command", Constants.SERVICE_CMD_WIFIDISCONNECTED);
                context.stopService(newIntent);
            }
       /* } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            Log.i("queryUser","network state change");
            NetworkInfo info = 
                intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if ((null != info) && (info.getType() == ConnectivityManager.TYPE_WIFI)) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    //Log.d(TAG, "WIFI is connected, will start Linx service");
                    Intent newIntent = new Intent(context, NotificationService.class);
                    newIntent.putExtra("service_command", Constants.SERVICE_CMD_WIFICONNECTED);
                    if (Constants.LOGV_ENABLED) {
                        Log.d(TAG, "Wifi connected.");
                    }
                    context.startService(newIntent);
                } else if (info.getState() == NetworkInfo.State.DISCONNECTED) {
                    //Log.d(TAG, "WIFI is disconnected, will start Linx service to disconnect");
                    if (Constants.LOGV_ENABLED) {
                        Log.d(TAG, "Wifi connected.");
                    }
                    Intent newIntent = new Intent(context, NotificationService.class);
                    newIntent.putExtra("service_command", Constants.SERVICE_CMD_WIFIDISCONNECTED);
                    context.stopService(newIntent);
                }*/
            else if(action.equals("com.borqs.notification.StartService")) {
                Intent newIntent = new Intent(context, NotificationService.class);
                context.startService(newIntent);
            }else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                Log.i("queryUser",
                        "recevive action android.net.conn.CONNECTIVITY_CHANGE");
                NetworkInfo info = intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (null != info) {
                    if (((!NotificationService.mIsUseGPRS) && (info.getType() == ConnectivityManager.TYPE_WIFI))
                            || (NotificationService.mIsUseGPRS)) {
                        Log.i("queryUser", "mIsUseGPRS is:"
                                + NotificationService.mIsUseGPRS);
                        Log.i("queryUser", "network type is:" + info.getType());
                        if (info.getState() == NetworkInfo.State.CONNECTED) {
                            // Log.d(TAG,
                            // "WIFI is connected, will start Linx service");
                            Intent newIntent = new Intent(context,
                                    NotificationService.class);
                            newIntent.putExtra("service_command",
                                    Constants.SERVICE_CMD_WIFICONNECTED);
                            if (Constants.LOGV_ENABLED) {
                                Log.i("queryUser",
                                        "**********************network type is:"
                                                + info.getType());
                                Log.d(TAG, "network connected.");
                                Log.i("queryUser",
                                        "******************network connected.");
                            }
                            context.startService(newIntent);
                        } else if (info.getState() == NetworkInfo.State.DISCONNECTED) {
                            Log.i("queryUser",
                                    "***************************network type is:"
                                            + info.getType());
                            Log.i("queryUser",
                                    "************************network is disconnected, will start Linx service to disconnect");
                            if (Constants.LOGV_ENABLED) {
                                Log.i("queryUser", "network disconnected.");
                            }
                            Intent newIntent = new Intent(context,
                                    NotificationService.class);
                            newIntent.putExtra("service_command",
                                    Constants.SERVICE_CMD_WIFIDISCONNECTED);
                            context.stopService(newIntent);
                        }
                    }
                }
            }
        }
    }
