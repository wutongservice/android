/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.borqs.account.login.provider.AccountProvider;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.AccountSession;
import com.borqs.account.login.util.BLog;

public class BMSAuthenticatorService extends Service {
    public static final String OPTIONS_USERNAME = "username";
    public static final String OPTIONS_USER_DATA = "user_data";
    public static final String OPTIONS_CONTACTS_SYNC_ENABLED = "contacts";

    /**
     * A very basic authenticator service for Borqs BMS. At the moment, it has
     * no UI hooks. When called with addAccount, it simply adds the account to
     * AccountManager directly with a username and password. We will need to
     * implement confirmPassword, confirmCredentials, and updateCredentials.
     */
    class BMSAuthenticator extends AbstractAccountAuthenticator {

        private Context mContext;

        public BMSAuthenticator(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response,
                String accountType, String authTokenType,
                String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            BLog.d("BMSAuthenticator.addAccount():" + mContext.getApplicationInfo().uid);
            Bundle b = new Bundle();
            // There are two cases here:
            // 1) We are called with a username/Session; this comes from the
            // traditional
            // app UI; we simply create the account and return the proper bundle
            if (options != null && options.containsKey(OPTIONS_USERNAME)) {
                BLog.d("BMS 1");
                
                String id = options.getString(OPTIONS_USERNAME);
                final Account account = new Account(id, ConstData.BORQS_ACCOUNT_TYPE);
                Bundle userData = options.getBundle(OPTIONS_USER_DATA);
                          
                //Must call the updateAccountUserData() before
                //AccountManager.addAccountExplicitly();
                if (userData != null) {
                    BLog.d("BMS save account data");
                    updateAccountUserData(mContext, account, options.getBundle(OPTIONS_USER_DATA));
                }
                
                // relogin go here
                if (!isAccountExists()){
                    BLog.d("BMS relogin add account");
                    // in new provider share data version, don't add data to account manager
                    AccountManager.get(BMSAuthenticatorService.this)
                                  .addAccountExplicitly(account, "", null); 
                }
                
                b.putString(AccountManager.KEY_ACCOUNT_NAME, options.getString(OPTIONS_USERNAME));
                b.putString(AccountManager.KEY_ACCOUNT_TYPE, ConstData.BORQS_ACCOUNT_TYPE);

                // handle login
                onAccountLogin(mContext,account);                
            } else {                
                BLog.d("BMS 2");
                String featureId = null;
                if (requiredFeatures != null && requiredFeatures.length > 0) {
                    featureId = requiredFeatures[0];
                }
                Intent pendingIntent = AccountHelper.actionLoginBorqsAccountIntent(
                                                        BMSAuthenticatorService.this, featureId);
                boolean reLogin = false;
                if ((options != null) && (options.containsKey(ConstData.OPTIONS_RELOGIN))) {
                  BLog.d("BMS user call relogin");
                  reLogin = true;
                }
                
                String sessionID = getAccountUserData(mContext, ConstData.ACCOUNT_LOGIN_ID);
                String accountID = AccountHelper.getBorqsAccountId(mContext);
                String loginId = sessionID;
                if (TextUtils.isEmpty(loginId)){
                    loginId = accountID;
                }
                
                // account data existis && system account exists && !relogin
                // show already have account dialog
                if ((!TextUtils.isEmpty(sessionID)) && (!TextUtils.isEmpty(accountID)) && !reLogin){
                    BLog.d("BMS already have account");
                    pendingIntent = AccountHelper.actionInfoDialogIntent(mContext);
                } else {  
                    if (TextUtils.isEmpty(loginId)){
                        // neither session data nor login id, first login
                        BLog.d("BMS system new login");
                        reLogin = false;
                    } else if (TextUtils.isEmpty(accountID)){
                        // system account was removed, can login use other uid login
                        BLog.d("BMS system account removed, login again");                        
                    } else if (TextUtils.isEmpty(sessionID)){
                        // user data lost, relogin update data
                        BLog.d("BMS user data lost, relogin");
                        reLogin = true;
                    } else {
                        BLog.d("both have account & userdata, must is user call relogin:" + reLogin);
                    }
                    
                    if (reLogin){
                        pendingIntent.putExtra(ConstData.OPTIONS_RELOGIN, true);
                        pendingIntent.putExtra(ConstData.ACCOUNT_USER_ID, loginId);
                    }
                    // Add extras that indicate this is an Borqs account creation
                    // So we'll skip the "account type" activity, and we'll use the
                    // response when we're done
                    
                    if ((options != null) && (options.containsKey(ConstData.LOGIN_REGISTER_FEATURE))) {
                        BLog.d("BMS feature:" + options.getInt(ConstData.LOGIN_REGISTER_FEATURE));
                        pendingIntent.putExtra(ConstData.LOGIN_REGISTER_FEATURE, 
                                               options.getInt(ConstData.LOGIN_REGISTER_FEATURE));
                    }
                }
                
                pendingIntent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                                        response);
                b.putParcelable(AccountManager.KEY_INTENT, pendingIntent);
            }
            
