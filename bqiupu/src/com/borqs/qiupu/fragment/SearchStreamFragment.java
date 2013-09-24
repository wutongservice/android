package com.borqs.qiupu.fragment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import twitter4j.AsyncQiupu;
import twitter4j.Stream;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.StreamListAdapter;
import com.borqs.common.adapter.StreamListAdapter.OlderPostsLoader;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.BpcSearchActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class SearchStreamFragment extends AbstractStreamListFragment implements OlderPostsLoader{
	private final static String TAG = "SearchStreamFragment";
    private Activity mActivity; 
    private String mSearchKey;
    private String mOldSearchKey;
    private AsyncQiupu asyncQiupu;
    private Handler mhandler;
    private long mGroupId;
    private int mPage;
    private int mCount = 20;
    private boolean mOldestLoaded;
    private boolean isFragmentReadly = false;
    
    @Override
    public void onAttach(Activity activity) {
    	Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;
    	try {
    		SearchStreamCallBackFragment listener = (SearchStreamCallBackFragment) mActivity;
    		listener.getSearchStreamFragment(this);
    		mSearchKey = listener.getSearchKey();
		} catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement SearchStreamCallBackFragment");
		}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
        mhandler = new MainHandler();
        
        if(StringUtil.isValidString(mSearchKey)){
        	mhandler.obtainMessage(LOAD_SEARCH).sendToTarget();
        }
        
        mGroupId = mActivity.getIntent().getLongExtra(BpcSearchActivity.GROUPID_STRING, -1);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mPostAdapter = StreamListAdapter.newInstance(mActivity, this);
        mListView.setAdapter(mPostAdapter);
        isFragmentReadly = true;
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
    	super.onDestroy();
    }
    
	private final static int LOAD_SEARCH     = 1;
	private final static int LOAD_SEARCH_END = 2;
	
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_SEARCH: {
				searchStream(mSearchKey);
				break;
			}
			case LOAD_SEARCH_END:
			{
				end();

//                if (mListView != null) {
//                    mListView.onRefreshComplete();
//                }

                //process for UI
                if (mPostAdapter != null) {
                    mPostAdapter.refreshLoadingStatus();
                }

                if (msg.getData().getBoolean(RESULT, false)) {
                	notifyDataSetChanged();
                }

                final String promptText = msg.getData().getString(PROMPT);
                if (!TextUtils.isEmpty(promptText)) {
                    showCustomToast(promptText);
                }
				break;
			} 
			}
		}
	}	
	
	boolean inloadingStreamSearch = false;
    Object  mSearchLock = new Object();
	private void searchStream(String searchKey) {
		
		if(mListView == null) {
    		return ;
    	}
		
    	if(!DataConnectionUtils.testValidConnection(mActivity)) {
    		ToastUtil.showCustomToast(mActivity, R.string.dlg_msg_no_active_connectivity, mHandler);
    		mListView.onRefreshComplete();
    		return ;	
    	}

		synchronized(mSearchLock) {
            if(inloadingStreamSearch == true) {
                Log.d(TAG, "in doing search stream data");
                callFailLoadStreamMethod();
                return;
            }
        }
		
        synchronized(mSearchLock) {
        	inloadingStreamSearch = true;
        }
        
        begin();
//        mListView.onRefreshStart();

        //set load older button text process for UI
        mPostAdapter.refreshLoadingStatus();

        HashMap<String, String> searchMap = new HashMap<String, String>();
        searchMap.put("type", "post");
        if(mGroupId > 0) {
        	searchMap.put("group", String.valueOf(mGroupId));
        }
        
        asyncQiupu.searchStream(AccountServiceUtils.getSessionID(), searchKey,
                searchMap, mPage, mCount, new TwitterAdapter() {
            public void SearchStream(final List<Stream> posts) {
                if (QiupuConfig.LOGD) Log.d(TAG, "finish search posts:" + posts.size());
                mOldSearchKey = mSearchKey;
                if(posts.size() == 0) {
					mOldestLoaded = true;
				}else {
					mOldestLoaded = false;
				}
				doSearchStreamCallBack(true, posts);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "getStream, server exception:", ex, method);
                doSearchStreamCallBack(false, null);
            }
        });
	}
	
	 private void doSearchStreamCallBack(boolean suc, final List<Stream> sts) {
		 synchronized(mSearchLock) {
			 inloadingStreamSearch = false;
	        }
		 
	        if(getActivity() != null && isDetached() == false && mPosts != null) {
	            if (mPage == 0) {
	            	mPosts.clear();
	            }
	            if(sts != null) {
	            	mPosts.addAll(sts);
	            }
	            Message msg = mhandler.obtainMessage(LOAD_SEARCH_END);
	            msg.getData().putBoolean(RESULT, suc);
	            msg.sendToTarget();
	        }
	    }
//
//	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
//    	mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
//    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
//    	mprogressDialog.show();    	
//    }
	
	
	public interface SearchStreamCallBackFragment {
		public void getSearchStreamFragment(SearchStreamFragment fragment);
		public String getSearchKey();
	}

    public View.OnClickListener loadOlderClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "load more user");
            getOldData();
        }
    };
    
    private void getOldData() {
        mPage += 1;
        Log.i(TAG, "Page:" + mPage);
        try {
        	failCallSyncStreamMethod = SearchStreamFragment.class
                    .getDeclaredMethod("SubStreamPage", (Class[]) null);
        } catch (Exception e) {
        }
        searchStream(mSearchKey);
    }
    
    protected Method failCallSyncStreamMethod;

    protected void SubStreamPage() {
        Log.d(TAG, "resore the dpage--");
        mPage--;
        if (mPage < 0)
            mPage = 0;
    }

    protected void callFailLoadStreamMethod() {
        try {
            if (failCallSyncStreamMethod != null) {
            	failCallSyncStreamMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
        }
    }
    
	@Override
	protected void onListViewRefresh() {
		if (mListView != null) {
            mListView.onRefreshComplete();
        }
	}

	@Override
	public OnClickListener getLoadOlderClickListener() {
		if (mOldestLoaded) {
            return null;
        }
        return loadOlderClick;
	}

	@Override
	public int getCaptionResourceId() {
		if (inloadingStreamSearch) {
            return R.string.loading;
        } else if (mOldestLoaded) {
            return R.string.last_stream_item_load;
        } else {
            return R.string.list_view_more;
        }
	}
	
	public void doMySearch(String key) {
		if(StringUtil.isEmpty(key)){
			Log.d(TAG, "doMySearch key is null " + key);
			Toast.makeText(mActivity, R.string.search_recommend, Toast.LENGTH_SHORT).show();
			return ;
		}else{
			if(isFragmentReadly) {
				if(!key.equals(mOldSearchKey)){
					mSearchKey = key;
					mPage = 0;
					mhandler.obtainMessage(LOAD_SEARCH).sendToTarget();		
				}else{
				    Log.d(TAG, "the search key has not changed");
				    if(mPosts != null) { 
				    	if(mPosts.size() <= 0) {
				    		ToastUtil.showShortToast(mActivity, mhandler, R.string.search_null_toast);
				    	}
				    }else {
				    	Log.d(TAG, "doMySearch: mPost is null");
				    }
				}
			}else {
				Log.d(TAG, "stream search fragment is not readly, do noting.");
			}
		}
	}
	
}
