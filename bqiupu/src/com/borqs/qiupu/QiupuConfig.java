package com.borqs.qiupu;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import static android.os.Environment.getExternalStorageDirectory;


public class QiupuConfig {
    public static final boolean LOGD = true;
    public static final boolean DBLOGD = false;
    public static final boolean LowPerformance = false;
    
    // APP_ID is defined by Server, need to change if server change, for Bpc, it is 9,
    // 1 is App, 2 is Book, 3 is music.
    public static final int APP_ID_QIUPU = 1;
    public static final String APP_SECRECT_QIUPU = "appSecret1";

    public static final long A_SECOND = 1000L;
    public static final long A_MINUTE = 60 * A_SECOND;
    public static final long AN_HOUR = 3600 * A_SECOND;
    public static final long A_DAY = 86400 * A_SECOND; // 24 * 60 * 60 = 86400;
 
    public static final long SYNC_USERAPK_PERIOD = 3*A_DAY;
    
    
    public static final long SYNC_USERINFO_PERIOD = AN_HOUR;	// 1 * 60 * 60* 1000L;
    
    public static final String MARKET_SEARCH_HOST = "market.android.com/search";
    public static final String HOST_CONFIG = "host_config.xml";
    

    public static final String APP_PACKAGE_NAME = "com.borqs.qiupu";
    public static final String APP_PHONE_PATH = "/data/data/" + APP_PACKAGE_NAME + "/";
    public static final String APP_ICON_PHONE_PATH = "/data/data/" + APP_PACKAGE_NAME + "/files/";
    public static final String APP_CACHE_PHONE_PATH =  "/data/data/" + APP_PACKAGE_NAME + "/files/cache/";

    private static final String SDCARD_ROOT = getExternalStorageDirectory().getPath();
    private static final String BORQS_SDCARD_PATH = SDCARD_ROOT + "/borqs";
    private static final String BORQS_APPS_SDCARD_PATH = BORQS_SDCARD_PATH + "/apps";
    private static final String QIUPU_SDCARD_PATH = BORQS_APPS_SDCARD_PATH + "/qiupu/";

    public static final String APP_GC_SDCARD_PATH = QIUPU_SDCARD_PATH + ".gc/";
    public static final String APP_ICON_SDCARD_PATH = QIUPU_SDCARD_PATH + ".icons/";
    public static final String APP_CACHE_SDCARD_PATH =  QIUPU_SDCARD_PATH + "cache/";
    public static final String APP_APK_SDCARD_PATH =  QIUPU_SDCARD_PATH + "download/";
    public static final String APP_MEDIA_SDCARD_PATH = QIUPU_SDCARD_PATH + "media/";
    
    public static final String PHOTO_MEDIA_SDCARD_PATH = BORQS_SDCARD_PATH + "/image/";

    public static final String APP_APK_PHONE_PATH =  "/download/";
    
    /** used for imagerun set default image */
    public final static int DEFAULT_IMAGE_INDEX_APK = 0;
    
    public final static int DEFAULT_IMAGE_INDEX_USER = 1;
    
    public final static int DEFAULT_IMAGE_INDEX_SCREENSHOT = 2;
    public final static int DEFAULT_IMAGE_INDEX_BOOK       = 3;
    public final static int DEFAULT_IMAGE_INDEX_Music      = 4;
    public final static int DEFAULT_IMAGE_INDEX_LINK = 5;
    /** default icon index with random strategy */
    public final static int DEFAUTL_RANDOM_LINK_INT_GREEN = 6;
    public final static int DEFAUTL_RANDOM_LINK_INT_BLUE = 7;
    public final static int DEFAUTL_RANDOM_LINK_INT_RED = 8;
    public final static int DEFAUTL_RANDOM_LINK_INT_YELLOW = 9;

//	private static final String URL_SHARE_PREFIX = ConfigurationBase.getAPIURL()+ "search?q=";
//	public static final String URL_QIUPU_URL    = ConfigurationBase.getAPIURL()+ "search?q=com.borqs.qiupu";
//	public static final String URL_SHARE_FULL_STR = URL_QIUPU_URL;
	
