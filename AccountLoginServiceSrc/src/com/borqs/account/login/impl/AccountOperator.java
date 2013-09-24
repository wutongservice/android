package com.borqs.account.login.impl;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.borqs.account.login.intf.DeviceFactory;
import com.borqs.account.login.intf.IAccountServiceOp;
import com.borqs.account.login.intf.IDevice;
import com.borqs.account.login.service.AccountService;
import com.borqs.account.login.service.BMSAuthenticatorService;
import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.transport.SMSSender;
import com.borqs.account.login.transport.Servlet;
import com.borqs.account.login.transport.SimpleHttpClient;
import com.borqs.account.login.transport.SyncClient;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.AccountSession;
import com.borqs.account.login.util.BLog;
import com.borqs.account.login.util.MD5;

public class AccountOperator {
    private static final int WAIT_INTERVAL = 2000;
    private static final int WAIT_TIMES = 30;
    private static final String SMS_SERVER_ERROR = "NotWork";
    
    private Context mContext;
    private IAccountServiceOp mAccountServer;
    private IDevice mDevice;
        
    private String mLastError;    
    private volatile boolean mCanceled;
    private boolean mIsNewCreated;
    
    private JSONObject mResultJson;

    public AccountOperator(Context context) {
        this(context, null, null);
        mDevice = DeviceFactory.getDefaultDevice(context);
        mAccountServer = new SyncClient(mContext, SimpleHttpClient.get());
    }
    
    public AccountOperator(Context context, IAccountServiceOp server) {
        this(context, null, server);
        mDevice = DeviceFactory.getDefaultDevice(context);
    }
    
    public AccountOperator(Context context, IDevice device, IAccountServiceOp server) {
        mContext = context;
        mDevice = device;
        mAccountServer = server;
        mCanceled = false;
        mIsNewCreated = false;
    }
    
    /**
     * get the error msg
     * @return
     */
    public String getError() {
        return mLastError;
    }

    public String getResult(String key){
        return (mResultJson == null)?"":mResultJson.optString(key);
    }
    
    public boolean isSmsServerWorking(){
        boolean res = true;
        if (haveError()){
            if (getError().equalsIgnoreCase(SMS_SERVER_ERROR)){
                res = false;
            }
        }
        
        return res;
    }
    
    /**
     * justify the current call whether have failed or not
     * @return false if no error occurred, otherwise true
     */
    public boolean haveError(){
        return (mLastError==null)?false:true;
    }
    
    /**
     * justify whether the account is new register or not
     * @return true-if the account is new register, or else return false
     */
    public boolean isNewCreated() {
        return mIsNewCreated;
    }  
    
    /**
     * cancel the current login process
     */
    public void cancel() {
        mCanceled = true;
    }
    
