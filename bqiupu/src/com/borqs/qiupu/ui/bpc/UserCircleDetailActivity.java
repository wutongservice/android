package com.borqs.qiupu.ui.bpc;

import java.util.HashMap;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import twitter4j.QiupuUser;
import twitter4j.UserCircle;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.UserCircleDetailFragmentAdapter;
import com.borqs.common.listener.OnListItemClickListener;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.IntentUtil;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.FriendsListFragment;
import com.borqs.qiupu.fragment.FriendsListFragment.FriendsListFragmentCallBackListener;
import com.borqs.qiupu.fragment.FixedTabsView;
import com.borqs.qiupu.fragment.StreamListFragment;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;

public class UserCircleDetailActivity extends BasicActivity implements UsersActionListner,
                          OnListItemClickListener, FriendsListFragmentCallBackListener, StreamListFragment.StreamListFragmentCallBack {

	private static final String TAG = "Qiupu.UserCircleDetailActivity";

	private static final String CIRCLE_NAME  = "CIRCLE_NAME";
    private static final String USER_ID      = "USER_ID";
    private static final String MEMBER_COUNT = "MEMBER_COUNT";

//    private int  mCircleId;
    private UserCircleDetailFragmentAdapter mAdapter;
    private int mCurrentpage;
    private FriendsListFragment mFriendsListFragment;
    private static final int DEFAULT_INDEX = 1;
    StreamListFragment.MetaData mFragmentData = new StreamListFragment.MetaData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_detail_view);

        QiupuHelper.registerUserListener(getClass().getName(), this);
        parseActivityIntent(getIntent());

        mAdapter = new UserCircleDetailFragmentAdapter(getSupportFragmentManager(), this);

        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(DEFAULT_INDEX);

        FixedTabsView mIndicator = (FixedTabsView) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
//        mIndicator.setSelectTab(mConcernType);
        mIndicator.setSelectTab(DEFAULT_INDEX);
        mIndicator.setAdapter(mAdapter);
        mIndicator.setOnPageChangeListener(pagerOnPageChangeListener);

//        PageIndicator mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
//        mIndicator.setViewPager(mPager);
//        mIndicator.setCurrentItem(mConcernType);
//        mIndicator.setOnPageChangeListener(pagerOnPageChangeListener);


        showMiddleActionBtn(true);
        showLeftActionBtn(true);
        showRightActionBtn(false);

//        alterMiddleActionBtnByInvitation();
        overrideMiddleActionBtn(R.drawable.actionbar_icon_release_normal , gotoComposeListener);
        mAdapter.setTabBtnBg(DEFAULT_INDEX);
    }

    @Override
    protected void createHandler() {
    }

    @Override
    protected void loadSearch()
    {
        gotoSearchActivity();
    }
    
    protected View.OnClickListener gotoComposeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            UserCircle circle = QiupuORM.getInstance(UserCircleDetailActivity.this).queryOneCircle(-1, mFragmentData.mCircleId);
            if (circle.memberCount <= 0) {
//                UserCircleDetailActivity.this.showShortToast(R.string.no_user_in_circle);
                IntentUtil.startComposeIntent(UserCircleDetailActivity.this, "#" +  mFragmentData.mCircleId, false, null);
            } else {
                IntentUtil.startComposeIntent(UserCircleDetailActivity.this, "#" +  mFragmentData.mCircleId, true, null);
            }
        }
    };

    // TODO: parse intent scheme from 3rd component.
    private void parseActivityIntent(Intent intent) {
        String requestName = null;
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri dataUri = getIntent().getData();
            if (null != dataUri) {
                Cursor cursor = getContentResolver().query(dataUri,
                        new String[]{ContactsContract.Groups.SOURCE_ID},
                        null, null, null);
                if (null != cursor && cursor.moveToNext()) {
                    try {
                        mFragmentData.mUserId = AccountServiceUtils.getBorqsAccountID();
                        mFragmentData.mCircleId = cursor.getLong(0);
                        requestName = CircleUtils.getLocalCircleName(this, mFragmentData.mCircleId, orm.getOneCircleName(mFragmentData.mCircleId));
                    } finally {
                        cursor.close();
                    }
                }
            }
        } else {
            Bundle bundle = intent.getExtras();
            requestName = bundle.getString(CIRCLE_NAME);
//        mUserId   = bundle.getLong(USER_ID, -1);
            mFragmentData.mCircleId = bundle.getLong(CircleUtils.CIRCLE_ID, -1);

            mFragmentData.mFragmentTitle = getString(R.string.circle_detail_post);
        }
        setHeadTitle(requestName);
    }

    ViewPager.OnPageChangeListener pagerOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int page) {
            mCurrentpage = page;
            mAdapter.setTabBtnBg(mCurrentpage);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int page) {
        }
    };


//    public long getUserId(){
//        return mUserid;
//    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    };
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {}

    @Override
    protected void loadRefresh() {
        Log.d(TAG, "currentpage: " + mCurrentpage);
        mAdapter.loadRefresh(mCurrentpage);
    }

    @Override
    public void uiLoadEnd() {
        if(mAdapter.getLoadStatus(mCurrentpage)) {
            super.uiLoadEnd();
        }
    }

    @Override
    public void onListItemClick(View view, Fragment fg) {

    }


    @Override
	public long getCircleId() {
		return mFragmentData.mCircleId;
	}

    @Override
    public StreamListFragment.MetaData getFragmentMetaData(int index) {
        return mFragmentData;
    }

    @Override
    public String getSerializeFilePath() {
        return QiupuHelper.circle + mFragmentData.mCircleId;
    }

    public static void startCircleDetailIntent(Context context, UserCircle circle, boolean fromTab) {
        if (null != context && null != circle) {
            final Intent intent = new Intent(context, UserCircleDetailActivity.class);

            Bundle profileBundle = new Bundle();
            profileBundle.putString(CIRCLE_NAME,
                    CircleUtils.getLocalCircleName(context, circle.circleid, circle.name));
            profileBundle.putLong(USER_ID, AccountServiceUtils.getBorqsAccountID());
            profileBundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
            profileBundle.putInt(MEMBER_COUNT, circle.memberCount);
            if (fromTab == false) {
                profileBundle.putString("home", "main");
            }
            intent.putExtras(profileBundle);

            context.startActivity(intent);
        }
    }
    
    @Override
	protected void deletePeopleInCircle()
	{
    	if(mFriendsListFragment != null) {
    		mFriendsListFragment.gotoPickCircleUserActivity(PickCircleUserActivity.type_delete_friends);
    	}
	}

	@Override
	public void getFriendsListFragment(FriendsListFragment fragment) {
		mFriendsListFragment = fragment;
	}

	@Override
	public void updateItemUI(QiupuUser user) {
		if(mFriendsListFragment != null){
			mFriendsListFragment.updateListUI(false);
		}
		
	}

	@Override
	public void addFriends(QiupuUser user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refuseUser(long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUser(QiupuUser user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRequest(QiupuUser user) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		QiupuHelper.unregisterUserListener(getClass().getName());
	}
}
