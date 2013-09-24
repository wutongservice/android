package com.borqs.qiupu.ui;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twitter4j.ApkBasicInfo;
import twitter4j.QiupuPhoto;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.CommentsAdapter;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.TargetLikeActionListener;
import com.borqs.common.util.DataConnectionUtils;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CommentItemView;
import com.borqs.common.view.ConversationMultiAutoCompleteTextView;
import com.borqs.common.view.KeyboardLayout;
import com.borqs.common.view.KeyboardLayout.onKybdsChangeListener;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.QiupuMessage;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.util.DateUtil;

public class QiupuCommentsPicActivity extends QiupuBaseCommentsActivity implements TargetLikeActionListener {
    private static final String TAG = "Qiupu.QiupuCommentsActivity";

    private ListView stream_comments_list;
    private QiupuPhoto qiupuPhoto;
    private String photo_id;
    private int photo_position = 0;
    private boolean islike;
    private View streamPicView;
    private ImageView stream_pic;
    private View loading_layout;
    private TextView tv_loading;

    private int mTotalCommentCount;
    private List<Stream_Post> mDisplayCommentList = new ArrayList<Stream_Post>();

    private TextWatcher mTextWatcher;
    private View comments_mention_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensureAccountLogin();
        setContentView(R.layout.comments_pic_ui);
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
        setHeadTitle(R.string.app_comments);
        stream_comments_list = (ListView) findViewById(R.id.stream_comments_list);
        loading_layout = findViewById(R.id.loading_layout);
        tv_loading  = (TextView)findViewById(R.id.tv_loading);

        CommentItemView.setCommentActionListener(this);

        final View comments_share_button = findViewById(R.id.comments_share_button);
        mCommentText = ((ConversationMultiAutoCompleteTextView)findViewById(R.id.compose_text));
        final boolean enableCommitBtn = null != mCommentText && !TextUtils.isEmpty(mCommentText.getText());
        comments_share_button.setEnabled(enableCommitBtn);
        mTextWatcher = new QiupuCommentsActivity.MyTextWatcher(comments_share_button);
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
        final String search_id = BpcApiUtils.parseSchemeValue(intent, BpcApiUtils.SEARCH_KEY_ID);
        if (TextUtils.isEmpty(search_id)) {
            parseStreamFromIntentBundle(intent);
        } else {
        	photo_id = search_id;
            mHandler.obtainMessage(PHOTO_GET).sendToTarget();
        }
    }

//    private ListView mSetListView;
//    @Override
//    protected void setCommentSettingListener() {
//        mSetListView = (ListView) getLayoutInflater().inflate(R.layout.default_listview, null);
//        mSetListView.setAdapter(new CommentSettingAdapter(this, streamForComment.canComment, streamForComment.canLike, streamForComment.canReshare));
//        mSetListView.setOnItemClickListener(listItemListener);
//        DialogUtils.ShowDialogwithView(this, getString(R.string.stream_setting),
//                0, mSetListView, positiveListener, negativeListener);
//    }
//
//    private AdapterView.OnItemClickListener listItemListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position,
//                long id) {
//            if (CommentSettingItemView.class.isInstance(view)) {
//                CommentSettingItemView commentView = (CommentSettingItemView) view;
//                commentView.setCheckedStatus();
//            }
//        }
//    };

    @Override
    protected void updateCommentActivityUI(String post_id, boolean canComment,
            boolean canLike, boolean canReshare) {
//        if (streamForComment != null && streamForComment.post_id.equals(post_id)) {
//            streamForComment.canComment = canComment;
//            streamForComment.canLike = canLike;
//            streamForComment.canReshare = canReshare;
//        }

        applyUpdatedStream(true);
        setCommentButtonStatus();
    }

