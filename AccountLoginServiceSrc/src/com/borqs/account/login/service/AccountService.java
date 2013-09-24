package com.borqs.account.login.service;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.borqs.account.login.impl.AccountBasicProfile;
import com.borqs.account.login.provider.AccountProvider;
import com.borqs.account.login.ui.AccountDetailActivity;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.BLog;
import com.borqs.app.Env;

public class AccountService { 
    public final static int ACS_LOGIN_OK = 0x00; 
    public final static int ACS_LOGIN_CANCEL = 0x01; // user cancel
    public final static int ACS_LOGIN_ERROR_UNKNOWN = 0x10; // user cancel
    
    @Deprecated
    public final static int ACS_FEATURE_REGISTER_BY_PHONE = ConstData.FEATURE_SUPPORT_PHONE;
    @Deprecated
    public final static int ACS_FEATURE_REGISTER_BY_EMAIL = ConstData.FEATURE_SUPPORT_EMAIL;
    
    public final static int ACS_FEATURE_SUPPORT_PHONE = ConstData.FEATURE_SUPPORT_PHONE;
    public final static int ACS_FEATURE_SUPPORT_EMAIL = ConstData.FEATURE_SUPPORT_EMAIL;
    public final static int ACS_FEATURE_SUPPORT_ONEKEY_REGISTER = ConstData.FEATURE_SUPPORT_ONEKEY_REGISTER;
    
    //private static final String SERVICE_INTENT = "com.borqs.account.login.service.accountdataservice";
    private static final int MSG_ACCOUNT_SERVICE_ERROR = 0x21;
    private static final int MSG_ACCOUNT_SERVICE_CONNECTED = 0x20;
    //private static final int MSG_DATA_SERVICE_DISCONNECTED = 0x22;
    //private static final int MSG_ACCOUNT_GET_SESSION_DATA = 0x23;
    
    private static final int LOGIN_DELAY_INTERVAL = 2000;
    private static final int LOGIN_RETRY_TIMES = 6;
        
    private Context mContext;    
    private IOnAccountLogin mLoginListener;
    private IOnAccountLogin mReLoginListener;
    private IOnAccountDataLoad mLoadListener;
    private Activity mLoginActivity;    
    private AsyncHandler mHandler;
    
    private int mFeatures;
    private int mTryLoginTimes = 0;    
    private static final AccountSessionData mSessionData = new AccountSessionData();
    
    
    /**
     * default constructor
     * @param ctx mustn't be null
     *  when need show login UI, the ctx must be instance of Activity     
     */
    public AccountService(Context ctx){
        if (ctx != null){
            if (ctx instanceof Activity){
                mLoginActivity = (Activity)ctx;
            }
            
            mContext = ctx.getApplicationContext();     
            mHandler = new AsyncHandler();
            
            Env.init(ctx);
        }
    }
    
    /**
     * when you want to change the server, use this contstructor and pass server ip in
     * @param ctx
     * @param serverHost
     */
    public AccountService(Context ctx, String serverHost){
        this(ctx);
        //use Env to get the host
    }
        
    /**
     * @return true if have logged in, otherwise false
     */
    public boolean isAccountLogin(){
        boolean isLogin = false;        
        if (AccountHelper.isBorqsAccountLogin(mContext)){
            if (hasUserInfo()){
                //avoid user clean app data, if this happend system
                //have account, but user can't get data, must relogin
                isLogin = true;
            }
        }
        BLog.d("is account login:" + isLogin);
        return isLogin;
    }
    
    /**
     * @return true if current account is new register, otherwise false
     */
    public boolean isNewRegister(){
        return mSessionData.isNewRegister();
    }

    @Deprecated
    /**
     * Now all the getXXX call is synchronized, the isReady not necessary
     * before you call getXXX, only you do is assure use login (isAccountLogin) 
     * if data is ready you can safely call getXXX interface
     * @return true if account data is available, otherwise false
     */
    public boolean isReady(){
        return mSessionData.hasData();
    }
    
    /**
     * logout from the current account session, not used at present
     */
    private void logout(AccountSessionData session){
        mSessionData.clean();
    }
    
