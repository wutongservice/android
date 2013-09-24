package com.borqs.qiupu.fragment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import twitter4j.AsyncQiupu;
import twitter4j.Stream;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.StreamListAdapter;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.TwitterExceptionUtils;
import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.ui.BasicActivity;
import com.borqs.qiupu.util.ToastUtil;

public class TopPostListFragment extends AbstractStreamListFragment {
    private static final String TAG = "TopPostListFragment";
    private AsyncQiupu mAsyncQiupu;
    private String mTitle;
    private String mCircleId;

    public TopPostListFragment() {
        super();
    }

    public TopPostListFragment(String circle_id, AsyncQiupu asyncQiupu) {
        mCircleId = circle_id;
        mAsyncQiupu = asyncQiupu;
    }

    public interface TopPostListFragmentCallBack {

        public String getSerializeFilePath();
        public long getId();
        public void setTitle(String newTitle);
        public void showErrorToast(String reason, boolean isShort);
    }

    private long  id;
    private TopPostListFragmentCallBack listener;

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach ");
        super.onAttach(activity);

        mActivity = activity;

        if (mActivity instanceof TopPostListFragmentCallBack) {
            listener = (TopPostListFragmentCallBack) activity;

            newsfeed_sfile = listener.getSerializeFilePath();
            id = listener.getId();
        }
        mFragmentId = getClass().getName() + newsfeed_sfile;
    }

    private void registerListeners() {
        QiupuHelper.registerStreamListener(mFragmentId, this);
        QiupuHelper.registerTargetLikeListener(mFragmentId, this);
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

        registerListeners();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setOnCreateContextMenuListener(mActivity);

        mPostAdapter = StreamListAdapter.newInstance(mActivity, null, false);
        mPostAdapter.showLoadMoreButton(false);
        mListView.setAdapter(mPostAdapter);
        AbstractStreamRowView.attachListViewItemClickerContext(mActivity, mListView);

        new DeSerializationTask().execute();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestory: ");
        serialization(newsfeed_sfile);

        unregisterListeners();

        super.onDestroy();

        mActivity = null;
        mHandler = null;
        mPostAdapter = null;
        if(mListView != null) {
        	mListView.removeAllViewsInLayout();
        	mListView = null;
        }
        mFragmentId = null;

    }


    public void loadRefresh() {
        if(mHandler != null) {
            Log.d(TAG, "loadRefresh.");
            mHandler.obtainMessage(GET_TOP_POST).sendToTarget();
        }
    }

    private void serialization(final String filePath) {
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

    private static final int GET_TOP_POST = 2;
    private static final int GET_TOP_POST_END = 3;
    private static final int UPDATE_TITLE = 4;
    private static final int UPDATE_TITLE_END = 5;

    private boolean isLoadingFromServer = false;
    Object circleLock = new Object();
    private long mLoadingCircleId;
    private long mPendingToLoadCircleId;
    private boolean mForceRefresh;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_TOP_POST: {
                    Log.d(TAG, "handle message GET_TOP_POST: " + GET_TOP_POST);
                    getTopPost();
                    break;
                }
                case GET_TOP_POST_END: {
                    end();

                    if (mPosts.isEmpty()) {
                        if (updateTopListTitleListener != null && updateTopListTitleListener.get() != null) {
                            updateTopListTitleListener.get().hideTopListTitle();
                        }
                        showListViewEmpty();
                    }

                    mListView.onRefreshComplete();

                    //process for UI
                    mPostAdapter.refreshLoadingStatus();

                    if (!msg.getData().getBoolean(RESULT, false)) {
                        //No need toast
                    }

                    final String promptText = msg.getData().getString(PROMPT);
                    if (!TextUtils.isEmpty(promptText)) {
                        showCustomToast(promptText);
                    }
                    notifyDataSetChanged();
                    break;
                }
                case UPDATE_TITLE: {
                    updateTitle();
                    break;
                }
                case UPDATE_TITLE_END: {
                    try {
                        mActivity.dismissDialog(DIALOG_PROFILE_UPDATE_SERVER);
                    } catch (Exception e) {
                    }
                    boolean suc = msg.getData().getBoolean(RESULT);
                    if (suc) {
                        ToastUtil.showShortToast(mActivity, mHandler, R.string.operate_succeed);
                        setTitle();
                        if (updateTopListTitleListener != null && updateTopListTitleListener.get() != null) {
                            updateTopListTitleListener.get().setTopListTitle(mTitle);
                        }
                    } else {
                        String ErrorMsg = msg.getData().getString("ERROR_MSG");
                        if (TextUtils.isEmpty(ErrorMsg)) {
                            if (listener != null) {
                                listener.showErrorToast(ErrorMsg, true);
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private void setTitle() {
        if (listener != null) {
            listener.setTitle(mTitle);
        } else {
            Log.d(TAG, "setTitle() listener is null");
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

    private void onLoadingReady(String promptText,boolean result) {
        synchronized (circleLock) {
            isLoadingFromServer = false;
        }
        if(mHandler != null)
        {
            Message msg = mHandler.obtainMessage(GET_TOP_POST_END);
            if(TextUtils.isEmpty(promptText) == false) {
                msg.getData().putString(PROMPT, promptText);
            }
            msg.getData().putBoolean(RESULT, result);
            msg.sendToTarget();
        }
    }

    private void getTopPost() {
    	if(mListView == null) {
    		return ;
    	}
    	if(!DataConnectionUtils.testValidConnection(mActivity)) {
    		ToastUtil.showCustomToast(mActivity, R.string.dlg_msg_no_active_connectivity, mHandler);
    		mListView.onRefreshComplete();
    		return ;	
    	}
    	
        if (!isReadyForLoad()) {
            Log.d(TAG, "getTopPost, in loading post");
            return;
        }
        
        if(((BasicActivity)getActivity()).asyncQiupu == null)
        {
        	return;
        }

        begin();

        mListView.onRefreshStart();

        //set load older button text process for UI
        mPostAdapter.refreshLoadingStatus();

        if (mLoadingCircleId != mPendingToLoadCircleId) {
            mForceRefresh = true;
        }

        mPendingToLoadCircleId = mLoadingCircleId;

        ((BasicActivity)getActivity()).asyncQiupu.getPostTop(AccountServiceUtils.getSessionID(), id, new TwitterAdapter() {
            
            @Override
            public void getPostTop(List<Stream> posts) {
                Log.d(TAG, "getPostTop() posts = " + posts);
                Collections.sort(posts);
                mPosts.clear();
                mPosts = posts;
                onLoadingReady(null,true);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                TwitterExceptionUtils.printException(TAG, "getPostTop, server exception:", ex, method);
                
                onLoadingReady(ex.getMessage(),false);
            }
        });
    }



    public View.OnClickListener loadOlderClick = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "load older message");
            loadOlderPost();
        }
    };

    protected void loadOlderPost() {
        getTopPost();
    }

    public void updateTopListTitle(String title) {
        mTitle = title;
        mHandler.obtainMessage(UPDATE_TITLE).sendToTarget();
    }

    private Object mEditInfoLock = new Object();
    private boolean inEditProcess;
    private static final int DIALOG_PROFILE_UPDATE_SERVER = 15;

    private void updateTitle() {
        if (!AccountServiceUtils.isAccountReady()) {
            return;
        }

        synchronized (mEditInfoLock) {
            if (inEditProcess == true) {
                Log.d(TAG, "in update top lsit title");
                return;
            }
        }
        mActivity.showDialog(DIALOG_PROFILE_UPDATE_SERVER);

        synchronized (mEditInfoLock) {
            inEditProcess = true;
        }

        Log.d(TAG, "updateTitle() mTitle = " + mTitle);
        HashMap<String, String> coloumsMap = new HashMap<String, String>();
        coloumsMap.put("id", mCircleId);
        coloumsMap.put("top_name", mTitle);

        mAsyncQiupu.editPulbicCircle(AccountServiceUtils.getSessionID(), coloumsMap, true,
                new TwitterAdapter() {
                    public void editPulbicCircle(boolean result) {
                        Log.d(TAG, "finish edit top list title");
                        synchronized (mEditInfoLock) {
                            inEditProcess = false;
                        }

                        Message msg = mHandler.obtainMessage(UPDATE_TITLE_END);
                        msg.getData().putBoolean(RESULT, result);
                        msg.sendToTarget();
                    }

                    public void onException(TwitterException ex, TwitterMethod method) {

                        synchronized (mEditInfoLock) {
                            inEditProcess = false;
                        }
                        TwitterExceptionUtils.printException(TAG, "updateTitleInfo, server exception:", ex, method);

                        Message msg = mHandler.obtainMessage(UPDATE_TITLE_END);
                        msg.getData().putBoolean(RESULT, false);
                        msg.getData().putString("ERROR_MSG", ex.getMessage());
                        msg.sendToTarget();
                    }
                });
    }

    private static WeakReference<UpdateTitleUICallBack> updateTopListTitleListener;

    public static void setTopListTitleListener(UpdateTitleUICallBack listener) {
        updateTopListTitleListener = new WeakReference<UpdateTitleUICallBack>(listener);
    }

    public interface UpdateTitleUICallBack {
        public void setTopListTitle(String title);
        public void hideTopListTitle();
    }

    @Override
    protected void onListViewRefresh() {
        Log.d(TAG, "onListViewRefresh enter");
        Message msg = mHandler.obtainMessage(GET_TOP_POST);
        msg.sendToTarget();
    }
}