	public static final String[] mailSuf = new String[]{"gmail.com","borqs.com","sina.com","sohu.com",
			"139mail.com","yahoo.com.cn","yahoo.com","msn.com","mac.com","qq.com","163.com","126.com"};
	
	public static final HashMap<String, String> apkLevelMap = new HashMap<String, String>();
		
	static {
		apkLevelMap.put("3.0", "11");
		apkLevelMap.put("2.3.3", "10");
		apkLevelMap.put("2.3", "9");
		apkLevelMap.put("2.2.1", "8");
		apkLevelMap.put("2.2", "8");
		apkLevelMap.put("2.1-update1", "7");
		apkLevelMap.put("1.6", "4");
		apkLevelMap.put("1.5", "3");
	}
    
	public final static int APK_VISIBILITY_EVERYONE = 1;
	
	public final static int APK_VISIBILITY_FRIENDS = 2;
	
	public final static int APK_VISIBILITY_NONE = 3;
	
	public final static int PHONEBOOK_PRIVACY_EVERYONE = 1;
	public final static int PHONEBOOK_PRIVACY_NONE = 2;
	public final static int PHONEBOOK_PRIVACY_CIRCLE = 3;
	
	public static final String RESOURCE_PHONEBOOK = "phonebook";
	
	public static final int RELATIONSHIP_NULL      = 0;
	public static final int RELATIONSHIP_FRIEND    = 1;
	public static final int RELATIONSHIP_BLACK     = 2;
	public static final int RELATIONSHIP_FOLLOWER  = 3;
	public static final int RELATIONSHIP_EACH_FRIENDS = 4;

	public static final String MESSAGE_SOURCE_QIUPU = "QiupuApplication";
	
	public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat TIME_FORMATTER_NO_YEAR = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public static final int POSTS_SERIALIZE_ITEM_COUNT = 10;
	public static final int POSTS_SERIALIZE_APK_COUNT = 10;
	
//	public static final int USER_INDEX_FRIENDS = 0;
//	public static final int USER_INDEX_FOLLOWERS = 1;
	public static final int USER_INDEX_YOU_MAY_KNOW = 2;
	public static final long QIUPU_USER_ID = 102;
	public static final String APK_SUFFIX_STRING = ".apk";
	
	public static final int INVITE_TYPE_EMAIL = 1;
	public static final int INVITE_TYPE_MESSAGE = 2;
	
    public static final int USER_INDEX_FRIENDS = 0;
	public static final int USER_INDEX_CIRCLES = 1;
	public static final int USER_INDEX_FOLLOWERS = 2;

	public static final int CATEGORY_APP_POOL = 1;
	public static final int CATEGORY_APP_RECOMMEND = 2;
	public static final int CATEGORY_APP_LATESTED = 3;
	
	public static final long APP_CATEGORY_SEARCH = 0;
	
	public static final String DEFAULT_CIRCLE_ID = "6";
	public static final String DEFAULT_CIRCLE_NAME = "Default";
	public static final String RESON_INSTALL = "installing";
	public static final String RESON_ALL = "";
	public static final String RESON_FAVORITE = "favorite";
	
    public static final String SDCARD_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/download/";
    public static final String PKG_DOWNLOAD_NAME_SUFFIX = ".dl";
//	public static final String PKG_QIUPU_DOWNLOAD_NAME = "com.borqs.qiupu.apk.dl";

	public static final float DEFAULT_RATING = 2.0f;
	
	public static final String SORT_HOT = "download";
	public static final String SORT_LATEST = "updated";
	
	public static final String PREFECTUR_INTERFACE = "prefectur_Interface";


// Object type
    public static final int USER_OBJECT = 1;
    public static final int POST_OBJECT = 2;
    public static final int VIDEO_OBJECT = 3;
    public static final int APK_OBJECT = 4;
    public static final int MUSIC_OBJECT = 5;
    public static final int BOOK_OBJECT = 6;
    public static final int COMMENT_OBJECT = 7;
    public static final int LIKE_OBJECT = 8;
    public static final int LINK_OBJECT = 9;
    public static final int PHOTO_OBJECT = 10;
    public static final int FILE_OBJECT = 11;
    public static final int POLL_OBJECT = 12;
    public static final int PUBLIC_CIRCLE_OBJECT = 13;
    public static final int EVENT_OBJECT = 14;
    public static final int COMPANY_OBJECT = 15;
    public static final int PAGE_OBJECT = 16;
	public static final String TYPE_STREAM = "2";
	public static final String TYPE_APK    = "4";
	public static final String TYPE_COMMENT = "7";
    public static final String TYPE_PHOTO = "10";
    public static final String TYPE_POLL = "12";

