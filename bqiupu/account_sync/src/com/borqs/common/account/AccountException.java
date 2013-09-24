/*
 * Copyright (C) 2007-2011 Borqs Ltd.
 *  All rights reserved.
 */
package com.borqs.common.account;

public class AccountException extends Exception {
	public static final int INVALID_CODE = -1;
	
	//defined for Account service
	public static final int REGISTER_DUPLICATED_EMAIL_CODE = 102;
	public static final int REGISTER_DUPLICATED_MOBILE_CODE = 103;
	public static final int LOGIN_INVALID_USER_PASSWORD = 201;
	
    // password error or user is not exists when login
    public static final int LOGIN_NAME_OR_PASSWORD_ERROR = 209;

    // user is already exists when register
    public static final int LOGIN_NAME_EXISTS = 210;

    // user is not exists when query from account server
    public static final int USER_NOT_EXISTS = 211;

    // create user ID error
    public static final int GENERATE_USER_ID_ERROR = 212;
    // create ticket error when login
    public static final int CREATE_SESSION_ERROR = 213;

    // defined for Sync App service
    public static final int SYNC_APP_NOT_FOUNF_CODE = 511;
    private int mErrorCode;
    private static final long serialVersionUID = -2623309261327598086L;
	
	public AccountException(int code, String msg){
		super(msg);
		mErrorCode = code;
	}
	
	public AccountException(Exception e){
		super(e);
		mErrorCode = INVALID_CODE;
	}
	
	public int getErrorCode(){
		return mErrorCode;
	}
}
