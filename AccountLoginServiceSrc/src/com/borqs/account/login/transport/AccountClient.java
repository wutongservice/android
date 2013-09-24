/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.account.login.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.AccountInfo;
import com.borqs.account.login.util.AccountSession;
import com.borqs.account.login.util.BLog;
import com.borqs.account.login.util.Configuration;
import com.borqs.account.login.util.ProfileContactInfo;
import com.borqs.account.login.util.ProfileInfo;
import com.borqs.account.login.util.Utility;

/**
 * Client of Borqs cloud service
 * 
 * @author Borqs
 * 
 */
public final class AccountClient extends HttpRequestExecutor {
    public AccountClient(Context context, HttpClient httpClient) {
        super(context, httpClient);
    }

    @Override
    protected String getHostServer() {
        return Configuration.getAccountServerHost(mContext);        
    }

    /**
     * register a account by the loginId and password asynchronous, and the
     * result is sent back by parameter result if set
     * 
     * @param loginId
     *            - user id
     * @param password
     *            - the password
     * @param callback
     *            - result holder
     */
    public synchronized void signup(String featureId, String loginId,
            String password, String nickName, String cellNumber, String imsi,
            String imei, final OnResultCallback<Boolean> callback) {
        String pwdBase64 = Utility.MD5Encode(password);
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.COMMAND_REGISTE)
                .parameter("login_email", loginId)
                .parameter("password", pwdBase64)
                .parameter("login_phone", cellNumber)
                .parameter("display_name", nickName)
                .parameter("appid", featureId).parameter("imei", imei)
                .parameter("imsi", imsi).create();

