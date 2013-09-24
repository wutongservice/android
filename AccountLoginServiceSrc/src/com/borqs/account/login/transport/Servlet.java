package com.borqs.account.login.transport;

public final class Servlet {
    public static final String TAG_GUID = "guid";
    public static final String TAG_PHONE = "phone";
    //public static final String TAG_SIM = "sim";
    public static final String TAG_DEVICE_ID = "device_id";
    public static final String TAG_VERIFYCODE = "verifycode";
    public static final String TAG_MSGFMT = "msgfmt";
    public static final String TAG_ERROR = "error";
    public static final String TAG_RESULT = "result";
    public static final String TAG_CREATE = "create";
    public static final String TAG_NAME = "name";
    public static final String TAG_PASS = "pass";
    public static final String TAG_FIELD = "fieldandvalue";
    public static final String TAG_TICKET = "ticket";
    public static final String TAG_PHOTO= "photo";

    public static final String REQUEST_PARMETER_DATA = "data";
    public static final String COMMAND_FETCH_CHANGE_REQUEST = "changerequest/query_detail";
    public static final String COMMAND_CONFIG_QUERY = "configuration/query";
    public static final String COMMAND_QUERY_CONTACT_REAL_NAME = "contact/real_name";
    public static final String COMMAND_QUERY_ACCOUNTREQUEST_GETGUIDBYSIM = "accountrequest/getguidbysim";
    public static final String COMMAND_QUERY_ACCOUNTREQUEST_FASTLOGIN = "accountrequest/fastlogin";
    public static final String COMMAND_QUERY_ACCOUNTREQUEST_NORMAL = "accountrequest/normallogin";
    public static final String COMMAND_QUERY_ACCOUNTREQUEST_VERIFYNOSIM = "accountrequest/verifynosim";
    public static final String COMMAND_QUERY_ACCOUNTREQUEST_GETGUIDBYCODE = "accountrequest/getguidbycode";
    public static final String COMMAND_QUERY_ACCOUNTREQUEST_MODIFYFIELD = "accountrequest/modifyfield";
    public static final String COMMAND_GET_NEW_PASSWORD = "accountrequest/getnewpwd";
    public static final String COMMAND_CHANGE_PHOTO = "accountrequest/changephoto";

    public static final String SMS_SERVICE_NUMBER = "sms_service_number";
}
