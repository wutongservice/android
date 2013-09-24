package com.borqs.account.login.service;

public class ConstData { 
    public static final String PROFILE_GENDER_MALE = "m";
    public static final String PROFILE_GENDER_FEMALE = "f";
    
    public static final String REGISTER_RESPONSE_RESULT = "from_register";
    
    // Global configuration, DO NOT MDOIFY
    public static final String BORQS_ACCOUNT_TYPE = "com.borqs";

    public static final String INTENT_ACTION_ACCOUNT_LOGIN = "com.borqs.intent.action.ACCOUNT_LOGIN";
    public static final String INTENT_ACTION_ACCOUNT_LOGOUT = "com.borqs.intent.action.ACCOUNT_LOGOUT";
    
    public static final String LOGIN_BY_PASS_WAY = "login_by_pass";
    public static final String OPTIONS_RELOGIN = "force_relogin";
    public static final String DEFAULT_AUTH_TYPE = "com.borqs.service";
    
    // key of user data in system after account login, be able to
    // access by API getUserData().
    public static final String ACCOUNT_SESSION = "borqs_session";
    public static final String ACCOUNT_NICK_NAME = "borqs_nick_name";
    public static final String ACCOUNT_USER_ID = "borqs_uid";
    public static final String ACCOUNT_LOGIN_ID = "login_uid";
    public static final String ACCOUNT_SCREEN_NAME = "borqs_screen_name";
    public static final String ACCOUNT_ERROR = "borqs_account_error";
    public static final String ACCOUNT_GUID = "borqs_account_login_guid";
    
    //error code
    public static final int ERROR_UNKNOWN = 230;
    public static final int ERROR_SERVER_CONNECT = 231;
    public static final int ERROR_SERVER_RSP_DATA = 232;
    public static final int ERROR_REGISTER_TIME_OUT = 233;
    public static final int ERROR_SEND_REGISTER_SMS = 234;
    public static final int ERROR_NO_SIM_CARD = 235;
    public static final int ERROR_SERVER_ERROR = 236;
    public static final int ERROR_TICKET_INVALID = 237;
    public static final int ERROR_USER_PWD_INVALID = 238;
    public static final int ERROR_NO_PHONE = 239;
    public static final int ERROR_USER_NOT_EXISTS = 240;
    public static final int ERROR_NO_USER_RECORD = 241;
    public static final int ERROR_NO_INPUT_VERFIY_CODE = 242;
    public static final int ERROR_VERIFY_CODE = 243;
    public static final int ERROR_VERFIY_CODE_OUT = 244;
    public static final int ERROR_INVALID_USER = 245;
    public static final int ERROR_SERVER_EXCEPTION = 246;
    
    public static final String LOGIN_REGISTER_FEATURE = "register_feature";
    
    @Deprecated
    public final static int FEATURE_REGISTER_BY_PHONE = 0x01;
    @Deprecated
    public final static int FEATURE_REGISTER_BY_EMAIL = 0x02;
    
    public final static int FEATURE_SUPPORT_EMAIL = 0x01;
    public final static int FEATURE_SUPPORT_PHONE = 0x02;
    public final static int FEATURE_SUPPORT_ONEKEY_REGISTER = 0x04;
    
}
