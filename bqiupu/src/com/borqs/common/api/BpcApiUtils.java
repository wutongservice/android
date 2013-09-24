package com.borqs.common.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import twitter4j.Stream;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-10-21
 * Time: 上午11:40
 * To change this template use File | Settings | File Templates.
 */

/**
 *  Helper class to access BPC exported Api to other component.
 *  @author b608
 */
public class BpcApiUtils {
    private static final String TAG = "BpcApiUtils";
    /**
     *  the scheme, host of BPC, which are identical to those in AndroidManifest.xml
     */
    private static final String SCHEME = "borqs";
//    public static final String HOST = "borqs.com";

    /**
     *  the scheme path and search method to launch Stream Activity with specific parameter, 3rd party could simply
     *  call wrapper method startStreamActivityWithAppId or startStreamActivityWithStreamType rather use such  definitions directly.
     */
    public static final String SEARCH_KEY_ID = "id";    // general identity
    public static final String SEARCH_KEY_UID = "uid";  // for user identity
    public static final String SEARCH_KEY_APPID = "appid";
    public static final String SEARCH_KEY_TYPE = "type";
    public static final String SEARCH_KEY_TAB = "tab";
    public static final String SEARHC_KEY_CIRCLEID = "circleId";

    /**
     *  Friends tab
     */
    public static int TAB_FRIENDS_CIRCLE = 0;
    public static int TAB_FRIENDS_PEOPLE = 1;
    public static int TAB_FRIENDS_SUGGESTION = 2; // People may know

    private static final String STREAM_PATH = "stream/details";
    public static final String STREAM_SCHEME_PREFIX = SCHEME + "://" + STREAM_PATH;

    private static final String FRIENDS_PATH = "friends/details";
    public static final String FRIENDS_SCHEME_PREFIX = SCHEME + "://" +  FRIENDS_PATH;

    private static final String PROFILE_PATH = "profile/details";
    public static final String PROFILE_SCHEME_PREFIX = SCHEME + "://"  + PROFILE_PATH;
    public static final String PROFILE_SEARCH_USERID_PREFIX = PROFILE_SCHEME_PREFIX +
            "?" + SEARCH_KEY_UID + "=";

    public static final String STREAM_COMMENT_SCHEME_PATH = "borqs://stream/comment";
    public static final String APP_COMMENT_SCHEME_PATH = "borqs://stream/comment";
    public static final String APP_FAVORITE_SCHEME_PATH = "borqs://application/user_favorite";
    public static final String APP_DETAIL_SCHEME_PATH = "borqs://application/detail";
    public static final String APP_LIST_SCHEME = "borqs://application/mylist";


    public static final String CIRCLE_INVITATION_SCHEME_PATH = "borqs://circle/invitation";
    public static final String CIRCLE_JOIN_REQUEST_SCHEME_PATH = "borqs://circle/join_request";

    /**
     *  APPID, which is identical to the definition from server side.
     */
    public static class APPID {
        public static final int NONE = -1;
        public static final int APK = 1;
        public static final int BOOK = 2;
        public static final int MUSIC = 3;
        public static final int BPC_APPID = 9; // define by server side.

    };

    /**
     *  Valid stream type, keep identical to server definition.
     */
    public static final int TEXT_POST = 1;
    public static final int IMAGE_POST = 1 << 1;
    public static final int VIDEO_POST = 1 << 2;
    public static final int AUDIO_POST = 1 << 3;
    public static final int BOOK_POST = 1 << 4;
    public static final int APK_POST = 1 << 5;
    public static final int LINK_POST = 1 << 6;
    public static final int APK_LINK_POST = 1 << 7;
    public static final int APK_COMMENT_POST = 1 << 8;
    public static final int APK_LIKE_POST = 1 << 9;
    public static final int BOOK_LIKE_POST = 1 << 10;
    public static final int BOOK_COMMENT_POST = 1 << 11;
    public static final int MAKE_FRIENDS_POST = 1 << 12;
    public static final int MUSIC_POST = 1 << 13;
    public static final int MUSIC_COMMENT_POST = 1 << 14;
    public static final int MUSIC_LIKE_POST = 1 << 15;
    public static final int STATIC_FILE_POST = 131072;

    /**
     * MAX_STREAM_POST_OFFSET will be kept sync with the maximize bit of valid stream type.
     */
    protected static final int MAX_STREAM_POST_OFFSET = 15;
    /**
     *  Helper shortcut definition for ALL posts.
     */
    public static final int ALL_TYPE_POSTS = -1;

    /**
     *  Helper shortcut definition for Apk posts.
     */
    public static final int ONLY_PURE_APK_POST = (APK_POST | APK_LIKE_POST | APK_COMMENT_POST );
    public static final int ONLY_APK_POST = (APK_POST | APK_LIKE_POST | APK_COMMENT_POST | APK_LINK_POST);
    public static final int ALL_APK_POST = (TEXT_POST | ONLY_APK_POST);