	public static final int PUBLIC_CIRCLE = 0;          // virtual
	public static final int FRIENDS_CIRCLE = 1;         // virtual, finite
	public static final int STRANGER_CIRCLE = 2;        // virtual
	public static final int FOLLOWERS_CIRCLE = 3;       // virtual
	public static final int BLOCKED_CIRCLE = 4;         // actual,  finite
	public static final int ADDRESS_BOOK_CIRCLE = 5;    // actual, finite
	public static final int DEFAULT_CIRCLE = 6;         // actual, finite
	public static final int ME_CIRCLE = 7;              // virtual, finite
	public static final int FAMILY_CIRCLE = 9;                 // actual, finite
	public static final int CLOSE_FRIENDS_CIRCLE = 10;  // actual, finite
	public static final int ACQUAINTANCE_CIRCLE = 11;  // actual, finite

	public static final String TYPE_PHONE1    = "mobile_telephone_number";
	public static final String TYPE_PHONE2    = "mobile_2_telephone_number";
	public static final String TYPE_PHONE3    = "mobile_3_telephone_number";
	public static final String TYPE_EMAIL1    = "email_address";
	public static final String TYPE_EMAIL2    = "email_2_address";
	public static final String TYPE_EMAIL3    = "email_3_address";
	
	public static final int TYPE_PHONE = 1;
	public static final int TYPE_EMAIL = 2;

	
//	public static final int TEXT_POST = 1;
//    public static final int IMAGE_POST = 1 << 1;
//    public static final int VIDEO_POST = 1 << 2;
//    public static final int AUDIO_POST = 1 << 3;
//    public static final int BOOK_POST = 1 << 4;
//    public static final int APK_POST = 1 << 5;
//    public static final int LINK_POST = 1 << 6;
//    public static final int APK_COMMENT_POST = 1 << 8;
//    public static final int APK_LIKE_POST = 1 << 9;
//
//    public static final int ALL_POST = TEXT_POST | APK_POST | LINK_POST | APK_COMMENT_POST | APK_LIKE_POST;
    
    public static final long USER_ID_ALL = -1;
//    public static final long USER_ID_PUBLIC = -2;

    public static final int CIRCLE_ID_ALL = -1;
    public static final int CIRCLE_ID_PUBLIC = -2;
    public static final int CIRCLE_ID_HOT = -3;
    public static final int CIRCLE_ID_NEAR_BY = -4;
    
    public static final int CIRCLE_ID_PRIVACY = -100;
    
    public static final int FROM_HOME = 1;

    public static final String CIRCLE_NAME_PUBLIC = "#-2";
    public static final String CIRCLE_NAME_HOT = "#-3";
    public static final String CIRCLE_NAME_NEAR_BY = "#-4";

    // TODO: may move to setting in future.
    public static final boolean FORCE_BORQS_ACCOUNT_SERVICE_USED = true;
    public static final boolean AUTO_DOWNLOAD_NECESSARY_APPS = true;
    
    public static final boolean IS_USER_CONTACT_EDIT = false;
    public static final boolean IS_SHOW_ALBUM = true;
    public static boolean IS_USE_BAIDU_LOCATION_API = true;
    public static boolean IS_USE_MIXED_LBS = true;

	public static String getTmpCachePath()
	{
        String cachePath = "";
		 if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
             cachePath = QiupuConfig.APP_CACHE_SDCARD_PATH;
	     } else {
             cachePath = QiupuConfig.APP_CACHE_PHONE_PATH;
		 }

