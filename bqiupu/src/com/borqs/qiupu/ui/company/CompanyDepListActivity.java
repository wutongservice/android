package com.borqs.qiupu.ui.company;

import java.util.ArrayList;

import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.DepartmentAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.DepartmentItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class CompanyDepListActivity extends BasicNavigationActivity {

    private final static String TAG = "CompanyDepListActivity";
//    private String mCircleids;
    
    private long mCircleId;
    private int mFormal;
    private ListView            mListView;
    private DepartmentAdapter     mAdapter;
    private ArrayList<UserCircle> mCircleList = new ArrayList<UserCircle>();
    public static final String FORMAL_TAG = "FORMAL_TAG";
    public static final String TITLE_TAG = "TITLE_TAG";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.dep_list_main);
//        mCircleids = getIntent().getStringExtra(CircleUtils.CIRCLE_ID);
        Intent intent = getIntent();
        mCircleId = intent.getLongExtra(CircleUtils.CIRCLE_ID, -1);
        mFormal = intent.getIntExtra(FORMAL_TAG, -1);
        String title = intent.getStringExtra(TITLE_TAG);
        if(StringUtil.isValidString(title)) {
        	setHeadTitle(title);
        }else {
        	setHeadTitle(R.string.user_circles);
        }

        showRightActionBtn(false);
        showLeftActionBtn(true);
        
        mListView = (ListView) findViewById(R.id.default_listview);
        
      mAdapter = new DepartmentAdapter(this, mCircleList);
      mListView.setAdapter(mAdapter);
      mListView.setOnItemClickListener(mItemClickListener);

      loadRefresh();

    }
    
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (DepartmentItemView.class.isInstance(view)) {
            	DepartmentItemView civ = (DepartmentItemView) view;
            	if(civ.getDataInfo() != null)
                {
            		IntentUtil.startPublicCircleDetailIntent(CompanyDepListActivity.this, civ.getDataInfo());
                }
            } else {
                Log.d(TAG, "mItemClickListener error, view = " + view);
            }
        }
    };

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadSearch() 
    {
        gotoSearchActivity();
    }
    
    @Override
    protected void loadRefresh() {
    	mHandler.obtainMessage(GET_CIRCLE).sendToTarget();
    }
    
    private final int GET_CIRCLE = 1;
    private final int GET_CIRCLE_END = 2;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GET_CIRCLE: {                
            	getChildCircle();
                break;
            }
            case GET_CIRCLE_END:{
                end();
                if (msg.getData().getBoolean(RESULT)) {
                    mAdapter.alterDataList(mCircleList);
                } else {
                    ToastUtil.showOperationFailed(CompanyDepListActivity.this, mHandler, false);
                }
                break;
            }
            
            }
        }
    }
    
//    boolean inloadingDep = false;
//    Object depLock = new Object();
//
//    private void getCompanyCircle() {
//        synchronized (depLock) {
//            if (inloadingDep == true) {
//                Log.d(TAG, "in doing syncCircleInfo data");
//                return;
//            }
//        }
//
//        synchronized (depLock) {
//        	inloadingDep = true;
//        }
//
//        begin();
//        asyncQiupu.syncPublicCircles(AccountServiceUtils.getSessionID(),mCircleids,
//                new TwitterAdapter() {
//                    public void syncPublicCircles(ArrayList<UserCircle> userCircles) {
//                    	Log.d(TAG, "finish syncPublicCircles= " + userCircles.size());
//                    	if(mCircleList == null) return;
//                    	mCircleList.clear();
//                    	mCircleList.addAll(userCircles);
//                        
////                        if (userCircles.size() > 0) {
////                            orm.removeAllCirclesWithOutNativeCircles();
////                            orm.insertCircleList(userCircles, AccountServiceUtils.getBorqsAccountID());
////                        }
//                        dogetUserCircleCallBack(true);
//                        synchronized (depLock) {
//                        	inloadingDep = false;
//                        }
//                    }
//
//                    public void onException(TwitterException ex,
//                            TwitterMethod method) {
//                        TwitterExceptionUtils.printException(TAG,
//                                "getUserCircle, server exception:", ex, method);
//                        synchronized (depLock) {
//                        	inloadingDep = false;
//                        }
//                        dogetUserCircleCallBack(false);
//                    }
//                });
//    }
    
    
    boolean inloadingChildCircle = false;
    Object loadingLock = new Object();

    private void getChildCircle() {
        synchronized (loadingLock) {
            if (inloadingChildCircle == true) {
                Log.d(TAG, "in doing syncCircleInfo data");
                return;
            }
        }

        synchronized (loadingLock) {
        	inloadingChildCircle = true;
        }

        begin();
        asyncQiupu.syncChildCircles(AccountServiceUtils.getSessionID(), mCircleId, mFormal,
                new TwitterAdapter() {
                    public void syncChildCircles(ArrayList<UserCircle> userCircles) {
                    	Log.d(TAG, "finish syncChildCircles= " + userCircles.size());
                    	if(mCircleList == null) return;
                    	mCircleList.clear();
                    	mCircleList.addAll(userCircles);
                        
//                        if (userCircles.size() > 0) {
//                            orm.removeAllCirclesWithOutNativeCircles();
//                            orm.insertCircleList(userCircles, AccountServiceUtils.getBorqsAccountID());
//                        }
                        dogetUserCircleCallBack(true);
                        synchronized (loadingLock) {
                        	inloadingChildCircle = false;
                        }
                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        TwitterExceptionUtils.printException(TAG,
                                "getUserCircle, server exception:", ex, method);
                        synchronized (loadingLock) {
                        	inloadingChildCircle = false;
                        }
                        dogetUserCircleCallBack(false);
                    }
                });
    }
    private void dogetUserCircleCallBack(boolean suc) {
        Message msg = mHandler.obtainMessage(GET_CIRCLE_END);
        msg.getData().putBoolean(RESULT, suc);
        msg.sendToTarget();
    }
    


    @Override
    protected void onDestroy() {
        super.onDestroy();
        QiupuHelper.unregisterUserListener(getClass().getName());
    }

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.friends_circle);
//    }
}