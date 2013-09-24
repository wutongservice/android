package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.QiupuUser;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.adapter.EventListAdapter;
import com.borqs.common.listener.UsersActionListner;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.common.view.EventItemView;
import com.borqs.common.view.TwoWayAdapterView;
import com.borqs.common.view.TwoWayAdapterView.OnItemClickListener;
import com.borqs.common.view.TwoWayGridView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.service.FriendsManager;
import com.borqs.qiupu.service.QiupuService;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.ui.circle.EditPublicCircleActivity;
import com.borqs.qiupu.ui.circle.EventDetailActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class EventListActivity extends BasicNavigationActivity implements UsersActionListner, OnItemClickListener, FriendsManager.FriendsServiceListener {

    private final static String TAG = "EventListActivity";
    private TwoWayGridView mListView;
    private TextView mToastTextView;
    private EventListAdapter mEventAdapter;
    private static final int TYPE_UPCOMING = 1;
    private static final int TYPE_PAST = 2;
    private int mCurrentScreen;
    private Spinner mSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLeftNav();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list_main);
        setHeadTitle(R.string.event_title_upcoming);
//        showLeftActionBtn(true);
//        showRightActionBtn(true);
//        showTitleSpinnerIcon(true);
        overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, editProfileClick);
//        overrideRightActionBtn(R.drawable.home_screen_create_event_icon, addEventListener);
        mListView  = (TwoWayGridView) findViewById(R.id.gridview);
        mToastTextView = (TextView) findViewById(R.id.toast_tv);
        mEventAdapter = new EventListAdapter(this);
        mListView.setSelector(R.drawable.list_selector_background);
        mListView.setAdapter(mEventAdapter);
        mListView.setOnItemClickListener(this);
        
        mSpinner = (Spinner) findViewById(R.id.event_category_spinner);
        buildEventCategoryList();
        
        onConfigurationChanged(getResources().getConfiguration());
        
        mCurrentScreen = TYPE_UPCOMING;
        refreshCircleList(mCurrentScreen);

        if(QiupuService.mFriendsManager != null && QiupuService.mFriendsManager.getLoadStatus(FriendsManager.SYNC_TYPE_EVENTS)) {
        	begin();
        }
        FriendsManager.registerFriendsServiceListener(getClass().getName(), this);
        QiupuHelper.registerUserListener(getClass().getName(), this);
        mHandler.obtainMessage(SYNC_EVENT).sendToTarget();
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadRefresh() {
    	mHandler.obtainMessage(SYNC_EVENT).sendToTarget();
    }
    
    private final int SYNC_EVENT = 101;
    private final int SYNC_EVNET_END = 102;
    private final int SYNC_EVENTS_CALLBACK = 103;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SYNC_EVENT: {
            	syncEventInfo("", false);
                break;
            }
            case SYNC_EVNET_END: {
            	end();
            	if(msg.getData().getBoolean(RESULT)) {
            		refreshCircleList(mCurrentScreen);
            	}else {
            		ToastUtil.showOperationFailed(EventListActivity.this, mHandler, false);
            	}
            	break;
            }
            case SYNC_EVENTS_CALLBACK: {
            	if(msg.getData().getInt(FriendsManager.SYNC_TYPE) != FriendsManager.SYNC_TYPE_EVENTS) {
            		return ;
            	}
                int statuscode = msg.getData().getInt("statuscode");
                if (statuscode == FriendsManager.STATUS_DO_FAIL) {
                    end();
                } else if (statuscode == FriendsManager.STATUS_DOING) {
                    begin();
                } else if (statuscode == FriendsManager.STATUS_DO_OK) {
                    end();
                    refreshCircleList(mCurrentScreen);
                } else if (statuscode == FriendsManager.STATUS_ITERATING) {
                	refreshCircleList(mCurrentScreen);
                }
                break;
            }
            }
        }
    }
    
    @Override
    protected void onDestroy() {
    	QiupuHelper.unregisterUserListener(getClass().getName());
    	FriendsManager.unregisterFriendsServiceListener(getClass().getName());
    	mEventAdapter.clearCursor();
        super.onDestroy();
    }
    
    boolean inLoadingEvent;
    Object mLockSyncEventInfo = new Object();
    public void syncEventInfo(final String circleId, final boolean with_member) {
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
    	
    	asyncQiupu.syncEventInfo(AccountServiceUtils.getSessionID(), circleId, with_member, new TwitterAdapter() {
    		public void syncEventInfo(ArrayList<UserCircle> circles) {
    			Log.d(TAG, "finish syncEventInfo=" + circles.size());
    			
    			if (circles.size() > 0) {
//    				orm.insertEventList(circles);
    				orm.insertEventsList(EventListActivity.this, circles);
                }
    			
    			Message msg = mHandler.obtainMessage(SYNC_EVNET_END);
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
    			Message msg = mHandler.obtainMessage(SYNC_EVNET_END);
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
    
	private void refreshCircleList(int currenttype) {
		Cursor events = null;
		if(currenttype == TYPE_UPCOMING) {
			events = orm.queryUpcomingEvents();
			if (events != null)
				Log.d(TAG, "TYPE_UPCOMING events.getCount() = " +events.getCount());
	        mEventAdapter.alterCircles(events);
		}else if(currenttype == TYPE_PAST) {
			events = orm.queryPastEvents();
			if (events != null)
				Log.d(TAG, "TYPE_PAST events.getCount() = " +events.getCount());
	        mEventAdapter.alterCircles(events);
		}
		
        if(events != null && events.getCount() > 0) {
        	mToastTextView.setVisibility(View.GONE);
        	mListView.setVisibility(View.VISIBLE);
        }else {
        	mToastTextView.setVisibility(View.VISIBLE);
        	mListView.setVisibility(View.GONE);
        }
    } 

    @Override
	public void updateItemUI(QiupuUser user) {
		refreshCircleList(mCurrentScreen);
	}

	@Override
	public void addFriends(QiupuUser user) {
	}

	@Override
	public void refuseUser(long uid) {
	}

	@Override
	public void deleteUser(QiupuUser userc) {
	}

	@Override
	public void sendRequest(QiupuUser user) {
	}
	
//	 View.OnClickListener addEventListener = new OnClickListener() {
//	        public void onClick(View v) {
//	        	//TODO start create event
//	        	IntentUtil.gotoCreateEventActivity(EventListActivity.this, EditPublicCircleActivity.type_create_event, null, null);
//	        }
//	    };
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
	
	@Override
	protected void showCorpusSelectionDialog(View view) {
		int location[] = new int[2];
        view.getLocationInWindow(location);
        int x = location[0];
        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);

        ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        items.add(new SelectionItem("", getString(R.string.event_title_upcoming)));
        items.add(new SelectionItem("", getString(R.string.event_title_past)));
        DialogUtils.showCorpusSelectionDialog(this, x, y, items, circleListItemClickListener);
	}
	
	AdapterView.OnItemClickListener circleListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                setHeadTitle(item.getText());
                onCorpusSelected(item.getText());
            }
        }
    };
    
    private void onCorpusSelected(String value) {
    	if(getString(R.string.event_title_upcoming).equals(value)) {
    		setHeadTitle(R.string.event_title_upcoming);
    		mCurrentScreen = TYPE_UPCOMING;
    		refreshCircleList(mCurrentScreen);
    	}else if(getString(R.string.event_title_past).equals(value)) {
    		setHeadTitle(R.string.event_title_past);
    		mCurrentScreen = TYPE_PAST;
    		refreshCircleList(mCurrentScreen);
    	}else if(getString(R.string.label_refresh).equals(value)){ 
    		loadRefresh();
    	}else if(getString(R.string.create_new_event_title).equals(value)){
    		IntentUtil.gotoCreateEventActivity(EventListActivity.this, EditPublicCircleActivity.type_create_event, null, null);
    	}else {
    		Log.d(TAG, "more drop down items " + value);
    	}
    }

	@Override
	public void updateUI(int msgcode, Message message) {
		if (QiupuConfig.LOGD) Log.d(TAG, "msgcode: " + msgcode + " message: " + message);
        Message msg = mHandler.obtainMessage(SYNC_EVENTS_CALLBACK);
        msg.getData().putInt("statuscode", msgcode);
        if(message != null) {
        	msg.getData().putInt(FriendsManager.SYNC_TYPE, message.getData().getInt(FriendsManager.SYNC_TYPE));
        }
        msg.sendToTarget();
	}
	
	View.OnClickListener editProfileClick = new View.OnClickListener() {
        public void onClick(View v) {
        	ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
        	items.add(new SelectionItem("", getString(R.string.label_refresh)));
        	items.add(new SelectionItem("", getString(R.string.create_new_event_title)));
        	
        	showCorpusSelectionDialog(items);
        }
    };
    
    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
	    if(mRightActionBtn != null) {
	        int location[] = new int[2];
	        mRightActionBtn.getLocationInWindow(location);
	        int x = location[0];
	        int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
	        
	        DialogUtils.showCorpusSelectionDialog(this, x, y, items, actionListItemClickListener);
	    }
	}
    
    AdapterView.OnItemClickListener actionListItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());             
            }
        }
    };
    
    private void buildEventCategoryList() {
    	final String[] adapterValue = new String[]{getString(R.string.event_title_upcoming), getString(R.string.event_title_past)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.event_spinner_textview, adapterValue);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(0);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View view,
        			int position, long id) {
        		onCorpusSelected(adapterValue[position]);
        	}
        	
        	@Override
        	public void onNothingSelected(AdapterView<?> parent) {
        	}
        });
    }
}