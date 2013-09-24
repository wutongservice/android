/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.sync.client.common;

import android.content.Context;
import android.text.TextUtils;
import com.borqs.common.util.BLog;
import com.borqs.sync.store.ContactSyncSettings;

import java.util.UUID;

/**
 * Date: 7/20/12
 * Time: 12:59 PM
 * Borqs project
 */
public class SyncDeviceContext {
    private static String CONTACT_SYNC_DEVID = "contact_sync_devid";
    private static String CONTACT_SYNC_LAST_SUCCESS_ANCHOR = "contact_sync_last_anchor";
    private static String CONTACT_SYNC_LAST_START_ANCHOR = "contact_sync_last_start_anchor";

    private Context mContext;
    private ContactSyncSettings mShareData;

    public SyncDeviceContext(Context context){
        mContext = context;
        mShareData = new ContactSyncSettings(context);
    }

    public String getDeviceId(){
        String devId = mShareData.get(CONTACT_SYNC_DEVID, null);

        if(devId == null){
            String imei = com.borqs.syncml.ds.imp.common.Util.getImei(mContext);
            if (TextUtils.isEmpty(imei)) {
                devId = "UUID:" + UUID.randomUUID().toString();
            } else {
                devId = "IMEI:" + imei;
            }
            BLog.d("getDeviceId() create new device id:" + devId);
            mShareData.put(CONTACT_SYNC_DEVID, devId);
        }

        return devId;
    }


    public long getLastSuccessSyncAnchor(){
        long anchor =  mShareData.getLong(CONTACT_SYNC_LAST_SUCCESS_ANCHOR, 0);
        BLog.d("getLastAnchor() : " + anchor);
        return anchor;
    }

    public void setLastSuccessSyncAnchor(long lastAnchor){
        BLog.d("setLastAnchor() : " + lastAnchor);
        mShareData.putLong(CONTACT_SYNC_LAST_SUCCESS_ANCHOR, lastAnchor);
    }
    
    public long getLastSyncAnchor(){
        long anchor =  mShareData.getLong(CONTACT_SYNC_LAST_START_ANCHOR, 0);
        BLog.d("getLastSyncStartAnchor() : " + anchor);
        return anchor;
    }

    public void setLastSyncAnchor(long lastAnchor){
        BLog.d("setLastSyncStartAnchor() : " + lastAnchor);
        mShareData.putLong(CONTACT_SYNC_LAST_START_ANCHOR, lastAnchor);
    }
}