		 if(new File(cachePath).exists() == false)
		 {
		      new File(cachePath).mkdirs();
		 }
		 return cachePath;
	}

    public static String getSdcardPath () {
        return QiupuConfig.SDCARD_ROOT + "/";
    }
    public static String getSdcardPath(final String dir) {
        return getSdcardPath() + dir;
    }
    
    public static boolean isEnoughSpace(final String path, long reservedSize) {
        if (TextUtils.isEmpty(path)) {
            StatFs tmpstat = new StatFs(QiupuConfig.SDCARD_ROOT);
            if(tmpstat != null)
            {
                int blockSize = tmpstat.getBlockSize();
                int availableBlocks = tmpstat.getAvailableBlocks();
                if (blockSize * ((long) availableBlocks - 4) <= reservedSize)
                {
                    return false;
                }
            }
        } else {

        }

        return true;
    }
    
    public static final long PUBLIC_CIRCLE_MIN_ID = 10000000000L;
    public static final long PUBLIC_CIRCLE_MAX_ID = 11000000000L;
    public static final long ACTIVITY_MAX_ID = 12000000000L;
    public static final long EVENT_MIN_ID = 14000000000L;
    public static final long EVENT_MAX_ID = 15000000000L;
    public static final long LONG_USER_ID = 1000000000000000000L;
    public static final long PAGE_ID_BEGIN = 20000000001L;
    public static final long PAGE_ID_END = 21000000000L;
    public static final boolean isPublicCircleProfile(long id) {
        if(id >= PUBLIC_CIRCLE_MIN_ID && id < PUBLIC_CIRCLE_MAX_ID) {
            return true;
        }
        return false;
    }
    public static final boolean isActivityId(long id) {
        if(id >= PUBLIC_CIRCLE_MAX_ID && id < ACTIVITY_MAX_ID) {
            return true;
        }
        return false;
    }

    public static final boolean isEventIds(long id) {
        if(id >= EVENT_MIN_ID && id < EVENT_MAX_ID) {
            return true;
        }
        return false;
    }
    
    public static final boolean isEventIds(String id) {
    	return isEventIds(Long.parseLong(id));
    }
    
    public static final boolean isPageId(long id) {
    	if(id >= PAGE_ID_BEGIN && id < PAGE_ID_END) {
            return true;
        }
        return false;
    }
    
    
    /// helper methods, see to:
    /// http://developer.android.com/intl/zh-CN/reference/android/os/StrictMode.html
    /// disable the Strict mode by set both option to false.
    private static boolean DEVELOPER_MODE = false;
    private static boolean AGGRESSIVE_MODE = false;
    public static void enableStrictMode() {
        if (DEVELOPER_MODE || AGGRESSIVE_MODE) {
            if (DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder();
                threadPolicyBuilder.penaltyLog();
                if (AGGRESSIVE_MODE) {
                    threadPolicyBuilder.detectAll();
                } else if (DEVELOPER_MODE) {
                    threadPolicyBuilder.detectDiskReads();
                    threadPolicyBuilder.detectDiskWrites();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        threadPolicyBuilder.penaltyFlashScreen();
                    }
                }
                threadPolicyBuilder.detectNetwork();
                StrictMode.setThreadPolicy(threadPolicyBuilder.build());

                StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder();
                vmPolicyBuilder.penaltyLog();
                if (AGGRESSIVE_MODE) {
                    vmPolicyBuilder.detectAll();
                    vmPolicyBuilder.penaltyDeath();
                }
                vmPolicyBuilder.detectLeakedSqlLiteObjects();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    vmPolicyBuilder.detectLeakedClosableObjects();

                }
                StrictMode.setVmPolicy(vmPolicyBuilder.build());
            }
        }
    }

    public static void setDefaultLocationApi(boolean isBaidu) {
        IS_USE_BAIDU_LOCATION_API = isBaidu;
    }

    public static final String GOOGLE_MAP_APPKEY = "AIzaSyAabECASqEEIhPIZt4m5aO-9lnXEsgm4vk";
    public static final String BAIDU_MAP_APPKEY  = "30dd1731c7734dba3b06b0209c662bba";

    public static final boolean isShowPage = false;
}
