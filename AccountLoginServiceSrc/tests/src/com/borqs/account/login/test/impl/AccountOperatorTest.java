package com.borqs.account.login.test.impl;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.test.AndroidTestCase;
import android.text.TextUtils;

import com.borqs.account.login.impl.AccountOperator;
import com.borqs.account.login.service.AccountService;
import com.borqs.account.login.service.ConstData;
import com.borqs.account.login.util.AccountHelper;
import com.borqs.account.login.util.BLog;

public class AccountOperatorTest extends AndroidTestCase {
    
    private void removeAccount() {
        AccountManager am = AccountManager.get(mContext);

        Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            BLog.d("removeAccount");
            am.removeAccount(accounts[0], null, null);
            tryWait(3000);
        }
    }
    
    private boolean hasBorqsAccount() {
        boolean res = false;
        AccountManager am = AccountManager.get(mContext);
        BLog.d("hasBorqsAccount 1");
        Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
        BLog.d("hasBorqsAccount 2");
        if (accounts.length > 0) {
            BLog.d("hasBorqsAccount:" + accounts[0].name);
           if (accounts[0].name.equals("13521680964")){
               BLog.d("hasBorqsAccount 3");
               res = true;
           }
        }
        BLog.d("hasBorqsAccount 4");
        return res;
    }
    
    private void tryWait(long delay){
        try{
            Thread.sleep(delay);
        } catch (Exception e){
            
        }
    }
    
    private String getUserData(String key){
        /*String res = "";
        AccountManager am = AccountManager.get(mContext);
        Account[] accounts = am.getAccountsByType(ConstData.BORQS_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            try{
                res = am.getUserData(accounts[0], key);
            }catch(Exception e){
                BLog.d("ACD getuserdata error:" + e.getMessage());
            }
        }
        return res;*/
        
        AccountService acs = new AccountService(mContext);
        return acs.getUserData(key);
    }
    
    /**
     * doFastLogin test case 1: exception & error testing
     */
    public void test_doFastLogin1(){
        removeAccount();
        
        // do Fast login, no account data
        MockDevice device = new MockDevice();            
        MockAccountServiceOp serviceOp = new MockAccountServiceOp();        
        AccountOperator op = new AccountOperator(mContext, device, serviceOp);
        
        //1: no guid && no sim id, will cause no sim id error
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_NO_SIM_CARD));
        
        //2: client no guid but have device id, this will goto getGuidByDeviceId
        op = new AccountOperator(mContext, device, serviceOp);
        device.setDeviceId("123456");
        
        //2.1: server return "" or null
        serviceOp.setGetGuidBySimResult(null, false, false);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
        
        //2.2: server IOException
        serviceOp.setGetGuidBySimResult("test", true, false);        
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_CONNECT));
        
        //2.3: server AccountException
        serviceOp.setGetGuidBySimResult("test", false, true);        
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());
        assertTrue(op.getError().contains(serviceOp.getExceptionReason()));
        
        //3: server no guid, this will go register process
        serviceOp.setGetGuidBySimResult("{\"result\":\"\"}", false, false);        
        device.setDeviceId("123456");
        
        //3.1 queryConfig exception        
        serviceOp.setQueryConfigResult(null, true, false);
        device.setDeviceId("123456");        
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_CONNECT));
        
        //3.2 queryConfig return null
        serviceOp.setQueryConfigResult(null, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());        
        assertTrue(op.getError().contains(AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SEND_REGISTER_SMS)));
        
        //3.3 queryConfig return wrong sms servie center number
        serviceOp.setQueryConfigResult("1086253938257", false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());        
        assertTrue(op.getError().contains(AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SEND_REGISTER_SMS)));
        
        //3.3 queryConfig return right, but server not return the guid will cause register time out
        // TODO: this case must run in a phone with valid sim card(can send sms)
        serviceOp.setQueryConfigResult("13521680964", false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError());        
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_REGISTER_TIME_OUT));
    }
    
    /**
     * doFastLogin test case 2
     * client no account, new register in server
     */
    public void test_doFastLogin2(){
        removeAccount();
        
        // this case will goto loginByGuid
        MockDevice device = new MockDevice();            
        MockAccountServiceOp serviceOp = new MockAccountServiceOp();
        
        serviceOp.setGetGuidBySimResult("{\"result\":\"6502-3249-1101-2506\"}", false, false); 
        serviceOp.setQueryConfigResult("13521680964", false, false);
        device.setDeviceId("123456");
        
        // 1 exception & error
        // 1.1 fastLogin io exception
        serviceOp.setFastLoginResult(null, true, false);
        AccountOperator op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError()); 
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_CONNECT));
        
        // 1.2 fastLogin AccountException
        serviceOp.setFastLoginResult(null, false, true);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError()); 
        assertTrue(op.getError().contains(serviceOp.getExceptionReason()));
        
        // 1.3 fastLogin return null or ""
        serviceOp.setFastLoginResult(null, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError()); 
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
                
        // 1.4 falstLogin return illegal string, no display_name
        String result = "{\"create\":\"true\",\"result\":{\"user_id\" : \"10524\"," +
        		        "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
        		        "\"name\" : \"lin\", " +
        		        "\"login_name\" : \"13521680964\"}";
        serviceOp.setFastLoginResult(result, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doFastLogin());
        assertTrue(op.haveError()); 
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
        
        // 2 normal process
        // 2.1 server created a new account
        result = "{\"create\":\"true\",\"result\":{\"user_id\" : \"10524\"," +
                "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
                "\"display_name\" : \"lin\", " +
                "\"login_name\" : \"13521680964\"}}";
        serviceOp.setFastLoginResult(result, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertTrue(op.doFastLogin());
        assertFalse(op.haveError()); 
        assertTrue(op.isNewCreated());
        tryWait(3000);
        assertTrue(hasBorqsAccount());
        assertEquals(getUserData(ConstData.ACCOUNT_GUID), "6502-3249-1101-2506");
        
        // 2.2 server already have the account
        removeAccount();
        result = "{\"result\":{\"user_id\" : \"10524\"," +
                "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
                "\"display_name\" : \"lin\", " +
                "\"login_name\" : \"13521680964\"}}";
        serviceOp.setFastLoginResult(result, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertTrue(op.doFastLogin());
        assertFalse(op.haveError()); 
        assertFalse(op.isNewCreated());
        tryWait(3000);
        assertTrue(hasBorqsAccount());
        assertEquals(getUserData(ConstData.ACCOUNT_NICK_NAME), "lin");
        //removeAccount();
    }
    
    public void test_doNormalLogin(){
        removeAccount();
        
        // caller must assure user name&pwd not null                    
        MockAccountServiceOp serviceOp = new MockAccountServiceOp();        
        AccountOperator op = new AccountOperator(mContext, serviceOp);
        
        // 1 exception & error
        // 1.1 doNormalLogin io exception
        serviceOp.setNormalLoginResult(null, true, false);
        assertFalse(op.doNormalLogin("10524", "123456"));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_CONNECT));
        
        // 1.1 doNormalLogin account exception
        serviceOp.setNormalLoginResult(null, false, true);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.doNormalLogin("10524", "123456"));
        assertTrue(op.haveError());
        assertTrue(op.getError().contains(serviceOp.getExceptionReason()));
        
        // 1.2 doNormalLogin return null or "" result
        serviceOp.setNormalLoginResult(null, false, false);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.doNormalLogin("10524", "123456"));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
        
        // 1.3 doNormalLogin return illegal string, no display name
        String result = "{\"result\":{\"user_id\" : \"10524\"," +
                "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +                
                "\"login_name\" : \"13521680964\"}}";
        serviceOp.setNormalLoginResult(result, false, false);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.doNormalLogin("10524", "123456"));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
        
        // 2 normal Login
       result = "{\"result\":{\"user_id\" : \"10524\"," +
                "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
                "\"display_name\" : \"lin\", " +
                "\"login_name\" : \"13521680964\"}}";
       serviceOp.setNormalLoginResult(result, false, false);
       op = new AccountOperator(mContext, serviceOp);
       assertTrue(op.doNormalLogin("10524", "123456"));
       assertFalse(op.haveError()); 
       assertFalse(op.isNewCreated());
       tryWait(5000);
       assertTrue(hasBorqsAccount());
       assertEquals(getUserData(ConstData.ACCOUNT_NICK_NAME), "lin");
       removeAccount();
    }
    
    public void test_getVerifyCode(){
        // caller must assure user number&fmt not null                    
        MockAccountServiceOp serviceOp = new MockAccountServiceOp();        
        AccountOperator op = new AccountOperator(mContext, serviceOp);
        
        // 1 exception & error
        // 1.1 verifyNosim io exception
        serviceOp.setVerifyNosimResult(null, true, false);
        assertFalse(op.getVerifyCode("13521680964", "your verify code is "));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_CONNECT));
        
        // 1.1 verifyNosim account exception
        serviceOp.setVerifyNosimResult(null, false, true);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.getVerifyCode("13521680964", "your verify code is "));
        assertTrue(op.haveError());
        assertTrue(op.getError().contains(serviceOp.getExceptionReason()));
        
        // 1.2 verifyNosim return null or "" result
        serviceOp.setVerifyNosimResult("", false, false);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.getVerifyCode("13521680964", "your verify code is "));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
        
        // 1.3 verifyNosim return error
        String result = "{\"error_msg\":\"server busy\"}";
        serviceOp.setVerifyNosimResult(result, false, false);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.getVerifyCode("13521680964", "your verify code is "));
        assertTrue(op.haveError());
        assertTrue(op.getError().contains("server busy"));
        
        // 2 verifyNosim return OK
       result = "{\"result\":\"ok\"}";
       serviceOp.setVerifyNosimResult(result, false, false);
       op = new AccountOperator(mContext, serviceOp);
       assertTrue(op.getVerifyCode("13521680964", "your verify code is "));
       assertFalse(op.haveError()); 
    }
    
    /**
     * doVerifyCodeLogin test case 1
     * main test getGuidByCode exception case
     */
    public void test_doVerifyCodeLogin1(){
        removeAccount();
        
        // caller must assure number&code not null                    
        MockAccountServiceOp serviceOp = new MockAccountServiceOp();        
        AccountOperator op = new AccountOperator(mContext, serviceOp);
        
        // 1 exception & error
        // 1.1 getGuidByCode io exception        
        serviceOp.setGetGuidByCodeResult(null, true, false);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_CONNECT));
        
        // 1.1 getGuidByCode account exception
        serviceOp.setGetGuidByCodeResult(null, false, true);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError());
        assertTrue(op.getError().contains(serviceOp.getExceptionReason()));
        
        // 1.2 getGuidByCode return null or "" result
        serviceOp.setGetGuidByCodeResult(null, false, false);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
        
        // 1.3 getGuidByCode return illegal string
        String result = "{\"ahh\":\"user_id\"}";
        serviceOp.setGetGuidByCodeResult(result, false, false);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError());
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));        
        
        // 1.4 getGuidByCode return error
        result = "{\"error_msg\":\"server busy\"}";
        serviceOp.setGetGuidByCodeResult(result, false, false);
        op = new AccountOperator(mContext, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError());
        assertTrue(op.getError().contains("server busy"));
    }
    
    /**
     * doVerifyCodeLogin test case 2
     * main test loginByGuid exception case
     */
    public void test_doVerifyCodeLogin2(){
        removeAccount();
        
        // this case will goto loginByGuid
        MockDevice device = new MockDevice();            
        MockAccountServiceOp serviceOp = new MockAccountServiceOp();        
        serviceOp.setGetGuidByCodeResult("{\"result\":\"6502-3249-1101-2506\"}", false, false); 
                
        // 1 exception & error
        // 1.1 fastLogin io exception
        serviceOp.setFastLoginResult(null, true, false);
        AccountOperator op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError()); 
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_CONNECT));
        
        // 1.2 fastLogin AccountException
        serviceOp.setFastLoginResult(null, false, true);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError()); 
        assertTrue(op.getError().contains(serviceOp.getExceptionReason()));
        
        // 1.3 fastLogin return null or ""
        serviceOp.setFastLoginResult(null, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError()); 
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
                
        // 1.4 falstLogin return illegal string, no display_name
        String result = "{\"create\":\"true\",\"result\":{\"user_id\" : \"10524\"," +
                        "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
                        "\"name\" : \"lin\", " +
                        "\"login_name\" : \"13521680964\"}";
        serviceOp.setFastLoginResult(result, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertFalse(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertTrue(op.haveError()); 
        assertEquals(op.getError(), AccountHelper.GetErrorDesc(mContext, ConstData.ERROR_SERVER_RSP_DATA));
        
        // 2 normal process
        // 2.1 server created a new account
        result = "{\"create\":\"true\",\"result\":{\"user_id\" : \"10524\"," +
                "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
                "\"display_name\" : \"lin\", " +
                "\"login_name\" : \"13521680964\"}}";
        serviceOp.setFastLoginResult(result, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        
        assertTrue(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        //op.doVerifyCodeLogin("13521680964", "123456", "1234");
        //BLog.d("last error:" + op.getError());
        assertFalse(op.haveError()); 
        assertTrue(op.isNewCreated());
        tryWait(2000);
        assertTrue(hasBorqsAccount());
        assertEquals(getUserData(ConstData.ACCOUNT_GUID), "6502-3249-1101-2506");
        
        // 2.2 server already have the account
        removeAccount();        
        result = "{\"result\":{\"user_id\" : \"10524\"," +
                "\"ticket\" : \"MTM1MjE2ODA5NjRfMTM0MzExNDYyMDAwNl83OTY4\"," +
                "\"display_name\" : \"lin\", " +
                "\"login_name\" : \"13521680964\"}}";
        serviceOp.setFastLoginResult(result, false, false);
        op = new AccountOperator(mContext, device, serviceOp);
        assertTrue(op.doVerifyCodeLogin("13521680964", "123456", "1234"));
        assertFalse(op.haveError()); 
        assertFalse(op.isNewCreated());
        tryWait(2000);
        assertTrue(hasBorqsAccount());
        assertEquals(getUserData(ConstData.ACCOUNT_NICK_NAME), "lin");
        removeAccount();
    }
    
    /*public void test_changeProfileInfo(){
        
    }*/
}
