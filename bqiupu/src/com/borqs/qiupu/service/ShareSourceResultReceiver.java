package com.borqs.qiupu.service;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.QiupuMessage;

/**
 * Created by IntelliJ IDEA.
 * User: zixuan
 * Date: 11-11-21
 * Time: 下午11:20
 * To change this template use File | Settings | File Templates.
 */
public class ShareSourceResultReceiver extends BroadcastReceiver {
    private final static String TAG = "ShareSourceResultReceiver";

//    public class ShareSourceItem implements java.io.Serializable {
//		private static final long serialVersionUID = 8020166132262934739L;
//		public String mId;
//        public String mLabel;
//        public String mScheme;
//        public int mCount;
//        public int mType;
//
//        public ShareSourceItem(String id) {
//            mId = id;
//        }
//    }

    private static ServiceHandler mHandler;

    public static Map<String, ShareSourceItem> mShareSourceMap = new HashMap<String, ShareSourceItem>();

    public ShareSourceResultReceiver(List<ResolveInfo> infoList) {
        String pkgName;
        if(infoList == null){
        	return ;
        }
        final int size = infoList.size();

        for (int i = 0; i < size; ++i) {
            ActivityInfo ai = infoList.get(i).activityInfo;
            ServiceInfo si = infoList.get(i).serviceInfo;
            if (null != ai && !TextUtils.isEmpty(ai.packageName)) {
                pkgName = ai.packageName;
            } else if (null != si && !TextUtils.isEmpty(si.packageName)) {
                pkgName = si.packageName;
            } else {
                Log.i(TAG, "ShareSourceResultReceiver, not activity or service in: " + infoList.get(i));
                continue;
            }

            if (mShareSourceMap.get(pkgName) == null) {
                mShareSourceMap.put(pkgName, new ShareSourceItem(pkgName));
            }
        }

        mHandler = new ServiceHandler();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BpcApiUtils.Action.SHARE_SOURCE_RESULT.equals(intent.getAction())) {
            Bundle bundle = intent.getBundleExtra(BpcApiUtils.Action.SHARE_SOURCE_RESULT);
            final String pkgName = bundle.getString(BpcApiUtils.Result.PACKAGE_NAME);
            final String label = bundle.getString(BpcApiUtils.Result.SOURCE_LABEL);
            final int count = bundle.getInt(BpcApiUtils.Result.SOURCE_COUNT);
            final String scheme = bundle.getString(BpcApiUtils.Result.CALLBACK_SCHEME);
            final String target = bundle.getString(BpcApiUtils.Result.TARGET_PACKAGE);
            Log.d(TAG, String.format("onReceive, result pkgName:%s, label:%s, count:%d, scheme:%s.",
                    pkgName, label, count, scheme));

            ShareSourceItem item = mShareSourceMap.get(pkgName);
            if (item == null) {
                item = new ShareSourceItem(pkgName);
                mShareSourceMap.put(pkgName, item);
            }

            item.mLabel = label;
            item.mScheme = scheme;
            item.mTarget = target;
            item.mCount = count;

            mShareSourceMap.put(pkgName, item);

            Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_REFRESH_UI);
            msg.sendToTarget();
        }

//        Intent callbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
//        callbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (BpcApiUtils.isActivityReadyForIntent(context, callbackIntent)) {
//            context.startActivity(callbackIntent);
//        } else {
//            Log.d(TAG, "onReceive, no activity for ACTION_VIEW scheme: " + scheme);
//        }
    }

    private static final HashMap<String, WeakReference<ShareSourceServiceListener>> listeners =
            new HashMap<String, WeakReference<ShareSourceServiceListener>>();

    public interface ShareSourceServiceListener {
        public void updateUI(int msgcode, Message msg);
    }

    public static void registerServiceListener(String key, ShareSourceServiceListener listener) {
        if (listeners.get(key) == null) {
            synchronized (listeners) {
            	WeakReference<ShareSourceServiceListener> ref = listeners.get(key);            	
            	if(ref != null && ref.get() != null)
            	{
            		ref.clear();
            	}
                listeners.put(key, new WeakReference<ShareSourceServiceListener>(listener));
            }
        }
    }

    public static void unregisterServiceListener(String key) {
        synchronized (listeners) {
            listeners.remove(key);
        }
    }

    private static void updateActivityUI(int msgcode, Message msg) {
//    	if(QiupuConfig.LowPerformance)
        Log.d(TAG, "updateActivityUI msgcode:" + msgcode + " listener count:" + listeners.size());
        synchronized (listeners) {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if(listeners.get(key) != null)
                {
	                ShareSourceServiceListener listener =  listeners.get(key).get();
	                if (listener != null) {
	                    listener.updateUI(msgcode, msg);
	                }
                }
            }
        }
    }

    private class ServiceHandler extends Handler {
        public ServiceHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case QiupuMessage.MESSAGE_REFRESH_UI:
                    updateActivityUI(QiupuMessage.MESSAGE_REFRESH_UI, msg);
                    break;
                default:
            }
        }
    }
}

;

