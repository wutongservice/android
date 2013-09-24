package com.borqs.information.util;

import twitter4j.conf.ConfigurationBase;
import android.content.Context;
import android.text.TextUtils;

import com.borqs.common.util.DataConnectionUtils;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;

public class InformationConstant {
	public static final String USERAGENT = "borqs/notification/1.0";
	
	//<notification_uri>http://apptest0.borqs.com/bmb</notification_uri>
	private static final String NOTIFICATION_KEY = "notification_uri";
		
	public static String getBorqsURL() {
		QiupuORM orm = QiupuHelper.getORM();
		if (orm == null) {
			return ConfigurationBase.DEFAULT_BORQS_URL;
		}

		// Should this always query db be cached for performance.
		final String apiUrl = orm.getCurrentApiUrl();
		if (!TextUtils.isEmpty(apiUrl)) {
			return apiUrl;
		} else {
            if(orm.isUsingTestURL())
            {
                return ConfigurationBase.DEFAULT_BORQS_URL_TEST;
            }
            else
            {
                return ConfigurationBase.DEFAULT_BORQS_URL;
            }
        }
	}

	// TODO I will delete this method later, It drives me crazy now.
	private static String getBorqsHost() {
		try {
			// http://api.borqs.com/ -> api.borqs.com
			String host = getBorqsURL();
			if (host.startsWith("http://")) {
				host = host.substring(7);
			}
			if (host.endsWith("/")) {
				host = host.substring(0, host.length() - 1);
			}
			return host.trim();
		} catch (Exception e) {
			return ConfigurationBase.DEFAULT_BORQS_HOST;
		}
	}
	
	public static String getBorqsHost2(Context context) {
		String host = DataConnectionUtils.getCurrentServerUrl(context, NOTIFICATION_KEY);
		if(TextUtils.isEmpty(host)) {
			return getBorqsHost();
		} else {
			if(host.startsWith("http://")) {
				host = host.substring(7);
				int index = host.indexOf('/');
				if(index == -1) {
					return host;
				} else {
					host = host.substring(0, index);
					return host;
				}
			}
		}
		return getBorqsHost();
	}
	
	public static final String HTTP_SCHEME = "http";
	public static final String NOTIFICATION_HOST_SEND_PATH = "bmb/service/informations/send.json";
	public static final String NOTIFICATION_HOST_LIST_BY_ID_PATH = "bmb/service/informations/listbyid.json";
	public static final String NOTIFICATION_HOST_LIST_BY_TIME = "bmb/service/informations/listbytime.json";
	public static final String NOTIFICATION_HOST_LIST_PATH = "bmb/service/informations/list.json";
	public static final String NOTIFICATION_HOST_COUNT_PATH = "bmb/service/informations/count.json";
	public static final String NOTIFICATION_HOST_TOP_PATH = "bmb/service/informations/top.json";
	public static final String NOTIFICATION_HOST_DONE_PATH = "bmb/service/informations/done.json";
	public static final String NOTIFICATION_HOST_READ_PATH = "bmb/service/informations/read.json";
//	public static final String NOTIFICATION_HOST_UNREADS_PATH = "bmb/service/informations/unreads.json";
	public static final String NOTIFICATION_HOST_VIEW_PATH_TO_FORMAT = "bmb/service/informations/view/{0}.json";
	public static final String NOTIFICATION_HOST_UPDATE_PATH_TO_FORMAT = "bmb/service/informations/update/{0}.json";
	public static final String NOTIFICATION_HOST_REMOVE_PATH_TO_FORMAT = "bmb/service/informations/remove/{0}.json";
	public static final String NOTIFICATION_REQUEST_PARAM_TICKET = "ticket";
	public static final String NOTIFICATION_REQUEST_PARAM_STATUS = "status";
	public static final String NOTIFICATION_REQUEST_PARAM_UID = "uid";
	public static final String NOTIFICATION_REQUEST_PARAM_MID = "mid";
	public static final String NOTIFICATION_REQUEST_PARAM_FROM = "from";
	public static final String NOTIFICATION_REQUEST_PARAM_SIZE = "size";
	public static final String NOTIFICATION_REQUEST_PARAM_COUNT = "count";
	public static final String NOTIFICATION_REQUEST_PARAM_TOPN = "topn";
	
	public static final String NOTIFICATION_REQUEST_PARAM_TYPE = "type";
	public static final String NOTIFICATION_REQUEST_PARAM_READ = "read";
	public static final String NOTIFICATION_REQUEST_PARAM_APPID = "appId";
	
	public static final String NOTIFICATION_REQUEST_PARAM_SCENEID = "scene";
	
	public static final String NOTIFICATION_DOWNLOAD_SERVICE_ACTION = "com.borqs.notification.notify";
	public static final String NOTIFICATION_DOWNLOAD_SERVICE_STATUS_ACTION = "com.borqs.notification.service.download";
	
	public static final String NOTIFICATION_DOWNLOAD_STATUS = "status";
	public static final int NOTIFICATION_DOWNLOAD_SERVICE_FINISHED = 0;
	public static final int NOTIFICATION_DOWNLOAD_SERVICE_START = 1;
	public static final int NOTIFICATION_DOWNLOAD_SERVICE_FAILED = 2;
	
	public static final String NOTIFICATION_DOWNLOADED_COUNT = "count";
	
	public static final String NOTIFICATION_DOWNLOAD_MODE = "mode";
	public static final int NOTIFICATION_DOWNLOAD_MODE_ATUO = 1;
	public static final int NOTIFICATION_DOWNLOAD_MODE_MANUAL = 2;
	
	public static final String NOTIFICATION_PREF = "information";
	public static final String NOTIFICATION_PREF_NEW_NOTIFICATION_COUNT = "new_notification_count";
	public static final String NOTIFICATION_PREF_LAST_CHECK_TIME = "last_check_time";
	
	public static final int DEFAULT_NOTIFICATION_COUNT = 20;
	public static final int DEFAULT_EARLYNOTIFICATION_COUNT = -20;
	
	public static final String NOTIFICATION_INTENT_PARAM_ISTOME = "isToMe";
//	public static final String NOTIFICATION_INTENT_PARAM_ISFromActivity = "isFromActivity";
	public static final String NOTIFICATION_INTENT_SYNC_TYPE = "sync_type";
	
	public static final int NOTIFICATION_INTENT_PARAM_READ = 1;
	public static final int NOTIFICATION_INTENT_PARAM_UNREAD = 0;
	
	public static final int NOTIFICATION_INTENT_PARAM_TOME = 0;
	public static final int NOTIFICATION_INTENT_PARAM_NO_TOME = 1;
	
	public static final int NOTIFICATION_DB_MAX_SIZE = 50;
}
