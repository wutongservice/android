package com.borqs.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import twitter4j.ApkBasicInfo;
import twitter4j.ApkResponse;
import twitter4j.Company;
import twitter4j.PollInfo;
import twitter4j.QiupuPhoto;
import twitter4j.QiupuSimpleUser;
import twitter4j.QiupuUser;
import twitter4j.Stream;
import twitter4j.UserCircle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.PickFriendsFragment;
import com.borqs.qiupu.service.ApkFileManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.QiupuCommentsActivity;
import com.borqs.qiupu.ui.QiupuCommentsPicActivity;
import com.borqs.qiupu.ui.bpc.AlbumActivity;
import com.borqs.qiupu.ui.bpc.BeamDataByNFCActivity;
import com.borqs.qiupu.ui.bpc.BpcAddFriendsActivity;
import com.borqs.qiupu.ui.bpc.BpcExchangeCardActivity;
import com.borqs.qiupu.ui.bpc.BpcFindFriendsFragmentActivity;
import com.borqs.qiupu.ui.bpc.BpcFriendsActivity;
import com.borqs.qiupu.ui.bpc.BpcFriendsFragmentActivity;
import com.borqs.qiupu.ui.bpc.BpcPostsNewActivity;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.ui.bpc.BpcShakeExchangeCardActivity;
import com.borqs.qiupu.ui.bpc.CircleFragmentActivity;
import com.borqs.qiupu.ui.bpc.ExchangedCardFriendsActivity;
import com.borqs.qiupu.ui.bpc.FriendsFragmentActivity;
import com.borqs.qiupu.ui.bpc.FriendsListActivity;
import com.borqs.qiupu.ui.bpc.GridPicActivity;
import com.borqs.qiupu.ui.bpc.IMComposeActivity;
import com.borqs.qiupu.ui.bpc.LinksTraActivity;
import com.borqs.qiupu.ui.bpc.PhotosViewActivity;
import com.borqs.qiupu.ui.bpc.PickAudienceActivity;
import com.borqs.qiupu.ui.bpc.PickCircleUserActivity;
import com.borqs.qiupu.ui.bpc.PickPeopleActivity;
import com.borqs.qiupu.ui.bpc.PickUserFragmentActivity;
import com.borqs.qiupu.ui.bpc.PickVCardActivity;
import com.borqs.qiupu.ui.bpc.PollCreateActivity;
import com.borqs.qiupu.ui.bpc.PollDetailActivity;
import com.borqs.qiupu.ui.bpc.PollListActivity;
import com.borqs.qiupu.ui.bpc.QiupuComposeActivity;
import com.borqs.qiupu.ui.bpc.RequestActivity;
import com.borqs.qiupu.ui.bpc.ShareResourcesActivity;
import com.borqs.qiupu.ui.bpc.TopPostListActivity;
import com.borqs.qiupu.ui.bpc.UserCircleDetailActivity;
import com.borqs.qiupu.ui.bpc.UserCircleSelectedActivity;
import com.borqs.qiupu.ui.bpc.UserProfileFragmentActivity;
import com.borqs.qiupu.ui.bpc.UsersArrayListActivity;
import com.borqs.qiupu.ui.bpc.UsersCursorListActivity;
import com.borqs.qiupu.ui.circle.CircleEventsActivity;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import com.borqs.qiupu.ui.circle.EventDetailActivity;
import com.borqs.qiupu.ui.circle.PublicCirclePeopleActivity;
import com.borqs.qiupu.ui.circle.UserPublicCircleDetailActivity;
import com.borqs.qiupu.ui.company.CompanyDetailActivity;
import com.borqs.qiupu.ui.page.CreatePageActivity;
import com.borqs.qiupu.ui.page.PageDetailActivity;
import com.borqs.qiupu.ui.page.PageListActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.wutong.HomePickerActivity;
import com.borqs.wutong.OrganizationHomeActivity;

/**
 * Created by IntelliJ IDEA.
 * User: b608
 * Date: 11-8-29
 * Time: 下午4:47
 * To change this template use File | Settings | File Templates.
 */
public class IntentUtil {
    private final static String TAG = "IntentUtil";

    public static final String EXTRA_KEY_CIRCLE_ID = "circleid";

    // TODO: combine all intent action in this class, keep synchronization as AdnroidManifest.xml
    public final static String ACTION_BORQS_ACCOUNT_SERVICE = "com.borqs.account.service.BorqsAccountService_wutong";
    public final static String ACTION_COLLECT_PHONE_INFO = "com.android.borqsaccount.service.COLLECTPHONEINFO_wutong";

    public final static String ACTION_MAIN_ACTIVITY = "com.borqs.bpc.maintab";

    protected final static String ACTION_START_BORQS_ACCOUNT_SERVICE = "com.borqs.account.service.BorqsAccountService_Start_wutong";

    public final static String ACTION_PICK_APPS = "android.intent.action.PICKAPPS";
    public final static String WUTONG_ACTION_TAGS = "wutong.intent.action.TAGS";

    public static final int WUTONG_NOTIFICATION_ID = 16;

    public static void shootCollectPhoneInfoIntent(Context context) {
        Intent intent = new Intent(ACTION_COLLECT_PHONE_INFO);
        context.startService(intent);
    }

    public static void shootBorqsAccountServiceStartIntent(Context context) {
        Intent accountService = new Intent(ACTION_START_BORQS_ACCOUNT_SERVICE);
        context.startService(accountService);
    }

    public static Intent parseValidServiceIntent(Context context, String globalAction, String localAction) {
        Intent intent = new Intent(globalAction);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> l = pm.queryIntentServices(intent, 0);
        if (l.isEmpty()) {
            intent = new Intent(localAction);
        }

        return intent;
    }

