package com.borqs.qiupu.fragment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import twitter4j.Stream;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.StreamListAdapter;
import com.borqs.common.adapter.StreamListAdapter.OlderPostsLoader;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.RefreshPostListener;
import com.borqs.common.listener.RefreshPostProfileImageListener;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.common.view.CustomListView.LoadOldListener;
import com.borqs.information.util.InformationReadCache;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.cache.StreamCacheManager;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.ui.bpc.BpcPostsFilterActivity;
import com.borqs.qiupu.util.ToastUtil;

public class StreamListFragment extends AbstractStreamListFragment implements
        OlderPostsLoader,
        RefreshPostListener,
        RefreshPostProfileImageListener, LoadOldListener {
    private static final String TAG = "StreamListFragment";

    public static class MetaData implements Parcelable {
        public long mCircleId;
        public long mUserId;
        public int mSourceFilter;
        public int mSourceAppKey;
        public String mFragmentTitle;
        public int mFromHome;

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(mCircleId);
            out.writeLong(mUserId);
            out.writeInt(mSourceFilter);
            out.writeInt(mSourceAppKey);
            out.writeString(mFragmentTitle);
            out.writeInt(mFromHome);
        }

        public static final Parcelable.Creator<MetaData> CREATOR
                = new Parcelable.Creator<MetaData>() {
            public MetaData createFromParcel(Parcel in) {
                return new MetaData(in);
            }

            public MetaData[] newArray(int size) {
                return new MetaData[size];
            }
        };

        private MetaData(Parcel in) {
            mCircleId = in.readLong();
            mUserId = in.readLong();
            mSourceFilter = in.readInt();
            mSourceAppKey = in.readInt();
            mFragmentTitle = in.readString();
            mFromHome = in.readInt();
        }

        public MetaData() {
            mUserId = -1;
            mCircleId = -1;
            mSourceFilter = -1;
            mSourceAppKey = -1;
            mFragmentTitle = "";
            mFromHome = -1;
        }
    }

    public boolean mIsDirty;

    private int mFilterType = -1;

    public StreamListFragment() {
        super();
    }

    public interface StreamListFragmentCallBack {
        public MetaData getFragmentMetaData(int index);
        public String getSerializeFilePath();
    }

    @Override
    public View.OnClickListener getLoadOlderClickListener() {
        if (mOldestLoaded) {
            return null;
        }
        return loadOlderClick;
    }

    @Override
    public int getCaptionResourceId() {
        if (isLoadingFromServer) {
            return R.string.loading;
        } else if (mOldestLoaded) {
            return R.string.last_stream_item_load;
        } else {
            return R.string.list_view_more;
        }
    }

    private boolean mOldestLoaded;
    private MetaData mMetaData;
    private int mIndex = 0;
    private int pagesize = 20;

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach ");
        super.onAttach(activity);

        mActivity = activity;

        if (mActivity instanceof StreamListFragmentCallBack) {
            StreamListFragmentCallBack listener = (StreamListFragmentCallBack) activity;
            mMetaData = listener.getFragmentMetaData(mIndex);
            newsfeed_sfile = listener.getSerializeFilePath();
        }

        if (null == mMetaData) {
            mMetaData = new MetaData();
        }

        mFilterType = mMetaData.mSourceFilter;

        if (TextUtils.isEmpty(newsfeed_sfile)) {
            newsfeed_sfile = QiupuHelper.posts_public + AccountServiceUtils.getBorqsAccountID() + mMetaData.mUserId + mMetaData.mCircleId + mMetaData.mSourceFilter + mMetaData.mSourceAppKey;
        }

        Log.d(TAG, "onAttach mUserId : " + mMetaData.mUserId + "mCircleId : " + mMetaData.mCircleId +
                " mSourceFilter: " + mMetaData.mSourceFilter + " mSourceAppKey:" + mMetaData.mSourceAppKey);
        mFragmentId = getClass().getName() + newsfeed_sfile;
    }

    private void registerListeners() {
        QiupuHelper.registerStreamListener(mFragmentId, this);
        QiupuHelper.registerTargetLikeListener(mFragmentId, this);
        QiupuHelper.registerRefreshPostListener(mFragmentId, this);
        QiupuHelper.registerRefreshPostProfileImageListener(mFragmentId, this);
    }

    private void unregisterListeners () {
        QiupuHelper.unregisterStreamListener(mFragmentId);
        QiupuHelper.unRegisterTargetLikeListener(mFragmentId);
        QiupuHelper.unregisterRefreshPostListener(mFragmentId);
        QiupuHelper.unregisterRefreshPostProfileImageListener(mFragmentId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ");
        super.onCreate(savedInstanceState);

        mHandler = new MainHandler();
        mPendingToLoadCircleId = QiupuConfig.CIRCLE_ID_ALL;

        registerListeners();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated ");
//        mListView.setOnScrollListener(this);
        mListView.setOnCreateContextMenuListener(mActivity);
        mListView.setLoadOldListener(this);
        mPostAdapter = StreamListAdapter.newInstance(mActivity, this, BpcApiUtils.ONLY_PURE_APK_POST == mMetaData.mSourceFilter);
        mListView.setAdapter(mPostAdapter);
        AbstractStreamRowView.attachListViewItemClickerContext(mActivity, mListView);

        restoreStreamList(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestory: " + mIsDirty);
        if (/*mIsDirty*/true) {
            serialization(newsfeed_sfile);
            mIsDirty = false;
        }

        unregisterListeners();

        super.onDestroy();

        mActivity = null;
        mHandler = null;
        mMetaData = null;
        mPostAdapter = null;
        if(mListView != null) {
        	mListView.removeAllViewsInLayout();
        	mListView = null;
        }
        mFragmentId = null;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        super.onDetach();
    }
    public boolean getLoadStatus() {
        return isLoadingFromServer;
    }

    public void loadRefresh() {
        if(mHandler != null) {
            Log.d(TAG, "loadRefresh.");
            mHandler.obtainMessage(GET_STREAM).sendToTarget();
        }
    }

//    private class SwitchCircleTask extends UserTask<Void, Void, Integer> {
//        @Override
//        public void onPreExecute() {
//            mPosts.clear();
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public Integer doInBackground(Void... params) {
//            return deSerialization();
//        }
//
//        @Override
//        public void onPostExecute(Integer param) {
//            onSerializedCompleted(param);
//        }
//    }


    protected void serialization(final String filePath) {
    	if(mHandler == null)
    		return;
    	
    	final List<Stream> copyPost = new ArrayList<Stream>();
    	for(int i=0; i<mPosts.size(); i++) {
    		Stream stream = mPosts.get(i);
    		copyPost.add(stream);
    	}
    	
    	mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (copyPost != null && copyPost.size() > 0) {
					synchronized (copyPost) {
						FileOutputStream fos = null;
						ObjectOutputStream out = null;
						try {
							File postsFile = new File(filePath);
							
							if (postsFile.exists()) {
								Log.i(TAG, "serialization, file existing: " + filePath);
								// TODO: detect if it is neccessary to overwrite the existing file.
							} else {
								QiupuHelper.getTmpCachePath();  // create "cache" if "cache" is not exit.
								postsFile.createNewFile();
							}
							
							if (postsFile.canWrite()) {
								fos = new FileOutputStream(filePath);
								out = new ObjectOutputStream(fos);
								Date date = new Date();
								out.writeLong(date.getTime());
								
								int count = copyPost.size();
								if (count > QiupuConfig.POSTS_SERIALIZE_ITEM_COUNT) {
									count = QiupuConfig.POSTS_SERIALIZE_ITEM_COUNT;
								}
								
								out.writeInt(count);
								for (int i = 0; i < count; i++) {
									Stream item = copyPost.get(i);
									item.isFromSerialize = true;
									out.writeObject(item);
								}
								out.close();
							} else {
								Log.e(TAG, "serialization, fail to write read-only file: " + newsfeed_sfile);
							}
						} catch (IOException ex) {
							Log.d(TAG, "serialization fail=" + ex.getMessage());
						} finally {
							copyPost.clear();
						}
					}
				}
			}
		});
    }

    protected static final int GET_STREAM = 2;
    protected static final int GET_STREAM_END = 3;

    protected boolean isLoadingFromServer = false;
    Object circleLock = new Object();
    private long mLoadingCircleId;
    private long mPendingToLoadCircleId;
    protected boolean mForceRefresh;
    
    protected long mPendingLoadCategoryId;
    protected long mLoadingCategoryId;
    protected long mSelectCategoryId ;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_STREAM: {
                    Log.d(TAG, "handle message GET_STREAM: " + GET_STREAM);
                    if (msg.getData().getBoolean(RESULT, false)) {
                        //No need toast
                    	if (mPosts != null && mPosts.size() > 0) {
                    		long prelasttime = mPosts.get(0).created_time;
                    		Log.d(TAG, "loadNewPost");
                    		getStream(prelasttime, true);
                    	}else {
                    		getStream(0, true);
                    	}
                    }else {
                    	getStream(0, true);
                    }
                    break;
                }
                case GET_STREAM_END: {
                    end();

                    if (mListView != null) {
                        mListView.onRefreshComplete();
                    }

                    //process for UI
                    if (mPostAdapter != null) {
                        mPostAdapter.refreshLoadingStatus();
                    }

//                    if (mPosts.isEmpty()) {
//                        showListViewEmpty();
//                    }

                    if (!msg.getData().getBoolean(RESULT, false)) {
                        //No need toast
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

    private boolean isReadyForLoad() {
        synchronized (circleLock) {
            if (isLoadingFromServer) {
                return false;
            }
        }

        synchronized (circleLock) {
            isLoadingFromServer = true;
        }

        return true;
    }

    private void onLoadingReady(String promptText, boolean result) {
        synchronized (circleLock) {
            isLoadingFromServer = false;
        }

        if (mPendingToLoadCircleId != mLoadingCircleId || mPendingLoadCategoryId != mLoadingCategoryId) {
            Log.d(TAG, "onLoadingReady invoke stream loading.");
            if(mHandler == null)
        		return;
            
            Message msg = mHandler.obtainMessage(GET_STREAM);
			msg.getData().putBoolean(RESULT, true);
			msg.sendToTarget();
        } else {
        	if(mHandler != null)
        	{
	            Message msg = mHandler.obtainMessage(GET_STREAM_END);
	            msg.getData().putString(PROMPT, promptText);
	            msg.getData().putBoolean(RESULT, result);
	            msg.sendToTarget();
        	}
        }
    }
    
    private void mergeLoadingIdBeforeGetStream() {
    	if (mLoadingCircleId != mPendingToLoadCircleId) {
            mForceRefresh = true;
        }

        mLoadingCircleId = mMetaData.mCircleId;
        mPendingToLoadCircleId = mLoadingCircleId;
        
        if (mLoadingCategoryId != mPendingLoadCategoryId) {
            mForceRefresh = true;
        }

    	mLoadingCategoryId = mSelectCategoryId;
        mPendingLoadCategoryId = mLoadingCategoryId;
	}
    
    private boolean isLoadingIdEqualPendingId() {
    	return mLoadingCircleId == mPendingToLoadCircleId && mLoadingCategoryId == mPendingLoadCategoryId;
    }

    private void getStream(long prelasttime, final boolean newpost) {
    	if(mListView == null) {
    		return ;
    	}
    	if(!DataConnectionUtils.testValidConnection(mActivity)) {
    		ToastUtil.showCustomToast(mActivity, R.string.dlg_msg_no_active_connectivity, mHandler);
    		mListView.onRefreshComplete();
    		return ;	
    	}
    	
        if (!isReadyForLoad()) {
            Log.d(TAG, "getStream, in loading stream");
            return;
        }
        
        if(((BasicActivity)getActivity()).asyncQiupu == null)
        {
        	return;
        }

        begin();
        Log.d(TAG, "getStream, time = " + prelasttime + ", flag = " + newpost);

        mListView.onRefreshStart();

        //set load older button text process for UI
        mPostAdapter.refreshLoadingStatus();

        mergeLoadingIdBeforeGetStream();

        final String appKey = mMetaData.mSourceAppKey > 0 ? String.valueOf(mMetaData.mSourceAppKey) : "";
        ((BasicActivity)getActivity()).asyncQiupu.getPostTimeLine(AccountServiceUtils.getSessionID(), mMetaData.mUserId,
                mLoadingCircleId, pagesize, String.valueOf(prelasttime), newpost, appKey,
                mFilterType,mLoadingCategoryId,mMetaData.mFromHome, new TwitterAdapter() {
            public void getPostTimeLine(final List<Stream> posts) {
                if (QiupuConfig.LOGD) Log.d(TAG, "finish getStream posts:");
                boolean outdated = true;
                int updatedCount = 0;
                if (isLoadingIdEqualPendingId()) {
                    if (posts != null) {
                        updatedCount = posts.size();
                        if (updatedCount == 0 && !newpost) {
                            mOldestLoaded = true;
                        } else if (mForceRefresh && newpost) {
                            mOldestLoaded = false;
                        }

                        //TODO temp fix
                        if (newpost) {
                            outdated = getLastViewCount(posts, mForceRefresh);
                        } else {
                            outdated = getPreviousViewCount(posts);
                        }
                    }
                    mForceRefresh = false;
                    if (outdated) {
                    	mIsDirty = true;
                        InformationReadCache.ReadStreamCache.cache(posts);
                    }
                }


                onLoadingReady("", outdated);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "getStream, server exception:", ex, method);

                if (mForceRefresh) {
                    getLastViewCount(new ArrayList<Stream>(), mForceRefresh);
                }
                onLoadingReady(ex.getMessage(), false);
            }
        });
    }

    private boolean getPreviousViewCount(final List<Stream> streams) {
        if (null == streams || streams.isEmpty() || mHandler== null) {
            Log.d(TAG, "getPreviousViewCount, empty stream list.");
            return false;
        }

        mHandler.post(new Runnable() {
            public void run() {
                synchronized (this) {
                    mPosts.addAll(streams);
                    notifyDataSetChanged();
                }
            }
        });

        return true;
    }

    private boolean getLastViewCount(final List<Stream> sts, final boolean forrefresh) {
        if (sts == null || sts.size() == 0)
            return false;

        if(mHandler == null || getActivity().isFinishing())
        {
        	return false;
        }
        
        mHandler.post(new Runnable() {
            public void run() {
                synchronized (mPosts) {
                    //remove pre-serialize stream to let the stream is update to date
                    //			if(sts.size() == limitation && mPosts.size() > 0)

                    //just keep last 20 items
                    //if the return less than 20, it mean the serialized data is a little new
                    //so keetp it
                    //if the reuturn data is big than 20, remove all old serialized data
                    if (mPosts.size() > 0) {
                        if (mPosts.get(0).isFromSerialize) {
                            if (sts.size() >= pagesize) {
                                //remove all older stream;
                                mPosts.clear();
                            } else {
                                int removesize = (sts.size() + mPosts.size() - pagesize);
                                if (removesize > 0) {
                                    final int keepsize = mPosts.size() - removesize;
                                    while (mPosts.size() > keepsize) {
                                        mPosts.remove(mPosts.size() - 1);
                                    }
                                }
                            }
                        } else if (forrefresh) {
                            mPosts.clear();
                        }
                    }

//                    if (sts.size() > 0 && mPosts.size() > 0) {
//                        if (mPosts.get(0).isFromSerialize) {
//                            while (mPosts.size() >= pagesize / 2) {
//                                Stream item = mPosts.get(mPosts.size() - 1);
//
//                                mPosts.remove(mPosts.size() - 1);
//
//                                item.despose();
//                            }
//                        }
//                    }

                    // buggy, need to removed possible deleted item from mPost
//                    for (int i = 0; i < sts.size(); i++) {
//                        Stream item = sts.get(i);
//                        boolean isExist = false;
//                        for (int j = 0; j < mPosts.size(); j++) {
//                            Stream exist = mPosts.get(j);
//                            if (item.post_id.equalsIgnoreCase(exist.post_id)) {
//                                isExist = true;
//                                //update the content
//                                exist.despose();
//
//                                mPosts.set(j, item);
//                                break;
//                            }
//                        }
//
//                        if (isExist == false) {
//                            mPosts.add(item);
//                        }
//                    }

                    if (mPosts.isEmpty()) {
                        mPosts.addAll(sts);
                    } else  if (sts.isEmpty()) {
                        // need to do nothing
                    } else {
                        final int stsSize = sts.size();
                        final long mergingStopTime = mPosts.get(0).created_time;

                        Stream incomingItem = sts.get(stsSize - 1); // incoming stream that will be insert/replace in existing list.
                        final long loopStopTime = incomingItem.created_time;

                        if (loopStopTime > mergingStopTime) {
                            // insert all the incoming list to the top of the existing list
                            mPosts.removeAll(sts);
                            mPosts.addAll(0, sts);
                        } else {
                            // 1. remove intersect items from existing list (updated/deleted items)
                            // 2. insert all the incoming list
                            final int loopSize = mPosts.size();
                            ArrayList<Stream> intersectList = new ArrayList<Stream>(loopSize);
                            Stream item;
                            for (int i = 0; i < loopSize; i++) {
                                item = mPosts.get(i);
                                if (item.created_time < loopStopTime) {
                                    break; // caught the last intersect index
                                }
                                intersectList.add(item);
                            }

                            // First, remove all existing posts created within intersect time,
                            // Then, remove existing posts that was merged with new coming item (identical id)
                            // Last, merged both left items.
                            mPosts.removeAll(intersectList);
                            mPosts.removeAll(sts);
                            mPosts.addAll(0, sts);
                        }
                    }

                    Collections.sort(mPosts);
                }

                notifyDataSetChanged();
            }
        });

        return true;
    }

    public View.OnClickListener loadOlderClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "load older message");
            loadOlderPost();
        }
    };

    protected void loadOlderPost() {
        if (mPosts != null && mPosts.size() > 0) {
            long prelasttime = mPosts.get(mPosts.size() - 1).created_time;
            Log.d(TAG, "loadOlderPost");
            getStream(prelasttime, false);
        } else {
            Log.d(TAG, "loadOlderPost");
            getStream(0, false);
        }
    }

    public String getFragmentTitle() {
        return mMetaData != null ? mMetaData.mFragmentTitle : "";
    }

    public void setInitialIndex(int index) {
        mIndex = index;
    }

    protected boolean refreshPost(final String post_id, final Stream stream) {
        if (super.refreshPost(post_id, stream)) {
            mIsDirty = true;
            return true;
        }

        return false;
    }

    @Override
    public void loadNewPost(boolean isSecretly, String toUsers) {
        if (mMetaData != null && mMetaData.mUserId > 0) { // mUserid > 0 , get usertime
            if (mPosts != null && mPosts.size() > 0) {
                long prelasttime = mPosts.get(0).created_time;
                Log.d(TAG, "loadNewPost");
                getStream(prelasttime, true);
            }else {
            	getStream(0, true);
            }
        }
    }

    public boolean applyFilterType(int filterType) {
    	if(mHandler == null)
    		return false;
    	
        boolean isOutdated = false;

        final int target = mMetaData.mSourceFilter > 0 ? (mMetaData.mSourceFilter & filterType) : filterType;
        if (target != mFilterType) {
            mFilterType = target;
            isOutdated = true;
        } else {
            Log.d(TAG, "applyFilterType, ignore identical filter value:" + filterType);
        }

        if (isOutdated) {
            final List<Stream> oldPosts = mPosts;
            if (oldPosts != null && oldPosts.size() > 0) { //clear current content
                mHandler.post(new Runnable() {
                    public void run() {
                        oldPosts.clear();
                        notifyDataSetChanged();
                        loadRefresh();
                    }
                });
            } else {
                loadRefresh();
            }
        }

        return isOutdated;
    }

    public void filterStream() {
        if (isFilterNeeded()) {
            BpcPostsFilterActivity.startActivityForResult(getActivity(), mMetaData.mSourceFilter, mMetaData.mSourceAppKey,
                    mFilterType, BasicActivity.REQUEST_CODE_STREAM_FILTER);
        } else {
            Log.w(TAG, "filterStream, unexpected mInitAppId: " + mMetaData.mSourceAppKey + ", mFilterType:" + mFilterType);
        }
    }

    public boolean isFilterNeeded() {
        if (mMetaData.mSourceAppKey < 0 && mMetaData.mSourceFilter < 0) {
            return true;
        }

        return false;
    }

    protected String getNewsfile() {
    	final String oldfeed_sfile = QiupuHelper.posts_public + AccountServiceUtils.getBorqsAccountID() + mMetaData.mUserId + mPendingToLoadCircleId + mFilterType + mMetaData.mSourceAppKey;
    	return oldfeed_sfile; 
	}
    /**
     * TODO: handle such situation rather than simply call loadRefresh:
     * a. request switching while loading stream with other circle ID
     * b. request switching while loading stream with the same circle ID
     * c. request switching while ready to load.
     *
     * @param circleId
     */
    public boolean switchCircle(long circleId) {
    	if(mPendingToLoadCircleId == circleId) {
    		return false;
    	}
    	
    	if(mIsDirty) {
    		final String oldfeed_sfile = QiupuHelper.posts_public + AccountServiceUtils.getBorqsAccountID() + mMetaData.mUserId + mPendingToLoadCircleId + mFilterType + mMetaData.mSourceAppKey;

            StreamCacheManager.addCache(oldfeed_sfile, mPosts);
    		serialization(oldfeed_sfile);
    		mIsDirty = false;
//    	}else {
//    		for(Stream stream : mPosts) {
//    			stream.despose();
//    			stream = null;
//    		}
    	}
    	
        mPendingToLoadCircleId = circleId;
        mMetaData.mCircleId = circleId;
        
        newsfeed_sfile = QiupuHelper.posts_public + AccountServiceUtils.getBorqsAccountID() + mMetaData.mUserId + circleId + mFilterType + mMetaData.mSourceAppKey;

//        new SwitchCircleTask().execute();
        mPosts.clear();
        notifyDataSetChanged();
        new DeSerializationTask().execute();

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState, isDirty = " + mIsDirty);

        outState.putBoolean("isDirty", mIsDirty);
        outState.putString("savePath", newsfeed_sfile);
        if (/*mIsDirty*/true) {
            serialization(newsfeed_sfile);
            mIsDirty = false;
        }
    }

    private void restoreStreamList(Bundle savedInstanceState) {
        Log.d(TAG, "restoreStreamList, savedInstanceState = " + savedInstanceState);
        if (null != savedInstanceState) {
            if (savedInstanceState.getBoolean("isDirty")) {
                Log.d(TAG, "restoreStreamList, isDirty = true.");
            }
            final String savedPath = savedInstanceState.getString("savePath");
            if (!TextUtils.isEmpty(savedPath)) {
                newsfeed_sfile = savedPath;
                Log.d(TAG, "restoreStreamList, savedPath = " + savedPath);
            }
        }
        new DeSerializationTask().execute();
    }

	@Override
	public void refreshPostProfileImage(String imageurl) {
		for(Stream tmpPost : mPosts) {
			if(tmpPost != null && tmpPost.fromUser != null
					&& tmpPost.fromUser.uid == AccountServiceUtils.getBorqsAccountID()) {
				tmpPost.fromUser.profile_image_url = imageurl;
				mIsDirty = true;
			}
		}
		notifyDataSetChanged();
	}

    @Override
    protected void onPostRemovedInvoke() {
        mIsDirty = true;
    }

    @Override
    protected void onNewStreamInserted(int position) {
        mIsDirty = true;
    }

    @Override
    protected void onPhotoStreamReplaced() {
        mIsDirty = true;
    }

    @Override
    protected void onListViewRefresh() {
        Log.d(TAG, "OnRefreshListener.onRefresh.");
        Message msg = mHandler.obtainMessage(GET_STREAM);
        msg.getData().putBoolean(RESULT, true);
        msg.sendToTarget();
    }

	private void loadOlderPost(boolean formore, boolean forceget, int mShowItemCount) {
        int count = mPosts.size();
        Log.i(TAG, "count:" + count);
        if ((mShowItemCount == (count + mListView.getHeaderViewsCount() + 1) && formore) || forceget) {
        	loadOlderPost();
        }
    }

	@Override
	public void loadmore(boolean formore, boolean forceget, int count) {
		if(mPostAdapter.isNeedLoadMore()) {
			loadOlderPost(formore, forceget, count);
		}
	}

}
