package com.borqs.common.view;

import java.lang.reflect.Method;
import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.PollInfo;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.PollListAdapter;
import com.borqs.common.adapter.PollListAdapter.LoaderMoreListener;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshBase.OnRefreshListener;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshListView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.ui.bpc.PollDetailActivity;
import com.borqs.qiupu.util.ToastUtil;

public class StreamRightPollListViewUi implements OnScrollListener, LoaderMoreListener {
	private final static String TAG = StreamRightPollListViewUi.class
			.getName();
	private ListView mListView;
	private PullToRefreshListView mPullView;
	private PollListAdapter mPollAdapter;
	private long mCircleId;
	private Handler mHandler;
	private ArrayList<PollInfo> mPollList = new ArrayList<PollInfo>();
	protected String RESULT = "result";
	protected Context mContext;
	protected AsyncQiupu mAsyncQiupu;
	private int mPage = 0;
    private int mCount = 20;
    private QiupuORM mOrm;
    private boolean mForceRefresh;
    private boolean showMoreButton;
    private static final int DEFAULT_CIRCLR_POLL_TYPE = 2;
    private boolean mAlreadyLoad;

	public StreamRightPollListViewUi() {
	}

	public void init(Context con, PullToRefreshListView listView, long circleid) {
		mContext = con;
		mHandler = new MainHandler();
		mAsyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mOrm = QiupuORM.getInstance(con);
		mPullView = listView;
		mListView = mPullView.getRefreshableView();
		
		mPollAdapter = new PollListAdapter(con, mPollList,false, this);
		mListView.setAdapter(mPollAdapter);
		mCircleId = circleid;
		mListView.setOnItemClickListener(mItemClickListener);
		mPullView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadRefresh();
			}
		});
		
		if (mCircleId > 0) {
            mPollList = QiupuORM.queryCirclePollList(con, String.valueOf(mCircleId));
            if(mPollList != null && mPollList.size() > 0) {
            	if(QiupuConfig.LOGD)Log.d(TAG, "have poll list in local db.");
            	mPollAdapter.alterPollList(mPollList, showMoreButton);
//            }else {
//            	mHandler.obtainMessage(SYNC_POLL).sendToTarget();
            }
        } else {
            Log.d(TAG, "circleid is null");
        }
		
	}

	private void loadRefresh() {
		mForceRefresh = true;
        mHandler.obtainMessage(SYNC_POLL).sendToTarget();
	}

	private final int SYNC_POLL     = 101;
    private final int SYNC_POLL_END = 102;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SYNC_POLL: {
                	getUserPollList(DEFAULT_CIRCLR_POLL_TYPE, mPage);
                    break;
                }
                case SYNC_POLL_END: {
                	if(mPullView != null) {
                		mPullView.onRefreshComplete();
                	}
                    mPollAdapter.refreshLoadingStatus();
                    if(mForceRefresh) {
                        mForceRefresh = false;
                    }
                    if (msg.getData().getBoolean(RESULT)) {
                    	mAlreadyLoad = true;
                    	 mPollAdapter.alterPollList(mPollList, showMoreButton);
                    } else {
                        ToastUtil.showOperationFailed(mContext, mHandler, false);
                    }
                    break;
                }
            }
        }
    }

    private boolean isLoadingPoll;
    private Object loadingLock = new Object();
    public void getUserPollList(final int type, final int page) {
        if (!ToastUtil.testValidConnectivity(mContext)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            callFailLoadPollMethod();
            return;
        }
        synchronized (loadingLock) {
			if (isLoadingPoll) {
				Log.d(TAG, "in doing getUserPollList data");
				callFailLoadPollMethod();
				return;
			}
		}
        
        synchronized (loadingLock) {
        	isLoadingPoll = true;
		}
        
        mPollAdapter.refreshLoadingStatus();

        mAsyncQiupu.getUserPollList(AccountServiceUtils.getSessionID(),type, page, mCount, mCircleId, new TwitterAdapter() {
            @Override
            public void getUserPollList(ArrayList<PollInfo> pollList) {
                Log.d(TAG, "finish pollList.size() " );
                showMoreButton(pollList);
                
                if(mForceRefresh || page == 0) {
                	mPollList.clear();
                }
                if(pollList != null) {
                	mPollList.addAll(pollList);
                }
                
                Message msg = mHandler.obtainMessage(SYNC_POLL_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                
                synchronized (loadingLock) {
                	isLoadingPoll = false;
                }
            }

            public void onException(TwitterException ex,
                    TwitterMethod method) {
            	callFailLoadPollMethod();
            	Message msg = mHandler.obtainMessage(SYNC_POLL_END);
            	msg.getData().putBoolean(RESULT, false);
            	msg.sendToTarget();
            	
            	synchronized (loadingLock) {
            		isLoadingPoll = false;
				}
            }
        });
    }

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
        	if (PollItemView.class.isInstance(view)) {
                PollItemView itemView = (PollItemView) view;
                Intent intent = new Intent();
            	intent.setClass(mContext, PollDetailActivity.class);
            	intent.putExtra(PollDetailActivity.POLL_ID_KEY, itemView.getPollInfo());
            	mContext.startActivity(intent);
            } else {
                Log.d(TAG, "mItemClickListener error, view = " + view);
            }
        }
    };
    
    public void onDestory() {
    	insertCirclePollToDb(mPollList);
    }
    
    private void insertCirclePollToDb(ArrayList<PollInfo> datalist) {
        if(datalist == null) return;
        int size = datalist.size();
        if (size > 0) {
            Log.d(TAG, "insertCirclePollToDb -------------size = " + size);
            if(size > mCount) {
                size = mCount;
            }
            final ArrayList<PollInfo> cachePollList = new ArrayList<PollInfo>();
            for(int i = 0; i < size; i++) {
                cachePollList.add(datalist.get(i));
            }
            datalist.clear();
            datalist = null;
            QiupuORM.sWorker.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "insertCirclePollToDb---------cachePollListsize = " + cachePollList.size());
                    mOrm.insertCirclePollList(cachePollList);
                    cachePollList.clear();
                }
            });
        }else {
        	QiupuORM.removeCirclePoll(mCircleId, mContext);
        }
    }
    
    private void showMoreButton(ArrayList<PollInfo> pollList) {
    	if (pollList != null && pollList.size() < mCount) {
    		showMoreButton = false;
        } else {
        	showMoreButton = true;
        }
    }

    private int followerItem = 0;
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		followerItem = firstVisibleItem + visibleItemCount;		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int state) {
		boolean forloadmore = (state == OnScrollListener.SCROLL_STATE_IDLE);
        loadOldPoll(forloadmore, false);		
	}
	
	private void loadOldPoll(boolean formore, boolean forceget) {
        int pollcount = mPollList.size();
        Log.i(TAG, "poll count:" + pollcount);
        if ((followerItem == pollcount + 2 && formore) || forceget) {
        	getOldPollData();
        }
    }
	
	private void SubPollPage() {
		Log.d(TAG, "resore the dpage--");
		mPage--;
		if (mPage < 0)
			mPage = 0;
	}
	
	private Method failCallSyncPollMethod;
	
	private void callFailLoadPollMethod() {
		try {
			if (failCallSyncPollMethod != null) {
				failCallSyncPollMethod.invoke(this, (Object[]) null);
			}
		} catch (Exception ne) {
		}
	}
	
	private void getOldPollData() {
		mPage += 1;
		Log.i(TAG, "Page:" + mPage);
        try {
        	failCallSyncPollMethod = StreamRightPollListViewUi.class.getDeclaredMethod("SubPollPage", (Class[]) null);
        } catch (Exception e) {
        }
        getUserPollList(DEFAULT_CIRCLR_POLL_TYPE, mPage);
    }

	@Override
	public int getCaptionResourceId() {
        return isLoadingPoll ? R.string.loading : R.string.list_view_more;
	}

	@Override
	public OnClickListener loaderMoreClickListener() {
		return loadOldClickListener;
	}
	
	private View.OnClickListener loadOldClickListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            mHandler.obtainMessage(SYNC_POLL).sendToTarget();
        }
    };

	public void loadDataOnMove() {
		if(mPollList != null && mPollList.size() <= 0 && !mAlreadyLoad) {
			mHandler.obtainMessage(SYNC_POLL).sendToTarget();			
		}
	}
}
