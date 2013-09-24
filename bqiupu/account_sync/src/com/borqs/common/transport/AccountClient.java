/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.transport;

import android.content.Context;
import com.borqs.account.login.util.BLog;
import com.borqs.common.account.*;
import com.borqs.sync.client.common.AppConstant;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Client of Borqs cloud service
 * @author Borqs
 *
 */
public final class AccountClient extends SyncHttpRequestExecutor {
	public AccountClient(Context context, HttpClient httpClient){
	    super(context, httpClient);
	}

    @Override
    protected String getHostServer() {
        return Configuration.getAccountServerHost(mContext);
    }	

    public  String getProfileDetail(String uid, String sessionTicket) throws IOException, AccountException {
        String sign = AccountInfo.md5Sign(AppConstant.BORQS_SYNC_APP_SECRET, Arrays.asList("users", "columns"));
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.ACCOUNT_SHOW)
            .parameter("users", uid)
            .parameter("ticket", sessionTicket)
            .parameter("columns", "#full")
            .parameter("sign_method", "md5")
            .parameter("sign", sign)
            .parameter("appid", AppConstant.BORQS_SYNC_APP_ID)
            .create();
        return asString(doRequest(request));
    }

    public  List<ProfileInfo> retrieveUserList(String uid, String myId, String fields) throws IOException, AccountException, JSONException {
        List<ProfileInfo> buddy_list = new ArrayList<ProfileInfo>();
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.INTERNAL_GET_USERS)
                .parameter("viewerId", myId)
                .parameter("userIds", uid)
                .parameter("cols", fields)
                .parameter("privacyEnabled", String.valueOf(false))
                .create();
        JSONArray friend_json_list = new JSONArray(asString(doRequest(request)));
        for(int i=0; i<friend_json_list.length(); i++){
            ProfileInfo buddy = ProfileInfo.from(friend_json_list.getJSONObject(i));
            buddy_list.add(buddy);
        }
        return buddy_list;
    }

    public  String updateAccount(String sessionTicket,Map<String,String> paramMap) throws IOException, AccountException{
        List<String> update_key_list = new ArrayList<String>();
        for(String value :paramMap.keySet()){
            update_key_list.add(value);
         }

        String sign = AccountInfo.md5Sign(AppConstant.BORQS_SYNC_APP_SECRET, update_key_list);
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.POST,
            Servlet.ACCOUNT_UPDATE)
            .parameter("ticket", sessionTicket)
            .parameter("sign_method", "md5")
            .parameter("sign", sign)
            .parameter("appid", AppConstant.BORQS_SYNC_APP_ID)
            .parameter(paramMap)
            .create();
        return asString(doRequest(request));
    }


    public  List<ProfileCircle> retrieveCircleList(String sessionTicket) throws IOException, AccountException, JSONException {
        String sign = AccountInfo.md5Sign(AppConstant.BORQS_SYNC_APP_SECRET, Arrays.asList("circles", "with_users", "with_public_circles"));
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.COMMAND_CIRCLE_SHOW)
                .parameter("circles", "")
                .parameter("with_users", String.valueOf(false))
                .parameter("with_public_circles", String.valueOf(true))
                .parameter("ticket", sessionTicket)
                .parameter("sign_method", "md5")
                .parameter("sign", sign)
                .parameter("appid", AppConstant.BORQS_SYNC_APP_ID)
                .create();

        JSONArray circles = new JSONArray(asString(doRequest(request)));
        List<ProfileCircle> result = new ArrayList<ProfileCircle>();
        for(int i=0; i<circles.length(); i++){
            JSONObject circle = circles.getJSONObject(i);
            result.add(ProfileCircle.fromJson(circle));
        }
        return result;
    }

    /**
     * query the AppInfo from server by packageName
     * 
     * @param packageName the app's packageName
     * @param sessionTicket the ticket who can define the user
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String getAppInfo(String packageName, String sessionTicket)
            throws ClientProtocolException, IOException {
        String sign = AccountInfo.md5Sign(AppConstant.BORQS_SYNC_APP_SECRET, Arrays.asList("apps"));
        HttpRequestBuilder reqBuilder = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.COMMAND_APP_INFO);
        reqBuilder.parameter("ticket", sessionTicket);
        reqBuilder.parameter("sign_method", "md5");
        reqBuilder.parameter("sign", sign);
        reqBuilder.parameter("appid", AppConstant.BORQS_SYNC_APP_ID);
        reqBuilder.parameter("apps", packageName);
        HttpRequestBase request = reqBuilder.create();
        String result = asString(doRequest(request));
        BLog.d("get appinfo,package:" + packageName + ",info resposne:" + result);
        return result;
    }

	private final static class Servlet{       
        private static final String COMMAND_FRIEND_SHOW = "friend/show";
        private static final String COMMAND_CIRCLE_SHOW = "circle/show";
        private static final String ACCOUNT_SHOW = "user/show";
        private static final String ACCOUNT_UPDATE = "account/update";
        private static final String REQUEST_EXCHANGE_CARD = "request/profile_access_approve";
        private static final String COMMAND_SYNC_PROFILE = "profile/syncdata";
        private static final String COMMAND_GET_PROFILE = "profile/getdata";
        private static final String COMMAND_APP_INFO = "qiupu/app/get";
        private static final String INTERNAL_GET_USERS = "internal/getUsers";
	}
}
