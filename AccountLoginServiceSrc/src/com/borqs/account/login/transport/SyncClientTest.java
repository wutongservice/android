package com.borqs.account.login.transport;

import java.io.IOException;

import org.apache.http.client.HttpClient;

import android.content.Context;

import com.borqs.account.login.intf.IAccountServiceOp;
import com.borqs.account.login.util.BLog;

/**
 * a test class to test different exception for AccountOperator
 * @author linxh
 *
 */
public class SyncClientTest implements IAccountServiceOp{
    
    private static int mCount;
    
    public SyncClientTest(Context context, HttpClient httpClient) {
    }
    
    public String doRequest(final String reqData, final int action) throws IOException, AccountException {
        mCount++;
        if (mCount > 8){
            mCount = 1;
        }
        mCount = mCount%8;
        BLog.d("current test value:" + mCount);
        
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
        default:
            throw new AccountException(AccountException.CREATE_SESSION_ERROR, "method not supprot");
        }
    }
    
    private String queryConfig(final String key) throws IOException, AccountException {
        return null;
    }

    private String getGuidBySim(final String data) throws IOException, AccountException {
        return null;
    }

    private String fastLogin(final String data) throws IOException, AccountException {
        return "{\"result\":{\"user_id\" : \"10524\"," +
                "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
                "\"display_name\" : \"lin\", " +
                "\"login_name\" : \"13521680964\"}}";
    }

    private String normalLogin(final String data) throws IOException, AccountException {
        return null;
    }

    private String verifyNosim(final String data) throws IOException, AccountException {
        switch (mCount){
        case 1: // IOException
            throw new IOException("network failed");
        case 2: // AccountException
            throw new AccountException(AccountException.CREATE_SESSION_ERROR, "account session error");
        case 3: // server reply null string
            return "";
        case 4: // server reply invalid string
            return "\"result\"";
        case 5: // server return error: 1025
            return "{\"error_code\":\"1021\","
            +"\"error\":\"no sim card\"}";
        default: //sucess:
            tryWait(3*1000);
            return "{\"result\":\"OK\","
                    + "\"verify_code_to\":\"mail\"}";
        }
    }

    private String getGuidByCode(final String data) throws IOException,
            AccountException {
        switch (mCount){
        case 1: // IOException
            throw new IOException("network failed");
        case 2: // AccountException
            throw new AccountException(AccountException.CREATE_SESSION_ERROR, "account session error");
        case 3: // server reply null string
            return "";
        case 4: // server reply invalid string
            return "\"result\"";
        case 5: // server return error: 1025
            return "{\"error_code\":\"1022\","
            +"\"error\":\"no USER\"}";
        case 6: // server return error but no error_code
            return "{\"error\":\"no USER\"}";
        default: //sucess:
            tryWait(3*1000);
            return "{\"result\":\"123456\"}";
        }
    }

    public String changeFields(final String data) throws IOException,
            AccountException {
        return "{\"result\":\"OK\"}";
    }
    
    
    private String getNewPassword(final String data) throws IOException, AccountException {
        switch (mCount){
        /*case 1: // IOException
            throw new IOException("network failed");
        case 2: // AccountException
            throw new AccountException(AccountException.CREATE_SESSION_ERROR, "account session error");
        case 3: // server reply null string
            return null;
        case 4: // server reply invalid string
            return "\"result\"";
        case 5: // server return error: 211
            return "{\"error_code\":\"211\","
                   +"\"error_msg\":\"User is not exists\"}";
        case 6: // server return error: 1025
            return "{\"error_code\":\"1025\","
            +"\"error\":\"User is not exists\"}";*/
        default: //sucess:
            tryWait(3*1000);
            return "{\"result\":\"OK\","
                    + "\"pwd_to\":\"mail\"}";
        }
    }
    
    private void tryWait(final int millSeconds){
        try{
            Thread.sleep(millSeconds);
        } catch (Exception e){
            BLog.d("wait exception:" + e.getMessage());
        }
    }
}