//    private DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            if (mSetListView != null && mSetListView.getCount() > 0) {
//                boolean[] canParameter = new boolean[mSetListView.getCount()];
//                for (int i = 0, count = mSetListView.getCount(); i < count; i++) {
//                    canParameter[i] = ((CompoundButton)mSetListView.getChildAt(i).findViewById(R.id.item_button)).isChecked();
//                }
//                if (canParameter[0] == streamForComment.canComment && canParameter[1] == streamForComment.canLike && canParameter[2] == streamForComment.canReshare) {
//                    Log.d(TAG, "No status change");
//                } else {
//                    updateStreamSetting(streamForComment.post_id, canParameter[0], canParameter[1], canParameter[2]);
//                }
//            } else {
//                Log.d(TAG, "Set ListView is null.");
//            }
//        }
//    };

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
        photo_id = intent.getStringExtra(BpcApiUtils.SEARCH_KEY_ID);
        photo_position = intent.getIntExtra("photo_position",0);
    	qiupuPhoto = (QiupuPhoto)intent.getSerializableExtra("qiupuPhoto");

        applyUpdatedStream(true);

        setCommentButtonStatus();
    }

    private void setCommentButtonStatus() {
        if (isFinishing()) {
            return;
        }

        mCommentText.setText(null);

//        if (null != streamForComment && streamForComment.canComment) {
            mCommentText.setFocusable(true);
            mCommentText.setFocusableInTouchMode(true);
            mCommentText.setHint(R.string.write_comment_hint);
            View commitBtn = findViewById(R.id.comments_share_button);
            commitBtn.setVisibility(View.VISIBLE);
            comments_mention_button.setClickable(true);
//        } else {
//            mCommentText.setFocusable(false);
//            mCommentText.setHint(R.string.menu_comment_attribute_disable);
//            View commitBtn = findViewById(R.id.comments_share_button);
//            commitBtn.setVisibility(View.GONE);
//            comments_mention_button.setClickable(false);
//        }
    }

    @Override
    protected void loadRefresh() {
        super.loadRefresh();
        mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
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
//                Message msg = mHandler.obtainMessage(islike ? REMOVE_LIKE : LIKE_ADD);
//                msg.sendToTarget();
                if (qiupuPhoto.iliked) {
                    removeLike(photo_id, QiupuConfig.TYPE_PHOTO);
                } else {
                	createLike(photo_id, QiupuConfig.TYPE_PHOTO);
                }
            }
        }
    };

    private final static int COMMENTS_GET = 1;
    private final static int COMMENTS_GET_END = 2;

    private final static int COMMENTS_ADD = 6;
    private final static int COMMENTS_ADD_END = 7;

    private final static int LIKE_ADD = 8;
//    private final static int LIKE_ADD_END = 9;

//    private final static int REMOVE_LIKE = 10;
//    private final static int REMOVE_LIKE_END = 11;

    private final static int COMMENTS_REMOVE = 12;
    private final static int COMMENTS_REMOVE_END = 13;

    private final static int STREAM_REMOVE = 14;
    private final static int STREAM_REMOVE_END = 15;

    private final static int PHOTO_GET = 16;
    private final static int PHOTO_GET_END = 17;
    private final static int LOAD_IMAGE_SUCCESS = 18;
    private final static int LOAD_IMAGE_FAILED = 19;

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
//                        mcomments_editor.setText(null);
                    	if(mCommentText != null) {
                    		mCommentText.setText(null);
                    	}
                        Log.d(TAG, "comment add end!");
                        refreshUI();
                    } else {
                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
                        if (isEmpty(ErrorMsg) == false) {
                            showCustomToast(ErrorMsg);
                        }
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
                        //refreshUI();

                    } else {
                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
                        if (isEmpty(ErrorMsg) == false) {
                            showCustomToast(ErrorMsg);
                        }
                    }
                    break;
                }
//                case LIKE_ADD: {
//                    likePost();
//                    break;
//                }
//                case LIKE_ADD_END: {
//                    try {
//                        dismissDialog(DIALOG_ADD_LIKE);
//                    } catch (Exception ex) {
//                    }
//
//                    boolean result = msg.getData().getBoolean(RESULT);
//                    if (result) {
//                        islike = true;
//                        updateLikeUi(islike);
//                    } else {
//                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
//                        if (isEmpty(ErrorMsg) == false) {
//                            showLongToast(ErrorMsg);
//                        }
//                    }
//                    break;
//                }