    /**
     * login interface, should call it in main UI thread
     * @param loginListener when login finished, will call it to pass the result
     */
    public void login(IOnAccountLogin loginListener){
        loginByFeatures(loginListener, 
                    ACS_FEATURE_SUPPORT_PHONE|ACS_FEATURE_SUPPORT_EMAIL
                    |ACS_FEATURE_SUPPORT_ONEKEY_REGISTER 
                    );        
    }
    
    /**
     * login interface, should call it in main UI thread
     * you can define which register type you want: phone or email, or both
     * @param loginListener - when login finished, will call it to pass the result
     * @param features - a combination value of ACS_FEATURES_XXX
     */
    public void loginByFeatures(IOnAccountLogin loginListener, int features){
        // TODO: must avoid reentry
        BLog.d("login:" + mContext.getApplicationInfo().uid);
        mTryLoginTimes = 0;
        mLoginListener = loginListener;
        mFeatures = features;            
        //login
        doLogin();
    }
    
    public void reLogin(IOnAccountLogin loginListener){
       mTryLoginTimes = 0;
    	mReLoginListener = loginListener;    	
    	updateAccountSession();
    }
    
    /**
     * load account session data,
     * @param loadListener when load finished will call it to pass the result
     */
    public void loadData(IOnAccountDataLoad loadListener){
        mLoadListener = loadListener;        
        getExistSessionData();
    }

    public String getLoginId(){  
        String id = null;
        if (hasUserInfo()){
            // in case for account existis but user data was cleaned
            // case 1: user clean app data
            // case 2: apk A login, install apk B but not start it, uninstall A
            id = mSessionData.getLoginId(mContext); 
        } 
        return id;
    }

    
    /**
     * 
     * @return account session id
     */
    public String getSessionId(){
        initSessionData();
        return mSessionData.getSessionId();
    }
    
    /**
     * 
     * @return account type
     */
    public String getAccountType(){        
        return ConstData.BORQS_ACCOUNT_TYPE;
    }
            
    /**
     * 
     * @return account user id
     */
    public String getUserId() {
        initSessionData();
        return mSessionData.getUserId();
    }
    
    
    /**
     * get user self defined data(key-value), must sure isReady call return true
     * @param key
     * @return the corresponds key-value data
     */
    public String getUserData(String key){
        initSessionData();
    	return mSessionData.getUserData(key);
    }
    
    /**
     * set user self defined data (key-value pair)
     * @param key
     * @param value
     */
    public void setUserData(String key, String value){        
    	if (isValidKey(key)){
    	    initSessionData();
    	    BLog.d("as set user data:" + key);
	        String data = (value==null)?"":value;
	        mSessionData.setUserData(key, data);
    	}
    }
    
    private boolean isValidKey(String key){
        boolean res = false;
        if (!TextUtils.isEmpty(key)) {
            // the following key data, not enable user to use
            if (!key.equals(ConstData.ACCOUNT_ERROR) 
                && !key.equals(ConstData.ACCOUNT_USER_ID)
                && !key.equals(ConstData.ACCOUNT_GUID)
                && !key.equals(ConstData.ACCOUNT_SESSION)) {
                res = true;
            }
                
        }
        BLog.d("as key " + key + " is valid" + res);
        return res;    
    }
    
    private boolean hasUserInfo(){
        return !TextUtils.isEmpty(getUserId());
    }
    
    private void tryLoginLater(){
        BLog.d("try login later");
        mHandler.postDelayed(new LoginService(), LOGIN_DELAY_INTERVAL);
    }
    
    private void doLogin(){
        BLog.d("do login");
        mTryLoginTimes++;
        if (mTryLoginTimes > LOGIN_RETRY_TIMES){
            mHandler.sendEmptyMessage(MSG_ACCOUNT_SERVICE_ERROR);
        } else if (isAccountLogin()){
            BLog.d("login already have account");
            // already have account log in, get the data
            getExistSessionData();            
        } else if (AccountHelper.isBorqsAccountLogin(mContext)){
            BLog.d("login update account");
            // no user data, but hava account, update session only
            updateAccountSession();
        } else {
            BLog.d("login new login");
            // login    
            addNewAccount();
       }
    }
    
