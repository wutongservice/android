package com.borqs.qiupu.fragment;


import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.PageInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.PageListAdapter;
import com.borqs.common.listener.PageActionListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.PageListItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.util.ToastUtil;

public final class PageListFragment extends PeopleSearchableFragment implements OnItemClickListener, PageActionListener{
	private static final String TAG = "PageListFragment";
	private Activity mActivity;
//	private ListView mListView;
	private QiupuORM orm ;
	private Handler mHandler;
	protected AsyncQiupu asyncQiupu;
	private static final String RESULT = "result";
	private static final String ERRORMSG = "errormsg";
//	private TextView mToastTextView;
    private PageListAdapter mPageAdapter;
    private Cursor mPage;

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach " );
		super.onAttach(activity);
		mActivity = activity;

		try{
			CallBackPageListFragmentListener listener = (CallBackPageListFragmentListener)activity;
			listener.getPageListFragment(this);
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackCircleListFragmentListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate " );
		super.onCreate(savedInstanceState);
		showEditText = false;
		orm = QiupuORM.getInstance(mActivity);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mHandler = new MainHandler();
		QiupuHelper.registerPageListener(getClass().getName(), this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView " );
		View convertView = super.onCreateView(inflater, container, savedInstanceState);
//		View mContentView = inflater.inflate(R.layout.page_list_main, container, false);
//		mListView  = (ListView) mContentView.findViewById(R.id.default_listview);
//        mToastTextView = (TextView) mContentView.findViewById(R.id.toast_tv);
        mPageAdapter = new PageListAdapter(mActivity);
        mListView.setAdapter(mPageAdapter);
        mListView.setOnItemClickListener(this);
        
		return convertView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mHandler.post(new Runnable() {
		    @Override
		    public void run() {
		    	refreshPageList();
		        mHandler.obtainMessage(SYNC_PAGE).sendToTarget();
		    }
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		QiupuHelper.unregisterPageListener(getClass().getName());
    	mPageAdapter.clearCursor();
		super.onDestroy();
	}

	private void refreshPageList() {
		if(mPage != null) {
			mPage.close();
		}
		mPage = orm.queryAllSimplePages();
		if (mPage != null) {
			Log.d(TAG, "refreshPageList.getCount() = " +mPage.getCount());
			mPageAdapter.alterPages(mPage);
		}
		
		if(mPage != null && mPage.getCount() > 0) {
			mEmptyText.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}else {
			mEmptyText.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			mEmptyText.setText(R.string.have_no_page);
		}
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
            		ToastUtil.showOperationFailed(mActivity, mHandler, false);
            	}
            	break;
            }
            }
        }
    }

    boolean inLoadingPage;
    Object mLockSyncPageInfo = new Object();
    public void syncPageInfo(final String pageIds) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }
        
    	if (inLoadingPage == true) {
    		ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
    		return;
    	}
    	
    	synchronized (mLockSyncPageInfo) {
    		inLoadingPage = true;
    	}
    	begin();
    	
    	asyncQiupu.syncPageList(AccountServiceUtils.getSessionID(), pageIds, new TwitterAdapter() {
    		public void syncPageList(ArrayList<PageInfo> pages) {
    			Log.d(TAG, "finish syncEventInfo=" + pages.size());
    			
//    			if (pages.size() > 0) {
    				orm.insertPageList(pages);
//                }
    			
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
	
    public void loadRefresh() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                mHandler.obtainMessage(SYNC_PAGE).sendToTarget();
            }
        }, 500);
    }
	
	public interface CallBackPageListFragmentListener {
		public void getPageListFragment(PageListFragment fragment);
	}

	@Override
	public void refreshpage(PageInfo info) {
		refreshPageList();		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(PageListItemView.class.isInstance(view)) {
			PageListItemView item = (PageListItemView) view;
			PageInfo info = item.getItem();
			if(info != null) {
				IntentUtil.startPageDetailActivity(mActivity, info.page_id);
			}
		}else {
            Log.d(TAG, "the page is null.");
        }
	}

	public boolean getLoadStatus() {
		return inLoadingPage;
	}

	@Override
	public void doSearch(String key) {
		if(mPage != null) {
			mPage.close();
		}
		mPage = orm.queryPagesCursor(key);
		if (mPage != null) {
			mPageAdapter.alterPages(mPage);
		}
		showSearchFromServerButton(key.length() > 0 ? true : false, key);
	}
	
	@Override
	protected void showSearchFromServerButton(boolean show, final String key,
			OnClickListener callback) {
		super.showSearchFromServerButton(show, key, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				IntentUtil.startSearchActivity(mActivity, key, BpcSearchActivity.SEARCH_TYPE_PAGE);
			}
		});
	}
}
