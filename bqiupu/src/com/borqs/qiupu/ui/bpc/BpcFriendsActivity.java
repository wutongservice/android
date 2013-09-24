package com.borqs.qiupu.ui.bpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.text.TextUtils;
import android.util.Xml;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.Utilities;
import com.borqs.common.view.BpcFriendsItemView;
import com.borqs.qiupu.ui.*;
import com.borqs.qiupu.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.account.service.ContactSimpleInfo;
import com.borqs.common.adapter.BPCFriendsAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.TabFilterListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.TabBottomView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.util.StringUtil;

public class BpcFriendsActivity extends BasicActivity implements
        OnScrollListener, TabFilterListener, BPCFriendsAdapter.MoreItemCheckListener,
        View.OnClickListener, UsersActionListner{
	private static final String TAG = "Qiupu.FriendsActivity";

    private static String platform;

	private long mUserid;
//	private String mUserName;
	private int mConcernType;
	
	private ListView       mListView;
	
	private BPCFriendsAdapter mbpcFriendsAdapter;
	
	private ArrayList<QiupuUser> mFriends;
	private ArrayList<QiupuUser> mFollowers;
	private ArrayList<QiupuUser> mBilateral;
	
    private LinearLayout tutorial;
    
	private int mFriendsPage = 0;
	private int mFollowerPage = 0;
	private int pagesize = 20;
	private final int maxcount = 100;
    public  enum UITAB{NONE, DISPLAY_CONCERN, DISPLAY_FOLLOWER, DISPLAY_YOU_MAY_KNOW, DISPLAY_FIND_FRIENDS};
	private UITAB currentTab;
	
	private ArrayList<ContactSimpleInfo> toAddress = new ArrayList<ContactSimpleInfo>();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(QiupuConfig.LOGD)Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_main);
		
		parseActivityIntent(getIntent());

		tutorial             = (LinearLayout)findViewById(R.id.tutorial);
		
		ImageView btn_clear_text = (ImageView) findViewById(R.id.btn_clear_text);
		
		mFriends   = new ArrayList<QiupuUser>();
		mFollowers = new ArrayList<QiupuUser>();
		mBilateral = new ArrayList<QiupuUser>();
		
		mbpcFriendsAdapter = new BPCFriendsAdapter(this, this);
		
		mListView = (ListView) findViewById(R.id.content);
		mListView.setOnScrollListener(this);
		mListView.setOnCreateContextMenuListener(this);
		mListView.setAdapter(mbpcFriendsAdapter);
    	mListView.setOnItemClickListener(friendItemClickListener);
		
		initTutorial(tutorial);
		
		currentTab = UITAB.DISPLAY_FIND_FRIENDS;
		initTabView();
		
        overrideMiddleActionBtn(R.drawable.actionbar_icon_invite_normal, inviteFriendsClick);

        String nickName = orm.getUserName(mUserid);
        setHeadTitle(TextUtils.isEmpty(nickName) ? "" : nickName);
		
		showMiddleActionBtn(false);
		showLeftActionBtn(false);

        mHandler.obtainMessage(LOAD_DATA).sendToTarget();
	}	

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent:" + intent);
		
		 parseActivityIntent(intent);
         String nickName = orm.getUserName(mUserid);
		 setHeadTitle(TextUtils.isEmpty(nickName) ? "" : nickName);
		 mHandler.obtainMessage(LOAD_DATA).sendToTarget();
	}
	
	// TODO: parse intent scheme from 3rd component.
    private void parseActivityIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (null != bundle) {
            mUserid = bundle.getLong(BpcApiUtils.User.USER_ID, -1L);
            if (mUserid == -1L) {
                mUserid = AccountServiceUtils.getBorqsAccountID();
//                mUserName = AccountServiceUtils.getAccountNickName();
            } else {
                mUserid = bundle.getLong(BpcApiUtils.User.USER_ID);
//                mUserName = bundle.getString(USER_NICKNAME);
            }
            mConcernType = bundle.getInt(USER_CONCERN_TYPE, 0);
        } else {
            mUserid = AccountServiceUtils.getBorqsAccountID();
//            mUserName = AccountServiceUtils.getAccountNickName();
            mConcernType = 0;
        }
    }
    
	@Override
	protected void postToSomeoneWall() {
        IntentUtil.startComposeIntent(this, mUserid == getSaveUid() ? "" : String.valueOf(mUserid), true, null);
	}

	@Override
	protected void onDestroy() {
		QiupuHelper.unregisterUserListener(getClass().getName());
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume:");
        QiupuHelper.registerUserListener(getClass().getName(), this);
		super.onResume();
	}	

	private void loadData()
	{
		Log.d(TAG, "mUserid :"+mUserid);
		if(mConcernType == QiupuConfig.USER_INDEX_FRIENDS) 
		{
			setBottomShortcuts(R.id.bottom_shortcut_following,true);
		}
		else if(mConcernType == QiupuConfig.USER_INDEX_FOLLOWERS) 
		{
			setBottomShortcuts(R.id.bottom_shortcut_follower, true);			
		}
	}
	
	private void afterGetDataEnd(Message msg)
	{
		boolean sucadd = msg.getData().getBoolean(RESULT, false);
		if(sucadd == true)
		{
            if(currentTab == UITAB.DISPLAY_CONCERN)
			{
				refreshPeopleUI(mFriends);
			}
			else if(currentTab == UITAB.DISPLAY_FOLLOWER)
			{
				refreshPeopleUI(mFollowers);
			}
			else if(currentTab == UITAB.DISPLAY_YOU_MAY_KNOW)
			{
				refreshPeopleUI(mBilateral);
			}
		}
		else
		{
			String ErrorMsg = msg.getData().getString(ERROR_MSG);
			if(isEmpty(ErrorMsg) == false)
			{
			    Toast.makeText(BpcFriendsActivity.this, ErrorMsg, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private static final int GET_OTHER_FRIENDS         = 103;
	private static final int GET_OTHER_FOLLOWERS       = 104;
	private static final int LOAD_DATA                 = 105;
	private static final int GET_USERS_BILATERAL       = 106;
		
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_DATA:
			{
				loadData();
				break;
			}			
			case QiupuMessage.MESSAGE_LOAD_DATA_END:
			{
				Log.d(TAG, "load data end");
				end();
				afterGetDataEnd(msg);
				break;
			}
			case GET_OTHER_FRIENDS:
			{
				syncfriendsInfo(mUserid, mFriendsPage, pagesize, "", true);
				break;
			}
			case GET_OTHER_FOLLOWERS:
			{
				syncfriendsInfo(mUserid, mFollowerPage, pagesize, "", false);
				break;
			}
			case GET_USERS_BILATERAL:
			{
				getFriendsBilateral();
				break;
			}
		 }
		}
	}	
	
	private void refreshUser()
	{
		if(currentTab == UITAB.DISPLAY_CONCERN)
		{
			mFriendsPage = 0;
			mFriends.clear();
			mHandler.obtainMessage(GET_OTHER_FRIENDS).sendToTarget();
		}
		else if(currentTab == UITAB.DISPLAY_FOLLOWER)
		{
			mFollowerPage = 0;
			mFollowers.clear();
			mHandler.obtainMessage(GET_OTHER_FOLLOWERS).sendToTarget();
		}
		else if(currentTab == UITAB.DISPLAY_YOU_MAY_KNOW)
		{
			mHandler.obtainMessage(GET_USERS_BILATERAL).sendToTarget();
		}
	}
	
	@Override
	protected void loadRefresh() 
	{
		if(AccountServiceUtils.isAccountReady())
		{
			setTutorialGone();
			refreshUser();
		}
	}

	@Override
	protected void loadSearch() 
	{
        gotoSearchActivity();
	}
	
	public void onClick(View view) 
	{
		final int id = view.getId();
		switch (id) 
		{
			case 0:
//				onBackPressed();
				break;		
			default:
				Object tag = view.getTag();
				if(tag != null && tag instanceof Integer) 
				{
					setBottomShortcuts((Integer)tag, true);
				}
				
		}
		super.onClick(view);
	}
	
	public View.OnClickListener loadRefreshClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			refreshUser();
		}
	};
	
	View.OnClickListener friendsClickListener = new  View.OnClickListener(){
		public void onClick(View v) 
		{
			if (v.getId() == R.id.id_friends) {
				switchFriendsTitleUI(UITAB.DISPLAY_CONCERN);
			} else if (v.getId() == R.id.id_followers) {
				switchFriendsTitleUI(UITAB.DISPLAY_FOLLOWER);
			} else if (v.getId() == R.id.id_you_may_know) {
				switchFriendsTitleUI(UITAB.DISPLAY_YOU_MAY_KNOW);
			}
		}		
	};
	
	
	private void switchFriendsTitleUI(UITAB tab)
	{
		currentTab = tab;
		if(!AccountServiceUtils.isAccountReady())
		{
			return ;
		}
		if(tab == UITAB.DISPLAY_CONCERN)
		{
			refreshPeopleUI(mFriends);
			if(mFriends.size() <= 0)
			{
				Message msg = mHandler.obtainMessage(GET_OTHER_FRIENDS);
				msg.sendToTarget();
			}
		}
		else if(tab == UITAB.DISPLAY_FOLLOWER)
		{
			refreshPeopleUI(mFollowers);
			if(mFollowers.size() <= 0)
			{
				mHandler.obtainMessage(GET_OTHER_FOLLOWERS).sendToTarget();
			}
		}
		else if(tab == UITAB.DISPLAY_YOU_MAY_KNOW)
		{
			mCommonBg.setText(R.string.common_know);
			refreshPeopleUI(mBilateral);
			if(mBilateral.size() <= 0)
			{
				mHandler.obtainMessage(GET_USERS_BILATERAL).sendToTarget();
			}
		}
	}
	
	private class FriendsItemClickListener implements AdapterView.OnItemClickListener 
	{
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) 
		{
			if (BpcFriendsItemView.class.isInstance(view)) {
				BpcFriendsItemView fv = (BpcFriendsItemView) view;
				
				QiupuUser tmpUser = fv.getUser();

                if (null == tmpUser) {
                    Log.w(TAG, "FriendsItemClickListener, onItemClick fail to get user.");
                } else {
                    IntentUtil.startUserDetailIntent(fv.getContext(), tmpUser.uid, tmpUser.nick_name, tmpUser.circleName);
                }
			}
		}
	}
	private boolean followershowmore;
    private boolean followingshowmore;
	@Override
	protected void getUserInfoEndCallBack(List<QiupuUser> users, boolean isfollowing)
	{
		if(users.size() <= 0)
		{
			if(isfollowing) {
                followingshowmore = false;
            } else {
                followershowmore = false;
            }
		}
		else
		{
			if(isfollowing)
			{
                followingshowmore = true;
				mFriends.addAll(users);
			}
			else
			{
                followershowmore = true;
				mFollowers.addAll(users);
			}
		}
	}
	
	@Override
	public boolean isInProcess() {	
		switch(currentTab)
		{
			case DISPLAY_CONCERN:
			{
//			    synchronized(mFollowingLock)
//				{
//					return inloadingFollowing==true;
//				}			    
			}
			case DISPLAY_FOLLOWER:
			{
//				synchronized(mLoadingLock)
//				{
//					return inLoading==true;
//				}			    
			}
		}
		return false;		
	}
	
	@Override
	protected void createHandler() 
	{
		mHandler = new MainHandler();
	}

	protected void uiLoadBegin() {
		super.uiLoadBegin();
		
		for(int i=mListView.getChildCount()-1;i>0;i--)            
        {
            View v = mListView.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(getString(R.string.loading));
                break;
            }
        }
	}
	
	protected void uiLoadEnd() {
		super.uiLoadEnd();
		
		for(int i=mListView.getChildCount()-1;i>0;i--)            
        {
            View v = mListView.getChildAt(i);
            if(Button.class.isInstance(v))
            {
                Button bt = (Button)v;
                bt.setText(getString(R.string.list_view_more));
                break;
            }
        }    
	}

	private int followingItem = 0; 
	private int followerItem = 0;
	public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(UITAB.DISPLAY_CONCERN == currentTab)
		{
			 followingItem = firstVisibleItem + visibleItemCount ;  
		}
		else if(UITAB.DISPLAY_FOLLOWER == currentTab)
		{
			followerItem = firstVisibleItem + visibleItemCount ;  
		}
	}

	public void onScrollStateChanged(AbsListView v, int state) {
		boolean forloadmore=(state == OnScrollListener.SCROLL_STATE_IDLE);
		loadOlderUsers(forloadmore, false);
	}
	
	public View.OnClickListener loadOlderClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			Log.d(TAG, "load older applications="+currentTab);		  	  	
			if(UITAB.DISPLAY_CONCERN == currentTab)
			{					
				syncFollowingInfo();
			}
			else if(UITAB.DISPLAY_FOLLOWER == currentTab)
			{
				syncFollowerInfo();
			}
		}
	};
	
	protected void loadOlderUsers(boolean formore, boolean forceget)
    {
    	if(UITAB.DISPLAY_CONCERN == currentTab)
		{
    		int followingcount=mFriends.size();
    		Log.i(TAG, "followingcount:" + followingcount); 
    		if ((followingItem == followingcount + 1 && formore) || forceget) 
    		{  
    			syncFollowingInfo();
    		}  
		}
		else if(UITAB.DISPLAY_FOLLOWER == currentTab){
			int followercount = mFollowers.size();
			Log.i(TAG, "followercount:" + followercount); 
			if ((followerItem == followercount + 1 && formore) || forceget) 
			{  
				syncFollowerInfo();
			}
		}
    }
	
	private void syncFollowingInfo(){
		mFriendsPage += 1;
		Log.i(TAG, "mFriendsPage:" + mFriendsPage); 
		try {
			failCallFollowingMethod = BpcFriendsActivity.class.getDeclaredMethod("SubApplicationPage", (Class[])null);
		} catch (Exception e){}
		syncfriendsInfo(mUserid, mFriendsPage, pagesize, "", true);
	}
	
	private void syncFollowerInfo(){
		mFollowerPage += 1;
		Log.i(TAG, "mFollowerPage:" + mFollowerPage); 
		try {
			failCallFollowerMethod = BpcFriendsActivity.class.getDeclaredMethod("SubApplicationPage", (Class[])null);
		} catch (Exception e){}
		syncfriendsInfo(mUserid, mFollowerPage, pagesize, "", false);
	}
	
	protected Method failCallFollowerMethod;
	protected Method failCallFollowingMethod;
	protected void SubApplicationPage()
	{
		Log.d(TAG, "resore the dpage--");
		if(currentTab == UITAB.DISPLAY_CONCERN)
		{
			mFriendsPage--;
			if(mFriendsPage < 0)
				mFriendsPage = 0;
		}
		else if(currentTab == UITAB.DISPLAY_FOLLOWER)
		{
			mFollowerPage--;
			if(mFollowerPage < 0)
				mFollowerPage = 0;
		}
	}
	 
	protected void callFailLoadFollowerMethod()
	{
		try 
		{
			if(failCallFollowerMethod!=null)
			{
				failCallFollowerMethod.invoke(this, (Object[])null);
			}
		}catch(Exception ne){}
	}
	
	protected void callFailLoadFollowingMethod()
	{
		try 
		{
			if(failCallFollowingMethod!=null)
			{
				failCallFollowingMethod.invoke(this, (Object[])null);
			}
		}catch(Exception ne){}
	}
	 
	@Override
	protected boolean  goNextPage()
    {
		if(currentTab == UITAB.DISPLAY_CONCERN)
		{
			setBottomShortcuts(R.id.bottom_shortcut_follower, true);			
		}
		else if(currentTab == UITAB.DISPLAY_FOLLOWER)
		{
			setBottomShortcuts(R.id.bottom_shortcut_bilateral, true);
		}
		else if(currentTab == UITAB.DISPLAY_YOU_MAY_KNOW)
		{
			setBottomShortcuts(R.id.bottom_shortcut_following, true);
		}
        return true;
    }
    
	@Override
	protected boolean  goPrePage()
	{
		if(currentTab == UITAB.DISPLAY_CONCERN)
		{
			setBottomShortcuts(R.id.bottom_shortcut_bilateral, true);
		}
		else if(currentTab == UITAB.DISPLAY_FOLLOWER)
		{
			setBottomShortcuts(R.id.bottom_shortcut_following, true);
		}
		else if(currentTab == UITAB.DISPLAY_YOU_MAY_KNOW)
		{
			setBottomShortcuts(R.id.bottom_shortcut_follower, true);
		}
    	return true;
	}
	
	private TextView  mFollowingBg,  mFollowerBg,  mCommonBg;
	private TabBottomView bottom_region;
	
	private void initTabView()
	{
		bottom_region = (TabBottomView)findViewById(R.id.bottom_region);
		bottom_region.setTabFilterLister(this);
				
		mFollowingBg = (TextView) findViewById(R.id.bottom_shortcut_following);
        mFollowingBg.setTag(R.id.bottom_shortcut_following);
        mFollowingBg.setOnClickListener(this);
        
        mFollowerBg = (TextView) findViewById(R.id.bottom_shortcut_follower);
        mFollowerBg.setTag(R.id.bottom_shortcut_follower);
        mFollowerBg.setOnClickListener(this);
        
        mCommonBg = (TextView) findViewById(R.id.bottom_shortcut_bilateral);
        mCommonBg.setTag(R.id.bottom_shortcut_bilateral);
        mCommonBg.setOnClickListener(this);
	}

	private View getViewFromTouchPointer(int x)
	{
		int []location = new int[2];
    	
		mFollowingBg.getLocationInWindow(location);
    	if(x>location[0] && x < (location[0] +  mFollowingBg.getWidth()))
    	{
    		return mFollowingBg;
    	}
    	
    	mFollowerBg.getLocationInWindow(location);
    	if(x>location[0] && x < (location[0] +  mFollowerBg.getWidth()))
    	{
    		return mFollowerBg;
    	}
    	
    	mCommonBg.getLocationInWindow(location);
    	if(x>location[0] && x < (location[0] +  mCommonBg.getWidth()))
    	{
    		return mCommonBg;
    	}
    	return null;
	}
	
	public void beginDrag(MotionEvent ev) {}

	public void dismissOverlayer() {}

	public void filterMoveAction(MotionEvent ev) {
		View view = getViewFromTouchPointer((int)ev.getX());
		if(view != null)
		{
		    setBottomShortcuts((Integer)view.getTag(), true);
		}
	}
	
	private void setBottomShortcuts(Integer id, boolean callDataEffect) 
	{
		if(id == R.id.bottom_shortcut_following) {    		
    		if(currentTab != UITAB.DISPLAY_CONCERN)
    		{
    		    mFollowingBg.setBackgroundResource(R.drawable.bottom_btn_selected);
    		    mFollowerBg.setBackgroundResource(R.drawable.tab_view_bg);
    		    mCommonBg.setBackgroundResource(R.drawable.tab_view_bg);
	    		
	    		if(callDataEffect)
	    		switchFriendsTitleUI(UITAB.DISPLAY_CONCERN);
    		}
    		
		} else if(id == R.id.bottom_shortcut_follower) {    		
    		if(currentTab != UITAB.DISPLAY_FOLLOWER)
    		{
    		    mFollowerBg.setBackgroundResource(R.drawable.bottom_btn_selected);
    		    mFollowingBg.setBackgroundResource(R.drawable.tab_view_bg);
    		    mCommonBg.setBackgroundResource(R.drawable.tab_view_bg);
	    		
	    		if(callDataEffect)
	    		switchFriendsTitleUI(UITAB.DISPLAY_FOLLOWER);
    		}
		}else if(id == R.id.bottom_shortcut_bilateral) {    		
    		if(currentTab != UITAB.DISPLAY_YOU_MAY_KNOW)
    		{
    		    mCommonBg.setBackgroundResource(R.drawable.bottom_btn_selected);
	    		mFollowingBg.setBackgroundResource(R.drawable.tab_view_bg);
	    		mFollowerBg.setBackgroundResource(R.drawable.tab_view_bg);
	    		
	    		if(callDataEffect)
	    		{
	    			switchFriendsTitleUI(UITAB.DISPLAY_YOU_MAY_KNOW);
	    		}
    		}
		}
	}	
	
	boolean inloadingBilateral = false;
	Object  mBilateralLock = new Object();
	
	private void getFriendsBilateral()
	{
		synchronized(mBilateralLock)
    	{
    		if(inloadingBilateral == true)
    		{
    			Log.d(TAG, "in doing get Bilateral data");
    			return ;
    		}
    	}
		
		synchronized(mBilateralLock)
		{
			inloadingBilateral = true;				    		
		}
		begin();
		
		asyncQiupu.getFriendsBilateral(getSavedTicket(), mUserid, -1, -1, new TwitterAdapter()
		{
			public void getFriendsBilateral(ArrayList<QiupuUser> users) {
			    Log.d(TAG,"finish getFriendsBilateral="+users.size());
			    
			    mBilateral = users;
				Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
				msg.getData().putBoolean(RESULT,   true);
				msg.sendToTarget();
				
				synchronized(mBilateralLock)
				{
					inloadingBilateral = false;				    		
				}
			}
			
			public void onException(TwitterException ex,TwitterMethod method) 
			{
                TwitterExceptionUtils.printException(TAG, "getFriendsBilateral, server exception:", ex, method);
				synchronized(mBilateralLock)
				{
					inloadingBilateral = false;				    		
				}
				Message msg = mHandler.obtainMessage(QiupuMessage.MESSAGE_LOAD_DATA_END);
				msg.getData().putBoolean(RESULT,   false);
				msg.getData().putString(ERROR_MSG, ex.getMessage());
				msg.sendToTarget();
			}
		});   
	}
	
	private void setTutorialGone(){
		if(tutorial.getVisibility() == View.VISIBLE)
		{
			tutorial.setVisibility(View.GONE);
		}
	}
	
	AdapterView.OnItemClickListener friendItemClickListener = new FriendsItemClickListener();
    private void refreshPeopleUI(ArrayList<QiupuUser> users) {
    	mbpcFriendsAdapter.alterDataList(users);
    }
	
	public void updateItemUI(final QiupuUser user)
	{
		if(user != null)
		{
			refreshUserItem(user);
		}
	}
	
	public void addFriends(QiupuUser user)
	{
		IntentUtil.startCircleSelectIntent(this, user.uid, null);
	}

	public void refuseUser(long uid){}
	
	@Override
	public void deleteUser(QiupuUser user)
	{}

	@Override
	public void sendRequest(QiupuUser user)
	{}
	
	private void refreshUserItem(final QiupuUser user) {
		
		ArrayList<QiupuUser> datasource = null;
		if(currentTab == UITAB.DISPLAY_CONCERN)
		{
			datasource = mFriends;
		}
		else if(currentTab == UITAB.DISPLAY_FOLLOWER)
		{
			datasource = mFollowers;
		}
		else if(currentTab == UITAB.DISPLAY_YOU_MAY_KNOW)
		{
			datasource = mBilateral;			
		}
		final int count = datasource.size();
		mHandler.post( new Runnable()
		{
			public void run()
			{
		        for(int j=0; j < count;j++)
		        {
		            View v = mListView.getChildAt(j);
		            if(BpcFriendsItemView.class.isInstance(v))
		            {
		            	BpcFriendsItemView fv = (BpcFriendsItemView)v;
		    			
		                if(fv.getUserID() == user.uid)
		                {
		                	// parse user info to db
		                	QiupuUser tmpuser = fv.getUser();
		                	tmpuser.pedding_requests = user.pedding_requests;
		                	tmpuser.circleId = user.circleId;
		                	tmpuser.circleName = user.circleName;
		                    fv.refreshUI();
		                    break;
		                }
		            }
		        }
			}
		});
    }
	
	@Override
	protected void doGetFriendsEndCallBack(Message msg)
	{
		afterGetDataEnd(msg);
	}

    public boolean isMoreItemHidden() {
        if (currentTab == UITAB.DISPLAY_YOU_MAY_KNOW
                || currentTab == UITAB.DISPLAY_FOLLOWER && followershowmore == false
                || currentTab == UITAB.DISPLAY_CONCERN && followingshowmore == false
                || currentTab == UITAB.DISPLAY_FIND_FRIENDS) {
            return true;
        }

        return false;
    }

    @Override
    public View.OnClickListener getMoreItemClickListener() {
        return loadOlderClick;
    }

    @Override
    public int getMoreItemCaptionId() {
        return isInProcess() ? R.string.loading : R.string.list_view_more;
    }


    View.OnClickListener inviteFriendsClick = new View.OnClickListener() {
        public void onClick(View v) {
            if (null == platform) {
                platform = Utilities.getplatform("apps.setting.platformversion");
                if (StringUtil.isEmpty(platform)) {
                    platform = loadVersionInfo();
                }
            }

            if (QiupuConfig.LOGD) Log.d(TAG, "platfom info :" + platform);


            if (platform.contains("OPhone") &&
                    Integer.parseInt(Utilities.getSdkVersion()) >= 8) {
                pickRecipients();
            } else {
                Intent intent = new Intent(BpcFriendsActivity.this, com.borqs.qiupu.ui.InviteContactActivity.class);
                startActivity(intent);
            }
        }
    };

    private static String loadVersionInfo() {
        FileReader verReader;
        String mPlatformversion = "";
        File verFile = new File("/opl/etc/settings_version_info.xml");

        try {
            verReader = new FileReader(verFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Couldn't find or open tone file " + verFile);
            return "";
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(verReader);
            XmlUtils.beginDocument(parser, "version");
            while (parser.getEventType() != parser.END_DOCUMENT) {
                XmlUtils.nextElement(parser);
                String name = parser.getName();
                if ("info".equals(name)) {
                    String temp = null;
                    temp = parser.getAttributeValue(null, "apps.setting.platformversion");
                    if (!TextUtils.isEmpty(temp)) {
                        mPlatformversion = temp;
                        temp = null;
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Got execption parsing tone resource.", e);
        }
        Log.w(TAG, "load the profile from xml file");
        return mPlatformversion;
    }
}
