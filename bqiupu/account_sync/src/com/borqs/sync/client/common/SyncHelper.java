/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */

package com.borqs.sync.client.common;


import java.util.Date;

import org.apache.http.HttpStatus;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.borqs.common.account.AccountAdapter;
import com.borqs.common.account.Configuration;
import com.borqs.common.util.BLog;
import com.borqs.contacts.app.ApplicationGlobals;
import com.borqs.contacts_plus.R;
import com.borqs.json.JSONException;
import com.borqs.json.JSONObject;
import com.borqs.sync.client.transport.BitmapLoader;
import com.borqs.syncml.ds.exception.DsException;
import com.borqs.syncml.ds.imp.common.Constant;
import com.borqs.syncml.ds.imp.tag.StatusValue;
import com.borqs.syncml.ds.xml.SyncmlXml.SyncML;

public class SyncHelper {
    
    public static final int SYNC_FAIL = 1;
    public static final int SYNC_SUCCESS = 2;
    public static final int SYNC_USER_INTERRUPTED = 3;
    

    private static final String TAG = "SyncHelper";

    private static final String NEED_SYNC_KEY = "need_sync";
    
    private static final String SUCCESSED_SYNC_KEY = "sync_success";
    private static final String FIRST_SYNC_END = "first_sync_end";
    
    private static boolean mManual;
    