        doRequestAsync(request, new OnResultCallback<HttpResponse>() {
            @Override
            public void onResult(HttpResponse resp, Exception e) {
                try {
                    boolean result = parseBooleanResult(resp);
                    callback.onResult(result, e);
                } catch (IOException ioe) {
                    BLog.d("IOException(signup): " + ioe.getMessage());
                    callback.onResult(null, ioe);
                } catch (AccountException ae) {
                    BLog.d("ServerError(signup): " + ae.getMessage());
                    callback.onResult(null, ae);
                }
            }
        });
    }

    /**
     * sign in a account
     * 
     * @param loginId
     *            - user id
     * @param password
     *            - the password
     * @param callback
     *            - result holder
     */
    public synchronized void signIn(String featureId, String loginId,
            String password, final OnResultCallback<AccountSession> callback) {
        String pwdBase64 = Utility.MD5Encode(password);
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.COMMAND_LOGIN)
                .parameter("login_name", loginId).parameter("appid", featureId)
                .parameter("password", pwdBase64).create();

        doRequestAsync(request, new OnResultCallback<HttpResponse>() {
            @Override
            public void onResult(HttpResponse resp, Exception e) {
                try {
                    AccountSession data = parseJSONUserSession(asString(resp));
                    callback.onResult(data, e);
                } catch (IOException ioe) {
                    BLog.d("IOException(signIn): " + ioe.getMessage());
                    callback.onResult(null, ioe);
                } catch (AccountException ae) {
                    BLog.d("ServerError(signIn): " + ae.getMessage());
                    callback.onResult(null, ae);
                }
            }
        });
    }

    public synchronized void resetPwdByEmail(String emailAddress,
            final OnResultCallback<Boolean> callback) {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.COMMAND_RESET_PASSWORD_BY_EMAIL)
                .parameter("login_name", emailAddress).create();

        doRequestAsync(request, new OnResultCallback<HttpResponse>() {
            @Override
            public void onResult(HttpResponse result, Exception e) {
                try {
                    boolean isSucc = false;
                    if (result != null) {
                        isSucc = parseBooleanResult(result);
                    }
                    callback.onResult(isSucc, e);
                } catch (IOException ioe) {
                    BLog.d("IOException(query): " + ioe.getMessage());
                    callback.onResult(false, ioe);
                } catch (AccountException ae) {
                    BLog.d("ServerError(query): " + ae.getMessage());
                    callback.onResult(false, ae);
                }
            }
        });
    }

    public synchronized ProfileContactInfo getProfileContactInfoAsObject(
            String uid, String sessionTicket) throws IOException,
            AccountException {
        String sign = AccountInfo.md5Sign(AccountHelper.BORQS_SYNC_APP_SECRET,
                Arrays.asList("users", "columns"));
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.ACCOUNT_SHOW)
                .parameter("users", uid).parameter("ticket", sessionTicket)
                .parameter("columns", "contact_info")
                .parameter("sign_method", "md5").parameter("sign", sign)
                .parameter("appid", AccountHelper.BORQS_SYNC_APP_ID).create();
        return parseJSONContactInfo(asString(doRequest(request)));
    }

    public String getProfileContactInfo(String uid, String sessionTicket)
            throws IOException, AccountException {
        String sign = AccountInfo.md5Sign(AccountHelper.BORQS_SYNC_APP_SECRET,
                Arrays.asList("users", "columns"));
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.ACCOUNT_SHOW)
                .parameter("users", uid).parameter("ticket", sessionTicket)
                .parameter("columns", "contact_info")
                .parameter("sign_method", "md5").parameter("sign", sign)
                .parameter("appid", AccountHelper.BORQS_SYNC_APP_ID).create();
        return asString(doRequest(request));
    }

    public String getProfileDetail(String uid, String sessionTicket)
            throws IOException, AccountException {
        String sign = AccountInfo.md5Sign(AccountHelper.BORQS_SYNC_APP_SECRET,
                Arrays.asList("users", "columns"));
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.ACCOUNT_SHOW)
                .parameter("users", uid).parameter("ticket", sessionTicket)
                .parameter("columns", "#full").parameter("sign_method", "md5")
                .parameter("sign", sign)
                .parameter("appid", AccountHelper.BORQS_SYNC_APP_ID).create();
        return asString(doRequest(request));
    }

    public ProfileInfo getProfileDetailAsObject(String uid, String sessionTicket)
            throws IOException, AccountException {
        String detail = getProfileDetail(uid, sessionTicket);
        return parseJSONDetailInfo(detail);
    }

    public String getBaseProfileInfo(String uid, String sessionTicket)
            throws IOException, AccountException {
        return getProfileInfo(uid, sessionTicket,
                "gender,display_name,large_image_url,contact_info");
    }

    public String getProfileInfo(String uids, String sessionTicket,
            String fields) throws IOException {
        String sign = AccountInfo.md5Sign(AccountHelper.BORQS_SYNC_APP_SECRET,
                Arrays.asList("users", "columns"));
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.ACCOUNT_SHOW)
                .parameter("users", uids).parameter("ticket", sessionTicket)
                .parameter("columns", fields).parameter("sign_method", "md5")
                .parameter("sign", sign)
                .parameter("appid", AccountHelper.BORQS_SYNC_APP_ID).create();
        return asString(doRequest(request));
    }

    public String updateAccount(String sessionTicket,
            Map<String, String> paramMap) throws IOException, AccountException {
        String sign = AccountInfo.md5Sign(AccountHelper.BORQS_SYNC_APP_SECRET,
                Arrays.asList("users", "columns"));
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.POST, Servlet.ACCOUNT_UPDATE)
                .parameter("ticket", sessionTicket)
                .parameter("sign_method", "md5").parameter("sign", sign)
                .parameter("appid", AccountHelper.BORQS_SYNC_APP_ID)
                .parameter(paramMap).create();
        return asString(doRequest(request));
    }

    public List<ProfileInfo> retrieveFriendList(String uid,
            String sessionTicket, String fields) throws IOException,
            AccountException, JSONException {
        List<ProfileInfo> buddy_list = new ArrayList<ProfileInfo>();
        String sign = AccountInfo.md5Sign(AccountHelper.BORQS_SYNC_APP_SECRET,
                Arrays.asList("users", "columns", "page", "count"));
        if (fields == null) {
            fields = "user_id,display_name,profile_privacy";
        }
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.COMMAND_FRIEND_SHOW)
                .parameter("users", uid).parameter("ticket", sessionTicket)
                .parameter("columns", fields).parameter("page", "0")
                .parameter("count", "10000").parameter("sign_method", "md5")
                .parameter("sign", sign)
                .parameter("appid", AccountHelper.BORQS_SYNC_APP_ID).create();
        JSONArray friend_json_list = new JSONArray(asString(doRequest(request)));
        for (int i = 0; i < friend_json_list.length(); i++) {
            ProfileInfo buddy = ProfileInfo.from(friend_json_list
                    .getJSONObject(i));
            buddy_list.add(buddy);
        }
        return buddy_list;
    }

    /*
     * Example for the user session response {"nickname":"cat", "uid":"my1234",
     * "sessionid":"s0123456789", "username":"myusername",
     * "screenname":"myscreenname" }
     */
    private AccountSession parseJSONUserSession(String jsonData)
            throws AccountException {
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }

        JSONObject jobject = null;
        try {
            jobject = new JSONObject(jsonData);
            return AccountSession.from(jobject);
        } catch (JSONException e) {
            e.printStackTrace();
            throw throwError(jsonData);
        }
    }

    private ProfileInfo parseJSONDetailInfo(String jsonData)
            throws AccountException {
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }

        try {
            JSONArray list = new JSONArray(jsonData);
            JSONObject profile = list.getJSONObject(0);
            return ProfileInfo.from(profile);
        } catch (JSONException e) {
            e.printStackTrace();
            throw throwError(jsonData);
        }
    }

    private ProfileContactInfo parseJSONContactInfo(String jsonData)
            throws AccountException {
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }

        try {
            JSONArray list = new JSONArray(jsonData);
            JSONObject profile = list.getJSONObject(0);
            return ProfileContactInfo.from(profile
                    .getJSONObject("contact_info"));
        } catch (JSONException e) {
            e.printStackTrace();
            throw throwError(jsonData);
        }
    }

    /****Config *****/
    public String queryConfig(String key) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,
                Servlet.COMMAND_CONFIG_QUERY)
                .parameter("key", key)
                .create();
        return paraseValueInJsonResult(doRequest(request), "result");
    }
    
    private final static class Servlet {
        private static final String COMMAND_LOGIN = "account/login";
        private static final String COMMAND_REGISTE = "account/create";
        private static final String COMMAND_RESET_PASSWORD_BY_EMAIL = "account/reset_password";
        private static final String COMMAND_FRIEND_SHOW = "friend/show";
        private static final String COMMAND_CONFIG_QUERY = "configuration/query";
        private static final String ACCOUNT_SHOW = "user/show";
        private static final String ACCOUNT_UPDATE = "account/update";
    }
}
