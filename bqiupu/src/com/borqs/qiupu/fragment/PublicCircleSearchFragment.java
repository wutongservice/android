package com.borqs.qiupu.fragment;


import java.lang.reflect.Method;
import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.publicCircleSearchAdapter;
import com.borqs.common.adapter.publicCircleSearchAdapter.MoreItemCheckListener;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CircleItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public final class PublicCircleSearchFragment extends BasicFragment implements MoreItemCheckListener{
	private static final String TAG = "PublicCircleSearchFragment";
	private Activity mActivity;
	private ListView mListView;
	private Handler mHandler;
	protected AsyncQiupu asyncQiupu;
	private static final String RESULT = "result";
	private String mSearchKey;
	private String mOldSearchKey;
	private boolean isFragmentReadly = false;
	private int mPage = 0;
	private int mCount = 20;
	private boolean isUserShowMore;
	
	private publicCircleSearchAdapter mSearchAdapter;
	private ArrayList<UserCircle> mSearchCircles = new ArrayList<UserCircle>();

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach " );
		super.onAttach(activity);
		mActivity = activity;

		try{
			CallBackPublicCircleSearchListener listener = (CallBackPublicCircleSearchListener)activity;
			listener.getCircleSearchFragment(this);
			mSearchKey = listener.getSearchKey();
		}catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackCircleListFragmentListener");
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate " );
		super.onCreate(savedInstanceState);
		asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mHandler = new MainHandler();
		
		if(StringUtil.isValidString(mSearchKey)){
			mHandler.obtainMessage(SEARCH_CIRCLE).sendToTarget();
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView " );
		mListView = (ListView) inflater.inflate(R.layout.default_listview, container, false);	
		mListView.setOnItemClickListener(circleListClickListener);
		mListView.setDivider(null);
		mSearchAdapter = new publicCircleSearchAdapter(mActivity);
		mListView.setAdapter(mSearchAdapter);
		mListView.setOnItemClickListener(itemClickListener);
		
		return mListView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
	

	private final int SEARCH_CIRCLE = 1;
    private final int SEARCH_CIRCLE_END = 2;
    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SEARCH_CIRCLE: {                
                searchPublicCircles(mSearchKey);
                break;
            }case SEARCH_CIRCLE_END: {
                end();
                boolean ret = msg.getData().getBoolean(RESULT, false);
                if (ret) {
                    if(mSearchCircles.size() > 0) {
                        mSearchAdapter.alterDataList(null, mSearchCircles);
                    }else {
                        ToastUtil.showShortToast(mActivity, mHandler, R.string.search_null_toast);
                    }
                } else {
                    ToastUtil.showOperationFailed(mActivity, mHandler, true);
                }
                break;
            }
            }
        }
    }

    OnItemClickListener circleListClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (CircleItemView.class.isInstance(view)) {
                CircleItemView fv = (CircleItemView) view;
                UserCircle tmpCircle = fv.getCircle();
                IntentUtil.startPublicCircleDetailIntent(mActivity, tmpCircle);
            }
        }
    };
    
	boolean inSearchCircle;
    Object mLockSearchCircle = new Object();
    
    private void searchPublicCircles(final String name) {
    	synchronized(mLockSearchCircle) {
    		if (inSearchCircle == true) {
    			Toast.makeText(mActivity, R.string.string_in_processing,Toast.LENGTH_SHORT).show();
    			callFailLoadCircleMethod();
    			return ;
    		}
    	}
    	
    	synchronized (mLockSearchCircle) {
    	    inSearchCircle = true;
    	}
    	begin();
    	setMoreItem(true);
    	asyncQiupu.searchPublicCircles(AccountServiceUtils.getSessionID(), name, mPage, mCount, new TwitterAdapter() {
    		public void searchPublicCircles(ArrayList<UserCircle> circles) {
    			Log.d(TAG, "finish searchPublicCircles=" + circles.size());
    			mOldSearchKey = mSearchKey;
    			doSearchCircleCallBack(true, circles);
				
    			synchronized (mLockSearchCircle) {
    			    inSearchCircle = false;
    			}
    		}
    		
    		public void onException(TwitterException ex,TwitterMethod method) {
    			synchronized (mLockSearchCircle) {
    			    inSearchCircle = false;
    			}
    			doSearchCircleCallBack(false, null);
    			callFailLoadCircleMethod();
    		}
    	});
    }
    
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        	if (CircleItemView.class.isInstance(view)) {
        		CircleItemView fv = (CircleItemView) view;
				UserCircle tmpCircle = fv.getCircle();
				IntentUtil.startPublicCircleDetailIntent(mActivity, tmpCircle);
			}
        }
    };
    
	public interface CallBackPublicCircleSearchListener {
		public void getCircleSearchFragment(PublicCircleSearchFragment fragment);
		public String getSearchKey();
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
					mSearchCircles.clear();
//					doInLineSearch(key);
					mHandler.obtainMessage(SEARCH_CIRCLE).sendToTarget();		
				}else{
				    Log.d(TAG, "the search key has not change");
				    if(mSearchCircles.size() <= 0) {
				        ToastUtil.showShortToast(mActivity, mHandler, R.string.search_page_result_null);
				    }
				}
			}else {
				Log.d(TAG, "circle search fragment is not readly, do noting.");
			}
		}
	}
	
	private void setMoreItem(boolean loading) {
        //set load older button text process for UI
        if(mListView != null) {
            for(int i=mListView.getChildCount()-1;i>0;i--) {
                View v = mListView.getChildAt(i);
                if(Button.class.isInstance(v)) {
                    Button bt = (Button)v;
                    if(loading) {
                        bt.setText(R.string.loading);
                    } else {
                        bt.setText(R.string.list_view_more);
                    }
                    break;
                }
            }
        }
    }
	
	private void doSearchCircleCallBack(boolean suc, ArrayList<UserCircle> circles) {
        if(getActivity() != null && isDetached() == false && mSearchCircles != null) {
            if (mPage == 0) {
            	mSearchCircles.clear();
            }
            if(circles != null) {
            	mSearchCircles.addAll(circles);
            	if (circles.size() > mCount) {
            		isUserShowMore = true;
            	} else {
            		isUserShowMore = false;
            	}
            }
            Message msg = mHandler.obtainMessage(SEARCH_CIRCLE_END);
            msg.getData().putBoolean(RESULT, suc);
            msg.sendToTarget();
        }
    }
	
	protected void callFailLoadCircleMethod() {
        try {
            if (failCallSyncCircleMethod != null) {
            	failCallSyncCircleMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
        }
    }
	protected Method failCallSyncCircleMethod;

    protected void SubPagesPage() {
        Log.d(TAG, "resore the dpage--");
        mPage--;
        if (mPage < 0)
            mPage = 0;
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
            failCallSyncCircleMethod = PeopleSearchFragment.class
                    .getDeclaredMethod("SubUserPage", (Class[]) null);
        } catch (Exception e) {
        }
        searchPublicCircles(mSearchKey);
    }
    
	@Override
	public boolean isMoreItemHidden() {
		return isUserShowMore;
	}

	@Override
	public OnClickListener getMoreItemClickListener() {
		return loadOlderClick;
	}

	@Override
	public int getMoreItemCaptionId() {
		// TODO Auto-generated method stub
		return 0;
	}
}
