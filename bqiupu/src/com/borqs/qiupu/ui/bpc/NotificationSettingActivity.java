package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import com.borqs.qiupu.ui.BasicActivity;
import twitter4j.AsyncQiupu;
import twitter4j.NotificationInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;

public class NotificationSettingActivity extends BasicActivity.StatPreferenceActivity implements Preference.OnPreferenceChangeListener
{
	private static final String TAG = "Qiupu.NotificationSettingActivity";
	
	private AsyncQiupu asyncQiupu;
	private QiupuORM orm;
	private HandlerLoad handler;
//	private HashMap<String, String> ntfMap = new HashMap<String, String>();
	private HashMap<String, CheckBoxPreference> preferenceMap = new HashMap<String, CheckBoxPreference>();
	private ArrayList<NotificationInfo> ntfList = new ArrayList<NotificationInfo>();
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.notificataion_set_preference);

	    orm = QiupuORM.getInstance(this);

        final boolean isEnableNotification = orm.isEnableGetNotification();
        Preference key_notification_enable = findPreference("key_notification_enable");
        if (null != key_notification_enable) {
            key_notification_enable.setOnPreferenceChangeListener(this);
            ((CheckBoxPreference) key_notification_enable).setChecked(isEnableNotification);
        }

        handler = new HandlerLoad();
	    asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
	    
	    CheckBoxPreference acceptSuggestPage = (CheckBoxPreference) findPreference("key_accept_suggest");
	    acceptSuggestPage.setOnPreferenceChangeListener(this);
        acceptSuggestPage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_ACCEPT_SUGGEST));
        preferenceMap.put(NotificationInfo.NTF_ACCEPT_SUGGEST, acceptSuggestPage);
        
        CheckBoxPreference myAppCommentPage = (CheckBoxPreference) findPreference("key_my_app_comment");
        myAppCommentPage.setOnPreferenceChangeListener(this);
        myAppCommentPage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_MY_APP_COMMENT));
        preferenceMap.put(NotificationInfo.NTF_MY_APP_COMMENT, myAppCommentPage);
        
        CheckBoxPreference myAppLikePage = (CheckBoxPreference) findPreference("key_my_app_like");
        myAppLikePage.setOnPreferenceChangeListener(this);
        myAppLikePage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_MY_APP_LIKE));
        preferenceMap.put(NotificationInfo.NTF_MY_APP_LIKE, myAppLikePage);
        
        CheckBoxPreference newFollowerPage = (CheckBoxPreference) findPreference("key_new_follower");
        newFollowerPage.setOnPreferenceChangeListener(this);
        newFollowerPage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_NEW_FOLLOWER));
        preferenceMap.put(NotificationInfo.NTF_NEW_FOLLOWER, newFollowerPage);
        
        CheckBoxPreference profileUpdatePage = (CheckBoxPreference) findPreference("key_profile_update");
        profileUpdatePage.setOnPreferenceChangeListener(this);
        profileUpdatePage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_PROFILE_UPDATE));
        preferenceMap.put(NotificationInfo.NTF_PROFILE_UPDATE, profileUpdatePage);
        
        CheckBoxPreference appSharePage = (CheckBoxPreference) findPreference("key_app_share");
        appSharePage.setOnPreferenceChangeListener(this);
        appSharePage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_APP_SHARE));
        preferenceMap.put(NotificationInfo.NTF_APP_SHARE, appSharePage);
        
        CheckBoxPreference otherSharePage = (CheckBoxPreference) findPreference("key_other_share");
        otherSharePage.setOnPreferenceChangeListener(this);
        otherSharePage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_OTHER_SHARE));
        preferenceMap.put(NotificationInfo.NTF_OTHER_SHARE, otherSharePage);
        
        CheckBoxPreference suggestUserPage = (CheckBoxPreference) findPreference("key_suggest_user_to_you");
        suggestUserPage.setOnPreferenceChangeListener(this);
        suggestUserPage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_SUGGEST_USER));
        preferenceMap.put(NotificationInfo.NTF_SUGGEST_USER, suggestUserPage);
        
        CheckBoxPreference streamCommentPage = (CheckBoxPreference) findPreference("key_my_stream_comment");
        streamCommentPage.setOnPreferenceChangeListener(this);
        streamCommentPage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_MY_STREAM_COMMENT));
        preferenceMap.put(NotificationInfo.NTF_MY_STREAM_COMMENT, streamCommentPage);
        
        CheckBoxPreference streamLikePage = (CheckBoxPreference) findPreference("key_my_stream_like");
        streamLikePage.setOnPreferenceChangeListener(this);
        streamLikePage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_MY_STREAM_LIKE));
        preferenceMap.put(NotificationInfo.NTF_MY_STREAM_LIKE, streamLikePage);
        
        CheckBoxPreference streamRetweetPage = (CheckBoxPreference) findPreference("key_my_stream_retweet");
        streamRetweetPage.setOnPreferenceChangeListener(this);
        streamRetweetPage.setChecked(orm.getNotificationValue(NotificationInfo.NTF_MY_STREAM_RETWEET));
        preferenceMap.put(NotificationInfo.NTF_MY_STREAM_RETWEET, streamRetweetPage);
        
        handler.obtainMessage(NOTIFICATION_GET).sendToTarget();
	}

	
	private final static int NOTIFICATION_SET_END = 1;
	private final static int NOTIFICATION_GET     = 2;
	private final static int NOTIFICATION_GET_END = 3;
	private class HandlerLoad extends Handler 
 	{
 		public HandlerLoad()
 		{
 			super();
 			
 			Log.d(TAG, "new HandlerLoad");
 		}
 		
 		@Override
 		public void handleMessage(Message msg)  
 		{
 			switch(msg.what)
 			{
 			   case NOTIFICATION_SET_END:
 			   {
 				   if(!msg.getData().getBoolean("result"))
 				   {
 					   Toast.makeText(NotificationSettingActivity.this, R.string.notification_set_failed, Toast.LENGTH_LONG).show();
 				   }
	    	      break;
 			   }
 			   case NOTIFICATION_GET:
 			   {
 				  getNotificationValue(NotificationInfo.NTF_START_NTF);
 				   break;
 			   }
 			   case NOTIFICATION_GET_END:
 			   {
 				  reSetPreferenceValue();
 				   break;
 			   }
 			}
 		}
 	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object value)
	{
		
		boolean show = (Boolean)value;
		((CheckBoxPreference)(pref)).setChecked(show);
		String key = "" ;
		if (pref.getKey().equals("key_accept_suggest"))
	    {
			key = NotificationInfo.NTF_ACCEPT_SUGGEST;
	    }
    	else if(pref.getKey().equals("key_my_app_comment"))
    	{
    		key = NotificationInfo.NTF_MY_APP_COMMENT;
    	}
    	else if(pref.getKey().equals("key_my_app_like"))
    	{
    		key = NotificationInfo.NTF_MY_APP_LIKE;
    	}
    	else if(pref.getKey().equals("key_new_follower"))
    	{
    		key = NotificationInfo.NTF_NEW_FOLLOWER;
    	}
    	else if(pref.getKey().equals("key_profile_update"))
    	{
    		key = NotificationInfo.NTF_PROFILE_UPDATE;
    	}
    	else if(pref.getKey().equals("key_app_share"))
    	{
    		key = NotificationInfo.NTF_APP_SHARE;
    	}
    	else if(pref.getKey().equals("key_other_share"))
    	{
    		key = NotificationInfo.NTF_OTHER_SHARE;
    	}
    	else if(pref.getKey().equals("key_suggest_user_to_you"))
    	{
    		key = NotificationInfo.NTF_SUGGEST_USER;
    	}
    	else if(pref.getKey().equals("key_my_stream_comment"))
    	{
    		key = NotificationInfo.NTF_MY_STREAM_COMMENT;
    	}
    	else if(pref.getKey().equals("key_my_stream_like"))
    	{
    		key = NotificationInfo.NTF_MY_STREAM_LIKE;
    	}
    	else if(pref.getKey().equals("key_my_stream_retweet"))
    	{
    		key = NotificationInfo.NTF_MY_STREAM_RETWEET;
    	}
//		ntfMap.clear();
//		ntfMap.put(key, show ? "0" : "1");
		
//		setNotification(ntfMap);
		setNotification(key, show);
		return false;
	}
	
	public void setNotification(final String key, final boolean value) {

		asyncQiupu.setNotification(AccountServiceUtils.getSessionID(), key, value, new TwitterAdapter() {
			public void setNotification(HashMap<String, String> resultmap) {
				Log.d(TAG, "finish setNotification=");
				boolean result = false;
				if(resultmap != null)
				{
					result = true;
					Iterator iter = resultmap.entrySet().iterator();
					while (iter.hasNext()) {
						Map.Entry entry = (Map.Entry) iter.next();
						String resultkey = (String) entry.getKey();
						String resultval = (String) entry.getValue();
						orm.addNtfSetting(resultkey, resultval);
					}
				}
				Message msg = handler.obtainMessage(NOTIFICATION_SET_END);
				msg.getData().putBoolean("result", result);
				msg.sendToTarget();
			}

			public void onException(TwitterException ex,TwitterMethod method) {
				Message msg = handler.obtainMessage(NOTIFICATION_SET_END);
				msg.getData().putBoolean("result", false);
				msg.sendToTarget();
			}
		});
	}
	
	boolean ingetNotificationValue;
	Object mLockgetNotification = new Object();
	private void getNotificationValue(final String key) {
		if (ingetNotificationValue == true) {
			Toast.makeText(this, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
			return;
		}
		
		synchronized (mLockgetNotification) {
			ingetNotificationValue = true;
		}
		
		asyncQiupu.getNotificationValue(AccountServiceUtils.getSessionID(), key, new TwitterAdapter() {
			public void getNotificationValue(ArrayList<NotificationInfo> result) {
				Log.d(TAG, "finish getNotificationValue=" + result);
				if(result.size() > 0)
				{
					orm.insertNotificationList(result);
					ntfList.clear();
					ntfList.addAll(result);
				}
				
				Message msg = handler.obtainMessage(NOTIFICATION_GET_END);
				msg.getData().putBoolean("result", true);
				msg.sendToTarget();
				synchronized (mLockgetNotification) {
					ingetNotificationValue = false;
				}
			}
			
			public void onException(TwitterException ex,TwitterMethod method) {
				synchronized (mLockgetNotification) {
					ingetNotificationValue = false;
				}
				Message msg = handler.obtainMessage(NOTIFICATION_GET_END);
				msg.getData().putBoolean("result", false);
				msg.sendToTarget();
			}
		});
	}
	
	private void reSetPreferenceValue()
	{
		for(int i=0; i<ntfList.size(); i++)
		{
			NotificationInfo info = ntfList.get(i);
			
			CheckBoxPreference pre = preferenceMap.get(info.ntftype);
			
			if(pre != null)
			{
				pre.setChecked(Long.parseLong(info.ntfvalue) == 0 ? true : false);
			}
		}
	}

    public static void startActivity(Context context, boolean withSwitcher) {
        Intent intent = new Intent(context, NotificationSettingActivity.class);
        context.startActivity(intent);
    }
}
