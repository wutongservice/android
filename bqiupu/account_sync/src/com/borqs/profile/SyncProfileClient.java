/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.profile;

import java.io.IOException;

import com.borqs.common.transport.SyncHttpRequestExecutor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;

import android.content.Context;

import com.borqs.common.account.AccountException;
import com.borqs.common.account.Configuration;

/**
 * Client of Borqs cloud service
 * @author Borqs
 *
 */
public final class SyncProfileClient extends SyncHttpRequestExecutor {
    private final String PARAM_USER_ID = "uid";
    private final String PARAM_TICKET = "ticket";
    
	public SyncProfileClient(Context context, HttpClient httpClient){
	    super(context, httpClient);
	}

    @Override
    protected String getHostServer() {
        return Configuration.getWebagentHost(mContext);
        //return "http://192.168.4.35:8881/"; //localhost, del "sync/webagent"
    }	
	
    public  String getProfile(String uid, String sessionTicket) throws IOException, AccountException{
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET, Servlet.COMMAND_GET_PROFILE)
            .parameter(PARAM_USER_ID, uid)
            .parameter(PARAM_TICKET, sessionTicket)
            .create();        
        return asString(doRequest(request));
    }
    
	public String syncProfile(String uid, String sessionTicket, String localInfo) throws AccountException, IOException{
	    HttpRequestBuilder reqBuilder = new HttpRequestBuilder(HttpRequestBuilder.POST,
                                                               Servlet.COMMAND_SYNC_PROFILE);
	    reqBuilder.parameter(PARAM_TICKET, sessionTicket);
        reqBuilder.parameter(PARAM_USER_ID, uid);     
        reqBuilder.entity(new StringEntity(localInfo, "UTF-8"));
        
        HttpRequestBase request = reqBuilder.create();
        String result = asString(doRequest(request));
	    return result;
	}
    
    // the following code for testing
    /*private static int mGetCount = 0;
    public  String getProfile(String uid, String sessionTicket) throws IOException, AccountException{
        mGetCount++;
        BLog.d("current get test is:" + mGetCount);
        String result = null;
        if (mGetCount%7 == 1){
            throw new IOException("profile test io exception");
        } else if (mGetCount%7 == 2){
            throw new AccountException(211, "profile test account exception");
        } else if (mGetCount%7 == 3){ //empty string
            result = "";
        } else if (mGetCount%7 == 4){ //error
            result = "{\"error\":\"211\",\"error_msg\":\"user not exits\"}";
        } else if (mGetCount%7 == 5){ //invalide string
            result = "{\"result\":\"ok\",\"data\":\"\"}";
        } else if (mGetCount%7 == 6){ //invalide string
            result = "{\"result\":\"ok\",\"data\":\"jdfls\"}";
        } else { // correct
            mGetCount = 0;
            result = "{\"result\":\"ok\","
                    +"\"data\":{\"TEL\":[{\"MOBILE\":\"135 2168 0964\"},{\"HOME\":\"652 4345\"}],"
                    +"\"N\":{\"MN\":\"x\",\"FN\":\"h\",\"LN\":\"l\",\"NN\":\"linghjjj\"}," 
                    +"\"EMAIL\":[{\"HOME\":\"tuugg@fgg.hj\"}],"
                    +"\"ADDR\":[{\"OTHER\":{\"ZC\":\"\",\"PRO\":\"\",\"ST\":\"北京东\",\"CITY\":\"\"}}]}"
                    +"}";
        }  
        
        return result;
    }
    
    private static int mSyncCount = 0;
    public String syncProfile(String uid, String sessionTicket, String localInfo) throws AccountException, IOException{     
        mSyncCount++;
        BLog.d("current sync test is:" + mSyncCount);
        String result = null;
        if (mSyncCount%8 == 1){
            throw new IOException("profile test io exception");
        } else if (mSyncCount%8 == 2){
            throw new AccountException(211, "profile test account exception");
        } else if (mSyncCount%8 == 3){ //empty string
            result = null;
        } else if (mSyncCount%8 == 4){ //error
            result = "{\"error\":\"211\",\"error_msg\":\"user not exits\"}";
        } else if (mSyncCount%8 == 5){ //invalide string
            result = "{\"result\":\"ok\",\"data\":\"\"}";
        } else if (mSyncCount%8 == 6){ //OK, but no need update in client
            result = "{\"result\":\"ok\"}";
        } else if (mSyncCount%8 == 7){ //OK, but may cause JSONExceptin
            result = "{\"result\":\"ok\", \"data\":\"{\"EMAIL\":[{\"HOME\":\"tuugg@fgg.hj\"}],}\"}";
        }else { // correct
            mSyncCount = 0;
            result = "{\"result\":\"ok\","
                    +"\"data\":{\"TEL\":[{\"MOBILE\":\"135 2168 0964\"},{\"HOME\":\"0106524345\"}],"
                    +"\"N\":{\"MN\":\"x\",\"FN\":\"h\",\"LN\":\"l\",\"NN\":\"linxh\"}," 
                    +"\"EMAIL\":[{\"HOME\":\"tuugg@fgg.hj\"}]}"                    
                    +"}";
        }  
        
        return result;
    }*/

	private final static class Servlet{
        private static final String COMMAND_SYNC_PROFILE = "profile/syncdata";
        private static final String COMMAND_GET_PROFILE = "profile/getdata";
	}
}
