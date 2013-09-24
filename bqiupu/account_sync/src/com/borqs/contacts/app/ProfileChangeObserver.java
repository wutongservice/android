/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.contacts.app;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract;
import com.borqs.common.contact.ContactSyncHelper;
import com.borqs.common.util.BLog;
import com.borqs.profile.AccountProfileInfo;
import com.borqs.profile.ProfileSyncService;

/**
 * User: b251
 * Date: 12/14/12
 * Time: 4:47 PM
 * Borqs project
 */
public class ProfileChangeObserver extends ContentObserver {
    private Context mContext;
    private Handler mHandler;

    public static ProfileChangeObserver create(Context context){
        Handler handler = new Handler(context.getMainLooper());
        return new ProfileChangeObserver(context.getApplicationContext(), handler);
    }

    /**
     * onChange() will happen on the provider Handler.
     *
     * @param handler The handler to run {@link #onChange} on.
     */
    private ProfileChangeObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }


    public void register(){
        mContext.getContentResolver()
                .registerContentObserver(getPeopleUri(), true, this);
        if (ContactSyncHelper.isSDK4_0Available()){
            mContext.getContentResolver()
                .registerContentObserver(getProfileUri(), true, this);
        }
    }

    public void unregister(){
        mContext.getContentResolver().unregisterContentObserver(this);
    }

    @Override
    public void onChange(boolean selfChange) {
        mHandler.removeCallbacks(mContactsChangeDetector);

        if(!ContactSyncHelper.isContactsInSyncing(mContext)){
            mHandler.postDelayed(mContactsChangeDetector, 15*1000);
         }
    }

    private void checkContactsChange() {
        BLog.d("checkContactsChange");

        if (!ContactSyncHelper.isContactsInSyncing(mContext)
            && AccountProfileInfo.create(mContext).isProfileChanged()){
            ProfileSyncService.actionSyncUpProfile(mContext);
        }
    }
    private Uri getPeopleUri(){
        return ContactsContract.RawContacts.CONTENT_URI;
    }

    private Uri getProfileUri(){
        Uri uri = null;
        if (ContactSyncHelper.isSDK4_0Available()){
            uri = ContactsContract.Profile.CONTENT_URI;
        }
        return uri;
    }

    private static final HandlerThread sWorkerThread = new HandlerThread("contact-observer");
    static {
        sWorkerThread.start();
    }
	public static final Handler sWorker = new Handler(sWorkerThread.getLooper());


    private Runnable mContactsChangeDetector = new Runnable() {
        @Override
        public void run() {
        	sWorker.post(new Runnable() {
                @Override
                public void run() {
                    checkContactsChange();
                }
            });
        }
    };

}
