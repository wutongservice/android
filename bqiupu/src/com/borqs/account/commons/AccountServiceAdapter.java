package com.borqs.account.commons;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.borqs.account.login.service.AccountService;
//import com.borqs.account.login.util.AccountUtils;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-8-9
 * Time: 下午6:48
 * clone from account_sync, AccountAdapter.
 */
/**
 * Date: 8/6/12
 * Time: 4:52 PM
 * Borqs project
 */
public class AccountServiceAdapter {
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
//    public static void requestAccountSignin(Activity activity,
//                                            String featureId, Bundle options,
//                                            AccountManagerCallback<Bundle> callback) {
//        AccountUtils.requestAccountSignin(activity, featureId, options, callback);
//    }

//    public static void requestUpdateCredential(Activity activity,
//                                                Bundle options,
//                                                AccountManagerCallback<Bundle> callback) {
//        AccountUtils.requestUpdateCredential(activity, options, callback);
//    }

    public static void login(Activity activity, String serverHost, AccountService.IOnAccountLogin listener) {
        AccountService as = TextUtils.isEmpty(serverHost) ? new AccountService(activity) :
                new AccountService(activity, serverHost);
//        as.login(listener);
        as.loginByFeatures(listener, AccountService.ACS_FEATURE_REGISTER_BY_EMAIL | AccountService.ACS_FEATURE_REGISTER_BY_PHONE);
    }

    public static void reLogin(Activity activity, String serverHost, AccountService.IOnAccountLogin listener) {
        AccountService as = TextUtils.isEmpty(serverHost) ? new AccountService(activity) :
                        new AccountService(activity, serverHost);
        as.reLogin(listener);
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

//    public static void removeAccount(Context context){
//        AccountUtils.removeAccount(context);
//    }

    public static boolean isAccountPreloaded(Context context) {
        return true;
    }

    public static boolean isAccountLogin(Context context) {
        AccountService as = new AccountService(context);
        return as.isAccountLogin();
    }

    public static String getSessionId(Context context) {
        AccountService as = new AccountService(context);
        return as.getSessionId();
    }

    public static void setExpiredSession(Context context) {
        setUserData(context, BORQS_ACCOUNT_OPTIONS_KEY_SESSION, "");
        setUserData(context, BORQS_ACCOUNT_OPTIONS_KEY_SESSION, null);
    }


    /**
     * Retrieve the account Id/Name in the system. Require
     * "android.permission.GET_ACCOUNTS"
     *
     * @param context
     * @return borqs account id, null if no borqs account
     */
//    public static String getLoginId(Context context) {
//        AccountService as = new AccountService(context);
//        return as.getLoginId();
//    }

    public static String getNickName(Context context) {
        return getUserData(context, BORQS_ACCOUNT_OPTIONS_KEY_NICK_NAME);
    }

    public static String getScreenName(Context context) {
        return getUserData(context, BORQS_ACCOUNT_OPTIONS_KEY_SCREEN_NAME);
    }

    public static boolean performLogin(Activity activity, String serverHost, AccountService.IOnAccountLogin callback) {
        final String sessionId = AccountServiceAdapter.getSessionId(activity.getApplicationContext());
        if (TextUtils.isEmpty(sessionId)) {
            final String userId = getUserID(activity.getApplicationContext());
            if (!TextUtils.isEmpty(userId)) {
                reLogin(activity, serverHost, callback);
            } else {
                login(activity, serverHost, callback);
            }

            return true;
//        } else if (sessionId.equals(getSessionId(activity))) {
//            reLogin(activity, serverHost, callback);
//            return true;
        }

        return false;
    }

//    public static boolean setExpiredSession(Activity activity, String serverHost, AccountService.IOnAccountLogin callback) {
//        final String sessionId = AccountServiceAdapter.getSessionId(activity.getApplicationContext());
//        if (TextUtils.isEmpty(sessionId)) {
//            final String userId = getUserID(activity.getApplicationContext());
//            if (!TextUtils.isEmpty(userId)) {
//                reLogin(activity, serverHost, callback);
//            } else {
//                login(activity, serverHost, callback);
//            }
//
//            return true;
//        } else if (sessionId.equals(getSessionId(activity))) {
//            reLogin(activity, serverHost, callback);
//            return true;
//        }
//
//        return false;
//    }

    public static final String CONFIG_ACCOUNT_SERVER_HOST = FEATURE_ACCOUNT_SERVER_HOST;
}