    /**
     * fast login interface, use the sim ID as a token to login
     * first it will get the phone number which corresponds to the sim id
     * if not have the corresponds relation in server, it will create it
     * @return
     */
    public boolean doFastLogin() {
        BLog.d("doFastLogin");
        String guid = getUserGuid();
        if (TextUtils.isEmpty(guid)) {
            // register & login
            BLog.d("doFastLogin 1");
            guid = getGuidFromServer();
        }
        
        boolean res = false;
        if (!TextUtils.isEmpty(guid)) {
            BLog.d("doFastLogin 2");
            res = loginByGuid(guid);
        }
        
        return res;
    }

    
    /**
     * use name&pass schema to login an account
     * @param name -account user name
     * @param pass -account user password
     * @return
     */
    public boolean doNormalLogin(String name, String pass) {        
        boolean result = false;        
        BLog.d("doNormalLogin");
        try {
            JSONObject data = new JSONObject();
            
            data.put(Servlet.TAG_NAME, name);
            data.put(Servlet.TAG_PASS, MD5.toMd5(pass.getBytes()).toUpperCase());
            String r = mAccountServer.doRequest(data.toString(), 
                    IAccountServiceOp.SERVER_ACTION_NORMAL_LOGIN);
            BLog.d("doNormalLogin res:" + r);
            
            JSONObject res = buildRspResult(r);
            if (!haveError()){
                JSONObject obj = buildRspResult(res.getString(Servlet.TAG_RESULT));
                if (!haveError()){
                    AccountSession userData = AccountSession.from(obj);
                    addAccount(userData);
                    result = true;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        
        return result;
    }

    /**
     * get verify code of a phone
     * @param userName-phone number/mail address
     * @param fmt-the data format that will return to the user sms inbox
     * @return
     */
    public boolean getVerifyCode(String userName, String fmt) {
        boolean result = false;        
        try {
            JSONObject data = new JSONObject();
            data.put(Servlet.TAG_MSGFMT, fmt);
            data.put(Servlet.TAG_NAME, userName);
            data.put(Servlet.TAG_DEVICE_ID, mDevice.getDeviceId());
            String r = mAccountServer.doRequest(data.toString(), 
                    IAccountServiceOp.SERVER_ACTION_VERIFY_NO_SIM);
            BLog.d("getCode rsp:" + r);
            buildRspResult(r);
            if (!haveError()){
                result = true;
            } 
        } catch (Exception e) {
            handleException(e);
        }        
        
        return result;
    }

    /**
     * verify code login
     * @param userName:phone number/mail address
     * @return
     */
    public boolean doVerifyCodeLogin(String userName, String code) {
        BLog.d("doVerifyCodeLogin");
        boolean result = false;
        try {
            JSONObject data = new JSONObject();
            data.put(Servlet.TAG_VERIFYCODE, code);
            data.put(Servlet.TAG_NAME, userName);
            data.put(Servlet.TAG_DEVICE_ID, mDevice.getDeviceId());
            String r = mAccountServer.doRequest(data.toString(), 
                    IAccountServiceOp.SERVER_ACTION_GET_GUID_BY_CODE);
            BLog.d("doVerifyCodeLogin rsp:" + r);
            
            JSONObject res = buildRspResult(r);
            if (!haveError()){
                result = loginByGuid(res.getString(Servlet.TAG_RESULT));
            }
            
//            if (result && !haveError()){
//                AccountBasicProfile profile = new AccountBasicProfile(userName, password);
//                result = changeProfileInfo(profile);
//            }
        } catch (Exception e) {
            handleException(e);
        }
        
        return result;
    }    

    /**
     * get new password for user
     * @param userName-user account name(phone or email)
     * @return true success, otherwise false
     */
    public boolean getNewPassword(final String userName) {
        boolean result = false;          
        try {
            JSONObject data = new JSONObject();
            data.put(Servlet.TAG_NAME, userName);
            String r = mAccountServer.doRequest(data.toString(), 
                    IAccountServiceOp.SERVER_ACTION_GET_NEW_PWD);
            BLog.d("getCode rsp:" + r);
            buildRspResult(r);
            if (!haveError()){
                result = true;
            } 
        } catch (Exception e) {
            handleException(e);
        }
        
        return result;
    }
    
    /**
     * change user profile info,currently support name, pass and gender modify
     * @return true modify success, otherwise false
     */
    public boolean changeProfileInfo(final AccountBasicProfile profile) {
        boolean result = false;
        try {
            JSONObject fv = profile.toJson();
           
           if (fv.length() == 0){
               result = true;
           } else {            
               result = changeFields(fv);
               /*if (result){ // for test only
                   SyncClient client = new SyncClient(mContext, SimpleHttpClient.get());
                   AccountService acn = new AccountService(mContext);
                   client.getProfileDetail(acn.getUserId(), acn.getSessionId());
               }*/     
               /*String photo = profile.getUserPhoto();
               if (!result && !TextUtils.isEmpty(photo)){
                   // update photo
                   result = changePhoto(photo);
               }*/
           }
        } catch (Exception e) {
            handleException(e);
        }
        
        return result;
    }    
    
    /**
     * 
     * @param fieldAndValue
     * @return
     */
    //TODO: this methods have problem, why use guid to change fields??
    private boolean changeFields(JSONObject fieldAndValue){
        BLog.d("changeFields");
        boolean result = false;        
        String guid = getUserGuid();        
        if (!TextUtils.isEmpty(guid)) {
            BLog.d("changeFields 1");
            try {
                JSONObject data = new JSONObject();
                data.put(Servlet.TAG_GUID, guid);
                data.put(Servlet.TAG_FIELD, fieldAndValue.toString());            
                String r = mAccountServer.doRequest(data.toString(), 
                                                    IAccountServiceOp.SERVER_ACTION_CHANGE_FIELDS);
                BLog.d("changeFields rsp:" + r);
                buildRspResult(r);           
                if (!haveError()){
                    //mGuid = res.getString(Servlet.TAG_RESULT);
                    result = true;
                }
            } catch (Exception e) {
                handleException(e);
            }
        }
        
        return result;
    }
    
    private boolean changePhoto(String photoData){
        BLog.d("changePhoto");
        boolean result = false;        
        String ticket = getUserTicket();        
        if (!TextUtils.isEmpty(ticket)) {
            BLog.d("changePhoto 1");
            try {
                JSONObject data = new JSONObject();
                data.put(Servlet.TAG_TICKET, ticket);
                data.put(Servlet.TAG_PHOTO, photoData);            
                String r = mAccountServer.doRequest(data.toString(), 
                                                    IAccountServiceOp.SERVER_ACTION_CHANGE_PHOTO);
                BLog.d("changePhoto rsp:" + r);
                buildRspResult(r);           
                if (!haveError()){
                    //mGuid = res.getString(Servlet.TAG_RESULT);
                    result = true;
                }
            } catch (Exception e) {
                handleException(e);
            }
        }
        
        return result;
    }
    
    private JSONObject buildRspResult(String res) throws JSONException {
        JSONObject obj = null;
        if (TextUtils.isEmpty(res)){
            obj = new JSONObject();
            mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA);
        } else {
            obj = new JSONObject(res);
            handleServerError(obj);
        }
        mResultJson = obj;
        return obj;
    }
    
    private String registerDevice(){
        String guid = null;
        if (reqRegister()){
            guid = getRegisterResult();
        }
        
        return guid;
    }
    
    private String getRegisterResult(){
        String guid = null;
        int wait_times = 0;
        while (!mCanceled) {
            if(wait_times>=WAIT_TIMES){
                mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_REGISTER_TIME_OUT);
                break;
            }
            
            guid = getGuidByDeviceId(mDevice.getDeviceId());
            if (!TextUtils.isEmpty(guid)){
                break;
            }
            
            try {
                Thread.sleep(WAIT_INTERVAL);
                wait_times++;
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                break;
            }
        }
        
        return guid;
    }
    
    private boolean reqRegister() {
        BLog.d("reqRegister");
        boolean result = false;        
        try {
            String sendto = mAccountServer.doRequest(Servlet.SMS_SERVICE_NUMBER, 
                    IAccountServiceOp.SERVER_ACTION_QUERY_CONIFG); 
            if (sendto.equalsIgnoreCase(SMS_SERVER_ERROR)){                
                // here we'll use mLastError later, must reset here
                mLastError = SMS_SERVER_ERROR; 
            } else {            
                JSONObject data = new JSONObject();
                data.put(Servlet.TAG_DEVICE_ID, mDevice.getDeviceId());
                BLog.d("The request message body is :'" + data.toString() + "'");
                final SMSSender sender = new SMSSender(mContext);
                sender.sendMessage(sendto, data.toString(), new Runnable() {
                    @Override
                    public void run() {
                        int code = sender.getResultCode();
                        BLog.d("send message result:" + code);
                        if (Activity.RESULT_OK != code) {
                            mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SEND_REGISTER_SMS) 
                                         + sender.getLastError().toString();
                            cancel();
                        }
                    }
                });
                result = true;
            }
        } catch (Exception e) {
            handleException(e);           
        }

        return result;
    }
    
