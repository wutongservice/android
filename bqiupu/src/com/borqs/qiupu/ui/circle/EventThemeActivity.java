package com.borqs.qiupu.ui.circle;

import java.util.ArrayList;

import twitter4j.EventTheme;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.EventThemeListAdapter;
import com.borqs.common.view.EventThemeItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ToastUtil;

public class EventThemeActivity extends BasicActivity {

	private static final String TAG = "EventThemeActivity";
	private ListView mListView;
	private EventThemeListAdapter mAdapter;
	private int mPage = 0;
	private int mCount = 100;
	public static final String SELECT_THEME_URL = "themeUrl";
	public static final String SELECT_THEME_ID = "themeId";
	@Override
	protected void createHandler() {
		mHandler = new MainHandler();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_theme_ui);
		setHeadTitle(R.string.select_theme);
		showRightActionBtn(false);
		showLeftActionBtn(true);
		mListView = (ListView) findViewById(R.id.default_listview);
		mAdapter = new EventThemeListAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(themeItemClickListener);
		Cursor cursor = orm.queryAllThemes();
		if(cursor != null && cursor.getCount() > 1) { // maybe sync one theme from create event ui
			mAdapter.alterDataList(cursor);
//		}else {
		}
		mHandler.obtainMessage(SYNC_THEME).sendToTarget();// always sync theme list to check themes changed.
	}
	
	@Override
	protected void loadRefresh() {
		mHandler.obtainMessage(SYNC_THEME).sendToTarget();
	}
	
	private final static int SYNC_THEME = 101;
	private final static int SYNC_EVNET_THEME_END = 102;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SYNC_THEME: {
            	syncEventInfo(mPage, mCount);
                break;
            }
            case SYNC_EVNET_THEME_END: {
            	end();
            	if(msg.getData().getBoolean(RESULT)) {
            		mAdapter.alterDataList(orm.queryAllThemes());
            	}else {
            		Log.d(TAG, "sync event themes failed");
            	}
            	break;
            }
            }
        }
    }
    
    boolean inLoadingTheme;
    Object mLockSyncEventTheme = new Object();
    public void syncEventInfo(final int page, final int count) {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inLoadingTheme == true) {
    		ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockSyncEventTheme) {
    		inLoadingTheme = true;
    	}
    	begin();
    	
    	asyncQiupu.syncThemes(AccountServiceUtils.getSessionID(), page, count, new TwitterAdapter() {
    		public void syncEventThemes(ArrayList<EventTheme> themes) {
    			Log.d(TAG, "finish syncEventThemes=" + themes.size());
    			
    			if (themes.size() > 0) {
    				orm.insertEventThemes(themes);
                }
    			
    			Message msg = mHandler.obtainMessage(SYNC_EVNET_THEME_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    			synchronized (mLockSyncEventTheme) {
    				inLoadingTheme = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockSyncEventTheme) {
    		    	inLoadingTheme = false;
    			}
    			Message msg = mHandler.obtainMessage(SYNC_EVNET_THEME_END);
    			msg.getData().putBoolean(RESULT, false);
    			msg.sendToTarget();
    		}
    	});
    }
    
    OnItemClickListener themeItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(EventThemeItemView.class.isInstance(view)) {
				EventThemeItemView item = (EventThemeItemView) view;
				EventTheme theme = item.getItem();
                if(theme != null) {
                	Intent data = new Intent();
        			data.putExtra(SELECT_THEME_URL, theme.image_url);
        			data.putExtra(SELECT_THEME_ID, theme.id);
        			EventThemeActivity.this.setResult(Activity.RESULT_OK, data);
        			finish();
                }else {
                    Log.d(TAG, "get circle is null.");
                }
            }
		}
	};
}