    /**
     *  Helper shortcut definition for Book posts.
     */
    public static final int ONLY_BOOK_POST = (BOOK_POST | BOOK_LIKE_POST | BOOK_COMMENT_POST);
    public static final int ALL_BOOK_POST = (TEXT_POST | ONLY_BOOK_POST);

    /**
     *  Helper shortcut definition for Music posts.
     */
    public static final int ONLY_MUSIC_POST = (MUSIC_POST | MUSIC_LIKE_POST | MUSIC_COMMENT_POST);
    public static final int ALL_MUSIC_POST = (TEXT_POST | ONLY_MUSIC_POST);

    /**
     *  Helper definition the package name for those will integrated into BPC home activity, which
     *  could use to download from Apk server, launch up from Bpc, etc.
     */
    public static final String TARGET_PKG_QIUPU = "com.borqs.qiupu";
    public static final String TARGET_PKG_BROOK = "com.borqs.brook";
    public static final String TARGET_PKG_BMUSIC = "com.borqs.music";
    public static final String TARGET_PKG_BACCOUNT = "com.borqs.service.accountsync";
    /**
     *  verify if it is a valid app Id of current design.
     * @param appId, int, which is used as identification of an valid application of server.
     * @return true if it is recognized by current system design, other wise, false..
     */
    public static boolean isValidAppId(int appId) {
        return (appId > APPID.NONE && appId <= APPID.MUSIC)
                || appId == APPID.BPC_APPID;
    }

    /**
     *
     * @param type
     * @return
     */
    public static boolean isValidStreamType(int type) {
        Log.v(TAG, "isValidStreamType, type:" + type);
        boolean ret = false;
        if (type > 0 && (type >> (MAX_STREAM_POST_OFFSET + 1)) < 1) {
            for (int i = 0; i <= MAX_STREAM_POST_OFFSET; ++i) {
                if ((1 & type) == 1) {
                    Log.v(TAG, "isValidStreamType, type: " + type + ", i = " + i + ", ret = " + ret);
                    ret = true;
                    break;
                } else  if (type < 1) {
                    Log.v(TAG, "isValidStreamType, type: " + type + ", i = " + i + ", ret = " + ret);
                    break;
                }

                type >>= 1;
            }
        } else if (ALL_TYPE_POSTS == type) {
            ret = true;
        } else if(STATIC_FILE_POST == type) {
        	ret = true;
        }

        Log.v(TAG, "isValidStreamType, return ret:" + ret);
        return ret;
    }

    /**
     *  Launch Stream Activity for specific applications. The alternative method is  startStreamActivityWithStreamType
     * @param context
     * @param appId
     */
    public static void startFriendsActivityWithAppId(Context context, int appId, long userId) {
        final String schemeTxt = FRIENDS_SCHEME_PREFIX +
                "?" + SEARCH_KEY_APPID + "=" + appId +
                "&" + SEARCH_KEY_UID + "=" + userId;
        Log.d(TAG, "startFriendsActivityWithAppId, appId:" + appId + ", userId:" + userId +
                ", scheme:" + schemeTxt);
        startActivityByScheme(context, schemeTxt);
    }

    public static void startFriendsActivityWithAppId(Context context, int appId, long userId, int tab) {
        final String schemeTxt = FRIENDS_SCHEME_PREFIX +
                "?" + SEARCH_KEY_APPID + "=" + appId +
                "&" + SEARCH_KEY_UID + "=" + userId +
                "&" + SEARCH_KEY_TAB + "=" + tab;
        Log.d(TAG, "startFriendsActivityWithAppId, appId:" + appId + ", userId:" + userId +
                ", tab:" + tab + ", scheme:" + schemeTxt);
        startActivityByScheme(context, schemeTxt);
    }


    /**
     *  Launch Stream Activity for specific applications. The alternative method is  startStreamActivityWithStreamType
     * @param context
     * @param appId
     */
    public static void startProfileActivityWithAppId(Context context, int appId, long userId) {
        final String schemeTxt = PROFILE_SCHEME_PREFIX +
                "?" + SEARCH_KEY_APPID + "=" + appId +
                "&" + SEARCH_KEY_UID + "=" + userId;
        startActivityByScheme(context, schemeTxt);
    }