//                case REMOVE_LIKE: {
//                    unLikePost(post_id);
//                    break;
//                }
//                case REMOVE_LIKE_END: {
//                    dismissDialog(DIALOG_REMOVE_LIKE);
//                    boolean result = msg.getData().getBoolean(RESULT);
//                    if (result) {
//                        islike = false;
//                        updateLikeUi(islike);
//                    } else {
//                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
//                        if (isEmpty(ErrorMsg) == false) {
//                            showLongToast(ErrorMsg);
//                        }
//                    }
//                    break;
//                }
                case COMMENTS_REMOVE:
                    showDialog(DIALOG_REMOVE_COMMENT);
                    long comment_id = msg.getData().getLong("comment_id");
                    removeComments(comment_id);
                    break;
                case COMMENTS_REMOVE_END:
                    dismissDialog(DIALOG_REMOVE_COMMENT);
                    boolean result = msg.getData().getBoolean(RESULT);
                    if (result) {
                        refreshUI();
                    } else {
                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
                        if (isEmpty(ErrorMsg) == false) {
                            showCustomToast(ErrorMsg);
                        }
                    }
                    break;
//                case STREAM_REMOVE:
//                    removeStream(streamForComment.post_id);
//                    break;
//                case STREAM_REMOVE_END:
//                    try {
//                        dismissDialog(DIALOG_REMOVE_POST);
//                    } catch (Exception ex) {
//                    }
//
//                    boolean suc = msg.getData().getBoolean(RESULT);
//                    if (suc) {
//                        QiupuCommentsPicActivity.this.finish();
//                    } else {
//                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
//                        if (isEmpty(ErrorMsg) == false) {
//                            showCustomToast(ErrorMsg);
//                        }
//                    }
//                    break;
                case PHOTO_GET: {
                    getPhotoById();
                    break;
                }
                case PHOTO_GET_END: {
                    synchronized (mLock) {
                        inProcess = false;
                    }
                    end();
                    if (msg.getData().getBoolean(RESULT)) {
                        Log.d(TAG, "PHOTO_GET_END, get photo succeed!");
                        applyUpdatedStream(true);
                        refreshUI();

                    } else {
                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
                        if (isEmpty(ErrorMsg) == false) {
                            showCustomToast(ErrorMsg);
                        }
                    }
                    break;
                }
                case LOAD_IMAGE_SUCCESS:{
                	loading_layout.setVisibility(View.GONE);
                	break;
                }
                case LOAD_IMAGE_FAILED:{
                	loading_layout.setVisibility(View.GONE);
    				tv_loading.setText(R.string.loading_failed);
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

    private void refreshUI() {
        final CommentsAdapter sa = refreshCommentAdapter();
        stream_comments_list.setAdapter(sa);
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
        if (!TextUtils.isEmpty(error)) {
            msg.getData().putString(ERROR_MSG, error);
        }
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

        asyncQiupu.postStreamComment(getSavedTicket(), photo_id, mReferredCommentId, content,QiupuConfig.TYPE_PHOTO,new TwitterAdapter() {
            public void getPostComment(Stream_Post cominfo) {
                if (QiupuConfig.LOGD) Log.d(TAG, "postStreamComment, CommentInfo:" + cominfo);
                if (cominfo != null) {
                    if(qiupuPhoto != null) {
                        qiupuPhoto.comments_count++;
                    }
                    mTotalCommentCount++;
                    mDisplayCommentList.add(0, cominfo);
                    onCommentsUpdated(photo_id, mDisplayCommentList, mTotalCommentCount);
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

//    List<Stream_Post> getCommentsList() {
//        return mDisplayCommentList;
//    }

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

//                if (isapk) {
//                    appForComment.comments.stream_posts.clear();
//                    appForComment.comments.stream_posts.addAll(lists);
//                } else {
//                }
                
                refreshUI();

                mTotalCommentCount = commentList.size();
                Log.d(TAG, "mTotalCommentCount: " + mTotalCommentCount + " mDisplayCommentList.size : " + mDisplayCommentList.size() );
//                onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);  //TODO
            }
        });
    }

    private void loadCommentFromServer() {
        if (inProcess == true) {
            Log.d(TAG, "in load comment");
            showCustomToast(R.string.string_in_processing);
            return;
        }
        synchronized (mLock) {
            inProcess = true;
        }

        begin();
        final int pageIndex = -1;
        final int pageSize = -1;
        asyncQiupu.getCommentsList(getSavedTicket(), QiupuConfig.TYPE_PHOTO, photo_id, pageIndex, pageSize, new TwitterAdapter() {
            public void getCommentsList(List<Stream_Post> commentList) {
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
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
                synchronized (mLock) {
                    inProcess = false;
                }
            }
        });
    }

    private void getPhotoById() {
        if (inProcess || TextUtils.isEmpty(photo_id)) {
            Log.d(TAG, "getPhotoById, in load or photo_id is null");
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
        
        asyncQiupu.getPhotoById(getSavedTicket(), photo_id, new TwitterAdapter() {
            @Override
            public void getPhotoById(QiupuPhoto photo) {
                qiupuPhoto = photo;
                Message msg = mHandler.obtainMessage(PHOTO_GET_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
                
            }
            
            @Override
            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(PHOTO_GET_END);
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }
    
//    private void onGetStreamWithCommentEnd(boolean result, String prompt) {
//        synchronized (mLock) {
//            inProcess = false;
//        }
//
//        Message msg = mHandler.obtainMessage(STREAM_LOAD_END);
//        if (!TextUtils.isEmpty(prompt)) {
//            msg.getData().putString(ERROR_MSG, prompt);
//        }
//        msg.getData().putBoolean(RESULT, result);
//        msg.sendToTarget();
//    }

    @Override
    public void deleteComments(Stream_Post comment) {
        Message msg = mHandler.obtainMessage(COMMENTS_REMOVE);
        msg.getData().putLong("comment_id", comment.id);
        msg.sendToTarget();
    }

//    private boolean inremoveStream = false;
//    private final Object mremoveLock = new Object();
//
//    protected void removeStream(final String postId) {
//        synchronized (mremoveLock) {
//            if (inremoveStream == true) {
//                Log.d(TAG, "in doing remove stream");
//                return;
//            }
//        }
//
//        synchronized (mremoveLock) {
//            inremoveStream = true;
//        }
//
//        showDialog(DIALOG_REMOVE_POST);
//        asyncQiupu.deletePost(getSavedTicket(), postId, new TwitterAdapter() {
//            public void deletePost(boolean suc) {
//                Log.d(TAG, "finish removeStream" + suc);
//                if (suc) {
//                    QiupuHelper.updateStreamRemovedUI(postId);
////                    updateActivityUI(postId);
//                }
//                Message msg = mHandler.obtainMessage(STREAM_REMOVE_END);
//                msg.getData().putBoolean(RESULT, suc);
//                msg.sendToTarget();
//
//                synchronized (mremoveLock) {
//                    inremoveStream = false;
//                }
//            }
//
//            public void onException(TwitterException ex, TwitterMethod method) {
//                synchronized (mremoveLock) {
//                    inremoveStream = false;
//                }
//                preHandleTwitterException(ex);
//
//                Message msg = mHandler.obtainMessage(STREAM_REMOVE_END);
//                msg.getData().putBoolean(RESULT, false);
//                msg.getData().putString(ERROR_MSG, ex.getMessage());
//                msg.sendToTarget();
//            }
//        });
//    }

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
        asyncQiupu.deleteComments(getSavedTicket(), QiupuConfig.TYPE_STREAM, commentId, new TwitterAdapter() {
            public void deleteComments(boolean suc) {
                Log.d(TAG, "finish remove comments");
                if (suc) {
                    if(qiupuPhoto != null) {
                        qiupuPhoto.comments_count--;
                    }
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
                    onCommentsUpdated(photo_id, mDisplayCommentList, mTotalCommentCount);
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
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }

//    private void updateActivityUI(String postid) {
//
//        synchronized (QiupuHelper.listeners) {
//            Set<String> set = QiupuHelper.listeners.keySet();
//            Iterator<String> it = set.iterator();
//            while (it.hasNext()) {
//                String key = it.next();
//                if(QiupuHelper.listeners.get(key) != null)
//                {
//	                StreamActionListener listener = QiupuHelper.listeners.get(key).get();
//	                if (listener != null) {
//	                    listener.updateStreamRemovedUI(postid, -1);
//	                }
//                }
//            }
//        }
//    }

    private void onCommentsUpdated(final String postId, final List<Stream_Post> postList, final int totalCount) {
        ArrayList<Stream_Post> latestComments = new ArrayList<Stream_Post>();
        int count = postList.size();
        if (count == 1) {
            latestComments.add(postList.get(0));
        } else if (count > 1) {
            for (int i = 0; i < 2; i++) {
                latestComments.add(postList.get(i));
            }
        }
//            QiupuHelper.onCommentsUpdated(streamForComment, commentType, latestComments, totalCount);

//            synchronized (QiupuHelper.listeners) {
//            Set<String> set = QiupuHelper.listeners.keySet();
//            Iterator<String> it = set.iterator();
//            while (it.hasNext()) {
//                String key = it.next();
//                if(QiupuHelper.listeners.get(key) != null)
//                {
//	                StreamActionListener listener = QiupuHelper.listeners.get(key).get();
//	                if (listener != null) {
//	                    listener.updateStreamCommentUI(postId, streamForComment.type, totalCount, latestComments);
//	                }
//                }
//            }
//        }
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
        }else{
            ImageView likeToggle = (ImageView) findViewById(R.id.head_action_middle);
            if (null != likeToggle) {
                likeToggle.setImageResource(resid);
            }
        }
        
        tv_like.setText(String.format(getString(R.string.who_like),String.valueOf(qiupuPhoto.likes_count)));
    }

//    private void resetDisplayingViews() {
//        if (stream_comments_list.getHeaderViewsCount() > 0) {
//            stream_comments_list.removeHeaderView(streamPicView);
//        }
//
//        stream_comments_list.setAdapter(null);
//    }
    TextView tv_like;
    private void applyUpdatedStream(boolean force) {
//        resetDisplayingViews();

        if (null != qiupuPhoto) {
//            islike = streamForComment.iLike;
//
//            if (streamForComment.canReshare) {
//                overrideAndShowRightActionBtn(R.drawable.menu_share_attribute_enable, reshareClick);
//            } else {
//                overrideAndShowRightActionBtn(R.drawable.memu_forbid_share, reshareClick);
//            }
//
//            attachStreamProperty(R.id.id_stream_property, streamForComment);

//            streamItemView = StreamListAdapter.newStreamItemView(this, streamForComment, true);
//            stream_comments_list.addHeaderView(streamItemView);
            streamPicView = getLayoutInflater().inflate(R.layout.stream_pic, null);
            setupTimeSpanUi(streamPicView);
            stream_pic = (ImageView)streamPicView.findViewById(R.id.stream_pic);
            final ArrayList<QiupuPhoto> pList =  new ArrayList<QiupuPhoto>();
            pList.add(qiupuPhoto);
            stream_pic.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					IntentUtil.startPhotosViewIntent(QiupuCommentsPicActivity.this,
                            Long.valueOf(qiupuPhoto.album_id), qiupuPhoto.uid,
                            0,null,
                            pList,
                            qiupuPhoto.from_nick_name);
					
				}
			});
            tv_like = (TextView)streamPicView.findViewById(R.id.tv_like);
            updateLikeUi(qiupuPhoto.iliked);
////            loading_layout = streamPicView.findViewById(R.id.loading_layout);
////            tv_loading  = (TextView) streamPicView.findViewById(R.id.tv_loading);
//            stream_pic_container = (RelativeLayout)streamPicView.findViewById(R.id.stream_pic_container); 
//            
//          
            setupPosterImageRunner(streamPicView, qiupuPhoto.from_image_url);
            TextView tv_name = (TextView)streamPicView.findViewById(R.id.poster_name);
            tv_name.setText(qiupuPhoto.from_nick_name);
            stream_comments_list.addHeaderView(streamPicView);
            stream_comments_list.setFocusable(false);
            
//            stream_pic.setImageResource(R.drawable.photo_transparent);
//            final Photo link;
//            if (streamForComment.attachment.attachments.size() > mAttachIndex) {
//                link = (Photo) streamForComment.attachment.attachments.get(mAttachIndex);
//            } else {
//                link = (Photo) streamForComment.attachment.attachments.get(0);
//            }

            if(!TextUtils.isEmpty(qiupuPhoto.photo_url_small)) {
            	shootImageRunner(qiupuPhoto.photo_url_small);
            }


//            mDisplayCommentList.addAll(streamForComment.comments.getCommentList());
//        	final boolean hasmore = mTotalCommentCount > mDisplayCommentList.size();
            final boolean hasmore = true;
            CommentsAdapter sa = new CommentsAdapter(this, mDisplayCommentList, hasmore, isStreamOwner());
            stream_comments_list.setAdapter(sa);

            if(force) {
                mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
            }else {
                if (hasmore) {
                    mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
                }
            }
        }
    }
    
    void setupTimeSpanUi(View streamPicView) {
        TextView postTimeTV = (TextView) streamPicView.findViewById(R.id.post_time);
        if (null != postTimeTV) {
            String day = DateUtil.converToRelativeTime(this, qiupuPhoto.created_time);
            postTimeTV.setText(day);
        }
    }
    
    private void setupPosterImageRunner(View parent, String url) {
        ImageView posterIconIV = (ImageView) parent.findViewById(R.id.user_icon);
        if (isEmpty(url)) {
            posterIconIV.setImageResource(R.drawable.default_user_icon);
        } else {
            ImageRun imagerun = new ImageRun(null, url, 0);
            imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
            imagerun.noimage = false;
            imagerun.addHostAndPath = true;
            imagerun.setRoundAngle = true;
            if(imagerun.setImageView(posterIconIV) == false)
                imagerun.post(null);
        }
        posterIconIV.setOnClickListener(userClick);
    }
    
    protected OnClickListener userClick = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            IntentUtil.startUserDetailIntent(QiupuCommentsPicActivity.this,qiupuPhoto.from_user_id, qiupuPhoto.from_nick_name);
        }
    };
    
    private void shootImageRunner(String photoUrl) {
		ImageRun photo_1 = new ImageRun(mHandler, photoUrl, 0);
		photo_1.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_SCREENSHOT;
		photo_1.SetOnImageRunListener(new ImageRun.OnImageRunListener() {

			@Override
			public void onLoadingFinished() {
				Message msg = mHandler.obtainMessage(LOAD_IMAGE_SUCCESS);
                msg.sendToTarget();
			}

			@Override
			public void onLoadingFailed() {
				Message msg = mHandler.obtainMessage(LOAD_IMAGE_FAILED);
                msg.sendToTarget();
                showCustomToast(R.string.loading_failed);
			}});
		photo_1.addHostAndPath = true;
		final Resources resources = stream_pic.getResources();
		photo_1.noimage = true;
		photo_1.isRoate = false;
		if(photo_1.setImageView(stream_pic) == false)
		photo_1.post(null);
	}

    protected boolean isStreamOwner() {
        boolean ret = false;
        if (null != qiupuPhoto) {
            ret = qiupuPhoto.uid == getSaveUid();
        }

        return ret;
    }

//    private void gotoReshareActivity() {
//        final String externalText = getCommentContent();
//        if (null != streamPicView) {
//            IntentUtil.startComposeIntent(this, streamForComment,
//                    new long[]{streamForComment.fromUser.uid}, externalText, "reshare");
//        }
//    }

//    private final OnClickListener reshareClick = new OnClickListener() {
//        public void onClick(View v) {
//            if (null != streamForComment && streamForComment.canReshare) {
//                if (streamForComment.isPrivacy()) {
//                    DialogUtils.showOKDialog(QiupuCommentsPicActivity.this, R.string.commments_tips_title,
//                            R.string.comments_tips_message, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            gotoReshareActivity();
//                        }
//                    });
//                } else {
//                    gotoReshareActivity();
//                }
//            } else {
//                showCustomToast(R.string.menu_share_attribute_disable);
//            }
//        }
//    };

//    private final OnClickListener shareApkClick = new OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            gotoReshareActivity();
//        }
//    };

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
        Intent intent = new Intent(context, QiupuCommentsPicActivity.class);
        intent.putExtra(BpcApiUtils.SEARCH_KEY_ID, apkInfo.apk_server_id);
        intent.putExtra(QiupuMessage.BUNDLE_POST_IS_LIKE, apkInfo.iLike);
        intent.putExtra(QiupuMessage.BUNDLE_POST_APK, true);
        intent.putExtra(QiupuMessage.BUNDLE_APKINFO, apkInfo);
        return intent;
    }

//    final Touch.ClickListener clickListener = new Touch.ClickListener() {
//		
//		@Override
//		public void onClick() {
//			if(!isVisible) {
//				show();
//			} 
//			
//		}
//	};
	
//	private void show() {
//		stream_pic.setScaleType(ScaleType.FIT_CENTER);
//		title_container.setVisibility(View.VISIBLE);
//		photo_comments_layout.setVisibility(View.VISIBLE);
//		dividerView.setVisibility(View.VISIBLE);
////		mDisplayCommentList.addAll(streamForComment.comments.stream_posts);
//        final boolean hasmore = mTotalCommentCount > mDisplayCommentList.size();
//        CommentsAdapter sa = new CommentsAdapter(this, mDisplayCommentList, hasmore, isStreamOwner());
//        stream_comments_list.setAdapter(sa);
//		stream_comments_list.setDividerHeight(divider_height);
//		stream_pic.setOnTouchListener(null);
//		stream_comments_list.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				return false;
//			}
//			
//		});
//		int streamContainerHeight = photo_comments_layout.getTop()-title_container.getBottom();
//		stream_pic_container.setLayoutParams(new FrameLayout.LayoutParams(
//				FrameLayout.LayoutParams.FILL_PARENT,streamContainerHeight));
//		stream_pic_container.removeView(stream_pic);
//		stream_pic.setLayoutParams(
//				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
//						RelativeLayout.LayoutParams.WRAP_CONTENT));
//		stream_pic_container.addView(stream_pic);
////		scrollView.setScroll(true);
//		isVisible = !isVisible;
//	}
//	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if(isVisible==false&&keyCode==KeyEvent.KEYCODE_BACK&& event.getRepeatCount() == 0) {
//			show();
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("photo_position", photo_position);
        data.putExtra("qiupuPhoto", qiupuPhoto);
        setResult(RESULT_OK, data);
        super.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        

        QiupuHelper.unRegisterTargetLikeListener(getClass().getName());
        CommentItemView.setCommentActionListener(null);

        mCommentText.removeTextChangedListener(mTextWatcher);
//        mCommentText.setOnEditorActionListener(null);
        mCommentText.destroy();
        mCommentText = null;
    }

    @Override
    public void onTargetLikeCreated(String targetId, String targetType) {
        if (QiupuConfig.TYPE_COMMENT.equals(targetType)) {
            final long cId = Long.valueOf(targetId);
            for (Stream_Post comment : mDisplayCommentList) {
                if (cId == comment.id) {
                    comment.iLike = true;
                    comment.likerList.count++;
//                    comment.likerList.friends.remove(comment);
                    final QiupuSimpleUser user = AccountServiceUtils.touchMySimpleUserInfo();
                    if (null != user) {
                        comment.likerList.friends.add(user);
                    }

                    refreshCommentLikeUi(cId, comment);
                    onCommentsUpdated(photo_id, mDisplayCommentList, mTotalCommentCount);

                    break;
                }
            }
        } else {
            qiupuPhoto.iliked = true;
            qiupuPhoto.likes_count++;
            updateLikeUi(qiupuPhoto.iliked);
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
                    onCommentsUpdated(photo_id, mDisplayCommentList, mTotalCommentCount);

                    break;
                }
            }
        } else {
            qiupuPhoto.iliked = false;
            qiupuPhoto.likes_count--;
            updateLikeUi(qiupuPhoto.iliked);

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
//        String content = null;
//        if (null != mCommentText) {
//            CharSequence text = mCommentText.getText();
//            if (text instanceof Spanned) {
//                Spanned spannedText = (Spanned) text;
//                URLSpan[] urlSpans = spannedText.getSpans(0, text.length(), URLSpan.class);
//                if (null != urlSpans && urlSpans.length > 0) {
////                    content = Html.toHtml(spannedText);
////                    if (content.startsWith("<p>")) {
////                        final int len = content.length();
////                        final int start = 3; // lend of "<p>"
////                        int end = content.lastIndexOf("</p>");
////                        if (end <= 0) {
////                            end = len;
////                        }
////                        content = content.substring(start, end);
////                    }
//
//                    int spanStart = 0, spanEnd = 0;
//                    String url;
//                    String name;
//                    StringBuilder stringBuilder = new StringBuilder();
//                    for(URLSpan item : urlSpans) {
//                        spanStart = spannedText.getSpanStart(item);
//                        // Append non-spanned text after the end of last URLSpan item.
//                        stringBuilder.append(text.subSequence(spanEnd, spanStart));
//
//                        spanEnd = spannedText.getSpanEnd(item);
//                        name = text.subSequence(spanStart, spanEnd).toString();
//                        url = item.getURL();
//                        // Append the encoding spanned text.
//                        stringBuilder.append("<a href='").append(url).append("'>").
//                                append(name).append("</a>");
//                    }
//                    // Append the non-spanned text on the tail of spans.
//                    stringBuilder.append(text.subSequence(spanEnd, text.length()));
//                    content = stringBuilder.toString();
//                } else {
//                    content = text.toString();
//                }
//            } else {
//                content = text.toString();
//            }
//        }
//
//        return content;
    }

    protected String getObjectId() {
        return photo_id;
    }
}
