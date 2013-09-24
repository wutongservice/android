package com.borqs.qiupu.fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.borqs.common.util.FileUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.AnimationImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.cache.StreamCacheManager;

import twitter4j.ApkBasicInfo;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream;
import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.StreamListAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.StreamActionListener;
import com.borqs.common.listener.TargetLikeActionListener;
import com.borqs.common.listener.TargetTopActionListener;
import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.common.view.CustomListView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.ui.BasicActivity;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-11-20
 * Time: 下午3:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractStreamListFragment extends BasicFragment implements
        TargetLikeActionListener, TargetTopActionListener,
        StreamActionListener {
    private static final String TAG = "AbstractStreamListFragment";

    protected Activity mActivity;
    protected Handler mHandler;
    protected Handler mBaseHandler;
    protected static final String RESULT = "result";
    protected static final String PROMPT = "PROMPT";

    protected List<Stream> mPosts = new ArrayList<Stream>();
    protected String newsfeed_sfile;
    protected StreamListAdapter mPostAdapter;
    protected CustomListView mListView;
    protected TextView mEmptyTextView;

    protected String mFragmentId;
    private View topBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	mBaseHandler = new BaseHandler();

        QiupuHelper.registerTargetTopListener(mFragmentId, this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView ");
        View view = inflater.inflate(R.layout.customized_list_view, container, false);
        mListView = (CustomListView) view.findViewById(R.id.custom_list_view);
        mEmptyTextView = (TextView) view.findViewById(R.id.empty_text_view);
        topBack = view.findViewById(R.id.back_to_top);
        setBackToTopListener();

        mListView.setonRefreshListener(new CustomListView.OnRefreshListener() {
            public void onRefresh() {
                onListViewRefresh();
            }
            public void onFootShown(boolean show) {
                View foot = getActivity().findViewById(R.id.bottom_actions_layout);
                onScrollingBottomView(show, foot);
                onScrollingStateIdleView(!show, topBack);
            }
        });

//        setScrollingListener();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setBackToTopListener() {
        if (null != topBack) {
            topBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListView.setSelection(0);
                    topBack.setVisibility(View.GONE);
                    AnimationImageRun.fadeOutAnimate(topBack);
                }
            });
        }
    }

    public void onScrollingBottomView (boolean toShow, final View view) {
        if (view != null) {
            if (toShow) {
                if (View.GONE == view.getVisibility()) {
                    view.setVisibility(View.VISIBLE);
                    AnimationImageRun.fadeInAnimate(view);
                }
            } else {
                if (View.VISIBLE == view.getVisibility()){
                    view.setVisibility(View.GONE);
                    AnimationImageRun.fadeOutAnimate(view);
                }
            }
        }
    }
    
    private static void onScrollingStateIdleView (boolean toShow, final View view) {
        if (view != null) {
            if (toShow) {
                if (View.GONE == view.getVisibility()) {
                    view.setVisibility(View.VISIBLE);
                    AnimationImageRun.fadeInAnimate(view);
                }
            } else {
                if (View.VISIBLE == view.getVisibility()){
                    view.setVisibility(View.GONE);
                    AnimationImageRun.fadeOutAnimate(view);
                }
            }
        }
    }