    private void handleServerError(JSONObject obj)  throws JSONException{
        if (obj.has("error_msg")) { // error corresponds with account login
            if (obj.has("error_code")){
                int errCode = obj.getInt("error_code");
                if (errCode == 106){ // ticket invalid
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_TICKET_INVALID);
                } else if (errCode == 209){ //user name&pwd error
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_USER_PWD_INVALID); 
                } else if (errCode == 211){ //user not exists
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_USER_NOT_EXISTS); 
                }
            }                    
            if (TextUtils.isEmpty(mLastError)){
                mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_ERROR) 
                             + obj.getString("error_msg");
            }
        } else if (obj.has("error")) {
            if (obj.has("error_code")){
                int errCode = Integer.valueOf(obj.getString("error_code"));
                if (errCode == 1021){ // no sim
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_NO_SIM_CARD);
                } else if (errCode == 1022){ // no phone
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_NO_PHONE); 
                } else if (errCode == 1023){ // no user
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_USER_NOT_EXISTS); 
                } else if (errCode == 1024){ // no recode
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_NO_USER_RECORD); 
                } else if (errCode == 1025){ // no verify code
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_NO_INPUT_VERFIY_CODE); 
                } else if (errCode == 1026){ // verify code error
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_VERIFY_CODE); 
                } else if (errCode == 1027){ // verify code out
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_VERFIY_CODE_OUT); 
                } else if (errCode == 1028){ // verify code out
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_INVALID_USER); 
                } else if (errCode == 1030){ // server exception
                    mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_EXCEPTION); 
                } else if (errCode == 1029){ // sms server not work
                    mLastError = SMS_SERVER_ERROR; 
                }                      
            }
            
            if (TextUtils.isEmpty(mLastError)){
                mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_UNKNOWN);
            }
        }
    }
    
    private void handleException(Exception e){
        if (e instanceof JSONException){            
            mLastError = AccountHelper.getErrorDesc(mContext, e,
                                                    ConstData.ERROR_SERVER_RSP_DATA);
        } else if (e instanceof IOException){            
            mLastError = AccountHelper.getErrorDesc(mContext, e,
                                                    ConstData.ERROR_SERVER_CONNECT);
        } else {
            mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_UNKNOWN)+e.toString();
        }
        e.printStackTrace();
    }    
    
    private String getUserGuid(){
        AccountService service = new AccountService(mContext);
        return service.getUserData(ConstData.ACCOUNT_GUID);
    }
    
    private String getUserTicket(){
        AccountService service = new AccountService(mContext);
        return service.getSessionId();
    }
    
    /**
     * 
     * @return
     */
    private String getGuidFromServer() {
        String guid = null;
        if (TextUtils.isEmpty(mDevice.getDeviceId())) {
            mLastError = AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_NO_SIM_CARD);
        } else {
            guid = getGuidByDeviceId(mDevice.getDeviceId());
            if ((!haveError())&&(TextUtils.isEmpty(guid))){
                //can't get from server, new register it
                guid = registerDevice();
            }
        }
        
        return guid;
    }
    
    private String getGuidByDeviceId(String deviceID) {
        String guid = null;
        JSONObject data = new JSONObject();
        try {
            data.put(Servlet.TAG_DEVICE_ID, deviceID);
            String r = mAccountServer.doRequest(data.toString(), 
                    IAccountServiceOp.SERVER_ACTION_GET_GUID_BY_SIM);
            BLog.d("getGuidByDeviceId res :" + r);
            JSONObject res = buildRspResult(r);
            guid = res.getString(Servlet.TAG_RESULT);
        } catch (Exception e) {
            handleException(e);
        }
        
        return guid;
    }

    /**
     * use user guid to login, don't need user name&password
     * @return
     */
    private boolean loginByGuid(String guid) {
        boolean result = false;
        JSONObject data = new JSONObject();
        try {
            data.put(Servlet.TAG_GUID, guid);
            String r = mAccountServer.doRequest(data.toString(), 
                            IAccountServiceOp.SERVER_ACTION_FAST_LOGIN);
            JSONObject res = buildRspResult(r);            
            BLog.d("loginByGuid res :" + r);
            if (res.has(Servlet.TAG_CREATE)) {
                mIsNewCreated = true;
            }
            String accStr = res.getString(Servlet.TAG_RESULT);
            JSONObject obj = buildRspResult(accStr);
            if (!haveError()){
                AccountSession userData = AccountSession.from(obj);
                userData.accountGuid = guid;
                addAccount(userData);
                result = true;
            }
        } catch (Exception e) {
            handleException(e);
        }
        
        return result;
    }
    
    private void addAccount(AccountSession userData) {
        //AccountHelper.removeBorqsAccount(mContext);
        BMSAuthenticatorService.addSystemAccount(mContext, userData);
    }
}
