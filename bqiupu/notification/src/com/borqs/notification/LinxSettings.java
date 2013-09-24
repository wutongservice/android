package com.borqs.notification;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


public class LinxSettings {

    /*
     * Some constants.
     */
    public static final String DEFAULT_DOWNLOAD_PATH = 
            Environment.getExternalStorageDirectory() +  "/download/"; 
    public static  String BORQS_SEND_FILE_URL = "http://push.borqs.com/upload_file.php";
    public static  String BORQS_GET_FILE_URL = "http://push.borqs.com/cache/";
    public static  String BORQS_DELETE_FILE_URL = "http://push.borqs.com:9090/plugins/xDevice/fileinfor?addORdelete=delete&time=";

    public static final String QIUPU_SETTINGS_URI = "content://com.borqs.qiupu/settings";
    private static String LINX_SERVER_KEY = "xdevice_host";
    private static String API_SERVER_KEY = "api_host";

    private static LinxSettings mInstance;

    private Context mContext;
    private static String TAG = "LinxSettings";
    private String mLinxServer;
    private String mApiServer;

    public static synchronized LinxSettings create(Context context) {
        if (mInstance == null) {
            mInstance = new LinxSettings(context);
        }
        return mInstance;
    }

    public static synchronized LinxSettings getInstance() {
        return mInstance;
    }

    private LinxSettings(Context context) {
        mContext = context;
        init();
    }

    public String getLinxServer() {
        return mLinxServer;
    }

    public String getApiServer() {
        return mApiServer;
    }

    private void init() {
        mLinxServer = getServerUrl(LINX_SERVER_KEY);
        mApiServer = getServerUrl(API_SERVER_KEY);
        if (Constants.LOGV_ENABLED) {
        	Log.d(TAG, "Linx Server : " + mLinxServer);
        	Log.d(TAG, "API server : " + mApiServer);
        }
        if(!TextUtils.isEmpty(mLinxServer)){
            Constants.DEFAULT_SERVER = mLinxServer;
            Constants.DEFAULT_FILE_SERVER = mLinxServer;
            BORQS_SEND_FILE_URL = "http://" + mLinxServer + "/upload_file.php";
            BORQS_GET_FILE_URL = "http://" + mLinxServer + "/cache/";
            BORQS_DELETE_FILE_URL = "http://"+ mLinxServer + ":9090/plugins/xDevice/fileinfor?addORdelete=delete&time=";
        }
        if(TextUtils.isEmpty(mApiServer)){
            mApiServer = Constants.DEFAULT_API_SERVER;
        }
        
        if(TextUtils.isEmpty(mLinxServer)) {
        	mLinxServer = Constants.DEFAULT_SERVER;
        }
        
        Constants.DEFAULT_FILE_SERVER = mLinxServer;
        BORQS_SEND_FILE_URL = "http://" + mLinxServer + "/upload_file.php";
        BORQS_GET_FILE_URL = "http://" + mLinxServer + "/cache/";
        BORQS_DELETE_FILE_URL = "http://"+ mLinxServer + ":9090/plugins/xDevice/fileinfor?addORdelete=delete&time=";
        
        if (Constants.LOGV_ENABLED) {
            Log.d(TAG, "url is:" + Constants.DEFAULT_SERVER );
            Log.d(TAG, "send file url : " + BORQS_SEND_FILE_URL);
            Log.d(TAG, "get file url : " + BORQS_GET_FILE_URL);
            Log.d(TAG, "delete file url : " + BORQS_DELETE_FILE_URL);
        }
    }

    /*
     * Read the server URL from Qiupu.
     */
    private String getServerUrl(String appKey){
        String urlHost = "";
        final Uri SETTINGS_CONTENT_URI = Uri.parse(QIUPU_SETTINGS_URI);
        final String[] projects =  new String[] {"_id", "name", "value", };
        final String where = "name='" + appKey + "'";
        Cursor cursor = mContext.getContentResolver().query(SETTINGS_CONTENT_URI, projects, where, null, null);
    
        if (null != cursor ){
            if(cursor.getCount() > 0) {            
                cursor.moveToFirst();
                urlHost = cursor.getString(cursor.getColumnIndex("value"));
                if (Constants.LOGV_ENABLED) {
                	Log.d(TAG, "urlHost : " + urlHost);
                }
            } else {
                Log.w(TAG, "getCurrentServerUrl, fail to query cursor.");
            }
            cursor.close();
        }else{
            Log.w(TAG,"getCurrentServerUrl,cursor is null");
        }
//        if(TextUtils.isEmpty(urlHost)){
//            urlHost = Constants.DEFAULT_SERVER;
//        }
        
       return urlHost;
    }
}