    public static Intent getBorqsAccountServiceBindIntent() {
        final Intent intent = new Intent(ACTION_BORQS_ACCOUNT_SERVICE);
        return intent;
    }

    /**
     * startUserDetailIntent, helper methods to show user deatail info
     */
    public static void startUserDetailIntent(Context context, QiupuUser user) {
        UserProfileFragmentActivity.startUserDetailIntent(context, user);
    }

    public static void startUserDetailIntent(Context context, QiupuSimpleUser user) {
        startUserDetailIntent(context, user.uid, user.nick_name);
    }

    public static void startUserDetailIntent(Context context, long uid) {
        startUserDetailIntent(context, uid, null, null);
    }

    public static void startUserDetailIntent(Context context, long uid, String nickName) {
        startUserDetailIntent(context, uid, nickName, null);
    }

    public static void startUserDetailIntent(Context context, long uid, String nickName, String circleName) {
        UserProfileFragmentActivity.startUserDetailIntent(context, uid, nickName, circleName);
    }

    public static void startUserDetailAboutIntent(Context context, long uid, String nickName) {
        UserProfileFragmentActivity.startUserDetailAboutIntent(context, uid, nickName);
    }

    public static Intent buildUserDetailIntent(Context context, long uid, String nickName) {
        return UserProfileFragmentActivity.buildUserDetailIntent(context, uid, nickName);
    }


    // end of startUserDetailIntent


