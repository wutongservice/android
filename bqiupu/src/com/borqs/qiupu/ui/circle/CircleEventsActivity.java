package com.borqs.qiupu.ui.circle;

import java.util.ArrayList;
import java.util.HashMap;

import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.EventListAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.UserTask;
import com.borqs.common.view.EventItemView;
import com.borqs.common.view.TwoWayAdapterView;
import com.borqs.common.view.TwoWayAdapterView.OnItemClickListener;
import com.borqs.common.view.TwoWayGridView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class CircleEventsActivity extends BasicActivity implements OnItemClickListener {

    private final static String TAG = "CircleEventsActivity";
    private TwoWayGridView mListView;
    private TextView mToastTextView;
    private EventListAdapter mEventAdapter;
    private int mPage = 0;
    private int mCount = 200;
    private long mCircleId;
    private String mCircleName;
    private ArrayList<UserCircle> mCircleEvents = new ArrayList<UserCircle>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list_main);
        mCircleId = getIntent().getLongExtra(CircleUtils.CIRCLE_ID, -1);
        mCircleName = getIntent().getStringExtra(CircleUtils.CIRCLE_NAME);
        
        if(StringUtil.isValidString(mCircleName)) {
        	setHeadTitle(String.format(getString(R.string.whose_event), mCircleName));
        }else {
        	setHeadTitle(R.string.event);
        }
        
        showLeftActionBtn(true);
        showRightActionBtn(true);
        overrideRightActionBtn(R.drawable.home_screen_create_event_icon, addEventListener);
        mListView  = (TwoWayGridView) findViewById(R.id.gridview);
        mToastTextView = (TextView) findViewById(R.id.toast_tv);
        mEventAdapter = new EventListAdapter(this);
        mListView.setSelector(R.drawable.list_selector_background);
        mListView.setAdapter(mEventAdapter);
        mListView.setOnItemClickListener(this);
        
        onConfigurationChanged(getResources().getConfiguration());
        
        if(mCircleId > 0) {
        	mCircleEvents = orm.queryCircleEvents(mCircleId);
        	refreshEventList();
        }else {
        	Log.d(TAG, "circleid is null");
        }

        mHandler.obtainMessage(SYNC_CIRCLE_EVENT).sendToTarget();
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadRefresh() {
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
            	end();
            	if(msg.getData().getBoolean(RESULT)) {
            		refreshEventList();
            	}else {
            		ToastUtil.showOperationFailed(CircleEventsActivity.this, mHandler, false);
            	}
            	break;
            }
            }
        }
    }
    
    @Override
    protected void onDestroy() {
    	mEventAdapter.clearCursor();
        super.onDestroy();
        insertMax20CircleEvents();
    }
    
    boolean inLoadingEvent;
    Object mLockSyncEventInfo = new Object();
    public void syncCircleEventInfo(final long circleId, final boolean with_member) {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inLoadingEvent == true) {
    		ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockSyncEventInfo) {
    		inLoadingEvent = true;
    	}
    	begin();
    	
    	asyncQiupu.syncCircleEventInfo(AccountServiceUtils.getSessionID(), circleId, with_member, mPage, mCount, new TwitterAdapter() {
    		public void syncCircleEventInfo(ArrayList<UserCircle> circles) {
    			Log.d(TAG, "finish syncCircleEventInfo=" + circles.size());
    			
    			if(mPage == 0) {
    				orm.deleteCircleEvents(circleId);
    				if(mCircleEvents != null) {
        				mCircleEvents.clear();
        				mCircleEvents.addAll(circles);
        			}	
    			}else {
    				if(mCircleEvents != null) {
        				mCircleEvents.addAll(circles);
        			}
    			}
    			
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

//    @Override
//    public void setLeftMenuPosition() {
//        mPosition = LeftMenuMapping.getPositionForActivity(this);
//        mTitle = getString(R.string.event);
//    }
    
	private void refreshEventList() {
//		Cursor events = null;
//		if(currenttype == TYPE_UPCOMING) {
//			events = orm.queryUpcomingEvents();
//			if (events != null)
//				Log.d(TAG, "TYPE_UPCOMING events.getCount() = " +events.getCount());
//	        mEventAdapter.alterCircles(events);
//		}else if(currenttype == TYPE_PAST) {
//			events = orm.queryPastEvents();
//			if (events != null)
//				Log.d(TAG, "TYPE_PAST events.getCount() = " +events.getCount());
//	        mEventAdapter.alterCircles(events);
//		}
//		
//        if(events != null && events.getCount() > 0) {
//        	mToastTextView.setVisibility(View.GONE);
//        	mListView.setVisibility(View.VISIBLE);
//        }else {
//        	mToastTextView.setVisibility(View.VISIBLE);
//        	mListView.setVisibility(View.GONE);
//        }
		
		mEventAdapter.alterCircles(mCircleEvents);
    } 
	
	@Override
	public void onItemClick(TwoWayAdapterView<?> parent, View view,
			int position, long id) {
		if(EventItemView.class.isInstance(view)) {
			EventItemView item = (EventItemView) view;
            UserCircle circle = item.getItemView();
            if(circle != null) {
            	final Intent intent = new Intent(this, EventDetailActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(CircleUtils.CIRCLE_NAME,
                        CircleUtils.getLocalCircleName(this, circle.circleid, circle.name));
                bundle.putLong(CircleUtils.CIRCLE_ID, circle.circleid);
                intent.putExtras(bundle);
                startActivity(intent);
//            	IntentUtil.startPublicCircleDetailIntent(EventListActivity.this, circle);
                }
            }else {
                Log.d(TAG, "get circle is null.");
            }
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Configuration conf = getResources().getConfiguration();
		if(conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mListView.setNumRows(1);
		} else {
			mListView.setNumColumns(1);
		}
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
			for(int i= 0; i<count; i++) {
				orm.insertOneCircleEvent(mid, mCircleEvents.get(i));
			}
			return null;
		}
		
		@Override
		public void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
		
	}
	
	 View.OnClickListener addEventListener = new OnClickListener() {
	        public void onClick(View v) {
	        	//TODO start create event
	            HashMap<String, String> receiverMap = new HashMap<String, String>();
	            receiverMap.put(String.valueOf(mCircleId), mCircleName);
	            String receiverId = "";
	            if(mCircleId > 0 && (QiupuConfig.isEventIds(mCircleId) || QiupuConfig.isPublicCircleProfile(mCircleId))) {
	            	receiverId = "#" + mCircleId;
	            }else if(mCircleId > 0 && QiupuConfig.isPageId(mCircleId)) {
	            	receiverId = String.valueOf(mCircleId);
	            }
	        	IntentUtil.gotoCreateEventActivity(CircleEventsActivity.this, EditPublicCircleActivity.type_create_event, receiverMap, receiverId, mCircleId, -1);
	        }
	    };
}