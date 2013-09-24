package com.borqs.qiupu.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.ApkResponse;
import twitter4j.ChatInfo;
import twitter4j.Company;
import twitter4j.Education;
import twitter4j.Employee;
import twitter4j.EventTheme;
import twitter4j.InfoCategory;
import twitter4j.NotificationInfo;
import twitter4j.PageInfo;
import twitter4j.PollInfo;
import twitter4j.PublicCircleRequestUser;
import twitter4j.QiupuAccountInfo;
import twitter4j.QiupuAccountInfo.PhoneEmailInfo;
import twitter4j.QiupuAlbum;
import twitter4j.QiupuPhoto;
import twitter4j.QiupuSimpleUser;
import twitter4j.QiupuUser;
import twitter4j.QiupuUser.PerhapsName;
import twitter4j.Requests;
import twitter4j.SharedPhotos;
import twitter4j.UserCircle;
import twitter4j.UserCircle.Group;
import twitter4j.UserImage;
import twitter4j.WorkExperience;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.PeopleLookupHelper;
import com.borqs.app.Env;
import com.borqs.common.ShareSourceItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.Utilities;
import com.borqs.information.db.NotificationOperator;
import com.borqs.qiupu.QiupuApplication;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.db.HanziToPinyin.Token;
import com.borqs.qiupu.util.CalendarMappingUtils;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;


public class QiupuORM {
	private final static String TAG = "QiupuORM";
	private static QiupuORM _instance;
	private Context mContext;
    private static UserPhotoUrlCache mPhotoUrls;

    private static final boolean DEBUG = QiupuConfig.DBLOGD;
    private static final boolean LowPerformance = false || QiupuConfig.LowPerformance;

	public static final String LAST_SYNC_USERAPKINFO_TIME  = "last_sync_userapkinfo_time";
	public static final String LAST_SYNC_USERINFO_TIME  = "last_sync_userinfo_time";
	public static final String LAST_SYNC_REQUESTS_TIME  = "last_sync_request_time";
	public static final String LAST_SYNC_FRIENDS_APK_INFO="last_sync_friends_apk_info";
	public static final String LAST_SYNC_EVENTS_TIME = "LAST_SYNC_EVENTS_TIME";
	public static final String LAST_SYNC_CIRCLE_TIME = "LAST_SYNC_CIRCLE_TIME";
	
	public static final String HOME_ACTIVITY_ID = "HOME_ACTIVITY_ID";
//	public static final String LAST_GET_STREAM="LAST_GET_STREAM";
	public static final String LAST_GET_FRIENDS="LAST_GET_FRIENDS";
	public static final String APK_PERMISSION="APK_PERMISSION";
	public static final String INTERVAL_GET_REQUESTS="INTERVAL_GET_REQUESTS";
//	public static final String LAST_GET_ADDRESS="LAST_GET_ADDRESS";
	public static final String SHOW_PIC="SHOW_PIC";
	public static final String SHOW_MAIN_UI="SHOW_MAIN_UI";
	public static final String SELECT_BORQS_TEST_URL="SELECT_BORQS_TEST_URL";//TODO just to test
	public static final String SHOW_HTTP_LOG="SHOW_HTTP_LOG";//TODO just to test
	public static final String SHOW_LKE_MARKET="SHOW_LKE_MARKET";
	public static final String AUTO_INSTALL_DOWNLOAD="AUTO_INSTALL_DOWNLOAD";
	public static final String SHOW_MARKET_APP_PAGE="SHOW_MARKET_APP_PAGE";
	public static final String AUTO_SHARE_DYNAMIC="AUTO_SHARE_DYNAMIC";
//	public static final String PHONEBOOK_PRIVACY = "PHONEBOOK_PRIVACY";
	public static final String ENABLE_NOTIFICATION = "ENABLE_NOTIFICATION";
	public static final String ENABLE_GET_NOTIFICATION = "ENABLE_GET_NOTIFICATION";
	public static final String ENABLE_NOTIFICATION_VIBRATE = "ENABLE_NOTIFICATION_VIBRATE";
    private static final String ENABLE_PUSH_SERVICE = "ENABLE_PUSH_SERVICE";
    private static final String DATA_FLOW_AUTO_SAVE_MODE = "DATA_FLOW_AUTO_SAVE_MODE";
    private static final String IS_ALLOW_LOOKUP = "IS_ALLOW_LOOKUP";
    private static final String IS_CONFIRM_LOOKUP = "IS_CONFIRM_LOOKUP";
    private static final String SYNC_STATUS = "SYNC_STATUS";
    private static final String ENABLE_FIND_CONTACT = "ENABLE_FIND_CONTACT";

    private static final String LAST_LAST_USED_ICON_VERSION = "LAST_LAST_USED_ICON_VERSION";
    private static final String LAST_LAST_USED_EXTERNAL_ICON_VERSION = "LAST_LAST_USED_EXTERNAL_ICON_VERSION";

    private static final String AUTO_UPLOAD_APP_BACKUP="AUTO_UPLOAD_APP_BACKUP";
    private static final String DEFAULT_THEME_ID = "DEFAULT_THEME_ID";
//    private static final String DEV_FEATURE_TRY = "com.borqs.qiupu.DEV_FEATURE_TRY";
//    private static final String DEV_STREAM_TRY = "com.borqs.qiupu.DEV_STREAM_TRY";
//    private static final String PROFILE_GATEWAY_ACTION_VERIFIED = "PROFILE_GATEWAY_ACTION_VERIFIED";

//	public static final String LAST_LOGIN_USER = "last_login_user";
	
//	private static final Uri APKINFO_CONTENT_URI = Uri.parse("content://"+ApkInfoProvider.AUTHORITY+"/apkinfo");
//	public static final Uri APKINFO_GROUP_URI   = Uri.parse("content://"+ApkInfoProvider.AUTHORITY+"/apkinfo/apkgroup");
	
	public static final Uri SETTINGS_CONTENT_URI = Uri.parse("content://"+QiupuProvider.AUTHORITY+"/misc_settings");
	public static final Uri NOTIFICATION_SETTINGS_CONTENT_URI = Uri.parse("content://"+QiupuProvider.AUTHORITY+"/ntf_settings");
//    public static final Uri HOST_SETTINGS_CONTENT_URI = Uri.parse("content://"+QiupuProvider.AUTHORITY+"/settings");

	public static final Uri USERS_CONTENT_URI = Uri.parse("content://"+UserProvider.AUTHORITY+"/users");
	public static final Uri CIRCLES_CONTENT_URI = Uri.parse("content://"+UserProvider.AUTHORITY+"/circles");
	public static final Uri CIRCLE_USERS_CONTENT_URI = Uri.parse("content://"+UserProvider.AUTHORITY+"/circle_users");
	public static final Uri WORK_EXPERIENCE_CONTENT_URI = Uri.parse("content://"+UserProvider.AUTHORITY+"/work_experience");
	public static final Uri EDUCATION_CONTENT_URI = Uri.parse("content://"+UserProvider.AUTHORITY+"/education");
	
	public static final Uri SHARE_RESOURCE_URI = Uri.parse("content://"+UserProvider.AUTHORITY+"/share_resource");
	
	public static final Uri LOOKUP_CONTENT_LOOKUP_URI = Uri.parse("content://" + LookUpProvider.AUTHORITY + "/lookup");
	
	public static final Uri PHONEEMAIL_CONTENT_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/phone_email");
	public static final Uri USER_IMAGE_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/user_image");
	public static final Uri SHARED_PHOTOS_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/shared_photos");
	public static final Uri PERHAPS_NAME_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/perhaps_name");
	public static final Uri QIUPU_ALBUM_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/qiupu_album");
	public static final Uri QIUPU_PHOTO_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/qiupu_photo");
	
	public static final Uri CIRCLE_GROUP_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/groups");
	public static final Uri CHAT_RECORD_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/chat_record");
	
	private static final Uri THEMES_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/themes");
	private static final Uri POLL_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/poll");
	private static final Uri EVENT_CALENDARS_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/events_calendar");
	private static final Uri COMPANY_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/company");
	private static final Uri PAGE_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/pages");
	private static final Uri CIRCLE_EVENTS_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/circle_event");
	private static final Uri CIRCLE_POLL_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/circle_poll");

    private static final Uri EMPLOYEE_CONTENT_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/" + UserProvider.TABLE_EMPLOYEE);
    
    private static final Uri CATEGORY_CONTENT_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/" + UserProvider.TABLE_CATEGORY);
    
    private static final Uri CIRCLE_CIRCLES_CONTENT_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/" + UserProvider.CIRCLE_CIRCLES);
    
    private static final String IS_SHOW_RIGHT_MOVE_GUIDE  = "IS_SHOW_RIGHT_MOVE_GUIDE";

	private static final HandlerThread sWorkerThread = new HandlerThread("launcher-orm");
    static {
        sWorkerThread.start();
    }
	public static final Handler sWorker = new Handler(sWorkerThread.getLooper());
	
	private QiupuORM(Context context) {
		mContext = context;

        mPhotoUrls = new UserPhotoUrlCache();
	}
	
	public QiupuORM(Context context, boolean forSync) {
        mContext = context;     
    }
	
	static HanziToPinyin cjkToPinyin;
	
	public static QiupuORM getInstance(Context context) {
		if(_instance == null)
		{
			_instance = new QiupuORM(context);
			cjkToPinyin = HanziToPinyin.getInstance();
		}
		
		return _instance;
	}
	
	
//	static final String[] ACCOUNT_PROJECTION = {
//		AccountColumns.UID,
//		AccountColumns.EMAIL,
//		AccountColumns.PWD,
//		AccountColumns.NICKNAME,
//		AccountColumns.SESSION_ID,
//		AccountColumns.URLNAME
//	};
//
//    public static final class AccountColumns
//	{
//		public static final String ID = "_id";
//		public static final String UID = "uid";
//		public static final String EMAIL = "email";
//		public static final String PWD = "password";
//		public static final String NICKNAME = "nickname";
//		public static final String SESSION_ID = "session_id";
//		public static final String URLNAME = "urlname";
//	}
//
	public final static class CircleColumns{
		public final static String ID                     = "_id";
		public final static String USERID 		          = "uid";
		public final static String CIRCLE_ID              = "circleid";
		public final static String CIRCLE_NAME     		  = "name";
		public final static String MEMBER_COUNT    	      = "memberCount";
		public final static String SHOW_ON_STREAM         = "showOnStream";
		public final static String TYPE                   = "type";
		public final static String REFERRED_COUNT = "referred_count";
	}
	
	public final static class CircleCirclesColumns{
		public final static String CIRCLEID                 = "circleid";
		public final static String CIRCLE_NAME 		    = "circleid_name";
		public final static String PROFILE_IMAGE_URL        = "profile_image_url";
		public final static String PROFILE_SIMAGE_URL     	= "profile_simage_url";
		public final static String PROFILE_LIMAGE_URL    	= "profile_limage_url";
		public final static String DESCRIPTION              = "description";
		public final static String ROLE_IN_GROUP            = "role_in_group";
		public final static String FORMAL                   = "formal";
		public final static String SUBTYPE                  = "subtype";
		public final static String PARENT_ID                = "parent_id";
		public final static String REFERRED_COUNT           = "referred_count";
	}
	
	public final static class GroupColumns {
		
		public final static String CIRCLE_ID              = "circleid";
        public final static String PROFILE_IMAGE_URL      = "profile_image_url";
		public final static String PROFILE_SIMAGE_URL     = "profile_simage_url";
		public final static String PROFILE_LIMAGE_URL     = "profile_limage_url";
		public final static String COMPANY                = "company";
		public final static String OFFICE_ADDRESS         = "office_address";
		public final static String DEPARTMENT             = "department";
		public final static String JOB_TITLE              = "job_title";
		public final static String LOCATION               = "location";
		public final static String DESCRIPTION            = "description";
		
		public final static String MEMBER_LIMIT           = "member_limit";
		public final static String IS_STREAM_PUBLIC       = "is_stream_public";
		public final static String CAN_SEARCH             = "can_search";
		public final static String CAN_VIEW_MEMBERS       = "can_view_members";
		public final static String CAN_JOIN               = "can_join";
		public final static String CAN_MEMBER_INVITE      = "can_member_invite";
		public final static String CAN_MEMBER_APPROVE     = "can_member_approve";
		public final static String CAN_MEMBER_POST        = "can_member_post";
		public final static String CAN_MEMBER_QUIT        = "can_member_quit";
		public final static String NEED_INVITE_CONFIRM    = "need_invite_confirm";
		public final static String CREATED_TIME           = "created_time";
		public final static String UPDATED_TIME           = "updated_time";
		public final static String DESTROYED_TIME         = "destroyed_time";
		public final static String ROLE_IN_GROUP          = "role_in_group";
		public final static String VIEWER_CAN_UPDATE      = "viewer_can_update";
		public final static String VIEWER_CAN_DESTROY     = "viewer_can_destroy";
		public final static String VIEWER_CAN_REMOVE      = "viewer_can_remove";
		public final static String VIEWER_CAN_GRANT       = "viewer_can_grant";
		public final static String VIEWER_CAN_QUIT        = "viewer_can_quit";
		public final static String BULLETIN               = "bulletin";
		public final static String BULLETIN_UPDATED_TIME  = "bulletin_updated_time";
		public final static String INVITE_IDS             = "invited_ids";
		public final static String START_TIME             = "start_time";
		public final static String END_TIME               = "end_time";
		public final static String COVER_URL               = "cover";
		public final static String CREATOR_ID             = "creator_id";
		public final static String CREATOR_NAME           = "creator_name";
		public final static String CREATOR_IMAGEURL       = "creator_imageurl";
		public final static String INVITED_COUNT          = "invited_count";
		public final static String APPLIED_COUNT          = "applied_count";
		public final static String REPEAT_TYPE            = "repeat_type";
		public final static String REMINDER_TIME          = "reminder_time";
		public final static String TOP_POST_NAME          = "top_post_name";
		public final static String TOP_POST_COUNT         = "top_post_count";
        public final static String TOP_POLL_COUNT         = "top_poll_count";
        public final static String PAGE_ID                = "page_id";
        public final static String FORMAL                 = "formal";
        public final static String SUBTYPE                = "subtype";
        public final static String PARENT_ID              = "parent_id";
        public final static String EVENT_COUNT            = "event_count";
        public final static String FORMAL_CIRCLE_COUNT    = "formal_circle_count";
        public final static String FREE_CIRCLE_COUNT    = "free_circle_count";
	}
	
	public final static class PageColumns {
		
		public final static String PAGE_ID              = "pageid";
		public final static String PAGE_NAME            = "name";
		public final static String PAGE_NAME_EN         = "name_en";
		public final static String PAGE_ADDRESS         = "address";
		public final static String PAGE_ADDRESS_EN      = "address_en";
		public final static String PAGE_DESCRIPTION     = "description";
		public final static String PAGE_DESCRIPTION_EN  = "description_en";
		public final static String EMAIL                = "email";
		public final static String WEBSITE              = "website";
		public final static String TEL                  = "tel";
		public final static String FAX                  = "fax";
		public final static String ZIP_CODE             = "zip_code";
		public final static String SMALL_LOGO_URL       = "small_logo_url";
		public final static String LOGO_URL             = "logo_url";
		public final static String LARGE_LOGO_URL       = "large_logo_url";
		public final static String SMALL_COVER_URL      = "small_cover_url";
		public final static String COVER_URL            = "cover_url";
		public final static String LARGE_COVER_URL      = "large_cover_url";
		public final static String ASSOCIATED_ID        = "associated_id";
		public final static String CREATED_TIME         = "created_time";
		public final static String UPDATED_TIME         = "updated_time";
		public final static String FOLLOWERS_COUNT      = "followers_count";
		public final static String FOLLOWED             = "followed";
		public final static String VIEWER_CAN_UPDATE    = "viewer_can_update";
		public final static String CREATORID            = "creatorId";
		public final static String FREE_CIRCLE_IDS      = "free_circle_ids";
	}
	
	public final static class CircleEventsColumns {
		
		public final static String CIRCLEID              = "circleid";
		public final static String EVENT_ID              = "event_id";
		public final static String EVENT_NAME            = "event_name";
		public final static String LOCATION              = "location";
		public final static String START_TIME            = "start_time";
		public final static String END_TIME              = "end_time";
		public final static String COVER                 = "cover";
		public final static String CREATOR_ID            = "creator_id";
		public final static String CREATOR_NAME          = "creator_name";
		public final static String CREATOR_IMAGEURL      = "creator_imageurl";
		public final static String CREATE_TIME           = "create_time";
		public final static String ROLE_IN_GROUP         = "role_in_group";
		public final static String REFERRED_COUNT        = "referred_count";
	}
	
	public final static class CategoryColumns {
		public final static String CATEGORY_ID           = "category_id";
		public final static String CATEGORY_NAME         = "category_name";
		public final static String CREATOR_ID            = "creator_id";
		public final static String SCOPE_ID              = "scope_id";
		public final static String SCOPE_NAME            = "scope_name";
	}

	final static String[] CATEGORY_PROJECTION = {
		CategoryColumns.CATEGORY_ID,
		CategoryColumns.CATEGORY_NAME,
		CategoryColumns.CREATOR_ID,
		CategoryColumns.SCOPE_ID,
		CategoryColumns.SCOPE_NAME
    };
	
	final static String[] CIRCLE_CIRCLES_PROJECTION = {
		CircleCirclesColumns.CIRCLEID,
		CircleCirclesColumns.CIRCLE_NAME,
		CircleCirclesColumns.PROFILE_IMAGE_URL,
		CircleCirclesColumns.PROFILE_SIMAGE_URL,
		CircleCirclesColumns.PROFILE_LIMAGE_URL,
		CircleCirclesColumns.DESCRIPTION,
		CircleCirclesColumns.ROLE_IN_GROUP,
		CircleCirclesColumns.FORMAL,
		CircleCirclesColumns.SUBTYPE,
		CircleCirclesColumns.PARENT_ID
    };
	
	
	final static String[] CIRCLE_EVENT_PROJECTION = {
		CircleEventsColumns.CIRCLEID,
		CircleEventsColumns.EVENT_ID,
		CircleEventsColumns.EVENT_NAME,
		CircleEventsColumns.LOCATION,
		CircleEventsColumns.START_TIME,
		CircleEventsColumns.END_TIME,
		CircleEventsColumns.COVER,
		CircleEventsColumns.CREATOR_ID,
		CircleEventsColumns.CREATOR_NAME,
		CircleEventsColumns.CREATOR_IMAGEURL,
		CircleEventsColumns.CREATE_TIME, 
		CircleEventsColumns.ROLE_IN_GROUP,
		CircleEventsColumns.REFERRED_COUNT
    };
	
	final static String[] PAGE_PROJECTION = {
		PageColumns.PAGE_ID,
		PageColumns.PAGE_NAME,
		PageColumns.PAGE_NAME_EN,
		PageColumns.PAGE_ADDRESS,
		PageColumns.PAGE_ADDRESS_EN,
		PageColumns.PAGE_DESCRIPTION,
		PageColumns.PAGE_DESCRIPTION_EN,
		PageColumns.EMAIL,
		PageColumns.WEBSITE,
		PageColumns.TEL,
		PageColumns.FAX,
		PageColumns.ZIP_CODE,
		PageColumns.SMALL_LOGO_URL,
		PageColumns.LOGO_URL,
		PageColumns.LARGE_LOGO_URL,
		PageColumns.SMALL_COVER_URL,
		PageColumns.COVER_URL,
		PageColumns.LARGE_COVER_URL,
		PageColumns.ASSOCIATED_ID,
		PageColumns.CREATED_TIME,
		PageColumns.UPDATED_TIME,
		PageColumns.FOLLOWERS_COUNT,
		PageColumns.FOLLOWED,
		PageColumns.VIEWER_CAN_UPDATE,
		PageColumns.CREATORID,
		PageColumns.FREE_CIRCLE_IDS
    };
	
	final static String[] PAGE_SIMPLE_PROJECTION = {
		PageColumns.PAGE_ID,
		PageColumns.PAGE_NAME,
		PageColumns.PAGE_NAME_EN,
		PageColumns.SMALL_LOGO_URL,
		PageColumns.LOGO_URL,
		PageColumns.LARGE_LOGO_URL,
		PageColumns.FOLLOWERS_COUNT,
		PageColumns.CREATORID,
		PageColumns.FOLLOWED
	};
	
	public final static class LookUpPeopleColumns{
		public final static String ID                     = "_id";
		public final static String CONTACT_ID             = "contact_id";
		public final static String UID                    = "uid";
		public final static String PHOTO_ID               = "photo_id";
		public final static String NAME                   = "name";
		public final static String TYPE                   = "type";
	}
	
	public final static class WorkExperienceColumns{
		public final static String ID                     = "_id";
		public final static String WORK_FROM			  = "work_from";
		public final static String WORK_TO			      = "work_to";
		public final static String USERID 		          = "uid";
		public final static String COMPANY                = "company";
		public final static String OFFICE_ADDRESS         = "office_address";
		public final static String DEPARTMENT    	      = "department";
		public final static String JOB_TITLE    	      = "job_title";
		public final static String JOB_DESCRIPTION    	  = "job_description";
	}
	
	public final static class EducationColumns{
		public final static String ID                     = "_id";
		public final static String EDU_FROM			      = "edu_from";
		public final static String EDU_TO			      = "edu_to";
		public final static String USERID 		          = "uid";
		public final static String SCHOOL                 = "school";
		public final static String TYPE                   = "type";
		public final static String SCHOOL_CLASS           = "class";
		public final static String SCHOOL_LOCATION    	  = "school_location";
		public final static String DEGREE    	      	  = "degree";
		public final static String MAJOR    	  		  = "major";
	}
	
	public final static class ShareResourceColumns {
		public final static String ID = "_id";
		public final static String USERID = "uid";
		public final static String SHARED_TEXT_COUNT = "shared_text_count";
		public final static String SHARED_APP_COUNT = "shared_app_count";
		public final static String SHARED_PHOTO_COUNT = "shared_photo_count";
		public final static String SHARED_LINK_COUNT = "shared_link_count";
		public final static String SHARED_BOOK_COUNT = "shared_book_count";
		public final static String SHARED_STATIC_FILE = "shared_static_file";
		public final static String SHARED_AUDIO = "shared_audio";
		public final static String SHARED_VIDEO = "shared_video";
		
		
	}
	
	public final static class CircleUsersColumns{
		public final static String ID                     = "_id";
		public final static String CIRCLE_ID              = "circleid";
		public final static String USERID 		          = "uid";		
	}
	public final static class PhoneEmailColumns{
		public final static String ID                     = "_id";
		public final static String USERID                 = "uid";
		public final static String TYPE                   = "type";
		public final static String INFO                   = "info";
		public final static String ISBIND                 = "isbind";
		public final static String PHONE_OR_EMAIL         = "phone_or_email";
	}
	public final static class UserImageColumns{
		public final static String ID                     = "_id";
		public final static String USERID                 = "uid";
		public final static String BELONG_USERID                 = "belong_uid";
		public final static String IMAGE_URL              = "image_url";
	    public final static String IMAGE_DISPLAY_NAME     = "display_name";
		public final static String TYPE                   = "type";
	}
	
	public final static class SharedPhotosColumns{
		public final static String ID                     = "_id";
		public final static String USERID                 = "uid";
		public final static String POST_ID                = "post_id";
		public final static String PHOTO_IMG_MIDDLE       = "photo_img_middle";
	}
	
	public final static class QiupuAlbumColumns{
		public final static String ID                     = "_id";
		public final static String ALBUM_ID               = "album_id";
		public final static String USERID                 = "uid";
		public final static String ALBUM_TYPE             = "album_type";
		public final static String TITLE                  = "title";
		public final static String SUMMARY                = "summary";
		public final static String PRIVACY                = "privacy";
		public final static String CREATED_TIME           = "created_time";
		public final static String UPDATED_TIME           = "updated_time";
		public final static String PHOTO_COUNT            = "photo_count";
		public final static String ALBUM_COVER_MID        = "album_cover_photo_middle";
		public final static String HAVE_EXPIRED           = "hava_expired";
	}
	
	public final static class QiupuPhotoColumns{
		public final static String ID                     = "_id";
		public final static String PHOTO_ID               = "photo_id";
	    public final static String ALBUM_ID               = "album_id";
	    public final static String USERID                 = "uid";
	    public final static String PHOTO_URL_SMALL        = "photo_url_small";
	    public final static String PHOTO_URL_MIDDLE       = "photo_url_middle";
	    public final static String PHOTO_URL_BIG          = "photo_url_big";
	    public final static String PHOTO_URL_THUMBNAIL    = "photo_url_thumbnail";
	    public final static String PHOTO_URL_ORIGINAL     = "photo_url_original";
	    public final static String CAPTION                = "caption";
	    public final static String CREATED_TIME           = "created_time";
	    public final static String LOCATION               = "location";
	    public final static String ILIKEED                = "iliked";
	    public final static String LIKES_COUNT            = "likes_count";
	    public final static String COMMENTS_COUNT         = "comments_count";
	    public final static String FROM_USER_ID           = "from_user_id";
	    public final static String FROM_NICK_NAME         = "from_nick_name";
	    public final static String FROM_IMAGE_URL         = "from_image_url";
    }
	
	public final static class PerhapsNameColumns{
        public final static String ID                     = "_id";
        public final static String USERID                 = "uid";
        public final static String FULLNAME               = "fullname";
        public final static String COUNT                  = "count";
    }

	public final static class RequestColumns {
        public static final String _ID = "_id";
        public static final String REQUEST_ID = "request_id";
        public static final String TYPE = "type";
        public static final String MESSAGE = "message";
        public static final String CREATE_TIME = "created_time";
        public static final String DATA = "data";
        public static final String UID = "uid";
        public static final String NICK_NAME = "nick_name";
        public static final String PROFILE_IMAGE_URL = "profile_image_url";
        public static final String PROFILE_SIMAGE_URL = "profile_simage_url";
        public static final String PROFILE_LIMAGE_URL = "profile_limage_url";
        public static final String EMAIL_1 = "email_1";
        public static final String EMAIL_2 = "email_2";
        public static final String EMAIL_3 = "email_3";
        public static final String PHONE_1 = "phone_1";
        public static final String PHONE_2 = "phone_2";
        public static final String PHONE_3 = "phone_3";
        public static final String SCENEID = "scene";
    }

	public final static class ThemesColumns {
		public static final String _ID = "_id";
		public static final String THEME_ID = "id";
		public static final String THEME_CREATOR = "creator";
		public static final String THEME_UPDATED_TIME = "updated_time";
		public static final String THEME_NAME = "name";
		public static final String THEME_IMAGE_URL = "image_url";
	}
	
	public final static class EventCalendarColumns {
		public static final String _ID = "_id";
		public static final String EVENT_ID = "event_id";
		public static final String CALENDAR_EVENT_ID = "calendar_event_id";
		public static final String UPDATE_TIME = "update_time";
	}
	
	final static String[] EVENTCALENDAR_PROJECTION = {
		EventCalendarColumns._ID,
		EventCalendarColumns.EVENT_ID,
		EventCalendarColumns.CALENDAR_EVENT_ID,
		EventCalendarColumns.UPDATE_TIME
    };
	
	final static String[] THEME_PROJECTION = {
		ThemesColumns._ID,
		ThemesColumns.THEME_ID,
		ThemesColumns.THEME_CREATOR,
		ThemesColumns.THEME_UPDATED_TIME,
		ThemesColumns.THEME_NAME,
		ThemesColumns.THEME_IMAGE_URL
    };
	
	final static String[] REQUEST_PROJECTION = {
        RequestColumns._ID,
        RequestColumns.REQUEST_ID,
        RequestColumns.TYPE,
        RequestColumns.MESSAGE,
        RequestColumns.CREATE_TIME,
        RequestColumns.DATA,
        RequestColumns.UID,
        RequestColumns.NICK_NAME,
        RequestColumns.PROFILE_IMAGE_URL,
        RequestColumns.PROFILE_SIMAGE_URL,
        RequestColumns.PROFILE_LIMAGE_URL,
        RequestColumns.EMAIL_1,
        RequestColumns.EMAIL_2,
        RequestColumns.EMAIL_3,
        RequestColumns.PHONE_1,
        RequestColumns.PHONE_2,
        RequestColumns.PHONE_3
    };

    public final static class PollColumns {
        public static final String _ID = "_id";
        public static final String POLL_ID = "poll_id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String TYPE = "type";
        public static final String RESTRICT = "restrict";
        public static final String TARGET = "target";
        public static final String MULTI = "multi_count";
        public static final String LIMIT = "limits";
        public static final String PRIVACY = "privacy";
        public static final String CREATED_TIME = "created_time";
        public static final String END_TIME = "end_time";
        public static final String UPDATE_TIME = "updated_time";
        public static final String DESTROY_TIME = "destroyed_time";
        // server should support
        public static final String ATTEND_STATUS = "attend_status";
        public static final String ATTEND_COUNT = "attend_count";
        public static final String DURATION_TIME = "left_time";
        public static final String VIEWER_CAN_VOTE = "viewer_can_vote";
        public static final String MODE = "mode";
        public static final String HAS_VOTED = "has_voted";
        public static final String VIEWER_LEFT = "viewer_left";
        public static final String UID = "uid";
        public static final String USER_NAME = "user_name";
        public static final String IMAGE_URL = "image_url";
    }
    
    public final static class CompanyColumns{
		public final static String ID                     = "_id";
		public final static String COMPANY_ID             = "company_id";
		public final static String DEPARTMENT_ID          = "department_id";
		public final static String CREATED_TIME           = "created_time";
		public final static String UPDATED_TIME           = "updated_time";
		public final static String ROLE                   = "role";
		public final static String DOMAIN1          	  = "domain1";
		public final static String DOMAIN2                = "domain2";
		public final static String DOMAIN3                = "domain3";
		public final static String DOMAIN4                = "domain4";
		public final static String NAME                   = "name";
		public final static String ADDRESS                = "address";
		public final static String EMAIL                  = "email";
		public final static String WEB_SITE               = "website";
		public final static String TEL                    = "tel";
		public final static String FAX                    = "fax";
		public final static String ZIP_CODE               = "zip_code";
		public final static String PERSON_COUNT           = "person_count";
		public final static String DEPARTMENT_COUNT           = "department_count";
		public final static String SMALL_LOGO_URL         = "small_logo_url";
		public final static String LOGO_URL               = "logo_url";
		public final static String LARGE_LOGO_URL         = "large_logo_url";
		public final static String SMALL_COVER_URL         = "small_cover_url";
		public final static String COVER_URL               = "cover_url";
		public final static String LARGE_COVER_URL         = "large_cover_url";
		public final static String DESCRIPTION		      = "description";
	}

    final static String[] COMPANY_PROJECTION = {
		CompanyColumns.COMPANY_ID,
		CompanyColumns.DEPARTMENT_ID,
		CompanyColumns.CREATED_TIME,
		CompanyColumns.UPDATED_TIME,
		CompanyColumns.ROLE,
		CompanyColumns.DOMAIN1,
		CompanyColumns.DOMAIN2,
		CompanyColumns.DOMAIN3,
		CompanyColumns.DOMAIN4,
		CompanyColumns.NAME,
		CompanyColumns.ADDRESS,
		CompanyColumns.EMAIL,
		CompanyColumns.WEB_SITE,
		CompanyColumns.TEL,
		CompanyColumns.FAX,
		CompanyColumns.ZIP_CODE,
		CompanyColumns.PERSON_COUNT,
		CompanyColumns.DEPARTMENT_COUNT,
		CompanyColumns.SMALL_LOGO_URL,
		CompanyColumns.LOGO_URL,
		CompanyColumns.LARGE_LOGO_URL,
		CompanyColumns.LARGE_COVER_URL,
		CompanyColumns.SMALL_COVER_URL,
		CompanyColumns.COVER_URL,
		CompanyColumns.DESCRIPTION
	};
    
    final static String[] POLL_PROJECTION = {
        PollColumns._ID,
        PollColumns.POLL_ID,
        PollColumns.TITLE,
        PollColumns.TYPE,
        PollColumns.RESTRICT,
        PollColumns.TARGET,
        PollColumns.MULTI,
        PollColumns.LIMIT,
        PollColumns.PRIVACY,
        PollColumns.CREATED_TIME,
        PollColumns.END_TIME,
        PollColumns.UPDATE_TIME,
        PollColumns.DESTROY_TIME,

        PollColumns.ATTEND_STATUS,
        PollColumns.ATTEND_COUNT,
        PollColumns.DURATION_TIME,
        PollColumns.VIEWER_CAN_VOTE,
        PollColumns.MODE,
        PollColumns.VIEWER_LEFT,
        PollColumns.HAS_VOTED,
        PollColumns.UID,
        PollColumns.USER_NAME,
        PollColumns.IMAGE_URL
    };

    final static String[] CIRCLE_POLL_PROJECTION = {
        PollColumns._ID,
        PollColumns.POLL_ID,
        PollColumns.TITLE,
        PollColumns.TARGET,
        PollColumns.CREATED_TIME,
        PollColumns.END_TIME,

        PollColumns.ATTEND_STATUS,
        PollColumns.ATTEND_COUNT,
        PollColumns.UID,
        PollColumns.USER_NAME,
        PollColumns.IMAGE_URL
    };

	final static String[] PHONE_EMAIL_PROJECTION = {
	    PhoneEmailColumns.ID,
	    PhoneEmailColumns.USERID,
	    PhoneEmailColumns.TYPE,
	    PhoneEmailColumns.INFO,
	    PhoneEmailColumns.ISBIND,
	    PhoneEmailColumns.PHONE_OR_EMAIL
	};
	
	final static String[] USER_IMAGE_PROJECTION = {
		UserImageColumns.ID,
		UserImageColumns.USERID,
		UserImageColumns.BELONG_USERID,
		UserImageColumns.IMAGE_URL,
		UserImageColumns.IMAGE_DISPLAY_NAME,
		UserImageColumns.TYPE
	};
	final static String[] SHARED_PHOTOS_PROJECTION = {
		SharedPhotosColumns.ID,
		SharedPhotosColumns.USERID,
		SharedPhotosColumns.POST_ID,
		SharedPhotosColumns.PHOTO_IMG_MIDDLE
	};
	
	final static String[] QIUPU_ALBUM_PROJECTION = {
		QiupuAlbumColumns.ID,
		QiupuAlbumColumns.ALBUM_ID,
		QiupuAlbumColumns.USERID,
		QiupuAlbumColumns.ALBUM_TYPE,
		QiupuAlbumColumns.TITLE,
		QiupuAlbumColumns.SUMMARY,
		QiupuAlbumColumns.PRIVACY,
		QiupuAlbumColumns.CREATED_TIME,
		QiupuAlbumColumns.UPDATED_TIME,
		QiupuAlbumColumns.HAVE_EXPIRED,
		QiupuAlbumColumns.PHOTO_COUNT,
		QiupuAlbumColumns.ALBUM_COVER_MID
	};
	final static String[] QIUPU_PHOTO_PROJECTION = {
	    QiupuPhotoColumns.ID,
	    QiupuPhotoColumns.ALBUM_ID,
	    QiupuPhotoColumns.USERID,
	    QiupuPhotoColumns.PHOTO_ID,
	    QiupuPhotoColumns.PHOTO_URL_SMALL,
	    QiupuPhotoColumns.PHOTO_URL_MIDDLE,
	    QiupuPhotoColumns.PHOTO_URL_BIG,
	    QiupuPhotoColumns.PHOTO_URL_THUMBNAIL,
	    QiupuPhotoColumns.PHOTO_URL_ORIGINAL,
	    QiupuPhotoColumns.CAPTION,
	    QiupuPhotoColumns.CREATED_TIME,
	    QiupuPhotoColumns.LOCATION,
	    QiupuPhotoColumns.ILIKEED,
	    QiupuPhotoColumns.LIKES_COUNT,
	    QiupuPhotoColumns.COMMENTS_COUNT,
	    QiupuPhotoColumns.FROM_USER_ID,
	    QiupuPhotoColumns.FROM_NICK_NAME,
	    QiupuPhotoColumns.FROM_IMAGE_URL
	};
	
	final static String[] PERHAPS_NAME_PROJECTION = {
        PerhapsNameColumns.ID,
        PerhapsNameColumns.USERID,
        PerhapsNameColumns.FULLNAME,
        PerhapsNameColumns.COUNT
    };
	
	final static String[] WORK_EXPERIENCE_PROJECTION = {
		WorkExperienceColumns.ID,
		WorkExperienceColumns.WORK_FROM,
		WorkExperienceColumns.WORK_TO,
		WorkExperienceColumns.USERID,
		WorkExperienceColumns.COMPANY,
		WorkExperienceColumns.OFFICE_ADDRESS,
		WorkExperienceColumns.DEPARTMENT,
		WorkExperienceColumns.JOB_TITLE,
		WorkExperienceColumns.JOB_DESCRIPTION
	};
	
	final static String[] EDUCATION_PROJECTION = {
		EducationColumns.ID,
		EducationColumns.EDU_FROM,
		EducationColumns.EDU_TO,
		EducationColumns.USERID,
		EducationColumns.SCHOOL,
		EducationColumns.TYPE,
		EducationColumns.SCHOOL_CLASS,
		EducationColumns.SCHOOL_LOCATION,
		EducationColumns.DEGREE,
		EducationColumns.MAJOR
	};
	
	final static String[] USER_CIRCLE_PROJECTION = {
		CircleColumns.ID,
		CircleColumns.USERID,
		CircleColumns.CIRCLE_ID,
		CircleColumns.CIRCLE_NAME,
		CircleColumns.MEMBER_COUNT,
		CircleColumns.TYPE
	};

	final static String[] GROUP_PROJECTION = {
		GroupColumns.CIRCLE_ID,
		GroupColumns.PROFILE_IMAGE_URL,
		GroupColumns.PROFILE_SIMAGE_URL,
		GroupColumns.PROFILE_LIMAGE_URL,
		GroupColumns.COMPANY,
		GroupColumns.OFFICE_ADDRESS,
		GroupColumns.DEPARTMENT,
		GroupColumns.JOB_TITLE,
		GroupColumns.LOCATION,
		GroupColumns.DESCRIPTION,
		
		GroupColumns.MEMBER_LIMIT,
		GroupColumns.IS_STREAM_PUBLIC,
		GroupColumns.CAN_SEARCH,
		GroupColumns.CAN_VIEW_MEMBERS,
		GroupColumns.CAN_JOIN,
		GroupColumns.CAN_MEMBER_INVITE,
		GroupColumns.CAN_MEMBER_APPROVE,
		GroupColumns.CAN_MEMBER_POST,
		GroupColumns.CAN_MEMBER_QUIT,
		GroupColumns.NEED_INVITE_CONFIRM,
		GroupColumns.CREATED_TIME,
		GroupColumns.UPDATED_TIME,
		GroupColumns.DESTROYED_TIME,
		GroupColumns.ROLE_IN_GROUP,
		GroupColumns.VIEWER_CAN_UPDATE,
		GroupColumns.VIEWER_CAN_DESTROY,
		GroupColumns.VIEWER_CAN_REMOVE,
		GroupColumns.VIEWER_CAN_GRANT,
		GroupColumns.VIEWER_CAN_QUIT,
		GroupColumns.BULLETIN,
		GroupColumns.BULLETIN_UPDATED_TIME,
		GroupColumns.INVITE_IDS,
		GroupColumns.START_TIME,
		GroupColumns.END_TIME,
		GroupColumns.COVER_URL,
		GroupColumns.CREATOR_ID,
		GroupColumns.CREATOR_NAME,
		GroupColumns.CREATOR_IMAGEURL,
		GroupColumns.INVITED_COUNT,
		GroupColumns.APPLIED_COUNT,
		GroupColumns.REPEAT_TYPE,
		GroupColumns.REMINDER_TIME,
		GroupColumns.TOP_POST_NAME,
		GroupColumns.TOP_POST_COUNT,
		GroupColumns.TOP_POLL_COUNT,
		GroupColumns.PAGE_ID,
		GroupColumns.FORMAL,
		GroupColumns.SUBTYPE,
		GroupColumns.PARENT_ID,
		GroupColumns.EVENT_COUNT,
		GroupColumns.FORMAL_CIRCLE_COUNT,
		GroupColumns.FREE_CIRCLE_COUNT
	};
	
	final static String[] LOOKUP_PEOPLE_PROJECTION = {
		LookUpPeopleColumns.ID,
		LookUpPeopleColumns.CONTACT_ID,
		LookUpPeopleColumns.UID,
		LookUpPeopleColumns.PHOTO_ID,
		LookUpPeopleColumns.NAME,
		LookUpPeopleColumns.TYPE
	};
	
	final static String[] USERS_INFO_PROJECTION={
		UsersColumns.ID,
		UsersColumns.USERID,
		UsersColumns.USERNAME,
		UsersColumns.NICKNAME,
		UsersColumns.DATA_OF_BIRTH,
		UsersColumns.COMPANY,
		UsersColumns.OFFICE_ADDRESS,
		UsersColumns.ABOUT_ME,
		UsersColumns.DEPARTMENT,
		UsersColumns.PROVINCE,
		UsersColumns.CITY,
		UsersColumns.CREATED_AT,
		UsersColumns.LAST_VISIT_TIME,
		UsersColumns.VERIFY_CODE,
		UsersColumns.VERIFIED,
//		UsersColumns.DOMAIN,
		UsersColumns.PROFILE_IMAGE_URL,
		UsersColumns.PROFILE_SIMAGE_URL,
		UsersColumns.PROFILE_LIMAGE_URL,
		UsersColumns.LOCATION,
		UsersColumns.DESCRIPTION,
		UsersColumns.URL,
		UsersColumns.GENDER,
		UsersColumns.FRIENDS_COUNT,
		UsersColumns.FOLLOWERS_COUNT,
		UsersColumns.FAVOURITES_COUNT,
		UsersColumns.APP_COUNT,
		UsersColumns.STATUS,
		UsersColumns.STATUS_TIME,
		UsersColumns.BIDI,
		UsersColumns.CIRCLE_ID,
		UsersColumns.CIRCLE_NAME,
		UsersColumns.PROFILE_PRIVACY,
		UsersColumns.REQUESTED_ID,
		UsersColumns.SHORTCUT,
		UsersColumns.HIS_FRIENDS,
		UsersColumns.NAME_PINGYIN,
		UsersColumns.JOB_TITLE,
		UsersColumns.WORK_HISTORY,
		UsersColumns.REMARK
	};
	
	final static String[] SIMPLE_USERS_INFO_PROJECTION={
		UsersColumns.ID,
		UsersColumns.USERID,
		UsersColumns.USERNAME,
		UsersColumns.BIDI,
		UsersColumns.NICKNAME,
		UsersColumns.NAME_PINGYIN,
		UsersColumns.PROFILE_IMAGE_URL,
		UsersColumns.PROFILE_SIMAGE_URL,
		UsersColumns.PROFILE_LIMAGE_URL,
		UsersColumns.LOCATION,
//		UsersColumns.DOMAIN,
	};
	
	final static String[] SHARE_SOURCE_PROJECTION={
		ShareResourceColumns.ID,
		ShareResourceColumns.USERID,
		ShareResourceColumns.SHARED_APP_COUNT,
		ShareResourceColumns.SHARED_BOOK_COUNT,
		ShareResourceColumns.SHARED_LINK_COUNT,
		ShareResourceColumns.SHARED_PHOTO_COUNT,
		ShareResourceColumns.SHARED_TEXT_COUNT,
		ShareResourceColumns.SHARED_STATIC_FILE,
		ShareResourceColumns.SHARED_AUDIO,
		ShareResourceColumns.SHARED_VIDEO
	};
	
	public final static class ApkinfoColumns{
		public final static String ID                    = "_id";
		public final static String UID                   = "uid";
		public final static String APKNAME               = "apkname";
		public final static String APKCOMPONENTNAME      = "apkcomponentname";
		public final static String APKVERSIONCODE        = "apkversioncode";
		public final static String APKVERSIONNAME        = "apkversionname";
		public final static String APKLATESTVERSIONCODE  = "apklatestversioncode";
		public final static String APKLATESTVERSIONNAME  = "apklatestversionname";
		public final static String APKDESC               = "apkdesc";
		public final static String FILESIZE              = "filesize";
		public final static String FILEPATH              = "filepath_url";
		public final static String Local_FILEPATH        = "localpath";
		public final static String APKSTATUS             = "apkstatus";
		public final static String APKICON               = "apkicon";
		public final static String PROCESSSTATUS         = "processstatus";
		public final static String VISIBILITY            = "visibility";
		public final static String RATIO                 = "ratio";
		public final static String APK_SERVER_ID         = "apk_server_id";
		public final static String ISFAVORITES           = "isfavorite";
		public final static String PRICE                 = "price";
		public final static String ISILIKE               = "isilike";
		public final static String APP_USERD             = "app_used";
		public final static String LAST_INSTALLED        = "last_installed_time";
	}
	
	
	public final static class UsersColumns{
		public final static String ID                     = "_id";
		public final static String USERID 		          = "uid";
		public final static String USERNAME        		  = "name";
		public final static String NICKNAME               = "nickname";
		public final static String DATA_OF_BIRTH          = "date_of_birth";
		public final static String ABOUT_ME                ="about_me";
		public final static String COMPANY                ="company";
		public final static String DEPARTMENT			  ="department";
		public final static String OFFICE_ADDRESS			  ="office_address";
		public final static String PROVINCE               = "province";
		public final static String CITY 		          = "city";
		public final static String CREATED_AT        	  = "created_at";
		public final static String LAST_VISIT_TIME    	  = "last_visit_time";
		public final static String VERIFY_CODE            = "verify_code";
		public final static String VERIFIED               = "verified";
//		public final static String DOMAIN                 = "domain";
		public final static String PROFILE_IMAGE_URL      ="profile_image_url";
		public final static String PROFILE_SIMAGE_URL     ="profile_simage_url";
		public final static String PROFILE_LIMAGE_URL     = "profile_limage_url";
		public final static String LOCATION 		      = "location";
		public final static String DESCRIPTION        	  = "description";
		public final static String URL    	              = "url";
		public final static String GENDER                 = "gender";
		public final static String FRIENDS_COUNT          = "friends_count";
		public final static String FOLLOWERS_COUNT        = "followers_count";
		public final static String FAVOURITES_COUNT       ="favourites_count";
		public final static String APP_COUNT              = "app_count";
		public final static String STATUS                 ="status";
		public final static String STATUS_TIME            ="status_time";
		
		public final static String BIDI                   = "bidi";
		public final static String CIRCLE_ID              = "circleid";
		public final static String CIRCLE_NAME            = "circlename";
		
		public final static String PROFILE_PRIVACY        = "profile_privacy";
		public final static String REQUESTED_ID        = "requested_id";
		public final static String SHORTCUT        = "shortcut";
		public final static String HIS_FRIENDS     = "his_friend";
		public final static String NAME_PINGYIN    = "name_pinyin";
		public final static String JOB_TITLE    = "job_title";
		public final static String WORK_HISTORY    = "work_history";
        public final static String REFERRED_COUNT = "referred_count";
		public final static String REMARK         = "remark";
		public final static String DB_STATUS      = "db_status";  
	}
	
	final static String[] APKINFO_PROJECTION = {
		ApkinfoColumns.ID,
		ApkinfoColumns.UID,
		ApkinfoColumns.APKNAME,
		ApkinfoColumns.APKCOMPONENTNAME,
		ApkinfoColumns.APKVERSIONCODE,
		ApkinfoColumns.APKVERSIONNAME,
		ApkinfoColumns.APKLATESTVERSIONCODE,
		ApkinfoColumns.APKLATESTVERSIONNAME,
		ApkinfoColumns.APKDESC,
		ApkinfoColumns.FILESIZE,
		ApkinfoColumns.FILEPATH,
		ApkinfoColumns.Local_FILEPATH,
		ApkinfoColumns.APKSTATUS,
		ApkinfoColumns.APKICON,
		ApkinfoColumns.PROCESSSTATUS,
		ApkinfoColumns.VISIBILITY,
		ApkinfoColumns.APP_USERD,
		ApkinfoColumns.RATIO,
		ApkinfoColumns.APK_SERVER_ID,
		ApkinfoColumns.ISFAVORITES,
		ApkinfoColumns.PRICE,
		ApkinfoColumns.ISILIKE,
		ApkinfoColumns.LAST_INSTALLED
	};
//
//	final static String[] APKINFO_GROUP_PROJECTION = {
//		"apkstatus"
//	};
//
	public final static class ApkinfoGroupColumns{
	    public final static String APKSTATUS = "apkstatus";	
	}
	
	 //settings
    public static class SettingsCol{
        public static final String ID      = "_id";
        public static final String Name    = "name";
        public static final String Value   = "value";
    }
    
    public static String[]settingsProject =  new String[]{
        "_id",
        "name",
        "value",
    };

    public final static class ChatRecordColumns {
        public final static String ID = "_id";
        public final static String BORQS_ID = "borqs_id";
        public final static String MESSAGE = "message";
        public final static String PROFILE_URL = "profile_url";
        public final static String TYPE = "type";
        public final static String CREATED_TIME = "created_time";
        public final static String UNREAD = "unread";
        public final static String DISPLAY_NAME = "display_name";
    }

    final static String[] CHAT_PROJECTION = {
        ChatRecordColumns.ID,
        ChatRecordColumns.BORQS_ID,
        ChatRecordColumns.MESSAGE,
        ChatRecordColumns.PROFILE_URL,
        ChatRecordColumns.TYPE,
        ChatRecordColumns.CREATED_TIME,
        ChatRecordColumns.UNREAD,
        ChatRecordColumns.DISPLAY_NAME
    };

    // should we remove the total database, in order to avoid repeating bug fix when
    // new settings item need to clear while logout.
    public void logout() {
        deleteUsers();
        
//        deleteApps();
        
        //clear time settings
//        removeSetting(LAST_SYNC_USERAPKINFO_TIME);
//        removeSetting(LAST_SYNC_USERINFO_TIME);
//        removeSetting(LAST_SYNC_FRIENDS_APK_INFO);    	
//        removeSetting(LAST_GET_FRIENDS);
//
//        removeSetting(AUTO_UPLOAD_APP_BACKUP);
//        removeSetting(LAST_SYNC_EVENTS_TIME);
//        removeSetting(LAST_SYNC_CIRCLE_TIME);
//        removeSetting(IS_SHOW_RIGHT_MOVE_GUIDE);
//        removeSetting(HOME_ACTIVITY_ID);

        clearAllSetting();
        
        removeAllCircles();
        deleteCachedRequest();
        removeEventCalendar();
        removePhoneEmail();
        removeAllCircleCircles();
        removeAllCirclePolls();
        removeAllCircleEvents();
        removeAllPoll();
        
        new NotificationOperator(mContext).clearAllDbNtf();
    }
    
    private void clearAllSetting() {
    	mContext.getContentResolver().delete(SETTINGS_CONTENT_URI, null, null);
	}

	private void removeAllPoll() {
    	mContext.getContentResolver().delete(POLL_URI, null, null);
    }
    private void removeAllCircleCircles() {
        mContext.getContentResolver().delete(CIRCLE_CIRCLES_CONTENT_URI, null, null);
    }
    private void removeAllCirclePolls() {
        mContext.getContentResolver().delete(CIRCLE_POLL_URI, null, null);
    }
    private void removeAllCircleEvents() {
        mContext.getContentResolver().delete(CIRCLE_EVENTS_URI, null, null);
    }
    
    private void removePhoneEmail() {
        mContext.getContentResolver().delete(PHONEEMAIL_CONTENT_URI, null, null);
    }
    
    private void removeEventCalendar() {
        mContext.getContentResolver().delete(EVENT_CALENDARS_URI, null, null);
    }
    
    private void deleteCachedRequest() {
        mContext.getContentResolver().delete(RequestProvider.CONTENT_REQUEST_URI, null, null);
    }

    private void deleteUsers() {        
        mContext.getContentResolver().delete(USERS_CONTENT_URI, null,null);
    }
    
//    private void deleteApps() {
//        mContext.getContentResolver().delete(APKINFO_CONTENT_URI, null,null);
//    }
//
    /**
     * 
     * @return user's applications' information, include installed & server apk
     */
//    public Cursor getUserApplications(long uid) {
//    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getUserApplications uid:"+uid);
//    	String where =  ApkinfoColumns.UID +"='"+uid+"'";
//        return getUserApplications(where);
//    }

//    public ApkResponse getUserApplication(long uid, String packagename) {
//    	if(QiupuConfig.DBLOGD)
//    		Log.d(TAG, "getUserApplication uid:"+uid+" packagename:"+packagename);
//    	ApkResponse apk = null;
//    	String where =  ApkinfoColumns.UID +"='"+uid+"' and " + ApkinfoColumns.APKCOMPONENTNAME +"='"+packagename+"'";
//    	Cursor cursor = mContext.getContentResolver().query(APKINFO_CONTENT_URI, APKINFO_PROJECTION , where, null, null);
//    	if(cursor != null) {
//    		if(cursor.getCount() > 0){
//    			apk = new ApkResponse();
//    			if(cursor.moveToFirst()){
//    				apk = createApkResponse(cursor);
//    			}
//    		}
//    		cursor.close();
//    		cursor = null;
//    	}
//
//    	return apk;
//    }
//
//    public String syncApkResponse(long uid, ApkResponse apk) {
//    	if(LowPerformance)Log.d(TAG, "syncApkResponse uid:"+uid+" apk:"+apk);
//
//    	if(apk == null) {
//    		return "";
//    	}
//
//    	String where =  ApkinfoColumns.UID +"='"+uid+"' and " + ApkinfoColumns.APKCOMPONENTNAME +"='"+apk.packagename+"'";
//    	Cursor mCursor = mContext.getContentResolver().query(APKINFO_CONTENT_URI, new String[]{ApkinfoColumns.ID, ApkinfoColumns.UID} , where, null, null);
//    	if(mCursor != null)
//    	{
//            final int totalCount = mCursor.getCount();
//    		if(totalCount > 0)
//    		{
//    			mCursor.moveToFirst();
//    			ContentValues values = createApkResponseValues(uid, apk);
//    			int count = mContext.getContentResolver().update(APKINFO_CONTENT_URI, values, where, null);
//    			if(count == 0)
//    			{
//    				Log.d(TAG, "fail to update rows="+apk);
//    			}
//
//                if (totalCount > 1) {
//                    Log.e(TAG, "syncApkResponse, duplicate records: " + totalCount +
//                            " for uid: " + uid + ", apk: " + apk);
//                }
//    		}else{
//    			ContentValues cv = createApkResponseValues(uid, apk);
//        		mContext.getContentResolver().insert(APKINFO_CONTENT_URI, cv);
//    		}
//
//    		mCursor.close();
//			mCursor = null;
//    	}
//
//    	return "";
//	}
//
//    public long syncApkResponse(long uid, ArrayList<ApkResponse> apks) {
//    	if(apks == null) {
//    		return -1L;
//    	}
//    	if(LowPerformance)Log.d(TAG, "syncApkResponse uid:"+uid+" apk size:"+apks.size());
//
//    	int count=0;
//    	for(ApkResponse apk: apks)
//    	{
//
//	    	String where =  ApkinfoColumns.UID +"='"+uid+"' and " + ApkinfoColumns.APKCOMPONENTNAME +"='"+apk.packagename+"'";
//
//	    	Cursor mCursor = mContext.getContentResolver().query(APKINFO_CONTENT_URI, new String[]{ApkinfoColumns.ID, ApkinfoColumns.UID} , where, null, null);
//	    	if(mCursor != null)
//	    	{
//	    		if(mCursor.getCount() > 0)
//	    		{
//	    			ContentValues values = createApkResponseValues(uid, apk);
//	    			count = mContext.getContentResolver().update(APKINFO_CONTENT_URI, values, where, null);
//	    			if(count == 0)
//	    			{
//	    				Log.d(TAG, "fail to update rows="+apk);
//	    			}
//	    		}else{
//	    			ContentValues cv = createApkResponseValues(uid, apk);
//	        		mContext.getContentResolver().insert(APKINFO_CONTENT_URI, cv);
//	    		}
//	    		mCursor.close();
//    			mCursor = null;
//	    	}
//    	}
//
//    	//insert bull
//    	//mContext.getContentResolver().bulkInsert(APKINFO_CONTENT_URI, values)
//    	return -1L;
//	}
//
//    public int resetDownloadingStatus() {
//    	sWorker.post(new Runnable()
//        {
//        	public void run()
//        	{
//		    	String where =  ApkinfoColumns.APKSTATUS +"=" + ApkBasicInfo.APKStatus.STATUS_DOWNLOADING +
//		                " or "+ ApkinfoColumns.APKSTATUS +"=" + ApkBasicInfo.APKStatus.STATUS_DEFAULT;
//
//				ContentValues values = new ContentValues();
//				values.put(ApkinfoColumns.APKSTATUS, ApkBasicInfo.APKStatus.STATUS_NEED_DOWNLOAD);
//				int count = mContext.getContentResolver().update(APKINFO_CONTENT_URI, values, where, null);
//
//		        if(QiupuConfig.LOGD)Log.d(TAG, "resetDownloadingStatus, reset to need download count = " + count);
//
//		        final String whereUpdating = ApkinfoColumns.APKSTATUS +"=" + ApkBasicInfo.APKStatus.STATUS_UPDATING;
//		        values.put(ApkinfoColumns.APKSTATUS, ApkBasicInfo.APKStatus.STATUS_NEED_UPDATE);
//		        count += mContext.getContentResolver().update(APKINFO_CONTENT_URI, values, whereUpdating, null);
//		        if(QiupuConfig.LOGD)Log.d(TAG, "resetDownloadingStatus, plus need update count = " + count);
//        	}
//        });
//
//    	return 0;
//	}
//
//    public int resetUploadingStatus() {
//    	sWorker.post(new Runnable()
//        {
//        	public void run()
//        	{
//		        String where =  ApkinfoColumns.APKSTATUS +"=" + ApkBasicInfo.APKStatus.STATUS_UPLOADING ;
//
//				ContentValues values = new ContentValues();
//				values.put(ApkinfoColumns.APKSTATUS, ApkBasicInfo.APKStatus.STATUS_NEED_UPLOAD);
//				int count = mContext.getContentResolver().update(APKINFO_CONTENT_URI, values, where, null);
//
//		        if(QiupuConfig.LOGD)Log.d(TAG, "resetUploadingStatus, count = " + count);
//        	}
//        });
//
//    	return 0;
//	}
//
//    public void deleteApkInfomationByPackageName(long uid, String packagename){
//    	String where =  ApkinfoColumns.UID +"='"+uid +"' and " + ApkinfoColumns.APKCOMPONENTNAME +"='" + packagename+"'";
//    	int count = mContext.getContentResolver().delete(APKINFO_CONTENT_URI, where,null);
//
//    	if(count <=0)
//    	{
//    		Log.d(TAG, "Fail to delete package from database="+packagename);
//    	}
//    }
//
//    public void deleteApkInfomation(long uid){
//    	String where =  ApkinfoColumns.UID +"='"+uid+"'";
//    	int count = mContext.getContentResolver().delete(APKINFO_CONTENT_URI, where,null);
//
//        Log.d(TAG, "deleteApkInfomation, delete " + count + " for uid:" + uid);
//    }

    private static final long  ONE_WEEK_MILLIS  = 604800000;
    private static final float ONE_MONTH_MILLIS = 2678400000f;
    public static final int WEEK_CATEGORY = 0;
    public static final int MONTH_CATEGORY = 1;
    public static final int MORETHAN_MONTH_CATEGORY = 2;

    public static ApkResponse createApkResponse(Cursor mCursor) {
    	if(mCursor == null) 
    		return null;
    	
    	ApkResponse info = new ApkResponse();
    	info.id            = mCursor.getLong(mCursor.getColumnIndex(ApkinfoColumns.ID));
    	info.uid           = Long.valueOf(mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.UID)));
    	info.packagename   = mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.APKCOMPONENTNAME));
    	info.label         = mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.APKNAME));
    	info.status        = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.APKSTATUS));
    	info.versioncode      = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.APKVERSIONCODE));
    	info.versionname = formatVersionCode(mCursor, ApkinfoColumns.APKVERSIONNAME);
    	info.latest_versioncode = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.APKLATESTVERSIONCODE));
    	info.latest_versionname = formatVersionCode(mCursor, ApkinfoColumns.APKLATESTVERSIONNAME);
    	info.description    = mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.APKDESC));
		info.apkurl         = mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.FILEPATH));
		info.intallpath     = mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.Local_FILEPATH));		
		info.apksize        = mCursor.getLong(mCursor.getColumnIndex(ApkinfoColumns.FILESIZE));
		info.iconurl    = mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.APKICON));
		info.progress   = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.PROCESSSTATUS));
		info.visibility = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.VISIBILITY));
		info.app_used = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.APP_USERD))==0?false:true;
		try{
		    info.ratio      = Float.valueOf(mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.RATIO)));
		}catch(Exception ne){}
		
		info.apk_server_id = mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.APK_SERVER_ID));
		info.price = Float.valueOf(mCursor.getString(mCursor.getColumnIndex(ApkinfoColumns.PRICE)));
		
		info.isFavorite = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.ISFAVORITES))==0?false:true;
		info.iLike      = mCursor.getInt(mCursor.getColumnIndex(ApkinfoColumns.ISILIKE))==0?false:true;
		info.last_installed_time = mCursor.getLong(mCursor.getColumnIndex(ApkinfoColumns.LAST_INSTALLED));

		if(QiupuConfig.DBLOGD)Log.d(TAG, "createApkResponse apkinfo:"+info);

        long currentTime = System.currentTimeMillis();
        float in_one_week = currentTime - ONE_WEEK_MILLIS;
        float in_one_month = currentTime - ONE_MONTH_MILLIS;

        if (info.last_installed_time >= in_one_week) {
            info.category = WEEK_CATEGORY;
        } else if (info.last_installed_time < in_one_week
                && info.last_installed_time >= in_one_month) {
            info.category = MONTH_CATEGORY;
        } else if (info.last_installed_time < in_one_month) {
            info.category = MORETHAN_MONTH_CATEGORY;
        }

		return info;
    }
    
    private static String formatVersionCode(Cursor mCursor, String versionDB)
    {
    	String versionnameDb = mCursor.getString(mCursor.getColumnIndex(versionDB));
    	String version ;
    	if(StringUtil.isValidString(versionnameDb))
    	{
    		version = versionnameDb;
    	}
    	else
    	{
    		version = "";
    	}
    	return version;
    }

//    private ContentValues createApkResponseValues(ApkResponse info) {
//        return createApkResponseValues(info.uid, info);
//    }
    private ContentValues createApkResponseValues(long uid, ApkResponse info){
    	ContentValues cv = new ContentValues();
    	cv.put(ApkinfoColumns.UID,              uid);
		cv.put(ApkinfoColumns.APKCOMPONENTNAME, info.packagename);
		cv.put(ApkinfoColumns.APKNAME,          info.label);
		cv.put(ApkinfoColumns.APKSTATUS,        info.status);
		cv.put(ApkinfoColumns.APKVERSIONCODE,   info.versioncode);
		cv.put(ApkinfoColumns.APKVERSIONNAME,   info.versionname);
		cv.put(ApkinfoColumns.APKLATESTVERSIONCODE,   info.latest_versioncode);
		cv.put(ApkinfoColumns.APKLATESTVERSIONNAME,   info.latest_versionname);
		cv.put(ApkinfoColumns.APKDESC,          info.description);
		cv.put(ApkinfoColumns.FILEPATH,         info.apkurl);
		cv.put(ApkinfoColumns.Local_FILEPATH,   info.intallpath);
		cv.put(ApkinfoColumns.FILESIZE,         info.apksize);
		cv.put(ApkinfoColumns.APKICON,          info.iconurl);
		cv.put(ApkinfoColumns.PROCESSSTATUS,    info.progress);
		cv.put(ApkinfoColumns.VISIBILITY,       info.visibility);
		cv.put(ApkinfoColumns.APP_USERD,        info.app_used==true?1:0);
		cv.put(ApkinfoColumns.RATIO,            String.valueOf(info.ratio));
		cv.put(ApkinfoColumns.APK_SERVER_ID,    info.apk_server_id);
		cv.put(ApkinfoColumns.PRICE,            String.valueOf(info.price));
		cv.put(ApkinfoColumns.ISFAVORITES,      info.isFavorite==true?1:0);
		cv.put(ApkinfoColumns.ISILIKE,          info.iLike==true?1:0);
		cv.put(ApkinfoColumns.LAST_INSTALLED,   info.last_installed_time);
		
		return cv;
    }

//    public ApkResponse getApkInfoByComponentName(String packageName){
//    	String where =  ApkinfoColumns.APKCOMPONENTNAME +"="+"'"+packageName+"'";
//    	Cursor cursor = mContext.getContentResolver().query(APKINFO_CONTENT_URI, APKINFO_PROJECTION,where , null, null);
//    	ApkResponse apkinfo  = null;
//    	if(cursor !=null) {
//            if (cursor.getCount()>0){
//                cursor.moveToFirst();
//                apkinfo = createApkResponse(cursor);
//            }
//            cursor.close();
//            cursor = null;
//        }
//    	return apkinfo;
//    }
//
//    private boolean isExistedApkinfo(String componentName){
//    	String where =  ApkinfoColumns.APKCOMPONENTNAME +"="+"'"+componentName+"'";
//    	Cursor cursor = mContext.getContentResolver().query(APKINFO_CONTENT_URI, APKINFO_PROJECTION,where , null, null);
//    	boolean result = false;
//    	if(cursor != null){
//    		if(cursor.getCount()>0){
//    			result = true;
//    		}
//    		cursor.close();
//    		cursor = null;
//    	}
//    	Log.d(TAG,"===isExistedApkinfo componentname="+componentName+"==result="+result);
//    	return result;
//    }
//
    public static String getPinyin(String name)
    {
    	if(true)
    	    return getSortKey(name);
    	
    	StringBuilder sb = new StringBuilder();
    	try{
	    	ArrayList<Token> tokens = cjkToPinyin.get(name.toUpperCase());
	    	for(Token item:tokens)
	    	{
	    		sb.append(item.target);
	    	}
    	}catch(Exception ne)
    	{
    		Log.e(TAG, "Fail to get pinyin"+ne.getMessage());
    		sb.append("ZZZZ");
    	}
    	
    	return sb.toString();
    }
    private static  boolean isEmpty(String str)
    {
    	return str == null || str.length() == 0;
    }
    
    public static String getSortKey(String displayName) {
    	if(isEmpty(displayName))
    	{
    		return "ZZZZZZ";
    	}
       
        try{
            ArrayList<Token> tokens = HanziToPinyin.getInstance().get(displayName.toUpperCase());  
            if (tokens != null && tokens.size() > 0) {  
                StringBuilder sb = new StringBuilder(); 
                
                boolean hasDuoyin = false;
                for (Token token : tokens) {  
                    // Put Chinese character's pinyin, then proceed with the  
                    // character itself.
                    
                    if (isEmpty(token.target[0]) == false) {
                        sb.append(token.target[0].toUpperCase());
                    }
                    else
                    {
                        if (isEmpty(token.target[1]) == false) {
                            sb.append(token.target[1].toUpperCase());	                	
                        }
                    }
                    
                    if (hasDuoyin == false && isEmpty(token.target[1]) == false) {
                    	hasDuoyin = true;
                    }
                }  
                
                //first character
                for (Token token : tokens) {
                	if (isEmpty(token.target[0]) == false)
                	{
                        sb.append(token.target[0].toUpperCase().charAt(0));
                	}
                }
                
                for (Token token : tokens) {
                    sb.append(token.source);
                }  
                
                //process for duoyin
                if(hasDuoyin == true)
                {
                	 for (Token token : tokens) {  
                         if (isEmpty(token.target[1]) == false) {
                             sb.append(token.target[1].toUpperCase());
                         }
                         else
                         {
                             if (isEmpty(token.target[0]) == false) {
                                 sb.append(token.target[0].toUpperCase());	                	
                             }
                         }
                     }
                	 
                	 //process for duoyin
                     for (Token token : tokens) {
                     	if (isEmpty(token.target[1]) == false)
                     	{
                             sb.append(token.target[1].toUpperCase().charAt(0));
                     	}
                     	else
                     	{
                     		if (isEmpty(token.target[0]) == false)
                         	{
                                 sb.append(token.target[0].toUpperCase().charAt(0));
                         	}
                     	}
                     }
                }
                
                return sb.toString();  
            }  
        }catch(Exception ne)
        {
            Log.e(TAG, "Fail to get pinyin"+ne.getMessage() + " source="+displayName, ne);
            if(HanziToPinyin.hasChinese)
                return displayName;
            else
                return "ZZZZZZ";
        }
        return displayName;
    }  

    ContentValues createUserInformationValues(QiupuUser userinfo){
    	return QiupuUser.toContentValues(userinfo);
    }
    
    
    public QiupuSimpleUser createSimpleUserInformation(Cursor mCursor) {
        if(LowPerformance)Log.d(TAG, "createSimpleUserInformation enter.");
    	QiupuSimpleUser result = new QiupuSimpleUser();
		result.uid = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.USERID));
		result.name = mCursor.getString(mCursor.getColumnIndex(UsersColumns.USERNAME));
//		result = mCursor.getInt(mCursor.getColumnIndex(UsersColumns.RELATIONSHIP));
		result.nick_name = mCursor.getString(mCursor.getColumnIndex(UsersColumns.NICKNAME));
		result.name_pinyin = mCursor.getString(mCursor.getColumnIndex(UsersColumns.NAME_PINGYIN));
		
//		result.domain=mCursor.getString(mCursor.getColumnIndex(UsersColumns.DOMAIN));
		result.profile_image_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL));
		result.profile_simage_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_SIMAGE_URL));
		result.profile_limage_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_LIMAGE_URL));
		result.location = mCursor.getString(mCursor.getColumnIndex(UsersColumns.LOCATION));
        if(LowPerformance)Log.d(TAG, "createSimpleUserInformation exit.");
		return result;
	}
    
//    public QiupuUser createUserSimpleInformation(Cursor mCursor) {
//    	QiupuUser result = new QiupuUser();
//		result.uid = mCursor.getLong(mCursor.getColumnIndex(UsersColumns.USERID));
//		result.name = mCursor.getString(mCursor.getColumnIndex(UsersColumns.USERNAME));
////		result = mCursor.getInt(mCursor.getColumnIndex(UsersColumns.RELATIONSHIP));
//		result.nick_name = mCursor.getString(mCursor.getColumnIndex(UsersColumns.NICKNAME));
//		result.name_pinyin = mCursor.getString(mCursor.getColumnIndex(UsersColumns.name_pinyin));
//		
//		result.domain=mCursor.getString(mCursor.getColumnIndex(UsersColumns.DOMAIN));
//		result.profile_image_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL));
//		result.profile_simage_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_SIMAGE_URL));
//		result.profile_limage_url = mCursor.getString(mCursor.getColumnIndex(UsersColumns.PROFILE_LIMAGE_URL));
//		result.location = mCursor.getString(mCursor.getColumnIndex(UsersColumns.LOCATION));		
//		return result;
//	}
    

    public QiupuUser createUserInformation(Cursor mCursor) {
        return createUserInformation(mContext, mCursor);
    }

    public static void queryEmailPhones(Context context, QiupuUser user) {
    	user.phoneList = queryUserPhoneOrEmail(context, user.uid, UserProvider.TYPE_PHONE);
        user.emailList = queryUserPhoneOrEmail(context, user.uid, UserProvider.TYPE_EMAIL);
    }
    public static QiupuUser createUserInformation(Context context, Cursor cursor) {
        if (LowPerformance) Log.d(TAG, "createUserInformation enter.");
        QiupuUser result = new QiupuUser();
        QiupuUser.createUserInformation(result, cursor);
        if (LowPerformance) Log.d(TAG, "createUserInformation user info created.");

        result.work_history_list = queryWorkExperience(context, result.uid);
        result.education_list = queryEducation(context, result.uid);
        result.phoneList = queryUserPhoneOrEmail(context, result.uid, UserProvider.TYPE_PHONE);
        result.emailList = queryUserPhoneOrEmail(context, result.uid, UserProvider.TYPE_EMAIL);
        result.friendsImageList = queryUserImage(context, result.uid, UserProvider.TYPE_FRIENDS);
        result.fansImageList = queryUserImage(context, result.uid, UserProvider.TYPE_FANS);
        result.shareImageList = querySharedPhotos(context, result.uid);
        result.perhapsNames = queryPerhapsNames(context, result.uid);
        result.sharedResource = queryShareSources(context, result.uid);
        if (LowPerformance) Log.d(TAG, "createUserInformation exit.");
        return result;
	}    

    public QiupuUser createUserInfoWithOutExpandInfo(Cursor cursor) {
        if (LowPerformance) Log.d(TAG, "createUserInformation enter.");
        QiupuUser result = new QiupuUser();
        QiupuUser.createUserInformation(result, cursor);

        return result;
	}

    /**
	 * friends
	 * @param userlist
	 */
	public void insertFriendsList(List<QiupuUser> userlist){
		Log.d(TAG, "insertFriendsList begin ------ ");
    	SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
    	db.beginTransaction();
    	try {
    		for(int i=0;i<userlist.size();i++){
    			QiupuUser user = userlist.get(i);
    			int count = updateFriendsInfoWithStatus(user, true);
    			if(count <= 0 ) {
    				insertFriendsWhitStatus(user, true);
//    				insertFriendsinfo(user);
    			}
//    			boolean isExist = isUserExist(user.uid);
//    			if(isExist == true){
//    				updateFriendsInfo(user);
//    			}else{
//    			}
    		}
    		Log.d(TAG, "insertFriendsList end ------ ");
    		db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.d(TAG, "insert friends list : " + e.getMessage());
		} finally {
			db.endTransaction();
		}
	}
	
	public void removeInvalidUser() {
		String where = UsersColumns.DB_STATUS + " = " + QiupuUser.USER_STATUS_DEFAULT;
		int count = mContext.getContentResolver().delete(USERS_CONTENT_URI, where, null);
		Log.d(TAG, "removeInvalidUser count: " + count);
        updateLocalDirectory();
	}
	public void revertUserStatus() {
		ContentValues cv = new ContentValues();
		cv.put(UsersColumns.DB_STATUS, QiupuUser.USER_STATUS_DEFAULT);
		int count = mContext.getContentResolver().update(USERS_CONTENT_URI, cv,null,null);
		Log.d(TAG, "revert user count: " + count);
	}
	
	public void insertNotificationList(ArrayList<NotificationInfo> ntfList){
		for(int i=0;i<ntfList.size();i++){
			NotificationInfo info = ntfList.get(i);
			addNtfSetting(info.ntftype, info.ntfvalue);
		}
	}
	
	private long insertFriendsWhitStatus(final QiupuUser qiupuuser, boolean fromList){	
		//delete old work history
	    deleteWorkExperienceInfo(qiupuuser.uid);
	    ArrayList<WorkExperience> work_list = qiupuuser.work_history_list;
	    if(work_list != null && work_list.size()>0) {
	        insertWorkExperienceList(work_list, qiupuuser.uid);
	    }
	    //delete old education history
	    deleteEducationInfo(qiupuuser.uid);
	    ArrayList<Education> edu_list = qiupuuser.education_list;
	    if(edu_list != null && edu_list.size()>0) {
	        insertEducationList(edu_list, qiupuuser.uid);
	    }
	    
	    //delete PhoneEmail info
	    deletePhoneEmailInfo(qiupuuser.uid);
	    insertPhoneEmailList(qiupuuser);
	    
        deleteUserImageInfo(qiupuuser.uid);
        insertUserImageList(qiupuuser);
        
        deleteSharedPhotosInfo(qiupuuser.uid);
        insertSharedPhotoList(qiupuuser);
        
        //update perhaps_name
        bulkInsertPerhapsNameList(qiupuuser);
        
        //TODO if use new thread will resulted query circle user incomplete.
        updateUserCircles(qiupuuser.uid, qiupuuser.circleId);

        ContentValues cv = createUserInformationValues(qiupuuser);
        if(fromList) {
        	cv.put(UsersColumns.DB_STATUS, QiupuUser.USER_STATUS_UPDATED);
        }
        Uri uri = mContext.getContentResolver().insert(USERS_CONTENT_URI, cv);
    	return ContentUris.parseId(uri);
    }
	
	private long insertFriendsinfo(final QiupuUser qiupuuser){	
		return insertFriendsWhitStatus(qiupuuser, false);
    }

//    public long insertQiupuUser(long uid, String name, ArrayList<String> phoneList, ArrayList<String> emailList) {
//        if (isUserExist(uid)) {
//            return updateQiupuUserByNFC(uid, name, phoneList, emailList);
//        } else {
//            return insertQiupuUserByNFC(uid, name, phoneList, emailList);
//        }
//    }
//
//    private long insertQiupuUserByNFC(long uid, String name, ArrayList<String> phoneList, ArrayList<String> emailList) {
//        ContentResolver cr = mContext.getContentResolver();
//        ContentValues values = new ContentValues();
//        values.put(UsersColumns.NICKNAME, name);
//        values.put(UsersColumns.USERID, uid);
//        values.put(UsersColumns.NAME_PINGYIN, getPinyin(name));
//
//        if (phoneList != null) {
//            values = insertPhone(phoneList, values);
//        }
//
//        if (emailList != null) {
//            values = insertEmail(emailList, values);
//        }
//
//        Uri uri = cr.insert(USERS_CONTENT_URI, values);
//        Log.d(TAG, "insertQiupuUserByNFC() insert: _id = " + ContentUris.parseId(uri));
//        return ContentUris.parseId(uri);
//    }
//
//    private int updateQiupuUserByNFC(long uid, String name, ArrayList<String> phoneList, ArrayList<String> emailList) {
//        String where = UsersColumns.USERID + " = " + uid;
//        ContentResolver cr = mContext.getContentResolver();
//        ContentValues values = new ContentValues();
//
//        values.put(UsersColumns.NICKNAME, name);
//        if (phoneList != null) {
//            values = insertPhone(phoneList, values);
//        }
//
//        if (emailList != null) {
//            values = insertEmail(emailList, values);
//        }
//
//        int count = cr.update(USERS_CONTENT_URI, values, where, null);
//        Log.d(TAG, "updateQiupuUserByNFC() update: count = " + count);
//        return count;
//    }
//
//    private ContentValues insertEmail(ArrayList<String> list, ContentValues values) {
//        int size = list.size();
//        switch (size) {
//        case 1:{
//            values.put(UsersColumns.CONTACT_EMAIL1, list.get(0));
//            values.put(UsersColumns.CONTACT_EMAIL2, "");
//            values.put(UsersColumns.CONTACT_EMAIL3, "");
//            break;
//        }
//        case 2:{
//            values.put(UsersColumns.CONTACT_EMAIL1, list.get(0));
//            values.put(UsersColumns.CONTACT_EMAIL2, list.get(1));
//            values.put(UsersColumns.CONTACT_EMAIL3, "");
//            break;
//        }
//        case 3:{
//            values.put(UsersColumns.CONTACT_EMAIL1, list.get(0));
//            values.put(UsersColumns.CONTACT_EMAIL2, list.get(1));
//            values.put(UsersColumns.CONTACT_EMAIL3, list.get(2));
//            break;
//        }
//        default:
//            Log.d(TAG, "error, outOfArrayListBoundary exception.");
//            break;
//        }
//        return values;
//    }
//
//    private ContentValues insertPhone(ArrayList<String> list, ContentValues values) {
//        int size = list.size();
//        switch (size) {
//        case 1:{
//            values.put(UsersColumns.CONTACT_PHONE1, list.get(0));
//            values.put(UsersColumns.CONTACT_PHONE2, "");
//            values.put(UsersColumns.CONTACT_PHONE3, "");
//            break;
//        }
//        case 2:{
//            values.put(UsersColumns.CONTACT_PHONE1, list.get(0));
//            values.put(UsersColumns.CONTACT_PHONE2, list.get(1));
//            values.put(UsersColumns.CONTACT_PHONE3, "");
//            break;
//        }
//        case 3:{
//            values.put(UsersColumns.CONTACT_PHONE1, list.get(0));
//            values.put(UsersColumns.CONTACT_PHONE2, list.get(1));
//            values.put(UsersColumns.CONTACT_PHONE3, list.get(2));
//            break;
//        }
//        default:
//            Log.d(TAG, "error, outOfArrayListBoundary exception.");
//            break;
//        }
//        return values;
//    }

	private int updateFriendsInfoWithStatus(final QiupuUser qiupuuser, boolean fromList) {
		//delete old work history
        if (LowPerformance) Log.d(TAG, "updateFriendsInfo enter.");
        deleteWorkExperienceInfo(qiupuuser.uid);
        ArrayList<WorkExperience> work_list = qiupuuser.work_history_list;
        if(work_list != null && work_list.size()>0) {
            insertWorkExperienceList(work_list, qiupuuser.uid);
        }
        
        //delet old educaiton history
        deleteEducationInfo(qiupuuser.uid);
        ArrayList<Education> edu_list = qiupuuser.education_list;
        if(edu_list != null && edu_list.size()>0) {
            insertEducationList(edu_list, qiupuuser.uid);
        }
        
        //delete PhoneEmail info
        deletePhoneEmailInfo(qiupuuser.uid);
        insertPhoneEmailList(qiupuuser);
        
        deleteUserImageInfo(qiupuuser.uid);
        insertUserImageList(qiupuuser);
        
        deleteSharedPhotosInfo(qiupuuser.uid);
        insertSharedPhotoList(qiupuuser);
        
       //update perhaps_name
        bulkInsertPerhapsNameList(qiupuuser);
        
        ContentValues cv = createUserInformationValues(qiupuuser);
        if(fromList) {
        	cv.put(UsersColumns.DB_STATUS, QiupuUser.USER_STATUS_UPDATED);
        }
    	//TODO if use new thread will resulted query circle user incomplete. 
//    	sWorker.post(new Runnable()
//    	{
//    		public void run()
//    		{
    			//update circle
    			updateUserCircles(qiupuuser.uid, qiupuuser.circleId);
//    		}
//    	});
    	
    	String where =  UsersColumns.USERID +"="+qiupuuser.uid;
    	int count = mContext.getContentResolver().update(USERS_CONTENT_URI, cv,where,null);
        if (LowPerformance) Log.d(TAG, "updateFriendsInfo exit.");
        return count;
	}
	
    public int updateFriendsInfo(final QiupuUser qiupuuser){
    	return updateFriendsInfoWithStatus(qiupuuser, false);
    }
    
    public int updateUserStatus(final QiupuUser qiupuuser){
    	ContentValues cv = new ContentValues();
		cv.put(UsersColumns.STATUS, qiupuuser.status);
        cv.put(UsersColumns.STATUS_TIME, qiupuuser.status_time);
    	
    	String where =  UsersColumns.USERID +"="+qiupuuser.uid;
    	return mContext.getContentResolver().update(USERS_CONTENT_URI, cv,where,null);
    }
    
    public int updateUserRemark(final QiupuUser qiupuuser){
        ContentValues cv = new ContentValues();
        cv.put(UsersColumns.REMARK, qiupuuser.remark);
        
        String where =  UsersColumns.USERID +"="+qiupuuser.uid;
        return mContext.getContentResolver().update(USERS_CONTENT_URI, cv,where,null);
    }
    
    private boolean isUserExist(long uid)
    {
    	boolean ret = false;
    	String where =  UsersColumns.USERID +"="+uid;
    	Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,new String[]{UsersColumns.USERID} , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){    		
    		ret = true;
    	}
    	if(cursor != null)
    		cursor.close();
    	
    	return ret;
    }
    
    private boolean isQiupuAlbumExist(long uid)
    {
    	boolean ret = false;
    	String where =  QiupuAlbumColumns.USERID +"="+uid;
    	Cursor cursor = mContext.getContentResolver().query(QIUPU_ALBUM_URI,new String[]{QiupuAlbumColumns.USERID} , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){    		
    		ret = true;
    	}
    	if(cursor != null)
    		cursor.close();
    	
    	return ret;
    }
    
    private boolean isExistQiupuAlbumByAlumId(long album_id)
    {
    	boolean ret = false;
    	String where =  QiupuAlbumColumns.ALBUM_ID +"="+album_id;
    	Cursor cursor = mContext.getContentResolver().query(QIUPU_ALBUM_URI,new String[]{QiupuAlbumColumns.ALBUM_ID} , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){    		
    		ret = true;
    	}
    	if(cursor != null)
    		cursor.close();
    	
    	return ret;
    }
    
    private boolean isQiupuPhotoExist(long uid,long album_id)
    {
    	boolean ret = false;
    	String where =  QiupuAlbumColumns.USERID +"="+uid + " AND " +QiupuAlbumColumns.ALBUM_ID +"="+album_id ;
    	Cursor cursor = mContext.getContentResolver().query(QIUPU_PHOTO_URI,new String[]{QiupuPhotoColumns.PHOTO_ID} , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){    		
    		ret = true;
    	}
    	if(cursor != null)
    		cursor.close();
    	
    	return ret;
    }
    
//    private boolean isWorkExperienceExist(long uid,long we_id)
//    {
//    	boolean ret = false;
//    	String where =  WorkExperienceColumns.USERID +"="+uid+" and "+WorkExperienceColumns.WE_ID +"="+we_id;
//    	Cursor cursor = mContext.getContentResolver().query(WORK_EXPERIENCE_CONTENT_URI,new String[]{WorkExperienceColumns.USERID} , where, null, null);
//    	if(cursor != null && cursor.getCount() > 0){    		
//    		ret = true;
//    	}
//    	if(cursor != null)
//    		cursor.close();
//    	
//    	return ret;
//    }
    
    public synchronized void insertUserinfo(QiupuUser qiupuuser){
    	boolean isExist = isUserExist(qiupuuser.uid);    	
		if(isExist == true){
			updateFriendsInfo(qiupuuser);
		}
		else
		{
			insertFriendsinfo(qiupuuser);
		}
		
		
//		//删除Db中旧的工作经历
//		deleteWorkExperienceInfo(qiupuuser.uid);
//		ArrayList<WorkExperience> work_list = qiupuuser.work_history_list;
//		if(work_list != null && work_list.size()>0) {
//			insertWorkExperienceList(work_list, qiupuuser.uid);
//		}
    }
    
    public void deleteWorkExperienceInfo(long uid){
    	try{
			String where =  WorkExperienceColumns.USERID +"="+uid;
	    	mContext.getContentResolver().delete(WORK_EXPERIENCE_CONTENT_URI,where,null);
    	}catch(Exception ne){}    
    }

    private void deleteWorkExperienceInfo(String idsSuffix) {
        try {
            String where = WorkExperienceColumns.USERID + idsSuffix;
            mContext.getContentResolver().delete(WORK_EXPERIENCE_CONTENT_URI,where,null);
        } catch (Exception ne) {

        }
    }

    public void deleteEducationInfo(long uid){
    	try{
			String where =  EducationColumns.USERID +"="+uid;
	    	mContext.getContentResolver().delete(EDUCATION_CONTENT_URI,where,null);
    	}catch(Exception ne){}    
    }


    private void deleteEducationInfo(String idsSuffix) {
        try {
            String where = EducationColumns.USERID + idsSuffix;
            mContext.getContentResolver().delete(EDUCATION_CONTENT_URI, where, null);
        } catch (Exception ne) {

        }
    }

    public void deletePhoneEmailInfo(long uid){
    	try{
    		String where =  PhoneEmailColumns.USERID +"="+uid;
    		mContext.getContentResolver().delete(PHONEEMAIL_CONTENT_URI, where, null);
    	}catch(Exception ne){}    
    }

    private void deletePhoneEmailInfo(String idsSuffix) {
    	try {
    		String where = PhoneEmailColumns.USERID + idsSuffix;
    		mContext.getContentResolver().delete(PHONEEMAIL_CONTENT_URI, where, null);
    	} catch (Exception ne) {
    	}
    }
    
    public void deletePhoneEmailInfo(long uid,String type) {
        try {
        	String where = PhoneEmailColumns.USERID + "=" + Long.valueOf(uid) + " and " + PhoneEmailColumns.TYPE + "='" + type + "'";
            mContext.getContentResolver().delete(PHONEEMAIL_CONTENT_URI, where, null);
        } catch (Exception ne) {
        }
    }

    public void deleteUserImageInfo(long uid){
    	try{
    		String where =  UserImageColumns.BELONG_USERID +"="+uid;
    		mContext.getContentResolver().delete(USER_IMAGE_URI, where, null);
    	}catch(Exception ne){}    
    }

    private void deleteUserImageInfo(String idsSuffix) {
        try {
            String where = UserImageColumns.BELONG_USERID + idsSuffix;
            mContext.getContentResolver().delete(USER_IMAGE_URI, where, null);
        } catch (Exception ne) {
        }
    }

    public void deleteQiupuAlbumsInfo(long uid){
    	try{
    		String where =  QiupuAlbumColumns.USERID +"="+uid;
    		mContext.getContentResolver().delete(QIUPU_ALBUM_URI, where, null);
    	}catch(Exception ne){}    
    }
    
    public void deleteQiupuAlbumByAlbumId(long album_id){
    	try{
    		String where =  QiupuAlbumColumns.ALBUM_ID +"="+album_id;
    		mContext.getContentResolver().delete(QIUPU_ALBUM_URI, where, null);
    	}catch(Exception ne){}    
    }
    public void deleteQiupuPhotosInfo(long uid,long album_id){
    	try{
    		String where =  QiupuPhotoColumns.USERID +"="+uid + " AND " + QiupuPhotoColumns.ALBUM_ID +"="+album_id;
    		mContext.getContentResolver().delete(QIUPU_PHOTO_URI, where, null);
    		mContext.getContentResolver().notifyChange(QIUPU_PHOTO_URI, null);
    	}catch(Exception ne){}    
    }
    
    public void deleteQiupuPhotoInfo(long uid,String photo_id){
    	try{
    		String where =  QiupuPhotoColumns.USERID +"="+uid + " AND " + QiupuPhotoColumns.PHOTO_ID +"="+photo_id;
    		mContext.getContentResolver().delete(QIUPU_PHOTO_URI, where, null);
    		mContext.getContentResolver().notifyChange(QIUPU_PHOTO_URI, null);
    	}catch(Exception ne){}    
    }
    
    public void deleteSharedPhotosInfo(long uid){
        try{
            String where =  SharedPhotosColumns.USERID +"="+uid;
            mContext.getContentResolver().delete(SHARED_PHOTOS_URI, where, null);
        }catch(Exception ne){}    
    }

    public void deletePerhapsName(long uid){
        try{
            String where =  PerhapsNameColumns.USERID +"="+uid;
            mContext.getContentResolver().delete(PERHAPS_NAME_URI, where, null);
        }catch(Exception ne){}    
    }

    private void deletePerhapsName(String idsSuffix) {
        try {
            String where = PerhapsNameColumns.USERID + idsSuffix;
            mContext.getContentResolver().delete(PERHAPS_NAME_URI, where, null);
        } catch (Exception ne) {
        }
    }

    public void deleteFriendsinfo(long id){
    	try{
			String where =  UsersColumns.USERID +"="+id;
	    	mContext.getContentResolver().delete(USERS_CONTENT_URI,where,null);
    	}catch(Exception ne){}    
    }

    public QiupuSimpleUser querySimpleUserInfo(long uid) {
        if (LowPerformance) Log.d(TAG, "querySimpleUserInfo enter.");
        QiupuSimpleUser user = null;
        Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,
                SIMPLE_USERS_INFO_PROJECTION, " uid=" + uid, null, " name_pinyin ASC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = createSimpleUserInformation(cursor);
            }
            cursor.close();
        }
        if (LowPerformance) Log.d(TAG, "querySimpleUserInfo exit.");
        return user;
	}


	public boolean setShortCutForUser(long uid, boolean isshortcut)
	{
		boolean ret = false;
        String where = String.format(" uid = %1$s ", uid);
        android.content.ContentValues ct = new android.content.ContentValues();         
        ct.put(UsersColumns.SHORTCUT, isshortcut);
        
        if(mContext.getContentResolver().update(USERS_CONTENT_URI, ct, where, null) > 0)
        {
            ret = true;
        }        
        return ret;
	}

	public boolean setRequestUser(long uid, String requestid)
	{
		boolean ret = false;
        String where = String.format(" uid = %1$s ", uid);
        android.content.ContentValues ct = new android.content.ContentValues();         
        ct.put(UsersColumns.REQUESTED_ID, requestid);
        
        if(mContext.getContentResolver().update(USERS_CONTENT_URI, ct, where, null) > 0)
        {
            ret = true;
        }        
        return ret;
	}
//
//    public Cursor queryAllSimpleUserInfo(){
////    	ArrayList<QiupuSimpleUser> allfriends=new ArrayList<QiupuSimpleUser>();
////    	Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,USERS_INFO_PROJECTION , null, null, " name_pinyin ASC");
////    	if(cursor!=null&&cursor.getCount()>0){
////    		 while(cursor.moveToNext()){
////    			 allfriends.add(createSimpleUserInformation(cursor));
////    		 }
////    	}
////    	cursor.close();
////		cursor = null;
////    	return allfriends;
//        String where = UsersColumns.USERID + " not in (" + AccountServiceUtils.getBorqsAccountID() + ")";
//    	return mContext.getContentResolver().query(USERS_CONTENT_URI,SIMPLE_USERS_INFO_PROJECTION , where, null, " name_pinyin ASC");
//    }

    public Cursor queryAllSimpleUserInfo(){
        if (QiupuApplication.VIEW_MODE_PERSONAL == QiupuApplication.mTopOrganizationId) {
            String where = UsersColumns.USERID + " NOT IN (" + AccountServiceUtils.getBorqsAccountID() + ")";
            return mContext.getContentResolver().query(USERS_CONTENT_URI,SIMPLE_USERS_INFO_PROJECTION , where, null, " name_pinyin ASC");
        } else {
            StringBuffer selectStr = new StringBuffer("SELECT ");
            boolean isFirst = true;
            for(String field : SIMPLE_USERS_INFO_PROJECTION) {
                if(isFirst) {
                    isFirst = false;
                    selectStr.append(" users.");
                }else {
                    selectStr.append(", users.");
                }
                selectStr.append(field);
            }

            for (String field : EmployeeColums.PROJECTION) {
                selectStr.append(", employee.").append(field);
            }

            selectStr.append(" FROM employee LEFT JOIN users ON users.").append(UsersColumns.USERID).
                    append("=employee.").append(EmployeeColums.USER_ID);

            if (QiupuApplication.mTopOrganizationId == QiupuApplication.VIEW_MODE_PERSONAL) {
//            selectStr.append(" WHERE employee.").append(EmployeeColums.OWNER_ID).
//                    append("=-1");
            } else {
                selectStr.append(" WHERE employee.").append(EmployeeColums.OWNER_ID).
                        append("=-1 OR employee.").append(EmployeeColums.OWNER_ID).append("=").
                        append(QiupuApplication.mTopOrganizationId.circleid);
            }

            if (Integer.parseInt(Utilities.getSdkVersion()) > 8) {
                selectStr.append(" GROUP BY employee.").append(EmployeeColums.USER_ID);
            }

            selectStr.append(" ORDER BY employee.").append(EmployeeColums.NAME_PINYIN).
                    append(",users.").append(UsersColumns.NAME_PINGYIN);

            if(UserProvider.mOpenHelper != null) {
                SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
                Cursor result = db.rawQuery(selectStr.toString(), null);
                dumpSimpleUserInfo(selectStr.toString(), result);
                return result;
            }

            return null;
        }
    }
    
    public Cursor querySimpleUsersWithoutIds(String ids){
        StringBuilder filterids = new StringBuilder();
        if(StringUtil.isValidString(ids)) {
            filterids.append(ids);
        }
        if(filterids.length() > 0) {
            filterids.append(",");
        }
        filterids.append(AccountServiceUtils.getBorqsAccountID());
        String where = UsersColumns.USERID + " not in (" + filterids + ")";
        return mContext.getContentResolver().query(USERS_CONTENT_URI,SIMPLE_USERS_INFO_PROJECTION , where, null, " name_pinyin ASC");
    }
    
    
    public Cursor querySearchUserNotInCircle(String circleid, String key){
    	key = key.replace("'", "");
    	SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
    	String sql = "select * from users where (nickname like \'%"+key+"%\'" +
    			" or name_pinyin like \'%"+key+"%\')" +
    			" and uid not in "+ getCircleUserWhere(circleid, true) + "  order by name_pinyin asc" ;
    	return db.rawQuery(sql, null);
    }
    
    public Cursor querySearchUserInCircle(String circleid, String key){
        key = key.replace("'", "");
    	SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
    	String sql = "select * from users where (nickname like \'%"+key+"%\'" +
    	              " or name_pinyin like \'%"+key+"%\')" +
    	              " and uid in "+ getCircleUserWhere(circleid, false) + "  order by name_pinyin asc" ;
    	return db.rawQuery(sql, null);
    }
    
    public Cursor queryUserNotInCircle(String circleid){
//    	ArrayList<QiupuSimpleUser> allfriends=new ArrayList<QiupuSimpleUser>();
    	String where =  UsersColumns.USERID + " not in " + getCircleUserWhere(circleid, true) ;
//    	Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,USERS_INFO_PROJECTION , where, null, " name_pinyin ASC");
//    	if(cursor != null && cursor.getCount() > 0){
//    		 while(cursor.moveToNext()){  
//    			 allfriends.add(createSimpleUserInformation(cursor));  
//    		 }  
//    	}
//    	if(cursor != null)
//    	{
//            cursor.close();
//		    cursor = null;
//    	}
//    	return allfriends;
    	return mContext.getContentResolver().query(USERS_CONTENT_URI,SIMPLE_USERS_INFO_PROJECTION , where, null, " name_pinyin ASC");
    }
    
//    public int removeAllUsers(){
//    	return mContext.getContentResolver().delete(USERS_CONTENT_URI,null, null);
//    }
//
    public int removeAllCirclesWithOutNativeCircles() {
        StringBuilder filterIds = new StringBuilder();
        filterIds.append(CircleUtils.getNativeCircleIds());
        String eventids = getEventIds();
        if(eventids.length() > 0) {
            filterIds.append(",").append(eventids);
        }
        String where = CircleColumns.CIRCLE_ID + " not in (" + filterIds.toString() + ")";
        String groupwhere = GroupColumns.CIRCLE_ID + " not in (" + eventids + ")";
        mContext.getContentResolver().delete(CIRCLE_GROUP_URI, groupwhere, null);
        return mContext.getContentResolver().delete(CIRCLES_CONTENT_URI, where, null);
    }
    
    public int removeAllCircles() {
        mContext.getContentResolver().delete(CIRCLE_GROUP_URI, null, null);
        return mContext.getContentResolver().delete(CIRCLES_CONTENT_URI, null, null);
    }

    public QiupuUser queryOneUserInfo(long userid){
//        if (LowPerformance) Log.d(TAG, "queryOneUserInfo enter.");
        QiupuUser user = queryOneUserInfo(mContext, userid);
//        if (LowPerformance) Log.d(TAG, "queryOneUserInfo exit.");
        return user;
    }
    
    final static String[] USERS_CIRCLE_INFO_PROJECTION={		
		UsersColumns.CIRCLE_ID,
		UsersColumns.CIRCLE_NAME,		
	};
    
    public Cursor queryOneUserCircleInfo(long userid){
    	String where =  UsersColumns.USERID +"="+userid;
    	return mContext.getContentResolver().query(USERS_CONTENT_URI,USERS_CIRCLE_INFO_PROJECTION , where, null, null);    	
    }
    
    public String queryOneUserCircleIds(long userid){
        if (LowPerformance) Log.d(TAG, "queryOneUserCircleIds enter.");
    	String where =  UsersColumns.USERID +"="+userid;
    	Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,new String[]{UsersColumns.CIRCLE_ID} , where, null, null);
    	String circleIds = ""; 
    	if(cursor != null) {
            if (cursor.moveToFirst()) {
            	circleIds = cursor.getString(cursor.getColumnIndex(UsersColumns.CIRCLE_ID));	
            }
            cursor.close();
        }
        if (LowPerformance) Log.d(TAG, "queryOneUserCircleIds exit.");
    	return circleIds;
    }

    public String getUserName(long uid) {
        String nickName = "";
        String where = UsersColumns.USERID + "=" + uid;
        String[] USERS_NickName_PROJECTION = {
                UsersColumns.USERID,
                UsersColumns.NICKNAME,
        };
        Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI, USERS_NickName_PROJECTION, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                nickName = cursor.getString(cursor.getColumnIndex(UsersColumns.NICKNAME));
            }
            cursor.close();
            cursor = null;
        }

        return nickName;
    }
    
    public String getCircleName(long cid) {
        String name = "";
        String where = CircleColumns.CIRCLE_ID + "=" + cid;
        Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, new String[]{CircleColumns.CIRCLE_NAME}, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
            }
            cursor.close();
            cursor = null;
        }

        return name;
    }

    public String getUserImageUrl(long userid) {
        if(LowPerformance)Log.d(TAG, "getUserImageUrl enter.");
        String url = mPhotoUrls.getUrl(userid);
        if(LowPerformance)Log.d(TAG, "getUserImageUrl exit.");
        return url;
    }

    public boolean cacheUserImageUrl(long uId, String path) {
        return mPhotoUrls.cache(uId,  path);
    }
	
    /**
     * settings
     * @param name
     * @return
     */
    
	public String getSettingValue(String name) {
        String va = null;
        String where = SettingsCol.Name +"='"+name+"'";
        Cursor cursor = mContext.getContentResolver().query(SETTINGS_CONTENT_URI,settingsProject,where, null, null);
        if(cursor != null)
        {
        	if(cursor.moveToFirst()){
        		va = cursor.getString(cursor.getColumnIndex(SettingsCol.Value));
        	}
            cursor.close();
        }
        return va;
    }
	
	
	public boolean getNotificationValue(String name){
		String value = getNtfSettingValue(name);
		if(value == null || value.equals("")) {
			return true;
		}
		
		if(QiupuConfig.DBLOGD)Log.d(TAG, "getFriendsApkInfoLastSyncTime time:"+value);
		
		return Integer.valueOf(value) == 0?true:false;
	}
	 
	 
	public String getNtfSettingValue(String name) {
        String va = null;
        String where = SettingsCol.Name +"='"+name+"'";
        Cursor cursor = mContext.getContentResolver().query(NOTIFICATION_SETTINGS_CONTENT_URI,settingsProject,where, null, null);
        if(cursor != null)
        {
        	if(cursor.moveToFirst()){
        		va = cursor.getString(cursor.getColumnIndex(SettingsCol.Value));
        	}
            cursor.close();
            cursor = null;
        }
        return va;
    }

    public static String getSettingValue(Context con, String name) {
        return getSettingValue(con, SETTINGS_CONTENT_URI, name);
    }

	public static String getSettingValue(Context con, Uri settingUri, String name) {
        String va = null;
        String where = SettingsCol.Name +"='"+name+"'";
        Cursor cursor = con.getContentResolver().query(settingUri, settingsProject,where, null, null);
        if(cursor != null)
        {
        	if(cursor.moveToFirst()){
        		va = cursor.getString(cursor.getColumnIndex(SettingsCol.Value));
        	}
            cursor.close();
            cursor = null;
        }
        return va;
    }
    
    public boolean removeSetting(String name) {
    	return removeSetting(mContext, name);
    }
    
    public static boolean removeSetting(Context context, String name) {
        int ret = -1;
        try{
            ret = context.getContentResolver().delete(SETTINGS_CONTENT_URI, " name='"+name+"'", null);
        }catch(SQLiteException ne){}
        return ret > 0;
    } 
    
    public Uri addSetting(String name, String value) {
        return addSetting(mContext, name, value);
    }

    public static Uri addSetting(Context context, String name, String value) {
        return addSetting(context, SETTINGS_CONTENT_URI, name, value);
    }

    private static Uri addSetting(Context context, Uri settingUri, String name, String value) {
        Uri ret = null;
        android.content.ContentValues ct = new android.content.ContentValues();
        ct.put(SettingsCol.Name, name);              
        ct.put(SettingsCol.Value, value);
        //if exist, update
        if(null != getSettingValue(context, settingUri, name))
        {
            updateSetting(context, settingUri, name, value);
        }
        else
        {        
            ret = context.getContentResolver().insert(settingUri, ct);
        }
        
        return ret;
    } 

    public static boolean updateSetting(Context context, Uri settingUri, String name, String value) {
        boolean ret = false;
        String where = String.format(" name = \"%1$s\" ", name);
        android.content.ContentValues ct = new android.content.ContentValues();         
        ct.put(SettingsCol.Value, value);
        
        if(context.getContentResolver().update(settingUri, ct, where, null) > 0)
        {
            ret = true;
        }        
        return ret;
    }

    public Uri addNtfSetting(String name, String value) {
        Uri ret = null;
        android.content.ContentValues ct = new android.content.ContentValues();
        ct.put(SettingsCol.Name, name);              
        ct.put(SettingsCol.Value, value);
        //if exist, update
        if(!StringUtil.isEmpty(getNtfSettingValue(name)))
        {
        	updateNtfSetting(name, value);
        }
        else
        {        
            ret = mContext.getContentResolver().insert(NOTIFICATION_SETTINGS_CONTENT_URI, ct); 
        }
        return ret;
    } 
    
    public boolean updateNtfSetting(String name, String value) {
        boolean ret = false;
        String where = String.format(" name = \"%1$s\" ", name);
        android.content.ContentValues ct = new android.content.ContentValues();         
        ct.put(SettingsCol.Value, value);
        
        if(mContext.getContentResolver().update(NOTIFICATION_SETTINGS_CONTENT_URI, ct, where, null) > 0)
        {
            ret = true;
        }        
        return ret;
    }

    public boolean showRightMoveGuide() {
    	String value = getSettingValue(IS_SHOW_RIGHT_MOVE_GUIDE);
    	if(value == null || value.equals("")) {
    		return true;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "showRightMoveGuide :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
    }
    
    public void setShowRightMoveGuide(boolean set) {
    	addSetting(IS_SHOW_RIGHT_MOVE_GUIDE, set==true?"1":"0");
    }
    
    public void setUserApkInfoLastSyncTime(){
    	addSetting(LAST_SYNC_USERAPKINFO_TIME, String.valueOf(System.currentTimeMillis()));
    }
    public void setUserInfoLastSyncTime(){
    	addSetting(LAST_SYNC_USERINFO_TIME,String.valueOf(System.currentTimeMillis()));
    }
    public void setRequestLastSyncTime(){
    	addSetting(LAST_SYNC_REQUESTS_TIME,String.valueOf(System.currentTimeMillis()));
    }

    public void setEventsLastSyncTime(){
    	addSetting(LAST_SYNC_EVENTS_TIME,String.valueOf(System.currentTimeMillis()));
    }
    
    public void setCircleLastSyncTime(){
    	addSetting(LAST_SYNC_CIRCLE_TIME, String.valueOf(System.currentTimeMillis()));
    }
    
    
    public void setApplicationAutoUploadBackupSettings(int value) {
        addSetting(AUTO_UPLOAD_APP_BACKUP, String.valueOf(value));
    }
    
    public void setDefaultThemeId(long id) {
        addSetting(DEFAULT_THEME_ID, String.valueOf(id));
    }
    
    public long getDefaultThemeId() {
    	String value = getSettingValue(DEFAULT_THEME_ID);
    	if(TextUtils.isEmpty(value)) {
    		return -1;
    	}

    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getDefaultThemeId :"+value);

    	return Long.parseLong(value);
	}
    
    public int getApplicationAutoUploadBackupSettings() {
    	String value = getSettingValue(AUTO_UPLOAD_APP_BACKUP);
    	if(TextUtils.isEmpty(value)) {
    		return -1;
    	}

    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getApplicationAutoUploadBackupSettings time:"+value);

    	return Integer.valueOf(value);
	}
    
    public static boolean isShowScreenSnap = false;
    public static int     getIsShowScreenSnap = -1;
    public boolean showApkScreenSnap() {		
    	if(getIsShowScreenSnap == -1)
    	{
	    	String value = getSettingValue(SHOW_PIC);
	    	if(value == null || value.equals("")) {
	    		getIsShowScreenSnap = 0;
	    		isShowScreenSnap = false;
	    		
	    		return false;
	    	}
	    	
	    	if(QiupuConfig.DBLOGD)Log.d(TAG, "showApkScreenSnap :"+value);
	    	
	    	getIsShowScreenSnap = 1;
	    	isShowScreenSnap = Integer.valueOf(value) == 1?true:false;
	    	
	    	return isShowScreenSnap;
    	}
    	else
    	{
    		return isShowScreenSnap;
    	}
	}

    /*
     * default to use Browser download
     */
    public boolean isUsingDownloadService() {		
    	 String value = getSettingValue("isUsingDownloadService");
         if(value == null || value.equals("")) {
        	if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO)
     		{
                return true;
     		}else
     		{
     			return false;
     		}
         }

         if(QiupuConfig.DBLOGD)Log.d(TAG, "isUsingDownloadService :"+value);
         return Integer.valueOf(value) == 1;
	}

    public boolean isUsingTestURL() {		
    	String value = getSettingValue(SELECT_BORQS_TEST_URL);
    	if(TextUtils.isEmpty(value)) {
    		return false;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isUsingTestURL :"+value);
    	
    	return Integer.valueOf(value) != 0?true:false;
	}

    public boolean showLinkMarket()
    {
    	String value = getSettingValue(SHOW_LKE_MARKET);
    	if(value == null || value.equals("")) {
    		return true;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "showLinkMarket :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
    }
    
    public void setShowApkScreenSnap(boolean set) {		
    	addSetting(SHOW_PIC, set==true?"1":"0");
    	isShowScreenSnap = set;
	}
    
    public void setLinkMarket(boolean set) {		
    	addSetting(SHOW_LKE_MARKET, set==true?"1":"0");
	}

    public boolean isEnableAutoIntallation() {		
    	String value = getSettingValue(AUTO_INSTALL_DOWNLOAD);
    	if(value == null || value.equals("")) {
    		return true;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isEnableAutoIntallation :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
	}	
    
    public boolean isEnableAutoShare() {		
    	String value = getSettingValue(AUTO_SHARE_DYNAMIC);
    	if(value == null || value.equals("")) {
    		return false;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isEnableAutoIntallation :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
	}	
    
    public boolean isEnableNotification() {		
    	String value = getSettingValue(ENABLE_NOTIFICATION);
    	if(value == null || value.equals("")) {
    		return false;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isEnableNotification :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
	}
    
    public boolean isEnableGetNotification() {		
    	String value = getSettingValue(ENABLE_GET_NOTIFICATION);
    	if(value == null || value.equals("")) {
    		return true;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isEnableGetNotification :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
	}
    
	 public boolean isEnableVibrate() {		
    	String value = getSettingValue(ENABLE_NOTIFICATION_VIBRATE);
    	if(value == null || value.equals("")) {
    		return false;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isEnableVibrate :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
	}
    
    public void setEnableAutoIntallation(boolean enable) {		
    	addSetting(AUTO_INSTALL_DOWNLOAD, enable==true?"1":"0");
	}	
    
    public void setEnableNotification(boolean enable)
    {
    	addSetting(ENABLE_NOTIFICATION, enable==true?"1":"0");
    }

    public void setEnableGetNotification(boolean enable)
    {
    	addSetting(ENABLE_GET_NOTIFICATION, enable==true?"1":"0");
    }
    
    public void setEnableVibrate(boolean enable)
    {
    	addSetting(ENABLE_NOTIFICATION_VIBRATE, enable==true?"1":"0");
    }
    
    public void setEnableAutoShare(boolean enable)
    {
    	addSetting(AUTO_SHARE_DYNAMIC, enable==true?"1":"0");
    }

    public boolean isShowMarketAppPage() {		
    	String value = getSettingValue(SHOW_MARKET_APP_PAGE);
    	if(value == null || value.equals("")) {
    		return false;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isShowMarketAppPage :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
	}	
    
    
    public void setShowMarketAppPage(boolean enable) {		
    	addSetting(SHOW_MARKET_APP_PAGE, enable==true?"1":"0");
	}	
    
    
    //days
    public void setFriendsInterval(int internal) {
    	addSetting(LAST_GET_FRIENDS, String.valueOf(internal));    	
	}
    
    public int getFriendsInterval() {
    	String value = getSettingValue(LAST_GET_FRIENDS);
    	if(value == null || value.equals("")) {
    		return 3;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getFriendsInterval time:"+value);
    	
    	return Integer.valueOf(value);
	}
    
    public long getEventsLastSyncTime(){
    	String value = getSettingValue(LAST_SYNC_EVENTS_TIME);
    	if(value == null || value.equals("")) {
    		return 0;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getEventsLastSyncTime time:"+value);
    	
    	return Long.valueOf(value);
    }
    
    public long getCircleLastSyncTime(){
    	String value = getSettingValue(LAST_SYNC_CIRCLE_TIME);
    	if(value == null || value.equals("")) {
    		return 0;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getCircleLastSyncTime time:"+value);
    	
    	return Long.valueOf(value);
    }
    
    public int getApkPermission() {
        String value = getSettingValue(APK_PERMISSION);
        if(value == null || value.equals("")) {
            return -1;
        }
        
        if(QiupuConfig.DBLOGD)Log.d(TAG, "getApkPermission time:"+value);
        
        return Integer.valueOf(value);
    }
    
    public void setApkPermission(int internal) {
        addSetting(APK_PERMISSION, String.valueOf(internal));        
    }
    
    
    //minutes
    public void setRequestsInterval(int internal) {
    	addSetting(INTERVAL_GET_REQUESTS, String.valueOf(internal));    	
	}
    
    //default is sex hour
    public int getRequestsInterval() {
    	String value = getSettingValue(INTERVAL_GET_REQUESTS);
    	if(value == null || value.equals("")) {
    		return 60 * 6;
    	}
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getFriendsInterval time:"+value);
    	
    	return Integer.valueOf(value);
	}

    public long getUserInfoLastSyncTime(){
    	String value = getSettingValue(LAST_SYNC_USERINFO_TIME);
    	if(value == null || value.equals("")) {
    		return 0;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getUserInfoLastSyncTime time:"+value);
    	
    	return Long.valueOf(value);
    }
    
    public long getRequstsLastSyncTime(){
    	String value = getSettingValue(LAST_SYNC_REQUESTS_TIME);
    	if(value == null || value.equals("")) {
    		return 0;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getRequstsLastSyncTime time:"+value);
    	
    	return Long.valueOf(value);
    }
    
    public long getUserApkInfoLastSyncTime(){
    	String value = getSettingValue(LAST_SYNC_USERAPKINFO_TIME);
    	if(value == null || value.equals("")) {
    		return 0;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "getUserApkInfoLastSyncTime time:"+value);
    	
    	return Long.valueOf(value);
    }

	public QiupuSimpleUser getLastSimpleQiupuUser() {
        Cursor cursor      = null;
        QiupuSimpleUser quser = null;
        try
        {
            cursor = mContext.getContentResolver().query(USERS_CONTENT_URI, SIMPLE_USERS_INFO_PROJECTION, null, null, null);       
            if(cursor != null)
            {    
                while(cursor.moveToLast())
                {
                	quser = this.createSimpleUserInformation(cursor);
                    break;
                }
                cursor.close();
            }
       }
       catch(SQLiteException ne)
       {}
       finally
       {
           if(cursor != null)
           {
              cursor.close();
           }
       }
       return quser;
	}

	public ArrayList<String> getUserImagesByuid() {
    	 String[] UsersImageProject = {UsersColumns.PROFILE_IMAGE_URL};
        Cursor cursor = null;
        ArrayList<String> ls = new ArrayList<String>();
        try {
            cursor = mContext.getContentResolver().query(USERS_CONTENT_URI, UsersImageProject, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String img = cursor.getString(cursor.getColumnIndexOrThrow(UsersColumns.PROFILE_IMAGE_URL));
                        if (img != null) {
                            ls.add(img);
                        }
                    } while (cursor.moveToNext());
                }
	         }
    	 }
 	     catch(SQLiteException ne)
 	     {}
 	     finally
 	     {
 		    if(cursor != null)
 		    {
 		       cursor.close();
 		    }
 	     }
         return ls;
	}

	public ArrayList<UserCircle> getUserCircles(long userid){
		ArrayList<UserCircle> allCircle = new ArrayList<UserCircle>();
//		String where = CircleColumns.USERID + "=" + userid;
        String where = CircleColumns.TYPE + " in (0, 1)";
		Cursor cursor = null ;
		try
		{
			cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,USER_CIRCLE_PROJECTION, where, null, " circleid ASC");       
			if(cursor != null && cursor.getCount()>0)
			{    
			    cursor.moveToFirst();
			    do {
			        allCircle.add(createCircleInformation(cursor));
                } while (cursor.moveToNext());
			}
		}
		catch(SQLiteException ne)
		{}
		finally
		{
			if(cursor != null)
			{
				cursor.close();
				cursor = null;
			}
		}
		return allCircle;
	}
	
	private static ContentValues createCircleInformationValues(UserCircle userCircle){
		ContentValues cv = new ContentValues();
		cv.put(CircleColumns.USERID, userCircle.uid);
		cv.put(CircleColumns.CIRCLE_ID, userCircle.circleid);
		cv.put(CircleColumns.CIRCLE_NAME, userCircle.name);
		cv.put(CircleColumns.MEMBER_COUNT, userCircle.memberCount);
		cv.put(CircleColumns.TYPE, userCircle.type);
		return cv;
	}
	
	private static ContentValues createGroupInformationValues(UserCircle userCircle) {
		ContentValues cv = new ContentValues();
		cv.put(GroupColumns.CIRCLE_ID, userCircle.circleid);
		
		cv.put(GroupColumns.PROFILE_IMAGE_URL, userCircle.profile_image_url);
		cv.put(GroupColumns.PROFILE_SIMAGE_URL, userCircle.profile_simage_url);
		cv.put(GroupColumns.PROFILE_LIMAGE_URL, userCircle.profile_limage_url);
		cv.put(GroupColumns.COMPANY, userCircle.company);
		cv.put(GroupColumns.OFFICE_ADDRESS, userCircle.office_address);
		cv.put(GroupColumns.DEPARTMENT, userCircle.department);
		cv.put(GroupColumns.JOB_TITLE, userCircle.jobtitle);
		cv.put(GroupColumns.LOCATION, userCircle.location);
		cv.put(GroupColumns.DESCRIPTION, userCircle.description);
		cv.put(GroupColumns.INVITED_COUNT, userCircle.invitedMembersCount);
		cv.put(GroupColumns.APPLIED_COUNT, userCircle.applyedMembersCount);
		
		cv.put(GroupColumns.FORMAL_CIRCLE_COUNT, userCircle.formalCirclesCount);
	    cv.put(GroupColumns.FREE_CIRCLE_COUNT, userCircle.freeCirclesCount);
		if(userCircle.mGroup != null) {
		    cv.put(GroupColumns.MEMBER_LIMIT, userCircle.mGroup.member_limit);
		    cv.put(GroupColumns.IS_STREAM_PUBLIC, userCircle.mGroup.is_stream_public);
		    cv.put(GroupColumns.CAN_SEARCH, userCircle.mGroup.can_search);
		    cv.put(GroupColumns.CAN_VIEW_MEMBERS, userCircle.mGroup.can_view_members);
		    cv.put(GroupColumns.CAN_JOIN, userCircle.mGroup.can_join);
		    cv.put(GroupColumns.CAN_MEMBER_INVITE, userCircle.mGroup.can_member_invite);
		    cv.put(GroupColumns.CAN_MEMBER_APPROVE, userCircle.mGroup.can_member_approve);
		    cv.put(GroupColumns.CAN_MEMBER_POST, userCircle.mGroup.can_member_post);
		    cv.put(GroupColumns.CAN_MEMBER_QUIT, userCircle.mGroup.can_member_quit);
		    cv.put(GroupColumns.NEED_INVITE_CONFIRM, userCircle.mGroup.need_invite_confirm);
		    cv.put(GroupColumns.CREATED_TIME, userCircle.mGroup.created_time);
		    cv.put(GroupColumns.UPDATED_TIME, userCircle.mGroup.updated_time);
		    cv.put(GroupColumns.DESTROYED_TIME, userCircle.mGroup.destroyed_time);
		    cv.put(GroupColumns.ROLE_IN_GROUP, userCircle.mGroup.role_in_group);
		    cv.put(GroupColumns.VIEWER_CAN_UPDATE, userCircle.mGroup.viewer_can_update ? 1 : 0);
		    cv.put(GroupColumns.VIEWER_CAN_DESTROY, userCircle.mGroup.viewer_can_destroy ? 1 : 0);
		    cv.put(GroupColumns.VIEWER_CAN_REMOVE, userCircle.mGroup.viewer_can_remove ? 1 : 0);
		    cv.put(GroupColumns.VIEWER_CAN_GRANT, userCircle.mGroup.viewer_can_grant ? 1 : 0);
		    cv.put(GroupColumns.VIEWER_CAN_QUIT, userCircle.mGroup.viewer_can_quit ? 1 : 0);
		    cv.put(GroupColumns.BULLETIN, userCircle.mGroup.bulletin);
		    cv.put(GroupColumns.BULLETIN_UPDATED_TIME, userCircle.mGroup.bulletin_updated_time);
		    cv.put(GroupColumns.INVITE_IDS, userCircle.mGroup.invited_ids);
		    cv.put(GroupColumns.START_TIME, userCircle.mGroup.startTime);
		    cv.put(GroupColumns.END_TIME, userCircle.mGroup.endTime);
		    cv.put(GroupColumns.COVER_URL, userCircle.mGroup.coverUrl);
		    cv.put(GroupColumns.REPEAT_TYPE, userCircle.mGroup.repeat_type);
		    cv.put(GroupColumns.REMINDER_TIME, userCircle.mGroup.reminder_time);
		    cv.put(GroupColumns.TOP_POST_NAME, userCircle.mGroup.top_post_name);
		    cv.put(GroupColumns.TOP_POST_COUNT, userCircle.mGroup.top_post_count);
		    cv.put(GroupColumns.TOP_POLL_COUNT, userCircle.mGroup.top_poll_count);
		    if(userCircle.mGroup.creator != null) {
		    	cv.put(GroupColumns.CREATOR_ID, userCircle.mGroup.creator.uid);
		    	cv.put(GroupColumns.CREATOR_IMAGEURL, userCircle.mGroup.creator.profile_image_url);
		    	cv.put(GroupColumns.CREATOR_NAME, userCircle.mGroup.creator.nick_name);
		    }
		    cv.put(GroupColumns.PAGE_ID, userCircle.mGroup.pageid);
		    cv.put(GroupColumns.FORMAL, userCircle.mGroup.formal);
		    cv.put(GroupColumns.SUBTYPE, userCircle.mGroup.subtype);
		    cv.put(GroupColumns.PARENT_ID, userCircle.mGroup.parent_id);
		    cv.put(GroupColumns.EVENT_COUNT, userCircle.mGroup.event_count);
		}
		return cv;
    }
    
    private static Group createGroupInformation(Cursor cursor, UserCircle circle) {
		Group result = new Group();
		circle.profile_image_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_IMAGE_URL));
		circle.profile_simage_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_SIMAGE_URL));
		circle.profile_limage_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_LIMAGE_URL));
		circle.company = cursor.getString(cursor.getColumnIndex(GroupColumns.COMPANY));
		circle.office_address = cursor.getString(cursor.getColumnIndex(GroupColumns.OFFICE_ADDRESS));
		circle.department = cursor.getString(cursor.getColumnIndex(GroupColumns.DEPARTMENT));
		circle.jobtitle = cursor.getString(cursor.getColumnIndex(GroupColumns.JOB_TITLE));
		circle.location = cursor.getString(cursor.getColumnIndex(GroupColumns.LOCATION));
		circle.description = cursor.getString(cursor.getColumnIndex(GroupColumns.DESCRIPTION));
		
		result.member_limit = cursor.getInt(cursor.getColumnIndex(GroupColumns.MEMBER_LIMIT));
		result.is_stream_public = cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_STREAM_PUBLIC));
		result.can_search = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_SEARCH));
		result.can_view_members = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_VIEW_MEMBERS));
		result.can_join = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_JOIN));
		result.can_member_invite = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_MEMBER_INVITE));
		result.can_member_approve = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_MEMBER_APPROVE));
		result.can_member_post = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_MEMBER_POST));
		result.can_member_quit = cursor.getInt(cursor.getColumnIndex(GroupColumns.CAN_MEMBER_QUIT));
		result.need_invite_confirm = cursor.getInt(cursor.getColumnIndex(GroupColumns.NEED_INVITE_CONFIRM));
		int role = cursor.getInt(cursor.getColumnIndex(GroupColumns.ROLE_IN_GROUP));
		if(role <= 0) {
			result.role_in_group = -1;
		}else {
			result.role_in_group = role;
		}
		result.viewer_can_destroy = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_DESTROY)) == 1 ? true : false;
		result.viewer_can_grant = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_GRANT)) == 1 ? true : false;
		result.viewer_can_remove = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_REMOVE)) == 1 ? true : false;
		result.viewer_can_update = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_UPDATE)) == 1 ? true : false;
		result.viewer_can_quit = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_QUIT)) == 1 ? true : false;
		result.bulletin = cursor.getString(cursor.getColumnIndex(GroupColumns.BULLETIN));
		result.bulletin_updated_time = cursor.getLong(cursor.getColumnIndex(GroupColumns.BULLETIN_UPDATED_TIME));
		result.invited_ids = cursor.getString(cursor.getColumnIndex(GroupColumns.INVITE_IDS));
		result.startTime = cursor.getLong(cursor.getColumnIndex(GroupColumns.START_TIME));
		result.endTime = cursor.getLong(cursor.getColumnIndex(GroupColumns.END_TIME));
		result.coverUrl = cursor.getString(cursor.getColumnIndex(GroupColumns.COVER_URL));
		result.creator = new QiupuSimpleUser();
		result.creator.uid = cursor.getLong(cursor.getColumnIndex(GroupColumns.CREATOR_ID));
		result.creator.nick_name = cursor.getString(cursor.getColumnIndex(GroupColumns.CREATOR_NAME));
		result.creator.profile_image_url = cursor.getString(cursor.getColumnIndex(GroupColumns.CREATOR_IMAGEURL));
		result.repeat_type = cursor.getInt(cursor.getColumnIndex(GroupColumns.REPEAT_TYPE));
		result.reminder_time = cursor.getInt(cursor.getColumnIndex(GroupColumns.REMINDER_TIME));
		result.top_post_name = cursor.getString(cursor.getColumnIndex(GroupColumns.TOP_POST_NAME));
		result.top_post_count = cursor.getInt(cursor.getColumnIndex(GroupColumns.TOP_POST_COUNT));
		result.top_poll_count = cursor.getInt(cursor.getColumnIndex(GroupColumns.TOP_POLL_COUNT));
		result.pageid = cursor.getLong(cursor.getColumnIndex(GroupColumns.PAGE_ID));
		result.formal = cursor.getInt(cursor.getColumnIndex(GroupColumns.FORMAL));
		result.subtype = cursor.getString(cursor.getColumnIndex(GroupColumns.SUBTYPE));
		result.parent_id = cursor.getLong(cursor.getColumnIndex(GroupColumns.PARENT_ID));
		result.event_count = cursor.getInt(cursor.getColumnIndex(GroupColumns.EVENT_COUNT));
		
		circle.invitedMembersCount = cursor.getInt(cursor.getColumnIndex(GroupColumns.INVITED_COUNT));
		circle.applyedMembersCount = cursor.getInt(cursor.getColumnIndex(GroupColumns.APPLIED_COUNT));
		circle.formalCirclesCount = cursor.getInt(cursor.getColumnIndex(GroupColumns.FORMAL_CIRCLE_COUNT));
		circle.freeCirclesCount = cursor.getInt(cursor.getColumnIndex(GroupColumns.FREE_CIRCLE_COUNT));
		
		return result;
	}
    
    private static Group createEventGroupInfo(Cursor cursor, UserCircle circle) {
		Group result = new Group();
//		circle.profile_image_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_IMAGE_URL));
//		circle.profile_simage_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_SIMAGE_URL));
//		circle.profile_limage_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_LIMAGE_URL));
		circle.location = cursor.getString(cursor.getColumnIndex(GroupColumns.LOCATION));
		result.bulletin = cursor.getString(cursor.getColumnIndex(GroupColumns.BULLETIN));
		result.startTime = cursor.getLong(cursor.getColumnIndex(GroupColumns.START_TIME));
		result.endTime = cursor.getLong(cursor.getColumnIndex(GroupColumns.END_TIME));
		result.coverUrl = cursor.getString(cursor.getColumnIndex(GroupColumns.COVER_URL));
		result.creator = new QiupuSimpleUser();
		result.creator.uid = cursor.getLong(cursor.getColumnIndex(GroupColumns.CREATOR_ID));
		result.creator.nick_name = cursor.getString(cursor.getColumnIndex(GroupColumns.CREATOR_NAME));
		result.creator.profile_image_url = cursor.getString(cursor.getColumnIndex(GroupColumns.CREATOR_IMAGEURL));
		return result;
	}
	
    public static UserCircle createCircleInformation(Cursor cursor) {
    	if (null != cursor) {
    		UserCircle result = new UserCircle();
    		result.uid = cursor.getLong(cursor.getColumnIndex(CircleColumns.USERID));
    		result.circleid = cursor.getLong(cursor.getColumnIndex(CircleColumns.CIRCLE_ID));
    		result.name = cursor.getString(cursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
    		result.memberCount = cursor.getInt(cursor.getColumnIndex(CircleColumns.MEMBER_COUNT));
    		result.type = cursor.getInt(cursor.getColumnIndex(CircleColumns.TYPE));
    		return result;
    	}
    	
    	Log.e(TAG, "createCircleInformation, unexpected null cursor.");
    	return null;
    }
    
    public static UserCircle parseSimpleCircleInfo(Cursor cursor) {
    	if (null != cursor) {
    		UserCircle result = new UserCircle();
    		int index = cursor.getColumnIndex(CircleColumns.CIRCLE_ID);
            if (index >= 0) {
            	result.circleid = cursor.getLong(index);
            }
            if (result.circleid <= 0) {
                index = cursor.getColumnIndex(CircleCirclesColumns.CIRCLEID);
                if (index >= 0) {
                	result.circleid = cursor.getLong(index);
                } else {
                    Log.e(TAG, "parserCursor, failed to parse circleid.");
                }
            }
            
            index = cursor.getColumnIndex(CircleColumns.CIRCLE_NAME);
            if (index >= 0) {
            	result.name = cursor.getString(index);
            }
            if (TextUtils.isEmpty(result.name)) {
                index = cursor.getColumnIndex(CircleCirclesColumns.CIRCLE_NAME);
                if (index >= 0) {
                	result.name = cursor.getString(index);
                } else {
                    Log.e(TAG, "parserCursor, failed to parse circlename.");
                }
            }
            
            result.type = UserCircle.CIRLCE_TYPE_PUBLIC;
    		return result;
    	}
    	
    	Log.e(TAG, "createCircleInformation, unexpected null cursor.");
    	return null;
    }
    
    public static UserCircle parseSimpleEventInfo(Cursor cursor) {
    	if (null != cursor) {
    		UserCircle result = new UserCircle();
    		int index = cursor.getColumnIndex(CircleEventsColumns.EVENT_ID);
            if (index >= 0) {
            	result.circleid = cursor.getLong(index);
            }
            if (result.circleid <= 0) {
                index = cursor.getColumnIndex(CircleColumns.CIRCLE_ID);
                if (index >= 0) {
                	result.circleid = cursor.getLong(index);
                } else {
                    Log.e(TAG, "parserCursor, failed to parse eventid.");
                }
            }
            
            index = cursor.getColumnIndex(CircleColumns.CIRCLE_NAME);
            if (index >= 0) {
            	result.name = cursor.getString(index);
            }
            if (TextUtils.isEmpty(result.name)) {
                index = cursor.getColumnIndex(CircleEventsColumns.EVENT_NAME);
                if (index >= 0) {
                	result.name = cursor.getString(index);
                } else {
                    Log.e(TAG, "parserCursor, failed to parse circlename.");
                }
            }
            
            result.type = UserCircle.CIRCLE_TYPE_EVENT;
    		return result;
    	}
    	
    	Log.e(TAG, "createCircleInformation, unexpected null cursor.");
    	return null;
    }
    
	public static UserCircle createCircleInformationWithImage(Cursor cursor) {
        if (null != cursor) {
            UserCircle result = new UserCircle();
            result.uid = cursor.getLong(cursor.getColumnIndex(CircleColumns.USERID));
            result.circleid = cursor.getLong(cursor.getColumnIndex(CircleColumns.CIRCLE_ID));
            result.name = cursor.getString(cursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
            result.memberCount = cursor.getInt(cursor.getColumnIndex(CircleColumns.MEMBER_COUNT));
            result.type = cursor.getInt(cursor.getColumnIndex(CircleColumns.TYPE));
            result.profile_limage_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_LIMAGE_URL));
            result.profile_image_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_IMAGE_URL));
            result.profile_simage_url = cursor.getString(cursor.getColumnIndex(GroupColumns.PROFILE_SIMAGE_URL));
            result.mGroup = new Group();
            result.mGroup.formal = cursor.getInt(cursor.getColumnIndex(GroupColumns.FORMAL));
            return result;
        }

        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}
	
	public static UserCircle createCircleAllInformation(Context context, Cursor cursor) {
        if (null != cursor) {
        	UserCircle result = new UserCircle();
        	result.uid = cursor.getLong(cursor.getColumnIndex(CircleColumns.USERID));
        	result.circleid = cursor.getLong(cursor.getColumnIndex(CircleColumns.CIRCLE_ID));
        	result.name = cursor.getString(cursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
        	result.memberCount = cursor.getInt(cursor.getColumnIndex(CircleColumns.MEMBER_COUNT));
        	result.type = cursor.getInt(cursor.getColumnIndex(CircleColumns.TYPE));
        	
        	result.sharedResource = queryShareSources(context, result.circleid);
            result.phoneList = queryUserPhoneOrEmail(context, result.circleid, UserProvider.TYPE_PHONE);
            result.emailList = queryUserPhoneOrEmail(context, result.circleid, UserProvider.TYPE_EMAIL);
            
            result.inMembersImageList = queryUserImage(context, result.circleid, UserProvider.IMAGE_TYPE_IN_MEMBER);
            result.applyedMembersList = queryUserImage(context, result.circleid, UserProvider.IMAGE_TYPE_APPLY_MEMBER);
            result.invitedMembersList = queryUserImage(context, result.circleid, UserProvider.IMAGE_TYPE_INVITE_MEMBER);
            result.formalCirclesList = queryUserImage(context, result.circleid, UserProvider.IMAGE_TYPE_FORMAL_CIRCLE);
            result.freeCirclesList = queryUserImage(context, result.circleid, UserProvider.IMAGE_TYPE_FREE_CIRCLE);
            result.simpleEventList = queryLastestCircleEvents(result.circleid, context);
            result.simplePoll = queryLimitCirclePollList(context,String.valueOf(result.circleid));
        	result.mGroup = queryOneGroup(context, result);
        	result.categories = queryCategories(context, String.valueOf(result.circleid));
            return result;
        }

        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}
	
	public static UserCircle createPublicCircleListInfo(Context context, Cursor cursor) {
        if (null != cursor) {
//        	UserCircle result = createCircleInformation(cursor);
        	UserCircle result = createCircleInformationWithImage(cursor);
//        	result.mGroup = new Group();
//        	result.mGroup.viewer_can_destroy = queryGroupCanDelete(context, result);
            return result;
        }

        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}
	
	public static boolean queryGroupCanDelete(Context context, UserCircle circle) {
		boolean result = false ;
        String where = GroupColumns.CIRCLE_ID +"="+circle.circleid;
    	Cursor cursor = context.getContentResolver().query(CIRCLE_GROUP_URI, new String[]{GroupColumns.VIEWER_CAN_DESTROY} , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
    		 while(cursor.moveToNext()){  
    			 result = cursor.getInt(cursor.getColumnIndex(GroupColumns.VIEWER_CAN_DESTROY)) == 1 ? true : false;
    		 }  
    	}
    	if(cursor != null)
    	{
            cursor.close();
		    cursor = null;
    	}
    	return result;
	}
	
	public static UserCircle createEventListInformation(Context context, Cursor cursor) {
        if (null != cursor) {
        	UserCircle result = new UserCircle();
        	result.uid = cursor.getLong(cursor.getColumnIndex(CircleColumns.USERID));
        	result.circleid = cursor.getLong(cursor.getColumnIndex(CircleColumns.CIRCLE_ID));
        	result.name = cursor.getString(cursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
        	result.memberCount = cursor.getInt(cursor.getColumnIndex(CircleColumns.MEMBER_COUNT));
        	result.type = cursor.getInt(cursor.getColumnIndex(CircleColumns.TYPE));
        	
        	result.mGroup = queryEventSimpleGroup(context, result);
            return result;
        }

        Log.e(TAG, "createEventListInformation, unexpected null cursor.");
		return null;
	}
	
	public static Group queryOneGroup(Context context, UserCircle circle) {
		Group group = null;
        String where = GroupColumns.CIRCLE_ID +"="+circle.circleid;
    	Cursor cursor = context.getContentResolver().query(CIRCLE_GROUP_URI, GROUP_PROJECTION , where, null, null);
    	if(cursor != null){
    		 if(cursor.moveToFirst()){  
    			 group = createGroupInformation(cursor, circle);  
    		 }  
    		 cursor.close();
    		 cursor = null;
    	}
    	return group;
	}
	
	public static Group queryEventSimpleGroup(Context context, UserCircle circle) {
		Group group = null;
        String where = GroupColumns.CIRCLE_ID +"="+circle.circleid;
    	Cursor cursor = context.getContentResolver().query(CIRCLE_GROUP_URI, GROUP_PROJECTION , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
    		 while(cursor.moveToNext()){  
    			 group = createEventGroupInfo(cursor, circle);  
    		 }  
    	}
    	if(cursor != null)
    	{
            cursor.close();
		    cursor = null;
    	}
    	return group;
	}
	
	private static ContentValues createWorkExperienceInformation(WorkExperience we){
		ContentValues cv = new ContentValues();
		cv.put(WorkExperienceColumns.USERID, we.uid);
		cv.put(WorkExperienceColumns.WORK_FROM, we.from);
		cv.put(WorkExperienceColumns.WORK_TO, we.to);
		cv.put(WorkExperienceColumns.COMPANY, we.company);
		cv.put(WorkExperienceColumns.DEPARTMENT, we.department);
		cv.put(WorkExperienceColumns.JOB_TITLE, we.job_title);
		cv.put(WorkExperienceColumns.OFFICE_ADDRESS, we.office_address);
		cv.put(WorkExperienceColumns.JOB_DESCRIPTION, we.job_description);
		return cv;
	}
	public static WorkExperience createWorkExperienceInformation(Cursor cursor) {
        if (null != cursor) {
            WorkExperience result = new WorkExperience();
            result.uid = cursor.getLong(cursor.getColumnIndex(WorkExperienceColumns.USERID));
            result.from = cursor.getString(cursor.getColumnIndex(WorkExperienceColumns.WORK_FROM));
            result.to = cursor.getString(cursor.getColumnIndex(WorkExperienceColumns.WORK_TO));
            result.company = cursor.getString(cursor.getColumnIndex(WorkExperienceColumns.COMPANY));
            result.department = cursor.getString(cursor.getColumnIndex(WorkExperienceColumns.DEPARTMENT));
            result.office_address = cursor.getString(cursor.getColumnIndex(WorkExperienceColumns.OFFICE_ADDRESS));
            result.job_title = cursor.getString(cursor.getColumnIndex(WorkExperienceColumns.JOB_TITLE));
            result.job_description = cursor.getString(cursor.getColumnIndex(WorkExperienceColumns.JOB_DESCRIPTION));
            return result;
        }

        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}
	private static ContentValues createEducationInformation(Education edu){
		ContentValues cv = new ContentValues();
		cv.put(EducationColumns.USERID, edu.uid);
		cv.put(EducationColumns.EDU_FROM, edu.from);
		cv.put(EducationColumns.EDU_TO, edu.to);
		cv.put(EducationColumns.SCHOOL, edu.school);
		cv.put(EducationColumns.TYPE, edu.type);
		cv.put(EducationColumns.SCHOOL_CLASS, edu.school_class);
		cv.put(EducationColumns.SCHOOL_LOCATION, edu.school_location);
		cv.put(EducationColumns.DEGREE, edu.degree);
		cv.put(EducationColumns.MAJOR, edu.major);
		return cv;
	}
	public static Education createEducationInformation(Cursor cursor) {
        if (null != cursor) {
        	Education result = new Education();
            result.uid = cursor.getLong(cursor.getColumnIndex(EducationColumns.USERID));
            result.from = cursor.getString(cursor.getColumnIndex(EducationColumns.EDU_FROM));
            result.to = cursor.getString(cursor.getColumnIndex(EducationColumns.EDU_TO));
            result.school = cursor.getString(cursor.getColumnIndex(EducationColumns.SCHOOL));
            result.type = cursor.getString(cursor.getColumnIndex(EducationColumns.TYPE));
            result.school_class = cursor.getString(cursor.getColumnIndex(EducationColumns.SCHOOL_CLASS));
            result.school_location = cursor.getString(cursor.getColumnIndex(EducationColumns.SCHOOL_LOCATION));
            result.degree = cursor.getString(cursor.getColumnIndex(EducationColumns.DEGREE));
            result.major = cursor.getString(cursor.getColumnIndex(EducationColumns.MAJOR));
            return result;
        }

        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}

	public void insertWorkExperienceList(ArrayList<WorkExperience> welist,long userid){
		for(int i=0;i<welist.size();i++){
			WorkExperience we = welist.get(i);
//			we.uid = userid;
//			WorkExperience dbWe = queryOneWorkExperience(we.uid, we.we_id);
//			if(dbWe != null){
//				updateWorkExperienceInfo(we);
//			}else{
				insertWorkExperienceInfo(we);
//			}
		}
	}
	public void insertEducationList(ArrayList<Education> edulist,long userid){
		for(int i=0;i<edulist.size();i++){
			Education edu = edulist.get(i);
			insertEducationInfo(edu);
		}
	}
//	public int updateWorkExperienceInfo(WorkExperience we){
//    	ContentValues cv = createWorkExperienceInformation(we);
//    	String where =  WorkExperienceColumns.USERID +"="+we.uid +" and "+WorkExperienceColumns.WE_ID +"="+we.we_id;
//    	return mContext.getContentResolver().update(WORK_EXPERIENCE_CONTENT_URI, cv, where, null);
//    }
	public long insertWorkExperienceInfo(WorkExperience we){	
    	ContentValues cv = createWorkExperienceInformation(we);
    	Uri uri = mContext.getContentResolver().insert(WORK_EXPERIENCE_CONTENT_URI, cv);
    	return ContentUris.parseId(uri);
    }
	public long insertEducationInfo(Education edu){	
    	ContentValues cv = createEducationInformation(edu);
    	Uri uri = mContext.getContentResolver().insert(EDUCATION_CONTENT_URI, cv);
    	return ContentUris.parseId(uri);
    }
	public void insertCircleList(ArrayList<UserCircle> circlelist,long userid){
		for(int i=0;i<circlelist.size();i++){
			UserCircle circle = circlelist.get(i);
			circle.uid = userid;
//			UserCircle dbcircle = queryOneCircle(circle.uid,circle.circleid);
			if(isCircleExist(circle.circleid)){
				updateCircleInfo(circle);
			}else{
				insertCircleInfo(circle);
			}
		}
	}
	
	public void insertOneCircle(UserCircle circle){
//		UserCircle dbcircle = queryOneCircle(circle.uid,circle.circleid);
		if(isCircleExist(circle.circleid)){
			updateCircleInfo(circle);
		}else{
			insertCircleInfo(circle);
		}
	}
	
	public int updateCircleInfo(UserCircle circle){
        if (LowPerformance) Log.d(TAG, "updateCircleInfo enter.");
		//update circle table
		ContentValues cv = createCircleInformationValues(circle);
    	String where = CircleColumns.CIRCLE_ID +"="+circle.circleid;
    	int count = mContext.getContentResolver().update(CIRCLES_CONTENT_URI, cv, where, null);
    	
    	//update group table
    	if(UserCircle.CIRLCE_TYPE_PUBLIC == circle.type || UserCircle.CIRCLE_TYPE_EVENT == circle.type) {
    		ContentValues groupCv = createGroupInformationValues(circle);
    		String selection = GroupColumns.CIRCLE_ID + "=" + circle.circleid;
    		mContext.getContentResolver().update(CIRCLE_GROUP_URI, groupCv, selection, null);

    		final String idsSuffix = " = " + circle.circleid;
    		circle.uid = circle.circleid; //TODO insert to phone Email list, QiupuAccountInfo has only uid.
    		updatePhoneEmailInfoDB(idsSuffix, circle);
    		updateShareSourceDB(circle.circleid, circle.sharedResource);
    		
    		deleteUserImageInfo(circle.circleid);
            insertPubclicPeopleImageList(circle);
    	}
    	
    	if(UserCircle.CIRLCE_TYPE_PUBLIC == circle.type) {
    		insertSimpleCircleEvent(circle.circleid, circle.simpleEventList);
    		insertSimpleCirclePoll(circle.simplePoll);
    		insertCategories(circle.categories);
    	}
    	
        if (LowPerformance) Log.d(TAG, "updateCircleInfo exit.");
    	return count;
    	
    	
    }
	
	public long insertCircleInfo(UserCircle userCircle){	

		// insert to circle table
		ContentValues cv = createCircleInformationValues(userCircle);
    	Uri uri = mContext.getContentResolver().insert(CIRCLES_CONTENT_URI, cv);
    	
    	// insert to group table
    	if(UserCircle.CIRLCE_TYPE_PUBLIC == userCircle.type || UserCircle.CIRCLE_TYPE_EVENT == userCircle.type) {
    		ContentValues groupCv = createGroupInformationValues(userCircle);
    		mContext.getContentResolver().insert(CIRCLE_GROUP_URI, groupCv);
    		final String idsSuffix = " = " + userCircle.circleid;
    		userCircle.uid = userCircle.circleid; //TODO insert to phone Email list, QiupuAccountInfo has only uid.
    		updatePhoneEmailInfoDB(idsSuffix, userCircle);
    		updateShareSourceDB(userCircle.circleid, userCircle.sharedResource);
    		
    		deleteUserImageInfo(userCircle.circleid);
    		insertPubclicPeopleImageList(userCircle);
    		
    		if(UserCircle.CIRLCE_TYPE_PUBLIC == userCircle.type) {
        		insertSimpleCircleEvent(userCircle.circleid, userCircle.simpleEventList);
        		insertSimpleCirclePoll(userCircle.simplePoll);
        		//insert circle's category
        		insertCategories(userCircle.categories);
        	}
    	}
        
    	return ContentUris.parseId(uri);
    }
	
	private void updatePhoneEmailInfoDB(String idsSuffix, QiupuAccountInfo info) {
        if (DEBUG) Log.d(TAG, "updatePhoneEmailInfoDB, deleting phone/email info.");
        deletePhoneEmailInfo(idsSuffix);
        ArrayList<ContentValues> phoneEmailInfo = new ArrayList<ContentValues>();
        buildPhoneEmailInfo(phoneEmailInfo, info);
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing phone/email info: " + phoneEmailInfo.size());
        bulkInsertPhoneEmailInfo(phoneEmailInfo);
	}
	
	public void deleteCircleByCricleId(long uid, String circleId)
	{
		// delete circle info from circle table
		String where = CircleColumns.CIRCLE_ID +"='" + circleId+"'";
    	int count = mContext.getContentResolver().delete(CIRCLES_CONTENT_URI, where,null);
    	
    	//delete group info from group table
    	String groupWhere = GroupColumns.CIRCLE_ID +"=" + circleId;
    	mContext.getContentResolver().delete(CIRCLE_GROUP_URI, groupWhere,null);
    	if(count <=0)
    	{
    		Log.d(TAG, "Fail to delete circle from database="+circleId);
    	}
	}
	
	public void updateUserInfoInCircle(long uid, String circleId, String circleName)
	{
		ArrayList<QiupuUser> usersInCircle = queryFriendsByCircleId(circleId);
		for(int i=0; i<usersInCircle.size(); i++)
		{
			QiupuUser tmpuser = usersInCircle.get(i);
			if(tmpuser.circleId.equals(circleId))
			{
				deleteFriendsinfo(tmpuser.uid);
			}
			else{
				String[] circleIds = tmpuser.circleId.split(",");
				String[] circleNames = tmpuser.circleName.split(",");
				StringBuilder idbuilder = new StringBuilder();
				StringBuilder namebuilder = new StringBuilder();
				for(int j=0; j<circleIds.length; j++)
				{
					if(!circleIds[j].equals(circleId))
					{
						if(idbuilder.length() > 0)
						{
							idbuilder.append(",");
						}
						idbuilder.append(circleIds[j]);
					}
				}
				for(int j=0; j<circleNames.length; j++)
				{
					if(!circleNames[j].equals(circleName))
					{
						if(namebuilder.length() > 0)
						{
							namebuilder.append(",");
						}
						namebuilder.append(circleNames[j]);
					}
				}
				tmpuser.circleId = idbuilder.toString();
				tmpuser.circleName = namebuilder.toString();
				updateFriendsInfo(tmpuser);
			}
		}
		String where =  /*CircleColumns.USERID +"='"+uid +"' and " + */CircleColumns.CIRCLE_ID +"='" + circleId+"'";
    	int count = mContext.getContentResolver().delete(CIRCLES_CONTENT_URI, where,null);
    	
    	if(count <=0)
    	{
    		Log.d(TAG, "updateUserInfoInCircle Fail to delete circle from database="+circleId);
    	}
	}
	
//	public WorkExperience queryOneWorkExperience(long userid,long we_id){
//    	WorkExperience we = null;
//    	String where =  WorkExperienceColumns.USERID +"="+userid+" and " + WorkExperienceColumns.WE_ID +"="+we_id;
//    	Cursor cursor = mContext.getContentResolver().query(WORK_EXPERIENCE_CONTENT_URI,WORK_EXPERIENCE_PROJECTION , where, null, null);
//    	if(cursor != null && cursor.getCount() > 0){
//    		 while(cursor.moveToNext()){  
//    			 we = createWorkExperienceInformation(cursor);  
//    		 }  
//    	}
//    	if(cursor != null)
//    	{
//            cursor.close();
//		    cursor = null;
//    	}
//    	return we;
//    }

    public static ArrayList<WorkExperience> queryWorkExperience(Context context, long userid){
		ArrayList<WorkExperience> work_list = new ArrayList<WorkExperience>();
		String where =  WorkExperienceColumns.USERID +"="+userid;
    	Cursor cursor = context.getContentResolver().query(WORK_EXPERIENCE_CONTENT_URI,WORK_EXPERIENCE_PROJECTION , where, null, " _id ASC");
    	if(cursor != null) {
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    work_list.add(createWorkExperienceInformation(cursor));
                }
            }
            cursor.close();
        }
    	return work_list;
    }

    public static ArrayList<Education> queryEducation(Context context, long userid){
		ArrayList<Education> edu_list = new ArrayList<Education>();
		String where =  EducationColumns.USERID +"="+userid;
    	Cursor cursor = context.getContentResolver().query(EDUCATION_CONTENT_URI,EDUCATION_PROJECTION , where, null, " _id ASC");
    	if(cursor !=null) {
            if (cursor.getCount()>0) {
                while(cursor.moveToNext()) {
                    edu_list.add(createEducationInformation(cursor));
                }
            }
            cursor.close();
        }
    	return edu_list;
    }
    
    private static ArrayList<PhoneEmailInfo> queryUserPhoneOrEmail(Context context, long userid, int type){
    	ArrayList<PhoneEmailInfo> phoneList = new ArrayList<PhoneEmailInfo>();
    	String where =  PhoneEmailColumns.USERID +"="+userid + " and " + PhoneEmailColumns.PHONE_OR_EMAIL + "=" + type;
    	Cursor cursor = context.getContentResolver().query(PHONEEMAIL_CONTENT_URI, PHONE_EMAIL_PROJECTION , where, null, null);
    	if(cursor !=null) {
    		if (cursor.getCount()>0) {
    			while(cursor.moveToNext()) {
    				phoneList.add(createPhoneEmailInformation(cursor));
    			}
    		}
    		cursor.close();
    	}
    	return phoneList;
    }
    private static ArrayList<UserImage> queryUserImage(Context context, long userid, int type){
    	ArrayList<UserImage> userList = new ArrayList<UserImage>();
    	String where =  UserImageColumns.BELONG_USERID +"="+userid + " and " + UserImageColumns.TYPE + "=" + type;
    	Cursor cursor = context.getContentResolver().query(USER_IMAGE_URI, USER_IMAGE_PROJECTION , where, null, null);
    	if(cursor !=null) {
    		if (cursor.getCount()>0) {
    			while(cursor.moveToNext()) {
    				userList.add(createUserImageInformation(cursor));
    			}
    		}
    		cursor.close();
    	}
    	return userList;
    }
    
    public static ArrayList<InfoCategory> queryCategories(Context context, final String circleid) {
    	ArrayList<InfoCategory> categoryList = new ArrayList<InfoCategory>();
    	String where = CategoryColumns.SCOPE_ID + " = " + circleid;
    	Cursor cursor = context.getContentResolver().query(CATEGORY_CONTENT_URI, CATEGORY_PROJECTION, where, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				categoryList.add(creatCategoryInformation(cursor));
				} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	return categoryList;
    }
    
    private static InfoCategory creatCategoryInformation(Cursor cursor) {
    	if (null != cursor) {
    		InfoCategory result = new InfoCategory();
    		result.categoryId = cursor.getLong(cursor.getColumnIndex(CategoryColumns.CATEGORY_ID));
    		result.categoryName = cursor.getString(cursor.getColumnIndex(CategoryColumns.CATEGORY_NAME));
    		result.creatorId = cursor.getLong(cursor.getColumnIndex(CategoryColumns.CREATOR_ID));
    		result.scopeId = cursor.getLong(cursor.getColumnIndex(CategoryColumns.SCOPE_ID));
    		result.scopeName = cursor.getString(cursor.getColumnIndex(CategoryColumns.SCOPE_NAME));
    		return result;
    	}
    	
    	Log.e(TAG, "createPhoneEmailInformation, unexpected null cursor.");
    	return null;
    }
    
    private static ArrayList<SharedPhotos> querySharedPhotos(Context context, long userid){
    	ArrayList<SharedPhotos> photoList = new ArrayList<SharedPhotos>();
    	String where =  SharedPhotosColumns.USERID +"="+userid;
    	Cursor cursor = context.getContentResolver().query(SHARED_PHOTOS_URI, SHARED_PHOTOS_PROJECTION , where, null, null);
    	if(cursor !=null) {
    		if (cursor.getCount()>0) {
    			while(cursor.moveToNext()) {
    				photoList.add(createSharedPhotosInformation(cursor));
    			}
    		}
    		cursor.close();
    	}
    	return photoList;
    }
    
    public static ArrayList<QiupuAlbum> queryQiupuAlbums(Context context, long userid){
    	ArrayList<QiupuAlbum> list = new ArrayList<QiupuAlbum>();
    	String where =  QiupuAlbumColumns.USERID +"="+userid;
    	Cursor cursor = context.getContentResolver().query(QIUPU_ALBUM_URI, QIUPU_ALBUM_PROJECTION , where, null, null);
    	if(cursor !=null) {
    		if (cursor.getCount()>0) {
    			while(cursor.moveToNext()) {
    				list.add(createQiupuAlbumInformation(cursor));
    			}
    		}
    		cursor.close();
    	}
    	return list;
    }
    public static QiupuAlbum queryQiupuAlbumById(Context context, long album_id){
    	QiupuAlbum album = null;
    	String where =  QiupuAlbumColumns.ALBUM_ID +"="+album_id;
    	Cursor cursor = context.getContentResolver().query(QIUPU_ALBUM_URI, QIUPU_ALBUM_PROJECTION , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
   		 while(cursor.moveToNext()){  
   			album = createQiupuAlbumInformation(cursor);  
   		 }  
    	}
	   	if(cursor != null)
	   	{
	           cursor.close();
			    cursor = null;
	   	}
    	return album;
    }
    
    public static Cursor queryQiupuPhotosCursor(Context context, long userid,long album_id){
    	ArrayList<QiupuPhoto> list = new ArrayList<QiupuPhoto>();
    	String where =  QiupuPhotoColumns.USERID +"="+userid + " AND " + QiupuPhotoColumns.ALBUM_ID +"="+album_id;
    	return context.getContentResolver().query(QIUPU_PHOTO_URI, QIUPU_PHOTO_PROJECTION , where, null, null);
//        Cursor cursor = context.getContentResolver().query(QIUPU_PHOTO_URI, QIUPU_PHOTO_PROJECTION , where, null, null);
//        if(cursor !=null) {
//            if (cursor.getCount()>0) {
//                while(cursor.moveToNext()) {
//                	list.add(createQiupuPhotoInformation(cursor));
//                }
//            }
//            cursor.close();
//        }
//        return list;
    }
    
    public static ArrayList<QiupuPhoto> queryQiupuPhotos(Context context, long userid,long album_id){
        ArrayList<QiupuPhoto> list = new ArrayList<QiupuPhoto>();
        String where =  QiupuPhotoColumns.USERID +"="+userid + " AND " + QiupuPhotoColumns.ALBUM_ID +"="+album_id;
        Cursor cursor = context.getContentResolver().query(QIUPU_PHOTO_URI, QIUPU_PHOTO_PROJECTION , where, null, null);
        if(cursor !=null) {
            if (cursor.getCount()>0) {
                while(cursor.moveToNext()) {
                	list.add(createQiupuPhotoInformation(cursor));
                }
            }
            cursor.close();
        }
        return list;
    }
    
    private static ArrayList<PerhapsName> queryPerhapsNames(Context context, long userid){
        ArrayList<PerhapsName> perhapsnames = new ArrayList<PerhapsName>();
        String where =  PerhapsNameColumns.USERID +"="+userid;
        Cursor cursor = context.getContentResolver().query(PERHAPS_NAME_URI, PERHAPS_NAME_PROJECTION , where, null, null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    perhapsnames.add(createPerhapsNameInformation(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;
        }
        return perhapsnames;
    }
    
    //TODO used to show simple info
	public UserCircle queryOneCircle(long userid,long circleid){
    	UserCircle circle = null;
    	String where = CircleColumns.CIRCLE_ID +"="+circleid;
    	Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,USER_CIRCLE_PROJECTION , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
    		 while(cursor.moveToNext()){  
    			 circle = createCircleInformation(cursor);  
    		 }  
    	}
    	if(cursor != null)
    	{
            cursor.close();
		    cursor = null;
    	}
    	return circle;
    }
	
	public boolean isCircleExist(long circleid) {
		boolean ret = false;
		String where = CircleColumns.CIRCLE_ID +"="+circleid;
		Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,new String[]{CircleColumns.ID} , where, null, null);
		if(cursor != null) {
			if(cursor.getCount() > 0) {
				ret = true;
				cursor.close();
				cursor = null;
			}  
		}
		return ret;
	}
	
	//TODO used to show group detail info
	public UserCircle queryOneCircleWithGroup(long circleid){
    	UserCircle circle = null;
        String where = CircleColumns.CIRCLE_ID +"="+circleid;
    	Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,USER_CIRCLE_PROJECTION , where, null, null);
    	if(cursor != null){
    		if(cursor.moveToFirst()) {
    			circle = createCircleAllInformation(mContext, cursor);  
    		}
    		cursor.close();
    		cursor = null;
    	}
    	return circle;
    }
	
	// query all friends by circle id 
    public ArrayList<QiupuUser> queryFriendsByCircleId(String circleid)
    {
    	ArrayList<QiupuUser> allfriends=new ArrayList<QiupuUser>();
    	String where = UsersColumns.USERID + " in " + getCircleUserWhere(circleid, false);
    	
    	Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,USERS_INFO_PROJECTION , where, null, " name_pinyin ASC");
    	if(cursor != null && cursor.getCount() > 0){
    		 while(cursor.moveToNext()){  
    			 allfriends.add(createUserInformation(cursor));  
    		 }  
    	}
    	if(cursor != null)
    	{
            cursor.close();
		    cursor = null;
    	}
    	return allfriends;
    }

    // query all friends by circle id 
    public Cursor queryFriendsCursorByCircleId(String circleid) {
        String where = UsersColumns.USERID + " in " + getCircleUserWhere(circleid, false);
        return mContext.getContentResolver().query(USERS_CONTENT_URI, USERS_INFO_PROJECTION, where, null, " name_pinyin ASC");
    }

    public Cursor queryFriendsCursorByCircleId(String circleId, String filterText) {
        if (TextUtils.isEmpty(filterText)) {
            return queryFriendsCursorByCircleId(circleId);
        } else {
            return searchFriendsCursorByCircleId(circleId, filterText);
        }
    }

    // query friends which have been sent card.
    public Cursor queryFriendsHaveSentCard(String circleid) {
        String selection = "(" + UsersColumns.REQUESTED_ID + " = 1 AND "
                + UsersColumns.PROFILE_PRIVACY + " = 1 ) OR "
                + UsersColumns.PROFILE_PRIVACY + " = 0 ";
        String where = UsersColumns.USERID + " in " + getCircleUserWhere(circleid, false) + " AND (" + selection + ")";
        return mContext.getContentResolver().query(USERS_CONTENT_URI,
                USERS_INFO_PROJECTION, where, null, " name_pinyin ASC");
    }

    public Cursor queryFriendsHaveReceivedCard(String circleid) {
        String selection = UsersColumns.PROFILE_PRIVACY + " = 0 ";
        String where = UsersColumns.USERID + " in " + getCircleUserWhere(circleid, false) + " AND (" + selection + ")";
        return mContext.getContentResolver().query(USERS_CONTENT_URI,
                USERS_INFO_PROJECTION, where, null, " name_pinyin ASC");
    }

    private Cursor queryFriendsCursorSortKeyByCircleId(String circleid)
    {
    	String where = UsersColumns.USERID + " in " + getCircleUserWhere(circleid, false);
    	return  mContext.getContentResolver().query(USERS_CONTENT_URI,new String[]{"name_pinyin"} , where, null, " name_pinyin ASC");
    }

    public Cursor queryFriendsCursor() {
        StringBuffer sb = new StringBuffer();
        sb.append(UsersColumns.USERID).append(" NOT IN (").
                append(AccountServiceUtils.getBorqsAccountID()).append(")");
//        String where = UsersColumns.USERID + " not in (" + AccountServiceUtils.getBorqsAccountID() + ")";
        return mContext.getContentResolver().query(USERS_CONTENT_URI, USERS_INFO_PROJECTION, sb.toString(), null, " name_pinyin ASC");
    }

    public Cursor queryFriendsCursor(String filterText) {
        if (TextUtils.isEmpty(filterText)) {
            return queryFriendsCursor();
        } else {
            return searchFriendsCursor(filterText);
        }
    }
    
    public Cursor queryPagesCursor(String filterText) {
        if (TextUtils.isEmpty(filterText)) {
            return queryAllSimplePages();
        } else {
            return searchPagesCursor(filterText);
        }
    }

    String[] CIRCLE_USERSID_PROJECT = new String[]{CircleUsersColumns.ID, CircleUsersColumns.USERID};

    private String getCircleUserWhere(String circleid, boolean isNeedMyself)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("(");
    	Cursor cursor = mContext.getContentResolver().query(CIRCLE_USERS_CONTENT_URI, CIRCLE_USERSID_PROJECT, CircleUsersColumns.CIRCLE_ID + "="+circleid, null, null);
    	if(cursor != null) {
            if (cursor.getCount() > 0)
            {
                while(cursor.moveToNext()){
                    if(sb.length() > 1)
                        sb.append(", ");
                    sb.append(cursor.getLong(cursor.getColumnIndex(CircleUsersColumns.USERID)));
                }
                if(isNeedMyself)
                    sb.append(",");
            }
            cursor.close();
        }
    	if(isNeedMyself)
    		sb.append(AccountServiceUtils.getBorqsAccountID());
    	
    	sb.append(")");
    	return sb.toString();
    }
    
    public Cursor searchFriendsCursorByCircleId(String circleid, String key)
    {
        key = key.replace("'", "");
    	if(UserProvider.mOpenHelper != null)
    	{
	    	SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
	    	String sql = "select * from users where (nickname like \'%"+key+"%\'" +
	    	              " or name_pinyin like \'%"+key+"%\')" +
	    	              " and uid in (select circle_users.uid from circle_users where circle_users.circleid = "+circleid + ")  order by name_pinyin asc" ;
	    	
//	    	 select * from users 
//	    	 where nickname like '%rao%' 
//	    	 and 
//	    	 uid in (select circle_users.uid from circle_users where circle_users.circleid = 5) order by nickname asc
	    	return db.rawQuery(sql, null);
	    	
    	}
	   	
    	return  null;
    }

    public Cursor searchExchangeCursor(String key, String uids) {
        key = key.replace("'", "");
        if (UserProvider.mOpenHelper != null) {
            SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
            String sql = "select * from users where (nickname like \'%" + key
                    + "%\'" + " or name_pinyin like \'%" + key + "%\')"
                    + " and uid not in ("
                    + uids + AccountServiceUtils.getBorqsAccountID() + ")"
                    + " order by name_pinyin asc";

            return db.rawQuery(sql, null);
        }
        return null;
    }

    public Cursor searchFriendsCursor(String key)
    {
    	return searchFriendsWithFilterIds(key, "");
    }
    
    public Cursor searchFriendsWithFilterIds(String key, String fileterIds)
    {
        key = key.replace("'", "");
    	if(UserProvider.mOpenHelper != null)
    	{
    		String tmpids = "";
    		if(fileterIds != null && fileterIds.length() > 0) {
    			tmpids = fileterIds + "," + AccountServiceUtils.getBorqsAccountID();
    		}
	    	SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
	    	String sql = "select * from users where (nickname like \'%"+key+"%\'" +
	    	               " or name_pinyin like \'%"+key+"%\')" + 
	    	               " and uid not in (" + tmpids + ")" + 
	    	              " order by name_pinyin asc" ;

	    	return db.rawQuery(sql, null);
    	}	   	
    	return  null;
    }
    
    public Cursor searchPagesCursor(String key)
    {
        key = key.replace("'", "");
    	if(UserProvider.mOpenHelper != null)
    	{
	    	SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
	    	String sql = "select * from pages where (name like \'%"+key+"%\'" +
	    	               " or name_en like \'%"+key+"%\')" + " order by name_en asc" ;

	    	return db.rawQuery(sql, null);
    	}	   	
    	return  null;
    }

    
    public Cursor queryAllCircleinfo(long uid) {
    	String where = null;
    	if(uid > 0) {
    		where = "uid=" + uid;
    	}
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
        		where, null, null);
        return cursor;
    }
    
    public Cursor queryCirclesEventOutofMonthinfo(String circleid, long uid) {
        StringBuilder ids = new StringBuilder();
        final String eventids = getEventIdsOutOneMonth();
//        where.append(CircleColumns.CIRCLE_ID + " not in (");
        if(StringUtil.isValidString(circleid)) {
        	ids.append(circleid);
        	if(StringUtil.isValidString(eventids)) {
        		ids.append(",").append(eventids);
        	}
        }else {
        	if(StringUtil.isValidString(eventids)) {
        		ids.append(eventids);
        	}
        }
        String where = CircleColumns.CIRCLE_ID + " not in (" + ids.toString() + ")";
            
        String sortBy = CircleColumns.CIRCLE_ID + " ASC";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, sortBy);
        return cursor;
    }
    
    public Cursor queryCirclesInfo(String filterids, String filterString) {
    	String where = null;
    	if(StringUtil.isValidString(filterids)) {
    		where = CircleColumns.CIRCLE_ID + " not in (" + filterids + ") and " + CircleColumns.TYPE + " = " + UserCircle.CIRLCE_TYPE_PUBLIC;
    	}
    	if(StringUtil.isValidString(filterString)) {
    		where = where + " and " + CircleColumns.CIRCLE_NAME + " like '%" + filterString + "%'";
    	}
        String sortBy = CircleColumns.REFERRED_COUNT + " DESC," + CircleColumns.CIRCLE_ID +" ASC ";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, sortBy);
        return cursor;
    }
    
    public Cursor queryGroupInfo(String filterids, String filterString) {
    	String where = null;
    	if(StringUtil.isValidString(filterids)) {
    		where = CircleColumns.CIRCLE_ID + " not in (" + filterids + ") and " + CircleColumns.TYPE + " = " + UserCircle.CIRCLE_TYPE_LOCAL ;
    	}
    	if(StringUtil.isValidString(filterString)) {
    		where = where + " and " + CircleColumns.CIRCLE_NAME + " like '%" + filterString + "%'";
    	}
        String sortBy = CircleColumns.CIRCLE_ID + " ASC";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, sortBy);
        return cursor;
    }
    
    public Cursor queryEventForPick(String filterids, String filterString) {
        StringBuilder ids = new StringBuilder();
        final String eventids = getEventIdsImIn();
//        where.append(CircleColumns.CIRCLE_ID + " not in (");
//        if(StringUtil.isValidString(filterids)) {
//        	ids.append(filterids);
//        	if(StringUtil.isValidString(eventids)) {
//        		ids.append(",").append(eventids);
//        	}
//        }else {
//        	if(StringUtil.isValidString(eventids)) {
//        		ids.append(eventids);
//        	}
//        }
        if(StringUtil.isValidString(eventids)) {
    		ids.append(eventids);
    	}
        
        String where = CircleColumns.CIRCLE_ID + " in (" + ids.toString() + ") and " + CircleColumns.TYPE +" = " + UserCircle.CIRCLE_TYPE_EVENT ;
            
        if(StringUtil.isValidString(filterString)) {
        	where = where + " and " + CircleColumns.CIRCLE_NAME + " like '%" + filterString + "%'";
        }
        String sortBy = CircleColumns.REFERRED_COUNT + " DESC," + CircleColumns.CIRCLE_ID +" ASC ";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, sortBy);
        return cursor;
    }
    
    public Cursor queryEventOutofMonthinfo(String filterids, String filterString) {
        StringBuilder ids = new StringBuilder();
        final String eventids = getEventIdsOutOneMonth();
//        where.append(CircleColumns.CIRCLE_ID + " not in (");
        if(StringUtil.isValidString(filterids)) {
        	ids.append(filterids);
        	if(StringUtil.isValidString(eventids)) {
        		ids.append(",").append(eventids);
        	}
        }else {
        	if(StringUtil.isValidString(eventids)) {
        		ids.append(eventids);
        	}
        }
        String where = CircleColumns.CIRCLE_ID + " not in (" + ids.toString() + ") and " + CircleColumns.TYPE +" = " + UserCircle.CIRCLE_TYPE_EVENT ;
            
        if(StringUtil.isValidString(filterString)) {
        	where = where + " and " + CircleColumns.CIRCLE_NAME + " like '%" + filterString + "%'";
        }
        String sortBy = CircleColumns.REFERRED_COUNT + " DESC," + CircleColumns.CIRCLE_ID +" ASC ";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, sortBy);
        return cursor;
    }
    
    public Cursor queryCirclesWithIds(String ids) {
        String where = CircleColumns.CIRCLE_ID + " in (" + ids + ")";
            
        String sortBy = CircleColumns.CIRCLE_ID + " DESC";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, sortBy);
        return cursor;
    }
    
    public Cursor searchCirclesWithIds(String ids, String filter) {
        String where = CircleColumns.CIRCLE_ID + " in (" + ids + ") and " + CircleColumns.CIRCLE_NAME + " like '%" + filter + "%'";
            
        String sortBy = CircleColumns.CIRCLE_ID + " DESC";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, sortBy);
        return cursor;
    }
    
    private String getEventIdsImIn() {
    	StringBuilder ids = new StringBuilder();
    	long monthAgo = System.currentTimeMillis() - 30*24*60*60*1000L;
    	String where = "(" + GroupColumns.END_TIME + " is null or " + GroupColumns.END_TIME + " <= 0 or " + GroupColumns.END_TIME + " > "  + monthAgo + ")" ;
    	
    	// TODO  add scene
    	final String homeid = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
    	if(!TextUtils.isEmpty(homeid)) {
    		where = where + " and " + GroupColumns.PARENT_ID + " = " + homeid;
    	}
    	
    	Cursor cursor = mContext.getContentResolver().query(CIRCLE_GROUP_URI, new String[]{GroupColumns.CIRCLE_ID} ,
                where, null, null); 
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
					if(ids.length() > 0) {
						ids.append(",");
					}
					ids.append(cursor.getLong(cursor.getColumnIndex(GroupColumns.CIRCLE_ID)));
				} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	
    	return ids.toString();
    }
    
    private String getEventIdsOutOneMonth() {
    	StringBuilder ids = new StringBuilder();
    	long monthAgo = System.currentTimeMillis() - 30*24*60*60*1000L;
    	String where = GroupColumns.END_TIME + " is not null and " + GroupColumns.END_TIME + " > 0 and " + GroupColumns.END_TIME + " < "  + monthAgo ;
    	
    	// TODO  add scene
    	final String homeid = QiupuORM.getSettingValue(mContext, QiupuORM.HOME_ACTIVITY_ID);
    	if(!TextUtils.isEmpty(homeid)) {
    		where = where + " and " + GroupColumns.PARENT_ID + " = " + homeid;
    	}
    	
    	Cursor cursor = mContext.getContentResolver().query(CIRCLE_GROUP_URI, new String[]{GroupColumns.CIRCLE_ID} ,
                where, null, null); 
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
					if(ids.length() > 0) {
						ids.append(",");
					}
					ids.append(cursor.getLong(cursor.getColumnIndex(GroupColumns.CIRCLE_ID)));
				} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	
    	return ids.toString();
    }

//    public ArrayList<UserCircle> queryAllCircleinfo(long uid){
//    	ArrayList<UserCircle> allcircles=new ArrayList<UserCircle>();
//    	Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,USER_CIRCLE_PROJECTION , "uid="+uid, null, null);
//    	if(cursor !=null && cursor.getCount() > 0){
//    		 while(cursor.moveToNext()){
//    			 allcircles.add(createCircleInformation(cursor));
//    		 }
//    	}
//    	cursor.close();
//		cursor = null;
//    	return allcircles;
//    }
    
    public Cursor queryLocalPublicCircleInfo(String filterIds) {
    	String where = CircleColumns.TYPE + " in (" + UserCircle.CIRCLE_TYPE_LOCAL + "," + UserCircle.CIRLCE_TYPE_PUBLIC + ") and "
    			+ CircleColumns.CIRCLE_ID + " not in (" + filterIds + ")";
      final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
              where, null, null);
      return cursor;
    }
    
    public UserCircle queryOneCricleInfo(String circleid){
    	UserCircle circle = null;
    	String where = CircleColumns.CIRCLE_ID + "=" + circleid ;
    	Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,USER_CIRCLE_PROJECTION , where, null, null);
    	
    	if(cursor != null && cursor.getCount() > 0){
    		while(cursor.moveToNext()){  
    			circle = createCircleInformation(cursor);  
    		}  
    	}
    	if(cursor != null)
    	{
    		cursor.close();
    		cursor = null;
    	}
    	return circle;
    }
    
    

//    private Cursor getUserAbsentApplications(long uid) {
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "getUserAbsentApplications uid:"+uid);
//    	String where =  ApkinfoColumns.UID +"='"+uid+"'" +
//                " AND " + ApkinfoColumns.APKSTATUS + "!='" + ApkResponse.APKStatus.STATUS_NEED_DOWNLOAD;
//        return getUserApplications(where);
//    }

//    private Cursor getUserInstalledApplications(long uid) {
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "getUserInstalledApplications uid:"+uid);
//    	String where =  ApkinfoColumns.UID +"='"+uid+"'";
//        return getUserApplications(where);
//    }

//    private Cursor getUserApplications(String where) {
//        return mContext.getContentResolver().query(APKINFO_CONTENT_URI, APKINFO_PROJECTION , where, null, null);
//    }

	public boolean isDebugMode() 
	{		
		String value = getSettingValue(SHOW_HTTP_LOG);
    	if(value == null || value.equals("")) {
    		return false;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "SHOW_HTTP_LOG :"+value);
    	
    	return Integer.valueOf(value) == 1?true:false;
	}
	
	public static boolean SHOW_HTTP_DEBUG_LOG=false;

	public void setDebugMode(boolean enable)
    {
		SHOW_HTTP_DEBUG_LOG= enable;
    	addSetting(SHOW_HTTP_LOG, enable==true?"1":"0");
    }

    /**
     *
     * @return user's all local applications including those need to upload to server or update from server.
     */
//    public  Cursor getUserAllLocalApplicationsCursor(long uid, String searchKey) {
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "getUserAllLocalApplicationsCursor uid:"+uid);
//
//        StringBuffer sbWhere = new StringBuffer(ApkinfoColumns.UID).append("='").append(uid).
//                append("' and ").append(ApkinfoColumns.APKSTATUS).append("!=").
//                append(ApkBasicInfo.APKStatus.STATUS_NEED_DOWNLOAD).
//                append(" and ").append(ApkinfoColumns.APKSTATUS).append("!=").
//                append(ApkBasicInfo.APKStatus.STATUS_DOWNLOADING);
//        catSearchingKeyword(sbWhere, searchKey);
////    	String where =  ApkinfoColumns.UID +"='"+uid + "' and (" +
////                ApkinfoColumns.APKSTATUS + " != " + ApkBasicInfo.APKStatus.STATUS_NEED_DOWNLOAD +
////                " and " + ApkinfoColumns.APKSTATUS + " != " + ApkBasicInfo.APKStatus.STATUS_DOWNLOADING + ")";
//
//        String sort = ApkinfoColumns.APKSTATUS + " ASC, " + ApkinfoColumns.APKNAME + " ASC";
//    	Cursor cursor = mContext.getContentResolver().query(APKINFO_CONTENT_URI, APKINFO_PROJECTION ,
//                sbWhere.toString(), null, sort);
//    	return cursor;
//    }

    private void catSearchingKeyword(StringBuffer sbWhere, final String searchKey) {
        if (!TextUtils.isEmpty(searchKey)) {
            sbWhere.append(" and (").append(ApkinfoColumns.APKNAME).
                    append(" like '%").append(searchKey).append("%'").append(" or ").
                    append(ApkinfoColumns.APKCOMPONENTNAME).
                    append(" like '%").append(searchKey).append("%' )");
        }
    }

    String[] CIRCLE_USERS_PROJECT = new String[]{CircleUsersColumns.ID, CircleUsersColumns.USERID, CircleUsersColumns.CIRCLE_ID};
    String circleuserFormat = CircleUsersColumns.CIRCLE_ID +" = %1$s and " + CircleUsersColumns.USERID+ "= %2$s";
	public void updateCircleUsers(String circles, List<QiupuUser> users) {
		
		//ContentValues []cvs = new ContentValues[users.size()];
		ContentValues cv = new ContentValues();
		for(int i=0;i<users.size();i++)
		{	
			Cursor cr = mContext.getContentResolver().query(CIRCLE_USERS_CONTENT_URI, CIRCLE_USERS_PROJECT, 
					String.format(circleuserFormat,circles, users.get(i).uid), null, null);
			if(cr != null && cr.getCount() > 0)
			{
				cr.close();
				cr = null;
			}
			else
			{
				cv.clear();
				cv.put(CircleUsersColumns.USERID, users.get(i).uid);
				cv.put(CircleUsersColumns.CIRCLE_ID, Long.parseLong(circles));			
				mContext.getContentResolver().insert(CIRCLE_USERS_CONTENT_URI, cv);
                if (cr != null) {
                    cr.close();
                    cr = null;
                }
            }
		}		
	}
	
    public void updateUserCircles(long uid, String circlestr) {
        if (circlestr == null || circlestr.length() == 0) {
            clearAllUserCircles(uid);
            return;
        }

        //clear firstly
        clearAllUserCircles(uid);

        //add
        ArrayList<Long> circleL = new ArrayList<Long>();
        String[] circles = circlestr.split(",");
        if (circles.length == 0) {
            circleL.add(Long.parseLong(circlestr));
        } else {
            for (String item : circles) {
                circleL.add(Long.parseLong(item));
            }
        }

        final int count = circleL.size();
        if (count > 0) {
            ContentValues[] cvs = new ContentValues[count];
            for (int i = 0; i < count; i++) {
                cvs[i] = new ContentValues();
                cvs[i].put(CircleUsersColumns.USERID, uid);
                cvs[i].put(CircleUsersColumns.CIRCLE_ID, circleL.get(i));
            }
            mContext.getContentResolver().bulkInsert(CIRCLE_USERS_CONTENT_URI, cvs);
        }
    }

    private void buildUserCircles(ArrayList<ContentValues> infoList, QiupuUser user) {
//        ArrayList<Long> circleL = new ArrayList<Long>();
        String[] circles = user.circleId.split(",");
//        if (circles.length == 0) {
//            circleL.add(Long.parseLong(user.circleId));
//        } else {
//            for (String item : circles) {
//                circleL.add(Long.parseLong(item));
//            }
//        }

//        final int count = circleL.size();
        final int count = circles.length;
        if (count > 0) {
//            ContentValues[] cvs = new ContentValues[count];
            for (int i = 0; i < count; i++) {
                ContentValues cv = new ContentValues();
                cv.put(CircleUsersColumns.USERID, user.uid);
//                cv.put(CircleUsersColumns.CIRCLE_ID, circleL.get(i));
                cv.put(CircleUsersColumns.CIRCLE_ID, Long.parseLong(circles[i]));
                infoList.add(cv);
            }
        }
    }

    private int bulkInsert(Uri url, ArrayList<ContentValues> cvs) {
        if (null == url || null == cvs || cvs.size() <= 0) {
            Log.e(TAG, "bulkInsert, ignore with invalid input.");
            return 0;
        }

        int count = 0;
        try {
            final int size = cvs.size();
            ContentValues[] values = cvs.toArray(new ContentValues[size]);
            count = mContext.getContentResolver().bulkInsert(url, values);
        } catch (Exception e) {
            Log.e(TAG, "bulkInsert failed, url:" + url.toString());
        }

        return count;
    }
    private void bulkInsertUserCirclesInfo(ArrayList<ContentValues> cvs) {
        bulkInsert(CIRCLE_USERS_CONTENT_URI, cvs);
    }
    
    public void insertCircleUserTable(long circleId, long userId) {
    	ContentValues cv = new ContentValues();
    	Cursor cr = mContext.getContentResolver().query(CIRCLE_USERS_CONTENT_URI, CIRCLE_USERS_PROJECT, 
				String.format(circleuserFormat,circleId, userId), null, null);
    	if(cr != null) {
    		if(cr.getCount() <= 0) {
    			cv.clear();
    			cv.put(CircleUsersColumns.USERID, userId);
    			cv.put(CircleUsersColumns.CIRCLE_ID, circleId);			
    			mContext.getContentResolver().insert(CIRCLE_USERS_CONTENT_URI, cv);
    		}
    		cr.close();
            cr = null;
    	}
    }

    public void updateCircleUsers(String circles, ArrayList<Long> users) {
		
		//ContentValues []cvs = new ContentValues[users.size()];
		ContentValues cv = new ContentValues();
		for(int i=0;i<users.size();i++)
		{	
			Cursor cr = mContext.getContentResolver().query(CIRCLE_USERS_CONTENT_URI, CIRCLE_USERS_PROJECT, 
					String.format(circleuserFormat,circles, users.get(i)), null, null);
			if(cr != null && cr.getCount() > 0)
			{
				cr.close();
				cr = null;
			}
			else
			{
				cv.clear();
				cv.put(CircleUsersColumns.USERID, users.get(i));
				cv.put(CircleUsersColumns.CIRCLE_ID, Long.parseLong(circles));			
				mContext.getContentResolver().insert(CIRCLE_USERS_CONTENT_URI, cv);
                if (cr != null) {
                    cr.close();
                    cr = null;
                }
			}
		}		
	}

	public void clearCircleUsers(String circleid) {
		mContext.getContentResolver().delete(CIRCLE_USERS_CONTENT_URI, CircleUsersColumns.CIRCLE_ID + "="+circleid, null);
	}

	public void clearCircleUser(String circleid, ArrayList<Long>uids) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(int i=0;i<uids.size();i++)
		{
			if(sb.length() > 1)
				sb.append(", ");
			
			sb.append(uids.get(i));
		}
		
		sb.append(" ) ");
		mContext.getContentResolver().delete(CIRCLE_USERS_CONTENT_URI, CircleUsersColumns.CIRCLE_ID + "="+circleid + " and " + CircleUsersColumns.USERID + " in "+sb.toString(), null);
	}

	public void clearAllUserCircles(long uid) {
		mContext.getContentResolver().delete(CIRCLE_USERS_CONTENT_URI, CircleUsersColumns.USERID + "="+uid, null);
	}
	
	public void clearUserCirclesWithOutPublicCircles(long uid) {
	    String where = CircleUsersColumns.USERID + "="+uid + " and " + CircleUsersColumns.CIRCLE_ID + " between " + 0 + " and " + QiupuConfig.PUBLIC_CIRCLE_MIN_ID;
        mContext.getContentResolver().delete(CIRCLE_USERS_CONTENT_URI, where, null);
	}

    private void deleteUserCircles(String idsSuffix) {
        mContext.getContentResolver().delete(CIRCLE_USERS_CONTENT_URI, CircleUsersColumns.USERID + idsSuffix, null);
    }
	
	public void updateUserCircles(ArrayList<Long> circles, long uid) {
		//ContentValues []cvs = new ContentValues[users.size()];
		ContentValues cv = new ContentValues();
		for(int i=0;i<circles.size();i++)
		{	
			Cursor cr = mContext.getContentResolver().query(CIRCLE_USERS_CONTENT_URI, CIRCLE_USERS_PROJECT, 
					String.format(circleuserFormat,circles.get(i), uid), null, null);
			if(cr != null && cr.getCount() > 0)
			{
				
			}
			else
			{
				cv.put(CircleUsersColumns.USERID, uid);
				cv.put(CircleUsersColumns.CIRCLE_ID, circles.get(i));			
				mContext.getContentResolver().insert(CIRCLE_USERS_CONTENT_URI, cv);
			}

            if (cr != null) {
                cr.close();
                cr = null;
            }
		}		
	}

    public String getCurrentApiUrl() {
        return DataConnectionUtils.getCurrentApiHost(mContext);
    }

//    public boolean isNewStreamMode()
//    {
//        String value = getSettingValue(DEV_STREAM_TRY);
//        if(TextUtils.isEmpty(value)) {
//            return true;
//        }
//
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "isNewStreamMode :"+value);
//        return Integer.valueOf(value) == 1;
//    }
//
//    public void setNewStreamMode(boolean enable)
//    {
//        addSetting(DEV_STREAM_TRY, enable? "1" : "0");
//    }

//    public boolean isDevFeatureFlag()
//    {
//        String value = getSettingValue(DEV_FEATURE_TRY);
//        if(TextUtils.isEmpty(value)) {
//            return TestServerSetActivity.getDefaultTryMode(mContext);
//        }
//
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "isDevFeatureFlag:"+value);
//
//        return Integer.valueOf(value) == 1;
//    }
//
//    public void setDevFeatureFlag(boolean enable)
//    {
//        addSetting(DEV_FEATURE_TRY, enable? "1" : "0");
//    }

    public boolean isEnablePushService() {
        String value = getSettingValue(ENABLE_PUSH_SERVICE);
        if(TextUtils.isEmpty(value)) {
            return false;
        }

        if(QiupuConfig.DBLOGD)Log.d(TAG, "isEnablePushService :" + value);

        return Integer.valueOf(value) == 1;
    }

    public void setEnablePushService(boolean enable) {
        addSetting(ENABLE_PUSH_SERVICE, enable ? "1" : "0");
    }

//    public static boolean isUsingTabNavigation(Context con)
//    {
//        String value = getSettingValue(con, "isUsingTabNavigation");
//        if(value == null || value.equals("")) {
//            return false;
//        }
//
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "isUsingTabNavigation :"+value);
//
//        return Integer.valueOf(value) == 1;
//    }

//    public boolean isUsingTabNavigation() {
//        return isUsingTabNavigation(mContext);
//    }

//    public void setUsingTabNavigation(boolean enable) {
//        addSetting("isUsingTabNavigation", enable ? "1" : "0");
//    }
    
    public static boolean isOpenNewPlatformSettings(Context con)
    {
    	String value = getSettingValue(con, "isOpenNewPlatformSettings0506");
    	if(value == null || value.equals("")) {
    		return false;
    	}
    	
    	if(QiupuConfig.DBLOGD)Log.d(TAG, "isOpenNewPlatformSettings :"+value);
    	
    	return Integer.valueOf(value) == 1;
    }

    public static boolean isShowAlbumSettings(Context con)
    {
        String value = getSettingValue(con, "isShowAlbumSettings0506");
        if(value == null || value.equals("")) {
            return QiupuConfig.IS_SHOW_ALBUM;
        }

        if(QiupuConfig.DBLOGD)Log.d(TAG, "isShowAlbumSettings :"+value);

        return Integer.valueOf(value) == 1;
    }
    
    // just used to change newPlatform url
    public boolean isOpenPublicCircle() {      
        String value = getSettingValue("isOpenPublicCircleSettings0506");
        if(TextUtils.isEmpty(value)) {
            return true;
        }
        
        if(QiupuConfig.DBLOGD)Log.d(TAG, "whichTestEnvir :"+value);
        
        return Integer.valueOf(value) == 1;
    }
//
//    public boolean isUserNewPlatform() {
//        String value = getSettingValue("isOpenNewPlatformSettings0506");
//        if(TextUtils.isEmpty(value)) {
//            return false;
//        }
//
//        if(QiupuConfig.DBLOGD)Log.d(TAG, "whichTestEnvir :"+value);
//
//        return Integer.valueOf(value) == 1;
//    }

    public boolean isShowAlbumSettings() {
    	return isShowAlbumSettings(mContext);
    }

    public void updateLookupPeopleType(long uid, boolean isadd , long contactId){
    	ContentValues values = new ContentValues();
    	final String where = LookUpPeopleColumns.UID + " = " + uid;

    	values.put(LookUpPeopleColumns.TYPE, PeopleLookupHelper.LOOKUP_FLAG_SCANNED);
        final int updatedUserCount = mContext.getContentResolver().update(LOOKUP_CONTENT_LOOKUP_URI, values, where, null);
        if (updatedUserCount <= 0) { // no user record found
            if(contactId > 0 && isadd) {
                final String queryContact = LookUpPeopleColumns.CONTACT_ID + " = " + contactId;
                values.put(LookUpPeopleColumns.UID, uid);
                final int count = mContext.getContentResolver().update(LOOKUP_CONTENT_LOOKUP_URI, values, queryContact, null);
                if (count <= 0) {
                    values.put(LookUpPeopleColumns.CONTACT_ID, contactId);
                    mContext.getContentResolver().insert(LOOKUP_CONTENT_LOOKUP_URI, values);
                }
            }
        }

//    	Cursor onePeople = mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, null, where, null, null);
//    	// have this user goto update data
//    	if(onePeople != null && onePeople.getCount() > 0) {
//    		if(isadd){
//    			values.put(LookUpPeopleColumns.TYPE, PeopleLookupHelper.TYPE_MY_FRIEND);
//    		}else{
//    			values.put(LookUpPeopleColumns.TYPE, PeopleLookupHelper.TYPE_IS_SYSTEM_USER);
//    		}
//    		mContext.getContentResolver().update(LOOKUP_CONTENT_LOOKUP_URI, values, where, null);
//    	}else{
//    		if(isadd){
//    			values.put(LookUpPeopleColumns.TYPE, PeopleLookupHelper.TYPE_MY_FRIEND);
//    			values.put(LookUpPeopleColumns.UID, uid);
//    			if(contactId > 0) {
//    				String tmpwhere = LookUpPeopleColumns.CONTACT_ID + " = " + contactId;
//    				Cursor tmpPeople = mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, null, tmpwhere, null, null);
//    				if(tmpPeople != null && tmpPeople.getCount() > 0) {
//    					mContext.getContentResolver().update(LOOKUP_CONTENT_LOOKUP_URI, values, tmpwhere, null);
//    				}else {
//    					values.put(LookUpPeopleColumns.CONTACT_ID, contactId);
//    					mContext.getContentResolver().insert(LOOKUP_CONTENT_LOOKUP_URI, values);
//    				}
//    				closeCursor(tmpPeople);
//    			}
//    		}
//    	}
//    	closeCursor(onePeople);
    }
//
//    public Cursor queryPeopleOutSystem() {
//        String selection = LookUpProvider.TYPE + " = \'0\'";
//        Cursor cursor = mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, null, selection, null, null);
//
//        if (cursor == null || cursor.getCount() == 0) {
//            return null;
//        } else {
//            Log.d(TAG, "cursor.getCount() = " + cursor.getCount());
//            return cursor;
//        }
//    }
//
//    public Cursor queryPeopleInSystem() {
//        String selection = LookUpPeopleColumns.TYPE + " > \'0\'";
//        Cursor cursor = mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, null, selection, null, null);
//
//        if (cursor == null || cursor.getCount() == 0) {
//            return null;
//        } else {
//            return cursor;
//        }
//    }
//
    final String contactQuery=LookUpPeopleColumns.CONTACT_ID+"=?";
    public Cursor queryLookUpPeopleByContactId(long contactid) {
        return  mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, LOOKUP_PEOPLE_PROJECTION, contactQuery, new String[]{String.valueOf(contactid)},null);
        
    }

    public long queryBorqsIdByContactId(long contactid) {
        String selection = LookUpPeopleColumns.CONTACT_ID + " = " + contactid;
        Cursor cursor = mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, new String[]{LookUpPeopleColumns.UID}, selection, null, null);
        long borqsId = -1;
        if(cursor != null) {
        	if (cursor.getCount() > 0
        			&& cursor.moveToFirst()){
        		long tempUid;
        		do {
        			tempUid = cursor.getLong(cursor.getColumnIndex(LookUpPeopleColumns.UID));
        			if (borqsId < tempUid) {
        				borqsId = tempUid;
        			}
        		} while(cursor.moveToNext());
        	}
        	
            cursor.close();
            cursor = null;
        }
        return borqsId;
    }
    
//    final static String lookupByUID = LookUpPeopleColumns.UID + " = ?";

    final static String  lookupByBorqsID = LookUpPeopleColumns.CONTACT_ID + " = ?";
    public Cursor queryLookUpTypeAndBorqsId(long ContactId) {        
        Cursor cursor = mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, new String[]{LookUpPeopleColumns.TYPE, LookUpPeopleColumns.UID}, lookupByBorqsID, new String[]{String.valueOf(ContactId)}, null);
        return cursor;
    }

    public String getContactIdInMyCircle()
    {
    	StringBuilder sb = new StringBuilder();
    	Cursor cursor = mContext.getContentResolver().query(LOOKUP_CONTENT_LOOKUP_URI, new String[]{LookUpPeopleColumns.ID, LookUpPeopleColumns.CONTACT_ID}, LookUpPeopleColumns.TYPE + ">" + 0, null, null);
    	if (cursor != null) {
            if (cursor.moveToFirst())
            {
                sb.append(cursor.getLong(cursor.getColumnIndex(LookUpPeopleColumns.CONTACT_ID)));
                while(cursor.moveToNext()) {
                    sb.append(", ");
                    sb.append(cursor.getLong(cursor.getColumnIndex(LookUpPeopleColumns.CONTACT_ID)));
                }
            }

            cursor.close();
        }
    	return sb.toString();
    }
    
    private static boolean isDataFlowAutoSaveMode = false;
    private static int fetchedisDataFlowAutoSaveMode = -1;
    public static boolean isDataFlowAutoSaveMode(Context context) {
    	if(fetchedisDataFlowAutoSaveMode > 0)
    	    return isDataFlowAutoSaveMode;    	
    	else
    	{
    		fetchedisDataFlowAutoSaveMode = 1;
	        String value = getSettingValue(context, DATA_FLOW_AUTO_SAVE_MODE);
	        if(value == null || value.equals("")) {
	        	isDataFlowAutoSaveMode = false;
	            return false;
	        }
	
	        if(QiupuConfig.DBLOGD)Log.d(TAG, "isDataFlowAutoSaveMode :" + value);
	        return isDataFlowAutoSaveMode = Integer.valueOf(value) == 1;
    	}
    }
    public boolean isDataFlowAutoSaveMode() {
    	if(fetchedisDataFlowAutoSaveMode > 0)
    	    return isDataFlowAutoSaveMode;    	
    	else
    	{
    		fetchedisDataFlowAutoSaveMode = 1;
	        String value = getSettingValue(DATA_FLOW_AUTO_SAVE_MODE);
	        if(value == null || value.equals("")) {
	        	isDataFlowAutoSaveMode = false;
	            return false;
	        }
	
	        if(QiupuConfig.DBLOGD)Log.d(TAG, "isDataFlowAutoSaveMode :" + value);
	        return isDataFlowAutoSaveMode = Integer.valueOf(value) == 1;
    	}
    }
    public static void setDataFlowAutoSaveMode(Context context, boolean enabled) {
    	isDataFlowAutoSaveMode = enabled;
        addSetting(context, DATA_FLOW_AUTO_SAVE_MODE, enabled ? "1" : "0");
    }
    public void setDataFlowAutoSaveMode(boolean enabled) {
    	isDataFlowAutoSaveMode = enabled;
        addSetting(DATA_FLOW_AUTO_SAVE_MODE, enabled ? "1" : "0");
    }
    
    public static String getSyncContactStatus(Context context) {
        String value = getSettingValue(context, SYNC_STATUS);
        if(TextUtils.isEmpty(value)) {
            return "";
        }

        if(QiupuConfig.DBLOGD)Log.d(TAG, "getSyncContactStatus :" + value);
        return value;
    }
    
    public static void setSyncContactStatus(Context context, String status) {
        addSetting(context, SYNC_STATUS, status);
    }
    
    public static void closeCursor(Cursor cursor) {
        if (null != cursor && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }

	public static boolean isAllowLookup(Context context) {
		String value = getSettingValue(context, IS_ALLOW_LOOKUP);
		if (value == null || value.equals("")) {
			return false;
		}

		if (QiupuConfig.DBLOGD)
			Log.d(TAG, "isAllowLookup :" + value);
		return Integer.valueOf(value) == 1;
	}

	public static void setAllowLookup(Context context, boolean enabled) {
		addSetting(context, IS_ALLOW_LOOKUP, enabled ? "1" : "0");
	}
	
	public void updateShareSourceDB(long uid, ArrayList<ShareSourceItem> sourceitems){
		boolean isExist = isSourceExist(uid);
		if(isExist == true){
			updateShareSource(uid, sourceitems);
		}else{
			insertShareSource(uid, sourceitems);
		}
	} 
	
	final static String queryByUID =  UsersColumns.USERID +"=?";
	private boolean isSourceExist(long uid)
    {
    	boolean ret = false;
    	Cursor cursor = mContext.getContentResolver().query(SHARE_RESOURCE_URI,new String[]{ShareResourceColumns.USERID} , queryByUID, new String[]{String.valueOf(uid)}, null);
    	if(cursor != null && cursor.getCount() > 0){    		
    		ret = true;
    	}
    	if(cursor != null)
    		cursor.close();
    	
    	return ret;
    }
	
	private void insertShareSource(long uid, ArrayList<ShareSourceItem> sourceitems){
		ContentValues cv = createShareSourceContentValues(uid, sourceitems);
		mContext.getContentResolver().insert(SHARE_RESOURCE_URI, cv);
	}
	
	private void updateShareSource(long uid, ArrayList<ShareSourceItem> sourceitems){
		
		ContentValues cv = createShareSourceContentValues(uid, sourceitems);
		String where =  ShareResourceColumns.USERID +"="+ uid;
    	mContext.getContentResolver().update(SHARE_RESOURCE_URI, cv, where, null);
	}
	
	private ContentValues createShareSourceContentValues(long uid, ArrayList<ShareSourceItem> sourceitems) {
		ContentValues cv = new ContentValues();
		cv.put(ShareResourceColumns.USERID, uid);
		for(int i=0;i<sourceitems.size();i++){
			ShareSourceItem item = sourceitems.get(i);
			if(BpcApiUtils.TEXT_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_TEXT_COUNT, item.mCount);
			}else if(BpcApiUtils.APK_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_APP_COUNT, item.mCount);
			}else if(BpcApiUtils.LINK_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_LINK_COUNT, item.mCount);
			}else if(BpcApiUtils.IMAGE_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_PHOTO_COUNT, item.mCount);
			}else if(BpcApiUtils.BOOK_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_BOOK_COUNT, item.mCount);
			}else if(BpcApiUtils.STATIC_FILE_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_STATIC_FILE, item.mCount);
			}else if(BpcApiUtils.AUDIO_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_AUDIO, item.mCount);
			}else if(BpcApiUtils.VIDEO_POST == item.mType){
				cv.put(ShareResourceColumns.SHARED_VIDEO, item.mCount);
			}
		}
		return cv;
	}
	
	public ArrayList<ShareSourceItem> queryShareSources(long uid) {
        return queryShareSources(mContext, uid);
    }
	
	public static ArrayList<ShareSourceItem> queryShareSources(Context context, long uid) {
        ArrayList<ShareSourceItem> shareSource = new ArrayList<ShareSourceItem>();
        Cursor cursor = context.getContentResolver().query(SHARE_RESOURCE_URI,
        		SHARE_SOURCE_PROJECTION, ShareResourceColumns.USERID + "=" + uid, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
            	shareSource = createShareSourceitemList(cursor, QiupuConfig.isPublicCircleProfile(uid) || QiupuConfig.isEventIds(uid));
            }
            cursor.close();
        }

        return shareSource;
	}
	
	public static ArrayList<ShareSourceItem> createShareSourceitemList(Cursor mCursor, boolean isCircle) {
		ArrayList<ShareSourceItem> items = new ArrayList<ShareSourceItem>();
		ShareSourceItem shared; 
		shared = new ShareSourceItem("");
		shared.mType = BpcApiUtils.TEXT_POST;
		shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_TEXT_COUNT));
		items.add(shared);
		
		shared = new ShareSourceItem("");
		shared.mType = BpcApiUtils.IMAGE_POST;
		shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_PHOTO_COUNT));
		items.add(shared);
		
		if(isCircle) {
		    shared = new ShareSourceItem("");
		    shared.mType = BpcApiUtils.BOOK_POST;
		    shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_BOOK_COUNT));
		    items.add(shared);
		    
		    shared = new ShareSourceItem("");
		    shared.mType = BpcApiUtils.APK_POST;
		    shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_APP_COUNT));
		    items.add(shared);
		}
		
		shared = new ShareSourceItem("");
		shared.mType = BpcApiUtils.LINK_POST;
		shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_LINK_COUNT));
		items.add(shared);
		
		shared = new ShareSourceItem("");
		shared.mType = BpcApiUtils.STATIC_FILE_POST;
		shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_STATIC_FILE));
		items.add(shared);
		shared = new ShareSourceItem("");
		shared.mType = BpcApiUtils.AUDIO_POST;
		shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_AUDIO));
		items.add(shared);
		shared = new ShareSourceItem("");
		shared.mType = BpcApiUtils.VIDEO_POST;
		shared.mCount = mCursor.getInt(mCursor.getColumnIndex(ShareResourceColumns.SHARED_VIDEO));
		items.add(shared);
			
		return items;
	}

    public static QiupuUser queryOneUserInfo(Context context, long userid){
    	QiupuUser user = null;
        if (LowPerformance) Log.d(TAG, "queryOneUserInfo enter.");
    	Cursor cursor = context.getContentResolver().query(USERS_CONTENT_URI,USERS_INFO_PROJECTION , queryByUID, new String[]{String.valueOf(userid)}, null);
    	if(cursor != null) {
            if (cursor.moveToFirst()){                
                user = createUserInformation(context, cursor);                
            }
            cursor.close();
        }
        if (LowPerformance) Log.d(TAG, "queryOneUserInfo exit.");
    	return user;
    }

    public String getOneCircleName(long circleid){
    	String circleName = "";
    	String where =  CircleColumns.CIRCLE_ID +"="+circleid;
    	Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,new String[]{CircleColumns.CIRCLE_NAME} , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
    		if(cursor.moveToFirst()) {
    			circleName = cursor.getString(0);  
    		}
    	}
    	if(cursor != null)
    	{
            cursor.close();
		    cursor = null;
    	}
    	return circleName;
    }
    
    public void insertExpandCirCleInfo() {
    	ArrayList<ContentValues> bulkInsertList = new ArrayList<ContentValues>();
    	
    	ContentValues cv = new ContentValues();
		cv.put(CircleColumns.CIRCLE_ID, QiupuConfig.CIRCLE_ID_ALL);
		cv.put(CircleColumns.SHOW_ON_STREAM, 1);
		bulkInsertList.add(cv);
		
		ContentValues cv1 = new ContentValues();
		cv1.put(CircleColumns.CIRCLE_ID, QiupuConfig.CIRCLE_ID_HOT);
		cv1.put(CircleColumns.SHOW_ON_STREAM, 1);
		bulkInsertList.add(cv1);
		
		ContentValues cv2 = new ContentValues();
		cv2.put(CircleColumns.CIRCLE_ID, QiupuConfig.CIRCLE_ID_NEAR_BY);
		cv2.put(CircleColumns.SHOW_ON_STREAM, 1);
		bulkInsertList.add(cv2);
		
		ContentValues cv3 = new ContentValues();
		cv3.put(CircleColumns.CIRCLE_ID, QiupuConfig.CIRCLE_ID_PUBLIC);
		cv3.put(CircleColumns.SHOW_ON_STREAM, 1);
		bulkInsertList.add(cv3);
		
		ContentValues[] valueArray = new ContentValues [bulkInsertList.size()];
		valueArray = bulkInsertList.toArray(valueArray);
		mContext.getContentResolver().bulkInsert(CIRCLES_CONTENT_URI, valueArray);
	}
    
    private void insertPhoneEmailList(QiupuAccountInfo user) {
        if(user.phoneList != null && user.phoneList.size() > 0) {
            for(int i=0;i<user.phoneList.size();i++){
                PhoneEmailInfo phone = user.phoneList.get(i);
                insertPhoneEmailInfo(phone, UserProvider.TYPE_PHONE);
            }
        }
        
        if(user.emailList != null && user.emailList.size() > 0) {
            for(int i=0;i<user.emailList.size();i++){
                PhoneEmailInfo phone = user.emailList.get(i);
                insertPhoneEmailInfo(phone, UserProvider.TYPE_EMAIL);
            }
        }
    }
    
    public long insertPhoneEmailInfo(PhoneEmailInfo info, int phoneEmailType){ 
    	ContentValues cv = createPhoneEmailInfo(info, phoneEmailType);
    	Uri uri = mContext.getContentResolver().insert(PHONEEMAIL_CONTENT_URI, cv);
    	return ContentUris.parseId(uri);
    }
    
    private void insertUserImageList(QiupuUser user) {
        insertUserImageList(user.uid, user.friendsImageList, UserProvider.TYPE_FRIENDS);
        insertUserImageList(user.uid, user.fansImageList, UserProvider.TYPE_FANS);
    }
    
    private void insertPubclicPeopleImageList(UserCircle circle) {
        insertUserImageList(circle.circleid, circle.inMembersImageList, UserProvider.IMAGE_TYPE_IN_MEMBER);
        insertUserImageList(circle.circleid, circle.applyedMembersList, UserProvider.IMAGE_TYPE_APPLY_MEMBER);
        insertUserImageList(circle.circleid, circle.invitedMembersList, UserProvider.IMAGE_TYPE_INVITE_MEMBER);
        insertUserImageList(circle.circleid, circle.formalCirclesList, UserProvider.IMAGE_TYPE_FORMAL_CIRCLE);
        insertUserImageList(circle.circleid, circle.freeCirclesList, UserProvider.IMAGE_TYPE_FREE_CIRCLE);
    }
    
    private void insertUserImageList(long id, ArrayList<UserImage> imageList, int type) {
        if(imageList != null && imageList.size() > 0 ) {
            int count = imageList.size();
            for(int i=0; i<count; i++){
                UserImage uImg = imageList.get(i);
                insertUserImageInfo(id, uImg, type);
            }
        }
    }
    
    private void insertSharedPhotoList(QiupuUser user) {
    	if(user.shareImageList != null && user.shareImageList.size() > 0) {
    		for(int i=0;i<user.shareImageList.size();i++){
    			SharedPhotos p = user.shareImageList.get(i);
    			insertSharedPhotosInfo(user,p);
    		}
    	}
    }
    public void insertQiupuAlbumList(ArrayList<QiupuAlbum> albums,long uid) {
        boolean isExist = isQiupuAlbumExist(uid);
        if(isExist) {
            deleteQiupuAlbumsInfo(uid);
        }
        if(albums != null && albums.size() > 0) {
            for(int i=0;i<albums.size();i++){
                QiupuAlbum album = albums.get(i);
                insertQiupuAlbumInfo(album,false);
            }
        }
    }
    public void updateQiupuAlbum(long album_id  ,boolean hava_expired) {
    	try{
            String where =  QiupuAlbumColumns.ALBUM_ID +"="+album_id;
            ContentValues values = new ContentValues();
            values.put(QiupuAlbumColumns.HAVE_EXPIRED, hava_expired);
            mContext.getContentResolver().update(QIUPU_ALBUM_URI, values, where, null);
        }catch(Exception ne){}    
    }
    
    public void bullinsertQiupuPhoto(ArrayList<QiupuPhoto> photos,long uid,long album_id) {
    	boolean isExist = isQiupuPhotoExist(uid,album_id);
    	if(isExist) {
    		deleteQiupuPhotosInfo(uid,album_id);
    	}
    	if(photos == null || photos.size() <= 0) {
    		return;
    	}
    	ContentValues []cvs = new ContentValues[photos.size()];
    	for(int i=0;i<photos.size();i++){
    		QiupuPhoto p = photos.get(i);
    		cvs[i] = createQiupuPhotoInfo(p);
    	}
    	
    	//insert bull
    	mContext.getContentResolver().bulkInsert(QIUPU_PHOTO_URI, cvs);
    	mContext.getContentResolver().notifyChange(QIUPU_PHOTO_URI, null);
    	cvs = null;
    	
	}
   
    private int bulkInsertPerhapsNameList(QiupuUser user) {
        deletePerhapsName(user.uid);

        int count = 0;
        final int arraySize = user.perhapsNames == null ? 0: user.perhapsNames.size();
        if(arraySize > 0) {
            ContentValues []cvs = new ContentValues[arraySize];
            for(int i = 0; i < arraySize; i++){
                cvs[i] = createPerhapsNameInfo(user,user.perhapsNames.get(i));
            }
            
            count = mContext.getContentResolver().bulkInsert(PERHAPS_NAME_URI, cvs);
            cvs = null;
        }
        return count;
    }

    private void buildPerhapsNameList(ArrayList<ContentValues> infoList, QiupuUser user) {
//        deletePerhapsName(user.uid);

//        int count = 0;
        final int arraySize = user.perhapsNames == null ? 0 : user.perhapsNames.size();
        if (arraySize > 0) {
//            ContentValues[] cvs = new ContentValues[arraySize];
            for (int i = 0; i < arraySize; i++) {
                infoList.add(createPerhapsNameInfo(user, user.perhapsNames.get(i)));
            }

//            count = mContext.getContentResolver().bulkInsert(PERHAPS_NAME_URI, cvs);
//            cvs = null;
        }
//        return count;
    }

    private void bulkInsertPossibleNameInfo(ArrayList<ContentValues> cvs) {
        bulkInsert(PERHAPS_NAME_URI, cvs);
    }
    
    public long insertSharedPhotosInfo(QiupuUser user,SharedPhotos info){ 
    	ContentValues cv = createSharedPhotosInfo(user, info);
    	Uri uri = mContext.getContentResolver().insert(SHARED_PHOTOS_URI, cv);
    	return ContentUris.parseId(uri);
    }
    
    public long insertQiupuAlbumInfo(QiupuAlbum info,boolean isdelete){ 
    	if(isdelete) {
    		if(isExistQiupuAlbumByAlumId(info.album_id)) {
    			deleteQiupuAlbumByAlbumId(info.album_id);
    		}
    	}
    	ContentValues cv = createQiupuAlbumInfo(info);
    	Uri uri = mContext.getContentResolver().insert(QIUPU_ALBUM_URI, cv);
    	return ContentUris.parseId(uri);
    }

    public long insertUserImageInfo(long id,UserImage info, int type){ 
        ContentValues cv = createUserImageInfo(id, info, type);
        Uri uri = mContext.getContentResolver().insert(USER_IMAGE_URI, cv);
        return ContentUris.parseId(uri);
    }

    private ContentValues createUserImageInfo(long id, UserImage info, int type){
    	ContentValues cv = new ContentValues();
    	cv.put(UserImageColumns.USERID, info.user_id);
    	cv.put(UserImageColumns.BELONG_USERID, id);
    	cv.put(UserImageColumns.TYPE,type);
    	cv.put(UserImageColumns.IMAGE_URL, info.image_url);
    	cv.put(UserImageColumns.IMAGE_DISPLAY_NAME, info.userName);
    	return cv;
    }

    private ContentValues createSharedPhotosInfo(QiupuUser user,SharedPhotos info){
    	ContentValues cv = new ContentValues();
    	cv.put(SharedPhotosColumns.POST_ID, info.post_id);
    	cv.put(SharedPhotosColumns.USERID, user.uid);
    	cv.put(SharedPhotosColumns.PHOTO_IMG_MIDDLE, info.photo_img_middle);
    	return cv;
    }
    
    private ContentValues createQiupuAlbumInfo(QiupuAlbum info){
    	ContentValues cv = new ContentValues();
    	cv.put(QiupuAlbumColumns.ALBUM_ID, info.album_id);
    	cv.put(QiupuAlbumColumns.USERID, info.user_id);
    	cv.put(QiupuAlbumColumns.ALBUM_TYPE, info.album_type);
    	cv.put(QiupuAlbumColumns.TITLE, info.title);
    	cv.put(QiupuAlbumColumns.SUMMARY, info.summary);
    	cv.put(QiupuAlbumColumns.PRIVACY, info.privacy == true?1:0);
    	cv.put(QiupuAlbumColumns.CREATED_TIME, info.created_time);
    	cv.put(QiupuAlbumColumns.UPDATED_TIME, info.updated_time);
    	cv.put(QiupuAlbumColumns.HAVE_EXPIRED, info.have_expired == true?1:0);
    	cv.put(QiupuAlbumColumns.PHOTO_COUNT, info.photo_count);
    	cv.put(QiupuAlbumColumns.ALBUM_COVER_MID, info.album_cover_photo_middle);
    	return cv;
    }
    
    private ContentValues createQiupuPhotoInfo(QiupuPhoto info){
    	ContentValues cv = new ContentValues();
    	cv.put(QiupuPhotoColumns.PHOTO_ID, info.photo_id);
    	cv.put(QiupuPhotoColumns.ALBUM_ID, info.album_id);
    	cv.put(QiupuPhotoColumns.USERID, info.uid);
    	cv.put(QiupuPhotoColumns.PHOTO_URL_SMALL, info.photo_url_small);
    	cv.put(QiupuPhotoColumns.PHOTO_URL_MIDDLE, info.photo_url_middle);
    	cv.put(QiupuPhotoColumns.PHOTO_URL_BIG, info.photo_url_big);
    	cv.put(QiupuPhotoColumns.PHOTO_URL_THUMBNAIL, info.photo_url_thumbnail);
    	cv.put(QiupuPhotoColumns.PHOTO_URL_ORIGINAL, info.photo_url_original);
    	cv.put(QiupuPhotoColumns.CREATED_TIME, info.created_time);
    	cv.put(QiupuPhotoColumns.CAPTION, info.caption);
    	cv.put(QiupuPhotoColumns.LOCATION, info.location);
    	cv.put(QiupuPhotoColumns.ILIKEED, info.iliked == true?1:0);
    	cv.put(QiupuPhotoColumns.LIKES_COUNT, info.likes_count);
    	cv.put(QiupuPhotoColumns.COMMENTS_COUNT, info.comments_count);
    	cv.put(QiupuPhotoColumns.FROM_USER_ID, info.from_user_id);
    	cv.put(QiupuPhotoColumns.FROM_NICK_NAME, info.from_nick_name);
    	cv.put(QiupuPhotoColumns.FROM_IMAGE_URL, info.from_image_url);
    	return cv;
    }
    
    private ContentValues createPerhapsNameInfo(QiupuUser user, PerhapsName info){
        ContentValues cv = new ContentValues();
        cv.put(PerhapsNameColumns.FULLNAME, info.name);
        cv.put(PerhapsNameColumns.USERID, user.uid);
        cv.put(PerhapsNameColumns.COUNT, info.count);
        return cv;
    }
    
    private ContentValues createPhoneEmailInfo(PhoneEmailInfo info, int phoneEmailType){
        ContentValues cv = new ContentValues();
        cv.put(PhoneEmailColumns.USERID, info.uid);
        cv.put(PhoneEmailColumns.TYPE, info.type);
        cv.put(PhoneEmailColumns.INFO, info.info);
        cv.put(PhoneEmailColumns.ISBIND, info.isbind ? 1 : 0);
        cv.put(PhoneEmailColumns.PHONE_OR_EMAIL, phoneEmailType);
        return cv;
    }
    
    private static PhoneEmailInfo createPhoneEmailInformation(Cursor cursor) {
    	if (null != cursor) {
    		PhoneEmailInfo result = new PhoneEmailInfo();
    		result.uid = cursor.getLong(cursor.getColumnIndex(PhoneEmailColumns.USERID));
    		result.type = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.TYPE));
    		result.info = cursor.getString(cursor.getColumnIndex(PhoneEmailColumns.INFO));
    		result.isbind = cursor.getInt(cursor.getColumnIndex(PhoneEmailColumns.ISBIND)) == 1;
    		return result;
    	}
    	
    	Log.e(TAG, "createPhoneEmailInformation, unexpected null cursor.");
    	return null;
    }
    
    private static UserImage createUserImageInformation(Cursor cursor) {
    	if (null != cursor) {
    		UserImage result = new UserImage();
    		result.user_id = cursor.getLong(cursor.getColumnIndex(UserImageColumns.USERID));
    		result.userName = cursor.getString(cursor.getColumnIndex(UserImageColumns.IMAGE_DISPLAY_NAME));
    		result.image_url = cursor.getString(cursor.getColumnIndex(UserImageColumns.IMAGE_URL));
    		return result;
    	}
    	
    	Log.e(TAG, "createPhoneEmailInformation, unexpected null cursor.");
    	return null;
    }
    
    private static SharedPhotos createSharedPhotosInformation(Cursor cursor) {
    	if (null != cursor) {
    		SharedPhotos result = new SharedPhotos();
    		result.post_id = cursor.getLong(cursor.getColumnIndex(SharedPhotosColumns.POST_ID));
    		result.photo_img_middle = cursor.getString(cursor.getColumnIndex(SharedPhotosColumns.PHOTO_IMG_MIDDLE));
    		return result;
    	}
    	
    	Log.e(TAG, "createPhoneEmailInformation, unexpected null cursor.");
    	return null;
    }
    
    private static QiupuAlbum createQiupuAlbumInformation(Cursor cursor) {
    	if (null != cursor) {
    		QiupuAlbum result = new QiupuAlbum();
    		result.album_id = cursor.getLong(cursor.getColumnIndex(QiupuAlbumColumns.ALBUM_ID));
    		result.user_id = cursor.getLong(cursor.getColumnIndex(QiupuAlbumColumns.USERID));
    		result.album_type = cursor.getInt(cursor.getColumnIndex(QiupuAlbumColumns.ALBUM_TYPE));
    		result.title = cursor.getString(cursor.getColumnIndex(QiupuAlbumColumns.TITLE));
    		result.summary = cursor.getString(cursor.getColumnIndex(QiupuAlbumColumns.SUMMARY));
    		result.privacy = cursor.getInt(cursor.getColumnIndex(QiupuAlbumColumns.PRIVACY)) == 0? false:true;
    		result.title = cursor.getString(cursor.getColumnIndex(QiupuAlbumColumns.TITLE));
    		result.created_time = cursor.getLong(cursor.getColumnIndex(QiupuAlbumColumns.CREATED_TIME));
    		result.have_expired = cursor.getInt(cursor.getColumnIndex(QiupuAlbumColumns.HAVE_EXPIRED)) == 0? false:true;
    		result.updated_time = cursor.getLong(cursor.getColumnIndex(QiupuAlbumColumns.UPDATED_TIME));
    		result.photo_count = cursor.getInt(cursor.getColumnIndex(QiupuAlbumColumns.PHOTO_COUNT));
    		result.album_cover_photo_middle = cursor.getString(cursor.getColumnIndex(QiupuAlbumColumns.ALBUM_COVER_MID));
    		return result;
    	}
    	
    	Log.e(TAG, "createPhoneEmailInformation, unexpected null cursor.");
    	return null;
    }
    
    private static QiupuPhoto createQiupuPhotoInformation(Cursor cursor) {
        if (null != cursor) {
        	QiupuPhoto result = new QiupuPhoto();
        	result.photo_id = cursor.getLong(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_ID));
    		result.album_id = cursor.getLong(cursor.getColumnIndex(QiupuPhotoColumns.ALBUM_ID));
    		result.uid = cursor.getLong(cursor.getColumnIndex(QiupuPhotoColumns.USERID));
    		result.photo_url_small = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_URL_SMALL));
    		result.photo_url_middle = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_URL_MIDDLE));
    		result.photo_url_big = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_URL_BIG));
    		result.photo_url_thumbnail = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_URL_THUMBNAIL));
    		result.photo_url_original = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.PHOTO_URL_ORIGINAL));
    		result.caption = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.CAPTION));
    		result.location = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.LOCATION));
            result.created_time = cursor.getLong(cursor.getColumnIndex(QiupuPhotoColumns.CREATED_TIME));
            result.iliked = cursor.getInt(cursor.getColumnIndex(QiupuPhotoColumns.ILIKEED)) == 0? false:true;
            result.likes_count = cursor.getInt(cursor.getColumnIndex(QiupuPhotoColumns.LIKES_COUNT));
            result.comments_count = cursor.getInt(cursor.getColumnIndex(QiupuPhotoColumns.COMMENTS_COUNT));
            result.from_user_id = cursor.getLong(cursor.getColumnIndex(QiupuPhotoColumns.FROM_USER_ID));
            result.from_nick_name = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.FROM_NICK_NAME));
            result.from_image_url = cursor.getString(cursor.getColumnIndex(QiupuPhotoColumns.FROM_IMAGE_URL));
            return result;
        }

        Log.e(TAG, "createPhoneEmailInformation, unexpected null cursor.");
        return null;
    }
    
    private static PerhapsName createPerhapsNameInformation(Cursor cursor) {
        if (null != cursor) {
            PerhapsName result = new PerhapsName();
            result.name = cursor.getString(cursor.getColumnIndex(PerhapsNameColumns.FULLNAME));
            result.count = cursor.getInt(cursor.getColumnIndex(PerhapsNameColumns.COUNT));
            return result;
        }
        Log.e(TAG, "createPerhapsNameInformation, unexpected null cursor.");
        return null;
    }
    
    public boolean getbindInfo(String content, long uid) {
        boolean ret = false;
        String where = PhoneEmailColumns.INFO + "='" + content + "' and " + PhoneEmailColumns.USERID + "=" + uid; 
        Cursor mCursor = mContext.getContentResolver().query(PHONEEMAIL_CONTENT_URI, new String[]{PhoneEmailColumns.ISBIND} , where, null, null);
        if(mCursor != null) {
            if(mCursor.moveToFirst()) {
                ret = mCursor.getInt(0) == 1;
            }
            mCursor.close();
            mCursor = null;
        }
        return ret;
    }
    
    public Cursor queryOneUserPhoneEmail(long uid) {
        String where = PhoneEmailColumns.USERID + "=" + uid;
        return mContext.getContentResolver().query(PHONEEMAIL_CONTENT_URI, PHONE_EMAIL_PROJECTION , where, null, null);
    }
    
    // just used old platform ---------
    public void updatePhoneEmailInfo(long uid, String type, String info) {
        String where = PhoneEmailColumns.USERID + "=" + uid + " and " + PhoneEmailColumns.TYPE + "='" + type + "'";
        ContentValues values = new ContentValues();
        values.put(PhoneEmailColumns.INFO, info);
        int count = mContext.getContentResolver().update(PHONEEMAIL_CONTENT_URI, values, where, null);
    }
    
    public String getInfoWhitType(String type, long uid) {
        String info = null;
        String where = PhoneEmailColumns.TYPE + "='" + type + "' and " + PhoneEmailColumns.USERID + "=" + uid; 
        Cursor mCursor = mContext.getContentResolver().query(PHONEEMAIL_CONTENT_URI, new String[]{PhoneEmailColumns.INFO} , where, null, null);
        if(mCursor != null) {
            if(mCursor.moveToFirst()) {
                info = mCursor.getString(0);
            }
            mCursor.close();
            mCursor = null;
        }
        return info;
    }
    //---------------

    public static boolean isConfirmLookup(Context context) {
        String value = getSettingValue(context, IS_CONFIRM_LOOKUP);
        if (value == null || value.equals("")) {
            return false;
        }

        if (QiupuConfig.DBLOGD)
            Log.d(TAG, "isConfirmLookup :" + value);
        return Integer.valueOf(value) == 1;
    }

    public static void setConfirmLookup(Context context, boolean enabled) {
   		addSetting(context, IS_CONFIRM_LOOKUP, enabled ? "1" : "0");
   	}
    
    public String getUserProfileImageUrl(long uid) {
        String profile_image = "";
        String where = UsersColumns.USERID + "=" + uid; 
        Cursor mCursor = mContext.getContentResolver().query(USERS_CONTENT_URI, new String[]{UsersColumns.PROFILE_IMAGE_URL} , where, null, null);
        if(mCursor != null) {
            if(mCursor.moveToFirst()) {
                profile_image = mCursor.getString(0);
            }
            mCursor.close();
            mCursor = null;
        }
        return profile_image;
    }
    
    private void buildWorkHistory(ArrayList<ContentValues> infoList, QiupuUser user) {
//        deleteWorkExperienceInfo(user.uid);
        final int arraySize = user.work_history_list != null ? user.work_history_list.size() : 0;
        if(arraySize > 0) {
//            ContentValues[] workHistoryCvs = new ContentValues[arraySize];
            for(int w = 0; w < arraySize; w++) {
                infoList.add(createWorkExperienceInformation(user.work_history_list.get(w)));
            }
        }
    }

    private void bulkInsertWorkHistory(ArrayList<ContentValues> cvs) {
        bulkInsert(WORK_EXPERIENCE_CONTENT_URI, cvs);
    }
    
    private void buildEducationInfo(ArrayList<ContentValues> infoList, QiupuUser user) {
//        deleteEducationInfo(user.uid);
        final int arraySize = user.education_list != null ? user.education_list.size() : 0;
        if(arraySize  > 0) {
//            ContentValues[] educationCvs = new ContentValues[user.education_list.size()];
            for(int i=0; i<user.education_list.size(); i++) {
                infoList.add(createEducationInformation(user.education_list.get(i)));
            }
        }
    }

    private void bulkInsertEducationHistory(ArrayList<ContentValues> cvs) {
        bulkInsert(EDUCATION_CONTENT_URI, cvs);
    }
    
    private void buildPhoneEmailInfo(ArrayList<ContentValues> infoList, QiupuAccountInfo user) {
        //delete PhoneEmail info
//        deletePhoneEmailInfo(user.uid);
        int arraySize = user.phoneList != null ? user.phoneList.size() : 0;
        if(arraySize  > 0) {
//            ContentValues[] phoneCvs = new ContentValues[user.phoneList.size()];
            for(int i=0;i<user.phoneList.size();i++){
                infoList.add(createPhoneEmailInfo(user.phoneList.get(i), UserProvider.TYPE_PHONE));
            }
//            mContext.getContentResolver().bulkInsert(PHONEEMAIL_CONTENT_URI, phoneCvs);
        }

        arraySize = user.emailList != null ? user.emailList.size() : 0;
        if(arraySize  > 0) {
//            ContentValues[] emailCvs = new ContentValues[user.emailList.size()];
            for(int i=0; i<user.emailList.size(); i++){
                infoList.add(createPhoneEmailInfo(user.emailList.get(i), UserProvider.TYPE_EMAIL));
            }

        }
    }

    private void bulkInsertPhoneEmailInfo(ArrayList<ContentValues> cvs) {
        bulkInsert(PHONEEMAIL_CONTENT_URI, cvs);
    }

    public long bullInsertFriendsList(List<QiupuUser> users) {
        if(users == null) {
            return -1L;
        }
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, start, count: " + users.size());
        final int userCount = users.size();
        Long[] ids = new Long[userCount];
        ContentValues[] cvs = new ContentValues[userCount];

        ArrayList<ContentValues> officeInfo = new ArrayList<ContentValues>();
        ArrayList<ContentValues> educationInfo = new ArrayList<ContentValues>();
        ArrayList<ContentValues> phoneEmailInfo = new ArrayList<ContentValues>();
//        ArrayList<ContentValues> imageInfo = new ArrayList<ContentValues>();
        ArrayList<ContentValues> possibleNameInfo = new ArrayList<ContentValues>();
        ArrayList<ContentValues> circleInfo = new ArrayList<ContentValues>();

        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, ready to build info");
        for(int i = 0; i < userCount; i++) {
            QiupuUser tmpUser = users.get(i);

            ids[i] = tmpUser.uid;
            cvs[i] = createUserInformationValues(tmpUser);
            
            buildWorkHistory(officeInfo, tmpUser);
            buildEducationInfo(educationInfo, tmpUser);
            buildPhoneEmailInfo(phoneEmailInfo, tmpUser);
//            buildUserImageInfo(imageInfo, tmpUser);
            buildPerhapsNameList(possibleNameInfo, tmpUser);
            buildUserCircles(circleInfo, tmpUser);
//            updateUserCircles(tmpUser.uid, tmpUser.circleId);
        }
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, info built.");

        final String idsSuffix = " in (" + TextUtils.join(",", ids) + ")";
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, deleting office info.");
        deleteWorkExperienceInfo(idsSuffix);
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing office info: " + officeInfo.size());
        bulkInsertWorkHistory(officeInfo);

        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, deleting education info.");
        deleteEducationInfo(idsSuffix);
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing education info: " + educationInfo.size());
        bulkInsertEducationHistory(educationInfo);

        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, deleting phone/email info.");
        deletePhoneEmailInfo(idsSuffix);
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing phone/email info: " + phoneEmailInfo.size());
        bulkInsertPhoneEmailInfo(phoneEmailInfo);

        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, deleting profile image info.");
        deleteUserImageInfo(idsSuffix);
//        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing profile image info: " + imageInfo.size());
//        bulkInsertUserImageInfo(imageInfo);

        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, deleting possible name info.");
        deletePerhapsName(idsSuffix);
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing possible name info: " + possibleNameInfo.size());
        bulkInsertPossibleNameInfo(possibleNameInfo);

        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, deleting circle info.");
        deleteUserCircles(idsSuffix);
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing circle info: " + circleInfo.size());
        bulkInsertUserCirclesInfo(circleInfo);

        //insert bull
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, writing user raw info: " + cvs.length);
        int count = mContext.getContentResolver().bulkInsert(USERS_CONTENT_URI, cvs);
        cvs = null;
        if (DEBUG) Log.d(TAG, "bullInsertFriendsList, all done: " + count);

        return count;
    }


//    public Uri updateHostConfiguration(String name, String value) {
//        return addSetting(mContext, HOST_SETTINGS_CONTENT_URI, name, value);
//    }

    public static String queryHostConfiguration(Context context, String appKey) {
//        String urlHost = null;
//        ContentResolver contentResolver = context.getContentResolver();
//        if (null == contentResolver) {
//            Log.w(TAG, "queryHostConfiguration, fail to get content resolver.");
//        } else {
//            final String[] projects =  new String[] {"_id", "name", "value", };
//            final String where = "name='" + appKey + "'";
//            Cursor cursor = null;
//            try {
//                cursor = contentResolver.query(HOST_SETTINGS_CONTENT_URI, projects, where, null, null);
//                if (null == cursor || cursor.getCount() == 0) {
//                    Log.w(TAG, "getCurrentServerUrl, fail to query cursor.");
//                } else {
//                    if (cursor.moveToFirst()) {
//                        urlHost = cursor.getString(cursor.getColumnIndex("value"));
//                    }
//                }
//            } finally {
//                if (cursor != null && !cursor.isClosed()) {
//                    cursor.close();
//                    cursor = null;
//                }
//            }
//        }
//
//        return urlHost;
        return Env.getHost(context, appKey);
    }

    public Cursor queryPublicCircles() {
    	String where = CircleColumns.TYPE + "=" + UserCircle.CIRLCE_TYPE_PUBLIC;
    	String orderBy = " type ASC";
    	final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
    			where, null, orderBy);
    	return cursor;        
    }
    
    public Cursor queryFreeCircleWithImage() {
    	return searchFreeCircleWithImage(null);
    }

    public Cursor searchFreeCircleWithImage(String key) {
    	return searchFreeCircleWithImage(key, false);
    }
    
    public Cursor searchFreeCircleWithImage(String key, boolean isTop) {
    	StringBuffer selectStr = new StringBuffer("select ");
    	for(int i = 0;i<USER_CIRCLE_PROJECTION.length;i++) {
    		if(i>0) {
    			selectStr.append(", circles.");
    		}else {
    			selectStr.append(" circles.");
    		}
    		selectStr.append(USER_CIRCLE_PROJECTION[i]);
    	}
    	selectStr.append(", groups." + GroupColumns.PROFILE_IMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.PROFILE_SIMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.PROFILE_LIMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.FORMAL);
    	selectStr.append(" from circles left join groups on circles."+CircleColumns.CIRCLE_ID+"=groups."+GroupColumns.CIRCLE_ID);
    	selectStr.append(" where  ");
    	selectStr.append(CircleColumns.TYPE+" = "+ UserCircle.CIRLCE_TYPE_PUBLIC + " and " + GroupColumns.FORMAL + " not in (" /*+ UserCircle.circle_sub_formal + ","*/ + UserCircle.circle_top_formal + ")");
    	if(isTop) {
    		selectStr.append(" and (" + GroupColumns.PARENT_ID + " is null or " +  GroupColumns.PARENT_ID + " <= 0 )");
    		
    	}
    	if(StringUtil.isValidString(key)) {
    		selectStr.append(" and " + CircleColumns.CIRCLE_NAME + " like \'%"+key+"%\'");
    	}
    	selectStr.append(" order by "+ CircleColumns.MEMBER_COUNT +" DESC ");
    	if(UserProvider.mOpenHelper != null)
    	{
    		SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
    		return db.rawQuery(selectStr.toString(), null);
    		
    	}
    	
    	return null;        
    }
    
    

    public static boolean hasHomeOption(Context context) {
//        if(UserProvider.mOpenHelper == null) {
//            Log.e(TAG, "skip with empty db helper in UserProvider.");
//            return false;
//        }
        UserProvider.ensureDatabase(context);

        StringBuffer selectStr = new StringBuffer("SELECT circles.").append(CircleColumns.ID);
        selectStr.append(" FROM circles LEFT JOIN groups ON circles.").append(CircleColumns.CIRCLE_ID)
                .append("=groups.").append(GroupColumns.CIRCLE_ID);
        selectStr.append(" WHERE  ")
                .append(CircleColumns.TYPE).append(" = ")
                .append(UserCircle.CIRLCE_TYPE_PUBLIC)
                .append(" AND ")
                .append(GroupColumns.FORMAL)
                .append(" IN (")
                .append(UserCircle.circle_top_formal)
                .append(")");

        SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectStr.toString(), null);
        final boolean ret = null != cursor && cursor.getCount() > 0;
        closeCursor(cursor);
        return ret;
    }
    
    public UserCircle queryFirstTopCircle() {
    	Cursor tmpCursor = queryTopCircle();
    	UserCircle tmpCircle = null;
    	if(tmpCursor != null) {
    		if(tmpCursor.moveToFirst()) {
    			tmpCircle = new UserCircle();
    			tmpCircle.name = tmpCursor.getString(tmpCursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
    			tmpCircle.circleid = tmpCursor.getLong(tmpCursor.getColumnIndex(CircleColumns.CIRCLE_ID));
    		}
    		tmpCursor.close();
    		tmpCursor = null;
    	}
    	return tmpCircle;
    }
    
    public Cursor queryOrganizationWithImage() {
    	return searchOrganizationWithImage(null);
    }
    
    public Cursor queryTopCircle() {
    	StringBuffer selectStr = new StringBuffer("select ");
    	for(int i = 0;i<USER_CIRCLE_PROJECTION.length;i++) {
    		if(i>0) {
    			selectStr.append(", circles.");
    		}else {
    			selectStr.append(" circles.");
    		}
    		selectStr.append(USER_CIRCLE_PROJECTION[i]);
    	}
    	selectStr.append(", groups." + GroupColumns.FORMAL);
    	selectStr.append(" from circles left join groups on circles."+CircleColumns.CIRCLE_ID+"=groups."+GroupColumns.CIRCLE_ID);
    	selectStr.append(" where  ");
    	selectStr.append(CircleColumns.TYPE+" = "+ UserCircle.CIRLCE_TYPE_PUBLIC + " and (" + GroupColumns.PARENT_ID + " is null or " +  GroupColumns.PARENT_ID + " <= 0 )");
    	selectStr.append(" order by "+ GroupColumns.FORMAL +" DESC ");
    	if(UserProvider.mOpenHelper != null)
    	{
    		SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
    		return db.rawQuery(selectStr.toString(), null);
    		
    	}
    	
    	return null;        
    }
    
    public Cursor searchOrganizationWithImage(String key) {
    	StringBuffer selectStr = new StringBuffer("select ");
    	for(int i = 0;i<USER_CIRCLE_PROJECTION.length;i++) {
    		if(i>0) {
    			selectStr.append(", circles.");
    		}else {
    			selectStr.append(" circles.");
    		}
    		selectStr.append(USER_CIRCLE_PROJECTION[i]);
    	}
    	selectStr.append(", groups." + GroupColumns.PROFILE_IMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.PROFILE_SIMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.PROFILE_LIMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.FORMAL);
    	selectStr.append(" from circles left join groups on circles."+CircleColumns.CIRCLE_ID+"=groups."+GroupColumns.CIRCLE_ID);
    	selectStr.append(" where  ");
    	selectStr.append(CircleColumns.TYPE+" = "+ UserCircle.CIRLCE_TYPE_PUBLIC + " and " + GroupColumns.FORMAL + " in (" /*+ UserCircle.circle_sub_formal + ","*/ + UserCircle.circle_top_formal + ")");
    	if(StringUtil.isValidString(key)) {
    		selectStr.append(" and " + CircleColumns.CIRCLE_NAME + " like \'%"+key+"%\'");
    	}
    	selectStr.append(" order by "+ CircleColumns.MEMBER_COUNT +" DESC ");
    	if(UserProvider.mOpenHelper != null)
    	{
    		SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
    		return db.rawQuery(selectStr.toString(), null);
    		
    	}
    	
    	return null;        
    }
    
    public UserCircle queryOneCircleWithImage(long circleid) {
    	StringBuffer selectStr = new StringBuffer("select ");
    	for(int i = 0;i<USER_CIRCLE_PROJECTION.length;i++) {
    		if(i>0) {
    			selectStr.append(", circles.");
    		}else {
    			selectStr.append(" circles.");
    		}
    		selectStr.append(USER_CIRCLE_PROJECTION[i]);
    	}
    	selectStr.append(", groups." + GroupColumns.PROFILE_IMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.PROFILE_SIMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.PROFILE_LIMAGE_URL);
    	selectStr.append(", groups." + GroupColumns.FORMAL);
    	selectStr.append(" from circles left join groups on circles."+CircleColumns.CIRCLE_ID+"=groups."+GroupColumns.CIRCLE_ID);
    	selectStr.append(" where  ");
    	selectStr.append("circles." + CircleColumns.CIRCLE_ID+" = "+ circleid) ;
    	if(UserProvider.mOpenHelper != null)
    	{
    		SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
    		Cursor tmpCursor = db.rawQuery(selectStr.toString(), null);
    		if(tmpCursor != null && tmpCursor.moveToFirst()) { 
    			UserCircle circle = createCircleInformationWithImage(tmpCursor);
    			return circle;
    		}else {
    			return null;
    		}
    	}
    	
    	return null;        
    }
    
    public Cursor queryLocalCircles() {
        String where = CircleColumns.TYPE + "=" + UserCircle.CIRCLE_TYPE_LOCAL + " and " + CircleColumns.CIRCLE_ID + " not in(" + CircleUtils.getAllFilterCircleIds() + ")";
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                where, null, null);
        return cursor;
    }
    
    public ArrayList<UserCircle> queryLocalCircleList(){
        ArrayList<UserCircle> list = new ArrayList<UserCircle>();
        Cursor cursor = queryLocalCircles();
        if(cursor !=null) {
            if (cursor.getCount()>0) {
                while(cursor.moveToNext()) {
                	list.add(createCircleInformation(cursor));
                }
            }
            cursor.close();
        }
        return list;
    }
    
    public Cursor queryEvents() {
    	 String where = CircleColumns.TYPE + "=" + UserCircle.CIRCLE_TYPE_EVENT;
         String orderBy = " type ASC";
         final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
                 where, null, orderBy);
         return cursor; 
    } 
    
    private Cursor queryEvents(String where) {
    	Cursor cursor = mContext.getContentResolver().query(CIRCLE_GROUP_URI, new String[]{GroupColumns.CIRCLE_ID} ,
                where, null, null);
    	StringBuilder Ids = new StringBuilder(); 
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				if(Ids.length() > 0) {
    					Ids.append(",");
    				}
    				Ids.append(cursor.getLong(cursor.getColumnIndex(GroupColumns.CIRCLE_ID)));
    			} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	
    	String upcomingWhere = CircleColumns.CIRCLE_ID + " in (" + Ids.toString() + ") and " +  CircleColumns.TYPE + "=" + UserCircle.CIRCLE_TYPE_EVENT;
        String orderBy = " type ASC";
        final Cursor upcomingCursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, USER_CIRCLE_PROJECTION ,
        		upcomingWhere, null, orderBy);
        return upcomingCursor;
    }
    
    public Cursor queryPastEvents() {
    	long currentTime = System.currentTimeMillis();
    	String where = GroupColumns.END_TIME + ">0 and " + GroupColumns.END_TIME + "<" + currentTime;
    	return queryEvents(where);
    }
    
    public Cursor queryUpcomingEvents() {
    	long currentTime = System.currentTimeMillis();
    	String where = GroupColumns.END_TIME + "=0 or " + GroupColumns.END_TIME + ">=" + currentTime;
    	return queryEvents(where);
    }
    
    public void insertMessage(Context context, QiupuUser qiupuUser, String message, int type) {
        ContentValues values = new ContentValues();
        values.put(ChatRecordColumns.BORQS_ID, qiupuUser.uid);
        values.put(ChatRecordColumns.MESSAGE, message);
        values.put(ChatRecordColumns.PROFILE_URL, qiupuUser.profile_image_url);
        values.put(ChatRecordColumns.TYPE, type);
        values.put(ChatRecordColumns.CREATED_TIME, System.currentTimeMillis());
        values.put(ChatRecordColumns.UNREAD, true);
        values.put(ChatRecordColumns.DISPLAY_NAME, qiupuUser.nick_name);
        context.getContentResolver().insert(QiupuORM.CHAT_RECORD_URI, values);
    }

    public Cursor queryChatRecord(String uid) {
        String selection = ChatRecordColumns.BORQS_ID + " = " + uid;
//        String sortOrder = ChatRecordColumns.CREATED_TIME + " DESC";
        String sortOrder = ChatRecordColumns.ID + " DESC";
        return mContext.getContentResolver().query(CHAT_RECORD_URI, CHAT_PROJECTION, selection, null, sortOrder);
    }

    public long bulkInsertChatRecord(ArrayList<ChatInfo> chatList) {
        if(chatList == null) {
            return -1L;
        }

        ContentValues []cvs = new ContentValues[chatList.size()];
        for(int i=0; i < chatList.size(); i++) {
            cvs[i] = createChatRecordValues(chatList.get(i));
        }

        //insert bulk
        int count = mContext.getContentResolver().bulkInsert(CHAT_RECORD_URI, cvs);
        cvs = null;

        return count;
    }

    private ContentValues createChatRecordValues(ChatInfo info){
        ContentValues cv = new ContentValues();

        cv.put(ChatRecordColumns.BORQS_ID, info.uid);
        cv.put(ChatRecordColumns.MESSAGE, info.msg);
        cv.put(ChatRecordColumns.PROFILE_URL, info.profile_url);
        cv.put(ChatRecordColumns.TYPE, info.type);
        cv.put(ChatRecordColumns.UNREAD, info.unread == true ? 0 : 1);
        cv.put(ChatRecordColumns.CREATED_TIME, info.created_time);
        cv.put(ChatRecordColumns.DISPLAY_NAME, info.display_name);

        return cv;
    }

    public ArrayList<ChatInfo> createChatInfoObject(Cursor cursor, int begin, int offset) {
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }

        ArrayList<ChatInfo> chatList = new ArrayList<ChatInfo>();

        if (begin == -1) {
            cursor.moveToFirst();
        } else {
            cursor.moveToPosition(begin);
        }

        for (int i = 0; i < offset; i++) {
            ChatInfo info = new ChatInfo();
            info.uid = cursor.getLong(cursor.getColumnIndex(ChatRecordColumns.BORQS_ID));
            info.msg = cursor.getString(cursor.getColumnIndex(ChatRecordColumns.MESSAGE));
            info.profile_url = cursor.getString(cursor.getColumnIndex(ChatRecordColumns.PROFILE_URL));
            info.type = cursor.getInt(cursor.getColumnIndex(ChatRecordColumns.TYPE));
            info.unread = cursor.getInt(cursor.getColumnIndex(ChatRecordColumns.UNREAD)) == 0 ? true : false;
            info.created_time = cursor.getLong(cursor.getColumnIndex(ChatRecordColumns.CREATED_TIME));
            info.display_name = cursor.getString(cursor.getColumnIndex(ChatRecordColumns.DISPLAY_NAME));
            chatList.add(0, info);
            Log.d(TAG, "========= \n info = \n" + info.toString());
            cursor.moveToNext();
        }

        return chatList;
    }

    public void updateLocalUserCircleInfo(ArrayList<Long> joinIds, long circleId, String circleName) {
        for(int i=0; i<joinIds.size(); i++) {
            long uid = joinIds.get(i);
            if(isUserExist(uid)) {
                String circleids = "";
                String circleNames = "";
                getCircleIdsNamesByUid(uid, circleids, circleNames);
                
                if(circleids != null && circleids.length() > 0) {
                    circleids = circleids + "," + circleId;
                }else {
                    circleids = String.valueOf(circleId);
                }
                
                if(circleNames != null && circleNames.length() > 0) {
                    circleNames = circleNames + "," + circleName;
                }else {
                    circleNames = circleName;
                }
                
                updateCircleIdsNames(uid, circleids, circleNames);
                
                insertCircleUserTable(circleId, uid);
            }else {
                if(QiupuConfig.DBLOGD)Log.d(TAG, "the user is not my friends " + uid);
            }
        }
    }
    
    private void updateCircleIdsNames(long uid, String circleIds, String circleName) {
        ContentValues cv = new ContentValues();
        cv.put(UsersColumns.CIRCLE_ID, circleIds);
        cv.put(UsersColumns.CIRCLE_NAME, circleName);
        String where =  UsersColumns.USERID + "=" + uid;
        mContext.getContentResolver().update(USERS_CONTENT_URI, cv,where,null);
    }
    private void getCircleIdsNamesByUid(long uid, String circleIds, String circleName) {
        Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,new String[]{UsersColumns.CIRCLE_ID, UsersColumns.CIRCLE_NAME} , queryByUID, new String[]{String.valueOf(uid)}, null);
        if(cursor != null) {
            if (cursor.moveToFirst()){
                circleIds = cursor.getString(cursor.getColumnIndex(UsersColumns.CIRCLE_ID));
                circleName = cursor.getString(cursor.getColumnIndex(UsersColumns.CIRCLE_NAME));
            }
            cursor.close();
        }
    }

//    public String queryUserNamesByIds(String selectedIds) {
//    	String where = UsersColumns.USERID + " in (" + selectedIds + ")";
//    	Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,new String[]{UsersColumns.NICKNAME} , where, null, null);
//    	StringBuilder names = new StringBuilder();
//    	if(cursor != null) {
//    		if(cursor.moveToFirst()) {
//    			do {
//    				String nick_name = cursor.getString(cursor.getColumnIndex(UsersColumns.NICKNAME));
//    				if(nick_name != null) {
//    					if(names.length() > 0) {
//    						names.append(",");
//    					}
//    					names.append(nick_name);
//    				}
//    			} while (cursor.moveToNext());
//    		}
//    		cursor.close();
//    		cursor = null;
//    	}
//    	return names.toString();
//    }
//
//    public String queryPublicCircleNamesByIds(String selectedIds) {
//        String where = CircleColumns.TYPE + "=" + UserCircle.CIRLCE_TYPE_PUBLIC + " AND " + CircleColumns.USERID + " in (" + selectedIds + ")";
//        Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI,new String[]{CircleColumns.CIRCLE_NAME} , where, null, null);
//        StringBuilder names = new StringBuilder();
//        if(cursor != null) {
//            if(cursor.moveToFirst()) {
//            	do {
//            		String nick_name = cursor.getString(cursor.getColumnIndex(CircleColumns.CIRCLE_NAME));
//            		if(nick_name != null) {
//            			if(names.length() > 0) {
//            				names.append(",");
//            			}
//            			names.append(nick_name);
//            		}
//            	} while (cursor.moveToNext());
//            }
//            cursor.close();
//            cursor = null;
//        }
//        return names.toString();
//    }

    public static boolean isEnableFindContacts(Context context) {
        String value = getSettingValue(context, ENABLE_FIND_CONTACT);
        if (value == null || value.equals("")) {
            return false;
        }

        if (QiupuConfig.DBLOGD)
            Log.d(TAG, "isEnableFindContacts :" + value);
        return Integer.valueOf(value) == 1;
    }

    public static void enableFindContacts(Context context, boolean enabled) {
        addSetting(context, ENABLE_FIND_CONTACT, enabled ? "1" : "0");
    }

    public String queryInviteIds(long circleId) {
    	String inviteString = "";
    	String where = GroupColumns.CIRCLE_ID + "=" + circleId;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_GROUP_URI, new String[]{GroupColumns.INVITE_IDS} , where, null, null);
		if(cursor != null) {
			if(cursor.moveToFirst()) {
				inviteString = cursor.getString(cursor.getColumnIndex(GroupColumns.INVITE_IDS));
			}
		}
		return inviteString;
    }
    
    public void updateInviteIds(String selectUid, long circleId) {
    	if(selectUid != null && selectUid.length() > 0) {
    		String invited_ids = queryInviteIds(circleId);
    		StringBuilder idsbuilder = new StringBuilder();
    		if(invited_ids != null) {
    			idsbuilder.append(invited_ids);
    		}
    		
    		if(idsbuilder.length() > 0) {
				idsbuilder.append(",");
			}
			idsbuilder.append(selectUid);
			
    		//update inviteids to db
			String where = GroupColumns.CIRCLE_ID + "=" + circleId;
    		ContentValues cv = new ContentValues();
    		cv.put(GroupColumns.INVITE_IDS, idsbuilder.toString());
    		mContext.getContentResolver().update(CIRCLE_GROUP_URI, cv, where, null);
    	}
    }

    public static void acceptUser(Context context, String userId) {
        final ContentResolver contentResolver = context.getContentResolver();
        final String where =  UsersColumns.USERID + "=" + userId;
        Cursor cursor = contentResolver.query(USERS_CONTENT_URI, new String[]{UsersColumns.REFERRED_COUNT}, where, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(cursor.getColumnIndex(UsersColumns.REFERRED_COUNT)) + 1;
                ContentValues values = new ContentValues();
                values.put(UsersColumns.REFERRED_COUNT, count);
                if (contentResolver.update(USERS_CONTENT_URI, values, where, null) < 1) {
                    Log.d(TAG, "acceptUser, failed to update reference count: " + count);
                }
            }
        }
        closeCursor(cursor);
    }
    
    public static void acceptEmpolyee(Context context, String userId) {
        final ContentResolver contentResolver = context.getContentResolver();
        String sceneId = getSettingValue(context, HOME_ACTIVITY_ID);
        String where = null;
        if(TextUtils.isEmpty(sceneId)) {
        	where =  EmployeeColums.USER_ID + "=" + userId;
        }else {
        	where =  EmployeeColums.USER_ID + "=" + userId + " and " + EmployeeColums.OWNER_ID + " = " + sceneId ;
        }
        Cursor cursor = contentResolver.query(EMPLOYEE_CONTENT_URI, new String[]{EmployeeColums.REFERRED_COUNT}, where, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(cursor.getColumnIndex(EmployeeColums.REFERRED_COUNT)) + 1;
                ContentValues values = new ContentValues();
                values.put(EmployeeColums.REFERRED_COUNT, count);
                if (contentResolver.update(EMPLOYEE_CONTENT_URI, values, where, null) < 1) {
                    Log.d(TAG, "acceptEmpolyee, failed to update reference count: " + count);
                }
            }
        }
        closeCursor(cursor);
    }
    
    public static void acceptCircle(Context context, String circleId) {
        final ContentResolver contentResolver = context.getContentResolver();
        final String where =  CircleCirclesColumns.CIRCLEID + "=" + circleId;
        Cursor cursor = contentResolver.query(CIRCLE_CIRCLES_CONTENT_URI, new String[]{CircleCirclesColumns.REFERRED_COUNT}, where, null, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(cursor.getColumnIndex(CircleCirclesColumns.REFERRED_COUNT)) + 1;
                ContentValues values = new ContentValues();
                values.put(CircleCirclesColumns.REFERRED_COUNT, count);
                if (contentResolver.update(CIRCLE_CIRCLES_CONTENT_URI, values, where, null) < 1) {
                    Log.d(TAG, "acceptCircle, failed to update reference count: " + count);
                }
            }
        }
        closeCursor(cursor);
    }

    public static String parseUserName(Cursor cursor) {
        if (null != cursor) {
            int index = cursor.getColumnIndex(UsersColumns.NICKNAME);
            String name = index < 0 ? null : cursor.getString(index);
            if (TextUtils.isEmpty(name)) {
                index = cursor.getColumnIndex(EmployeeColums.NAME);
                if (index >= 0) {
                    name = cursor.getString(index);
                }
            }

            return TextUtils.isEmpty(name) ? null : name.trim();
        } else {
            return null;
        }
    }

    public static String parseUserId(Cursor cursor) {
        if (null != cursor) {
            int index = cursor.getColumnIndex(UsersColumns.USERID);
            String userId = index < 0 ? null : cursor.getString(index);
            if (TextUtils.isEmpty(userId)) {
                index = cursor.getColumnIndex(EmployeeColums.USER_ID);
                if (index >= 0) {
                    userId = cursor.getString(index);
                }
            }

            return TextUtils.isEmpty(userId) ? null : userId.trim();
        } else {
            return null;
        }
    }

    public static String parseUserProfileImage(Cursor cursor) {
        if (null == cursor) {
            return null;
        } else {
            int index = cursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL);
            String url = index < 0 ? null : cursor.getString(index);
            if (TextUtils.isEmpty(url)) {
                index = cursor.getColumnIndex(EmployeeColums.IMAGE_URL_M);
                if (index >= 0) {
                    url = cursor.getString(index);
                }
            }
            return url;
        }
    }

    private final static String[] PRO_USER = {
            UsersColumns.ID,
            UsersColumns.USERID,
            UsersColumns.NICKNAME,
            UsersColumns.PROFILE_IMAGE_URL,//image url
    };
    
    public Cursor queryFrequentlyCircle(int limitCount, String filterString) {
    	StringBuffer selectStr = new StringBuffer("SELECT ");
    	selectStr.append(TextUtils.join(",", USER_CIRCLE_PROJECTION));
    	selectStr.append(" FROM circles");

    	String where = CircleColumns.REFERRED_COUNT + " > 0";
    	if(StringUtil.isValidString(filterString)) {
    		where = where + " AND " + CircleColumns.CIRCLE_NAME + " like '%" + filterString + "%'";
    	}
    	selectStr.append(" WHERE " + where);
    	final String sortBy = CircleColumns.REFERRED_COUNT + " DESC," + CircleColumns.CIRCLE_ID +" ASC ";
    	selectStr.append(" ORDER BY ").append(sortBy);
        if(limitCount > 0) {
        	selectStr.append(" LIMIT " + limitCount);
        }

        if (UserProvider.mOpenHelper != null) {
            SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
            Cursor result = db.rawQuery(selectStr.toString(), null);
            return result;
        }
        return null;
    }
    
    public static Cursor querySuggestionUser(CharSequence constraint, ArrayList<String> excludedList) {
    	return querySuggestionUser(constraint, excludedList, -1);
    }
    
    public static Cursor querySuggestionUser(CharSequence constraint, ArrayList<String> excludedList, int limitCount) {
//        String query;
//        if (constraint == null) {
//            query = null;
//        } else {
//            String cons = constraint.toString().trim().replace("'", "");
//            String[] strSplit = cons.split(",");
//            final String arg = strSplit[strSplit.length - 1];
//            query = UsersColumns.NICKNAME + " LIKE '%" + arg + "%' OR " +
//                    UsersColumns.NAME_PINGYIN + " LIKE '%" + arg + "%'";
//        }
//
//        if (null == excludedList || excludedList.isEmpty()) {
//            // do nothing, and keep query value.
//        } else {
//            final String exclusion = QiupuORM.UsersColumns.USERID + " NOT IN (" + TextUtils.join(",", excludedList) + ")";
//            query = TextUtils.isEmpty(query) ? exclusion : exclusion + " AND (" + query + ")";
//
//        }
//
//        final String sortBy = UsersColumns.REFERRED_COUNT + " DESC ," +
//                UsersColumns.NAME_PINGYIN + " ASC, " + UsersColumns.NICKNAME + " ASC";
//
//        Cursor cursor = mContentResolver.query(QiupuORM.USERS_CONTENT_URI, PRO_USER, query, null, sortBy);
//        return cursor;

        if (QiupuApplication.mTopOrganizationId == QiupuApplication.VIEW_MODE_PERSONAL) {
            String query;
            if (constraint == null) {
                query = null;
            } else {
                String cons = constraint.toString().trim().replace("'", "");
                String[] strSplit = cons.split(",");
                final String arg = strSplit[strSplit.length - 1];
                query = "(" + UsersColumns.NICKNAME + " LIKE '%" + arg + "%' OR " +
                        UsersColumns.NAME_PINGYIN + " LIKE '%" + arg + "%')";
            }

            if (null == excludedList || excludedList.isEmpty()) {
                // do nothing, and keep query value.
            } else {
                final String exclusion = QiupuORM.UsersColumns.USERID + " NOT IN (" + TextUtils.join(",", excludedList) + ")";
                query = TextUtils.isEmpty(query) ? exclusion : exclusion + " AND (" + query + ")";
            }
            
            if(limitCount > 0) {
            	if(TextUtils.isEmpty(query)) {
            		query = UsersColumns.REFERRED_COUNT + " > 0" ;
            	}else {
            		query = query + " AND " + UsersColumns.REFERRED_COUNT + " > 0" ;
            	}
            	Log.d(TAG, "query: " + query);
            }

            final String sortBy = UsersColumns.REFERRED_COUNT + " DESC," +
                    UsersColumns.NAME_PINGYIN + " ASC, " + UsersColumns.NICKNAME + " ASC";

            StringBuffer selectStr = new StringBuffer("SELECT ");
            selectStr.append(TextUtils.join(",", PRO_USER));

            selectStr.append(" FROM users");
            if (!TextUtils.isEmpty(query)) {
                selectStr.append(" WHERE ").append(query);
            }

            if (Integer.parseInt(Utilities.getSdkVersion()) > 8) {
                selectStr.append(" GROUP BY ").append(UsersColumns.USERID);
            }

            selectStr.append(" ORDER BY ").append(sortBy);
            if(limitCount > 0) {
            	selectStr.append(" LIMIT " + limitCount);
            }

            if (UserProvider.mOpenHelper != null) {
                SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
                Cursor result = db.rawQuery(selectStr.toString(), null);
                return result;
            }

            return null;
        } else {
            String query;
            if (constraint == null) {
                query = null;
            } else {
                String cons = constraint.toString().trim().replace("'", "");
                String[] strSplit = cons.split(",");
                final String arg = strSplit[strSplit.length - 1];
                query = "users." + UsersColumns.NICKNAME + " LIKE '%" + arg + "%' OR " +
                        "users." + UsersColumns.NAME_PINGYIN + " LIKE '%" + arg + "%' OR " +
                        "employee." + EmployeeColums.NAME_PINYIN + " LIKE '%" + arg + "%' OR " +
                        "employee." + EmployeeColums.NAME + " LIKE '%" + arg + "%'";
            }

            if (null == excludedList || excludedList.isEmpty()) {
                // do nothing, and keep query value.
            } else {
                final String exclusion = "users." + UsersColumns.USERID + " NOT IN (" + TextUtils.join(",", excludedList) + ")";
                query = TextUtils.isEmpty(query) ? exclusion : exclusion + " AND (" + query + ")";

            }

            final String sortBy = "users." + UsersColumns.REFERRED_COUNT + " DESC ," +
                    "users." + UsersColumns.NAME_PINGYIN + " ASC, " + "users." + UsersColumns.NICKNAME + " ASC";

            StringBuffer selectStr = new StringBuffer("SELECT ");
            boolean isFirst = true;
            for(String field : PRO_USER) {
                if(isFirst) {
                    isFirst = false;
                    selectStr.append(" users.");
                }else {
                    selectStr.append(", users.");
                }
                selectStr.append(field);
            }

            for (String field : EmployeeColums.PROJECTION) {
                selectStr.append(", employee.").append(field);
            }

            selectStr.append(" FROM employee LEFT JOIN users ON users.").append(UsersColumns.USERID).
                    append("=employee.").append(EmployeeColums.USER_ID);

            if (QiupuApplication.mTopOrganizationId == QiupuApplication.VIEW_MODE_PERSONAL) {
//            selectStr.append(" WHERE employee.").append(EmployeeColums.OWNER_ID).
//                    append("=-1");
                if (!TextUtils.isEmpty(query)) {
                    selectStr.append(" WHERE ").append(query);
                }

            } else {
                selectStr.append(" WHERE (employee.").append(EmployeeColums.OWNER_ID).
                        append("=-1 OR employee.").append(EmployeeColums.OWNER_ID).append("=").
                        append(QiupuApplication.mTopOrganizationId.circleid).append(")");
                selectStr.append(" AND (").append(query).append(")");
            }

            if (Integer.parseInt(Utilities.getSdkVersion()) > 8) {
                selectStr.append(" GROUP BY employee.").append(EmployeeColums.USER_ID);
            }

            selectStr.append(" ORDER BY ").append(sortBy).append(", employee.").append(EmployeeColums.NAME_PINYIN);

            if(limitCount > 0) {
            	selectStr.append(" LIMIT " + limitCount);
            }
            
            if(UserProvider.mOpenHelper != null) {
                SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
                Cursor result = db.rawQuery(selectStr.toString(), null);
                return result;
            }

            return null;
        }
    }
//
//    public static void acceptUsers(Context context, ArrayList<String> mUserIdList) {
//        for (String userId: mUserIdList) {
//            acceptUser(context, userId);
//        }
//    }

    // TODO: improve the performance of DB operation, especially for those
    // query a full user info and then update it totally.
    public void updateUserInfoToDb(String uid, String circleid, boolean isadd) {
        if (LowPerformance) Log.d(TAG, "updateUserInfoToDb enter.");
        QiupuUser dbUser = queryOneUserInfo(Long.parseLong(uid));

        if (dbUser != null) {
            String circleIds = "";
            String circleNames = "";
            if (isadd) {
                circleIds = parseUserCircleIdIsadd(dbUser.circleId, circleid);
                circleNames = parseUserCircleNameIsadd(dbUser.circleName, circleid);
            } else {
                circleIds = parseUserCircleIdIsDelete(dbUser.circleId, circleid);
                circleNames = parseUserCircleNameIsDelete(dbUser.circleName, circleid);
            }
            dbUser.circleId = circleIds;
            dbUser.circleName = circleNames;
            if (circleIds.length() <= 0 || circleIds.equals(String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE))) {
                deleteFriendsinfo(dbUser.uid);
                // update privacy circle count;
                UserCircle tmpCircle = queryOneCircle(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.ADDRESS_BOOK_CIRCLE);
                tmpCircle.memberCount = tmpCircle.memberCount - 1;
                updateCircleInfo(tmpCircle);
            } else {
                updateFriendsInfo(dbUser);
            }
        }
        if (LowPerformance) Log.d(TAG, "updateUserInfoToDb exit.");
    }

    private String parseUserCircleNameIsadd(String dbcircleName, String circleid) {
        String localName = CircleUtils.getLocalCircleName(mContext, Long.parseLong(circleid), "");
        if (localName.length() <= 0) {
            UserCircle circleinfo = queryOneCricleInfo(circleid);
            if (dbcircleName.length() > 0) {
                if (circleinfo != null)
                    return dbcircleName + "," + circleinfo.name;
            } else {
                if (circleinfo != null)
                    return circleinfo.name;
            }
        }
        return dbcircleName;
    }

    private String parseUserCircleNameIsDelete(String circleName, String circleid) {
        StringBuilder namebuilder = new StringBuilder();
        UserCircle cricleinfo = queryOneCricleInfo(circleid);
        if (cricleinfo != null) {
            String[] names = circleName.split(",");
            for (int i = 0; i < names.length; i++) {
                if (!cricleinfo.name.equals(names[i])) {
                    if (namebuilder.length() > 0) {
                        namebuilder.append(",");
                    }
                    namebuilder.append(names[i]);
                }
            }
        } else {
            namebuilder.append(circleName);
        }

        return namebuilder.toString();
    }

    private static String parseUserCircleIdIsadd(String circleId, String circleid) {
        if (circleId != null && circleId.length() > 0) {
            return circleId + "," + circleid;
        } else {
            return circleid;
        }
    }

    private static String parseUserCircleIdIsDelete(String circleId, String circleid) {
        StringBuilder idbuilder = new StringBuilder();
        String[] ids = circleId.split(",");
        for (int i = 0; i < ids.length; i++) {
            if (!ids[i].equals(circleid)) {
                if (idbuilder.length() > 0) {
                    idbuilder.append(",");
                }
                idbuilder.append(ids[i]);
            }
        }
        return idbuilder.toString();
    }

    public String queryUserName(long uid) {
        if (LowPerformance) Log.d(TAG, "queryUserName enter.");
        String userName = null;
        String where = UsersColumns.USERID + "=" + uid;
        Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,
                new String[]{UsersColumns.NICKNAME}, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userName = cursor.getString(cursor.getColumnIndex(UsersColumns.NICKNAME));
            }
        }
        if (LowPerformance) Log.d(TAG, "queryUserName exit.");
        return userName;
    }
    
    public String queryEmployeeName(long sceneId, long uid) {
        if (LowPerformance) Log.d(TAG, "queryUserName enter.");
        String userName = null;
        String where = EmployeeColums.OWNER_ID + "=" + sceneId + " and " + EmployeeColums.USER_ID + " = " + uid;
        Cursor cursor = mContext.getContentResolver().query(EMPLOYEE_CONTENT_URI,
                new String[]{EmployeeColums.NAME}, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                userName = cursor.getString(cursor.getColumnIndex(EmployeeColums.NAME));
            }
        }
        if (LowPerformance) Log.d(TAG, "queryUserName exit.");
        return userName;
    }

    public boolean isExistingUser(long uid) {
        if (LowPerformance) Log.d(TAG, "isExistingUser enter.");
        boolean existing = false;
        String where = UsersColumns.USERID + "=" + uid;
        Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,
                new String[]{UsersColumns.ID}, where, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                existing = true;
            }
        }
        if (LowPerformance) Log.d(TAG, "isExistingUser exit.");
        return existing;
    }
    
    public void checkExpandCirCle() {
    	UserCircle circle = queryOneCircle(AccountServiceUtils.getBorqsAccountID(), QiupuConfig.CIRCLE_ID_PUBLIC);
    	if(circle == null) {
    		deleteCirclesWithIds(CircleUtils.getNativeCircleIds());
        	insertExpandCirCleInfo();
    	}
    }
    
    public void deleteCirclesWithIds(String circleIds) {
    	String where = CircleColumns.CIRCLE_ID +" in (" + circleIds+")";
    	int count = mContext.getContentResolver().delete(CIRCLES_CONTENT_URI, where,null);
    	
    	//delete group info from group table
    	String groupWhere = GroupColumns.CIRCLE_ID +" in (" + circleIds + ")";
    	mContext.getContentResolver().delete(CIRCLE_GROUP_URI, groupWhere,null);
    	if(count <=0){
    		Log.d(TAG, "Fail to delete circle from database=");
    	}
    }

    public int getRequestCount(String types) {
    	String where = null;
    	if(StringUtil.isValidString(types)) {
    		where = RequestColumns.TYPE + " in (" + types + ")";
    	}
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(RequestProvider.CONTENT_REQUEST_URI, new String[]{RequestColumns.REQUEST_ID}, where, null, null);
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        } finally {
            closeCursor(cursor);
        }
    }

    public ArrayList<Requests> buildRequestList(String type, final long sceneId) {
//    	if(StringUtil.isEmpty(type)) {
//    		Log.d(TAG, "request type in empty");
//    		return null;
//    	}
    	
        String where = RequestColumns.SCENEID + " = " + sceneId;
        if(!StringUtil.isEmpty(type)) {
        	where = where + " and " + RequestColumns.TYPE + " in (" + type + ")";
        }
        Cursor cursor = null;
        ArrayList<Requests> requestList = new ArrayList<Requests>();
        try {
            cursor = mContext.getContentResolver().query(RequestProvider.CONTENT_REQUEST_URI, REQUEST_PROJECTION, where, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.d(TAG, "exchange request : count = " + cursor.getCount());
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    Requests request = new Requests();
                    request.rid = cursor.getString(cursor.getColumnIndex(RequestColumns.REQUEST_ID));
                    request.type = cursor.getInt(cursor.getColumnIndex(RequestColumns.TYPE));
                    request.message = cursor.getString(cursor.getColumnIndex(RequestColumns.MESSAGE));
                    request.data = cursor.getString(cursor.getColumnIndex(RequestColumns.DATA));
                    request.createTime = cursor.getLong(cursor.getColumnIndex(RequestColumns.CREATE_TIME));
                    int uid = cursor.getInt(cursor.getColumnIndex(RequestColumns.UID));
                    if (uid > 0) {
                        QiupuUser user = new QiupuUser();
                        user.uid = uid;
                        user.nick_name = cursor.getString(cursor.getColumnIndex(RequestColumns.NICK_NAME));
                        user.profile_image_url = cursor.getString(cursor.getColumnIndex(RequestColumns.PROFILE_IMAGE_URL));

                        ArrayList<PhoneEmailInfo> phoneList = new ArrayList<PhoneEmailInfo>();
                        String phone_1 = cursor.getString(cursor.getColumnIndex(RequestColumns.PHONE_1));
                        phoneList.add(createPhoneEmailInfo(user, phone_1, QiupuConfig.TYPE_PHONE1));
                        String phone_2 = cursor.getString(cursor.getColumnIndex(RequestColumns.PHONE_2));
                        phoneList.add(createPhoneEmailInfo(user, phone_2, QiupuConfig.TYPE_PHONE2));
                        String phone_3 = cursor.getString(cursor.getColumnIndex(RequestColumns.PHONE_3));
                        phoneList.add(createPhoneEmailInfo(user, phone_3, QiupuConfig.TYPE_PHONE3));

//                        for (int j = 0; j < phoneList.size(); j++) {
//                            Log.d(TAG, "phone[" + j + "] = " + phoneList.get(j));
//                        }

                        ArrayList<PhoneEmailInfo> emailList = new ArrayList<PhoneEmailInfo>();
                        String email_1 = cursor.getString(cursor.getColumnIndex(RequestColumns.EMAIL_1));
                        emailList.add(createPhoneEmailInfo(user, email_1, QiupuConfig.TYPE_EMAIL1));
                        String email_2 = cursor.getString(cursor.getColumnIndex(RequestColumns.EMAIL_2));
                        emailList.add(createPhoneEmailInfo(user, email_2, QiupuConfig.TYPE_EMAIL2));
                        String email_3 = cursor.getString(cursor.getColumnIndex(RequestColumns.EMAIL_3));
                        emailList.add(createPhoneEmailInfo(user, email_3, QiupuConfig.TYPE_EMAIL3));

//                        for (int j = 0; j < emailList.size(); j++) {
//                            Log.d(TAG, "email[" + j + "] = " + emailList.get(j));
//                        }

                        user.phoneList.addAll(phoneList);
                        user.emailList.addAll(emailList);

                        request.user = user;
                    }

                    requestList.add(request);
                    cursor.moveToNext();
                }
            }
            return requestList;
        } finally {
            closeCursor(cursor);
        }
        
    }

    private PhoneEmailInfo createPhoneEmailInfo(QiupuUser user, String content, String type) {
        PhoneEmailInfo phoneEmail = new PhoneEmailInfo();
        phoneEmail.uid = user.uid;
        phoneEmail.type = type;
        phoneEmail.info = content;
        return phoneEmail;
    }

    public void cacheRequests(final ArrayList<Requests> requestList, final String types, final long sceneId) {
    	Log.d(TAG, "cacheRequests() size = " + requestList.size());
    	QiupuORM.sWorker.post(new Runnable() {

			@Override
			public void run() {
//				Log.i(TAG, "cacheRequests begin ------ ");

//                SQLiteDatabase db = null;
//                try {
//                    db = RequestProvider.mOpenHelper.getReadableDatabase();
//                    db.beginTransaction();

                     //TODO: just a work-around fix, need reform these code ,  clear all request with sceneId
				String where = RequestColumns.SCENEID + " = " + sceneId;
                    int count = mContext.getContentResolver().delete(RequestProvider.CONTENT_REQUEST_URI, where, null);
                    Log.d(TAG, "delete saved request count = " + count);

                    ArrayList<ContentValues> bulkInsertList = new ArrayList<ContentValues>();
                    for (int i = 0; i < requestList.size(); i++) {
                        bulkInsertList.add(createRequestValue(requestList.get(i), sceneId));
                    }
                    if (bulkInsertList.size() > 0) {
                        ContentValues[] valueArray = new ContentValues[bulkInsertList.size()];
                        valueArray = bulkInsertList.toArray(valueArray);
                        int i = mContext.getContentResolver().bulkInsert(
                                RequestProvider.CONTENT_REQUEST_URI, valueArray);
                        Log.d(TAG, "cacheRequests() bulkInsert count = " + i);
                    }

//                    db.setTransactionSuccessful();
//
//                } catch(Exception e) {
//                    Log.d(TAG, "cacheRequests : " + e.getMessage());
//                } finally {
//                    if (db != null) {
//                        db.endTransaction();
//                    }
//                }

//                SQLiteDatabase db = RequestProvider.mOpenHelper.getReadableDatabase();
//            	db.beginTransaction();
//            	StringBuilder requestids = new StringBuilder();
//            	try { 
//            		for(int i=0;i<requestList.size();i++){
//            			if(requestids.length() > 0) {
//            				requestids.append(",");
//            			}
//            			Requests tmpRequest = requestList.get(i);
//            			int count = updateRequest(tmpRequest);
//            			if(count <= 0 ) {
//            				insertReqeust(tmpRequest);
//            			}
//            		}
//            		String deleteWhere = "";
//            		if(StringUtil.isEmpty(types)) {
//            			deleteWhere = RequestColumns.REQUEST_ID + "not in (" + requestids.toString() + ")";
//            		}else {
//            			deleteWhere = RequestColumns.REQUEST_ID + "not in (" + requestids.toString() 
//            					+ ") and " + RequestColumns.TYPE + " in (" + types + ")" ; 
//            		}
//            		int deleteCount = mContext.getContentResolver().delete(RequestProvider.CONTENT_REQUEST_URI, deleteWhere, null);
//            		Log.i(TAG, "remove redundant requests " + deleteCount);
//            		Log.i(TAG, "cacheRequests end ------ ");
//            		db.setTransactionSuccessful();
//        		} catch (Exception e) {
//        			Log.d(TAG, "cacheRequests : " + e.getMessage());
//        		} finally {
//        			db.endTransaction();
//        		}
			}
    		
    	});
    	
    }
    
    public void clearRequestsWithType(final String types) {
    	String deleteWhere = RequestColumns.TYPE + " in (" + types + ")" ;
    	int deleteCount = mContext.getContentResolver().delete(RequestProvider.CONTENT_REQUEST_URI, deleteWhere, null);
		Log.i(TAG, "remove redundant requests " + deleteCount);
    }

    private ContentValues createRequestValue(Requests request, final long sceneId) {
    	ContentValues value = new ContentValues();
        value.put(RequestColumns.REQUEST_ID, request.rid);
        value.put(RequestColumns.TYPE, request.type);
        value.put(RequestColumns.MESSAGE, request.message);
        value.put(RequestColumns.CREATE_TIME, request.createTime);
        value.put(RequestColumns.DATA, request.data);
        value.put(RequestColumns.SCENEID, sceneId);
        if (request.user != null) {
            value.put(RequestColumns.UID, request.user.uid);
            value.put(RequestColumns.NICK_NAME, request.user.nick_name);
            value.put(RequestColumns.PROFILE_IMAGE_URL, request.user.profile_image_url);
            value.put(RequestColumns.PROFILE_SIMAGE_URL, request.user.profile_simage_url);
            value.put(RequestColumns.PROFILE_LIMAGE_URL, request.user.profile_limage_url);

//            Log.d(TAG, "request.user.phoneList = " + request.user.phoneList);
            if (request.user.phoneList != null) {
                int phone_size = request.user.phoneList.size();
                if (phone_size > 0 && request.user.phoneList.get(0) != null)
                value.put(RequestColumns.EMAIL_1, request.user.phoneList.get(0).info);
                if (phone_size > 1 && request.user.phoneList.get(1) != null)
                value.put(RequestColumns.EMAIL_2, request.user.phoneList.get(1).info);
                if (phone_size > 2 && request.user.phoneList.get(2) != null)
                value.put(RequestColumns.EMAIL_3, request.user.phoneList.get(2).info);
            }

//            Log.d(TAG, "request.user.emailList = " + request.user.emailList);
            if (request.user.emailList != null) {
                int email_size = request.user.emailList.size();
                if (email_size > 0 && request.user.emailList.get(0) != null)
                value.put(RequestColumns.PHONE_1, request.user.emailList.get(0).info);
                if (email_size > 1 && request.user.emailList.get(1) != null)
                value.put(RequestColumns.PHONE_2, request.user.emailList.get(1).info);
                if (email_size > 2 && request.user.emailList.get(2) != null)
                value.put(RequestColumns.PHONE_3, request.user.emailList.get(2).info);
            }	
        }
        return value;
    }
    
    public boolean deleteDoneRequest(String request_id, final long sceneId) {
        String where = null;
        if (TextUtils.isEmpty(request_id) == false) {
            where = RequestColumns.REQUEST_ID + " = " + request_id;
        }else {
        	where = RequestColumns.SCENEID + " = " + sceneId;
        }
        return mContext.getContentResolver().delete(RequestProvider.CONTENT_REQUEST_URI, where, null) > 0;
    }

    public String getEventIds() {
    	String where = CircleColumns.TYPE +"="+ UserCircle.CIRCLE_TYPE_EVENT;
        final Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, new String[]{CircleColumns.CIRCLE_ID} ,
        		where, null, null);
        StringBuilder ids = new StringBuilder();
        if (null != cursor) {
            if (cursor.moveToFirst()) {
            	do {
            		long id = cursor.getLong(cursor.getColumnIndex(CircleColumns.CIRCLE_ID));
            		if(ids.length() > 0) {
            			ids.append(",");
            		}
            		ids.append(id);
				} while (cursor.moveToNext());
            }
        }
        closeCursor(cursor);   
        return ids.toString();
    }
    
    public int updateProfileImageUrl(String profile_image) {
    	ContentValues cv = new ContentValues();
    	cv.put(UsersColumns.PROFILE_IMAGE_URL, profile_image);
    	String where = UsersColumns.USERID + " = " + AccountServiceUtils.getBorqsAccountID();  
    	int count = mContext.getContentResolver().update(USERS_CONTENT_URI, cv,where,null);
    	return count;
    }
    
    private static ContentValues createThemeValues(EventTheme theme){
		ContentValues cv = new ContentValues();
		cv.put(ThemesColumns.THEME_ID, theme.id);
		cv.put(ThemesColumns.THEME_CREATOR, theme.creator);
		cv.put(ThemesColumns.THEME_UPDATED_TIME, theme.updated_time);
		cv.put(ThemesColumns.THEME_NAME, theme.name);
		cv.put(ThemesColumns.THEME_IMAGE_URL, theme.image_url);
		return cv;
	}
    
    public static EventTheme createEventThemeInfo(Cursor cursor) {
        if (null != cursor) {
        	EventTheme result = new EventTheme();
        	result.id = cursor.getLong(cursor.getColumnIndex(ThemesColumns.THEME_ID));
        	result.creator = cursor.getLong(cursor.getColumnIndex(ThemesColumns.THEME_CREATOR));
            result.updated_time = cursor.getLong(cursor.getColumnIndex(ThemesColumns.THEME_UPDATED_TIME));
            result.name = cursor.getString(cursor.getColumnIndex(ThemesColumns.THEME_NAME));
            result.image_url = cursor.getString(cursor.getColumnIndex(ThemesColumns.THEME_IMAGE_URL));
            return result;
        }

        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}
    
    public void insertEventList(ArrayList<UserCircle> circlelist) {
    	String where = CircleColumns.TYPE + "=" + UserCircle.CIRCLE_TYPE_EVENT;
    	String groupWhere = GroupColumns.CIRCLE_ID + " in (" + getEventIds() + ")";
    	ArrayList<ContentValues> circleInfo = new ArrayList<ContentValues>();
    	ArrayList<ContentValues> groupInfo = new ArrayList<ContentValues>();
    	mContext.getContentResolver().delete(CIRCLES_CONTENT_URI, where, null); 
    	mContext.getContentResolver().delete(CIRCLE_GROUP_URI, groupWhere, null);
    	for(int i=0;i<circlelist.size(); i++) {
    		final UserCircle userCircle = circlelist.get(i);
    		circleInfo.add(createCircleInformationValues(userCircle));
    		if(UserCircle.CIRLCE_TYPE_PUBLIC == userCircle.type 
    				|| UserCircle.CIRCLE_TYPE_EVENT == userCircle.type) {
    			groupInfo.add(createGroupInformationValues(userCircle));
    		}
    	}
    	
    	bulkInsert(CIRCLES_CONTENT_URI, circleInfo);
    	bulkInsert(CIRCLE_GROUP_URI, groupInfo);
    	
    }
    
    public void insertEventsList(Context context, ArrayList<UserCircle> circlelist) {
    	StringBuilder ids = new StringBuilder();
    	for(int i=0; i<circlelist.size(); i++) {
    		if(ids.length() > 0) {
    			ids.append(",");
    		}
    		ids.append(circlelist.get(i).circleid);
    	}
    	
    	// remove redundancy events
    	String redEventids = queryRedundancyEventIds(ids.toString());
    	deleteCirclesWithIds(redEventids);
    	//remove redundancy event_calendar & calendar events
    	String redCalendarEventIds = queryCalendarRedundancyids(redEventids);
    	deleteEventsCalendar(redEventids);
    	CalendarMappingUtils.removeCalendarEvents(context, redCalendarEventIds);
    	
    	for(int i=0; i<circlelist.size(); i++) {
    		UserCircle tmpCircle = circlelist.get(i);
    		insertOneCircle(tmpCircle);
    		
    		if(needInsertToCalendar(context, tmpCircle)) {
    			Log.d(TAG, "insertEventList: " + tmpCircle.circleid);
    			updateEventCalendarTime(tmpCircle);
    			CalendarMappingUtils.importEventTocalendar(context, tmpCircle, QiupuORM.getInstance(context));
    		}
    	}
    }
    
    public void updateEventCalendarTime(UserCircle circle) {
    	String where = EventCalendarColumns.EVENT_ID + " = " + circle.circleid;
    	ContentValues cv = new ContentValues();
    	cv.put(EventCalendarColumns.UPDATE_TIME, circle.mGroup.updated_time);
    	int count = mContext.getContentResolver().update(EVENT_CALENDARS_URI, cv, where, null);
    	Log.d(TAG, "updateEventCalendarTime: " + count);
    	
    }
    public boolean needInsertToCalendar(Context context, UserCircle circle) {
    	if(circle.mGroup == null) {
    		return true;
    	}
    	
    	long calendarEventId = getEventCalendarid(circle.circleid);
    	if(calendarEventId < 0) {
    		return true;
    	}
    	
    	boolean needInsert = false;
    	if(circle.mGroup.endTime == 0) {
    		if(circle.mGroup.repeat_type == CalendarMappingUtils.DOES_NOT_REPEAT) {
    			if(circle.mGroup.startTime > System.currentTimeMillis()) {
    				needInsert = true;
    			}
    		}else {
    			needInsert = false;
    		}
    	}else {
    		if(circle.mGroup.endTime > System.currentTimeMillis()) {
    			needInsert =  true;
    		}else {
    			needInsert = false;
    		}
    	}
    	
    	boolean ret = true;
    	if(needInsert) {
    		if(CalendarMappingUtils.isImportedToCalendar(context, calendarEventId)) {
    			String where = EventCalendarColumns.EVENT_ID + "=" + circle.circleid;
    			Cursor cursor = mContext.getContentResolver().query(EVENT_CALENDARS_URI, new String[]{EventCalendarColumns.UPDATE_TIME}, where, null, null);
    			if(cursor != null) {
    				if (cursor.moveToFirst()) {
    					long time = cursor.getLong(cursor.getColumnIndex(EventCalendarColumns.UPDATE_TIME));
    					if(circle.mGroup.updated_time == time) {
    						ret = false;
    					}
    				}
    				cursor.close();
    				cursor = null;
    			}
    		}
    	}else {
    		ret = false;
    	}
		return ret;
	}
    
    private String queryCalendarRedundancyids(String ids) {
    	String where = EventCalendarColumns.EVENT_ID + " in (" + ids + ")";
    	Cursor cursor = mContext.getContentResolver().query(EVENT_CALENDARS_URI, new String[]{EventCalendarColumns.CALENDAR_EVENT_ID}, where, null, null);
    	StringBuilder redundancyids = new StringBuilder();
        if (null != cursor) {
            if (cursor.moveToFirst()) {
            	do {
            		long id = cursor.getLong(cursor.getColumnIndex(EventCalendarColumns.CALENDAR_EVENT_ID));
            		if(redundancyids.length() > 0) {
            			redundancyids.append(",");
            		}
            		redundancyids.append(id);
				} while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;
        }
        return redundancyids.toString();
    }
    
    private String queryRedundancyEventIds(String ids) {
    	String where = CircleColumns.CIRCLE_ID + " not in(" + ids + ") and " + CircleColumns.TYPE + "=" + UserCircle.CIRCLE_TYPE_EVENT;
    	Cursor cursor = mContext.getContentResolver().query(CIRCLES_CONTENT_URI, new String[]{CircleColumns.CIRCLE_ID} ,
        		where, null, null);
    	StringBuilder redundancyids = new StringBuilder();
        if (null != cursor) {
            if (cursor.moveToFirst()) {
            	do {
            		long id = cursor.getLong(cursor.getColumnIndex(CircleColumns.CIRCLE_ID));
            		if(redundancyids.length() > 0) {
            			redundancyids.append(",");
            		}
            		redundancyids.append(id);
				} while (cursor.moveToNext());
            }
            cursor.close();
            cursor = null;
        }
        return redundancyids.toString(); 
    }
    
    
    public void insertEventThemes(ArrayList<EventTheme> themeList) {
    	mContext.getContentResolver().delete(THEMES_URI, null, null); // clear themes first
    	ArrayList<ContentValues> themesInfo = new ArrayList<ContentValues>();
    	for(int i=0; i<themeList.size(); i++) {
    		themesInfo.add(createThemeValues(themeList.get(i)));
    	}
    	bulkInsert(THEMES_URI, themesInfo);
    }
    
    public void insertOneTheme(EventTheme theme) {
    	EventTheme tmptheme = queryOneTheme(theme.id);
    	if(tmptheme != null) {
    		ContentValues cv = createThemeValues(theme);
        	String where = ThemesColumns.THEME_ID +"="+theme.id;
        	mContext.getContentResolver().update(THEMES_URI, cv, where, null);
    	}else {
    		ContentValues cv = createThemeValues(theme);
    		mContext.getContentResolver().insert(THEMES_URI, cv);
    	}
    }
    
    public Cursor queryAllThemes() {
        final Cursor cursor = mContext.getContentResolver().query(THEMES_URI, THEME_PROJECTION ,
        		null, null, null);
        return cursor;
    }
    
    public EventTheme queryOneTheme(long themeid) {
    	String where = ThemesColumns.THEME_ID + "=" + themeid;
    	EventTheme theme = null;
    	Cursor cursor = mContext.getContentResolver().query(THEMES_URI, THEME_PROJECTION ,
        		where, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			theme = createEventThemeInfo(cursor);
    		}
    		cursor.close();
    		cursor = null;
    	}
        return theme;
    }

    private class UserPhotoUrlCache extends ArrayList<Long> {
        private HashMap<Long, String> mCachedMap = new HashMap<Long, String>();
        private static final int CACHE_SIZE = 50;
        final String getUrl(Long uId) {
            String url = "";
            if (contains(uId)) {
                url = mCachedMap.get(uId);
                hit(uId);
            } else {
                url = queryUrlFromDb(uId);
                cache(uId, url);
            }
            return url;
        }

        private void hit(Long uid) {
            remove(uid);
            add(0, uid);
        }

        boolean cache(Long uid, String path) {
            if (!TextUtils.isEmpty(path)) {
                if (contains(uid)) {
                    hit(uid);
                } else {
                    mCachedMap.put(uid, path);
                    add(0, uid);

                    while (size() > CACHE_SIZE) {
                        Long id = get(49);
                        mCachedMap.remove(id);
                        remove(49);
                    }
                    Log.d(TAG, "UserPhotoUrlCache, LRU cached: " +
                            path + " size:" + size() + "/" + mCachedMap.size());
                }
                return true;
            }

            return false;
        }

        private String queryUrlFromDb(Long uId) {
            String url = "";
            String where = UsersColumns.USERID + "=" + uId;
            String[] USERS_NickName_PROJECTION = {
                    UsersColumns.USERID,
                    UsersColumns.PROFILE_IMAGE_URL,
            };
            Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI, USERS_NickName_PROJECTION, where, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    url = cursor.getString(cursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL));
                }
                cursor.close();
            }
            return url;
        }
    }
    
    public void deletePollnfo(String poll_id){
        try{
            String where =  PollColumns.POLL_ID +"="+poll_id;
            mContext.getContentResolver().delete(POLL_URI, where, null);
        }catch(Exception ne){}    
    }

    public void insertPollList(ArrayList<PollInfo> pollList,int type) {
        if (pollList == null || pollList.size() == 0) {
            Log.d(TAG, "insertPollList() pollList == null || pollList.size() == 0");
            return;
        }
        ArrayList<ContentValues> bulkInsertList = new ArrayList<ContentValues>();
        String where =  PollColumns.TYPE +"="+type;
        int deleteCount = mContext.getContentResolver().delete(POLL_URI, where, null);
        Log.d(TAG, "deleteCount = " + deleteCount);
//        ContentValues value = new ContentValues();

        for (int i = 0; i < pollList.size(); i++) {
            PollInfo pollInfo = pollList.get(i);
            if (pollInfo != null) {
                ContentValues value = new ContentValues();
                value.clear();
                value.put(PollColumns.POLL_ID, pollInfo.poll_id);
                value.put(PollColumns.TITLE, pollInfo.title);
                value.put(PollColumns.DESCRIPTION, pollInfo.description);
                value.put(PollColumns.TYPE, type);
                value.put(PollColumns.RESTRICT, pollInfo.restrict);
                value.put(PollColumns.TARGET, pollInfo.target_id);
                value.put(PollColumns.MULTI, pollInfo.multi);
                value.put(PollColumns.LIMIT, pollInfo.limit);
                value.put(PollColumns.PRIVACY, pollInfo.privacy);
                value.put(PollColumns.CREATED_TIME, pollInfo.created_time);
                value.put(PollColumns.END_TIME, pollInfo.end_time);
                value.put(PollColumns.UPDATE_TIME, pollInfo.updated_time);
                value.put(PollColumns.DESTROY_TIME, pollInfo.destroyed_time);
                value.put(PollColumns.ATTEND_STATUS, pollInfo.attend_status);
                value.put(PollColumns.ATTEND_COUNT, pollInfo.attend_count);
                value.put(PollColumns.DURATION_TIME, pollInfo.left_time);
                value.put(PollColumns.MODE, pollInfo.mode);
                value.put(PollColumns.VIEWER_LEFT, pollInfo.viewer_left);
                value.put(PollColumns.HAS_VOTED, pollInfo.has_voted ? 1 : 0);
                value.put(PollColumns.UID, pollInfo.sponsor.uid);
                value.put(PollColumns.USER_NAME, pollInfo.sponsor.nick_name);
                value.put(PollColumns.IMAGE_URL, pollInfo.sponsor.profile_image_url);

                bulkInsertList.add(value);
            }
        }

        if (bulkInsertList.size() > 0) {
            ContentValues[] valueArray = new ContentValues [bulkInsertList.size()];
            valueArray = bulkInsertList.toArray(valueArray);
            int i = mContext.getContentResolver().bulkInsert(POLL_URI, valueArray);
            Log.d(TAG, "insertPollList() insert count = " + i);
        }
    }

    public void insertCirclePollList(ArrayList<PollInfo> pollList) {
        if (pollList == null || pollList.size() == 0) {
            Log.d(TAG, "insertCirclePollList() pollList == null || pollList.size() == 0");
            return;
        }
        ArrayList<ContentValues> bulkInsertList = new ArrayList<ContentValues>();
        String where = null;
        if(StringUtil.isValidString(pollList.get(0).target_id)) {
        	where = PollColumns.TARGET + " = " + pollList.get(0).target_id;
        }
        int deleteCount = mContext.getContentResolver().delete(CIRCLE_POLL_URI, where, null);
        Log.d(TAG, "deleteCount = " + deleteCount + ", where = " + where);
//        ContentValues value = new ContentValues();

        for (int i = 0; i < pollList.size(); i++) {
            PollInfo pollInfo = pollList.get(i);
            if (pollInfo != null) {
                ContentValues value = new ContentValues();
                value.clear();
                value.put(PollColumns.POLL_ID, pollInfo.poll_id);
                value.put(PollColumns.UID, pollInfo.sponsor.uid);
                value.put(PollColumns.TITLE, pollInfo.title);
                value.put(PollColumns.TARGET, pollInfo.target_id);
                value.put(PollColumns.CREATED_TIME, pollInfo.created_time);
                value.put(PollColumns.END_TIME, pollInfo.end_time);
                value.put(PollColumns.ATTEND_STATUS, pollInfo.attend_status);
                value.put(PollColumns.ATTEND_COUNT, pollInfo.attend_count);
                value.put(PollColumns.USER_NAME, pollInfo.sponsor.nick_name);
                value.put(PollColumns.IMAGE_URL, pollInfo.sponsor.profile_image_url);

                bulkInsertList.add(value);
            }
        }

        if (bulkInsertList.size() > 0) {
            ContentValues[] valueArray = new ContentValues [bulkInsertList.size()];
            valueArray = bulkInsertList.toArray(valueArray);
            int i = mContext.getContentResolver().bulkInsert(CIRCLE_POLL_URI, valueArray);
            Log.d(TAG, "insertCirclePollList() insert count = " + i);
        }
    }

    public static void removeCirclePoll(long circleId, Context context) {
    	String where = PollColumns.TARGET + " = " + circleId;
    	context.getContentResolver().delete(CIRCLE_POLL_URI, where, null);
    }
    public static ArrayList<PollInfo> queryCirclePollList(Context context, String circle_id){
        ArrayList<PollInfo> list = new ArrayList<PollInfo>();
        Cursor cursor = context.getContentResolver().query(CIRCLE_POLL_URI, CIRCLE_POLL_PROJECTION, PollColumns.TARGET + " = " + circle_id, null, null);
        if(cursor !=null) {
            if (cursor.getCount()>0) {
                while(cursor.moveToNext()) {
                    PollInfo info = createPollInfo(context,cursor, circle_id);
                    if (info != null) {
                        list.add(info);
                    }
                }
            }
            cursor.close();
        }
        return list;
    }

    public static ArrayList<PollInfo> queryPollListInfo(Context context,int type){
        String where =  PollColumns.TYPE +"="+type;
        ArrayList<PollInfo> list = new ArrayList<PollInfo>();
        Cursor cursor = context.getContentResolver().query(POLL_URI, POLL_PROJECTION, where, null, null);
        if(cursor !=null) {
            if (cursor.getCount()>0) {
                while(cursor.moveToNext()) {
                    list.add(createPollInfo(context,cursor, ""));
                }
            }
            cursor.close();
        }
        return list;
    }

    public static PollInfo createPollInfo(Context context, Cursor cursor, String _id) {
        if (null != cursor) {
            PollInfo result = new PollInfo();

            result.target_id = cursor.getString(cursor.getColumnIndexOrThrow(PollColumns.TARGET));
//            Log.d(TAG, "target_id = " + result.target_id + ", _id = " + _id);
//            if (TextUtils.isEmpty(_id) == false) {
//                String[] targetArray = result.target_id.split(",");
//                boolean isHasTheCircle = false;
//                for (String tmp_id: targetArray) {
//                    if (_id.equals(tmp_id)) {
//                        isHasTheCircle = true;
//                    }
//                }
//                if (isHasTheCircle == false) {
//                    return null;
//                }
//            }

            result.poll_id = cursor.getString(cursor.getColumnIndex(PollColumns.POLL_ID));
            result.title = cursor.getString(cursor.getColumnIndex(PollColumns.TITLE));
            result.attend_status = cursor.getLong(cursor.getColumnIndex(PollColumns.ATTEND_STATUS));
            result.attend_count = cursor.getLong(cursor.getColumnIndex(PollColumns.ATTEND_COUNT));
            if (TextUtils.isEmpty(_id)) {
                result.type = cursor.getInt(cursor.getColumnIndex(PollColumns.TYPE));
                result.mode = cursor.getInt(cursor.getColumnIndex(PollColumns.MODE));
                if (cursor.getInt(cursor.getColumnIndex(PollColumns.HAS_VOTED)) == 1) {
                    result.has_voted = true;
                } else {
                    result.has_voted = false;
                }
                result.viewer_left = cursor.getInt(cursor.getColumnIndex(PollColumns.VIEWER_LEFT));
            }
            QiupuSimpleUser user = new QiupuSimpleUser();
            user.uid = cursor.getLong(cursor.getColumnIndex(PollColumns.UID));
            user.nick_name = cursor.getString(cursor.getColumnIndex(PollColumns.USER_NAME));
            user.profile_image_url = cursor.getString(cursor.getColumnIndex(PollColumns.IMAGE_URL));

            result.sponsor = user;
            return result;
        }

        Log.e(TAG, "createPollInfo, unexpected null cursor.");
        return null;
    }
    
    public void insertEventsCalendar(long eventid, long calendar_eventId, long updatetime) {
    	ContentValues cv = new ContentValues();
    	cv.put(EventCalendarColumns.CALENDAR_EVENT_ID, calendar_eventId);
    	cv.put(EventCalendarColumns.UPDATE_TIME, updatetime);
    	final ContentResolver cr = mContext.getContentResolver();
    	if(getEventCalendarid(eventid) > 0) {
    		String where =  EventCalendarColumns.EVENT_ID +"="+ eventid;
        	cr.update(EVENT_CALENDARS_URI, cv, where,null);
    	}else {
    		cv.put(EventCalendarColumns.EVENT_ID, eventid);
    		cr.insert(EVENT_CALENDARS_URI, cv);
    	}
    }

    public void deleteEventsCalendar(long eventid) {
    	String where = EventCalendarColumns.EVENT_ID + "=" + eventid;
    	mContext.getContentResolver().delete(EVENT_CALENDARS_URI, where, null);
    }
    
    public void deleteEventsCalendar(String eventids) {
    	String where = EventCalendarColumns.EVENT_ID + " in(" + eventids + ")";
    	mContext.getContentResolver().delete(EVENT_CALENDARS_URI, where, null);
    }
    
    public long getEventCalendarid(long eventid) {
    	String where = EventCalendarColumns.EVENT_ID + "=" + eventid;
    	long calendar_event_id = -1;
    	Cursor cursor = mContext.getContentResolver().query(EVENT_CALENDARS_URI, new String[]{EventCalendarColumns.CALENDAR_EVENT_ID} ,
    			where, null, null);
    	if(cursor != null ) {
    		if(cursor.getCount() > 0) {
    			cursor.moveToFirst();
    			calendar_event_id = cursor.getLong(0);
    		}
    		cursor.close();
    		cursor = null;
    	}
    	return calendar_event_id;
    } 
    
    public long queryEventUpdateTime(long eventId) {
    	long update_time = -1;
    	String where = GroupColumns.CIRCLE_ID +" = "+ eventId;
    	Cursor cursor = mContext.getContentResolver().query(CIRCLE_GROUP_URI, new String[]{GroupColumns.UPDATED_TIME}, where, null, null);
    	if(cursor != null) {
    		if(cursor.getCount() > 0) {
    			cursor.moveToFirst();
    			update_time = cursor.getLong(0);
    		}
    		cursor.close();
    		cursor = null;
    	}
    	return update_time;
    }
    
    private boolean isExistCompany(long cId)
    {
    	boolean ret = false;
    	String where =  CompanyColumns.COMPANY_ID +"="+cId;
    	Cursor cursor = mContext.getContentResolver().query(COMPANY_URI,new String[]{CompanyColumns.COMPANY_ID} , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){    		
    		ret = true;
    	}
    	if(cursor != null)
    		cursor.close();
    	
    	return ret;
    }
    
    public void deleteCompanyById(long company_id){
    	try{
    		String where =  CompanyColumns.COMPANY_ID +"="+company_id;
    		mContext.getContentResolver().delete(COMPANY_URI, where, null);
    	}catch(Exception ne){}    
    }
    public void deleteCompanyAll(){
    	try{
    		mContext.getContentResolver().delete(COMPANY_URI, null, null);
    	}catch(Exception ne){}    
    }
    
    private static Company createCompanyInfo(Context context,Cursor cursor) {
    	Company result = new Company();
		result.id = cursor.getLong(cursor.getColumnIndex(CompanyColumns.COMPANY_ID));
    	result.department_id = cursor.getLong(cursor.getColumnIndex(CompanyColumns.DEPARTMENT_ID));
    	result.created_time = cursor.getLong(cursor.getColumnIndex(CompanyColumns.CREATED_TIME));
    	result.updated_time = cursor.getLong(cursor.getColumnIndex(CompanyColumns.UPDATED_TIME));
    	result.role = cursor.getInt(cursor.getColumnIndex(CompanyColumns.ROLE));
    	result.email_domain1 = cursor.getString(cursor.getColumnIndex(CompanyColumns.DOMAIN1));
    	result.email_domain2 = cursor.getString(cursor.getColumnIndex(CompanyColumns.DOMAIN2));
    	result.email_domain3 = cursor.getString(cursor.getColumnIndex(CompanyColumns.DOMAIN3));
    	result.email_domain4 = cursor.getString(cursor.getColumnIndex(CompanyColumns.DOMAIN4));
    	result.name = cursor.getString(cursor.getColumnIndex(CompanyColumns.NAME));
    	result.address = cursor.getString(cursor.getColumnIndex(CompanyColumns.ADDRESS));
    	result.email = cursor.getString(cursor.getColumnIndex(CompanyColumns.EMAIL));
    	result.website = cursor.getString(cursor.getColumnIndex(CompanyColumns.WEB_SITE));
    	result.tel = cursor.getString(cursor.getColumnIndex(CompanyColumns.TEL));
    	result.fax = cursor.getString(cursor.getColumnIndex(CompanyColumns.FAX));
    	result.zip_code = cursor.getString(cursor.getColumnIndex(CompanyColumns.ZIP_CODE));
    	result.person_count = cursor.getInt(cursor.getColumnIndex(CompanyColumns.PERSON_COUNT));
    	result.department_count = cursor.getInt(cursor.getColumnIndex(CompanyColumns.DEPARTMENT_COUNT));
    	result.small_logo_url = cursor.getString(cursor.getColumnIndex(CompanyColumns.SMALL_LOGO_URL));
    	result.logo_url = cursor.getString(cursor.getColumnIndex(CompanyColumns.LOGO_URL));
    	result.large_logo_url = cursor.getString(cursor.getColumnIndex(CompanyColumns.LARGE_LOGO_URL));
    	result.small_cover_url = cursor.getString(cursor.getColumnIndex(CompanyColumns.SMALL_COVER_URL));
    	result.cover_url = cursor.getString(cursor.getColumnIndex(CompanyColumns.COVER_URL));
    	result.large_cover_url = cursor.getString(cursor.getColumnIndex(CompanyColumns.LARGE_COVER_URL));
    	result.description = cursor.getString(cursor.getColumnIndex(CompanyColumns.DESCRIPTION));
    	
    	result.memberList = queryUserImage(context, result.department_id, UserProvider.IMAGE_TYPE_COMPANY_MEMBER);
    	result.depList = queryUserImage(context, result.department_id, UserProvider.IMAGE_TYPE_DEPARTMENT_MEMBER);
		return result;
	}
    
    public static Company queryCompany(Context context, long company_id) {
    	Company cmp = null;
    	String where = CompanyColumns.COMPANY_ID +"="+company_id;
    	Cursor cursor = context.getContentResolver().query(COMPANY_URI, COMPANY_PROJECTION , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
    		while(cursor.moveToNext()){  
    			cmp = createCompanyInfo(context,cursor);  
    		}  
    	}
    	if(cursor != null)
    	{
    		cursor.close();
    		cursor = null;
    	}
    	return cmp;
    }
    
    public static ArrayList<Company> queryCompanyList(Context context){
        ArrayList<Company> list = new ArrayList<Company>();
        Cursor cursor = context.getContentResolver().query(COMPANY_URI, COMPANY_PROJECTION , null, null, null);
        if(cursor !=null) {
            if (cursor.getCount()>0) {
                while(cursor.moveToNext()) {
                	list.add(createCompanyInfo(context,cursor));
                }
            }
            cursor.close();
        }
        return list;
    }
    
    public static Company queryCompanyByCircleId(Context context, long circleId) {
		Company cmp = null;
        String where = CompanyColumns.DEPARTMENT_ID +"="+circleId;
    	Cursor cursor = context.getContentResolver().query(COMPANY_URI, COMPANY_PROJECTION , where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
    		 while(cursor.moveToNext()){  
    			 cmp = createCompanyInfo(context,cursor);  
    		 }  
    	}
    	if(cursor != null)
    	{
            cursor.close();
		    cursor = null;
    	}
    	return cmp;
	}
    
    public void insertCompanyList(ArrayList<Company> companyList) {
    	deleteCompanyAll();
    	if(companyList != null && companyList.size() > 0) {
    		for(int i=0;i<companyList.size();i++){
    			Company c = companyList.get(i);
    			insertCompanyInfo(c);
    		}
    	}
    }
    
    public void insertCompanyInfo(Company info){ 
    	if(isExistCompany(info.id)) {
    		updateCompanyInfo(info);
    	}else {
    		
    		ContentValues cv = createCompanyInfo(info);
    		mContext.getContentResolver().insert(COMPANY_URI, cv);
    	}
    	deleteUserImageInfo(info.department_id);
    	insertUserImageList(info.department_id, info.memberList, UserProvider.IMAGE_TYPE_COMPANY_MEMBER);
    	insertUserImageList(info.department_id, info.depList, UserProvider.IMAGE_TYPE_DEPARTMENT_MEMBER);
    }
    
    public int updateCompanyInfo(final Company info){
    	ContentValues cv = createCompanyInfo(info);
    	
    	String where =  CompanyColumns.COMPANY_ID +"="+info.id;
    	return mContext.getContentResolver().update(COMPANY_URI, cv,where,null);
    }
    
    private ContentValues createCompanyInfo(Company info){
    	ContentValues cv = new ContentValues();
		cv.put(CompanyColumns.COMPANY_ID, info.id);
		cv.put(CompanyColumns.DEPARTMENT_ID, info.department_id);
		cv.put(CompanyColumns.CREATED_TIME, info.created_time);
		cv.put(CompanyColumns.UPDATED_TIME, info.updated_time);
		cv.put(CompanyColumns.ROLE, info.role);
		cv.put(CompanyColumns.DOMAIN1, info.email_domain1);
		cv.put(CompanyColumns.DOMAIN2, info.email_domain2);
		cv.put(CompanyColumns.DOMAIN3, info.email_domain3);
		cv.put(CompanyColumns.DOMAIN4, info.email_domain4);
		cv.put(CompanyColumns.NAME, info.name);
		cv.put(CompanyColumns.ADDRESS, info.address);
		cv.put(CompanyColumns.EMAIL, info.email);
		cv.put(CompanyColumns.WEB_SITE, info.website);
		cv.put(CompanyColumns.TEL, info.tel);
		cv.put(CompanyColumns.FAX, info.fax);
		cv.put(CompanyColumns.ZIP_CODE, info.zip_code);
		cv.put(CompanyColumns.PERSON_COUNT, info.person_count);
		cv.put(CompanyColumns.DEPARTMENT_COUNT, info.department_count);
		cv.put(CompanyColumns.SMALL_LOGO_URL, info.small_logo_url);
		cv.put(CompanyColumns.LOGO_URL, info.logo_url);
		cv.put(CompanyColumns.LARGE_LOGO_URL, info.large_logo_url);
		cv.put(CompanyColumns.SMALL_COVER_URL, info.small_cover_url);
		cv.put(CompanyColumns.COVER_URL, info.cover_url);
		cv.put(CompanyColumns.LARGE_COVER_URL, info.large_cover_url);
    	cv.put(CompanyColumns.DESCRIPTION, info.description);
    	return cv;
    }

    public int getLastUsedIconVersion(boolean external) {
        final String key = external ? LAST_LAST_USED_EXTERNAL_ICON_VERSION : LAST_LAST_USED_ICON_VERSION;
        final String version = getSettingValue(key);
        return TextUtils.isEmpty(version) ? 0 : Integer.valueOf(version);
    }

    public void setLastUsedIconVersion(boolean external, int version) {
        final String key = external ? LAST_LAST_USED_EXTERNAL_ICON_VERSION : LAST_LAST_USED_ICON_VERSION;
        addSetting(key, String.valueOf(version));
    }
    
    public Cursor queryAllSimplePages() {
    	Cursor cursor = mContext.getContentResolver().query(PAGE_URI, PAGE_SIMPLE_PROJECTION,
                null, null, null);
        return cursor;
    }
    
    public static PageInfo createPageListInformation(Context context, Cursor cursor) {
        if (null != cursor) {
        	PageInfo result = new PageInfo();
        	result.page_id = cursor.getLong(cursor.getColumnIndex(PageColumns.PAGE_ID));
        	result.name = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_NAME));
        	result.name_en = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_NAME_EN));
        	result.followers_count = cursor.getInt(cursor.getColumnIndex(PageColumns.FOLLOWERS_COUNT));
        	result.small_logo_url = cursor.getString(cursor.getColumnIndex(PageColumns.SMALL_LOGO_URL));
        	result.logo_url = cursor.getString(cursor.getColumnIndex(PageColumns.LOGO_URL));
        	result.large_logo_url = cursor.getString(cursor.getColumnIndex(PageColumns.LARGE_LOGO_URL));
        	result.creatorId = cursor.getLong(cursor.getColumnIndex(PageColumns.CREATORID));
        	result.followed = cursor.getInt(cursor.getColumnIndex(PageColumns.FOLLOWED)) == 1 ? true : false;
            return result;
        }
		return null;
	}
    
    public static PageInfo createPageAllInformation(Context context, Cursor cursor) {
        if (null != cursor) {
        	PageInfo result = new PageInfo();
        	result.page_id = cursor.getLong(cursor.getColumnIndex(PageColumns.PAGE_ID));
        	result.name = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_NAME));
        	result.name_en = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_NAME_EN));
        	result.address = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_ADDRESS));
        	result.address_en = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_ADDRESS_EN));
        	result.description = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_DESCRIPTION));
        	result.description_en = cursor.getString(cursor.getColumnIndex(PageColumns.PAGE_ADDRESS_EN));
        	result.email = cursor.getString(cursor.getColumnIndex(PageColumns.EMAIL));
        	result.website = cursor.getString(cursor.getColumnIndex(PageColumns.WEBSITE));
        	result.tel = cursor.getString(cursor.getColumnIndex(PageColumns.TEL));
        	result.fax = cursor.getString(cursor.getColumnIndex(PageColumns.FAX));
        	result.zip_code = cursor.getString(cursor.getColumnIndex(PageColumns.ZIP_CODE));
        	result.small_logo_url = cursor.getString(cursor.getColumnIndex(PageColumns.SMALL_LOGO_URL));
        	result.logo_url = cursor.getString(cursor.getColumnIndex(PageColumns.LOGO_URL));
        	result.large_logo_url = cursor.getString(cursor.getColumnIndex(PageColumns.LARGE_LOGO_URL));
        	result.small_cover_url = cursor.getString(cursor.getColumnIndex(PageColumns.SMALL_COVER_URL));
        	result.cover_url = cursor.getString(cursor.getColumnIndex(PageColumns.COVER_URL));
        	result.large_cover_url = cursor.getString(cursor.getColumnIndex(PageColumns.LARGE_COVER_URL));
        	result.associated_id = cursor.getLong(cursor.getColumnIndex(PageColumns.ASSOCIATED_ID));
        	result.created_time = cursor.getLong(cursor.getColumnIndex(PageColumns.CREATED_TIME));
        	result.updated_time = cursor.getLong(cursor.getColumnIndex(PageColumns.UPDATED_TIME));
        	result.followers_count = cursor.getInt(cursor.getColumnIndex(PageColumns.FOLLOWERS_COUNT));
        	result.followed = cursor.getInt(cursor.getColumnIndex(PageColumns.FOLLOWED)) == 1 ? true : false;
        	result.viewer_can_update = cursor.getInt(cursor.getColumnIndex(PageColumns.VIEWER_CAN_UPDATE)) == 1 ? true : false;
        	result.creatorId = cursor.getLong(cursor.getColumnIndex(PageColumns.CREATORID));
        	result.free_circle_ids = cursor.getString(cursor.getColumnIndex(PageColumns.FREE_CIRCLE_IDS));
        	result.fansList = queryUserImage(context, result.page_id, UserProvider.IMAGE_TYPE_PAEG_FANS);
            return result;
        }
        
        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}

    public void deleteAllPages(){
    	try{
    		mContext.getContentResolver().delete(PAGE_URI, null, null);
    	}catch(Exception ne){}    
    }

    private ContentValues createPageInfoValue(PageInfo info){
        ContentValues cv = new ContentValues();
        cv.put(PageColumns.PAGE_ID, info.page_id);
        cv.put(PageColumns.PAGE_NAME, info.name);
        cv.put(PageColumns.PAGE_NAME_EN, info.name_en);
        cv.put(PageColumns.PAGE_ADDRESS, info.address);
        cv.put(PageColumns.PAGE_ADDRESS_EN, info.address_en);
        cv.put(PageColumns.PAGE_DESCRIPTION, info.description);
        cv.put(PageColumns.PAGE_DESCRIPTION_EN, info.description_en);
        cv.put(PageColumns.EMAIL, info.email);
        cv.put(PageColumns.WEBSITE, info.website);
        cv.put(PageColumns.TEL, info.tel);
        cv.put(PageColumns.FAX, info.fax);
        cv.put(PageColumns.ZIP_CODE, info.zip_code);
        cv.put(PageColumns.SMALL_LOGO_URL, info.small_logo_url);
        cv.put(PageColumns.LOGO_URL, info.logo_url);
        cv.put(PageColumns.LARGE_LOGO_URL, info.large_logo_url);
        cv.put(PageColumns.SMALL_COVER_URL, info.small_cover_url);
        cv.put(PageColumns.COVER_URL, info.cover_url);
        cv.put(PageColumns.LARGE_COVER_URL, info.large_cover_url);
        cv.put(PageColumns.ASSOCIATED_ID, info.associated_id);
        cv.put(PageColumns.CREATED_TIME, info.created_time);
        cv.put(PageColumns.UPDATED_TIME, info.updated_time);
        cv.put(PageColumns.FOLLOWERS_COUNT, info.followers_count);
        cv.put(PageColumns.FOLLOWED, info.followed ? 1 : 0); 
        cv.put(PageColumns.VIEWER_CAN_UPDATE, info.viewer_can_update ? 1 : 0); 
        cv.put(PageColumns.CREATORID, info.creatorId);
        cv.put(PageColumns.FREE_CIRCLE_IDS, info.free_circle_ids);
        return cv;
    }
    
    public void insertPageList(ArrayList<PageInfo> pageList) {
    	deleteAllPages();
    	if(pageList != null && pageList.size() > 0) {
    		final int count = pageList.size();
    		ArrayList<ContentValues> pagevalues = new ArrayList<ContentValues>();
    		for(int i=0; i<count; i++){
    			pagevalues.add(createPageInfoValue(pageList.get(i)));
    		}
    		bulkInsert(PAGE_URI, pagevalues);
    	}
    }
    
    public PageInfo queryOnePage(long pageId) {
    	PageInfo info = null;
    	String where = PageColumns.PAGE_ID +" = "+pageId;
    	Cursor cursor = mContext.getContentResolver().query(PAGE_URI, PAGE_PROJECTION, where, null, null);
    	if(cursor != null && cursor.getCount() > 0){
    		 while(cursor.moveToNext()){  
    			 info = createPageAllInformation(mContext, cursor);  
    		 }  
    	}
    	if(cursor != null)
    	{
            cursor.close();
		    cursor = null;
    	}
    	return info;
    }
    
    public void insertOnePage(PageInfo info){
    	int count = updatePageInfo(info);
    	if(count <= 0) {
    		insertPageInfo(info);
    	}
	}
    
    public int updatePageInfo(PageInfo info){
		//update page info
		ContentValues cv = createPageInfoValue(info);
    	String where = PageColumns.PAGE_ID +" = " + info.page_id;
    	int count = mContext.getContentResolver().update(PAGE_URI, cv, where, null);
    	
    	deleteUserImageInfo(info.page_id);
    	insertPageFansImageInfo(info);
    	return count;
    }
    
    private void insertPageFansImageInfo(PageInfo info) {
        insertUserImageList(info.page_id, info.fansList, UserProvider.IMAGE_TYPE_PAEG_FANS);
    }
    
    public int updatePageInfo(long pageid, ContentValues cv){
		//update page info
    	String where = PageColumns.PAGE_ID +" = " + pageid;
    	int count = mContext.getContentResolver().update(PAGE_URI, cv, where, null);
    	return count;
    }
    
    public void insertPageInfo(PageInfo info) {
    	ContentValues cv = createPageInfoValue(info);
    	Uri uri = mContext.getContentResolver().insert(PAGE_URI, cv);
    	deleteUserImageInfo(info.page_id);
    	insertPageFansImageInfo(info);
    }
    
    public void deletePageByPageId(long pageId) {
		String where = PageColumns.PAGE_ID +"='" + pageId+"'";
    	int count = mContext.getContentResolver().delete(PAGE_URI, where, null);
    	Log.d(TAG, "deletePageBypageId: " + count);
	}

	public void removeCirclePageId(long pageid) {
		String where = GroupColumns.PAGE_ID + " = " + pageid;
		ContentValues cv = new ContentValues();
		cv.put(GroupColumns.PAGE_ID, -1);
		mContext.getContentResolver().update(CIRCLE_GROUP_URI, cv, where, null);		
	}
	
	public void updateCirclePageId(long circleid, long pageid) {
		String where = GroupColumns.CIRCLE_ID + " = " + circleid;
        ContentValues cv = new ContentValues();
        cv.put(GroupColumns.PAGE_ID, pageid);
        int count = mContext.getContentResolver().update(CIRCLE_GROUP_URI, cv,where,null);
        Log.d(TAG, "updateCirclePageId: " + count);
    }
	
	private static ContentValues createCircleEventInformationValues(long circleid, UserCircle userCircle){
		ContentValues cv = new ContentValues();
		cv.put(CircleEventsColumns.CIRCLEID, circleid);
		cv.put(CircleEventsColumns.EVENT_ID, userCircle.circleid);
		
		cv.put(CircleEventsColumns.EVENT_NAME, userCircle.name);
		cv.put(CircleEventsColumns.LOCATION, userCircle.location);
		if(userCircle.mGroup != null) {
			cv.put(CircleEventsColumns.START_TIME, userCircle.mGroup.startTime);
			cv.put(CircleEventsColumns.END_TIME, userCircle.mGroup.endTime);
			cv.put(CircleEventsColumns.COVER, userCircle.mGroup.coverUrl);
			cv.put(CircleEventsColumns.CREATE_TIME, userCircle.mGroup.created_time);
			if(userCircle.mGroup.creator != null) {
				cv.put(CircleEventsColumns.CREATOR_ID, userCircle.mGroup.creator.uid);
				cv.put(CircleEventsColumns.CREATOR_NAME, userCircle.mGroup.creator.nick_name);
				cv.put(CircleEventsColumns.CREATOR_IMAGEURL, userCircle.mGroup.creator.profile_image_url);
				cv.put(CircleEventsColumns.ROLE_IN_GROUP, userCircle.mGroup.role_in_group);
			}
		}
		return cv;
	}
	
	public static UserCircle createCircleEventInformation(Cursor cursor) {
        if (null != cursor) {
        	UserCircle result = new UserCircle();
        	result.circleid = cursor.getLong(cursor.getColumnIndex(CircleEventsColumns.EVENT_ID));
        			result.name = cursor.getString(cursor.getColumnIndex(CircleEventsColumns.EVENT_NAME));
        	result.location = cursor.getString(cursor.getColumnIndex(CircleEventsColumns.LOCATION));
        	result.mGroup = new Group();
        	result.mGroup.startTime = cursor.getLong(cursor.getColumnIndex(CircleEventsColumns.START_TIME));
        	result.mGroup.endTime = cursor.getLong(cursor.getColumnIndex(CircleEventsColumns.END_TIME));
        	result.mGroup.coverUrl = cursor.getString(cursor.getColumnIndex(CircleEventsColumns.COVER));
        	result.mGroup.creator = new QiupuSimpleUser();
        	result.mGroup.creator.uid = cursor.getLong(cursor.getColumnIndex(CircleEventsColumns.CREATOR_ID));
        	result.mGroup.creator.nick_name = cursor.getString(cursor.getColumnIndex(CircleEventsColumns.CREATOR_NAME));
        	result.mGroup.creator.profile_image_url = cursor.getString(cursor.getColumnIndex(CircleEventsColumns.CREATOR_IMAGEURL));
        	result.mGroup.role_in_group = cursor.getInt(cursor.getColumnIndex(CircleEventsColumns.ROLE_IN_GROUP));
            return result;
        }
        
        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}
	
	public void deleteCircleEvents(final long circleid) {
		String where = CircleEventsColumns.CIRCLEID + "=" + circleid;
    	mContext.getContentResolver().delete(CIRCLE_EVENTS_URI, where, null); 
	}
	
	public void insertCircleEvents(final long circleid, final List<UserCircle> events) {
    	ArrayList<ContentValues> circleEventInfo = new ArrayList<ContentValues>();
    	for(int i=0;i<events.size(); i++) {
    		final UserCircle userCircle = events.get(i);
    		circleEventInfo.add(createCircleEventInformationValues(circleid, userCircle));
    	}
    	bulkInsert(CIRCLE_EVENTS_URI, circleEventInfo);
	}
	
	private void insertSimpleCircleEvent(final long circleid, final ArrayList<UserCircle> events) {
		if(events != null && events.size() > 0) {
			for(int i=0; i<events.size(); i++) {
				UserCircle tmpCircle = events.get(i);
//				if(isExistCircleEvent(tmpCircle.circleid)) {
//					return ;
//				}else {
					insertOneCircleEvent(circleid, tmpCircle);
//				}
			}
		}
	}
	
	public void insertCategories(final ArrayList<InfoCategory> categories) {
		if(categories != null && categories.size() > 0) {
			for(int i=0; i<categories.size(); i++) {
				InfoCategory tmpCategory = categories.get(i);
				if(isExistCategory(tmpCategory.scopeId, tmpCategory.categoryId)) {
					continue ;
				}else {
					insertCategories(tmpCategory);
				}
			}
		}
	}
	
	private ContentValues createCategoryInformationValues(InfoCategory category){
		ContentValues cv = new ContentValues();
		cv.put(CategoryColumns.CATEGORY_ID, category.categoryId);
		cv.put(CategoryColumns.CATEGORY_NAME, category.categoryName);
		cv.put(CategoryColumns.CREATOR_ID, category.creatorId);
		cv.put(CategoryColumns.SCOPE_ID, category.scopeId);
		cv.put(CategoryColumns.SCOPE_NAME, category.scopeName);
		return cv;
	}
	
	public void insertCategories(final InfoCategory category) {
		ContentValues cv = createCategoryInformationValues(category);
		Uri uri = mContext.getContentResolver().insert(CATEGORY_CONTENT_URI, cv);
	}
	
	private boolean isExistCategory(final long scopeid, final long categoryid) {
		boolean exist = false;
		String where = CategoryColumns.SCOPE_ID + "=" + scopeid + " and " + CategoryColumns.CATEGORY_ID + "=" + categoryid;
		Cursor cursor = mContext.getContentResolver().query(CATEGORY_CONTENT_URI, new String[]{CategoryColumns.CATEGORY_ID},
                where, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			exist = true;
		}
		return exist;
	}
	
	private boolean isExistCircleEvent(final long eventid) {
		boolean exist = false;
		String where = CircleEventsColumns.EVENT_ID + "=" + eventid;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_EVENTS_URI, CIRCLE_EVENT_PROJECTION,
                where, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			exist = true;
		}
		return exist;
	}
	
	public void insertOneCircleEvent(final long circleid, final UserCircle event) {
		ContentValues cv = createCircleEventInformationValues(circleid, event);
		String where = CircleEventsColumns.EVENT_ID + "=" + event.circleid; 
		int count = mContext.getContentResolver().update(CIRCLE_EVENTS_URI, cv, where, null);
		if(count <= 0 ) {
			Uri uri = mContext.getContentResolver().insert(CIRCLE_EVENTS_URI, cv);
		}
	}
	
	public ArrayList<UserCircle> queryCircleEvents(final long circleid) {
		ArrayList<UserCircle> events = new ArrayList<UserCircle>();
		String where = CircleEventsColumns.CIRCLEID + "=" + circleid;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_EVENTS_URI, CIRCLE_EVENT_PROJECTION,
                where, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				events.add(createCircleEventInformation(cursor));
    			} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	
    	return events;
	}
	
	public Cursor queryCircleEventsCursor(final long circleid) {
		String where = CircleEventsColumns.CIRCLEID + "=" + circleid;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_EVENTS_URI, CIRCLE_EVENT_PROJECTION,
                where, null, null);
    	return cursor;
	}
	
	public Cursor searchCircleEventsCursor(final long circleid, String filter) {
		String where = CircleEventsColumns.CIRCLEID + "=" + circleid + " and " + CircleEventsColumns.EVENT_NAME + " like '%" + filter + "%'" ;;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_EVENTS_URI, CIRCLE_EVENT_PROJECTION,
                where, null, null);
    	return cursor;
	}
	
	private static ArrayList<UserCircle> queryLastestCircleEvents(final long circleid, Context context) {
		ArrayList<UserCircle> events = new ArrayList<UserCircle>();
		String where = CircleEventsColumns.CIRCLEID + "=" + circleid ;
		String sortOrder = CircleEventsColumns.CREATE_TIME + " DESC";
		Cursor cursor = context.getContentResolver().query(CIRCLE_EVENTS_URI, CIRCLE_EVENT_PROJECTION,
                where, null, sortOrder);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			events.add(createCircleEventInformation(cursor));
    		}
    		cursor.close();
    		cursor = null;
    	}
    	return events;
	}
	
	public static void removeEmployeeWithCircle(long circleid, String userids, Context context) {
		String where = EmployeeColums.OWNER_ID + "=" + circleid + " and " + EmployeeColums.USER_ID + " in(" + userids + ")";
		int count = context.getContentResolver().delete(EMPLOYEE_CONTENT_URI, where, null);
		Log.d(TAG, "remove employee count : " + count);
	}
	
	public static void updateEmployeeRole(long circleid, String adminIds, String memberIds, Context context) {
		String where = null;
		int count = 0;
		ContentValues cv = new ContentValues();
		if(StringUtil.isValidString(adminIds)) {
			cv.put(EmployeeColums.ROLE_IN_GROUP, PublicCircleRequestUser.ROLE_TYPE_MANAGER);
			where = EmployeeColums.OWNER_ID + "=" + circleid + " and " + EmployeeColums.USER_ID + " in(" + adminIds + ")";
			count = context.getContentResolver().update(EMPLOYEE_CONTENT_URI, cv, where, null);
		}
		if(StringUtil.isValidString(memberIds)) {
			cv.put(EmployeeColums.ROLE_IN_GROUP, PublicCircleRequestUser.ROLE_TYPE_MEMEBER);
			where = EmployeeColums.OWNER_ID + "=" + circleid + " and " + EmployeeColums.USER_ID + " in(" + memberIds + ")";
			count = context.getContentResolver().update(EMPLOYEE_CONTENT_URI, cv, where, null);
		}
		Log.d(TAG, "updateEmployee Role count : " + count);
	}

    private static final String LAST_SYNC_DIRECTORY_PREFIX = "LAST_SYNC_DIRECTORY_PREFIX";
    private final static String DIRECTORY_OWNER_PREFIX =  EmployeeColums.OWNER_ID +"=?";
    private final static String DIRECTORY_EMPLOYEE_PREFIX =  EmployeeColums.USER_ID +"=?";
    private final static String DIRECTORY_USER_PREFIX =  EmployeeColums.USER_ID +"=? and "
            + DIRECTORY_OWNER_PREFIX;
    public void revertDirectoryMemberList(long circleId) {
        // unique record by user id.
//        if(UserProvider.mOpenHelper != null) {
//            StringBuffer selectStr = new StringBuffer("DELETE FROM employee WHERE ");
//            selectStr.append(EmployeeColums.USER_ID).append(" IN (").
//                    append("SELECT ").append(EmployeeColums.USER_ID).
//                    append(" FROM employee GROUP BY ").append(EmployeeColums.USER_ID).
//                    append(" HAVING COUNT(").append(EmployeeColums.USER_ID).append(")>1").
//                    append(")").
//                    append(" AND ").append(EmployeeColums.USER_ID).append(" NOT IN (").
//                    append("SELECT MIN(").append(EmployeeColums.USER_ID).append(") FROM employee GROUP BY ").
//                    append(EmployeeColums.USER_ID).
//                    append(" HAVING COUNT(").append(EmployeeColums.USER_ID).append(")>1").
//                    append(")");
//
//            SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
//            db.rawQuery(selectStr.toString(), null);
//        }
        // unique end.
        ContentValues cv = new ContentValues();
        cv.put(EmployeeColums.DB_STATUS, EmployeeColums.STATUS_DEFAULT);
        int count = mContext.getContentResolver().update(EMPLOYEE_CONTENT_URI,
                cv, DIRECTORY_OWNER_PREFIX, new String[]{String.valueOf(circleId)});
        Log.d(TAG, "revert directory member count: " + count);
    }

    public void removeInvalidDirectory(long circleId) {
//        String where = UsersColumns.DB_STATUS + " = " + QiupuUser.USER_STATUS_DEFAULT;
        final String where = EmployeeColums.DB_STATUS + "=? and " + DIRECTORY_OWNER_PREFIX;
        int count = mContext.getContentResolver().delete(EMPLOYEE_CONTENT_URI,
                where, new String[]{String.valueOf(QiupuUser.USER_STATUS_DEFAULT), String.valueOf(circleId)});
        Log.d(TAG, "removeInvalidUser count: " + count);
    }

    public void updateLocalDirectory() {
        revertDirectoryMemberList(-1);
        insertDirectoryMemberListNoDelete(-1, getLocalFriendsAsEmployee());
//        SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
//        db.beginTransaction();
//        try {
//            ArrayList<Employee> dataList = getLocalFriendsAsEmployee();
//            ContentValues cv;
//            for(Employee emp : dataList){
//                cv = createEmployeeInfoValues(emp, true);
//                int count =  mContext.getContentResolver().update(EMPLOYEE_CONTENT_URI, cv,
//                        DIRECTORY_EMPLOYEE_PREFIX,
//                        new String[]{String.valueOf(emp.user_id)});
//                if(count <= 0 ) {
//                    cv.put(EmployeeColums.OWNER_ID, -1);
//                    mContext.getContentResolver().insert(EMPLOYEE_CONTENT_URI, cv);
//                } else {
//                    Log.v(TAG, "update Employee  ---emp="+emp.user_id+"     emp.name="+emp.name + "      count="+count);
//                }
//            }
//            Log.v(TAG, "insertEmployeeList end ---dataList.size()--- ");
//            db.setTransactionSuccessful();
//        } catch (Exception e) {
//            Log.v(TAG, "insertEmployeeList : " + e.getMessage());
//        } finally {
//            db.endTransaction();
//        }

        removeInvalidDirectory(-1);
    }

    private static Employee parseFriendCursor(Cursor cursor) {
        Employee employee = new Employee();
        String name;
        name = cursor.getString(cursor.getColumnIndex(UsersColumns.USERNAME));
        employee.name = TextUtils.isEmpty(name) ?
                cursor.getString(cursor.getColumnIndex(UsersColumns.NICKNAME)) : name;
        employee.namePinYin = cursor.getString(cursor.getColumnIndex(UsersColumns.NAME_PINGYIN));
        employee.employee_id = null;
        employee.user_id = cursor.getString(cursor.getColumnIndex(UsersColumns.USERID));
        employee.image_url_s = cursor.getString(cursor.getColumnIndex(UsersColumns.PROFILE_SIMAGE_URL));
        employee.image_url_m = cursor.getString(cursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL));
        employee.image_url_l = cursor.getString(cursor.getColumnIndex(UsersColumns.PROFILE_LIMAGE_URL));
        employee.email = null;
        employee.tel = null;
        employee.mobile_tel = null;
        employee.department = null;
        employee.job_title = null;
        employee.is_favorite = false;
        return employee;
    }
    
    public static Employee parseAllEmployeeCursor(Cursor cursor) {
        Employee employee = new Employee();
        employee.name = cursor.getString(cursor.getColumnIndex(EmployeeColums.NAME));
        employee.namePinYin = cursor.getString(cursor.getColumnIndex(EmployeeColums.NAME_PINYIN));
        employee.employee_id = cursor.getString(cursor.getColumnIndex(EmployeeColums.EMPLOYEE_ID));
        employee.user_id = cursor.getString(cursor.getColumnIndex(EmployeeColums.USER_ID));
        employee.image_url_s = cursor.getString(cursor.getColumnIndex(EmployeeColums.IMAGE_URL_S));
        employee.image_url_m = cursor.getString(cursor.getColumnIndex(EmployeeColums.IMAGE_URL_M));
        employee.image_url_l = cursor.getString(cursor.getColumnIndex(EmployeeColums.IMAGE_URL_L));
        employee.email = cursor.getString(cursor.getColumnIndex(EmployeeColums.EMAIL));
        employee.tel = cursor.getString(cursor.getColumnIndex(EmployeeColums.TEL));
        employee.mobile_tel = cursor.getString(cursor.getColumnIndex(EmployeeColums.MOBILE_TEL));
        employee.department = cursor.getString(cursor.getColumnIndex(EmployeeColums.DEPARTMENT));
        employee.job_title = cursor.getString(cursor.getColumnIndex(EmployeeColums.JOB_TITLE));
        employee.role_in_group = cursor.getInt(cursor.getColumnIndex(EmployeeColums.ROLE_IN_GROUP));
        employee.is_favorite = false;
        return employee;
    }

    private ArrayList<Employee> getLocalFriendsAsEmployee() {
        ArrayList<Employee> data = new ArrayList<Employee>();
        String where = UsersColumns.USERID + " not in (" + AccountServiceUtils.getBorqsAccountID() + ")";
        Cursor cursor = mContext.getContentResolver().query(USERS_CONTENT_URI,SIMPLE_USERS_INFO_PROJECTION , where, null, " name_pinyin ASC");
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                do {
                    data.add(parseFriendCursor(cursor));
                } while (cursor.moveToNext());
            }
        }
        closeCursor(cursor);
        return data;
    }

    public void  insertDirectoryMemberListNoDelete(long circleId, ArrayList<Employee> dataList) {
        Log.v(TAG, "insertEmployeeList begin ------ ");
        SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
        db.beginTransaction();
        try {
            Employee emp;
            ContentValues cv;
            for(int i = 0; i < dataList.size(); i++){
                emp = dataList.get(i);
                cv = createEmployeeInfoValues(circleId, emp, true);
                int count =  mContext.getContentResolver().update(EMPLOYEE_CONTENT_URI, cv,
                        DIRECTORY_USER_PREFIX, new String[]{String.valueOf(emp.user_id), String.valueOf(circleId)});
                if(count <= 0 ) {
                    mContext.getContentResolver().insert(EMPLOYEE_CONTENT_URI, cv);
                }else {
                    if(QiupuConfig.DBLOGD)Log.d(TAG, "update Employee  ---emp="+emp.user_id+"     emp.name="+emp.name + "      count="+count);
                }
            }
            Log.v(TAG, "insertEmployeeList end ---dataList.size()--- ");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "insertEmployeeList : " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private ContentValues createEmployeeInfoValues(long circleId, Employee emp,boolean isUpdate) {
        ContentValues contentValues = createEmployeeInfoValues(emp, isUpdate);
        contentValues.put(EmployeeColums.OWNER_ID, circleId);
        return contentValues;
    }

    private ContentValues createEmployeeInfoValues(Employee emp,boolean isUpdate) {
        ContentValues cv = new ContentValues();

        cv.put(EmployeeColums.NAME, emp.name);
        cv.put(EmployeeColums.NAME_PINYIN, emp.namePinYin);
        cv.put(EmployeeColums.EMPLOYEE_ID, emp.employee_id);
        cv.put(EmployeeColums.USER_ID, emp.user_id);
        cv.put(EmployeeColums.IMAGE_URL_S, emp.image_url_s);
        cv.put(EmployeeColums.IMAGE_URL_M, emp.image_url_m);
        cv.put(EmployeeColums.IMAGE_URL_L, emp.image_url_l);
        cv.put(EmployeeColums.EMAIL, emp.email);
        cv.put(EmployeeColums.TEL, emp.tel);
        cv.put(EmployeeColums.MOBILE_TEL, emp.mobile_tel);
        cv.put(EmployeeColums.DEPARTMENT, emp.department);
        cv.put(EmployeeColums.JOB_TITLE, emp.job_title);
        cv.put(EmployeeColums.ROLE_IN_GROUP, emp.role_in_group);
        cv.put(EmployeeColums.DB_STATUS,isUpdate?EmployeeColums.STATUS_UPDATED:EmployeeColums.STATUS_DEFAULT);

//    	 EmployeeColums.IS_FAVORITE 收藏
        return cv;
    }

    public long getDirectoryInfoLastSyncTime(long circleId){
        String value = getSettingValue(LAST_SYNC_DIRECTORY_PREFIX + circleId);
        if(value == null || value.equals("")) {
            return 0;
        }

        if(QiupuConfig.DBLOGD)Log.d(TAG, "getDirectoryInfoLastSyncTime time:"+value);

        return Long.valueOf(value);
    }
    
    public void setDirectoryInfoLastSyncTime(long circleId){
        addSetting(LAST_SYNC_DIRECTORY_PREFIX + circleId, String.valueOf(System.currentTimeMillis()));
    }
    
    
    private static final String LAST_SYNC_CIRCLE_CIRCLES_PREFIX = "LAST_SYNC_CIRCLE_CIRCLES_PREFIX";
    public long getCircleCirclesLastSyncTime(long circleId){
        String value = getSettingValue(LAST_SYNC_CIRCLE_CIRCLES_PREFIX + circleId);
        if(value == null || value.equals("")) {
            return 0;
        }

        if(QiupuConfig.DBLOGD)Log.d(TAG, "getCircleCirclesLastSyncTime time:"+value);

        return Long.valueOf(value);
    }
    public void setCircleCirclesLastSyncTime(long circleId){
        addSetting(LAST_SYNC_CIRCLE_CIRCLES_PREFIX + circleId, String.valueOf(System.currentTimeMillis()));
    }

    public static QiupuSimpleUser parserSimpleUserCursor(Cursor cursor) {
        QiupuSimpleUser info = new QiupuSimpleUser();

        int index = cursor.getColumnIndex(UsersColumns.USERID);
        if (index >= 0) {
            info.uid = cursor.getLong(index);
        }
        if (info.uid == 0) {
            index = cursor.getColumnIndex(EmployeeColums.USER_ID);
            if (index >= 0) {
                info.uid = cursor.getLong(index);
            } else {
                Log.e(TAG, "parserCursor, failed to parse user id.");
                return info;
            }
        }

        index = cursor.getColumnIndex(UsersColumns.NICKNAME);
        if (index >= 0) {
            info.nick_name = cursor.getString(index);
        }
        if (TextUtils.isEmpty(info.nick_name)) {
            index = cursor.getColumnIndex(EmployeeColums.NAME);
            if (index >= 0) {
                info.nick_name = cursor.getString(index);
            } else {
                Log.e(TAG, "parseCursor, failed to parse user name.");
            }
        }

        index = cursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL);
        if (index >= 0) {
            info.profile_image_url = cursor.getString(index);
        }
        if (TextUtils.isEmpty(info.profile_image_url)) {
            index = cursor.getColumnIndex(EmployeeColums.IMAGE_URL_M);
            if (index >= 0) {
                info.profile_image_url = cursor.getString(index);
            } else {
                Log.e(TAG, "parseCursor, failed to parse user profile photo.");
            }
        }

        return info;
    }

    private void dumpSimpleUserInfo(String query, Cursor cursor) {
        if (DEBUG && null != cursor && !TextUtils.isEmpty(query)) {
            Log.d(TAG, "sqlite statment: " + query);
            if (cursor.moveToFirst()) {
                Log.v(TAG, "result count: " + cursor.getCount());
                do {
                    int index = cursor.getColumnIndex(UsersColumns.USERID);

                    Long uid = -1L;
                    if (index >= 0) {
                        uid = cursor.getLong(index);
                    }

                    if (uid <= 0) {
                        index = cursor.getColumnIndex(EmployeeColums.USER_ID);
                        if (index >= 0) {
                            uid = cursor.getLong(index);
                        } else {
                            Log.e(TAG, "parserCursor, failed to parse user id.");
                        }
                    }
                    Log.d(TAG, "dumpSimpleUserInfo, user id:" + uid);

                    index = cursor.getColumnIndex(UsersColumns.NICKNAME);
                    String name = "";
                    if (index >= 0) {
                        name = cursor.getString(index);
                    }
                    if (TextUtils.isEmpty(name)) {
                        index = cursor.getColumnIndex(EmployeeColums.NAME);
                        if (index >= 0) {
                            name = cursor.getString(index);
                        } else {
                            Log.e(TAG, "parseCursor, failed to parse user name.");
                        }
                    }
                    Log.d(TAG, "dumpSimpleUserInfo, user name:" + name);

                    String url = "";
                    index = cursor.getColumnIndex(UsersColumns.PROFILE_IMAGE_URL);
                    if (index >= 0) {
                        url = cursor.getString(index);
                    }
                    if (TextUtils.isEmpty(url)) {
                        index = cursor.getColumnIndex(EmployeeColums.IMAGE_URL_M);
                        if (index >= 0) {
                            url = cursor.getString(index);
                        } else {
                            Log.e(TAG, "parseCursor, failed to parse user profile photo.");
                        }
                    }
                    Log.d(TAG, "dumpSimpleUserInfo, user profile photo:" + url);
                } while (cursor.moveToNext());
            }
        }
    }

    public boolean existingChildCircles(long parentId) {
        Cursor cursor = queryChildCircleList(parentId);
        boolean ret = null != cursor && cursor.getCount() > 0;
        closeCursor(cursor);
        return ret;
    }
    public Cursor queryChildCircleList(long parentId) {
        StringBuffer selectStr = new StringBuffer("SELECT ");
        boolean isFirst = true;
        for(String column : USER_CIRCLE_PROJECTION) {
            if(isFirst) {
                isFirst = false;
                selectStr.append(" circles.");
            } else {
                selectStr.append(", circles.");
            }
            selectStr.append(column);
        }

//        selectStr.append(", groups." + GroupColumns.PROFILE_IMAGE_URL);
//        selectStr.append(", groups." + GroupColumns.PROFILE_SIMAGE_URL);
//        selectStr.append(", groups." + GroupColumns.PROFILE_LIMAGE_URL);
        selectStr.append(" FROM circles LEFT JOIN groups ON circles.").append(CircleColumns.CIRCLE_ID).
                append("=groups.").append(GroupColumns.CIRCLE_ID);
        selectStr.append(" WHERE  ");
        selectStr.append(CircleColumns.TYPE).append(" = ").append(UserCircle.CIRLCE_TYPE_PUBLIC).
                append(" AND ").append(GroupColumns.FORMAL).
                append(" NOT IN (" /*+ UserCircle.circle_sub_formal + ","*/ + UserCircle.circle_top_formal + ")");
//        if(StringUtil.isValidString(key)) {
//            selectStr.append(" AND " + CircleColumns.CIRCLE_NAME + " LIKE \'%"+key+"%\'");
//        }
        selectStr.append(" AND groups.").append(GroupColumns.PARENT_ID).append(" = ").append(parentId);
        selectStr.append(" ORDER BY "+ CircleColumns.CIRCLE_NAME +" ASC ");
        if(UserProvider.mOpenHelper != null)
        {
            SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
            return db.rawQuery(selectStr.toString(), null);

        }

        return null;
    }

    public Cursor queryAllCircleList() {
        StringBuffer selectStr = new StringBuffer("SELECT ");
        boolean isFirst = true;
        for(String column : USER_CIRCLE_PROJECTION) {
            if(isFirst) {
                isFirst = false;
                selectStr.append(" circles.");
            } else {
                selectStr.append(", circles.");
            }
            selectStr.append(column);
        }

        selectStr.append(" FROM circles LEFT JOIN groups ON circles.").append(CircleColumns.CIRCLE_ID).
                append("=groups.").append(GroupColumns.CIRCLE_ID);
        selectStr.append(" WHERE  ");
        selectStr.append(CircleColumns.TYPE).append(" = ").append(UserCircle.CIRLCE_TYPE_PUBLIC);
//        selectStr.append(" AND ").append(GroupColumns.FORMAL).
//                append(" NOT IN (" /*+ UserCircle.circle_sub_formal + ","*/ + UserCircle.circle_top_formal + ")");
        selectStr.append(" ORDER BY "+ CircleColumns.CIRCLE_NAME +" ASC ");
        if(UserProvider.mOpenHelper != null)
        {
            SQLiteDatabase db = UserProvider.mOpenHelper.getReadableDatabase();
            return db.rawQuery(selectStr.toString(), null);

        }

        return null;
    }
    
    public void insertOneCirclePoll(final PollInfo pollInfo) {
		 ContentValues cv = new ContentValues();
		 cv.put(PollColumns.POLL_ID, pollInfo.poll_id);
		 cv.put(PollColumns.TITLE, pollInfo.title);
		 cv.put(PollColumns.TARGET, pollInfo.target_id);
		 cv.put(PollColumns.CREATED_TIME, pollInfo.created_time);
		 cv.put(PollColumns.END_TIME, pollInfo.end_time);
		 cv.put(PollColumns.ATTEND_STATUS, pollInfo.attend_status);
		 cv.put(PollColumns.ATTEND_COUNT, pollInfo.attend_count);
		 
		 if(pollInfo.sponsor != null) {
			 cv.put(PollColumns.UID, pollInfo.sponsor.uid);
			 cv.put(PollColumns.USER_NAME, pollInfo.sponsor.nick_name);
			 cv.put(PollColumns.IMAGE_URL, pollInfo.sponsor.profile_image_url);
		 }
		 
		 mContext.getContentResolver().insert(CIRCLE_POLL_URI, cv);
	}
    
    private void insertSimpleCirclePoll(final ArrayList<PollInfo> polls) {
		if(polls != null && polls.size() > 0) {
			for(int i=0; i<polls.size(); i++) {
				PollInfo tmpPoll = polls.get(i);
				if(isExistCirclePoll(tmpPoll.poll_id)) {
					return ;
				}else {
					insertOneCirclePoll(tmpPoll);
				}
			}
		}
	}
    
    public static ArrayList<PollInfo> queryLimitCirclePollList(Context context, String circle_id){
    	String sortOrder = PollColumns.CREATED_TIME + " DESC limit 0,2";
    	ArrayList<PollInfo> list = new ArrayList<PollInfo>();
        Cursor cursor = context.getContentResolver().query(CIRCLE_POLL_URI, CIRCLE_POLL_PROJECTION, PollColumns.TARGET + " = " + circle_id, null, sortOrder);
        if(cursor !=null) {
            if (cursor.getCount()>0) {
                while(cursor.moveToNext()) {
                    PollInfo info = createPollInfo(context,cursor, circle_id);
                    if (info != null) {
                        list.add(info);
                    }
                }
            }
            cursor.close();
        }
        return list;
    }
    
    private boolean isExistCirclePoll(final String pollid) {
		boolean exist = false;
		String where = PollColumns.POLL_ID + "=" + pollid;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_POLL_URI, CIRCLE_POLL_PROJECTION,
                where, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			exist = true;
		}
		return exist;
	}
    
    public static Cursor queryCircleEmployee(final Context context, final long circleId) {
    	String where = EmployeeColums.OWNER_ID + " = " + circleId;
    	String orderby = EmployeeColums.ROLE_IN_GROUP + " DESC ";
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, orderby);
    	return cursor;
    }
    
    public static Cursor queryRefrerredEmployee(final Context context, final long circleId, final int limitCount) {
    	String where = EmployeeColums.OWNER_ID + " = " + circleId + " and " + EmployeeColums.REFERRED_COUNT + " >0" ;
    	String orderby = EmployeeColums.REFERRED_COUNT + " DESC LIMIT " + limitCount ;
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, orderby);
    	return cursor;
    }
    
    public static Cursor queryCircleEmployeeWithPinyin(final Context context, final long circleId) {
    	String where = EmployeeColums.OWNER_ID + " = " + circleId;
    	String orderby = EmployeeColums.NAME_PINYIN + " ASC ";
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, orderby);
    	return cursor;
    }
    
    public static Cursor queryCircleEmployeeWithPinyinFilter(final Context context, final long circleId, String filterid, String searchkey, String sortby) {
    	String where = EmployeeColums.OWNER_ID + " = " + circleId + " and " + EmployeeColums.USER_ID + " not in (" + filterid + ")";
    	if(StringUtil.isValidString(searchkey)) {
    		where = where + " and (" + EmployeeColums.NAME + " like '%" + searchkey + "%' or " + EmployeeColums.NAME_PINYIN + " like '%" + searchkey + "%')" ;;
    	}
    	
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, sortby);
    	return cursor;
    }
    
    public static Cursor searchCircleEmployeeWithPinyin(final Context context, final long circleId, final String filter) {
    	String where = EmployeeColums.OWNER_ID + " = " + circleId + " and (" + EmployeeColums.NAME + " like '%" + filter + "%' or " + EmployeeColums.NAME_PINYIN + " like '%" + filter + "%')" ;
    	
    	String orderby = EmployeeColums.NAME_PINYIN + " ASC ";
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, orderby);
    	return cursor;
    }
    
    
    
    public static Cursor queryCircleEmployeeWithFilter(final Context context, final long circleId, final String filterString) {
    	
    	String where = EmployeeColums.OWNER_ID + " = " + circleId + " and (" + EmployeeColums.NAME + " like '%" + filterString + "%' or " + EmployeeColums.NAME_PINYIN + " like '%" + filterString + "%')" ;
    	String orderby = EmployeeColums.ROLE_IN_GROUP + " DESC ";
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, orderby);
    	return cursor;
    }
    
    public static Cursor querySubEmployee(final Context context, final long circleId, final String circleName) {
    	String where = EmployeeColums.DEPARTMENT + " = '"+ circleName + "'";
    	String orderby = EmployeeColums.ROLE_IN_GROUP + " DESC ";
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, orderby);
    	return cursor;
    }
    
    public static Cursor querySubEmployeeWithFilter(final Context context, final long circleId, final String circleName, String filterString) {
    	String where = EmployeeColums.DEPARTMENT + " = '"+ circleName + "' and (" + EmployeeColums.NAME + " like '%" + filterString + "%' or " + EmployeeColums.NAME_PINYIN + " like '%" + filterString + "%'" + ")";
    	String orderby = EmployeeColums.ROLE_IN_GROUP + " DESC ";
    	Cursor cursor = context.getContentResolver().query(EMPLOYEE_CONTENT_URI, EmployeeColums.PROJECTION, where, null, orderby);
    	return cursor;
    }

    private ContentValues createCircleCirclesValues(long circleid, UserCircle userCircle){
		ContentValues cv = new ContentValues();
		cv.put(CircleCirclesColumns.PARENT_ID, circleid);
		cv.put(CircleCirclesColumns.CIRCLEID, userCircle.circleid);
		cv.put(CircleCirclesColumns.CIRCLE_NAME, userCircle.name);
		cv.put(CircleCirclesColumns.PROFILE_IMAGE_URL, userCircle.profile_image_url);
		cv.put(CircleCirclesColumns.PROFILE_SIMAGE_URL, userCircle.profile_simage_url);
		cv.put(CircleCirclesColumns.DESCRIPTION, userCircle.description);
		
		if(userCircle.mGroup != null) {
			cv.put(CircleCirclesColumns.ROLE_IN_GROUP, userCircle.mGroup.role_in_group);
			cv.put(CircleCirclesColumns.FORMAL, userCircle.mGroup.formal);
			cv.put(CircleCirclesColumns.SUBTYPE, userCircle.mGroup.subtype);
		}
		return cv;
	}
    
    public static UserCircle createCircleCircles(Cursor cursor) {
        if (null != cursor) {
        	UserCircle result = new UserCircle();
        	result.circleid = cursor.getLong(cursor.getColumnIndex(CircleCirclesColumns.CIRCLEID));
        	result.name = cursor.getString(cursor.getColumnIndex(CircleCirclesColumns.CIRCLE_NAME));
        	result.profile_image_url = cursor.getString(cursor.getColumnIndex(CircleCirclesColumns.PROFILE_IMAGE_URL));
        	result.profile_limage_url = cursor.getString(cursor.getColumnIndex(CircleCirclesColumns.PROFILE_LIMAGE_URL));
        	result.profile_simage_url = cursor.getString(cursor.getColumnIndex(CircleCirclesColumns.PROFILE_SIMAGE_URL));
        	result.description = cursor.getString(cursor.getColumnIndex(CircleCirclesColumns.DESCRIPTION));
        	result.mGroup = new Group();
        	result.mGroup.role_in_group = cursor.getInt(cursor.getColumnIndex(CircleCirclesColumns.ROLE_IN_GROUP));
        	result.mGroup.formal = cursor.getInt(cursor.getColumnIndex(CircleCirclesColumns.FORMAL));
        	result.mGroup.subtype= cursor.getString(cursor.getColumnIndex(CircleCirclesColumns.SUBTYPE));
        	result.mGroup.parent_id = cursor.getLong(cursor.getColumnIndex(CircleCirclesColumns.PARENT_ID));
            return result;
        }
        
        Log.e(TAG, "createCircleInformation, unexpected null cursor.");
		return null;
	}
    
    public ArrayList<UserCircle> queryCircleCircles(long circleId) {
    	ArrayList<UserCircle> tmpCircles = new ArrayList<UserCircle>();
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId;
		String sortBy = CircleCirclesColumns.REFERRED_COUNT + " DESC";
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, sortBy);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				tmpCircles.add(createCircleCircles(cursor));
    			} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	
    	return tmpCircles;
    }
    
    public Cursor queryCircleCircles(long circleId, String subType, String searchkey) {
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId + " and " + CircleCirclesColumns.SUBTYPE + " = '" + subType + "'";
		if(!TextUtils.isEmpty(searchkey)) {
			where = where + " and " + CircleCirclesColumns.CIRCLE_NAME + " like '%" + searchkey + "%'";
		}
		String sortBy = CircleCirclesColumns.REFERRED_COUNT + " DESC";
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, sortBy);
    	return cursor;
    }
    
    public Cursor queryCircleFreeCircles(long circleId, String searchkey) {
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId + " and " + CircleCirclesColumns.FORMAL + " = " + 0;
		if(!TextUtils.isEmpty(searchkey)) {
			where = where + " and " + CircleCirclesColumns.CIRCLE_NAME + " like '%" + searchkey + "%'";
		}
		String sortBy = CircleCirclesColumns.REFERRED_COUNT + " DESC";
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, sortBy);
    	return cursor;
    }
    
    //query circle's circles which i am in.
    public Cursor queryInCircleCircles(long circleId) {
    	String roleStr = PublicCircleRequestUser.ROLE_TYPE_CREATER + "," + PublicCircleRequestUser.ROLE_TYPE_MANAGER + "," + PublicCircleRequestUser.ROLE_TYPE_MEMEBER;
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId + " and " + CircleCirclesColumns.ROLE_IN_GROUP + " in (" + roleStr + ")";
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, null);
    	
    	return cursor;
    }
    
    public ArrayList<UserCircle> queryInCircleCirclesList(long circleId) {
    	ArrayList<UserCircle> tmpCircles = new ArrayList<UserCircle>();
    	String roleStr = PublicCircleRequestUser.ROLE_TYPE_CREATER + "," + PublicCircleRequestUser.ROLE_TYPE_MANAGER + "," + PublicCircleRequestUser.ROLE_TYPE_MEMEBER;
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId + " and " + CircleCirclesColumns.ROLE_IN_GROUP + " in (" + roleStr + ")";
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				tmpCircles.add(createCircleCircles(cursor));
    			} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	
    	return tmpCircles;
    }
    
    public Cursor queryChildCircles(long circleId) {
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, null);
    	return cursor;
    }
    
    public Cursor queryChildCirclesFilter(long circleId, String filterids, String searchkey) {
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId + " and " + CircleCirclesColumns.CIRCLEID + " not in (" + filterids + ")";
		if(StringUtil.isValidString(searchkey)) {
    		where = where + " and " + CircleCirclesColumns.CIRCLE_NAME + " like '%" + searchkey + "%'";
    	}
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, null);
    	return cursor;
    }
    
    public Cursor searchChildCircles(long circleId, String filter) {
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId + " and " + CircleCirclesColumns.CIRCLE_NAME + " like '%" + filter + "%'" ;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, null);
    	return cursor;
    	
    }
    
    public ArrayList<UserCircle> queryCircleCirclesWithFilter(long circleId, String filterString) {
    	ArrayList<UserCircle> tmpCircles = new ArrayList<UserCircle>();
		String where = CircleCirclesColumns.PARENT_ID + " = " + circleId + " and " + CircleCirclesColumns.CIRCLE_NAME + " like '%" + filterString + "%'" ;
		Cursor cursor = mContext.getContentResolver().query(CIRCLE_CIRCLES_CONTENT_URI, CIRCLE_CIRCLES_PROJECTION,
                where, null, null);
    	if(cursor != null) {
    		if(cursor.moveToFirst()) {
    			do {
    				tmpCircles.add(createCircleCircles(cursor));
    			} while (cursor.moveToNext());
    		}
    		cursor.close();
    		cursor = null;
    	}
    	
    	return tmpCircles;
    	
    }
    
    public void removeCircleCircles(long circleId) {
    	String where = CircleCirclesColumns.PARENT_ID + " = " + circleId;
    	mContext.getContentResolver().delete(CIRCLE_CIRCLES_CONTENT_URI, where, null);
    	
    }
	public void insertCirclesByCircle(long circleId, ArrayList<UserCircle> userCircles) {
		ArrayList<ContentValues> circleCirclesInfo = new ArrayList<ContentValues>();
    	for(int i=0;i<userCircles.size(); i++) {
    		final UserCircle userCircle = userCircles.get(i);
    		circleCirclesInfo.add(createCircleCirclesValues(circleId, userCircle));
    	}
    	bulkInsert(CIRCLE_CIRCLES_CONTENT_URI, circleCirclesInfo);
	}
	
	public void insertOneCircleCircles(long parentId, UserCircle userCircle) {
		ContentValues cv = createCircleCirclesValues(parentId, userCircle);
		String where =  CircleCirclesColumns.CIRCLEID +"="+userCircle.circleid;
    	int count = mContext.getContentResolver().update(CIRCLE_CIRCLES_CONTENT_URI, cv, where,null);
    	if(count <= 0) {
    		mContext.getContentResolver().insert(CIRCLE_CIRCLES_CONTENT_URI, cv);
    	}
	}
	
	public void removeOneCircleCircles(long circleid) {
		String where =  CircleCirclesColumns.CIRCLEID +"="+circleid;
		mContext.getContentResolver().delete(CIRCLE_CIRCLES_CONTENT_URI, where, null);
	}
	
	public static final Cursor queryOneuserPhoneEmail(long uid, Context context) {
		final String where =  PhoneEmailColumns.USERID +"="+uid;
		Cursor cursor = context.getContentResolver().query(PHONEEMAIL_CONTENT_URI, new String[]{PhoneEmailColumns.INFO} , where, null, null);
		return cursor;
	}

	public void deleteCacheCircleCircle(UserCircle circle) {
		if(circle == null ) {
			return;
		}
		if(circle.mGroup != null && circle.mGroup.parent_id <= 0) {
			// is top circle, need remove all childcircles 
		    removeCircleCircles(circle.circleid);
		}else {
			removeOneCircleCircles(circle.circleid);
		}
	}
}
