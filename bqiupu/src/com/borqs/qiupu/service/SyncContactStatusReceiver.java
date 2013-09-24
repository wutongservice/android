package com.borqs.qiupu.service;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.borqs.account.service.PeopleLookupHelper.SyncIntent;
import com.borqs.qiupu.db.QiupuORM;

public class SyncContactStatusReceiver extends BroadcastReceiver {
	private final static String TAG = "SyncContactStatusReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction(); 
		Log.d(TAG, "onReceive intent.getaction : " + action);
		if(action == SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN){
			QiupuORM.setSyncContactStatus(context, SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_BEGIN);
		}else if(action == SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END) {
			QiupuORM.setSyncContactStatus(context, SyncIntent.INTENT_ACTION_BORQS_CONTACT_SYNC_END);
		}
		Message msg = new SyncContactHandler().obtainMessage(CHANGE_ADDRESSBOOK_ACTION);
		msg.getData().putString("sync_action", action);
		msg.sendToTarget();
	}
	
	private static final HashMap<String, WeakReference<AddressBookSyncContactListener>> listeners =
			new HashMap<String, WeakReference<AddressBookSyncContactListener>>();
	
	public interface AddressBookSyncContactListener {
		public void intentToAddressBook(Message msg);
	}
	
	public static void registerSyncListener(String key, AddressBookSyncContactListener listener) {
        if (listeners.get(key) == null) {
            synchronized (listeners) {
            	WeakReference<AddressBookSyncContactListener> ref = listeners.get(key);            	
            	if(ref != null && ref.get() != null)
            	{
            		ref.clear();
            	}
                listeners.put(key, new WeakReference<AddressBookSyncContactListener>(listener));
            }
        }
    }

    public static void unregisterSyncListener(String key) {
        synchronized (listeners) {
            listeners.remove(key);
        }
    }
	
	private static final int CHANGE_ADDRESSBOOK_ACTION = 1;
	private class SyncContactHandler extends Handler {
        public SyncContactHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_ADDRESSBOOK_ACTION:
                    updateActivityUI(msg);
                    break;
                default:
            }
        }
    }
	
	private static void updateActivityUI(Message msg) {
        Log.d(TAG, "listener count:" + listeners.size());
        synchronized (listeners) {
            Set<String> set = listeners.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if(listeners.get(key) != null)
                {
                	AddressBookSyncContactListener listener =  listeners.get(key).get();
	                if (listener != null) {
	                    listener.intentToAddressBook(msg);
	                }
                }
            }
        }
    }
}


