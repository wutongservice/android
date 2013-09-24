package com.borqs.account.login.test.impl;

import java.io.IOException;

import com.borqs.account.login.intf.IAccountServiceOp;
import com.borqs.account.login.transport.AccountException;

public class MockAccountServiceOp implements IAccountServiceOp {
    private final String STR_IOException = "net work error";
    private final String STR_ACNException = "session error";
    
    private boolean mIsIOException;
    
    private boolean mIsQueriyConfigIOException;
    private boolean mIsQueriyConfigAccountException;
    private String mQueryConfigResult;
    
    private boolean mIsGetGuidByCodeIOException;
    private boolean mIsGetGuidByCodeAccountException;
    private String mGetGuidByCodeResult;
    
    private boolean mIsVerifyNosimIOException;
    private boolean mIsVerifyNosimAccountException;
    private String mVerifyNosimResult;
    
    private boolean mIsNormalLoginIOException;
    private boolean mIsNormalLoginAccountException;
    private String mNormalLoginResult;
    
    private boolean mIsFastLoginIOException;
    private boolean mIsFastLoginAccountException;
    private String mFastLoginResult;
    
    private boolean mIsGetGuidBySimIOException;
    private boolean mIsGetGuidBySimAccountException;
    private String mGetGuidBySimResult;
    
    private boolean mIsGetNewPwdIOException;
    private boolean mIsGetNewPwdAccountException;
    private String mGetNewPwdResult;
    
    @Override
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
        default:
            throw new AccountException(AccountException.CREATE_SESSION_ERROR, "method not supprot");
        }
    }
    
    public String getExceptionReason(){
        return mIsIOException?STR_IOException:STR_ACNException;
    }
    
    public String queryConfig(String key) throws IOException, AccountException {
        return doMockRequest(mQueryConfigResult, mIsQueriyConfigIOException, mIsQueriyConfigAccountException);
    }
    
    public void setQueryConfigResult(String result, boolean isIOException, boolean isAcnException){
        mQueryConfigResult = result;
        mIsQueriyConfigIOException = isIOException;
        mIsQueriyConfigAccountException = isAcnException;
    }

    public String getGuidBySim(String data) throws IOException,
            AccountException {
        return doMockRequest(mGetGuidBySimResult, mIsGetGuidBySimIOException, mIsGetGuidBySimAccountException);
    }
    
    public void setGetGuidBySimResult(String result, boolean isIOException, boolean isAcnException){
        mGetGuidBySimResult = result;
        mIsGetGuidBySimIOException = isIOException;
        mIsGetGuidBySimAccountException = isAcnException;
    }

    public String fastLogin(String data) throws IOException, AccountException {
        return doMockRequest(mFastLoginResult, mIsFastLoginIOException, mIsFastLoginAccountException);
    }
    
    public void setFastLoginResult(String result, boolean isIOException, boolean isAcnException){
        mFastLoginResult = result;
        mIsFastLoginIOException = isIOException;
        mIsFastLoginAccountException = isAcnException;
    }

    public String normalLogin(String data) throws IOException, AccountException {
        return doMockRequest(mNormalLoginResult, mIsNormalLoginIOException, mIsNormalLoginAccountException);
    }
    
    public void setNormalLoginResult(String result, boolean isIOException, boolean isAcnException){
        mNormalLoginResult = result;
        mIsNormalLoginIOException = isIOException;
        mIsNormalLoginAccountException = isAcnException;
    }

    public String verifyNosim(String data) throws IOException, AccountException {
        return doMockRequest(mVerifyNosimResult, mIsVerifyNosimIOException, mIsVerifyNosimAccountException);
    }
    
    public void setVerifyNosimResult(String result, boolean isIOException, boolean isAcnException){
        mVerifyNosimResult = result;
        mIsVerifyNosimIOException = isIOException;
        mIsVerifyNosimAccountException = isAcnException;
    }

    public String getGuidByCode(String data) throws IOException,AccountException {
        return doMockRequest(mGetGuidByCodeResult, mIsGetGuidByCodeIOException, mIsGetGuidByCodeAccountException);
    }
    
    public void setGetGuidByCodeResult(String result, boolean isIOException, boolean isAcnException){
        mGetGuidByCodeResult = result;
        mIsGetGuidByCodeIOException = isIOException;
        mIsGetGuidByCodeAccountException = isAcnException;
    }
    
    public String getNewPassword(String userName) throws IOException, AccountException {
        return doMockRequest(mGetNewPwdResult, mIsGetNewPwdIOException, mIsGetNewPwdAccountException);
    }
    
    public void setNewPasswordResult(String result, boolean isIOException, boolean isAcnException){
        mGetNewPwdResult = result;
        mIsGetNewPwdIOException = isIOException;
        mIsGetNewPwdAccountException = isAcnException;
    }

    public String changeFields(String data) throws IOException,AccountException {
        return doMockRequest("{\"result\":\"ok\"}", false, false);
    }

    private String doMockRequest(String result, boolean isIOException, boolean isAcnException) 
            throws IOException, AccountException{
        mIsIOException = false;
        if (isIOException){
            mIsIOException = true;
            throw new IOException(STR_IOException);
        }
        if (isAcnException){
            throw new AccountException(AccountException.CREATE_SESSION_ERROR, STR_ACNException);
        }
        return result;
    }
}