    public static Account getBorqsAccount(Context context){
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constant.BORQS_ACCOUNT_TYPE);
        Account account = null;
        if (accounts != null && accounts.length > 0) {
            account = accounts[0];
        }
        return account;
    }

    public static boolean isBackgroundEnable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getBackgroundDataSetting();
    }

    public static boolean isContactAutoSync(Account account) {
        int isSyncAble = ContentResolver.getIsSyncable(account, ContactsContract.AUTHORITY);
        boolean autoSync = ContentResolver
                .getSyncAutomatically(account, ContactsContract.AUTHORITY);
        boolean masterAutoSync = ContentResolver.getMasterSyncAutomatically();
        
        return isSyncAble > 0 && autoSync && masterAutoSync;
    }
    
    /**
     * request sync ,ignore any settings,sync directly
     * @param context
     * @param first
     * @return
     */
    public static boolean requestSyncPersonalContacts(Context context,boolean first) {
        Account account = getBorqsAccount(context);

        if (account == null) {
            BLog.d("account is null,do not start sync");
            return false;
        }

        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account, ContactsContract.AUTHORITY, extras);
        return true;
    }
    
    /**
     * check if the sync is success when account login(init sync)
     * @param context
     * @return true if the first sync success
     */
    public static boolean isInitSyncSuccess(Context context) {
        boolean syncSuccess = Boolean.valueOf(AccountAdapter.getUserData(context,
                SUCCESSED_SYNC_KEY));
        BLog.d("is successed sync : " + syncSuccess);
        return syncSuccess;
    }
    
    /**
     * set sync success when init sync
     * @param context
     * @param success
     */
    public static void setInitSyncSuccess(Context context, boolean success) {
        AccountAdapter.setUserData(context, SUCCESSED_SYNC_KEY, String.valueOf(success));
    }
    
    public static boolean isFirstSyncEnd(Context context) {
        boolean syncEnd = Boolean.valueOf(AccountAdapter.getUserData(context,
                FIRST_SYNC_END));
        BLog.d("is first sync end : " + syncEnd);
        return syncEnd;
    }
    
    public static void endFirstSync(Context context) {
        AccountAdapter.setUserData(context, FIRST_SYNC_END,"true");
    }

    public static boolean needSyncPersonalServerContacts(String uid, String deviceId, Context context) {
        String checkNeedSyncUrl = createCheckNeedSyncUrl(uid, deviceId, context);
        if(checkNeedSyncUrl == null){
            return false;
        }
        Logger.logD(TAG, "checkNeedSyncUrl: " + checkNeedSyncUrl);
        String needSync = BitmapLoader.getResponseString(checkNeedSyncUrl);
        Logger.logD(TAG, "needSync json: " + needSync);
        try {
            if (!TextUtils.isEmpty(needSync)) {
                JSONObject syncSourceVersionJson = new JSONObject(needSync);
                return "true".equals(syncSourceVersionJson.getString(NEED_SYNC_KEY));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String createCheckNeedSyncUrl(String uid, String deviceId, Context context) {
        String host = Configuration.getWebAgentServerHost(context);
        String commandNeedSync = Servlet.COMMAND_CHECK_NEED_SYNC;
        String url = host + commandNeedSync + "?uid=" + uid + "&device_id=" + deviceId;
        return url;
    }

    public static boolean needSyncSocialContacts(String uid) {
        // TODO how to judge if the social contacts need to be synced,now we
        return false;
    }
    
    private final static class Servlet{        
        private static final String COMMAND_CHECK_NEED_SYNC = "sync/needsync";
    }
    
    public static void setSyncTimeCausedByNetworkChange(Context context,long time) {
        AccountAdapter.setUserData(context, "sync_time_network_change", String.valueOf(time));
        Logger.logD(TAG, "save the sync time: " + new Date(time).toString());
    }
    
    public static long getSyncTimeCausedByNetworkChange(Context context) {
        String time = AccountAdapter.getUserData(context, "sync_time_network_change");
        if (!TextUtils.isEmpty(time)) {
            Logger.logD(TAG, "get last sync time : " + new Date(Long.parseLong(time)).toString());
            return Long.parseLong(time);
        }
        return 0;
    }
    
    public static void setManualSync(boolean manual){
        mManual = manual;
    }
    
    public static boolean isManualSync(){
        return mManual;
    }
    
    public static class SyncResult{
        private int result = -1;
        private int exceptionCode = -1;
        private int exceptionCategory = -1;
        
        public int getResult(){
            return result;
        }
        
        public int getExceptionCode(){
            return exceptionCode;
        }
        
        public int getExceptionCategory(){
            return exceptionCategory;
        }
        
        public static SyncResult parse(String result) {
            SyncResult sr = new SyncResult();
            if (!TextUtils.isEmpty(result)) {
                try {
                    JSONObject resultObj = new JSONObject(result);
                    if (resultObj.has("result")) {
                        sr.result = resultObj.getInt("result");
                    }
                    if (resultObj.has("exceptionCode")) {
                        sr.exceptionCode = resultObj.getInt("exceptionCode");
                    }
                    if (resultObj.has("exceptionCategory")) {
                        sr.exceptionCategory = resultObj.getInt("exceptionCategory");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return sr;
        }
        
        public static String compose(int result,int exceptionCode,int exceptionCategory){
            try {
                JSONObject resultObj = new JSONObject();
                resultObj.put("result", result);
                resultObj.put("exceptionCode", exceptionCode);
                resultObj.put("exceptionCategory", exceptionCategory);
                Logger.logE(TAG, "save sync result:" + result + " exceptionCode :" + exceptionCode);
                return resultObj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    public static void setLastSyncResult(Context context, int result, int exceptionCode,int exceptionCategory) {
        Logger.logD(TAG, "save sync result:" + result + " exceptionCode :" + exceptionCode);
        String resultStr = SyncResult.compose(result, exceptionCode,exceptionCategory);
        if (!TextUtils.isEmpty(resultStr)) {
            AccountAdapter.setUserData(context, "contact_last_sync_result", resultStr);
        }
    }
    
    public static SyncResult getLastSyncResult(Context context){
        String result = AccountAdapter.getUserData(context, "contact_last_sync_result");
        SyncResult sr = SyncResult.parse(result);
        Logger.logD(TAG, "read last sync result:" + sr.getResult() + ",exceptionCode :" + sr.getExceptionCode());
        return sr;
    }
    
    public static void showManyRestryFailNotifcation(Context context){
        //switch to manually sync
        Account acc = AccountAdapter.getBorqsAccount(context);
        if(acc == null){
            Logger.logE(TAG, "on sync fail many times,we want to notify user ,but the account is null.");
            return;
        }
        ContentResolver.setSyncAutomatically(acc, ContactsContract.AUTHORITY,false);
        
        Logger.logD(TAG, "notify user ,the auto sync failed many times in 12 hours");
        //show the sync fail notification
        
        String s = context.getText(R.string.contact_sync_fail_many_times_content).toString();
        Notification notification = new Notification(R.drawable.account_borqs_icon, s, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent mainActivityIntent = new Intent("com.borqs.account.action.SETTINGS_PLUGIN");
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
        notification.setLatestEventInfo(context, context.getText(R.string.contact_sync_fail_many_times_title), s, contentIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.string.contact_sync_fail_many_times_content, notification);
    }
    
    public static String getDsExceptionString(Context context,DsException e) {
        switch (e.getCategory()) {
        case DsException.CATEGORY_SYNC_STATUS:
            if(e.getCmd() != ""){
                return getSyncStatusExceptionString(context,e.getValue(), e.getCmd());
            }else{
                return getSyncStatusExceptionString(context,e.getValue());
            }           
        case DsException.CATEGORY_HTTP_STATUS:
            return getTransportExceptionString(context,e.getValue());
        case DsException.CATEGORY_CLIENT_SETTING:
            return getSyncExceptionString(context,e.getValue());
        case DsException.CATEGORY_OTHER:
            return getOtherExceptionString(context,e.getValue());
        default:
            return context.getString(R.string.sync_error);
        }
    }
    
    
    private static String getOtherExceptionString(Context context,int code) {
        switch (code) {
        case DsException.VALUE_DATABASE_FULL:
            return context.getString(R.string.sync_database_full);
        case DsException.VALUE_WBXML_ERROR:
            return context.getString(R.string.sync_msg_server_wbxml_error);
        default:
            return context.getString(R.string.sync_item_status_sync_error);
        }
    }

    private static String getSyncStatusExceptionString(Context context,int status, String cmd) {
        switch (status) {
        case StatusValue.NOT_FOUND:
            if(SyncML.Alert.equals(cmd)){
                return context.getString(R.string.sync_status_uri_wrong);
            }else{
                return context.getString(R.string.sync_status_not_found);
            }
        default:
            return context.getString(R.string.sync_error);
        }
    }
    
    private static String getSyncStatusExceptionString(Context context,int status) {
        switch (status) {
        case StatusValue.BAD_REQUEST:
            return context.getString(R.string.sync_status_bad_request);
        case StatusValue.INVALID_CREDENTIALS:
            return context.getString(R.string.sync_status_invalid_credentials);
        case StatusValue.PAYMENT_REQUIRED:
            return context.getString(R.string.sync_status_payment_required);
        case StatusValue.FORBIDDEN:
            BLog.d(context.getString(R.string.sync_status_forbidden));
            return context.getString(R.string.sync_status_forbidden);
        case StatusValue.NOT_FOUND:
            return context.getString(R.string.sync_status_not_found);   
        case StatusValue.COMMAND_NOT_ALLOWED:
            return context.getString(R.string.sync_status_command_not_allowed);
        case StatusValue.OPTIONAL_FEATURE_NOT_SUPPORTED:
            return context.getString(R.string.sync_status_optional_feature_not_supported);
        case StatusValue.MISSING_CREDENTIALS:
            return context.getString(R.string.sync_status_missing_credentials);
        case StatusValue.REQUEST_TIMEOUT:
            return context.getString(R.string.sync_status_request_timeout);
        case StatusValue.CONFLICT:
            return context.getString(R.string.sync_status_conflict);
        case StatusValue.GONE:
            return context.getString(R.string.sync_status_gone);
        case StatusValue.SIZE_REQUIRED:
            return context.getString(R.string.sync_status_size_required);
        case StatusValue.INCOMPLETE_COMMAND:
            return context.getString(R.string.sync_status_incomplete_command);
        case StatusValue.REQUEST_ENTITY_TOO_LARGE:
            return context.getString(R.string.sync_status_request_entity_too_large);
        case StatusValue.URI_TOO_LONG:
            return context.getString(R.string.sync_status_uri_too_long);
        case StatusValue.UNSUPPORTED_MEDIA_TYPE_OR_FORMAT:
            return context.getString(R.string.sync_status_unsupported_media_type_or_format);
        case StatusValue.REQUESTED_SIZE_TOO_BIG:
            return context.getString(R.string.sync_status_requested_size_too_big);
        case StatusValue.RETRY_LATER:
            return context.getString(R.string.sync_status_retry_later);
        case StatusValue.ALREADY_EXISTS:
            return context.getString(R.string.sync_status_already_exists);
        case StatusValue.CONFLICT_RESOLVED_WITH_SERVER_DATA:
            return context.getString(R.string.sync_status_conflict_resolved_with_server_data);
        case StatusValue.DEVICE_FULL:
            return context.getString(R.string.sync_status_device_full);
        case StatusValue.UNKNOWN_SEARCH_GRAMMAR:
            return context.getString(R.string.sync_status_unknown_search_grammar);
        case StatusValue.BAD_CGI_SCRIPT:
            return context.getString(R.string.sync_status_bad_cgi_script);
        case StatusValue.SOFT_DELETE_CONFLICT:
            return context.getString(R.string.sync_status_soft_delete_conflict);
        case StatusValue.SIZE_MISMATCH:
            return context.getString(R.string.sync_status_size_mismatch);
        case StatusValue.COMMAND_FAILED:
            return context.getString(R.string.sync_status_command_failed);
        case StatusValue.COMMAND_NOT_IMPLEMENTED:
            return context.getString(R.string.sync_status_command_not_implemented);
        case StatusValue.BAD_GATEWAY:
            return context.getString(R.string.sync_status_bad_gateway);
        case StatusValue.SERVICE_UNAVAILABLE:
            return context.getString(R.string.sync_status_service_unavailable);
        case StatusValue.GATEWAY_TIMEOUT:
            return context.getString(R.string.sync_status_gateway_timeout);
        case StatusValue.DTD_VERSION_NOT_SUPPORTED:
            return context.getString(R.string.sync_status_dtd_version_not_supported);
        case StatusValue.PROCESSING_ERROR:
            return context.getString(R.string.sync_status_processing_error);
        case StatusValue.ATOMIC_FAILED:
            return context.getString(R.string.sync_status_atomic_failed);
        case StatusValue.REFRESH_REQUIRED:
            return context.getString(R.string.sync_status_refresh_required);
        case StatusValue.RESERVED:
            return context.getString(R.string.sync_status_reserved);
        case StatusValue.DATA_STORE_FAILURE:
            return context.getString(R.string.sync_status_data_store_failure);
        case StatusValue.SERVER_FAILURE:
            return context.getString(R.string.sync_status_server_failure);
        case StatusValue.SYNCHRONIZATION_FAILED:
            return context.getString(R.string.sync_status_synchronization_failed);
        case StatusValue.PROTOCOL_VERSION_NOT_SUPPORTED:
            return context.getString(R.string.sync_status_protocol_version_not_supported);
        case StatusValue.OPERATION_CANCELLED:
            return context.getString(R.string.sync_status_operation_cancelled);
        case StatusValue.ATOMIC_ROLL_BACK_FAILED:
            return context.getString(R.string.sync_status_atomic_roll_back_failed);
        default:
            return context.getString(R.string.sync_error);
        }
    }

    private static String getSyncExceptionString(Context context,int code) {
        switch (code) {
        case DsException.VALUE_MALFORMED_URL:
            return context.getString(R.string.sync_msg_server_url_error);
        case DsException.VALUE_ACCESS_SERVER:
            return context.getString(R.string.access_server_error);
        default:
            return context.getString(R.string.sync_error);
        }
    }

    private static String getTransportExceptionString(Context context,int status) {
        switch (status) {
        case HttpStatus.SC_BAD_REQUEST:
            return context.getString(R.string.sync_msg_unsport_device_mode);
        default:
            return context.getString(R.string.sync_error) + "(HTTP:" + status + ")";
        }
    }
    
}