    private void addNewAccount() {        
        // android.permission.MANAGE_ACCOUNTS
        Bundle data = new Bundle();
        data.putInt(ConstData.LOGIN_REGISTER_FEATURE, mFeatures);
        AccountManager.get(mContext)
                      .addAccount(ConstData.BORQS_ACCOUNT_TYPE, null, /* authTokenType */
                                  new String[] { "1" }, /* requiredFeatures */
                                  data, /* addAccountOptions */
                                  mLoginActivity, 
                                  new LoginCallback(), 
                                  null /* handler */);
    }
    
    private void updateAccountSession(){
        if (TextUtils.isEmpty(mSessionData.getLoginId(mContext))){
            doLogin(); // no account
        } else {
        	Account account = new Account(mSessionData.getLoginId(mContext), 
        	                              ConstData.BORQS_ACCOUNT_TYPE);
        	
            AccountManager.get(mContext)
            			  .updateCredentials(account, 
            					            ConstData.DEFAULT_AUTH_TYPE, /* authTokenType */
            					            null, /* addAccountOptions */
            					            mLoginActivity, 
            					            new LoginCallback(), 
                                  			null /* handler */);
        }
    }
    
    private void initSessionData(){
        if (!isReady()){
            mSessionData.initData(mContext);
        }
    }
    
    private void getExistSessionData(){
        initSessionData();
        mHandler.sendEmptyMessageDelayed(MSG_ACCOUNT_SERVICE_CONNECTED,1000);
    }

    private void onLoginResult(int result){
        boolean isSuccess = false;
        if (result == ACS_LOGIN_OK){
            isSuccess = true;
        }
        synchronized(mSessionData){
            mSessionData.initData(mContext, result);
        }
        
        if (mLoginListener != null){
            mLoginListener.onAccountLogin(isSuccess, mSessionData);            
        }
        
        if ((mReLoginListener != mLoginListener) &&(mReLoginListener != null)){
        	mReLoginListener.onAccountLogin(isSuccess, mSessionData);  
        }
        
        if (mLoadListener != null){
            mLoadListener.onAccountDataLoad(isSuccess, mSessionData);            
        }
    }
        
    private class LoginService implements Runnable{        
        public void run(){            
            doLogin();
        } 
    }
    
