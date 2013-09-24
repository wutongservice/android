package com.borqs.qiupu.util;

public class ServiceConstance {
	public final static String CHECKACCOUNT = "com.android.borqsaccount.service.CHECKACCOUNT";
	public final static String USER_LOGIN = "com.android.borqsaccount.service.USERLOGIN";
	public final static String USER_LOGOUT = "com.android.borqsaccount.service.USERLOGOUT";
	public final static String SYNC_APK = "com.android.borqsaccount.service.SYNCAPK";
	
	public final static int SYNC_SERVICE_STATUS_IDLE = 0;
	public final static int GET_APK_FROM_SERVER      = 1;
	public final static int PREPARE_SYNCING_DATA     = 2;
	public final static int START_UPLOAD_DOWNLOAD_DATA = 3;
	
	public final static int INIT_VERSION_STATUS = 0;
	public final static int NEW_VERSION_STATUS  = 1;
	public final static int IGNOR_VERSION_STATUS = 2;
}