//
//    private static void onScrollingStateFlingView(View view) {
//        if (null != view && View.VISIBLE == view.getVisibility()) {
//            view.setVisibility(View.GONE);
//            AnimationImageRun.fadeOutAnimate(view);
//        }
//    }

    private void onScrollingStateIdle(int firstVisibleIndex) {
        if (firstVisibleIndex > 1) {
            if (null == topBack) {
            } else {
                if (View.GONE == topBack.getVisibility()) {
                    topBack.setVisibility(View.VISIBLE);
                    AnimationImageRun.fadeInAnimate(topBack);
                    topBack.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (View.VISIBLE == topBack.getVisibility()) {
                                topBack.setVisibility(View.GONE);
                                AnimationImageRun.fadeOutAnimate(topBack);
                            }
                        }
                    }, 2500);
                }
            }
        } else {
            onScrollingStateIdleView(false, topBack);
        }
    }

    private void setScrollingListener() {
        if (null != topBack || null != getActivity().findViewById(R.id.bottom_actions_layout)) {
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        final int firstVisibleIndex = view.getFirstVisiblePosition();
                        onScrollingStateIdle(firstVisibleIndex);
                    } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        onScrollingStateIdleView(false, topBack);
                    }

                    mListView.onScrollStateChanged(view, scrollState);
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    mListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

                }
            });
        }
    }

    protected void showListViewEmpty() {
        mEmptyTextView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    @Override
    public void onTargetLikeCreated(String targetId, String targetType) {
        final int size = mPosts.size();
        final boolean isApkTarget = QiupuConfig.TYPE_APK.equals(targetType);
        final boolean isStreamTarget = QiupuConfig.TYPE_STREAM.equals(targetType);
        Stream stream;
        for (int i = 0; i < size; ++i) {
            stream = mPosts.get(i);
            if (isApkTarget && BpcApiUtils.isValidTypeOfAppAttachment(stream.type)) {
                Stream.AttachmentBase attachment = stream.attachment;
                if (null != attachment && attachment instanceof Stream.ApkAttachment) {
                    if (null != attachment.attachments && attachment.attachments.size() > 0) {
                        final ApkBasicInfo apkInfo = (ApkBasicInfo) attachment.attachments.get(0);
                        if (apkInfo.apk_server_id.equals(targetId)) {
                            final long uid = AccountServiceUtils.getBorqsAccountID();
                            apkInfo.iLike = true;
                            apkInfo.likes.count++;
                            QiupuSimpleUser user = AccountServiceUtils.touchMySimpleUserInfo();
                            stream.likes.friends.add(user);
                            refreshPostLike(stream.post_id, stream);
                            return;
                        }
                    }
                }
            } else if (isStreamTarget && stream.post_id.equals(targetId)) {
                stream.iLike = true;
                QiupuSimpleUser user = AccountServiceUtils.touchMySimpleUserInfo();
                if (null != user) {
                    stream.likes.count++;
                    stream.likes.friends.add(user);
                }
                refreshPostLike(stream.post_id, stream);
            }
        }
    }

    @Override
    public void onTargetLikeRemoved(String targetId, String targetType) {
        final int size = mPosts.size();
        final boolean isApkTarget = QiupuConfig.TYPE_APK.equals(targetType);
        final boolean isStreamTarget = QiupuConfig.TYPE_STREAM.equals(targetType);
        Stream stream;
        for (int i = 0; i < size; ++i) {
            stream = mPosts.get(i);
            if (isApkTarget && BpcApiUtils.isValidTypeOfAppAttachment(stream.type)) {
                Stream.AttachmentBase attachment = stream.attachment;
                if (null != attachment && attachment instanceof Stream.ApkAttachment) {
                    if (null != attachment.attachments && attachment.attachments.size() > 0) {
                        final ApkBasicInfo apkInfo = (ApkBasicInfo) attachment.attachments.get(0);
                        if (apkInfo.apk_server_id.equals(targetId)) {
                            final long uid = AccountServiceUtils.getBorqsAccountID();

                            apkInfo.iLike = false;
                            apkInfo.likes_count--;
                            for (QiupuSimpleUser user : apkInfo.likes.friends) {
                                if (user.uid == uid) {
                                    apkInfo.likes.friends.remove(user);
                                    break;
                                }
                            }
                            refreshPostUnlike(stream.post_id, stream);
                            return;
                        }
                    }
                }
            } else if (isStreamTarget && stream.post_id.equals(targetId)) {
                final long uid = AccountServiceUtils.getBorqsAccountID();

                stream.iLike = false;
                for (QiupuSimpleUser user : stream.likes.friends) {
                    if (user.uid == uid) {
                        stream.likes.count--;
                        stream.likes.friends.remove(user);
                        break;
                    }
                }
                refreshPostUnlike(stream.post_id, stream);
            }
        }
    }

    @Override
    public void onTargetTopCreated(String group_id, String stream_id) {
        boolean isDirty = false;
        final int size = mPosts.size();
        Stream stream;
        for (int i = 0; i < size; ++i) {
            stream = mPosts.get(i);
            if (stream.post_id.equals(stream_id)) {
                stream.top_in_targets = stream.top_in_targets + "," + group_id;
                refreshPost(stream_id, stream);
                isDirty = true;
            }
        }
        if (isDirty) {
            notifyTopListChanged();
        }
    }

    @Override
    public void onTargetTopCancel(String group_id, String stream_id) {
        boolean isDirty = false;
        final int size = mPosts.size();
        Stream stream;
        for (int i = 0; i < size; ++i) {
            stream = mPosts.get(i);
            if (stream.post_id.equals(stream_id)) {
                isDirty = true;
                String[] array = stream.top_in_targets.split(",");
                ArrayList<String> targetList = new ArrayList<String>();
                for (String target: array) {
                    if (TextUtils.isEmpty(target) == false) {
                        targetList.add(target);
                    }
                }

                for (String target: targetList) {
                    if (targetList.contains(group_id)) {
                        targetList.remove(target);
                        break;
                    }
                }

                StringBuilder ids = new StringBuilder();
                if (targetList.size() > 0) {
                    for (int j = 0; j < targetList.size(); j++) {
                        if (ids.length() > 0) {
                            ids.append(",");
                        }
                        ids.append(targetList.get(i));
                    }
                }
                stream.top_in_targets = ids.toString();
                refreshPost(stream_id, stream);
            }
        }
        if (isDirty) {
            notifyTopListChanged();
        }
    }

    private void refreshPostLike(String post_id, Stream stream) {
        final int count = mPostAdapter.getCount();
        for (int j = 0; j < count; j++) {
            View v = mListView.getChildAt(j);
            if (AbstractStreamRowView.class.isInstance(v)) {
                AbstractStreamRowView fv = (AbstractStreamRowView) v;
                if (fv.refreshPostLike(post_id, stream)) {
                    break;
                }
            }
        }
    }

    private void refreshPostUnlike(String post_id, Stream stream) {
        final int count = mPostAdapter.getCount();
        for (int j = 0; j < count; j++) {
            View v = mListView.getChildAt(j);
            if (AbstractStreamRowView.class.isInstance(v)) {
                AbstractStreamRowView fv = (AbstractStreamRowView) v;
                if (fv.refreshPostUnlike(post_id, stream)) {
                    break;
                }
            }
        }
    }
    protected boolean refreshPost(final String post_id, final Stream stream) {
        final int count = mPostAdapter.getCount();
        for (int j = 0; j < count; j++) {
            View v = mListView.getChildAt(j);
            if (AbstractStreamRowView.class.isInstance(v)) {
                AbstractStreamRowView fv = (AbstractStreamRowView) v;
                if (fv.refreshItem(post_id, stream)) {
                    return true;
                }
            }
        }
        return false;
    }


    protected void notifyDataSetChanged() {
        if (mPostAdapter != null) {
            mPostAdapter.alterDataList(mPosts);
        } else {
            android.util.Log.d("AbstractStreamListFragment", "notifyDataSetChanged() mPostAdapter = " + mPostAdapter);
        }
    }


    protected void showCustomToast(String textMsg) {
        if (mActivity instanceof BasicActivity) {
            ((BasicActivity) mActivity).showCustomToast(textMsg);
        }
    }


    @Override
    public void updateStreamCommentStatus(final String postid, final boolean canComment,
                                          final boolean canLike, final boolean canReshare) {
        final int size = mPosts.size();
        for (int i = 0; i < size; i++) {
            Stream post = mPosts.get(i);
            if (post.post_id.equals(postid)) {
                if (post.comments != null) {

                } else {
                    post.comments = new Stream.Comments();
                }
                post.canComment = canComment;
                post.canLike = canLike;
                post.canReshare = canReshare;
                refreshPost(post.post_id, post);
                break;
            }
        }

    }

    @Override
    public void updateStreamRemovedUI(String postid, long uid) {
        if(mPosts == null) {
        	return ;
        }

        boolean itemRemoved = false;
        if (uid > 0) {
            ArrayList<Stream> removedList = new ArrayList<Stream>(mPosts.size());
            for (Stream post : mPosts) {
                if (uid == post.fromUser.uid) {
                    removedList.add(post);
                }
            }
            if (!removedList.isEmpty()) {
                mPosts.removeAll(removedList);
                itemRemoved = true;
            }
        } else if (!TextUtils.isEmpty(postid)) {
            final int size = mPosts.size();
            for (int i = 0; i < size; i++) {
                if (mPosts.get(i).post_id.equals(postid)) {
//                    invokeRemovePostRunnable(i);
                    mPosts.remove(i);
                    itemRemoved = true;
                    break;
                }
            }
//            for (Stream post : mPosts) {
//                if (postid.equals(post.post_id)) {
//                    mPosts.remove(post);
//                    itemRemoved = true;
//                }
//            }
        }

        if (itemRemoved) {
            if (null != mActivity) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                        onPostRemovedInvoke();
                    }
                });
            } else if (null != mHandler) {
                mHandler.post(new Runnable() {
                    public void run() {
                        notifyDataSetChanged();
                        onPostRemovedInvoke();
                    }
                });
            }
        }
    }

    @Override
    public void updatePhotoStreamUI(final Stream stream) {
        if (stream != null) {
            final int size = mPosts.size();
            for (int i = 0; i < size; i++) {
                if (mPosts.get(i).post_id.equals(stream.post_id)) {
                    final int index = i;
                    mHandler.post(new Runnable() {
                        public void run() {
                            mPosts.remove(index);
                            mPosts.add(index, stream);
                            notifyDataSetChanged();
                            onPhotoStreamReplaced();
                        }
                    });
                    break;
                }
            }
        }
    }

    @Override
    public void updateStreamCommentUI(final Stream stream, final int commentType, final int commentCount,
                                      final ArrayList<Stream.Comments.Stream_Post> streamComment) {
        final String postId = null == stream ? "" : stream.post_id;
        if(mHandler == null)
        {
        	return;
        }

        mHandler.post(new Runnable() {
            public void run() {
                int insertIndex = 0;

                for (Stream post : mPosts) {
                    if (post.post_id.equals(postId)) {
                        if (post.comments == null) {
                            post.comments = new Stream.Comments();
                        }
                        post.comments.alterCommentList(streamComment, commentCount);
                        refreshPost(postId, post);
                        insertIndex = -1;
                        return;
                    } else if (BpcApiUtils.isValidTypeOfAppAttachment(commentType) &&
                            BpcApiUtils.isValidTypeOfAppAttachment(post.type)) {
                        Stream.AttachmentBase attachment = post.attachment;
                        if (null != attachment && attachment instanceof Stream.ApkAttachment) {
                            if (null != attachment.attachments && attachment.attachments.size() > 0) {
                                final ApkBasicInfo apkInfo = (ApkBasicInfo) attachment.attachments.get(0);
                                if (apkInfo.apk_server_id.equals(postId)) {
                                    if (apkInfo.comments != null) {
                                    } else {
                                        apkInfo.comments = new Stream.Comments();
                                    }
                                    apkInfo.comments.alterCommentList(streamComment, commentCount);
                                    refreshPost(post.post_id, post);
                                    return;
                                }
                            }
                        }
                    } else if (stream.created_time < post.created_time) {
                        ++insertIndex;
                    }
                }

                if (insertIndex > 0 && insertIndex < mPosts.size()) {
                    mPosts.add(insertIndex, stream);
                    notifyDataSetChanged();
                    onNewStreamInserted(insertIndex);
                }
            }
        });
    }

    protected void onPostRemovedInvoke() {
    }

    protected void onNewStreamInserted(int position) {
    }

    protected void onPhotoStreamReplaced() {
    }


