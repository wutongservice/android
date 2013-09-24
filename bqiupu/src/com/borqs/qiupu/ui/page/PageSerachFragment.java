package com.borqs.qiupu.ui.page;

import java.lang.reflect.Method;
import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.PageInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.PageListAdapter;
import com.borqs.common.adapter.PageListAdapter.MoreItemCheckListener;
import com.borqs.common.listener.PageActionListener;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.PageListItemView;
import com.borqs.common.view.PageListItemView.followActionListener;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.fragment.BasicFragment;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class PageSerachFragment extends BasicFragment implements followActionListener, MoreItemCheckListener, PageActionListener{
	private final static String TAG = "PageSerachFragment";
    private Activity mActivity; 
    private ListView mListView;
    private PageListAdapter mListViewAdapter;
    private QiupuORM orm;
    private ArrayList<PageInfo> mSearch = new ArrayList<PageInfo>();
    private AsyncQiupu asyncQiupu;
    private Handler mhandler;
    private static final String RESULT = "result";
    private String mSearchKey;
    private String mOldSearchKey;
    private boolean isFragmentReadly = false;
    private ProgressDialog mprogressDialog;
    private int mPage = 0;
    private int mCount = 20;
    private boolean isUserShowMore;
    
    @Override
    public void onAttach(Activity activity) {
    	Log.d(TAG, "onAttach");
    	super.onAttach(activity);
    	mActivity = activity;
    	try {
    		CallBackPageSearchFragment listener = (CallBackPageSearchFragment) mActivity;
    		listener.getPageSerachFragment(this);
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
        QiupuHelper.registerPageListener(getClass().getName(), this);
        mListViewAdapter = new PageListAdapter(mActivity, this);
        
        if(StringUtil.isValidString(mSearchKey)){
        	mhandler.obtainMessage(LOAD_SEARCH).sendToTarget();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	mListView = (ListView) inflater.inflate(R.layout.default_listview, container, false);
    	
    	mListView.setDivider(null);
    	mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(itemClickListener);
		
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
    	mListViewAdapter.registerPageActionListner(getClass().getName(), this);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mListViewAdapter.unregisterPageActionListner(getClass().getName());
    	QiupuHelper.unregisterPageListener(getClass().getName());
    }
    
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        	if (PageListItemView.class.isInstance(view)) {
        		PageListItemView fv = (PageListItemView) view;
				PageInfo tmppage = fv.getItem();
				IntentUtil.startPageDetailActivity(mActivity, tmppage.page_id);
			}
        }
    };

	private final static int LOAD_SEARCH     = 101;
	private final static int LOAD_SEARCH_END = 102;
	private final static int FOLLOW_ACTION_END  = 103;
	
	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_SEARCH: {
			    if(DataConnectionUtils.testValidConnection(mActivity)) {
			    	searchPages();
                }else {
                    ToastUtil.showShortToast(mActivity, mhandler, R.string.dlg_msg_no_active_connectivity);
                }
				break;
			}
			case LOAD_SEARCH_END:
			{
				Log.d(TAG, "load search end");
				end();
				setMoreItem(false);
				if(msg.getData().getBoolean(RESULT)) {
				    if( mSearch.size() > 0) {
				        mListViewAdapter.alertpages(mSearch); 
				    }
				    else{
				        ToastUtil.showShortToast(mActivity, mhandler, R.string.search_page_result_null);
				    }
				}
				{
				}
				break;
			} 
			case FOLLOW_ACTION_END: {
				end();
				QiupuHelper.updatePageActivityUI(null);
				break;
			}
			}
		}
	}	
	
	boolean inloadingPageSearch = false;
    Object  mSearchLock = new Object();
	private void searchPages() {

		synchronized(mSearchLock) {
            if(inloadingPageSearch == true) {
                Log.d(TAG, "in doing get loadSearchPages data");
                callFailLoadPageMethod();
                return;
            }
        }
        synchronized(mSearchLock) {
        	inloadingPageSearch = true;
        }
        
		begin();
		setMoreItem(true);
		
		asyncQiupu.serachPage(AccountServiceUtils.getSessionID(), mSearchKey, mPage, mCount, new TwitterAdapter() {
			public void searchPage(ArrayList<PageInfo> pagelist) {
				Log.d(TAG, "finish search user : " + pagelist.size());
				
				mOldSearchKey = mSearchKey;
				doSearchPageCallBack(true, pagelist);
				synchronized(mSearchLock) {
					inloadingPageSearch = false;
		        }
			}

			public void onException(TwitterException ex,TwitterMethod method) {
				doSearchPageCallBack(false, null);
				callFailLoadPageMethod();
				synchronized(mSearchLock) {
					inloadingPageSearch = false;
		        }
			}
		});
	}
	
	boolean inFollowPage = false;
    Object  mFollowLock = new Object();
	private void followPages(final long pageid, final boolean isfollow) {

		synchronized(mFollowLock) {
            if(inFollowPage == true) {
                Log.d(TAG, "in doing get loadSearchAPPs data");
                return;
            }
        }
        synchronized(mFollowLock) {
        	inFollowPage = true;
        }
        
		begin();
		asyncQiupu.followPage(AccountServiceUtils.getSessionID(), pageid, isfollow, new TwitterAdapter() {
			public void followPage(PageInfo pageinfo) {
				Log.d(TAG, "finish followPage : " + pageinfo.page_id);
				if(isfollow) {
					orm.insertOnePage(pageinfo);
				}else {
					orm.deletePageByPageId(pageid);
				}
				refreshFollowAction(pageid);
				synchronized(mFollowLock) {
					inFollowPage = false;
				}
				Message msg = mhandler.obtainMessage(FOLLOW_ACTION_END);
				msg.getData().putBoolean(RESULT, true);
				msg.sendToTarget();
			}

			public void onException(TwitterException ex,TwitterMethod method) {
				synchronized(mFollowLock) {
					inFollowPage = false;
		        }
				Message msg = mhandler.obtainMessage(FOLLOW_ACTION_END);
	            msg.getData().putBoolean(RESULT, false);
	            msg.sendToTarget();
			}
		});
	}
	
    private void doSearchPageCallBack(boolean suc, ArrayList<PageInfo> users) {
        if(getActivity() != null && isDetached() == false && mSearch != null) {
            if (mPage == 0) {
                mSearch.clear();
            }
            if(users != null) {
            	mSearch.addAll(users);
            	if (users.size() > mCount) {
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
	
	private void refreshFollowAction(final long pageid)
	{
		mhandler.post( new Runnable()
		{
			public void run()
			{
				for(int i=mListView.getChildCount(); i >= 0; i--)
				{
					View v = mListView.getChildAt(i);
					if(PageListItemView.class.isInstance(v))
					{
						PageListItemView fv = (PageListItemView)v;
						if(fv.refreshItem(pageid)){
							break;
						}
					}
				}
			}
		});
	}
	
//	private void refreshMsearch(final long borqsid)
//	{
//		for(int i=0; i <mSearch.size(); i++)
//		{
//			PageInfo tmpInfo = mSearch.get(i);
//			if(tmpInfo.uid == borqsid){
//				tmpInfo = orm.queryOneUserInfo(borqsid);
//				mSearch.remove(i);
//				mSearch.add(0, tmpuser);
//				break;
//			}
//		}
//		mListViewAdapter.setUsersList(mSearch);
//	}
	
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
//					doInLineSearch(key);
					mhandler.obtainMessage(LOAD_SEARCH).sendToTarget();		
				}else{
				    Log.d(TAG, "the search key has not change");
				    if(mSearch.size() <= 0) {
				        ToastUtil.showShortToast(mActivity, mhandler, R.string.search_page_result_null);
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
	
	public interface CallBackPageSearchFragment {
		public void getPageSerachFragment(PageSerachFragment fragment);
		public String getSearchKey();
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
        return inloadingPageSearch ? R.string.loading : R.string.list_view_more;
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
        	failCallSyncPageMethod = PageSerachFragment.class
                    .getDeclaredMethod("SubPagesPage", (Class[]) null);
        } catch (Exception e) {
        }
        searchPages();
    }
    
    protected Method failCallSyncPageMethod;

    protected void SubPagesPage() {
        Log.d(TAG, "resore the dpage--");
        mPage--;
        if (mPage < 0)
            mPage = 0;
    }

    protected void callFailLoadPageMethod() {
        try {
            if (failCallSyncPageMethod != null) {
            	failCallSyncPageMethod.invoke(this, (Object[]) null);
            }
        } catch (Exception ne) {
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

	@Override
	public void followPage(long pageid) {
		followPages(pageid, true);
	}

	@Override
	public void unFollowPage(long pageid) {
		followPages(pageid, false);
	}

	@Override
	public void refreshpage(final PageInfo info) {
		if(info != null) {
			mhandler.post( new Runnable()
			{
				public void run()
				{
					for(int i=mListView.getChildCount(); i >= 0; i--)
					{
						View v = mListView.getChildAt(i);
						if(PageListItemView.class.isInstance(v))
						{
							PageListItemView fv = (PageListItemView)v;
							if(fv.getDataItemId() == info.page_id){
								fv.setPage(info);
								break;
							}
						}
					}
				}
			});
		}
	}
}
