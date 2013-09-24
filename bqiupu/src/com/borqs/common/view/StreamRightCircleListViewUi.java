package com.borqs.common.view;

import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.Circletemplate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.DepartmentExpnadleAdapter;
import com.borqs.common.listener.RefreshCircleListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshBase.OnRefreshListener;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshExpandableListView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.ToastUtil;

public class StreamRightCircleListViewUi implements RefreshCircleListener{
	private final static String TAG = StreamRightCircleListViewUi.class
			.getName();
	private DepartmentExpnadleAdapter mAdapter;
	private ExpandableListView mListView;
	private PullToRefreshExpandableListView mPullView;
	private Handler mHandler;
//	private ArrayList<UserCircle> mCircleList = new ArrayList<UserCircle>();
//	private ArrayList<UserCircle> mSearchCircleList = new ArrayList<UserCircle>();
	protected String RESULT = "result";
	protected Context mContext;
	protected AsyncQiupu mAsyncQiupu;
	private QiupuORM mOrm;
	private UserCircle mCircle;
	
	private Cursor mReferCircleCursor;
    private Cursor mFirstCursor;
    private Cursor mSecondCirclesCursor;
    private Cursor mThirdCirclesCursor;
    private Cursor mFreeCirclesCursor;
    
    private Cursor mSearchReferCircleCursor;
    private Cursor mSearchFirstCursor;
    private Cursor mSearchSecondCirclesCursor;
    private Cursor mSearchThirdCirclesCursor;
    private Cursor mSearchFreeCirclesCursor;

	public StreamRightCircleListViewUi() {
	}

	public void init(Context con, PullToRefreshExpandableListView pullView, UserCircle circle) {
		mContext = con;
		mCircle = circle;

		mHandler = new MainHandler();
		mAsyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mOrm = QiupuORM.getInstance(con);
		QiupuHelper.registerRefreshCircleListener(getClass().getName(), this);
		
		if(mCircle != null && mCircle.mGroup != null) {
			mAdapter = new DepartmentExpnadleAdapter(con, mCircle.mGroup.subtype, mItemClickListener);
		}else {
			mAdapter = new DepartmentExpnadleAdapter(con, Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY, mItemClickListener);// default init for company
		}
		mPullView = pullView;
		mListView = pullView.getRefreshableView();
		mListView.setGroupIndicator(null);
		mListView.setDivider(null);
		pullView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadRefresh();
			}
		});
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				refreshUi();
				mListView.setAdapter(mAdapter);
				if(mAdapter.getGroupCount() > 0) {
					mListView.expandGroup(0);
				}
				gotoSyncCircleCircles();
			}
		}, 1000);
	}

	// sync circle's circle a day.
	private void gotoSyncCircleCircles() {
		final long lastTime = mOrm.getCircleCirclesLastSyncTime(mCircle.circleid);
		if (lastTime <= 0 || (System.currentTimeMillis() - lastTime - QiupuConfig.A_DAY) > 0) {
			mHandler.obtainMessage(GET_CIRCLES).sendToTarget();
		}
	}
	
	private void loadRefresh() {
		mHandler.obtainMessage(GET_CIRCLES).sendToTarget();
	}

	private final int GET_CIRCLES = 1;
	private final int GET_CIRCLES_END = 2;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_CIRCLES: {
				getChildCircle();
				break;
			}
			case GET_CIRCLES_END: {
				if (mPullView != null) {
					mPullView.onRefreshComplete();
				}
				if (msg.getData().getBoolean(RESULT)) {
					refreshUi();
				} else {
					ToastUtil.showOperationFailed(mContext, mHandler, false);
				}
				break;
			}
			}
		}
	}

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

		mPullView.setRefreshing();
		mAsyncQiupu.syncChildCircles(AccountServiceUtils.getSessionID(),
				mCircle.circleid, -1, new TwitterAdapter() {
					public void syncChildCircles(ArrayList<UserCircle> userCircles) {
						Log.d(TAG, "finish syncChildCircles= " + userCircles.size());
						if(userCircles != null) {
							// first remove all child circle, then insert all circles;
							mOrm.removeCircleCircles(mCircle.circleid);
							mOrm.insertCirclesByCircle(mCircle.circleid, userCircles);
							mOrm.setCircleCirclesLastSyncTime(mCircle.circleid);
						}

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
		Message msg = mHandler.obtainMessage(GET_CIRCLES_END);
		msg.getData().putBoolean(RESULT, suc);
		msg.sendToTarget();
	}
	
	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.d(TAG, "itemClickListener:  " + position);
			if (DepartmentItemView.class.isInstance(view)) {
				Log.d(TAG, "itemClickListener DepartmentItemView:  " + position);
				DepartmentItemView civ = (DepartmentItemView) view;
				if(civ.getDataInfo() != null)
				{
					IntentUtil.startPublicCircleDetailIntent(mContext, civ.getDataInfo());
				}
			} else {
				Log.d(TAG, "mItemClickListener error, view = " + view);
			}
		}
	};

	public void doSearch(String newText) {
		if(TextUtils.isEmpty(newText)) {
			refreshUi();
		}else {
			if(mCircle.mGroup != null) {
				String subtype = mCircle.mGroup.subtype;
				if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(subtype)) {
					mSearchFirstCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_DEPARTMENT, newText);
					mSearchSecondCirclesCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_PROJECT, newText);
					mSearchThirdCirclesCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_APPLICATION, newText);
					mSearchFreeCirclesCursor = mOrm.queryCircleFreeCircles(mCircle.circleid, newText);
				}else if(Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL.equals(subtype)) {
					mSearchFirstCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_CLASS, newText);
					mSearchFreeCirclesCursor = mOrm.queryCircleFreeCircles(mCircle.circleid, newText);
				}else {
					Log.i(TAG, "the group subtype have no handle" + mCircle.mGroup.subtype);
				}
			}else {
				Log.i(TAG, "circle's group is null");
			}
			mAdapter.alterDataList(mSearchReferCircleCursor, mSearchFreeCirclesCursor, mSearchFirstCursor, mSearchSecondCirclesCursor, mSearchThirdCirclesCursor);
		}
	}

	@Override
	public void refreshUi() {
		if(mCircle.mGroup != null) {
			String subtype = mCircle.mGroup.subtype;
			if(Circletemplate.SUBTYPE_TEMPLATEFORMALCOMPANY.equals(subtype)) {
				mFirstCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_DEPARTMENT, null);
				mSecondCirclesCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_PROJECT, null);
				mThirdCirclesCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_APPLICATION, null);
				mFreeCirclesCursor = mOrm.queryCircleFreeCircles(mCircle.circleid, null);
			}else if(Circletemplate.SUBTYPE_TEMPLATEFORMALSCHOOL.equals(subtype)) {
				mFirstCursor = mOrm.queryCircleCircles(mCircle.circleid, Circletemplate.TEMPLATE_NAME_CLASS, null);
				mFreeCirclesCursor = mOrm.queryCircleFreeCircles(mCircle.circleid, null);
			}else {
				Log.i(TAG, "the group subtype have no handle" + mCircle.mGroup.subtype);
			}
		}else {
			Log.i(TAG, "circle's group is null");
		}
		mAdapter.alterDataList(mReferCircleCursor, mFreeCirclesCursor, mFirstCursor, mSecondCirclesCursor, mThirdCirclesCursor);
	}

	public void onDestory() {
		if(mAdapter != null) {
			mAdapter.closeAllCursor();
		}
		QiupuHelper.unregisterRefreshCircleListener(getClass().getName());
	}
}
