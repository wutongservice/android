/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.client.receiver;

import android.accounts.Account;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.borqs.common.contact.ContactSyncHelper;
import com.borqs.sync.client.common.Logger;

import java.util.Date;

/**
 * Date: 3/7/12
 * Time: 3:03 PM
 * Borqs project
 */
public class ScheduleSyncService extends Service {
    private static final String TAG = "ScheduleSyncService";
    private static final String ACTION_CONTACT_SYNC = "com.borqs.contactservice.action.CONTACT_SYNC";
    
    private static final String SERVICE_EXTRA_FIRST_SYNC = "first_sync";

    private static final long FIRST_SYNC_DELAY = 1000 * 60 * 2; //2 minutes


    public static void scheduleFirstSync(Context context){
        Intent i = createAlarmIntent(context);
        i.putExtra(SERVICE_EXTRA_FIRST_SYNC, true);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        long timeNow = SystemClock.elapsedRealtime();
        long nextCheckTime = timeNow + FIRST_SYNC_DELAY;
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime, pi);
        Logger.logD(TAG, "============scheduleFirstSync() at " + new Date(nextCheckTime).toLocaleString());
    }

    public static void cancelScheduledSync(Context context){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = createAlarmIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.cancel(pi);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null){
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        String action = intent.getAction();
        Logger.logD(TAG, "============onStartCommand() action: " + action);

        if(ACTION_CONTACT_SYNC.equals(intent.getAction())){
            Logger.logD(TAG, "============onStartCommand() first sync? " + intent.getBooleanExtra(SERVICE_EXTRA_FIRST_SYNC, false));
            if(intent.getBooleanExtra(SERVICE_EXTRA_FIRST_SYNC, false)){
                startFirstSync();
            }
        }
        
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private static Intent createAlarmIntent(Context context) {
        Intent i = new Intent();
        i.setClass(context, ScheduleSyncService.class);
        i.setAction(ACTION_CONTACT_SYNC);
        return i;
    }

    private void startFirstSync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Account account = ContactSyncHelper.getBorqsAccount(ScheduleSyncService.this);
                if (account != null) {
                    ContactSyncHelper.requestContactsSyncOnAccount(account);
                    ScheduleSyncService.this.stopSelf();
                }
            }

        }).start();

    }
}
