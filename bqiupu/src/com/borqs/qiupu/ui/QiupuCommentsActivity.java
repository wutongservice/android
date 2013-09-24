package com.borqs.qiupu.ui;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twitter4j.ApkBasicInfo;
import twitter4j.ApkResponse;
import twitter4j.ErrorResponse;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream;
import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.CommentSettingAdapter;
import com.borqs.common.adapter.CommentsAdapter;
import com.borqs.common.adapter.StreamListAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.TargetLikeActionListener;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.AbstractStreamRowView;
import com.borqs.common.view.ApplicationItemView;
import com.borqs.common.view.CommentItemView;
import com.borqs.common.view.CommentSettingItemView;
import com.borqs.common.view.ConversationMultiAutoCompleteTextView;
import com.borqs.common.view.KeyboardLayout;
import com.borqs.common.view.KeyboardLayout.onKybdsChangeListener;
import com.borqs.common.view.MyLinkMovementMethod;
import com.borqs.information.InformationHttpPushReceiver;
import com.borqs.information.db.NotificationOperator;
import com.borqs.information.util.InformationReadCache;
import com.borqs.information.util.InformationUtils;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.cache.StreamCacheManager;
import com.borqs.qiupu.ui.bpc.QiupuComposeActivity;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class QiupuCommentsActivity extends QiupuBaseCommentsActivity implements TargetLikeActionListener {
    private static final String TAG = "Qiupu.QiupuCommentsActivity";

    private ListView stream_comments_list;

    private Stream streamForComment;
    private ApkResponse appForComment;

    private String post_id;

    private boolean isapk;
    private boolean islike;
    private int type;

    private AbstractStreamRowView streamItemView;

    private int mTotalCommentCount;
    private List<Stream_Post> mDisplayCommentList = new ArrayList<Stream_Post>();

    private TextWatcher mTextWatcher;
    private View comments_mention_button;
    private TextView mComment_count_Span;
    
    private String has_same_photo_count="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_ui);
        setHeadTitle(R.string.app_comments); 
        KeyboardLayout mainView = (KeyboardLayout) findViewById(R.id.keyboardLayout1);
        mainView.setOnkbdStateListener(new onKybdsChangeListener() {
			
			@Override
			public void onKeyBoardStateChange(int state) {
				if(state == KeyboardLayout.KEYBOARD_STATE_HIDE) {
					
				}else if(state == KeyboardLayout.KEYBOARD_STATE_SHOW) {
					lastDownKeyCode = 0;
				}
			}
		});
        stream_comments_list = (ListView) findViewById(R.id.stream_comments_list);
        CommentItemView.setCommentActionListener(this);

        final View comments_share_button = findViewById(R.id.comments_share_button);

        mCommentText = ((ConversationMultiAutoCompleteTextView)findViewById(R.id.compose_text));
        final boolean enableCommitBtn = null != mCommentText && !TextUtils.isEmpty(mCommentText.getText());
        comments_share_button.setEnabled(enableCommitBtn);
        mTextWatcher = new MyTextWatcher(comments_share_button);
        this.mCommentText.addTextChangedListener(mTextWatcher);

        showMiddleActionBtn(true);
        showRightActionBtn(false);
        enableLeftActionBtn(false);
        
        overrideMiddleActionBtn(R.drawable.actionbar_icon_praise_normal, likeClick);

        comments_share_button.setOnClickListener(new CommentBtnClickListener());

        comments_mention_button = findViewById(R.id.comments_mention_button);
        if (null != comments_mention_button) {
            comments_mention_button.setOnClickListener(ConversationMultiAutoCompleteTextView.instanceMentionButtonClickListener(this, mCommentText));
        }

        processIntent(getIntent());

        QiupuHelper.registerTargetLikeListener(getClass().getName(), this);

        ensureAccountLogin();

    }

    public boolean isShowNotification()
    {
    	return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        final String streamId = BpcApiUtils.parseSchemeValue(intent, BpcApiUtils.SEARCH_KEY_ID);
        if (TextUtils.isEmpty(streamId)) {
            parseStreamFromIntentBundle(intent);
        } else {
            post_id = streamId;
            isapk = isAppSchema(intent);
            streamForComment = StreamCacheManager.getCacheItem(post_id);
            if (null == streamForComment) {
                mHandler.obtainMessage(STREAM_LOAD).sendToTarget();
            } else {
                if (null != streamForComment && null != streamForComment.comments) {
                    mTotalCommentCount = streamForComment.comments.getCount();
                } else {
                    mTotalCommentCount = 0;
                }
                applyUpdatedStream(true, false);
                setCommentButtonStatus();
            }
        }
    }

    private ListView mSetListView;
    @Override
    protected void setCommentSettingListener() {
        mSetListView = (ListView) getLayoutInflater().inflate(R.layout.default_listview, null);
        mSetListView.setBackgroundResource(R.color.white);
        mSetListView.setAdapter(new CommentSettingAdapter(this, streamForComment.canComment, streamForComment.canLike, streamForComment.canReshare));
        mSetListView.setOnItemClickListener(listItemListener);
        DialogUtils.ShowDialogwithView(this, getString(R.string.stream_setting),
                0, mSetListView, positiveListener, negativeListener);
    }

    private AdapterView.OnItemClickListener listItemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (CommentSettingItemView.class.isInstance(view)) {
                CommentSettingItemView commentView = (CommentSettingItemView) view;
                commentView.setCheckedStatus();
            }
        }
    };

    @Override
    protected void updateCommentActivityUI(String post_id, boolean canComment,
            boolean canLike, boolean canReshare) {
        if (streamForComment != null && streamForComment.post_id.equals(post_id)) {
            streamForComment.canComment = canComment;
            streamForComment.canLike = canLike;
            streamForComment.canReshare = canReshare;
        }
        applyUpdatedStream(true, false);
        setCommentButtonStatus();
    }

    private DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mSetListView != null && mSetListView.getCount() > 0) {
                boolean[] canParameter = new boolean[mSetListView.getCount()];
                for (int i = 0, count = mSetListView.getCount(); i < count; i++) {
                    canParameter[i] = ((CompoundButton)mSetListView.getChildAt(i).findViewById(R.id.item_button)).isChecked();
                }
                if (canParameter[0] == streamForComment.canComment && canParameter[1] == streamForComment.canLike && canParameter[2] == streamForComment.canReshare) {
                    Log.d(TAG, "No status change");
                } else {
                    updateStreamSetting(streamForComment.post_id, canParameter[0], canParameter[1], canParameter[2]);
                }
            } else {
                Log.d(TAG, "Set ListView is null.");
            }
        }
    };

    private DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            
        }
    };

    private void overrideAndShowRightActionBtn(int resId, OnClickListener listener) {
        super.showRightActionBtn(true);
        super.overrideRightActionBtn(resId, listener);
    }

    private void parseStreamFromIntentBundle(Intent intent) {
        post_id = intent.getStringExtra(BpcApiUtils.SEARCH_KEY_ID);
        isapk = intent.getBooleanExtra(QiupuMessage.BUNDLE_POST_APK, false);

        if (isapk == true) {
            appForComment = (ApkResponse) intent.getSerializableExtra(QiupuMessage.BUNDLE_APKINFO);
            if (null != appForComment && null != appForComment.comments) {
                mTotalCommentCount = appForComment.comments.getCount();
            } else {
                mTotalCommentCount = 0;
            }

            applyUpdatedApp(true);
        } else {
            boolean ser = intent.getBooleanExtra(QiupuMessage.BUNDLE_STREAM_IN_FILE, false);
            if (ser) {
            	streamForComment = QiupuHelper.deSerialization();
                if (null != streamForComment && null != streamForComment.comments) {
                    mTotalCommentCount = streamForComment.comments.getCount();
                } else {
                    mTotalCommentCount = 0;
                }
            } else {
                //TODO:
            }

            applyUpdatedStream(true, false);

            setCommentButtonStatus();
        }
    }

    private void setCommentButtonStatus() {
        if (isFinishing()) {
            return;
        }


        if (null != streamForComment && streamForComment.canComment) {
            mCommentText.setFocusable(true);
            mCommentText.setFocusableInTouchMode(true);
            mCommentText.setHint(R.string.write_comment_hint);
            View commitBtn = findViewById(R.id.comments_share_button);
            commitBtn.setVisibility(View.VISIBLE);
            comments_mention_button.setClickable(true);
        } else {
        	mCommentText.setText(null);
            mCommentText.setFocusable(false);
            mCommentText.setHint(R.string.menu_comment_attribute_disable);
            View commitBtn = findViewById(R.id.comments_share_button);
            commitBtn.setVisibility(View.GONE);
            comments_mention_button.setClickable(false);
        }
    }

    @Override
    protected void loadRefresh() {
        super.loadRefresh();
        final int msgId;
        if (isapk && null == appForComment) {
            msgId = STREAM_LOAD;
        } else if (!isapk && null == streamForComment) {
            msgId = STREAM_LOAD;
        } else {
            msgId = COMMENTS_GET;
        }
        mHandler.obtainMessage(msgId).sendToTarget();
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    private class CommentBtnClickListener implements OnClickListener {
        public void onClick(View arg0) {
            if (ensureAccountLogin()) {
                Message msg = mHandler.obtainMessage(COMMENTS_ADD);
                msg.sendToTarget();
            }
        }
    }

    private final OnClickListener likeClick = new OnClickListener() {
        public void onClick(View v) {
            if (ensureAccountLogin()) {
                if (islike) {
                    unlikeTarget(post_id, isapk);
                } else {
                    likeTarget(post_id, isapk);
                }
            }
        }
    };

    private void likeTarget(String targetId, boolean isApkTarget) {
        if (isApkTarget) {
            likePost("", targetId);
        } else {
            likePost(targetId, "");
        }
    }

    private void unlikeTarget(String targetId, boolean isApkTarget) {
        if (isApkTarget) {
            unLikePost("", targetId);
        } else {
            unLikePost(targetId, "");
        }
    }

    private final static int COMMENTS_GET = 1;
    private final static int COMMENTS_GET_END = 2;

    private final static int COMMENTS_ADD = 6;
    private final static int COMMENTS_ADD_END = 7;

    private final static int COMMENTS_REMOVE = 12;
    private final static int COMMENTS_REMOVE_END = 13;

    private final static int STREAM_REMOVE = 14;
    private final static int STREAM_REMOVE_END = 15;

    private final static int STREAM_LOAD = 16;
    private final static int STREAM_LOAD_END = 17;

    private class MainHandler extends Handler {
        public MainHandler() {
            super();
            Log.d(TAG, "new commentsHandler");
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COMMENTS_ADD: {
                    String content = getCommentContent();
                    if (TextUtils.isEmpty(content)) {
                        showCustomToast(R.string.input_content);
                        return;
                    } else {
                        postComment(content);
                    }
                    break;
                }
                case COMMENTS_ADD_END: {
                    try{
                        dismissDialog(DIALOG_ADD_COMMENTS);
                    } catch (Exception e) {
                    }
                    boolean result = msg.getData().getBoolean(RESULT);
                    if (result) {
                        if (mCommentText != null) {
                            mCommentText.setText(null);
                        }
                        Log.d(TAG, "comment add end!");
                        refreshCommentsUi();
                    }
                    break;
                }
                case COMMENTS_GET: {
                    loadCommentFromServer();
                    break;
                }
                case COMMENTS_GET_END: {
                    end();
                    boolean result = msg.getData().getBoolean(RESULT);
                    if (result) {
                        Log.d(TAG, "get comment end!");
                    }
                    break;
                }
                case COMMENTS_REMOVE:
                    showDialog(DIALOG_REMOVE_COMMENT);
                    long comment_id = msg.getData().getLong("comment_id");
                    removeComments(comment_id);
                    break;
                case COMMENTS_REMOVE_END:
                    try {
                        dismissDialog(DIALOG_REMOVE_COMMENT);
                    } catch (Exception e) {
                    }
                    boolean result = msg.getData().getBoolean(RESULT);
                    if (result) {
                        refreshCommentsUi();
                    }
                    break;
                case STREAM_REMOVE:
                    removeStream(streamForComment.post_id);
                    break;
                case STREAM_REMOVE_END:
                    try {
                        dismissDialog(DIALOG_REMOVE_POST);
                    } catch (Exception ex) { }

                    boolean suc = msg.getData().getBoolean(RESULT);
                    if (suc) {
                        QiupuCommentsActivity.this.finish();
                        QiupuHelper.updateStreamRemovedUI(post_id);
                    }else {
                    	if(msg.getData().getInt("errorCode") == ErrorResponse.STREAM_REMOVED) {
                    		QiupuCommentsActivity.this.finish();
                            QiupuHelper.updateStreamRemovedUI(post_id);
                    	}else {
                    		ToastUtil.showShortToast(QiupuCommentsActivity.this, mHandler, R.string.del_failed);
                    		Log.d(TAG, "stream remove failed ");
                    	}
                    }
                    break;
                case STREAM_LOAD: {
                    getStreamWithComments(post_id);
                    break;
                }
                case STREAM_LOAD_END: {
                    end();
                    if (msg.getData().getBoolean(RESULT)) {
                        boolean needUpdateStream = !SAME_PHOTO_COUNT.equals(msg.getData().getString(SAME_PHOTO_COUNT));
                        Log.d(TAG, "STREAM_LOAD_END, get stream succeed! needUpdateStream = " + needUpdateStream);
                        if (type == BpcApiUtils.IMAGE_POST && needUpdateStream) {
                            refreshPhotoStreamCallBack(needUpdateStream);
                        } else if (type == BpcApiUtils.IMAGE_POST && !needUpdateStream){
                            Log.d(TAG, " no need refresh stream, but need to refresh comment");
                            applyUpdatedStream(false, true);
                            setCommentButtonStatus();
                            refreshCommentsUi();
                            return;
                        }

                        if (isapk) {
                            applyUpdatedApp(false);
                        } else {
                            applyUpdatedStream(false, false);
                            setCommentButtonStatus();
                        }
                        refreshCommentsUi();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private CommentsAdapter refreshCommentAdapter() {
        final CommentsAdapter sa;
        final boolean hasMore = mTotalCommentCount > mDisplayCommentList.size();

        if (mDisplayCommentList.size() > 0) {
            sa = new CommentsAdapter(this, mDisplayCommentList, hasMore, isStreamOwner());
        } else {
            sa = null;
        }
        return sa;
    }

    private boolean mIsDirtyStreamItem;
    private void refreshCommentsUi() {
    	if(streamItemView != null && mIsDirtyStreamItem) {
    	    streamItemView.refreshItem(post_id, streamForComment);
            mIsDirtyStreamItem = false;
        }
    	
        final CommentsAdapter sa = refreshCommentAdapter();
        stream_comments_list.setAdapter(sa);
        
        refreshCommentsCount();
    }

    private boolean commentProcess = false;
    private Object commentLock = new Object();
    private boolean isReadyForAddComment() {
        if (commentProcess == true) {
            Log.d(TAG, "isReadyForAddComment, in load stream");
            showCustomToast(R.string.string_in_processing);
            return false;
        }
        showDialog(DIALOG_ADD_COMMENTS);
        synchronized (commentLock) {
            commentProcess = true;
        }
        return true;
    }

    @Override
    protected void onCommentAdded(boolean result, String error) {
        Message msg = mHandler.obtainMessage(COMMENTS_ADD_END);
        msg.getData().putBoolean(RESULT, result);

        msg.sendToTarget();
        synchronized (commentLock) {
            commentProcess = false;
        }
    }

    private void postComment(String content) {
        if (!isReadyForAddComment()) {
            Log.d(TAG, "postComment, could not request add comment now.");
            return;
        }

        synchronized (commentLock) {
            commentProcess = true;
        }

        if (isapk) {
            asyncQiupu.postApkComment(getSavedTicket(), appForComment.apk_server_id, mReferredCommentId, content, new TwitterAdapter() {
                public void getPostComment(Stream.Comments.Stream_Post cominfo) {
                    if (QiupuConfig.LOGD) Log.d(TAG, "postApkComment, CommentInfo:" + cominfo);
                    if (cominfo != null) {
                        mDisplayCommentList.add(0, cominfo);
                        mTotalCommentCount++;
                        Intent data = new Intent();
                        data.putExtra("APK_COMMENT_COUNT", mTotalCommentCount);
                        QiupuCommentsActivity.this.setResult(Activity.RESULT_OK, data);
                        onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);
                    }

                    onCommentAdded(true);
                }

                public void onException(TwitterException ex, TwitterMethod method) {
                    Log.d(TAG, "postApkComment exception:" + ex.getMessage());
                    preHandleTwitterException(ex);
                    onCommentAdded(false, ex.getMessage());
                }
            });
        } else {
            asyncQiupu.postStreamComment(getSavedTicket(), post_id, mReferredCommentId, content, new TwitterAdapter() {
                public void getPostComment(Stream_Post cominfo) {
                    if (QiupuConfig.LOGD) Log.d(TAG, "postStreamComment, CommentInfo:" + cominfo);
                    if (cominfo != null) {
                        mTotalCommentCount++;
                        mDisplayCommentList.add(0, cominfo);
                        onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);
                    }

                    onCommentAdded(true);
                }

                public void onException(TwitterException ex, TwitterMethod method) {
                    Log.d(TAG, "postStreamComment exception:" + ex.getMessage());
                    preHandleTwitterException(ex);
                    onCommentAdded(false, ex.getMessage());
                }
            });
        }
    }

    private void onCommentPageLoaded(final List<Stream_Post> commentList, final int pageIndex) {
        mHandler.post(new Runnable() {
            public void run() {
                List<Stream_Post> lists = mDisplayCommentList;

                List<Stream_Post> saveDisplayCommentList = new ArrayList<Stream_Post>();
                saveDisplayCommentList.addAll(mDisplayCommentList);

                synchronized (lists) {
                    if (lists.size() == 0) {
                        lists.addAll(commentList);
                    } else {
                        if (pageIndex <= 0) {
                            lists.clear();
                        } else {
                            lists.removeAll(commentList);
                        }
                        saveDisplayCommentList.removeAll(commentList);

                        lists.addAll(saveDisplayCommentList);
                        lists.addAll(commentList);
                    }

                    Collections.sort(lists);
                }

                mTotalCommentCount = commentList.size();
                refreshCommentsUi();

                Log.d(TAG, "mTotalCommentCount: " + mTotalCommentCount + " mDisplayCommentList.size : " + mDisplayCommentList.size());
                onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);
            }
        });
    }

    private void loadCommentFromServer() {
        if (inProcess == true) {
            Log.d(TAG, "in load stream");
            showCustomToast(R.string.string_in_processing);
            return;
        }

        final String id;
        if(isapk) {
            if(appForComment == null) {
                return;
            } else {
                id = appForComment.apk_server_id; 
            }
        }else {
            id = post_id;
        }

        synchronized (mLock) {
            inProcess = true;
        }

        begin();

        final int pageIndex = -1;
        final int pageSize = -1;
        String obj_type = isapk ? QiupuConfig.TYPE_APK :QiupuConfig.TYPE_STREAM;
        asyncQiupu.getCommentsList(getSavedTicket(), obj_type, id, pageIndex, pageSize, new TwitterAdapter() {
            public void getCommentsList(List<Stream_Post> commentList) {
                if (!isapk) {
                    InformationReadCache.ReadStreamCache.cache(Long.parseLong(id), commentList);
                }

                if (commentList != null) {
                    onCommentPageLoaded(commentList, pageIndex);
                }

                synchronized (mLock) {
                    inProcess = false;
                }

                Message msg = mHandler.obtainMessage(COMMENTS_GET_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "loadCommentFromServer exception:" + ex.getMessage());
                preHandleTwitterException(ex);

                Message msg = mHandler.obtainMessage(COMMENTS_GET_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
                synchronized (mLock) {
                    inProcess = false;
                }
            }
        });
    }

    private void getStreamWithComments(final String targetId) {
        if (inProcess || TextUtils.isEmpty(targetId)) {
            Log.d(TAG, "getStreamWithComments, in load stream or empty id:" + targetId);
            showCustomToast(R.string.string_in_processing);
            return;
        }

        if (!DataConnectionUtils.alarmTestValidConnection(this)) {
            Log.d(TAG, "getStreamWithComments, no valid connection available.");
            return;
        }
        
        synchronized (mLock) {
            inProcess = true;
        }

        begin();

        if (isapk) {
            asyncQiupu.getApkDetailInformation(getSavedTicket(), targetId, true, new TwitterAdapter() {
                public void getApkDetailInformation(ApkResponse info) {
                    if (info != null) {
                        if (QiupuConfig.LOGD) Log.d(TAG, "getStreamWithComments info:" + info);
                        if (null != info) {
                            if (null == appForComment) {
                                mIsDirtyStreamItem = true;
                            }
                            appForComment = info;
                            mTotalCommentCount = info.comments.getCount();
                        }
                    }

                    onGetStreamWithCommentEnd(true, null);
                }

                public void onException(TwitterException ex, TwitterMethod method) {
                    Log.d(TAG, "getStreamWithComments exception:" + ex.getMessage());
                    preHandleTwitterException(ex);
                    onGetStreamWithCommentEnd(false, ex.getMessage());
                }
            });
        } else {
            asyncQiupu.getStreamWithComments(getSavedTicket(), targetId, new TwitterAdapter() {
                public void getStreamWithComments(Stream stream) {
                    boolean changed = false;
                    if (stream != null) {
                        if (null == streamForComment) {
                            changed = true;
                            mIsDirtyStreamItem = true;
                        }

                        if (streamForComment != null && streamForComment.type == BpcApiUtils.IMAGE_POST
                                && stream.type == BpcApiUtils.IMAGE_POST) {
                            if (hasSamePhoto(stream)) {
                                has_same_photo_count = SAME_PHOTO_COUNT;
                                changed = null == streamForComment ? true :
                                        streamForComment.updated_time != stream.updated_time;
                            } else {
                                changed = true;
                                has_same_photo_count = DIFF_PHOTO_COUNT;
                            }
                        }

                        if (changed) {
                            if (null != streamForComment && null != streamForComment.comments) {
                                stream.comments.alterCommentList(streamForComment.comments.getCommentList(), streamForComment.comments.getCount());
                            }

                            streamForComment = stream;
                            mTotalCommentCount = stream.comments != null ? stream.comments.getCount() : 0;
                        }
                    }

                    onGetStreamWithCommentEnd(true, has_same_photo_count);
                }

                public void onException(TwitterException ex, TwitterMethod method) {
                    Log.d(TAG, "getStreamWithComments exception:" + ex.getMessage());
                    preHandleTwitterException(ex);
                    onGetStreamWithCommentEnd(false, ex.getMessage());
                }
            });
        }
    }

    private boolean hasSamePhoto(final Stream newStream) {
        int newPhotoCount = 0;
        int oldPhotoCount = 0;
        if (streamForComment != null && streamForComment.attachment != null
                && streamForComment.attachment.attachments != null) {
            oldPhotoCount = streamForComment.attachment.attachments.size();
        }

        if (newStream != null && newStream.attachment != null
                && newStream.attachment.attachments != null) {
            newPhotoCount = newStream.attachment.attachments.size();
        }

        Log.d(TAG, " newPhotoCount = " + newPhotoCount + " oldPhotoCount = " + oldPhotoCount);
        if (newPhotoCount == oldPhotoCount) {
            return true;
        } else {
            return false;
        }
    }

    private void onGetStreamWithCommentEnd(boolean result, String prompt) {
        synchronized (mLock) {
            inProcess = false;
        }

        Message msg = mHandler.obtainMessage(STREAM_LOAD_END);

        msg.getData().putBoolean(RESULT, result);
        msg.getData().putString(SAME_PHOTO_COUNT, prompt);
        msg.sendToTarget();
    }

    @Override
    public void deleteComments(Stream_Post comment) {
        Message msg = mHandler.obtainMessage(COMMENTS_REMOVE);
        msg.getData().putLong("comment_id", comment.id);
        msg.sendToTarget();
    }

    private boolean inremoveStream = false;
    private final Object mremoveLock = new Object();

    protected void removeStream(final String postId) {
        synchronized (mremoveLock) {
            if (inremoveStream == true) {
                Log.d(TAG, "in doing remove stream");
                return;
            }
        }

        synchronized (mremoveLock) {
            inremoveStream = true;
        }

        showDialog(DIALOG_REMOVE_POST);
        asyncQiupu.deletePost(getSavedTicket(), postId, new TwitterAdapter() {
            public void deletePost(boolean suc) {
                Log.d(TAG, "finish removeStream" + suc);
                if (suc) {
                    QiupuHelper.updateStreamRemovedUI(postId);
                }
                Message msg = mHandler.obtainMessage(STREAM_REMOVE_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.sendToTarget();

                synchronized (mremoveLock) {
                    inremoveStream = false;
                }
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mremoveLock) {
                    inremoveStream = false;
                }
//                preHandleTwitterException(ex);

                Message msg = mHandler.obtainMessage(STREAM_REMOVE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putInt("errorCode", ex.getStatusCode());
                msg.sendToTarget();
            }
        });
    }

    private boolean inremovecomment = false;
    private final Object mremoveCommentLock = new Object();

    protected void removeComments(final long commentId) {
        synchronized (mremoveCommentLock) {
            if (inremovecomment == true) {
                Log.d(TAG, "in doing remove comment");
                return;
            }
        }

        synchronized (mremoveCommentLock) {
            inremovecomment = true;
        }
        String obj_type = isapk ? QiupuConfig.TYPE_APK :QiupuConfig.TYPE_STREAM;
        asyncQiupu.deleteComments(getSavedTicket(), obj_type, commentId, new TwitterAdapter() {
            public void deleteComments(boolean suc) {
                Log.d(TAG, "finish remove comments");
                if (suc) {
                    mTotalCommentCount--;
                    for (Stream_Post comment : mDisplayCommentList) {
                        if (comment.id == commentId) {
                            mDisplayCommentList.remove(comment);
                            break;
                        }
                    }
//                    Stream_Post post = new Stream_Post();
//                    post.id = commentId;
//                    int pos = mDisplayCommentList.indexOf(post);
//                    mDisplayCommentList.remove(pos);
                    onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);
                }
                synchronized (mremoveCommentLock) {
                    inremovecomment = false;
                }

                Message msg = mHandler.obtainMessage(COMMENTS_REMOVE_END);
                msg.getData().putBoolean(RESULT, suc);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mremoveCommentLock) {
                    inremovecomment = false;
                }
                preHandleTwitterException(ex);
                Message msg = mHandler.obtainMessage(COMMENTS_REMOVE_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    private void onCommentsUpdated(final String postId, final List<Stream_Post> postList, final int totalCount) {
        // TODO begin: implement similar listener for this code pieces.
        if (isapk) {
            Intent data = new Intent();
            data.putExtra("APK_COMMENT_COUNT", totalCount);
            setResult(Activity.RESULT_OK, data);
        }
        // TODO end.

        if (null != streamForComment) {
            final int commentType = streamForComment.type;
            ArrayList<Stream_Post> latestComments = new ArrayList<Stream_Post>();
            int count = postList.size();
            if (count == 1) {
                latestComments.add(postList.get(0));
            } else if (count > 1) {
                for (int i = 0; i < 2; i++) {
                    latestComments.add(postList.get(i));
                }
            }
            QiupuHelper.onCommentsUpdated(streamForComment, commentType, latestComments, totalCount);
        }
    }

    @Override
    protected void doDeletePostCallBack() {
        mHandler.obtainMessage(STREAM_REMOVE).sendToTarget();
    }

    private void updateLikeUi(boolean likeFlag) {
        final int resid = likeFlag ?R.drawable.actionbar_icon_delete_praise_normal :  R.drawable.actionbar_icon_praise_normal;
        if(isUsingActionBar() && getActionBar() != null)
        {
            mMiddleActionBtnMenu.setIcon(resid);
        }
        else
        {
            ImageView likeToggle = (ImageView) findViewById(R.id.head_action_middle);
            if (null != likeToggle) {
                likeToggle.setImageResource(resid);
            }
        }
    }

    private void resetDisplayingViews() {
        if (stream_comments_list.getHeaderViewsCount() > 0) {
            stream_comments_list.removeHeaderView(streamItemView);
        }

        stream_comments_list.setAdapter(null);
    }

    private void applyUpdatedApp(boolean force) {
        resetDisplayingViews();

        if (appForComment != null) {
            islike = appForComment.iLike;
            updateLikeUi(islike);
            overrideAndShowRightActionBtn(R.drawable.tab_icon_share_normal, shareApkClick);

            final ApplicationItemView st = new ApplicationItemView(this, appForComment, true);
            st.setBackgroundResource(R.drawable.list_selector_background);
            stream_comments_list.addHeaderView(st);
            st.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    ApplicationItemView.invokeApkDetailActivity(view);
                }
            });

            mDisplayCommentList.addAll(appForComment.comments.getCommentList());
            final boolean hasmore = mTotalCommentCount > mDisplayCommentList.size();
            CommentsAdapter sa = new CommentsAdapter(this, mDisplayCommentList, hasmore, isStreamOwner());
            stream_comments_list.setAdapter(sa);
            
            if(force){
                mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
            }else {
                if (hasmore) {
                    mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
                }
            }

        }
    }

    private void applyUpdatedStream(boolean force, boolean noChange) {
        if (noChange == false) {
            resetDisplayingViews();
        }

        if (null != streamForComment) {
            islike = streamForComment.iLike;
            type = streamForComment.type;
            updateLikeUi(islike);

            
            if (streamForComment.canReshare) {
                overrideAndShowRightActionBtn(R.drawable.menu_share_attribute_enable, reshareClick);
            } else {
                overrideAndShowRightActionBtn(R.drawable.memu_forbid_share, reshareClick);
            }

            attachStreamProperty(R.id.id_stream_property, streamForComment);

            if (type == BpcApiUtils.IMAGE_POST) {
                if (noChange) {
                    mDisplayCommentList.clear();
                    mDisplayCommentList.addAll(streamForComment.comments.getCommentList());
                } else {
                    streamItemView = StreamListAdapter.newStreamItemView(this, streamForComment, true);
                    stream_comments_list.addHeaderView(streamItemView);
                    stream_comments_list.addHeaderView(initSpanHeadView());
                    stream_comments_list.setHeaderDividersEnabled(false);
                    mDisplayCommentList.addAll(streamForComment.comments.getCommentList());
                }
            } else {
                streamItemView = StreamListAdapter.newStreamItemView(this, streamForComment, true);
                stream_comments_list.addHeaderView(streamItemView);
                stream_comments_list.addHeaderView(initSpanHeadView());
                stream_comments_list.setHeaderDividersEnabled(false);
                mDisplayCommentList.addAll(streamForComment.comments.getCommentList());
            }

            final boolean hasmore = mTotalCommentCount > mDisplayCommentList.size();
            CommentsAdapter sa = new CommentsAdapter(this, mDisplayCommentList, hasmore, isStreamOwner());
            stream_comments_list.setAdapter(sa);

            if(force) {
                if (type == BpcApiUtils.IMAGE_POST){
                    mHandler.obtainMessage(STREAM_LOAD).sendToTarget();
                } else {
                    mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
                }
            } else{
                if (hasmore) {
                    mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
                }
            }
            
        }else {
        	mHandler.obtainMessage(STREAM_LOAD).sendToTarget();
        }
    }
    
    private View initSpanHeadView() {
    	View span_view = LayoutInflater.from(this).inflate(R.layout.comment_span_view, null);
    	mComment_count_Span = (TextView) span_view.findViewById(R.id.comment_count);
    	mComment_count_Span.setMovementMethod(MyLinkMovementMethod.getInstance());
    	refreshCommentsCount();
    	return span_view;
    }
    
    private void refreshCommentsCount() {
    	if(mComment_count_Span != null) {
    		mComment_count_Span.setText(String.format(getString(R.string.comment_count_label), mTotalCommentCount));
    	}
    }

    @Override
    protected boolean isStreamOwner() {
        boolean ret = false;
        if (null != streamForComment && null != streamForComment.fromUser) {
            ret = streamForComment.fromUser.uid == getSaveUid();
        } else {
            Log.e(TAG, "isStreamOwner, unexpected status: " + streamForComment);
        }

        return ret;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void gotoReshareActivity() {
        final String externalText = getCommentContent();
        if (!isapk && null != streamItemView) {
            IntentUtil.startComposeIntent(this, streamForComment,
                    new long[]{streamForComment.fromUser.uid}, externalText, "reshare");
        } else if (isapk && null != appForComment) {
            QiupuComposeActivity.startShareIntent(this, appForComment, externalText, "reshare");
        }
    }

    private final OnClickListener reshareClick = new OnClickListener() {
        public void onClick(View v) {
            if (null != streamForComment && streamForComment.canReshare) {
                if (streamForComment.isPrivacy()) {
                    DialogUtils.showOKDialog(QiupuCommentsActivity.this, R.string.commments_tips_title,
                            R.string.comments_tips_message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            gotoReshareActivity();
                        }
                    });
                } else {
                    gotoReshareActivity();
                }
            } else {
                showCustomToast(R.string.menu_share_attribute_disable);
            }
        }
    };

    private final OnClickListener shareApkClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            gotoReshareActivity();
        }
    };

    private void refreshCommentLikeUi(final long cId, final Stream_Post comment) {
        mHandler.post(new Runnable() {
            public void run() {
                final int count = stream_comments_list.getCount();
                for (int j = 0; j < count; j++) {
                    View v = stream_comments_list.getChildAt(j);
                    if (CommentItemView.class.isInstance(v)) {
                        CommentItemView fv = (CommentItemView) v;
                        if (cId == fv.getComment().id) {
                            fv.updateLikeCountUi(comment);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void setMoreButtonStatus(final boolean isLoading) {
        mHandler.post(new Runnable() {
            public void run() {
                final int resId = isLoading ? R.string.loading : R.string.list_view_more;
                int index = stream_comments_list.getCount() - 1;
                while (index >= 0) {
                    View view = stream_comments_list.getChildAt(index);
                    if (view instanceof Button) {
                        ((Button) view).setText(resId);
                        break;
                    }
                    index--;
                }
            }
        });
    }
    @Override
    protected void uiLoadBegin() {
        super.uiLoadBegin();

        setMoreButtonStatus(true);
    }

    @Override
    protected void uiLoadEnd() {
        super.uiLoadEnd();

        setMoreButtonStatus(false);
    }
    
    private boolean isAppSchema(Intent intent) {
        if (null != intent) {
            final String url = getIntentURL(intent);
            if (!TextUtils.isEmpty(url) && url.startsWith("borqs://application/comment")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo i = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int id = item.getItemId();
        if (R.id.bpc_item_delete == id) {
            if (CommentItemView.class.isInstance(i.targetView)) {
                CommentItemView commentItemView = (CommentItemView) i.targetView;
                Stream_Post comment = commentItemView.getComment();
                deleteComments(comment);
            }
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
    
    public static Intent getAppIntent(Context context, ApkBasicInfo apkInfo) {
        Intent intent = new Intent(context, QiupuCommentsActivity.class);
        intent.putExtra(BpcApiUtils.SEARCH_KEY_ID, apkInfo.apk_server_id);
        intent.putExtra(QiupuMessage.BUNDLE_POST_IS_LIKE, apkInfo.iLike);
        intent.putExtra(QiupuMessage.BUNDLE_POST_APK, true);
        intent.putExtra(QiupuMessage.BUNDLE_APKINFO, apkInfo);
        return intent;
    }

    @Override
    public void onDestroy() {
        QiupuHelper.unRegisterTargetLikeListener(getClass().getName());
        CommentItemView.setCommentActionListener(null);

        mCommentText.removeTextChangedListener(mTextWatcher);
        mCommentText.destroy();
        mCommentText = null;
        mHandler.post(new Runnable() {
            public void run() {
                updateInformationReadStatus(post_id);
            }
        });
        
        super.onDestroy();
    }

    private void refreshPhotoStreamCallBack(boolean needReplaceStream) {
        if (null != streamForComment && needReplaceStream) {
            if (type == BpcApiUtils.IMAGE_POST) {
                QiupuHelper.refreshPhotoStreamCallBack(streamForComment);
            }
        }
    }

    @Override
    public void onTargetLikeCreated(String targetId, String targetType) {
        if (QiupuConfig.TYPE_COMMENT.equals(targetType)) {
            final long cId = Long.valueOf(targetId);
            for (Stream_Post comment : mDisplayCommentList) {
                if (cId == comment.id) {
                    comment.iLike = true;
                    comment.likerList.count++;
                    final QiupuSimpleUser user = AccountServiceUtils.touchMySimpleUserInfo();
                    if (null != user) {
                        comment.likerList.friends.add(user);
                    }

                    refreshCommentLikeUi(cId, comment);
                    onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);

                    break;
                }
            }
        } else {
            islike = true;
            updateLikeUi(true);

            if (isapk) {
                // TODO
            } else {
                streamForComment.iLike = true;
                final QiupuSimpleUser user = AccountServiceUtils.touchMySimpleUserInfo();
                if (null != user) {
                    streamForComment.likes.count++;
                    streamForComment.likes.friends.add(user);
                }

                streamItemView.refreshItem(post_id, streamForComment);
            }
        }
    }

    @Override
    public void onTargetLikeRemoved(String targetId, String targetType) {
        if (QiupuConfig.TYPE_COMMENT.equals(targetType)) {
            final long cId = Long.valueOf(targetId);
            for (Stream_Post comment : mDisplayCommentList) {
                if (cId == comment.id) {
                    comment.iLike = false;
                    comment.likerList.count--;
                    final long uid = AccountServiceUtils.getBorqsAccountID();
                    for (QiupuSimpleUser user : comment.likerList.friends) {
                        if (user.uid == uid) {
                            comment.likerList.friends.remove(user);
                            break;
                        }
                    }

                    refreshCommentLikeUi(cId, comment);
                    onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);

                    break;
                }
            }
        } else {
            islike = false;
            updateLikeUi(false);

            if (isapk) {
                // TODO
            } else {
                streamForComment.iLike = false;
                streamForComment.likes.count--;
                final long uid = AccountServiceUtils.getBorqsAccountID();
                for (QiupuSimpleUser user : streamForComment.likes.friends) {
                    if (user.uid == uid) {
                        streamForComment.likes.friends.remove(user);
                        break;
                    }
                }
                streamItemView.refreshItem(post_id, streamForComment);
            }
        }
    }

    public static class MyTextWatcher implements TextWatcher {
        private final View mCommentButton;

        public MyTextWatcher(View paramView) {
            this.mCommentButton = paramView;
        }

        public void afterTextChanged(Editable paramEditable) {
            View localView = this.mCommentButton;
            boolean bool;
            if (TextUtils.getTrimmedLength(paramEditable) <= 0)
                bool = false;
            else
                bool = true;
            localView.setEnabled(bool);
        }

        public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
        }

        public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
        }
    }

    private boolean mDiscardConfirm = false;
    public void onDiscard() {
        mDiscardConfirm = false;
        if (mCommentText != null && !TextUtils.isEmpty(mCommentText.getText())) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    mDiscardConfirm = true;
                }
            };

            final String title = getString(R.string.discard_dialog_title, getString(R.string.news_feed_comment));
            final String message = getString(R.string.discard_dialog_message, getString(R.string.news_feed_comment));
            DialogUtils.showConfirmDialog(this, title, message, listener);
//                AlertFragmentDialog localAlertFragmentDialog = AlertFragmentDialog.newInstance(getString(2131165428), getString(2131165429), getString(2131165546), getString(2131165547));
//                localAlertFragmentDialog.setTargetFragment(this, 0);
//                localAlertFragmentDialog.show(getFragmentManager(), "quit");
        } else {
            finish();
            mDiscardConfirm = true;
        }
    }

    @Override
    protected boolean preEscapeActivity() {
        onDiscard();
        return mDiscardConfirm;
    }
    
    private String getCommentContent() {
        return ConversationMultiAutoCompleteTextView.getConversationText(mCommentText);
    }

    private void updateInformationReadStatus(String postid) {
        final String schemeParam = "?" + BpcApiUtils.SEARCH_KEY_ID + "=" + postid;
        final NotificationOperator mOperator = new NotificationOperator(this);
        final String url = BpcApiUtils.STREAM_COMMENT_SCHEME_PATH + schemeParam;
        final String inforId = mOperator.getUnReadInformationWithUrl(url);

        Log.d(TAG, "updateInformationReadStatus: " + inforId);
        if(StringUtil.isValidString(inforId)) {
            boolean flag = mOperator.updateReadStatusByUrl(url, true);
            changeSysNotication(mOperator, flag);
            InformationUtils.setReadStatus(QiupuCommentsActivity.this, inforId);
        }        
    }
    
    private void cancelNotice() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(InformationHttpPushReceiver.HTTPPUSH);
    }
    
    private void changeSysNotication(NotificationOperator mOperator, boolean hasChangeRead) {
        if(hasChangeRead) {
            int count = mOperator.getThisWeekUnReadCount();
            cancelNotice();
            if(count <= 0) {
                return;
            }
//            InformationUtils.showSysNotification(this, count);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected String getObjectId() {
        return post_id;
    }

    @Override
    protected List<QiupuSimpleUser> getLikerList() {
        if (isapk) {
            if (appForComment != null && appForComment.likes != null) {
                return appForComment.likes.friends;
            } else {
                return null;
            }
        } else {
            if (streamForComment != null && streamForComment.likes != null) {
                return streamForComment.likes.friends;
            } else {
                return null;
            }
        }
    }
    
    @Override
    protected void mutePost() {
    	super.mutePost();
    	QiupuHelper.updateStreamRemovedUI(post_id);
    	finish();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!isapk) {
            StreamCacheManager.updateItemComments(streamForComment, mDisplayCommentList);
        }
    }

}
