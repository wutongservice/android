package com.borqs.notification;

import android.os.Environment;


class Constants {
	static boolean LOG_ENABLED = true;

	// For debug
	static final String DEFAULT_PWD = "aBcPluS123";
    //static final String DEFAULT_SERVER = "192.168.5.187";
    static String DEFAULT_SERVER = "push.borqs.com";
    static String DEFAULT_FILE_SERVER = "push.borqs.com";
    static String DEFAULT_API_SERVER = "http://api.borqs.com";

	static boolean LOGD_ENABLED = true && LOG_ENABLED;
	static boolean LOGV_ENABLED = true && LOG_ENABLED;

	static boolean mWiFiOnly = true;

	public static final String KEY_USER_NAME = "user";
	public static final String KEY_PASSWORD = "password";
    public static final String KEY_SERVER = "server";
    public static final String KEY_REGISTERED = "registered";

    public static final boolean BORQS_ACCOUNT_ENABLED = true;
	// Notification
    public static final String BORQS_NOTIFICATION_INTENT = "com.borqs.notification.notify";
    public static final String BORQS_FILE_NOTIFICATION_INTENT = "com.borqs.notification.file";
    public static final String MSG_PUSH_CLIENT_RESPONSE = "notification.borqs.com";
    public static final String MSG_PUSH_CLIENT_RESPONSE_ACTION = "clientresponse";

    //Connection
    // boot complete, launch service, try to connect xmpp server, create account, then disconnect
    public static final int SERVICE_CMD_BOOTCOMPLETE = 0;
    // wifi connected, launch service, try to connect xmpp server and keep connection
    public static final int SERVICE_CMD_WIFICONNECTED = 1;
    // wifi disconnected, launch service, try to disconnect from xmpp server
    public static final int SERVICE_CMD_WIFIDISCONNECTED = 2;



    // App IDs:
    public static final int APP_ID_SELF = 999;
    public static final int APP_ID_SYNC = 11;
    public static final int APP_ID_OPENFACE = 12;
    public static final int APP_ID_XMESSAGE = 13;
    public static final int APP_ID_BROWSER = 14;
    public static final int APP_ID_DIALER = 15;
    public static final int APP_ID_WUTONG = 16;

    // Internal message types
    public static final int MSG_TYPE_KEEP_WORKING = 10;
    public static final int MSG_TYPE_NEW_FILE = 11;
    public static final int MSG_TYPE_DEFAULT = 99;
    

    //

}
