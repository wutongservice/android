package com.borqs.qiupu.fragment;

import java.lang.reflect.Method;
import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.Employee;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.TopSubCircleMemberAdapter;
import com.borqs.common.adapter.TopSubCircleMemberAdapter.MoreItemCheckListener;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.TopSubMemberItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class PeopleSearchFragment extends BasicFragment implements MoreItemCheckListener{
	private final static String TAG = "PeopleSearchFragment";
    private Activity mActivity; 
    private ListView mListView;
    private TopSubCircleMemberAdapter mListViewAdapter;
    private Cursor mInLineSearch;
    private QiupuORM orm;
    private ArrayList<Employee> mSearch = new ArrayList<Employee>();
    private AsyncQiupu asyncQiupu;
    private Handler mhandler;
    private static final String RESULT = "result";
    private String mSearchKey;
    private String mOldSearchKey;
    private boolean isFragmentReadly = false;
    private ProgressDialog mprogressDialog;
    private int mPage = 0;
    private int mCount = 20;
    private long mSceneId;
    private boolean isUserShowMore;
    
    @Override
    public void onAttach(Activity activity) {
    	Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;
    	try {
    		CallBackPeopleSearchFragment listener = (CallBackPeopleSearchFragment) mActivity;
    		listener.getPeopleSerachFragment(this);
    		mSearchKey = listener.getSearchKey();
		} catch (ClassCastException e) {
			Log.d(TAG, activity.toString() +  "must implement CallBackPeopleSearchFragment");
		}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
    	orm = QiupuORM.getInstance(mActivity);
    	asyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
        mhandler = new MainHandler();
        mListViewAdapter = new TopSubCircleMemberAdapter(mActivity, new UserCircle(), this);
        
        if(StringUtil.isValidString(mSearchKey)){
        	doInLineSearch(mSearchKey);
        	mhandler.obtainMessage(LOAD_SEARCH).sendToTarget();
        }
        
        final String homeid = QiupuORM.getSettingValue(mActivity, QiupuORM.HOME_ACTIVITY_ID);
    	if(TextUtils.isEmpty(homeid) == false) {
    		try {
    			mSceneId = Long.parseLong(homeid);
    		} catch (Exception e) {
    			Log.d(TAG, "homeid is null");
    		}
    	}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	mListView = (ListView) inflater.inflate(R.layout.default_listview, container, false);
    	mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(contactitemClickListener);
		
		return mListView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
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
    	QiupuHelper.unregisterUserListener(getClass().getName());
    	if(mInLineSearch != null)
			mInLineSearch.close();
    }
    
    private AdapterView.OnItemClickListener contactitemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        	if(TopSubMemberItemView.class.isInstance(view)) {
				TopSubMemberItemView tb = (TopSubMemberItemView) view;
				Employee tmpUser = tb.getUser();
				if (!TextUtils.isEmpty(tmpUser.user_id) && TextUtils.isDigitsOnly(tmpUser.user_id) 
						&& Long.parseLong(tmpUser.user_id) > 0) {
					IntentUtil.startUserDetailIntent(tb.getContext(),
							Long.parseLong(tmpUser.user_id), tmpUser.name, "");
				}else {
					Log.d(TAG, "the user id < 0, is not system user");
				}
			}
        }
    };

	public void doInLineSearch(String key)
    {
		Log.d(TAG, "doInLineSearch: " + key);

		mOldSearchKey = "";
		isUserShowMore = false;
		
		if(mInLineSearch != null)
			mInLineSearch.close();
		
		if(orm != null){
			mInLineSearch = QiupuORM.queryCircleEmployeeWithFilter(mActivity, mSceneId, key);
			mSearch.clear();
			mListViewAdapter.alterData(mInLineSearch, mSearch);
		}
    }
	
	private final static int LOAD_SEARCH     = 1;
	private final static int LOAD_SEARCH_END = 2;
	
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_SEARCH: {
			    if(DataConnectionUtils.testValidConnection(mActivity)) {
//                    searchFriends();
                    searchPeople(mSceneId, mSearchKey, mPage, mCount);
                }else {
                    ToastUtil.showShortToast(mActivity, mhandler, R.string.dlg_msg_no_active_connectivity);
                }
				break;
			}
			case LOAD_SEARCH_END:
			{
				Log.d(TAG, "load search end");
				end();
				if(msg.getData().getBoolean(RESULT)) {
				    if( mSearch.size() > 0) {
				        mListViewAdapter.alterDataList(mSearch); 
				    }
				    else{
				        ToastUtil.showShortToast(mActivity, mhandler, R.string.search_people_result_null);
				    }
				}else {
					ToastUtil.showOperationFailed(mActivity, mhandler, true);
				}
				break;
			} 
			}
		}
	}	
	
	boolean inSearchPeople = false;
	Object mSearchPeopleLock = new Object();
	
	private void searchPeople(final long circleid, String key,
            final int page, final int count) {
        if (QiupuConfig.LOGD) Log.d(TAG, "searchCirclePeople" + key);

        final String tmpkey = key.trim().toLowerCase();

        if (StringUtil.isEmpty(tmpkey)) {
            callFailLoadUserMethod();
            if (QiupuConfig.LOGD) Log.d(TAG, "input string is null");
            return;
        }
        
		synchronized (mSearchPeopleLock) {
			if (inSearchPeople == true) {
				Log.d(TAG, "in loading data");
				return;
			}
		}
        
        begin();
		
        mListViewAdapter.alterDataCursor(null);
        
        synchronized (mSearchPeopleLock) {
        	inSearchPeople = true;
		}
        asyncQiupu.getDirectoryInfo(AccountServiceUtils.getSessionID(), circleid, "", page, count, key, new TwitterAdapter() {
            public void getDirectoryInfo(ArrayList<Employee> users) {
                Log.d(TAG, "finish search user : " + users.size());

                mOldSearchKey = mSearchKey;
				if(users != null) 
				{
					doSearchUserCallBack(true, users);
				}
				synchronized(mSearchPeopleLock) {
					inSearchPeople = false;
		        }
				
            }

            public void onException(TwitterException ex, TwitterMethod method) {
            	doSearchUserCallBack(false, null);
				callFailLoadUserMethod();
				synchronized(mSearchPeopleLock) {
					inSearchPeople = false;
		        }
            }
        });
    }
	
