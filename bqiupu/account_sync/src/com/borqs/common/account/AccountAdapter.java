/*
 * Copyright (C) 2007-2012 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.account;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.borqs.account.login.service.AccountService;

/**
 * Date: 8/6/12
 * Time: 4:52 PM
 * Borqs project
 */
public class AccountAdapter {
    // Global configuration, DO NOT MDOIFY
    public static final String BORQS_ACCOUNT_TYPE = "com.borqs";

    public static final String INTENT_ACTION_ACCOUNT_LOGIN = "com.borqs.intent.action.ACCOUNT_LOGIN";
    public static final String INTENT_ACTION_ACCOUNT_LOGOUT = "com.borqs.intent.action.ACCOUNT_LOGOUT";
    static final String FEATURE_ACCOUNT_SERVER_HOST = "account_server_host";
    static final String FEATURE_SYNC_SERVER_HOST = "sync_server_host";

    // key of user data in system after account login, be able to
    // access by API getUserData().
    public static final String BORQS_ACCOUNT_OPTIONS_KEY_SESSION = "borqs_session";
    public static final String BORQS_ACCOUNT_OPTIONS_KEY_NICK_NAME = "borqs_nick_name";
    public static final String BORQS_ACCOUNT_OPTIONS_KEY_UID = "borqs_uid";
    public static final String BORQS_ACCOUNT_OPTIONS_KEY_SCREEN_NAME = "borqs_screen_name";
    public static final String BORQS_ACCOUNT_OPTIONS_KEY_ERROR = "borqs_account_error";

    /*
    * Key for profile's phones value is string as schema
    * <number>:<type>,<number>:<type>, for example, "139109111:2,62000312:4",
    * type refer to ContactsContract.CommonDataKinds.Phone you'd better use
    * Profile.getPhones() instead of the key directly
    */
    public static final String BORQS_ACCOUNT_OPTIONS_KEY_PROFILE_PHONES = "borqs_conact_phones";

    /**
     * Key for profile's EMails value is string as schema
     * <email>:<type>,<email>:<type> for example, "a@gmail.com:2,b@borqs.com:3",
     * type refer to ContactsContract.CommonDataKinds.Email you'd better use
     * Profile.getEMails() instead of the key directly
     */
    public static final String BORQS_ACCOUNT_OPTIONS_KEY_PROFILE_EMAILS = "borqs_conact_emails";

    public static final String DEFAULT_AUTH_TYPE = "com.borqs.service";

    /**
     * Request to add a account into system by the default login ui. Require
     * "android.permission.MANAGE_ACCOUNTS"
     *
     * @param activity
     * @featureId the application id assigned by Borqs server,can be null
     * @options options.putString(AccountAdapter.CONFIG_ACCOUNT_SERVER_HOST, "http://apitest.borqs.com/");
     */
    public static void requestAccountSignin(Activity activity,
                                            String featureId, Bundle options,
                                            AccountManagerCallback<Bundle> callback) {

    }

    /**
     * Retrieve the account Id/Name in the system. Require
     * "android.permission.GET_ACCOUNTS"
     *
     * @param context
     * @return borqs account id, null if no borqs account
     */
    public static String getLoginID(Context context) {
        AccountService as = new AccountService(context);
        return as.getLoginId();
    }
    
    /**
     * Retrieve the account borqsID in the system. Require
     * "android.permission.GET_ACCOUNTS"
     *
     * @param context
     * @return borqs account's borqsID, null if no borqs account
     */
    public static String getUserID(Context context) {
        AccountService as = new AccountService(context);
        return as.getUserId();
    }

    /**
     * return the Borqs account
     *
     * @param context
     * @return null if no Borqs account
     */
    public static Account getBorqsAccount(Context context) {
        AccountService as = new AccountService(context);
        return new Account(as.getLoginId(), as.getAccountType());
    }

    /**
     * Common interface to retrieve the user data by key. Require
     * "android.permission.AUTHENTICATE_ACCOUNTS"
     *
     * @param context
     * @param key
     *            - Key of user data
     * @return
     */
    public static String getUserData(Context context, String key) {
        AccountService as = new AccountService(context);
        return as.getUserData(key);
    }

    public static void setUserData(Context context, String key, String value) {
        AccountService as = new AccountService(context);
        as.setUserData(key, value);
    }

    public static void removeAccount(Context context){
    }

    public static String getSessionId(Context context) {
        return getUserData(context, BORQS_ACCOUNT_OPTIONS_KEY_SESSION);
    }
}
