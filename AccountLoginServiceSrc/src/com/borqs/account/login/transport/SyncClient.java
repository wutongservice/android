package com.borqs.account.login.transport;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;

import com.borqs.account.login.intf.IAccountServiceOp;
import com.borqs.account.login.util.AccountInfo;
import com.borqs.account.login.util.BLog;
import com.borqs.account.login.util.Configuration;

public class SyncClient extends HttpRequestExecutor implements IAccountServiceOp{

    public SyncClient(Context context, HttpClient httpClient) {
        super(context, httpClient);
    }

    @Override
    protected String getHostServer() {
        //return "http://apitest.borqs.com/sync/webagent/";
        return Configuration.getWebAgentServerHost(mContext);
        // return "http://192.168.7.8:8881/";
        // "http://apptest7.borqs.com:8881/sync/webagent/";
    }
    
    public String doRequest(final String reqData, final int action) throws IOException, AccountException {
        switch (action){
        case IAccountServiceOp.SERVER_ACTION_QUERY_CONIFG:            
            return queryConfig(reqData);
        case IAccountServiceOp.SERVER_ACTION_GET_GUID_BY_SIM:
            return getGuidBySim(reqData);
        case IAccountServiceOp.SERVER_ACTION_FAST_LOGIN:
            return fastLogin(reqData);
        case IAccountServiceOp.SERVER_ACTION_NORMAL_LOGIN:
            return normalLogin(reqData);
        case IAccountServiceOp.SERVER_ACTION_VERIFY_NO_SIM:
            return verifyNosim(reqData);
        case IAccountServiceOp.SERVER_ACTION_GET_GUID_BY_CODE:
            return getGuidByCode(reqData);
        case IAccountServiceOp.SERVER_ACTION_CHANGE_FIELDS:
            return changeFields(reqData);
        case IAccountServiceOp.SERVER_ACTION_GET_NEW_PWD:
            return getNewPassword(reqData);
        case IAccountServiceOp.SERVER_ACTION_CHANGE_PHOTO:
            return changePhoto(reqData);
        default:
            throw new AccountException(AccountException.CREATE_SESSION_ERROR, "method not supprot");
        }
    }

    /**** Config *****/
    private String queryConfig(final String key) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.COMMAND_CONFIG_QUERY)
                .parameter("key", key).create();
        return paraseValueInJsonResult(doRequest(request), "result");
    }

    /******** ACCOUNT REQUEST **********/
    private String getGuidBySim(final String data) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET,
                Servlet.COMMAND_QUERY_ACCOUNTREQUEST_GETGUIDBYSIM).parameter(
                Servlet.REQUEST_PARMETER_DATA, data).create();
        return asString(doRequest(request));
    }

    private String fastLogin(final String data) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET,
                Servlet.COMMAND_QUERY_ACCOUNTREQUEST_FASTLOGIN).parameter(
                Servlet.REQUEST_PARMETER_DATA, data).create();
        return asString(doRequest(request));
    }

    private String normalLogin(final String data) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET,
                Servlet.COMMAND_QUERY_ACCOUNTREQUEST_NORMAL).parameter(
                Servlet.REQUEST_PARMETER_DATA, data).create();
        return asString(doRequest(request));
    }

    private String verifyNosim(final String data) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET,
                Servlet.COMMAND_QUERY_ACCOUNTREQUEST_VERIFYNOSIM).parameter(
                Servlet.REQUEST_PARMETER_DATA, data).create();
        return asString(doRequest(request));
    }

    private String getGuidByCode(final String data) throws IOException,
            AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET,
                Servlet.COMMAND_QUERY_ACCOUNTREQUEST_GETGUIDBYCODE).parameter(
                Servlet.REQUEST_PARMETER_DATA, data).create();
        return asString(doRequest(request));
    }

    public String changeFields(final String data) throws IOException,
            AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.POST,
                Servlet.COMMAND_QUERY_ACCOUNTREQUEST_MODIFYFIELD).parameter(
                Servlet.REQUEST_PARMETER_DATA, data).create();
        return asString(doRequest(request));
    }
    
    private String getNewPassword(final String data) throws IOException, AccountException {
        HttpRequestBase request = new HttpRequestBuilder(
                HttpRequestBuilder.GET, Servlet.COMMAND_GET_NEW_PASSWORD).parameter(
                        Servlet.REQUEST_PARMETER_DATA, data).create();
        return asString(doRequest(request));
    }
        
    /**
     * only a test class
     * @param uid
     * @param sessionTicket
     * @return
     * @throws IOException
     * @throws AccountException
     */
    public  String getProfileDetail(final String uid, String sessionTicket) throws IOException, AccountException{
        String sign = AccountInfo.md5Sign("appSecret10", Arrays.asList("users", "columns"));
        HttpRequestBase request = new HttpRequestBuilder(HttpRequestBuilder.GET,"user/show")
            .parameter("users", uid)
            .parameter("ticket", sessionTicket)
            .parameter("columns", "#full")
            .parameter("sign_method", "md5")
            .parameter("sign", sign)
            .parameter("appid", "10")
            .create();
        return asString(doRequest(request));
    }
    
    private String changePhoto(final String data) throws IOException, AccountException {
        //HttpRequestBase request = new HttpRequestBuilder(
        //        HttpRequestBuilder.POST, Servlet.COMMAND_CHANGE_PHOTO).parameter(
        //                Servlet.REQUEST_PARMETER_DATA, data).create();
        HttpRequestBuilder reqBuilder = new HttpRequestBuilder(HttpRequestBuilder.POST,
                                                                Servlet.COMMAND_CHANGE_PHOTO);
        try {
            JSONObject ro = new JSONObject(data);
            reqBuilder.parameter(Servlet.TAG_TICKET, ro.optString(Servlet.TAG_TICKET));
            reqBuilder.entity(new StringEntity(ro.optString(Servlet.TAG_PHOTO), "UTF-8"));
        } catch (Exception exp){
            BLog.d("change photo exception:" + exp.getMessage());
        }
        
        HttpRequestBase request = reqBuilder.create();
        return asString(doRequest(request));
    }
}
