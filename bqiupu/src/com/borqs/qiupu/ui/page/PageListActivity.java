package com.borqs.qiupu.ui.page;

import java.util.ArrayList;

import twitter4j.PageInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.PageListAdapter;
import com.borqs.common.listener.PageActionListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.PageListItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicNavigationActivity;
import com.borqs.qiupu.util.ToastUtil;

public class PageListActivity extends BasicNavigationActivity implements OnItemClickListener, PageActionListener {

    private final static String TAG = "PageListActivity";
    private ListView mListView;
    private TextView mToastTextView;
    private PageListAdapter mPageAdapter;
//    private ArrayList<PageInfo> mPageList = new ArrayList<PageInfo>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableLeftNav();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_list_main);
        setHeadTitle(R.string.page_label);
        showLeftActionBtn(true);
        showRightActionBtn(true);
        showMiddleActionBtn(true);
        overrideMiddleActionBtn(R.drawable.create_page_icon, addPageListener);
        QiupuHelper.registerPageListener(getClass().getName(), this);
        mListView  = (ListView) findViewById(R.id.default_listview);
        mToastTextView = (TextView) findViewById(R.id.toast_tv);
        mPageAdapter = new PageListAdapter(this);
        mListView.setSelector(R.drawable.list_selector_background);
        mListView.setAdapter(mPageAdapter);
        mListView.setOnItemClickListener(this);
        
//        onConfigurationChanged(getResources().getConfiguration());
        
        refreshPageList();

        mHandler.obtainMessage(SYNC_PAGE).sendToTarget();
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    @Override
    protected void loadRefresh() {
    	mHandler.obtainMessage(SYNC_PAGE).sendToTarget();
    }
    
    @Override
    protected void loadSearch() {
    	Intent intent = new Intent(this, PageSearchActivity.class) ;
    	startActivity(intent);
    }
    
    private final int SYNC_PAGE = 101;
    private final int SYNC_PAGE_END = 102;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SYNC_PAGE: {
            	syncPageInfo("");
                break;
            }
            case SYNC_PAGE_END: {
            	end();
            	if(msg.getData().getBoolean(RESULT)) {
            		refreshPageList();
            	}else {
            		ToastUtil.showOperationFailed(PageListActivity.this, mHandler, false);
            	}
            	break;
            }
            }
        }
    }
    
    @Override
    protected void onDestroy() {
    	QiupuHelper.unregisterPageListener(getClass().getName());
    	mPageAdapter.clearCursor();
        super.onDestroy();
    }
    
    boolean inLoadingPage;
    Object mLockSyncPageInfo = new Object();
    public void syncPageInfo(final String pageIds) {
        if (!ToastUtil.testValidConnectivity(this)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inLoadingPage == true) {
    		ToastUtil.showShortToast(this, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockSyncPageInfo) {
    		inLoadingPage = true;
    	}
    	begin();
    	
    	asyncQiupu.syncPageList(AccountServiceUtils.getSessionID(), pageIds, new TwitterAdapter() {
    		public void syncPageList(ArrayList<PageInfo> pages) {
    			Log.d(TAG, "finish syncEventInfo=" + pages.size());
    			
    			if (pages.size() > 0) {
    				orm.insertPageList(pages);
                }
    			
    			Message msg = mHandler.obtainMessage(SYNC_PAGE_END);
    			msg.getData().putBoolean(RESULT, true);
    			msg.sendToTarget();
    			synchronized (mLockSyncPageInfo) {
    				inLoadingPage = false;
    			}
    		}
    		
    		public void onException(TwitterException ex, TwitterMethod method) {
    		    synchronized (mLockSyncPageInfo) {
    		    	inLoadingPage = false;
    			}
    			Message msg = mHandler.obtainMessage(SYNC_PAGE_END);
    			msg.getData().putBoolean(RESULT, false);
    			msg.sendToTarget();
    		}
    	});
    }

	private void refreshPageList() {
		Cursor pages = null;
		pages = orm.queryAllSimplePages();
			if (pages != null) {
				Log.d(TAG, "TYPE_UPCOMING events.getCount() = " +pages.getCount());
			mPageAdapter.alterPages(pages);
		}
		
        if(pages != null && pages.getCount() > 0) {
        	mToastTextView.setVisibility(View.GONE);
        	mListView.setVisibility(View.VISIBLE);
        }else {
        	mToastTextView.setVisibility(View.VISIBLE);
        	mListView.setVisibility(View.GONE);
        }
    } 

	 View.OnClickListener addPageListener = new OnClickListener() {
	        public void onClick(View v) {
	        	IntentUtil.startCreatePageActivity(PageListActivity.this);
	        }
	    };

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(PageListItemView.class.isInstance(view)) {
			PageListItemView item = (PageListItemView) view;
			PageInfo info = item.getItem();
			if(info != null) {
				IntentUtil.startPageDetailActivity(this, info.page_id);
			}
		}else {
            Log.d(TAG, "the page is null.");
        }
		
	}

	@Override
	public void refreshpage(PageInfo info) {
		refreshPageList();
	}
	
	@Override
    public boolean onSearchRequested() {
        return true;
    }

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		Configuration conf = getResources().getConfiguration();
//		if(conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			mListView.setNumRows(1);
//		} else {
//			mListView.setNumColumns(1);
//		}
//	}
}