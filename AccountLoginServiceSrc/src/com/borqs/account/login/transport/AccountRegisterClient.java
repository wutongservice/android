/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.transport;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

import android.content.Context;

import com.borqs.account.login.intf.DeviceFactory;
import com.borqs.account.login.intf.IDevice;
import com.borqs.account.login.util.Configuration;
import com.borqs.account.login.util.Utility;

public final class AccountRegisterClient extends HttpRequestExecutor{
    public AccountRegisterClient(Context context, HttpClient httpClient) {
        super(context, httpClient);
    }
    
    protected String getHostServer(){
        return Configuration.getAccountServerHost(mContext);
    }
    
    public void registerAccountByMobile(String appId, String requestGUID, String password, String displayName,
            final OnResultCallback<Boolean> callback) {
        IDevice device = DeviceFactory.getDefaultDevice(mContext);
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET, Servlet.COMMAND_REGISTER_MOBILE)
            .parameter("appid", appId)
            .parameter("guid", requestGUID)
            .parameter("password", Utility.MD5Encode(password))
            .parameter("displayname", displayName)
            .parameter("imei", device.getImei())
            .parameter("imsi", device.getImsi())
            .create();
        doRequestAsync(request, new OnResultCallback<HttpResponse>(){
            @Override
            public void onResult(HttpResponse response, Exception e) {
                if(callback == null){
                    return;
                }
                if(e != null){
                    callback.onResult(false, e);
                }else{
                    try {
                        callback.onResult(parseBooleanResult(response), null);
                    } catch (IOException e1) {
                        callback.onResult(false, e1);
                        e1.printStackTrace();
                    } catch (AccountException e1) {
                        callback.onResult(false, e1);
                        e1.printStackTrace();
                    }
                }
            }            
        });
    }    
    
    public void resetPwdByMobile(String requestGUID, String password,
            final OnResultCallback<Boolean> callback) {
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET, Servlet.COMMAND_RESET_PASSWORD_BY_MOBILE)
            .parameter("guid", requestGUID)
            .parameter("password", Utility.MD5Encode(password))
            .create();
        doRequestAsync(request, new OnResultCallback<HttpResponse>(){
            @Override
            public void onResult(HttpResponse response, Exception e) {
                if(callback == null){
                    return;
                }
                if(e != null){
                    callback.onResult(false, e);
                }else{
                    try {
                        callback.onResult(parseBooleanResult(response), null);
                    } catch (IOException e1) {
                        callback.onResult(false, e1);
                        e1.printStackTrace();
                    } catch (AccountException e1) {
                        callback.onResult(false, e1);
                        e1.printStackTrace();
                    }
                }
            }            
        });
    }   
    
    public String queryMobileNumberByGUID(String requestGUID) throws ClientProtocolException, IOException, AccountException{
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET, Servlet.COMMAND_QUERY_NUMBER)
            .parameter("guid", requestGUID)
            .create();

        return paraseValueInJsonResult(doRequest(request), "mobile");
    }
    
    public String resetPwdByMobile(String requestGUID , String passWord) throws ClientProtocolException, IOException, AccountException{
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET, Servlet.COMMAND_RESET_PASSWORD_BY_MOBILE)
            .parameter("guid", requestGUID)
            .parameter("password", passWord)
            .create();
        return paraseErrorMsg(doRequest(request));
    }
    
    private final static class Servlet{        
        private static final String COMMAND_QUERY_NUMBER = "account/account_query";
        private static final String COMMAND_REGISTER_MOBILE = "account/mobile_register";
        private static final String COMMAND_RESET_PASSWORD_BY_MOBILE = "account/reset_password_by_mobile";
    }
}