    /**
     *  Launch Stream Activity for specific applications. The alternative method is  startStreamActivityWithStreamType
     * @param context
     * @param appId
     */
    public static void startStreamActivityWithAppId(Context context, int appId) {
        final String schemeTxt = STREAM_SCHEME_PREFIX + "?" + SEARCH_KEY_APPID + "=" + appId;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schemeTxt));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("home", "main");
        Log.d(TAG, "startStreamActivityWithAppId, appId:" + appId + ", intent:" + intent);
        context.startActivity(intent);
    }

    /**
     *  Launch Stream Activity for specific Stream type. The alternative method is startStreamActivityWithAppId.
     * @param context
     * @param type
     */
    public static void startStreamActivityWithStreamType(Context context, int type) {
        final Intent intent = encodeStreamTypeIntent(type);
        if (null == intent) {
            Log.i(TAG, "startStreamActivityWithStreamType, failed to encode intent for stream type = " + type);
        } else {
            Log.d(TAG, "startStreamActivityWithStreamType, type:" + type + ", intent:" + intent);
            context.startActivity(intent);
        }
    }

    public static Intent encodeStreamTypeIntent(int type) {
        final String schemeTxt = STREAM_SCHEME_PREFIX + "?" + SEARCH_KEY_TYPE + "=" + type;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schemeTxt));
        intent.addCategory(Intent.CATEGORY_DEFAULT);        
        intent.putExtra("home", "main");        
        return intent;

    }

    public static int parseSearchedSteamAppId(final Intent intent) {
        final String value = parseSchemeValue(intent, SEARCH_KEY_APPID);
        return TextUtils.isEmpty(value) ? -1 : Integer.valueOf(value);
//        int appId = -1;
//        if (null != intent &&
//                Intent.ACTION_VIEW.equals(intent.getAction()) &&
//                Intent.CATEGORY_DEFAULT.equals(intent.getCategories())) {
//            final Uri uri = intent.getData();
//            if (null != uri) {
//                appId = Integer.valueOf(uri.getQueryParameter(SEARCH_KEY_APPID));
//            }
//        }
//        return appId;
    }

    public static int parseSearchedStreamType(final Intent intent) {
        final String value = parseSchemeValue(intent, SEARCH_KEY_TYPE);
        return TextUtils.isEmpty(value) ? -1 : Integer.valueOf(value);
//        int type = -1;
//        if (null != intent &&
//                Intent.ACTION_VIEW.equals(intent.getAction())) {
//            Set<String> categorySet = intent.getCategories();
//            if (categorySet.size() > 0 &&
//                    categorySet.contains(Intent.CATEGORY_DEFAULT)) {
//            final Uri uri = intent.getData();
//                if (null != uri) {
//                    type = Integer.valueOf(uri.getQueryParameter(SEARCH_KEY_TYPE));
//                }
//            }
//        }
//        return type;
    }

    public static String parseSchemeValue(final Intent intent, final String key) {
        if (null != intent) {
            final Uri uri = intent.getData();
            if (null != uri) {
                return uri.getQueryParameter(key);
            }
        }

        return null;
    }

    public static boolean isActivityReadyForIntent(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }

    public static boolean isActivityReadyForAction(Context context, String action) {
        return isActivityReadyForIntent(context, new Intent(action));
    }

//    public static Uri getSchemeDataUriWithUserId(long userId) {
//        return Uri.parse(PROFILE_SEARCH_USERID_PREFIX + userId);
//    }

    private static void startActivityByScheme(Context context, final String schemeTxt) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schemeTxt));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isActivityReadyForIntent(context, intent)) {
            context.startActivity(intent);
        } else {
            Log.d(TAG, "startActivityByScheme, ignore start intent: " + intent);
        }
    }

    public final class Action {
        public final static String SHARE_SOURCE = "com.borqs.bpc.action.SHARE_SOURCE";
        public final static String SHARE_SOURCE_RESULT = "com.borqs.bpc.action.SHARE_SOURCE_RESULT";
    }

    public final class Result {
        public final static String PACKAGE_NAME = "package";
        public final static String SOURCE_LABEL = "label";
        public final static String SOURCE_COUNT = "count";
        public final static String CALLBACK_SCHEME = "scheme";
        public final static String TARGET_PACKAGE = "target";
    }

    public final class User {
        public static final String USER_ID           =  "USER_ID";
        public static final String USER_NAME         =  "USER_NAME";
        public static final String USER_CIRCLE       =  "USER_CIRCLE";

    }


    public static Bundle getUserBundle(long uid) {
        return getUserBundle(uid, null, null);
    }

    public static Bundle getUserBundle(long uid, String userName) {
        return getUserBundle(uid, userName, null);
    }

    public static Bundle getUserBundle(long uid, String userName, String circleName) {
        Bundle bundle = new Bundle();
        bundle.putLong(User.USER_ID, uid);
        if (!TextUtils.isEmpty(userName)) {
            bundle.putString(User.USER_NAME, userName);
        }

        if (!TextUtils.isEmpty(circleName)) {
            bundle.putString(User.USER_CIRCLE, circleName);
        }

        return bundle;
    }

    public static boolean isValidTypeOfAppAttachment(int type) {
        boolean ret = false;
        if (type == BpcApiUtils.APK_POST || type == BpcApiUtils.APK_COMMENT_POST
                || type == BpcApiUtils.APK_LIKE_POST) {
            ret = true;
        }
        return ret;
    }
}
