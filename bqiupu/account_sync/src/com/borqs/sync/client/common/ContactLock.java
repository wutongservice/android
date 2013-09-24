/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.common;

import android.content.Context;
import com.borqs.common.util.BLog;
import com.borqs.contacts_plus.R;

public class ContactLock {

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_BUSY = 1;

    private static Integer mStatus = STATUS_IDLE;
    private static int mAppToken = -1;
    private static int mLastAppToken = -1;
    
    public static final int TOKEN_SYNC = 0;
    public static final int TOKEN_MERGE = 1;
    public static final int TOKEN_IMPORT = 2;
    public static final int TOKEN_MERGE_ANALYZE = 3;
    public static final int TOKEN_BATCH_DEL = 4;
    public static final int TOKEN_SYNC_PROFILE = 5;
    
    public static void unLockSyncRelated(){
        ContactLock.unLockStatus(ContactLock.TOKEN_SYNC);
        ContactLock.unLockStatus(ContactLock.TOKEN_MERGE);
        ContactLock.unLockStatus(ContactLock.TOKEN_IMPORT);
    }

    private static void setStatus(int status) {
        mStatus = status;
    }

    private static void setAppToken(int token) {
        mAppToken = token;
    }

    public static int getLastAppToken() {
        return mLastAppToken;
    }

    private static void setLastAppToken(int lastToken) {
        ContactLock.mLastAppToken = lastToken;
    }

    private static boolean checkToken(int token) {
        if (token != TOKEN_SYNC && token != TOKEN_SYNC_PROFILE && token != TOKEN_MERGE && token != TOKEN_IMPORT && token != TOKEN_MERGE_ANALYZE && token != TOKEN_BATCH_DEL) {
            BLog.d("ContactLock:invilidate token : "+token);
            return false;
        }
        return token == mAppToken;
    }

    public static boolean isInIdle() {
        return mStatus==STATUS_IDLE;
    }

    public static boolean lockStatus(int token) {
        boolean isIdle = false;
        synchronized (mStatus) {
            switch (mStatus) {
                case STATUS_IDLE:
                    setAppToken(token);
                    setLastAppToken(token);
                    setStatus(STATUS_BUSY);
                    isIdle = true;
                    BLog.d("ContactLock:contact lock . status : " + mStatus
                            + " -- token : " + mAppToken);
                    break;
                case STATUS_BUSY:
                default:
                    break;
            }
        }
        return isIdle;
    }

    public static String getErrorMsg(Context context) {
        String error = "";
        switch (mLastAppToken) {
            case TOKEN_SYNC:
                error = context.getString(R.string.error_sync_is_run);
                break;
            case TOKEN_IMPORT:
                error = context.getString(R.string.error_import_is_run);
                break;
            case TOKEN_MERGE:
                error = context.getString(R.string.error_merge_is_run);
                break;
            case TOKEN_MERGE_ANALYZE:
                error = context.getString(R.string.error_merge_analyze_is_run);
                break;
            case TOKEN_BATCH_DEL:
                error = context.getString(R.string.error_contact_operate_is_run);
                break;
            case TOKEN_SYNC_PROFILE:
                error =  context.getString(R.string.error_sync_profile_is_run);
                break;
            default:
                error = "unknown error!";
                break;
        }
        return error;
    }

    public static boolean unLockStatus(int token) {
        BLog.d("ContactLock: unLock ");
        boolean unRegister = false;
        if (checkToken(token)) {
            synchronized (mStatus) {
                mStatus = STATUS_IDLE;
                mAppToken = -1;
                unRegister = true;
                BLog.d("ContactLock: unLock. status : " + mStatus
                        + " -- token : " + mAppToken);
            }
        }
        return unRegister;
    }

}
