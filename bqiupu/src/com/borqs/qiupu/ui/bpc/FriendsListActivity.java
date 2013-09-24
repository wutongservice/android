package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.StaticLayout;
import twitter4j.*;
import twitter4j.ApkBasicInfo.Likes;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.DialogUsersAdapter;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.UserSelectItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.StringUtil;

public class FriendsListActivity extends BasicActivity{

	private final String TAG="FriendsListActivity";
	
	private Stream.Likes          likes;
	private ApkResponse.Likes     apkLikes;
	private long[]                ids;
	private int                   contentType;
	private String                objectID;
	private ArrayList<QiupuSimpleUser> mUsersList = new ArrayList<QiupuSimpleUser>();
	
	private int page  = 0;
	private int count = 100;
	private DialogUsersAdapter useradapter ;
	private View mProgressView; 
	private static final String KEY_EXTRA_TITLE = "title";
    private static final String KEY_EXTRA_TYPE = "type";
    private static final String KEY_EXTRA_OBJECTID = "objectid";
    private static final String KEY_EXTRA_APKLIKES = "apklikes";
    private static final String KEY_EXTRA_POSTLIKES = "users";
    private static final String KEY_EXTRA_IDS = "ids";
    private static final String KEY_EXTRA_USERSLIST = "userslist";
    
    private final static int INSTLLUSER_TYPE = 1987;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
//		setContentView(R.layout.dialog_user_list);
		
		likes  = (Stream.Likes)getIntent().getSerializableExtra(KEY_EXTRA_POSTLIKES);
		apkLikes = (ApkResponse.Likes) getIntent().getSerializableExtra(KEY_EXTRA_APKLIKES);
		// request all userslist
		ArrayList<QiupuSimpleUser> requestUsersList = (ArrayList<QiupuSimpleUser>)getIntent().getSerializableExtra(KEY_EXTRA_USERSLIST);
		
		ids    = getIntent().getLongArrayExtra(KEY_EXTRA_IDS);
		contentType = getIntent().getIntExtra(KEY_EXTRA_TYPE, -1);
		objectID    = getIntent().getStringExtra(KEY_EXTRA_OBJECTID); 
		String title = getIntent().getStringExtra(KEY_EXTRA_TITLE);
    	