    private class LoginCallback implements AccountManagerCallback<Bundle>{
        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            if(future != null){
                if(future.isCancelled()){
                    BLog.d("login callback cancel");
                    onLoginResult(ACS_LOGIN_CANCEL);
                    //Toast.makeText(mContext, "Canceled of account create", Toast.LENGTH_SHORT)
                    //               .show();
                } else if(future.isDone()){
                    Bundle result;
                    try {
                        result = future.getResult();
                        mSessionData.mIsNewRegister = result.getBoolean(ConstData.REGISTER_RESPONSE_RESULT);
                        if(isNewRegister() && (mLoginActivity != null)){
                            BLog.d("login callback register, edit profile");
                            AccountDetailActivity.actionShow(mLoginActivity,
                                            new AccountDetailActivity.IOnProfileChanged() {
                                                public void onProfileChanged(AccountBasicProfile profile) {                                                    
                                                    getExistSessionData();
                                                }
                                            });
                        }else{
                            BLog.d("login callback login");
                            getExistSessionData();
                        }                        
                    } catch (OperationCanceledException e) {
                        BLog.d("OperationCanceledException:" + e.getMessage());
                        e.printStackTrace();
                    } catch (AuthenticatorException e) {                        
                        BLog.d("AuthenticatorException:" + e.getMessage());
                        e.printStackTrace();
                        
                        // here maybe service not availabe
                        tryLoginLater();                        
                    } catch (IOException e) {
                        BLog.d("IOException:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    };    
    
    public interface IOnAccountLogin{
        /**
         * 
         * @param isSuccess true if login success, otherwise false(call 
         * data.getLoginError to get the explicit error)
         * @param data include login session data which maybe used in your app
         */
        public void onAccountLogin(boolean isSuccess, AccountSessionData data);
    }
    
    public interface IOnAccountDataLoad{
        /**
         * 
         * @param isSuccess true if login success, otherwise false(call 
         * data.getLoginError to get the explicit error)
         * @param data include login session data which maybe used in your app
         */
        public void onAccountDataLoad(boolean isSuccess, AccountSessionData data);
    }
    
    public static final class AccountSessionData{
        private String mLoginId; // user login name
        private String mSessionId; // session ticket
        private String mUserId;  //user account id
        private String mNickName; // display name
        private String mScreenName; // 
        private String mSessionError;
        private int mLoginError;
        private boolean mIsNewRegister;
        private AccountProvider mDataProvider;
                
        private void clean(){
            //mSessionId = null;            
        }
        
        public void initData(Context ctx, int error){
            mDataProvider = new AccountProvider(ctx);
            mLoginError = error;
            mLoginId = getAccountId(ctx);
            //mNickName = mDataProvider.getAccountData(ConstData.ACCOUNT_NICK_NAME);
            //mScreenName = mDataProvider.getAccountData(ConstData.ACCOUNT_SCREEN_NAME);
            //mSessionError = mDataProvider.getAccountData(ConstData.ACCOUNT_ERROR);
            //mSessionId = mDataProvider.getAccountData(ConstData.ACCOUNT_SESSION);
            //mUserId = mDataProvider.getAccountData(ConstData.ACCOUNT_USER_ID);
        }
        
        public void initData(Context ctx){
            initData(ctx, ACS_LOGIN_OK);
        }

        private String getUserData(String key){
            String res = null;
            if (mDataProvider != null){
                res = mDataProvider.getAccountData(key);
            }
            
            return res;
        }
        
        private void setUserData(String key, String value){
            if (mDataProvider != null){
                mDataProvider.setAccountData(key, value);
            }
        }
                
        private boolean hasData(){
            boolean res = false;
            if ((mLoginError == ACS_LOGIN_OK) && (mDataProvider != null)){
                res = true;
            }
            return res;
        }
        
        public boolean isNewRegister(){
            return mIsNewRegister;
        }
        
        public int getLoginError(){
            return mLoginError;
        }

        public String getLoginId(){
            return mLoginId;
        }
        
        public String getLoginId(Context ctx){
            return getAccountId(ctx);
        }
        
        public String getSessionId(){
            return mDataProvider.getAccountData(ConstData.ACCOUNT_SESSION);
        }
                
        public String getUserId() {
            return mDataProvider.getAccountData(ConstData.ACCOUNT_USER_ID);
        }

        public String getNickName() {
            return mDataProvider.getAccountData(ConstData.ACCOUNT_NICK_NAME);
        }

        public String getScreenName() {
            return mDataProvider.getAccountData(ConstData.ACCOUNT_SCREEN_NAME);
        }

        public String getSessionError() {
            return mDataProvider.getAccountData(ConstData.ACCOUNT_ERROR);
        }
        
        public String getAccountId(Context ctx) {
            Account[] accounts = AccountManager.get(ctx).getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
            if (accounts.length == 0) {
                BLog.d("get account id null");
                return null;
            }
            BLog.d("get account id:" + accounts[0].name);
            return accounts[0].name;
        }
    }
        
    private class AsyncHandler extends Handler{
        public AsyncHandler(){
            super(mContext.getMainLooper());
        }
        
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ACCOUNT_SERVICE_CONNECTED){
                onLoginResult(ACS_LOGIN_OK);
            } else if (msg.what == MSG_ACCOUNT_SERVICE_ERROR){
                onLoginResult(ACS_LOGIN_ERROR_UNKNOWN);
            } /*else if (msg.what == MSG_DATA_SERVICE_DISCONNECTED){
                connectExistService();
            } else if (msg.what == MSG_ACCOUNT_GET_SESSION_DATA){
                
            }*/
            super.handleMessage(msg);
        }
    };    
}
