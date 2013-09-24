package com.borqs.common.view;

import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.EventSimpleListAdapter;
import com.borqs.common.util.UserTask;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshBase.OnRefreshListener;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshListView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.circle.EventDetailActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class StreamRightEventListViewUi {
	private final static String TAG = StreamRightEventListViewUi.class
			.getName();
	private ListView mListView;
	private PullToRefreshListView mPullView;
	private EventSimpleListAdapter mEventAdapter;
	private long mCircleId;
	private Handler mHandler;
	private ArrayList<UserCircle> mCircleEvents = new ArrayList<UserCircle>();
	protected String RESULT = "result";
	protected Context mContext;
	protected AsyncQiupu mAsyncQiupu;
	private int mPage = 0;
    private int mCount = 200;
    private QiupuORM mOrm;
    private boolean mAlreadyLoad;

	public StreamRightEventListViewUi() {
	}

	public void init(Context con, PullToRefreshListView listView, long circleid) {
		mContext = con;
		mHandler = new MainHandler();
		mAsyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mOrm = QiupuORM.getInstance(con);
		
		mPullView = listView;
		mListView = mPullView.getRefreshableView();
		
		mEventAdapter = new EventSimpleListAdapter(con);
		mListView.setAdapter(mEventAdapter);
		mCircleId = circleid;
		mListView.setOnItemClickListener(mItemClickListener);
		
		mPullView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadRefresh();
			}
		});
		
		if(mCircleId > 0) {
        	mCircleEvents = mOrm.queryCircleEvents(mCircleId);
        	if(mCircleEvents != null && mCircleEvents.size() > 0) {
        		Log.d(TAG, "local have circle event");
        		mEventAdapter.alterCircles(mCircleEvents);
//        	}else {
        		
        	}
        	
        	mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mHandler.obtainMessage(SYNC_CIRCLE_EVENT).sendToTarget();
				}
			}, 2000);
        }else {
        	Log.d(TAG, "circleid is null");
        }
		
	}

	private void loadRefresh() {
		mHandler.obtainMessage(SYNC_CIRCLE_EVENT).sendToTarget();
	}

	private final int SYNC_CIRCLE_EVENT = 101;
    private final int SYNC_CIRCLE_EVNET_END = 102;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SYNC_CIRCLE_EVENT: {
            	syncCircleEventInfo(mCircleId, false);
                break;
            }
            case SYNC_CIRCLE_EVNET_END: {
            	if(mPullView != null) {
            		mPullView.onRefreshComplete();
            	}
            	if(msg.getData().getBoolean(RESULT)) {
            		mAlreadyLoad = true;
            		mEventAdapter.alterCircles(mCircleEvents);
            	}else {
            		ToastUtil.showOperationFailed(mContext, mHandler, false);
            	}
            	break;
            }
            }
        }
    }

	boolean inLoadingEvent;
    Object mLockSyncEventInfo = new Object();
    public void syncCircleEventInfo(final long circleId, final boolean with_member) {
        if (!ToastUtil.testValidConnectivity(mContext)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inLoadingEvent == true) {
    		ToastUtil.showShortToast(mContext, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockSyncEventInfo) {
    		inLoadingEvent = true;
    	}
    	
    	mAsyncQiupu.syncCircleEventInfo(AccountServiceUtils.getSessionID(), circleId, with_member, mPage, mCount, new TwitterAdapter() {
    		public void syncCircleEventInfo(ArrayList<UserCircle> circles) {
    			Log.d(TAG, "finish syncCircleEventInfo=" + circles.size());
    			
    			if(mCircleEvents == null) {
    				Log.e(TAG, "circle events array is null ");
    				return ;
    			}
    			
    			if(mPage == 0) {
    				mOrm.deleteCircleEvents(circleId);
    				mCircleEvents.clear();
    				mCircleEvents.addAll(circles);
    			}else {
    				mCircleEvents.addAll(circles);
    			}
    			
    			// insert all circle event;
    			mOrm.insertCircleEvents(circleId, mCircleEvents);
//    			for(int i= 0; i<circles.size(); i++) {
//    				mOrm.insertOneCircleEvent(circleId, circles.get(i));
//    			}
    			
    			Message msg = mHandler.obtainMessage(SYNC_CIRCLE_EVNET_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    			synchronized (mLockSyncEventInfo) {
    				inLoadingEvent = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockSyncEventInfo) {
    		    	inLoadingEvent = false;
    			}
    			Message msg = mHandler.obtainMessage(SYNC_CIRCLE_EVNET_END);
    			msg.getData().putBoolean(RESULT, false);
    			msg.sendToTarget();
    		}
    	});
    }

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
        	if(EventItemView.class.isInstance(view)) {
    			EventItemView item = (EventItemView) view;
                UserCircle circle = item.getItemView();
                if(circle != null) {
                	final Intent intent = new Intent(mContext, EventDetailActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putString(CircleUtils.CIRCLE_NAME,
                            CircleUtils.getLocalCircleName(mContext, circle.circleid, circle.name));
                    bundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                    }
                }else {
                    Log.d(TAG, "get circle is null.");
                }
        }
    };
    
    public void onDestory() {
    	mEventAdapter.clearCursor();
//    	insertMax20CircleEvents();
    }
    
    private void insertMax20CircleEvents() {
		if(mCircleId > 0 && mCircleEvents != null && mCircleEvents.size() > 0) {
			new insertCircleEventsTask(mCircleId).execute();
			
		}else {
			Log.d(TAG, "insertMax20 failed " + mCircleId);
		}
	}
    
    private class insertCircleEventsTask extends UserTask<Long, Void, Void> {
    	
    	private long mid;
    	
    	public insertCircleEventsTask(long circleid) {
    		this.mid = circleid;
    	}
    	
    	@Override
    	public Void doInBackground(Long... params) {
    		int count = 0;
    		if(mCircleEvents.size() >= mCount) {
    			count = mCount;
    		}else {
    			count = mCircleEvents.size();
    		}
    		mOrm.deleteCircleEvents(mid);
    		for(int i= 0; i<count; i++) {
    			mOrm.insertOneCircleEvent(mid, mCircleEvents.get(i));
    		}
    		return null;
    	}
    	
    	@Override
    	public void onPostExecute(Void result) {
    		super.onPostExecute(result);
    	}
    }

	public void loadDataOnMove() {
		if(mCircleEvents != null && mCircleEvents.size() <= 0 && !mAlreadyLoad) {
    		mHandler.obtainMessage(SYNC_CIRCLE_EVENT).sendToTarget();
    	}else {
    		if(QiupuConfig.LOGD)Log.d(TAG, "loadDataOnMove, do nothing.");
    	}
	}
}