		View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_user_list, null);
        ListView listview = (ListView) contentView.findViewById(R.id.user_list);   
        listview.setDivider(null);
        if(requestUsersList != null) {
        	useradapter = new DialogUsersAdapter(this, requestUsersList);
        }else if(likes != null){
        	useradapter = new DialogUsersAdapter(this, likes.friends);
        }else if(apkLikes != null) {
        	useradapter = new DialogUsersAdapter(this, apkLikes.friends);
        }else if(ids != null) {
        	useradapter = new DialogUsersAdapter(this);
        }else if(contentType == INSTLLUSER_TYPE) {
            useradapter = new DialogUsersAdapter(this);   
        }else {
        	Log.d(TAG, "request param is null do nothing");
        }
        
        listview.setAdapter(useradapter);
        useradapter.notifyDataSetChanged();
        
        mProgressView = contentView.findViewById(R.id.center_process);

        listview.setOnItemClickListener(UserSelectItemView.userClickListener);
        
        AlertDialog dialog = DialogUtils.ShowDialogwithView(this, title, 0, contentView, protiveListener, null);
        dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				FriendsListActivity.this.finish();
			}
		});
        loadAdditionalData();
	}
	
	
	final int MSG_FETCH_USERS           = 0;
	final int MSG_FETCH_USERS_UPDATE_UI = 1;
	final int MSG_FETCH_USERS_END       = 2;
	final int MSG_LOAD_USERS_WITH_IDS    = 3;
	final int MSG_LOAD_USERS_WITH_IDS_END= 4;
	final int LOAD_INSTALL_USERS         = 5;
	final int LOAD_INSTALL_USERS_END     = 6;
	
	private class PostHandler extends Handler 
    {
        public PostHandler()
        {
            super();            
            Log.d(TAG, "new PostHandler");
        }
        
        @Override
        public void handleMessage(Message msg) 
        {
            switch(msg.what)
            {
                case MSG_FETCH_USERS:
                	
                	//for case 1
                	getLikeUsers();
                	break;
                case MSG_FETCH_USERS_UPDATE_UI:                	
            	    break;
            	    
                case MSG_FETCH_USERS_END:
                	end();
                	if(msg.getData().getBoolean(RESULT) == true)
                	{
                		int size = msg.getData().getInt("size");
                		if(likes != null) {
                			useradapter.alterDate(likes.friends);
                		}else if(apkLikes != null) {
                			useradapter.alterDate(apkLikes.friends);
                		}
                	}
                	break;
                case MSG_LOAD_USERS_WITH_IDS:
                	getUsersWithIds();
                	break;
                case MSG_LOAD_USERS_WITH_IDS_END: 
                	end();
                	if(msg.getData().getBoolean(RESULT) == true)
                	{
                		int size = msg.getData().getInt("size");
                		if(ids != null) {
                			useradapter.alterDate(mUsersList);
                		}
                	}
                	break;
                case LOAD_INSTALL_USERS:
                    getInstallAppUsers();
                    break;
                case LOAD_INSTALL_USERS_END: 
                    end();
                    if(msg.getData().getBoolean(RESULT) == true) {
                        useradapter.alterDate(mUsersList);
                    }else {
                        Log.d(TAG, "loadInstall user failed ");
                    }
                    break;
            }
        }
    }
	
	private void loadAdditionalData() {
		//case 1, like count big than 5
		if(likes != null && likes.count > 5) { // likes from stream
		    mHandler.obtainMessage(MSG_FETCH_USERS).sendToTarget();
		}else if(apkLikes != null && apkLikes.count > 5 ) { //case 2 likes from apk
			mHandler.obtainMessage(MSG_FETCH_USERS).sendToTarget();
		}else if(ids != null) { //case 3, has input ids
			mHandler.obtainMessage(MSG_LOAD_USERS_WITH_IDS).sendToTarget();
		}else if(contentType == INSTLLUSER_TYPE) {
		    mHandler.obtainMessage(LOAD_INSTALL_USERS).sendToTarget();
		}else {
			//case 4, nothing, just type and objectid
		}
		
	}

	//for case 1
	private void getLikeUsers()
	{
		begin();
		asyncQiupu.getLikeUsers(AccountServiceUtils.getSessionID(), objectID, String.valueOf(contentType), page, count, new TwitterAdapter()
		{
			public void getLikeUsers(ArrayList<QiupuSimpleUser> users) {
			    Log.d(TAG,"finish getLikeUsers="+users.size());
			   
			    mergerUsers(users);
				Message mds = mHandler.obtainMessage(MSG_FETCH_USERS_END);
    			mds.getData().putBoolean(RESULT,      true);
    			mds.getData().putInt("size",            users.size());
    			mHandler.sendMessage(mds);
                
                users.clear();
			}
			
			public void onException(TwitterException ex,TwitterMethod method) 
			{
                TwitterExceptionUtils.printException(TAG, "getFriendsListPage, server exception:", ex, method);
				int error_code = ex.getStatusCode();
				Message mds = mHandler.obtainMessage(MSG_FETCH_USERS_END);
    			mds.getData().putBoolean(RESULT,      false);
    			mds.getData().putString(ERROR_MSG,      ex.getMessage());
    			mHandler.sendMessage(mds);
			}
		});   
	}
	
	
	private void mergerUsers(List<QiupuSimpleUser> users)
	{
		if(likes != null) {
			likes.friends.clear();
			likes.friends.addAll(users);
		}else if(apkLikes != null) {
			apkLikes.friends.clear();
			apkLikes.friends.addAll(users);
		}
	}
	
	@Override
	protected void createHandler() {
		mHandler = new PostHandler();
	}
	
	private DialogInterface.OnClickListener protiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	};
	
	protected void uiLoadBegin() {
		mProgressView.setVisibility(View.VISIBLE);
	};
	@Override
	protected void uiLoadEnd() {
		mProgressView.setVisibility(View.GONE);
	}
	
	
	private void getUsersWithIds()
	{
		String userIds = parseIdsToString();
		if(StringUtil.isEmpty(userIds)) {
			Log.i(TAG, "usrids in null or lengh() < 0 ");
			return;
		}
		begin();
		asyncQiupu.getUsersList(AccountServiceUtils.getSessionID(), userIds, new TwitterAdapter()
		{
			public void getUsersList(ArrayList<QiupuSimpleUser> users) {
			    Log.d(TAG,"finish getLikeUsers="+users.size());
			   
//			    mergerUsers(users);
			    mUsersList.clear();
			    mUsersList.addAll(users);
			    
				Message mds = mHandler.obtainMessage(MSG_LOAD_USERS_WITH_IDS_END);
    			mds.getData().putBoolean(RESULT,      true);
    			mds.getData().putInt("size",            users.size());
    			mHandler.sendMessage(mds);
                
                users.clear();
			}
			
			public void onException(TwitterException ex,TwitterMethod method) 
			{
                TwitterExceptionUtils.printException(TAG, "getFriendsListPage, server exception:", ex, method);
				int error_code = ex.getStatusCode();
				Message mds = mHandler.obtainMessage(MSG_LOAD_USERS_WITH_IDS_END);
    			mds.getData().putBoolean(RESULT,      false);
    			mds.getData().putString(ERROR_MSG,      ex.getMessage());
    			mHandler.sendMessage(mds);
			}
		});   
	}
	
	private Object mLoadUserLock = new Object();
    private boolean inLoadingUsers;

    private void getInstallAppUsers() {
        synchronized (mLoadUserLock) {
            if (inLoadingUsers == true) {
                Log.d(TAG, "in loading users, return");
                return;
            }
        }
        begin();
        synchronized (mLoadUserLock) {
            inLoadingUsers = true;
        }

        asyncQiupu.getInstalledUserList(getSavedTicket(), objectID, ApkBasicInfo.INSTALLUSERREASON, page, count,
                new TwitterAdapter() {
                    public void getInstalledUserList(ArrayList<QiupuSimpleUser> installedUserList) {
                        mUsersList.clear();
                        mUsersList.addAll(installedUserList);
                        synchronized (mLoadUserLock) {
                            inLoadingUsers = false;
                        }

                        Message msg = mHandler.obtainMessage(LOAD_INSTALL_USERS_END);
                        msg.getData().putBoolean(RESULT, true);
                        msg.sendToTarget();
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG, "getInstalledUserList, server exception:", ex,
                                method);

                        synchronized (mLoadUserLock) {
                            inLoadingUsers = false;
                        }

                        Message msg = mHandler.obtainMessage(LOAD_INSTALL_USERS_END);
                        msg.getData().putBoolean(RESULT, false);
                        msg.getData().putString(ERROR_MSG, ex.getMessage());
                        msg.sendToTarget();
                    }
                });

    }
	
	private String parseIdsToString() {
		if(ids != null && ids.length > 0) {
			StringBuilder idsString = new StringBuilder();
			for(int i=0; i<ids.length; i++) {
				if(idsString.length() > 0) {
					idsString.append(",");
				}
				idsString.append(ids[i]);
			}
			return idsString.toString();
		}
		return null;
	}

    public static void showUserList(Context context, String title, ArrayList userList) {
        Intent user_intent = new Intent(context, FriendsListActivity.class);
        user_intent.putExtra(KEY_EXTRA_TITLE, title);
        user_intent.putExtra(KEY_EXTRA_USERSLIST, userList);
        context.startActivity(user_intent);
    }
    
    public static void showApkInstallUserList(Context context, String title, String objectid) {
        Intent user_intent = new Intent(context, FriendsListActivity.class);
        user_intent.putExtra(KEY_EXTRA_TITLE, title);
        user_intent.putExtra(KEY_EXTRA_TYPE, INSTLLUSER_TYPE);
        user_intent.putExtra(KEY_EXTRA_OBJECTID, objectid);
        context.startActivity(user_intent);
    }
    
    public static void showApkLikeUserList(Context context, String title, String objectid, Likes likes ) {
        Intent user_intent = new Intent(context, FriendsListActivity.class);
        user_intent.putExtra(KEY_EXTRA_TITLE, title);
        user_intent.putExtra(KEY_EXTRA_OBJECTID, objectid);
        user_intent.putExtra(KEY_EXTRA_APKLIKES, likes);
        context.startActivity(user_intent);
    }
}
