package com.borqs.common.view;

import java.util.ArrayList;

import twitter4j.AsyncQiupu;
import twitter4j.QiupuAlbum;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.UserCircle;
import twitter4j.conf.ConfigurationContext;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.AlbumGridLayoutAdapter;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshBase.OnRefreshListener;
import com.borqs.common.view.pullRefreshGridView.PullToRefreshGridView;
import com.borqs.qiupu.db.QiupuORM;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.ToastUtil;

public class StreamRightAlbumViewUi {
	private final static String TAG = StreamRightAlbumViewUi.class
			.getName();
	private AlbumGridLayoutAdapter mGridAdapter;
	private GridView mGridView;
	private PullToRefreshGridView mPullView;
	private Handler mHandler;
	private String RESULT = "result";
	private String ERROR_MSG = "error_msg";
	private Context mContext;
	private AsyncQiupu mAsyncQiupu;
	private QiupuORM mOrm;
	private UserCircle mCircle;
	private boolean mAlreadyLoad;
	
	private ArrayList<QiupuAlbum> mAlbums = new ArrayList<QiupuAlbum>();

	public StreamRightAlbumViewUi() {
	}

	public void init(Context con, PullToRefreshGridView pullView, UserCircle circle) {
		mContext = con;
		mHandler = new MainHandler();
		mAsyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
		mOrm = QiupuORM.getInstance(con);
		mGridAdapter = new AlbumGridLayoutAdapter(con , 480, mAlbums);
		mPullView = pullView;
		mGridView = pullView.getRefreshableView();
		mGridView.setAdapter(mGridAdapter);
		mCircle = circle;
		mGridView.setOnItemClickListener(mItemClickListener);
		pullView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadRefresh();
			}
		});
		
		mAlbums = QiupuORM.queryQiupuAlbums(con, mCircle.circleid);
		mGridAdapter.alertData(mAlbums);
		
		onConfigurationChanged(mContext.getResources().getConfiguration());
		
//		mHandler.postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				loadRefresh();
//			}
//		}, 500);
	}
	
	private void loadRefresh() {
		if(mCircle != null) {
			Message msg = mHandler.obtainMessage(GET_ALBUM);
			msg.getData().putLong(CircleUtils.CIRCLE_ID, mCircle.circleid);
			msg.sendToTarget();
		}else {
			Log.d(TAG, "laodrefresh: circle is null");
		}
	}

	private final int GET_ALBUM = 1;
	private final int GET_ALBUM_END = 2;

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_ALBUM: {
				getAllAlbums(msg.getData().getLong(CircleUtils.CIRCLE_ID));
				break;
			}
			case GET_ALBUM_END: {
				if (mPullView != null) {
					mPullView.onRefreshComplete();
				}
				if (msg.getData().getBoolean(RESULT)) {
					mAlreadyLoad =  true;
					mGridAdapter.alertData(mAlbums);
				} else {
					ToastUtil.showOperationFailed(mContext, mHandler, false);
				}
				break;
			}
			}
		}
	}
	
	boolean inloadingAlbum = false;
	Object loadingLock = new Object();
	
	private void getAllAlbums(final long circleId) {
		if(circleId <= 0) {
			Log.e(TAG, "getAllAlbums: circleid is null ");
		}
		synchronized (loadingLock) {
			if (inloadingAlbum == true) {
				Log.d(TAG, "in doing getAllAlbums data");
				return;
			}
		}
		
		synchronized (loadingLock) {
			inloadingAlbum = true;
		}
		
		mAsyncQiupu.getAllAlbums(AccountServiceUtils.getSessionID(), circleId, false, new TwitterAdapter() {
			@Override
			public void getAllAlbums(ArrayList<QiupuAlbum> albums) {
				if(mAlbums != null) {
				    for(int i=0;i<albums.size();i++) {
				        for(int j=0;j<mAlbums.size();j++) {
				            if(albums.get(i).album_id == mAlbums.get(j).album_id) {
				                albums.get(i).have_expired = albums.get(i).compareTo(mAlbums.get(j)) == 1;
				            }
				        }
				    }
					mAlbums.clear();
					mAlbums.addAll(albums);
				}
				mOrm.insertQiupuAlbumList(albums, circleId);
				Log.d(TAG,"finish getAllAlbums="+albums.size());
				Message mds = mHandler.obtainMessage(GET_ALBUM_END);
				mds.getData().putBoolean(RESULT,true);
				mHandler.sendMessage(mds);
				
				synchronized (loadingLock) {
					inloadingAlbum = false;
				}
			}
			
			public void onException(TwitterException ex,TwitterMethod method) 
			{
				int error_code = ex.getStatusCode();
				Message mds = mHandler.obtainMessage(GET_ALBUM_END);
    			mds.getData().putBoolean(RESULT,false);
    			mds.getData().putString(ERROR_MSG,ex.getMessage());
    			mHandler.sendMessage(mds);
    			
    			synchronized (loadingLock) {
    				inloadingAlbum = false;
				}
			}
		});   
	}

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
        	if(mCircle != null) {
        		IntentUtil.startGridPicIntent(mContext, mAlbums.get(position).album_id, mCircle.circleid, mCircle.name, false);
        	}
        }
    };

	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation !=  Configuration.ORIENTATION_LANDSCAPE) {
   	      if (newConfig.orientation ==  Configuration.ORIENTATION_PORTRAIT) {
   	    	mGridView.setNumColumns(2);
   	      }
   	    } else {
   	    	mGridView.setNumColumns(4);
   	    }
	}

	public void loadDataOnMove() {
		if(mAlbums != null && mAlbums.size() <= 0 && !mAlreadyLoad) {
			loadRefresh();
		}
	}
}
