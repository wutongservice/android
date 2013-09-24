
package com.borqs.contacts.app;

import android.accounts.Account;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.borqs.common.account.Configuration;
import com.borqs.common.contact.ContactSyncHelper;
import com.borqs.common.util.BLog;
import com.borqs.contacts_plus.R;
import com.borqs.sync.client.activity.BorqsPlusTransitActivity;
import com.borqs.sync.client.activity.OpenfacePlusTransitActivity;
import com.borqs.sync.client.activity.SyncMainActivity;
import com.borqs.sync.client.activity.WutongPlusTransitActivity;
import com.borqs.sync.client.download.AppDownloadReceiver;
import com.borqs.sync.client.receiver.AccountUpdateReceiver;
import com.borqs.sync.client.receiver.BorqsSyncHttpPushReceiver;
import com.borqs.sync.client.receiver.ConnectivityChangeReceiver;
import com.borqs.sync.service.ContactsSyncAdapterService;
import com.borqs.sync.service.SyncMLService;

import java.util.List;

public class ContactsApp{
    private static boolean mIsLowStorage = false;
    private ProfileChangeObserver mContactsObserver;

    /**
     * set the status of low Storage
     */
    public static void setIsLowStorage(boolean isLowStorage) {
        mIsLowStorage = isLowStorage;
    }

    /**
     * get the status of low Storage
     */
    public static boolean isLowStorage() {
        return mIsLowStorage;
    }
    
    public void onCreate(Context context) {
        ApplicationGlobals.setContext(context.getApplicationContext());

        clearOneTimeLoginCache(context);
        NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNM.cancel(R.string.syncml_service_started);
        
        enableSyncAdapter(context);

        if(isContactServiceEnabledInSystem(context)){
            disableContactService(context);
            return;
        } else {
            enableContactService(context);
        }

        // register system event
        registerSystemEventReceiver(context);

        mContactsObserver = ProfileChangeObserver.create(context);
        mContactsObserver.register();
    }
    
    public void onTerminate(Context context) {
        // unregister system event
        unregisterSystemEventReceiver(context);
        if(mContactsObserver != null){
            mContactsObserver.unregister();
        }
    }

    /**
     * mSystemEventReceiver
     */
    private BroadcastReceiver mSystemEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_DEVICE_STORAGE_OK)) {
                // storage is ok
                ContactsApp.setIsLowStorage(false);
            } else if (action.equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
                // storage is low
                ContactsApp.setIsLowStorage(true);
            }
        }
    };

    /**
     * register System Event Receiver
     */
    private void registerSystemEventReceiver(Context context) {
        // register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        filter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        context.registerReceiver(mSystemEventReceiver, filter);
    }

    /**
     * unregister System Event Receiver
     */
    private void unregisterSystemEventReceiver(Context context) {
        context.unregisterReceiver(mSystemEventReceiver);
    }

    public static void saveOneTimeLoginCache(Context context, String key, String value){
        SharedPreferences sp = context.getSharedPreferences("login_in", Context.MODE_PRIVATE);
        String prefix = Configuration.getSharedPreferencePrefix(context);
        sp.edit().putString(prefix+key, value).commit();
    }
    
    public static String getLoginCachedValue(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences("login_in", Context.MODE_PRIVATE);
        String prefix = Configuration.getSharedPreferencePrefix(context);
        return sp.getString(key, null);
    }
    
    private static void clearOneTimeLoginCache(Context context){
        context.getSharedPreferences("login_in", Context.MODE_PRIVATE).edit().clear().commit();
    }

    private boolean isContactServiceEnabledInSystem(Context context){
        BLog.d("isContactServiceEnabledInSystem() running in " + context.getPackageName());
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("android.content.SyncAdapter");
        List<ResolveInfo> targets = pm.queryIntentServices(intent, PackageManager.GET_META_DATA);
        for(ResolveInfo info : targets){
            if(info == null || info.serviceInfo == null){
                continue;
            }
            Bundle metaData = info.serviceInfo.metaData;
            if(metaData != null && "contacts".equalsIgnoreCase(metaData.getString("borqs.service.SyncAdapter"))){
                BLog.d("ContactSyncAdapter is enabled in " + info.serviceInfo.packageName);
                if(!info.serviceInfo.packageName.equals(context.getPackageName())){
                    return true;
                }
            }
        }
        return false;
    }

    private void enableContactService(Context context){
        BLog.d("ContactSyncAdapter will be enabled in " + context.getPackageName());

        ComponentName[] components = new ComponentName[]{
            new ComponentName(context, ContactsSyncAdapterService.class),
            new ComponentName(context, SyncMLService.class),
            new ComponentName(context, AccountUpdateReceiver.class),
            new ComponentName(context, BorqsSyncHttpPushReceiver.class),
            /*borqs plus*/
            new ComponentName(context, WutongPlusTransitActivity.class),
            new ComponentName(context, OpenfacePlusTransitActivity.class),
            new ComponentName(context, BorqsPlusTransitActivity.class),
            new ComponentName(context, AppDownloadReceiver.class),
            /*borqs plus*/
            new ComponentName(context, SyncMainActivity.class),
            new ComponentName(context, ConnectivityChangeReceiver.class)
        };
        PackageManager pm = context.getPackageManager();
        for(ComponentName cn : components){
            pm.setComponentEnabledSetting(
                    cn,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED ,
                    PackageManager.DONT_KILL_APP);
        }
    }

    public void disableContactService(Context context){
        BLog.d("ContactSyncAdapter will be disabled in " + context.getPackageName());

        ComponentName[] components = new ComponentName[]{
                new ComponentName(context, ContactsSyncAdapterService.class),
                new ComponentName(context, SyncMLService.class),
                new ComponentName(context, AccountUpdateReceiver.class),
                new ComponentName(context, BorqsSyncHttpPushReceiver.class),
                /*borqs plus*/
                new ComponentName(context, WutongPlusTransitActivity.class),
                new ComponentName(context, OpenfacePlusTransitActivity.class),
                new ComponentName(context, BorqsPlusTransitActivity.class),
                new ComponentName(context, AppDownloadReceiver.class),
                /*borqs plus*/
                new ComponentName(context, SyncMainActivity.class),
                new ComponentName(context, ConnectivityChangeReceiver.class)
        };
        PackageManager pm = context.getPackageManager();
        for(ComponentName cn : components){
            pm.setComponentEnabledSetting(
                    cn,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED ,
                    PackageManager.DONT_KILL_APP);
        }
    }
    
    private void enableSyncAdapter(Context context){
    	Account bAccount = ContactSyncHelper.getBorqsAccount(context);
    	if(bAccount == null){
    		BLog.d("no borqs account exist.ignore syncAdapter enable");
    		return;
    	}
    	BLog.d("exist borqs account,we enable syncadapter if it is disable");
    	if(ContentResolver.getIsSyncable(bAccount, ContactsContract.AUTHORITY) <= 0){
    		BLog.d("set isSyncable as true");
    		ContentResolver.setIsSyncable(bAccount, ContactsContract.AUTHORITY, 1);
    	}else{
    		BLog.d("contact is syncbale,do not need enable");
    	}
    	if(!ContentResolver.getSyncAutomatically(bAccount, ContactsContract.AUTHORITY)){
    		BLog.d("setSyncAutomatically as true");
    		ContentResolver.setSyncAutomatically(bAccount, ContactsContract.AUTHORITY, true);
    	}else{
    		BLog.d("contact is SyncAutomatically,no not need enable");
    	}
    }
}