            return b;
        }        

        public boolean isAccountExists() {
            Account[] accounts = AccountManager.get(mContext)
                                            .getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
            if (accounts.length == 0) {
                return false;
            }
            return true;
        }

        @Override
        public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
            BLog.d("BMSAuthenticator.getAccountRemovalAllowed()");
            if(ContentResolver.getIsSyncable(account, ContactsContract.AUTHORITY)>0){
                // when have account sync component, go here
                BLog.d("BMSAuthenticator.getAccountRemovalAllowed() 1");  
                //TODO: this has a problem, delete task is async, after setting account data
                // when del task done will call onAccountLogout, this will clean account data
                Bundle result = new Bundle();
                result.putParcelable(AccountManager.KEY_INTENT, AccountHelper.actionRemoveAccountIntent(BMSAuthenticatorService.this));
                result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
                startActivity(AccountHelper.actionRemoveAccountIntent(BMSAuthenticatorService.this));
                return result;
            }
            BLog.d("BMSAuthenticator.getAccountRemovalAllowed() 2");
            onAccountLogout(mContext);
            return super.getAccountRemovalAllowed(response, account);
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response,
                String accountType) {
            BLog.d("BMSAuthenticator.editProperties()");
            return null;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                Account account, Bundle options) throws NetworkErrorException {
            BLog.d("BMSAuthenticator.confirmCredentials()");
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response,
                Account account, String authTokenType, Bundle options)
                throws NetworkErrorException {
            BLog.d("BMSAuthenticator.getAuthToken()");
            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            BLog.d("BMSAuthenticator.getAuthTokenLabel()");
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response,
                Account account, String authTokenType, Bundle options)
                throws NetworkErrorException {
            BLog.d("BMSAuthenticator.updateCredentials()");
            Bundle b = new Bundle();
            // There are two cases here:
            // 1) We are called with a username/Session; this comes from the
            // traditional
            // app UI; we simply create the account and return the proper bundle
            if (options != null && options.containsKey(OPTIONS_USER_DATA)) {
                BLog.d("BMSAuthenticator.updateCredentials() 1");
                updateAccountUserData(mContext, account, options.getBundle(OPTIONS_USER_DATA));
                
                b.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                b.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);                
            } else {
                BLog.d("BMSAuthenticator.updateCredentials() 2");                
                Intent pendingIntent = AccountHelper
                        .actionUpdateCredentialsIntent(
                                BMSAuthenticatorService.this, account);
                
                //Configuration.initOptions(mContext, options);
                // Add extras that indicate this is an Borqs account creation
                // So we'll skip the "account type" activity, and we'll use the
                // response when
                // we're done
                pendingIntent.putExtra(
                        AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                        response);
                b.putParcelable(AccountManager.KEY_INTENT, pendingIntent);                
            }
            
            return b;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response,
                Account account, String[] features)
                throws NetworkErrorException {
            BLog.d("BMSAuthenticator.hasFeatures()");
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        String authenticatorIntent = "android.accounts.AccountAuthenticator";

        if (authenticatorIntent.equals(intent.getAction())) {
            return new BMSAuthenticator(this).getIBinder();
        } else {
            return null;
        }
    }

    // Exported API to add a Borqs account into system via the
    // BMSAuthenticator.addAccount().
    public static AccountManagerFuture<Bundle> addSystemAccount(Context context, AccountSession user) {
        BLog.d("addSystemAccount:" + user.username);
        // Create a description of the new account
        Bundle options = new Bundle();
        options.putString(OPTIONS_USERNAME, user.username);

        Bundle userData = new Bundle();
        userData.putString(ConstData.ACCOUNT_SESSION, user.session_id);
        userData.putString(ConstData.ACCOUNT_NICK_NAME, user.display_name);
        userData.putString(ConstData.ACCOUNT_SCREEN_NAME, user.urlname);
        userData.putString(ConstData.ACCOUNT_USER_ID, user.uid);
        userData.putString(ConstData.ACCOUNT_GUID, user.accountGuid);
        options.putBundle(OPTIONS_USER_DATA, userData);
        Account account = new Account(user.username, ConstData.BORQS_ACCOUNT_TYPE);
        //updateAccountUserData(context, account, userData);
        if (isAccountExists(context, account)){
            return AccountManager.get(context).updateCredentials(account, ConstData.DEFAULT_AUTH_TYPE, 
                                                          options, null, null, null);
        } else {
            AccountHelper.removeBorqsAccount(context);
            return  AccountManager.get(context).addAccount(ConstData.BORQS_ACCOUNT_TYPE, null, null, 
                                                   options, null, null, null);
        }

        // Here's where we tell AccountManager about the new account. The
        // addAccount
        // method in AccountManager calls the addAccount method in our
        // authenticator
        // service (BMSAuthenticatorService)
        //return AccountManager.get(context).addAccount(
        //        ConstData.BORQS_ACCOUNT_TYPE, null, null, options, null,
        //        null, null);
    }
    
    private static boolean isAccountExists(Context ctx, Account account){
        boolean res = false;
        String acnId = AccountHelper.getBorqsAccountId(ctx); 
        if ((acnId != null) &&(acnId.equalsIgnoreCase(account.name))){
            res = true;            
        }
        return res;
    }

    /*public static AccountManagerFuture<Bundle> updateAccount(Context context,
            Account account, AccountSession user) {
        BLog.d("updateAccount");
        // Create a description of the new account
        Bundle options = new Bundle();
        options.putString(OPTIONS_USERNAME, user.username);

        Bundle userData = new Bundle();
        userData.putString(ConstData.ACCOUNT_SESSION, user.session_id);
        userData.putString(ConstData.ACCOUNT_NICK_NAME, user.display_name);
        userData.putString(ConstData.ACCOUNT_SCREEN_NAME, user.urlname);
        userData.putString(ConstData.ACCOUNT_USER_ID, user.uid);
        userData.putString(ConstData.ACCOUNT_GUID, user.accountGuid);
        //options.putBundle(OPTIONS_USER_DATA, userData);
        updateAccountUserData(context, account, userData);
        return AccountManager.get(context).updateCredentials(account,
                ConstData.DEFAULT_AUTH_TYPE, options, null, null, null);
    }*/

    private static void updateAccountUserData(Context ctx, Account account, Bundle userData){
        BLog.d("updateAccountUserData");
        /*AccountManager am = AccountManager.get(ctx);
        am.setUserData(
                account,
                ConstData.ACCOUNT_NICK_NAME,
                userData.getString(ConstData.ACCOUNT_NICK_NAME));
        am.setUserData(
                account,
                ConstData.ACCOUNT_SESSION,
                userData.getString(ConstData.ACCOUNT_SESSION));
        am.setUserData(
                account,
                ConstData.ACCOUNT_USER_ID,
                userData.getString(ConstData.ACCOUNT_USER_ID));
        am.setUserData(
                account,
                ConstData.ACCOUNT_SCREEN_NAME,
                userData.getString(ConstData.ACCOUNT_SCREEN_NAME));
        am.setUserData(
                account,
                ConstData.ACCOUNT_GUID,
                userData.getString(ConstData.ACCOUNT_GUID));*/
        AccountProvider provider = new AccountProvider(ctx);
        
        String value = userData.getString(ConstData.ACCOUNT_NICK_NAME);
        value = (value==null)?"":value;
        provider.setAccountData(ConstData.ACCOUNT_NICK_NAME,value);
        
        value = userData.getString(ConstData.ACCOUNT_SESSION);
        value = (value==null)?"":value;
        provider.setAccountData(ConstData.ACCOUNT_SESSION,value);
        
        value = userData.getString(ConstData.ACCOUNT_USER_ID);
        value = (value==null)?"":value;
        provider.setAccountData(ConstData.ACCOUNT_USER_ID,value);
        
        value = userData.getString(ConstData.ACCOUNT_SCREEN_NAME);
        value = (value==null)?"":value;
        provider.setAccountData(ConstData.ACCOUNT_SCREEN_NAME,value);
        
        value = userData.getString(ConstData.ACCOUNT_GUID);
        value = (value==null)?"":value;
        provider.setAccountData(ConstData.ACCOUNT_GUID,value);
        
        value = (account==null)?"":account.name;
        provider.setAccountData(ConstData.ACCOUNT_LOGIN_ID,value);
    }
    
    private static String getAccountUserData(Context ctx, String key){
        AccountProvider provider = new AccountProvider(ctx);
        return provider.getAccountData(key);
    }

    private static void onAccountLogin(Context context,Account account) {
        // 1.send the broadcast for account login.
        BLog.d("account login,send the login broadcast");
        context.sendBroadcast(new Intent(ConstData.INTENT_ACTION_ACCOUNT_LOGIN));
        
//        only for fixed bug :can not edit/new borqs contacts when the account and 
//        syncadapter is installed separately in system
        enableSyncAdapter(context,account);

        // 2.reset the status,and then clear the syncinfo
//        context.getContentResolver().delete(Uri.parse("content://"
//                + "com.borqs.sync.syncML" + "/" + "function/clear_sync_info"), null, null);
    }    
    
    public static void onAccountLogout(Context context) {
        BLog.d("account logout,send the logout broadcast");
        Context ctx = context.getApplicationContext();
        //1:clean user data
        AccountProvider provider = new AccountProvider(ctx);
        provider.cleanAccountData();
        // 2.send the broadcast for account login.        
        ctx.sendBroadcast(new Intent(ConstData.INTENT_ACTION_ACCOUNT_LOGOUT));

        // 2.reset the status,and then clear the syncinfo
//        context.getContentResolver().delete(Uri.parse("content://"
//                + "com.borqs.sync.syncML" + "/" + "function/clear_sync_info"), null, null);
    }
    
    private static void enableSyncAdapter(Context context,Account bAccount){
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