    public static void startComposeIntent(Context context) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        context.startActivity(intent);
    }

    public static void startTakingPhotoIntent(Context context) {
        QiupuComposeActivity.startTakingPhotoIntent(context);
    }

    public static void startPickingPhotoIntent(Context context) {
        QiupuComposeActivity.startPickingPhotoIntent(context);
    }

    public static void startTakingPhotoIntent(Context context, String recipient, long scene, long fromid) {
        QiupuComposeActivity.startTakingPhotoIntent(context, recipient, scene, fromid);
    }

    public static void startPickingPhotoIntent(Context context, String recipient, long scene, long fromid) {
        QiupuComposeActivity.startPickingPhotoIntent(context, recipient, scene, fromid);
    }
    
    public static void startTakingPhotoIntent(Context context, String recipient, HashMap<String, String> recipientMap, long scene, long fromid) {
        QiupuComposeActivity.startTakingPhotoIntent(context, recipient, scene, fromid);
    }

    public static void startPickingPhotoIntent(Context context, String recipient, HashMap<String, String> recipientMap, long scene, long fromid) {
        QiupuComposeActivity.startPickingPhotoIntent(context, recipient, scene, fromid);
    }

    public static void startComposeIntent(Context context, String fileUri, String photo_id) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra(QiupuComposeActivity.EXTRA_FLAG_KEY, QiupuComposeActivity.EXTRA_FLAG_SERVER);
        intent.putExtra(QiupuComposeActivity.EXTRA_FILE_URI, fileUri);
        intent.putExtra(QiupuComposeActivity.EXTRA_PHOTO_ID, photo_id);
        
        context.startActivity(intent);
    }
    
    public static void startTopPostIntent(Context context, long id,String title, boolean isAdmin) {
        Intent intent = new Intent(context,TopPostListActivity.class);
        intent.putExtra(TopPostListActivity.EXTRA_ID_KEY,id);
    	intent.putExtra(TopPostListActivity.EXTRA_TITLE_KEY,title);
    	intent.putExtra(TopPostListActivity.EXTRA_VIEWER_ROLE_KEY, isAdmin);
    	
    	context.startActivity(intent);
    }

    public static void startPollIntent(Context context, long userId, String UserName, int currentScreen, boolean fromCircle) {
        Intent intent = new Intent(context,PollListActivity.class);
        intent.putExtra(PollListActivity.EXTRA_USER_ID_KEY, userId);
        intent.putExtra(PollListActivity.EXTRA_CURRENT_SCREEN_KEY,currentScreen);
        intent.putExtra(PollListActivity.EXTRA_USER_NAME_KEY, UserName);
        intent.putExtra(PollListActivity.EXTRA_FROM_CIRCLE, fromCircle);
        context.startActivity(intent);
    }

    public static void startComposeActivity(Context context, String receiverid, boolean isPrivate, boolean isAdmin, HashMap<String, String> receiverMap, long scene, long fromId) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra(QiupuComposeActivity.IS_ADMIN_KEY, isAdmin);
        if (null != receiverid) {
            intent.putExtra("receivers", receiverid);
        }
        intent.putExtra(QiupuComposeActivity.IS_PRIVATE_KEY, isPrivate);
        if(null != receiverMap) {
            intent.putExtra(QiupuComposeActivity.RECEIVER_KEY, receiverMap);
        }
        intent.putExtra(CircleUtils.INTENT_SCENE, scene);
        intent.putExtra(CircleUtils.CIRCLE_ID, fromId);
        context.startActivity(intent);
    }

    public static void startComposeIntent(Context context, String receivers, boolean isPrivate, HashMap<String, String> receiverMap) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        if (null != receivers) {
            intent.putExtra("receivers", receivers);            
        }
        intent.putExtra(QiupuComposeActivity.IS_PRIVATE_KEY, isPrivate);
        if(null != receiverMap) {
        	intent.putExtra(QiupuComposeActivity.RECEIVER_KEY, receiverMap);
        }
        
        final String homeid = QiupuORM.getSettingValue(context, QiupuORM.HOME_ACTIVITY_ID);
    	long homeScene = TextUtils.isEmpty(homeid) ? -1 : Long.parseLong(homeid);
        intent.putExtra(CircleUtils.INTENT_SCENE, homeScene);
        
        context.startActivity(intent);
    }

    public static void startComposeIntent(Context context, Stream post, long[] receivers) {
        startComposeIntent(context, post, receivers, null, null);
    }

    public static void startComposeIntent(Context context, Stream post, long[] receivers, String comment, String reshare) {
        Intent intent = new Intent(context, QiupuComposeActivity.class);
        intent.putExtra(QiupuComposeActivity.RESHARE_KEY, reshare);
        if (null != post) {
            boolean ser = QiupuHelper.serialization(post);
            intent.putExtra(QiupuMessage.BUNDLE_STREAM_IN_FILE, ser);
        }

//        if (null != receivers) {
//            intent.putExtra("receivers", receivers);
//        }

        if (!TextUtils.isEmpty(comment)) {
            intent.putExtra(QiupuComposeActivity.EXTERNAL_COMMENT, comment);
        }
        context.startActivity(intent);
    }

    public static void startPeopleSearchIntent(Context context) {
        startPeopleSearchIntent(context, null);
    }

    public static void startPeopleSearchIntent(Context context, String keyword) {
    	startSearchActivity(context, keyword, BpcSearchActivity.SEARCH_TYPE_PEOPLE);
    }
    
    public static void startSearchActivity(Context context, String keyword, int searchType) {
    	startSearchActivity(context, keyword, searchType, -1);
    }
    
    public static void startSearchActivity(Context context, String keyword, int searchType, long groupid) {
    	Log.d(TAG, "startSearchActivity " + context);
        Intent intent = new Intent(context, BpcSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_SEARCH);
        if(searchType > 0) {
        	intent.putExtra(BpcSearchActivity.SEARCH_TYPE_STRING, searchType);
        }
        if(TextUtils.isEmpty(keyword) == false) {
            intent.putExtra(SearchManager.QUERY, keyword);
        }
        if(groupid > 0) {
        	intent.putExtra(BpcSearchActivity.GROUPID_STRING, groupid);
        }
        context.startActivity(intent);
    }

    public static void startStreamCommentIntent(Context context, Stream post) {
        if (null != post) {
            Intent intent = new Intent(context, QiupuCommentsActivity.class);
            boolean ser = QiupuHelper.serialization(post);
            intent.putExtra(QiupuMessage.BUNDLE_STREAM_IN_FILE, ser);
            intent.putExtra(BpcApiUtils.SEARCH_KEY_ID, post.post_id);
            intent.putExtra(QiupuMessage.BUNDLE_POST_IS_LIKE, post.iLike);
            context.startActivity(intent);
        }
    }
    
    public static void startStreamCommentIntentByStreamID(Context context, String sid) {
        if (null != sid) {
            Intent intent = new Intent(context, QiupuCommentsActivity.class);

            intent.putExtra(BpcApiUtils.SEARCH_KEY_ID, sid);
            context.startActivity(intent);
        }
    }
    public static void startStreamCommentPicIntent(Context context, Stream post) {
    	if (null != post) {
    		Intent intent = new Intent(context, QiupuCommentsPicActivity.class);
    		boolean ser = QiupuHelper.serialization(post);
    		intent.putExtra(QiupuMessage.BUNDLE_STREAM_IN_FILE, ser);
    		intent.putExtra(BpcApiUtils.SEARCH_KEY_ID, post.post_id);
    		intent.putExtra(QiupuMessage.BUNDLE_POST_IS_LIKE, post.iLike);
    		context.startActivity(intent);
    	}
    }
    
    public static void startPhotosViewIntent(Context context,long uid, int current_item,long album_id,String nick_name) {
    	Intent intent = new Intent(context,PhotosViewActivity.class);
    	intent.putExtra("uid", uid);
    	intent.putExtra("current_item", current_item);
    	intent.putExtra("album_id", album_id);
    	intent.putExtra("nick_name", nick_name);
    	context.startActivity(intent);
    }
    
    public static void startPhotosViewIntent(Context context,long album_id,long uid,int position,String album_name,ArrayList<QiupuPhoto> photoList,String user_name) {
    	Intent intent = new Intent(context,PhotosViewActivity.class);
    	intent.putExtra("fromStreamItem", true);
    	intent.putExtra("position", position);
    	intent.putExtra("photoList", photoList);
    	intent.putExtra("uid", uid);
    	intent.putExtra("album_id", album_id);
    	intent.putExtra("album_name", album_name);
    	intent.putExtra("user_name", user_name);
    	context.startActivity(intent);
    }
    
    public static void startGridPicIntent(Context context,long album_id,long uid,String nick_name,boolean fromStream) {
    	Intent intent = new Intent(context,GridPicActivity.class);
    	intent.putExtra("album_id",album_id);
    	intent.putExtra("uid", uid);
    	intent.putExtra("nick_name", nick_name);
    	intent.putExtra("fromStream", fromStream);
    	context.startActivity(intent);
    }
    
    public static void startAlbumIntent(Context context,long uid,String nick_name) {
        Intent intent = new Intent(context,AlbumActivity.class);
        intent.putExtra("uid",uid);
        intent.putExtra("nick_name",nick_name);
        context.startActivity(intent);
    }
    
    public static void startAlbumIntent(Context context,long uid,String nick_name,boolean isSupportLeftNavigation) {
		Intent intent = new Intent(context,AlbumActivity.class);
		intent.putExtra("uid",uid);
    	intent.putExtra("nick_name",nick_name);
    	intent.putExtra("supportLeftNavigation", isSupportLeftNavigation);
		context.startActivity(intent);
    }
    
    public static void startImComposeIntent(Context context, QiupuUser user) {
    	Intent intent = new Intent(context, IMComposeActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("to_url", QiupuORM.getInstance(context).getUserProfileImageUrl(AccountServiceUtils.getBorqsAccountID()));
        context.startActivity(intent);
    }

    public static void startAppCommentIntent(Context context, Stream post) {
        if (null != post && null != post.attachment) {
            List attachList = post.attachment.attachments;
            if (null != attachList && attachList.size() > 0) {
                ApkBasicInfo apkBasicInfo = attachList.get(0) instanceof ApkBasicInfo ?
                        (ApkBasicInfo)attachList.get(0) : null;
                if (null != apkBasicInfo) {
                    if (apkBasicInfo.comments_count < post.comments.getCount()) {
                        apkBasicInfo.comments = post.comments;
                    }
                    Intent intent = QiupuCommentsActivity.getAppIntent(context, apkBasicInfo);
                    context.startActivity(intent);
                }
            }
        }
    }

    private static boolean USE_FRAGMENT_ACTIVITY = true;
    public static void startCircleDetailIntent(Context context, UserCircle circle, boolean fromTab) {
        UserCircleDetailActivity.startCircleDetailIntent(context, circle, fromTab);
    }
    
    public static void startPublicCircleDetailIntent(Context context, UserCircle circle) {
//        UserPublicCircleDetailActivity.startPublicCircleDetailIntent(context, circle);
        if (null != context && null != circle) {
            final Intent intent = new Intent(context, UserPublicCircleDetailActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString(CircleUtils.CIRCLE_NAME,
                    CircleUtils.getLocalCircleName(context, circle.circleid, circle.name));
            bundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
            intent.putExtras(bundle);
            intent.putExtra(CircleUtils.CIRCLEINFO, circle);
            context.startActivity(intent);
        }
    }
    
    public static void startEventDetailIntent(Context context, UserCircle circle) {
      if (null != context && null != circle) {
          final Intent intent = new Intent(context, EventDetailActivity.class);

          Bundle bundle = new Bundle();
          bundle.putString(CircleUtils.CIRCLE_NAME,
                  CircleUtils.getLocalCircleName(context, circle.circleid, circle.name));
          bundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
          intent.putExtras(bundle);
          context.startActivity(intent);
      }
  }

    public static void startStreamListIntent(Context context, boolean fromHome) {
        Intent intent = getStreamListIntent(context, fromHome, false, false);
        //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    public static Intent getStreamListIntent(Context context, boolean fromHome, boolean fromTab, boolean isAppBox) {
        Intent intent= new Intent(context, BpcPostsNewActivity.class);
        intent.putExtra("from_home", fromHome);
        intent.putExtra("fromtab", fromTab);
        intent.putExtra("for_appbox", isAppBox);        
        return intent;
    }
    
    public static void startContactDetailIntent(Context context, long contactId) {
    	Intent intent = new Intent("android.intent.action.VIEW");
		Uri mUrl = Uri.parse(ContactsContract.Contacts.CONTENT_URI + "/" +  contactId);
		intent.setData(mUrl);
		context.startActivity(intent);
    }

    public static String getParameter(String paramString1, String paramString2) {
        String str1;
        if (!paramString2.endsWith("="))
            str1 = paramString2 + "=";
        else
            str1 = paramString2;
        int j = paramString1.indexOf(str1);
        String str2 = null;
        if (j == -1) {
            str1 = null;
        } else {
            j += str1.length();
            int i = paramString1.indexOf('&', j);
            if (i != -1)
                str2 = paramString1.substring(j, i);
            else
                str2 = paramString1.substring(j);
        }
        return str2;
    }

    public static boolean isProfileUrl(String paramString) {
        return paramString.startsWith("borqs://profile/details?");
//        return paramString.startsWith("#~loop:svt=person&");
    }

    
    public static void startCircleSelectIntent(Context context, long uid, String circleId) {
   	 Intent cirintent = new Intent(context, UserCircleSelectedActivity.class);
        cirintent.putExtra("uid", uid);
        if(null != circleId) {
        	cirintent.putExtra("circleid", circleId);
        }
        context.startActivity(cirintent);
   }
    
    public static void ShowUserList(Context context, String title, Stream.Likes likes, int type, String ObjectID) {
    	
    	Intent intent = new Intent(context, FriendsListActivity.class);
    	intent.putExtra("title",  title);
    	intent.putExtra("users",    likes);    	
    	intent.putExtra("type",     type);
    	intent.putExtra("objectid", ObjectID);    	
    	context.startActivity(intent);
    }

    public static void loadCircleAndUserFromServer(Context context) {
        QiupuService.loadUsersFromServer(context, true);
    }

    public static void loadUsersFromServer(Context context) {
        QiupuService.loadUsersFromServer(context, false);
    }
    public static void loadCircleFromServer(Context context) {
        QiupuService.loadUsersFromServer(context, false, true);
    }
    
    public static void loadEventsFromServer(Context context) { 
    	QiupuService.loadEventFromServer(context);
    }
    
	public static void showMyConcerningPeople(Context context) {
		Intent intent = new Intent(context, FriendsFragmentActivity.class);
		Bundle bundle = BpcApiUtils.getUserBundle(Long.valueOf(AccountServiceUtils.getBorqsAccountID()));
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	public static void startFriendActivity(Context context, int type, long uid){
		Intent intent = new Intent(context, BpcFriendsActivity.class);
		Bundle bundle = BpcApiUtils.getUserBundle(uid);
		bundle.putInt(BpcFriendsActivity.USER_CONCERN_TYPE, type);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	public static void startCircleActivity(Context context, long uid){
		Intent intent = new Intent(context, CircleFragmentActivity.class);
		Bundle bundle = BpcApiUtils.getUserBundle(uid);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

    public static void showUserFansList(Context context, long uid) {
        UsersArrayListActivity.showUserArrayList(context, uid);
    }

    public static void showExchangedUserList(Context context) {
        UsersCursorListActivity.showUserCursorList(context);
    }

    public static void startBeamDataByNFCActivity(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, BeamDataByNFCActivity.class);
        context.startActivity(intent);
    }

    public static void startRequestActivity(Context context, int exchangeRequestCount) {
        Intent intent = new Intent();
        intent.setClass(context, RequestActivity.class);
        intent.putExtra("request_count", exchangeRequestCount);
        context.startActivity(intent);
    }

    public static void startExchangeVCardActivity(Context context, int requestCount) {
        Intent intent = new Intent(context, BpcExchangeCardActivity.class);
        intent.putExtra("request_count", requestCount);
//        intent.putExtra("request_count", count);
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    public static void startFriendsCircleActivity(Context context, long accountId, String nickName) {
    	Intent intent = new Intent(context, BpcFriendsFragmentActivity.class);
    	Bundle bundle = BpcApiUtils.getUserBundle(AccountServiceUtils.getBorqsAccountID());
    	bundle.putString(BasicActivity.USER_NICKNAME, nickName);
    	bundle.putInt(BasicActivity.USER_CONCERN_TYPE, BpcFriendsFragmentActivity.current_type_circle);
    	intent.putExtras(bundle);
    	context.startActivity(intent);
//        }
    }
    public static void startFriendsFragmentActivity(Context context, long accountId, String nickName) {
            Intent intent = new Intent(context,FriendsFragmentActivity.class);
            Bundle bundle = BpcApiUtils.getUserBundle(AccountServiceUtils.getBorqsAccountID());
            bundle.putString(BasicActivity.USER_NICKNAME, nickName);
            intent.putExtras(bundle);
            context.startActivity(intent);
//        }
    }

    public static void startBpcFriendsActivity(Context context) {
        Intent intent = new Intent(context, BpcFindFriendsFragmentActivity.DISABLED ? BpcAddFriendsActivity.class : BpcFindFriendsFragmentActivity.class);
        context.startActivity(intent);
    }

    public static void startProfileActivity(Context context, long accountId, String nickName) {
        Intent intent = IntentUtil.buildUserDetailIntent(context, accountId, nickName);
        if(intent != null ) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("supportLeftNavigation", true);
            context.startActivity(intent);
        }
    }

    public static void startStream(Context context) {
        Intent intent= new Intent(context, BpcPostsNewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("from_home", false);
        intent.putExtra("fromtab", false);
        intent.putExtra("for_appbox", false);
        context.startActivity(intent);
    }

    public static Intent getAppPickerIntent(Context context) {
        Intent intent = new Intent(ACTION_PICK_APPS);
        intent.putExtra("KEY_INVOKER_APP_SELECT", true);
        intent.addCategory(Intent.CATEGORY_DEFAULT); 
        return intent;
    }

    public static Intent getVCardPickerIntent(Context context) {
        Intent intent = PickVCardActivity.getStartupIntent(context);
        return intent;
    }
    
    public static void gotoEditPublicCircleActivity(final Context context, final String circleName, final UserCircle circle, final int type) {
    	Intent intent = new Intent(context, EditPublicCircleActivity.class);
    	intent.putExtra(CircleUtils.CIRCLE_NAME, circleName);
    	intent.putExtra(CircleUtils.EdIT_TYPE, type);
    	intent.putExtra(CircleUtils.CIRCLEINFO, circle);
    	context.startActivity(intent);
    }
    
    public static void gotoCreateEventActivity(final Context context, final int type, final HashMap<String, String> receiverMap, final String mReceivers) {
    	final String homeid = QiupuORM.getSettingValue(context, QiupuORM.HOME_ACTIVITY_ID);
    	long homeScene = TextUtils.isEmpty(homeid) ? -1 : Long.parseLong(homeid);
    	gotoCreateEventActivity(context, type, receiverMap, mReceivers, homeScene, homeScene);
    }
    
    public static void gotoCreateEventActivity(final Context context, final int type, final HashMap<String, String> receiverMap, final String mReceivers, final long parent_id, final long sceneid) {
    	Intent intent = new Intent(context, EditPublicCircleActivity.class);
    	intent.putExtra(CircleUtils.EdIT_TYPE, type);
    	if(mReceivers != null) {
    		intent.putExtra("receiver", mReceivers);
    	}
    	if(receiverMap != null) {
    		intent.putExtra("receivermap", receiverMap);
    	}
    	
    	if(parent_id > 0 ) {
    		intent.putExtra(CircleUtils.INTENT_PARENT_ID, parent_id);
    	}
    	
    	intent.putExtra(CircleUtils.INTENT_SCENE, sceneid);
    	context.startActivity(intent);
    	
    }
    
	public static void gotoEditPublicCircleActivity(final Context context, final String company_id, final int type) {
    	Intent intent = new Intent(context, EditPublicCircleActivity.class);
    	intent.putExtra(CircleUtils.EdIT_TYPE, type);
    	intent.putExtra(CircleUtils.COMPANY_ID, company_id);
    	context.startActivity(intent);
    }
	
    public static void startPublicCirclePeopleActivity(final Context context, final UserCircle circle, final int status, final String title) {
    	Intent intent = new Intent(context, PublicCirclePeopleActivity.class);
    	intent.putExtra(PublicCirclePeopleActivity.CIRCLEINFO, circle);
    	intent.putExtra(PublicCirclePeopleActivity.STATUS, status);
    	intent.putExtra("title", title);
    	context.startActivity(intent);
    }

    public static void showShakingActivity(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, BpcShakeExchangeCardActivity.class);
        context.startActivity(intent);
    }

    public static void showExchangeCardFriendsActivity(Context context, String filter_title) {
        Intent intent = new Intent();
        intent.setClass(context, ExchangedCardFriendsActivity.class);
        intent.putExtra("exchange_vcard_title", filter_title);
        context.startActivity(intent);
    }

    public static void showExchangeVcardListActivity(Context context) {
        Intent intent = new Intent(context, PickCircleUserActivity.class);
        intent.putExtra(PickCircleUserActivity.RECEIVER_TYPE, PickCircleUserActivity.type_add_friends);
        intent.putExtra("circleid", String.valueOf(QiupuConfig.ADDRESS_BOOK_CIRCLE));
        intent.putExtra("from_exchange", "from_exchange");
        context.startActivity(intent);
    }

    public static void startPickUserActivity(Context context, String uids) {
        Intent intent = new Intent(context, PickUserFragmentActivity.class);
        intent.putExtra("uids", uids);
        intent.putExtra("from_exchange", "from_exchange");
        context.startActivity(intent);
    }

    public static Intent getApkDetailIntent(final Context context, final ApkBasicInfo apk) {
//        Intent intent = new Intent(context, ApkDetailInfoActivity.class);
//        intent.putExtra(QiupuMessage.BUNDLE_APKINFO, apk);
        final String schemeTxt = BpcApiUtils.APP_DETAIL_SCHEME_PATH + "?" +
                BpcApiUtils.SEARCH_KEY_UID + "=" + AccountServiceUtils.getBorqsAccountID() +
                "&" +
                BpcApiUtils.SEARCH_KEY_ID + "=" + apk.apk_server_id;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schemeTxt));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    public static Intent getAppBoxIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(getAppBoxComponentName());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }

    public static void startApkDetailActivity(final Context context, final ApkBasicInfo apk) {
//        Intent intent = getApkDetailIntent(context, apk);
        Intent intent = getAppBoxIntent(context);
        if (existComponent(context, intent)) {
            try {
                Intent apkIntent = new Intent();
                apkIntent.setClassName(APPBOX_PKG_NAME, "com.borqs.appbox.ui.ApkDetailInfoActivity");
                apkIntent.putExtra(QiupuMessage.BUNDLE_APKINFO, apk);
                apkIntent.putExtra("RESULT", true);
                context.startActivity(apkIntent);
            } catch(ActivityNotFoundException e) {
                Log.d(TAG, "ActivityNotFoundException, not found ApkDetailInfoActivity.");
            }
        } else {
            final Dialog dialog = new Dialog(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View view = inflater.inflate(R.layout.app_share_install, null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(view);
//            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle(R.string.home_application);

            final View appShare = dialog.findViewById(R.id.app_share_download);
            if (null != appShare) {
                appShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shootAppBoxDownload(context);
                        dialog.cancel();
                    }
                });
            }

            final TextView appInfo = (TextView) dialog.findViewById(R.id.app_package_summary);
            if (null != appInfo) {
                ImageRun.shootImageRunner(apk.iconurl, QiupuConfig.DEFAULT_IMAGE_INDEX_APK, 64, appInfo, false, true);
                StringBuffer appSummary = new StringBuffer();
                appSummary.append(apk.label).append("<br>");
                appSummary.append("NO.").append(apk.versioncode);
                appSummary.append("  V").append(apk.versionname);
                appSummary.append("<br>");
                appSummary.append(FileUtils.formatPackageFileSizeString(context, apk.apksize));

                String htmlStr = MyHtml.toDumbClickableText(appSummary.toString());
                appInfo.setText(MyHtml.fromHtml(htmlStr));
            }

            final boolean wasInstalled = isAppInstalled(context, apk.packagename);
            final TextView openItem = (TextView) dialog.findViewById(R.id.app_package_open);
            if (null != openItem) {
                openItem.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         openApp(context, apk.packagename);
                         dialog.cancel();
                     }
                 });
                openItem.setVisibility(wasInstalled ? View.VISIBLE : View.GONE);
            }

            final TextView installItem = (TextView) dialog.findViewById(R.id.app_package_install);
            if (null != installItem) {
                installItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shootAppDownload(context, apk.packagename, apk.label);
                        dialog.cancel();
                    }
                });
                installItem.setVisibility(wasInstalled ? View.GONE : View.VISIBLE);
            }

            dialog.show();
        }
    }

    public static void startApkDetailActivity(final Context context, final int initStatus, final ApkResponse apk) {
        apk.status = initStatus;
        startApkDetailActivity(context, apk);
    }

    public static void lunchMyContact(Context context) {
        Intent intent = new Intent(context, PickPeopleActivity.class);
        context.startActivity(intent);
    }

    // todo : detest and download app box.
    public static void startApplicationBoxActivity(Context context) {
//        Intent intent = new Intent(context, TabStyleMainActivity.class);
//        context.startActivity(intent);
        startComponent(context, getAppBoxComponentName());
    }

    public static void startApplicationBoxActivity(Context context, String scheme, long uid, String name) {
        //	        Intent intent = new Intent(mActivity, AppFavoritesActivity.class);
        //	        intent.putExtra(AppFavoritesActivity.USERID_STRING, mUser.uid);
        //	        intent.putExtra(AppFavoritesActivity.USERNAME_STRING, mUser.nick_name);
        //	        startActivity(intent);
        final String schemeTxt = scheme + "?" +
                BpcApiUtils.SEARCH_KEY_UID + "=" + uid +
                "&" + BpcApiUtils.User.USER_NAME + "=" + name;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schemeTxt));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (!startActivityIntent(context, intent)) {
            Log.d(TAG, "startApplicationBoxActivity, no activity for intent: " + intent);
            shootAppBoxDownload(context);
        }
    }

    public static void startApplicationBoxActivity(Context context, String scheme, long uid, String name, String circleName) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BpcApiUtils.APP_LIST_SCHEME));
        if (BpcApiUtils.isActivityReadyForIntent(context, intent)) {
            Bundle bundle = new Bundle();
            bundle.putLong(BpcApiUtils.User.USER_ID, uid);
            bundle.putString(BpcApiUtils.User.USER_NAME, name);
            bundle.putString(BpcApiUtils.User.USER_CIRCLE, circleName);
            intent.putExtras(bundle);

            context.startActivity(intent);
        } else {
            Log.d(TAG, "startApplicationBoxActivity, no activity for intent: " + intent);
            shootAppBoxDownload(context);
        }
    }

    public final static String APPBOX_SPLASH_NAME = "com.borqs.appbox.ui.SplashActivity";
    public final static String APPBOX_PKG_NAME = "com.borqs.appbox";
    public static boolean isAppBoxActivity(String name) {
        return APPBOX_SPLASH_NAME.equals(name);
    }
    public static ComponentName getAppBoxComponentName() {
        return new ComponentName(APPBOX_PKG_NAME, APPBOX_SPLASH_NAME);
    }
    public static void startComponent(Context context, ComponentName component) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(component);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (APPBOX_PKG_NAME.equals(component.getPackageName())) {
            if (ensureAppShareVersion(context)) {
                if (!startActivityIntent(context, intent)) {
                    shootAppBoxDownload(context);
                }
            } 
        } else {
            context.startActivity(intent);
        }
    }

    private static boolean existComponent(Context context, Intent intent) {
        final boolean ret;
        if (BpcApiUtils.isActivityReadyForIntent(context, intent)) {
            ret = true;
        } else {
            ret = false;
            Log.w(TAG, "startActivityIntent, no activity found for intent: " + intent);
        }
        return ret;
    }

    private static boolean startActivityIntent(Context context, Intent intent) {
        final boolean ret;
        if (BpcApiUtils.isActivityReadyForIntent(context, intent)) {
            context.startActivity(intent);
            ret = true;
        } else {
            ret = false;
            Log.w(TAG, "startActivityIntent, no activity found for intent: " + intent);
        }
        return ret;
    }


    public static boolean ensureAppShareVersion(Context context) {
        boolean result = true;
        final int minVersion = 21;
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(IntentUtil.APPBOX_PKG_NAME, 0);
            if (info.versionCode < minVersion) {
                result = false;
                forceAppBoxUpgrade(context);
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return result;
    }

    private static void forceAppBoxUpgrade(final Context context) {
        final String pkgLabel = context.getString(R.string.home_application);

        final DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ApkFileManager.shootReplaceDownloadAppService(context, APPBOX_PKG_NAME, pkgLabel);
            }
        };

