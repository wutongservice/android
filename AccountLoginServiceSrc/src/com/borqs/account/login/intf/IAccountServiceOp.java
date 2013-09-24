package com.borqs.account.login.intf;

import java.io.IOException;

import com.borqs.account.login.transport.AccountException;


/**
 * account login service operator which define the intherface that will interact info with server 
 * @author linxh
 *
 */
public interface IAccountServiceOp {
    public static final int SERVER_ACTION_NONE = 0x00;
    public static final int SERVER_ACTION_QUERY_CONIFG = 0x01;
    public static final int SERVER_ACTION_GET_GUID_BY_SIM = 0x02;
    public static final int SERVER_ACTION_FAST_LOGIN = 0x03;
    public static final int SERVER_ACTION_NORMAL_LOGIN = 0x04;
    public static final int SERVER_ACTION_VERIFY_NO_SIM = 0x05;
    public static final int SERVER_ACTION_GET_GUID_BY_CODE = 0x06;
    public static final int SERVER_ACTION_CHANGE_FIELDS = 0x07;
    public static final int SERVER_ACTION_GET_NEW_PWD = 0x08;
    public static final int SERVER_ACTION_CHANGE_PHOTO = 0X09;
    
    public String doRequest(final String reqData, final int action) throws IOException, AccountException;
    /*public String queryConfig(final String key) throws IOException, AccountException ;
    public String getGuidBySim(final String data) throws IOException, AccountException ;    
    public String fastLogin(final String data) throws IOException, AccountException ;
    public String normalLogin(final String data) throws IOException, AccountException ;
    public String verifyNosim(final String data) throws IOException, AccountException ;
    public String getGuidByCode(final String data) throws IOException, AccountException ;    
    public String changeFields(final String data) throws IOException, AccountException ;
    public String getNewPassword(final String data) throws IOException, AccountException ;*/
}