//    protected class DeSerializationTask extends UserTask<Void, Void, Integer> {
//    	@Override
//    	public void onPreExecute() {
//    	}
//    	
//    	@Override
//    	public Integer doInBackground(Void... params) {
//    		return deSerialization();
//    	}
//    	
//    	@Override
//    	public void onPostExecute(Integer param) {
//    		onSerializedCompleted(param);
//    	}
//    }
    protected class DeSerializationTask{
        public void execute() {
        	ArrayList<Stream> cacheStream = StreamCacheManager.getCacheStreamList(newsfeed_sfile);
            if (null != cacheStream) {
                mPosts = cacheStream;
                Log.d(TAG, "deSerialization, all in cache, size: " + mPosts.size());
                notifyDataSetChanged();
                return;
            }


            if (TextUtils.isEmpty(newsfeed_sfile)) {
                Log.w(TAG, "deSerialization, skip with invalid file name: " + newsfeed_sfile);
                return ;
            }

            new Thread(TAG){
				@Override
				public void run() {
					int result = deSerialization();
                    if (null == mBaseHandler) {
                        mBaseHandler = new BaseHandler();
                    }
					Message msg = mBaseHandler.obtainMessage(result);
					mBaseHandler.sendMessage(msg);
					
				}
			}.start();
        }
    }

    protected int deSerialization() {
        int result = -1;

//        ArrayList<Stream> cacheStream = StreamCacheManager.getCacheStreamList(newsfeed_sfile);
//        if (null != cacheStream) {
//            mPosts = cacheStream;
//            Log.d(TAG, "deSerialization, all in cache, size: " + mPosts.size());
//            return 1;
//        }
//
//
//        if (null == mHandler || TextUtils.isEmpty(newsfeed_sfile)) {
//            Log.w(TAG, "deSerialization, skip with invalid handler or file name: " + newsfeed_sfile);
//            return result;
//        }

//        result = -2;

        synchronized (mPosts) {
            FileInputStream fis = null;
            ObjectInputStream in = null;
            try {
                Log.d(TAG, "deSerialization : " + newsfeed_sfile);
                if (FileUtils.testReadFile(new File(newsfeed_sfile))) {
                    fis = new FileInputStream(newsfeed_sfile);
                    in = new ObjectInputStream(fis);
                    long lastrecord = in.readLong();
                    Date now = new Date();

                    final long interval = now.getTime() - lastrecord;
                    final long deltaHours = interval / QiupuConfig.AN_HOUR;
//                    if (deltaHours > 720) {  // 30 * 24 hours
//                        Log.d(TAG, String.format("it is %1$s hours ago, ignore the data",
//                                deltaHours));
//                    } else {
                        int count = in.readInt();
                        ArrayList<Stream> readList = new ArrayList<Stream>();
                        for (int i = 0; i < count; i++) {
                            Stream item = (Stream) in.readObject();
                            readList.add(item);
                        }
                        mPosts = readList;
                        result = NOTIFY_DATA_CHANGE;
//                    }
                    in.close();

                    if (deltaHours > 12L) {
                        Log.d(TAG, "load stream after deSerialization, interval=" + interval +
                                ", delta hours = " + deltaHours);
                        result = LOAD_REFRESH;
                    }
                } else {
                    Log.e(TAG, "cached stream file could not be read: " + newsfeed_sfile);
                    result = LOAD_REFRESH;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                try {
                    new File(newsfeed_sfile).delete();
                } catch (Exception ne) {
                }
                Log.d(TAG, "deSerialization fail=" + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                Log.d(TAG, "deSerialization fail=" + ex.getMessage());
            }
        }

        return result;
    }


    protected void onSerializedCompleted(Integer param) {
        final int result = param.intValue();
        switch (result) {
            case 0:
                loadRefresh();
                break;
            case 1:
                notifyDataSetChanged();
                break;
            default:
                Log.d(TAG, "onPostExecute, result = " + param);
                break;
        }
    }
    
    private final int LOAD_REFRESH = 0;
    private final int NOTIFY_DATA_CHANGE = 1;
    private class BaseHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_REFRESH:
                    notifyDataSetChanged();
                    loadRefresh();
                    break;
                case NOTIFY_DATA_CHANGE:
                	notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }

    public void loadRefresh() {
    }
    
    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        StreamCacheManager.addCache(newsfeed_sfile, mPosts);
    }

    @Override
    public void onDestroy() {
        QiupuHelper.unRegisterTargetTopListener(mFragmentId);
        super.onDestroy();
    }

    private void notifyTopListChanged() {
        final Activity activity = getActivity();
        if (null != activity && activity instanceof AbstractStreamRowView.SetTopInterface) {
            AbstractStreamRowView.SetTopInterface callback = (AbstractStreamRowView.SetTopInterface)activity;
            callback.notifyTopListChanged();
        }
    }

    protected abstract void onListViewRefresh();
}
