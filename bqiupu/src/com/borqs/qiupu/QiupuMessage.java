package com.borqs.qiupu;

public class QiupuMessage {
	
	public static final String BUNDLE_APK_PACKAGENAME = "BUNDLE_APK_PACKAGENAME";
	
	public static final String BUNDLE_ITEM_POSTION = "BUNDLE_ITEM_POSTION";
	public static final String BUNDLE_ITEM_APK_LABEL = "BUNDLE_ITEM_APK_LABEL";
	public static final String BUNDLE_APKINFO = "BUNDLE_APKINFO";
	public static final String BUNDLE_IM_RECORD = "BUNDLE_IM_RECORD";
	public static final String BUNDLE_STREAMINFO = "BUNDLE_STREAMINFO";
	public static final String BUNDLE_STREAM_IN_FILE = "BUNDLE_STREAM_IN_FILE";
//	public static final String BUNDLE_STREAM_POST_ID = "BUNDLE_STREAM_POST_ID";
	public static final String BUNDLE_APK_POST_ID = "BUNDLE_APK_POST_ID";
	public static final String BUNDLE_POST_APK           = "BUNDLE_POST_TYPE";
	public static final String BUNDLE_POST_COMMENT_COUNT = "BUNDLE_POST_COMMENT_COUNT";
	public static final String BUNDLE_POST_IS_LIKE = "BUNDLE_POST_IS_LIKE";
	public static final String BUNDLE_SHARE_CONTACT = "BUNDLE_SHARE_CONTACT";
	public static final String BUNDLE_SHARE_CONTACT_INFO = "serializable_contact_info";
	
	public static final String BUNDLE_DOWNLOAD_FOR_APK_UPDATE = "BUNDLE_DOWNLOAD_FOR_APK_UPDATE";
	
	public static final int MESSAGE_SYNC_USER_APK = 100;
//	public static final int MESSAGE_SYNC_USER_APK_OK = 101;
//	public static final int MESSAGE_SYNC_USER_APK_FAILED = 102;
//	public static final int MESSAGE_SYNC_USER_APK_END = 103;
//    public static final int MESSAGE_SYNC_USER_APK_BEGIN = 104;
	
	public static final int MESSAGE_DOWNLOAD_APK = 200;
	public static final int MESSAGE_DOWNLOAD_APK_LOADING = 201;
	public static final int MESSAGE_DOWNLOAD_APK_OK = 202;
	public static final int MESSAGE_DOWNLOAD_APK_FAILED = 203;
	public static final int MESSAGE_DOWNLOAD_MADE_CONNECTION = 204;
	public static final int MESSAGE_START_DOWNLOAD = 205;
	
	public static final int MESSAGE_UPLOAD_APK = 300;
	public static final int MESSAGE_UPLOAD_APK_OK = 301;
	public static final int MESSAGE_UPLOAD_APK_LOADING = 302;
	public static final int MESSAGE_UPLOAD_APK_FAILED = 303;
	
//	public static final int MESSAGE_BACKUP_APK = 400;
//	public static final int MESSAGE_BACKUP_APK_OK = 401;
//	public static final int MESSAGE_BACKUP_APK_FAILED = 402;
//	public static final int MESSAGE_BACKUP_APK_LOADING = 403;
	
	public static final int MESSAGE_UPDATE_APK = 404;
	public static final int MESSAGE_UPDATE_APK_END = 405;
	
	public static final int MESSAGE_LOAD_USER_APK = 500;
	public static final int MESSAGE_LOAD_USER_APK_OK = 501;
	public static final int MESSAGE_LOAD_USER_APK_FAILED = 502;
	public static final int MESSAGE_LOAD_USER_APK_END = 503;
	
	public static final int MESSAGE_ADD_FRIEND = 600;
	public static final int MESSAGE_ADD_FRIEND_END = 601;
	
	public static final int MESSAGE_DELETE_FRIEND = 700;
	public static final int MESSAGE_DELETE_FRIEND_END = 701;

	public static final int MESSAGE_RM_APK_FILE = 800;

	public static final int MESSAGE_APK_INSTALL = 900;
	public static final int MESSAGE_APK_UNINSTALL = 901;
	public static final int MESSAGE_APK_CHANGED = 902;
	
	public static final int MESSAGE_LOAD_DATA = 1000;
	public static final int MESSAGE_LOAD_DATA_END = 1001;
	public static final String BUNDLE_LOAD_DATA_FAILD = "BUNDLE_LOAD_DATA_FAILD";

	public static final int MESSAGE_SET_SHARESCOPE = 1100;
	
	public static final int MESSAGE_CHECK_UPDATE_QIUPU = 1200;
	public static final int MESSAGE_CHECK_UPDATE_QIUPU_END = 1201;

	public static final int MESSAGE_REFRESH_UI = 1400;
	
	public static final int MESSAGE_ADD_FAVORITES = 1500;
	public static final int MESSAGE_ADD_FAVORITES_END = 1501;
	public static final int MESSAGE_REMOVE_FAVORITES = 1502;
	public static final int MESSAGE_REMOVE_FAVORITES_END = 1503;
	
	public static final int MESSAGE_RETWWET_END = 1600;
	public static final int MESSAGE_LIKE_ADD_END = 1601;
	public static final int MESSAGE_LIKE_REMOVE_END = 1602;
	
	public static final int MESSAGE_DELETE_APK_END = 1700;
	
	public static final int MESSAGE_SEND_MAIL = 1800;
	public static final int MESSAGE_SEND_MAIL_END = 1801;

	public static final int MESSAGE_SET_TOP_END = 1900;
	
	public static final String BUNDLE_PROGRESS = "BUNDLE_PROGRESS";
	public static final String BUNDLE_APK_DOWNLOAD_STORE_PATH = "BUNDLE_APK_DOWNLOAD_STORE_PATH";
}