//        AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
//        replaceBuilder.setTitle(R.string.apk_download)
//                .setMessage(R.string.app_share_force_upgrade)
//                .setPositiveButton(R.string.label_ok, positiveClick)
//                .setCancelable(false);
//        AlertDialog dialog = replaceBuilder.create();
//        dialog.show();
//
        DialogUtils.showConfirmDialog(context,
                context.getString(R.string.apk_download),
                context.getString(R.string.app_share_force_upgrade, pkgLabel),
                positiveClick
        );
//        ApkFileManager.shootReplaceDownloadAppService(context, APPBOX_PKG_NAME, pkgLabel);
    }

    private static void shootAppBoxDownload(final Context context) {
        shootAppDownload(context, APPBOX_PKG_NAME, context.getString(R.string.home_application));
    }

    private static void shootAppDownload(final Context context, final String pkgName,
                                         final String pkgLabel) {
        final DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ApkFileManager.shootDownloadAppService(context, pkgName, pkgLabel);
            }
        };

        if (ApkFileManager.existDownloadAPK(context, pkgName)) {
            final String downloadTitle = context.getString(R.string.install_app);
            final String downloadMsg = context.getString(R.string.apk_install_download_msg, pkgLabel);
            final DialogInterface.OnClickListener neutralClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ApkFileManager.shootReplaceDownloadAppService(context, pkgName, pkgLabel);
                }
            };
            AlertDialog.Builder replaceBuilder = new AlertDialog.Builder(context);
            replaceBuilder.setTitle(downloadTitle)
                    .setMessage(downloadMsg)
                    .setPositiveButton(R.string.install_app, positiveClick)
                    .setNeutralButton(R.string.apk_download, neutralClick)
                    .setNegativeButton(R.string.label_cancel, null);
            AlertDialog dialog = replaceBuilder.create();

            dialog.show();
        } else {
            DialogUtils.showConfirmDialog(context,
                    context.getString(R.string.apk_download),
                    context.getString(R.string.apk_download_msg, pkgLabel),
                    positiveClick
            );
        }


    }

    public static void startShareSourceActivity(Context context, long uid, long circleId, int type, String sourceItemLabel) {
        Intent intent = new Intent(context, ShareResourcesActivity.class);
        intent.putExtra("userid", uid);
        intent.putExtra(CircleUtils.CIRCLE_ID, circleId);
        intent.putExtra("sourcefilter", type);
        intent.putExtra("title", sourceItemLabel);
        context.startActivity(intent);
    }

    public static void startPollDetailActivity(Activity context, PollInfo pollInfo,int req_code) {
    	Intent intent = new Intent();
    	intent.setClass(context, PollDetailActivity.class);
    	intent.putExtra(PollDetailActivity.POLL_ID_KEY, pollInfo);
    	context.startActivityForResult(intent,req_code);
    }
    
    public static void startCompanyDetailActivity(Activity context,Company company) {
        Intent intent = new Intent();
        intent.setClass(context, CompanyDetailActivity.class);
        intent.putExtra(Company.COMPANY_INFO, company);
        context.startActivity(intent);
    }

    public static void startProfileFromPoll(Context context, long accountId, String nickName) {
        Intent intent = IntentUtil.buildUserDetailIntent(context, accountId, nickName);
        if(intent != null ) {
//            intent.putExtra("supportLeftNavigation", true);
            context.startActivity(intent);
        }
    }
    
    public static void startLinkTraActivity (Context context, String url) {
    	Intent intent = new Intent(context, LinksTraActivity.class);
    	intent.putExtra(LinksTraActivity.LINK_URL, url);
    	context.startActivity(intent);
    }


    public static void gotoPickAudienceActivity(Context context, UserCircle circle, int pickType) {
        Intent intent = new Intent(context, PickAudienceActivity.class);
        StringBuffer strbuf = new StringBuffer();
        
        if (circle.mGroup != null) {
        	if(!TextUtils.isEmpty(circle.mGroup.invited_ids)) {
        		strbuf.append(circle.mGroup.invited_ids);
        	}
            if(circle.mGroup.parent_id > 0) {
            	intent.putExtra(CircleUtils.INTENT_SCENE, circle.mGroup.parent_id);
            }else {
            	// is top circle , invite the pending people
//            	intent.putExtra(CircleUtils.INTENT_SCENE, circle.circleid);
            }
        }
        if(strbuf.length() > 0) {
        	strbuf.append(",");
        	strbuf.append(circle.circleid);
        }
        intent.putExtra(PickFriendsFragment.FILTER_IDS, strbuf.toString());
        intent.putExtra(PickAudienceActivity.PICK_FROM, pickType);
        intent.putExtra(CircleUtils.CIRCLE_ID, circle.circleid);
        intent.putExtra(CircleUtils.CIRCLE_NAME, circle.name);
        intent.putExtra(CircleUtils.INTENT_FROM_ID, circle.circleid);
        context.startActivity(intent);
    }
 
    public static void startPageListActivity(Context context) {
    	Intent intent = new Intent(context, PageListActivity.class);
    	context.startActivity(intent);
    }
    
    public static void startCreatePageActivity(Context context) {
    	Intent intent = new Intent(context, CreatePageActivity.class);
    	context.startActivity(intent);
    }
    
    public static void startPageDetailActivity(Context context, long pageid) {
    	final Intent intent = new Intent(context, PageDetailActivity.class);

        Bundle bundle = new Bundle();
        bundle.putLong("pageid", pageid);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
    
    public static void startCreatePollActivity(Context context, HashMap<String, String> receiverMap, String receiverid, final long parent_id, final long sceneId) {
    	Intent intent = new Intent(context, PollCreateActivity.class);
        if(!TextUtils.isEmpty(receiverid)) {
        	intent.putExtra("receivers", receiverid);
        }
        if(receiverMap != null) {
        	intent.putExtra("receiversMap", receiverMap);
        }
        if(parent_id > 0) {
        	intent.putExtra(CircleUtils.INTENT_PARENT_ID, parent_id);
        }
        intent.putExtra(CircleUtils.INTENT_SCENE, sceneId);
        context.startActivity(intent);
    }
    
    public static void startCircleEventList(Context context, long circleid, String circleName) {
    	Intent intent = new Intent(context, CircleEventsActivity.class);
		intent.putExtra(CircleUtils.CIRCLE_ID, circleid);
		intent.putExtra(CircleUtils.CIRCLE_NAME, circleName);
		context.startActivity(intent);
    }


    public static void gotoOrganisationHome(Context context, String cName, long cId) {
        if (null != context) {
            final Intent intent = new Intent(context, OrganizationHomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(CircleUtils.CIRCLE_NAME, cName);
            bundle.putLong(CircleUtils.CIRCLE_ID, cId);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

    public static void gotoHomePickerActivity(Context context) {
        Intent intent = new Intent(context, HomePickerActivity.class);
        context.startActivity(intent);
    }

    public static void loadCircleDirectoryFromServer(Context context, long circleId) {
        QiupuService.loadCircleDirectoryFromServer(context, circleId);
    }

    public static boolean isAppInstalled(Context context, String pkgName) {
        boolean existing = false;
        try {
            context.getPackageManager().getPackageInfo(pkgName, 0);
            existing = true;
        } catch (PackageManager.NameNotFoundException e) {
        } finally {
            return existing;
        }
    }

    public static void openApp(Context context, String pkgName) {
        final PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(pkgName);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mainIntent, 0);

        if (activities != null && !activities.isEmpty()) {
            ResolveInfo ri = activities.get(0);
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName(
                    ri.activityInfo.applicationInfo.packageName,
                    ri.activityInfo.name));
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);// not start a new activity
            try {
                context.startActivity(i);
            } catch (Exception e) {
                Toast.makeText(context, e.toString(), 3500).show();
            }
        }
    }
}