//	boolean inloadingPeopleSearch = false;
//    Object  mSearchLock = new Object();
//	private void searchFriends() {
//
//		synchronized(mSearchLock) {
//            if(inloadingPeopleSearch == true) {
//                Log.d(TAG, "in doing get loadSearchAPPs data");
//                callFailLoadUserMethod();
//                return;
//            }
//        }
//        synchronized(mSearchLock) {
//        	inloadingPeopleSearch = true;
//        }
//        
//		begin();
//		setMoreItem(true);
//		
//		asyncQiupu.getUserListWithSearchName(AccountServiceUtils.getSessionID(), mSearchKey, mSearchKey, mSearchKey,mPage, mCount, new TwitterAdapter() {
//			public void getUserListWithSearchName(ArrayList<QiupuUser> users) {
//				Log.d(TAG, "finish search user : " + users.size());
//				
//				mOldSearchKey = mSearchKey;
//				if(users != null) 
//				{
//					doSearchUserCallBack(true, users);
//				}
//				synchronized(mSearchLock) {
//		        	inloadingPeopleSearch = false;
//		        }
//			}
//
//			public void onException(TwitterException ex,TwitterMethod method) {
//				doSearchUserCallBack(false, null);
//				callFailLoadUserMethod();
//				synchronized(mSearchLock) {
//		        	inloadingPeopleSearch = false;
//		        }
//			}
//		});
//	}
	
    private void doSearchUserCallBack(boolean suc, ArrayList<Employee> users) {
        if(getActivity() != null && isDetached() == false && mSearch != null) {
            if (mPage == 0) {
                mSearch.clear();
            }
            if(users != null) {
            	mSearch.addAll(users);
            	if (users.size() >= mCount) {
            		isUserShowMore = true;
            	} else {
            		isUserShowMore = false;
            	}
            }
            Message msg = mhandler.obtainMessage(LOAD_SEARCH_END);
            msg.getData().putBoolean(RESULT, suc);
            msg.sendToTarget();
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
					mSearch.clear();
					doInLineSearch(key);
					mhandler.obtainMessage(LOAD_SEARCH).sendToTarget();		
				}else{
				    Log.d(TAG, "the search key has not change");
				    if(mSearch.size() <= 0) {
				        ToastUtil.showShortToast(mActivity, mhandler, R.string.search_people_result_null);
				    }
				}
			}else {
				Log.d(TAG, "people search fragment is not readly, do noting.");
			}
		}
	}

	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
    	mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
    			resId, CanceledOnTouchOutside, Indeterminate, cancelable);
    	mprogressDialog.show();    	
    }
	
	public interface CallBackPeopleSearchFragment {
		public void getPeopleSerachFragment(PeopleSearchFragment fragment);
		public String getSearchKey();
	}

	@Override
    public boolean isMoreItemHidden() {
        return !isUserShowMore;
    }

    @Override
    public OnClickListener getMoreItemClickListener() {
        return loadOlderClick;
    }

    @Override
    public int getMoreItemCaptionId() {
        return inSearchPeople ? R.string.loading : R.string.list_view_more;
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
            failCallSyncUserMethod = PeopleSearchFragment.class
                    .getDeclaredMethod("SubUserPage", (Class[]) null);
        } catch (Exception e) {
        }
        searchPeople(mSceneId, mSearchKey, mPage, mCount);
//        searchFriends();
    }
    
    protected Method failCallSyncUserMethod;

    protected void SubUserPage() {
        Log.d(TAG, "resore the dpage--");
        mPage--;
        if (mPage < 0)
            mPage = 0;
    }

    protected void callFailLoadUserMethod() {
        try {
            if (failCallSyncUserMethod != null) {
                failCallSyncUserMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
        }
    }
}
