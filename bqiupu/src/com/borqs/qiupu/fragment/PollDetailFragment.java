package com.borqs.qiupu.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import twitter4j.AsyncQiupu;
import twitter4j.PollInfo;
import twitter4j.PollItemInfo;
import twitter4j.QiupuSimpleUser;
import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationContext;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.adapter.CommentsAdapter;
import com.borqs.common.adapter.PollDetailAdapter;
import com.borqs.common.adapter.PollDetailAdapter.AddPollItemListener;
import com.borqs.common.adapter.PollDetailAdapter.LoaderMoreListener;
import com.borqs.common.listener.CommentActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.view.CommentItemView;
import com.borqs.common.view.ConversationMultiAutoCompleteTextView;
import com.borqs.common.view.PollChildItemView;
import com.borqs.common.view.PollChildItemView.PollItemCheckActionListener;
import com.borqs.common.view.SNSItemView;
import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.ui.QiupuCommentsActivity.MyTextWatcher;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;
import com.borqs.qiupu.util.ToastUtil;

public class PollDetailFragment extends BasicFragment implements PollItemCheckActionListener, 
        AnimationListener, CommentActionListener, LoaderMoreListener, AddPollItemListener {

    private final static String TAG         = "PollDetailFragment";

    private ListView            mListView;
//    private ListView mPollCommentListView;
//    private TextView            mEmptyTextView;
    private TextView mDeadLineDetail, mLeftTicketCount, mPollTitle, mCreatorName, mCreateTime, mDescriptionText;
    private ImageView mCreatorIcon, mDescriptionArrow;
    private View mDescriptionLayout, mBottomView;

    private String mReferredCommentId;

    private String              mPollId     = "";
    private MainHandler         mHandler;
    private Activity mActivity;
    private AsyncQiupu mAsyncQiupu;
    private PollDetailAdapter mDetailAdapter;
    private PollInfo mPollInfo;
    public static final int INCOMING = 0;
    public static final int GOING    = 1;
    public static final int ENDING   = 2;
    private static final int PAGE_COUNT = 20;
    private static final int MODE_REVERT = 2;
    private ProgressDialog mprogressDialog;
    private View mHeaderView;
    
    private ConversationMultiAutoCompleteTextView mCommentText;
    private TextWatcher mTextWatcher;
    private View comments_mention_button;

    public static String        POLL_ID_KEY = "POLL_ID_KEY";
    PollDetailFragmentCallBack mCallBackListener;
    private boolean mForceRefresh;
    private boolean mShowMoreButton = false;
    private static final String ADD_POLL_ITEM_KEY = "ADD_POLL_ITEM_KEY";

    public PollDetailFragment() {
        
    }

    public PollDetailFragment(PollInfo pollInfo) {
        mPollInfo = pollInfo;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mReferredCommentId = null;
        if (mActivity instanceof PollDetailFragmentCallBack) {
        	mCallBackListener = (PollDetailFragmentCallBack)activity;
        	mCallBackListener.getPollDetailFragment(this);
        	mPollId = mCallBackListener.getPollId();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncQiupu = new AsyncQiupu(ConfigurationContext.getInstance(),null,null);
        mHandler = new MainHandler();
        mDetailAdapter = new PollDetailAdapter(mActivity, this);
        mDetailAdapter.setAddPollItemListener(this);
        mDetailAdapter.registerCheckClickActionListener(getClass().getName(), this);
        CommentItemView.setCommentActionListener(this);
    }

    View mCommentListUI, comments_share_button;
//    View mCommentCount;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.poll_detail_main, null);
        mBottomView = view.findViewById(R.id.bottom_layout);
        mCommentText = (ConversationMultiAutoCompleteTextView) view.findViewById(R.id.compose_text);
        final boolean enableCommitBtn = null != mCommentText && !TextUtils.isEmpty(mCommentText.getText());
        comments_share_button = view.findViewById(R.id.comments_share_button);
        comments_share_button.setEnabled(enableCommitBtn);
        mTextWatcher = new MyTextWatcher(comments_share_button);
        mCommentText.addTextChangedListener(mTextWatcher);
        comments_share_button.setOnClickListener(new CommentBtnClickListener());
        comments_mention_button = view.findViewById(R.id.comments_mention_button);
        if (null != comments_mention_button) {
            comments_mention_button.setOnClickListener(ConversationMultiAutoCompleteTextView.instanceMentionButtonClickListener(mActivity, mCommentText));
        }
        mCommentListUI = view.findViewById(R.id.poll_comment_list_ui);
        mBottomView.setVisibility(View.GONE);
        mListView = (ListView) view.findViewById(R.id.default_listview);
        mListView.setSelector(R.drawable.list_selector_background);
        mListView.addHeaderView(buildHeaderView());
        mListView.setAdapter(mDetailAdapter);
        mListView.setVisibility(View.GONE);
        showDescription();
        return view;
    }

    private class CommentBtnClickListener implements OnClickListener {
        public void onClick(View view) {
            boolean login = false;
            if (mActivity instanceof PollDetailFragmentCallBack) {
                login = mCallBackListener.ensureLogin();
            }
            if (login) {
                Message msg = mHandler.obtainMessage(COMMENTS_ADD);
                msg.sendToTarget();
            } else {
                Log.d(TAG, "error, need to login.");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	refreshHeaderView();
    	mHandler.obtainMessage(SYNC_POLL).sendToTarget();
    	mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if(mDetailAdapter != null) {
    		mDetailAdapter.unRegisterCheckClickActionListener(getClass().getName());
    	}
    	mCommentText.removeTextChangedListener(mTextWatcher);
        mCommentText.destroy();
    }

    private View buildHeaderView() {
        mHeaderView = LayoutInflater.from(mActivity).inflate(
                R.layout.poll_detail_header_view, null);
        mHeaderView.setVisibility(View.GONE);

        mCreatorIcon = (ImageView) mHeaderView.findViewById(R.id.creator_icon);
        mCreatorIcon.setOnClickListener(imageListener);
        mCreatorName = (TextView) mHeaderView.findViewById(R.id.creator_name);
        mCreateTime = (TextView) mHeaderView.findViewById(R.id.create_time);
        mPollTitle = (TextView) mHeaderView.findViewById(R.id.detail_header_title);
        mDeadLineDetail = (TextView) mHeaderView.findViewById(R.id.left_time);
        mLeftTicketCount = (TextView) mHeaderView.findViewById(R.id.left_ticket_count);

        mDescriptionText = (TextView) mHeaderView.findViewById(R.id.description_text);
        mDescriptionArrow = (ImageView) mHeaderView.findViewById(R.id.folder_arrow);
        mDescriptionLayout = mHeaderView.findViewById(R.id.description_bottom_layout);
        mDescriptionLayout.setOnClickListener(foldListener);

//        mFoldView.setOnClickListener(foldListener);
        return mHeaderView;
    }

    private View.OnClickListener foldListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDescription();
        }
    };

    private boolean mShowDescription = true;

    private void setItem(boolean flag, TextUtils.TruncateAt at) {
        mDescriptionText.setSingleLine(flag);// isSingleLine
        mDescriptionText.setHorizontallyScrolling(flag);// isUnfoldStatus
        mDescriptionText.setEllipsize(at);
    }

    private void showDescription() {
        Log.d(TAG, "showDescription() mShowDescription = " + mShowDescription);
        if (mShowDescription) {
            setItem(true, TextUtils.TruncateAt.END);
            mDescriptionArrow.setImageResource(R.drawable.down_icon);
        } else {
            setItem(false, null);
            mDescriptionArrow.setImageResource(R.drawable.up_icon);
        }
        mShowDescription = !mShowDescription;
    }

    private View.OnClickListener imageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPollInfo.sponsor != null) {
                IntentUtil.startProfileFromPoll(mActivity, mPollInfo.sponsor.uid, mPollInfo.sponsor.nick_name);
            }
        }
    };

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (PollChildItemView.class.isInstance(view)) {
                PollChildItemView itemView = (PollChildItemView) view;
                if(itemView.getPollItemInfo().viewer_voted == false || mPollInfo.mode == MODE_REVERT) {
                	itemView.setCheckStatus();
                }
            } else if (CommentItemView.class.isInstance(view)){
                
            } else {
                Log.d(TAG, "mItemClickListener error, view = " + view);
            }
        }
    };

    private final int                       SYNC_POLL          = 101;
    private final int                       SYNC_POLL_END      = 102;
    private final int                       VOTE_POLL          = 103;
    private final int                       VOTE_POLL_END      = 104;
    private final int                       DELETE_POLL        = 105;
    private final int                       DELETE_POLL_END    = 106;
    private final int                       ADD_POLL_ITEM      = 107;
    private final int                       ADD_POLL_ITEM_END  = 108;
    private final static int COMMENTS_GET = 1;
    private final static int COMMENTS_GET_END = 2;
    private final static int COMMENTS_ADD = 6;
    private final static int COMMENTS_ADD_END = 7;
    private final static int COMMENTS_REMOVE = 12;
    private final static int COMMENTS_REMOVE_END = 13;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SYNC_POLL: {
                    syncPollInfo(mPollId, false, true);
                }
                    break;
                case SYNC_POLL_END: {
                    if (isNotOwner()) {
                        end();
                    } else {
                        showLeftBtn();
                    }
                    if (msg.getData().getBoolean("RESULT")) {
                        refreshUI();
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, false);
                    }
                }
                    break;
                case ADD_POLL_ITEM: {
                    String item_ids = getNewSelectIds();
                    ArrayList<String> msgList = msg.getData().getStringArrayList(ADD_POLL_ITEM_KEY);
                    addPollItems(mPollId, item_ids, msgList);
                }
                break;
                case ADD_POLL_ITEM_END: {
//                    end();
                    showLeftBtn();
                    if (msg.getData().getBoolean("RESULT")) {
                        refreshUI();
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, false);
                    }
                }
                break;
                case VOTE_POLL: {
                    vote();
                }
                break;
                case VOTE_POLL_END: {
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) { }
                    if (msg.getData().getBoolean("RESULT")) {
                        refreshUI();
                        ToastUtil.showShortToast(mActivity, mHandler, R.string.vote_successfully);
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, false);
                    }
                    break;
                }
                case DELETE_POLL: {
                    delPoll();
                    break;
                }
                case DELETE_POLL_END: {
                	try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) { }
                    if (msg.getData().getBoolean("RESULT")) {
                    	ToastUtil.showShortToast(mActivity, mHandler, R.string.operate_succeed);
                    	Intent intent = new Intent();
                    	intent.putExtra(POLL_ID_KEY,mPollId);
                    	mActivity.setResult(Activity.RESULT_OK,intent);
                    	mActivity.finish();
                    } else {
                        ToastUtil.showOperationFailed(mActivity, mHandler, false);
                    }
                }
                    break;
                case COMMENTS_GET: {
                    loadCommentFromServer(getCurrentPage());
                    break;
                }
                case COMMENTS_GET_END: {
                    if (isNotOwner()) {
                        end();
                    } else {
                        showLeftBtn();
                    }
                    if(mForceRefresh) {
                        mForceRefresh = false;
                    }
                    try {
                        mprogressDialog.dismiss();
                        mprogressDialog = null;
                    } catch (Exception ne) { }
                    boolean result = msg.getData().getBoolean("RESULT");
                    if (result) {
                        Log.d(TAG, "get comment end, comment size = " + mDisplayCommentList.size());
                        if (mDisplayCommentList.size() > 0 && mPollInfo != null && mPollInfo.pollItemList != null) {
                            refreshCommentListUI();
                        }
                    }
                    break;
                }
                case COMMENTS_ADD: {
                    String content = getCommentContent();
                    if (TextUtils.isEmpty(content)) {
                        if (mActivity instanceof PollDetailFragmentCallBack) {
                            mCallBackListener.showCustomToastInPoll(R.string.input_content);
                        }
                        return;
                    } else {
                        postComment(content);
                    }
                    break;
                }
                case COMMENTS_ADD_END: {
                    try{
                        mActivity.dismissDialog(DIALOG_ADD_COMMENTS);
                    } catch (Exception e) {
                    }
                    boolean result = msg.getData().getBoolean("RESULT");
                    if (result) {
                        Log.d(TAG, "add comment end!");
                        mPollInfo.comment_count++;
                        refreshCommentListUI();
                        setCommentButtonStatus();
                    }
                    break;
                }
                case COMMENTS_REMOVE: {
                    mActivity.showDialog(DIALOG_REMOVE_COMMENT);
                    long comment_id = msg.getData().getLong("comment_id");
                    removeComments(comment_id);
                    break;
                }
                case COMMENTS_REMOVE_END: {
                    try {
                        mActivity.dismissDialog(DIALOG_REMOVE_COMMENT);
                    } catch (Exception e) {
                    }
                    boolean result = msg.getData().getBoolean("RESULT");
                    if (result) {
                        mPollInfo.comment_count--;
                        refreshCommentListUI();
                    }
                    break;
                }
                default:                    break;
            }
        }
    }

    private void showLeftBtn() {
        if (mCallBackListener != null) {
            mCallBackListener.showRefreshButton(false);
        }
    }

    private boolean isNotOwner() {
        boolean isOwner = mPollInfo != null && mPollInfo.sponsor != null
                && AccountServiceUtils.getBorqsAccountID() == mPollInfo.sponsor.uid;
        boolean canVote = mPollInfo != null && mPollInfo.viewer_can_vote;
        if (isOwner) {
            if (canVote) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void setCommentButtonStatus() {
        if (mActivity.isFinishing()) {
            return;
        }

        mCommentText.setText(null);

//        if (null != streamForComment && streamForComment.canComment) {
            mCommentText.setFocusable(true);
            mCommentText.setFocusableInTouchMode(true);
            mCommentText.setHint(R.string.type_to_compose_text_enter_to_send);
            comments_share_button.setVisibility(View.VISIBLE);
            comments_mention_button.setClickable(true);
//        } else {
//            mCommentText.setFocusable(false);
//            mCommentText.setHint(R.string.menu_comment_attribute_disable);
//            comments_share_button.setVisibility(View.GONE);
//            comments_mention_button.setClickable(false);
//        }
    }

    private String getCommentContent() {
        return ConversationMultiAutoCompleteTextView.getConversationText(mCommentText);
    }

    private void refreshHeaderView() {
        if(mPollInfo == null) {
            return ;
        }

        mListView.setVisibility(View.VISIBLE);
        mHeaderView.setVisibility(View.VISIBLE);
        mCreatorIcon.setImageResource(R.drawable.default_user_icon);
        if(mPollInfo.sponsor != null) {
//            Log.d(TAG, "image_url = " + mPollInfo.sponsor.profile_image_url + " name = " + mPollInfo.sponsor.nick_name) ;
            initImageUI(mPollInfo.sponsor.profile_image_url);
            mCreatorName.setText(mPollInfo.sponsor.nick_name);
        }
        mCreateTime.setText(DateUtil.converToRelativeTime(mActivity, new Date(mPollInfo.created_time)));

        if (TextUtils.isEmpty(mPollInfo.description)) {
            mDescriptionLayout.setVisibility(View.GONE); 
        } else {
//            mDescription.setText(mPollInfo.description);
            SNSItemView.attachHtml(mDescriptionText, mPollInfo.description);
            mDescriptionText.setOnClickListener(foldListener);

            DisplayMetrics metric = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
            int screenWidth = metric.widthPixels;

            Rect bounds = new Rect();
            TextPaint paint = mDescriptionText.getPaint();
            paint.getTextBounds(mPollInfo.description, 0, mPollInfo.description.length(), bounds);
            int textWidth = bounds.width();

            if (textWidth >= screenWidth) {
                mDescriptionArrow.setVisibility(View.VISIBLE);
            } else {
                mDescriptionArrow.setVisibility(View.GONE);
            }
//            if (mDescriptionText.getLineCount() > 1) {
//                mDescriptionArrow.setVisibility(View.VISIBLE);
//            } else {
//                mDescriptionArrow.setVisibility(View.GONE);
//            }

            mDescriptionLayout.setVisibility(View.VISIBLE);
        }

        setPollTimeUI();
        setPollCountUI();
    }

    private void refreshUI() {
        if (mPollInfo == null) {
            return;
        }

        Log.d(TAG, "mPollInfo.viewer_can_vote = " + mPollInfo.viewer_can_vote);
        
        if (mPollInfo.viewer_can_vote == false) {
            showPollButton(false);
        } else {
            showPollButton(true);
        }

        refreshHeaderView();

        if (mPollInfo.sponsor != null
                && AccountServiceUtils.getBorqsAccountID() == mPollInfo.sponsor.uid) {
            if (mCallBackListener != null) {
                mCallBackListener.showDeleteButton(true,
                    mPollInfo.viewer_can_vote, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDeleteConfirmDialog();
                        }
                });
            }
        } else {
            if (mCallBackListener != null) {
                mCallBackListener.showDeleteButton(false, mPollInfo.viewer_can_vote, null);
            }
        }

        Log.d(TAG, "mPollInfo.viewer_can_vote = " + mPollInfo.viewer_can_vote);
        if (mPollInfo.viewer_can_vote) {
            mListView.setOnItemClickListener(mItemClickListener);
        } else {
            mListView.setFocusable(false);
            mListView.setOnItemClickListener(null);
        }

        mBottomView.setVisibility(View.VISIBLE);

//        Log.d(TAG, "mPollInfo.description = " + mPollInfo.description);

        mSelectItemsList.clear();
        mNewSelectItemsList.clear();
        generateSelectedItems(mPollInfo.pollItemList);
        refreshCommentListUI();
    }

    public void showDeleteConfirmDialog() {
        DialogUtils.showConfirmDialog(mActivity,getString(R.string.delete_poll),
                getString(R.string.confirm_delete_poll), delPoolListener,null);
    }

    private void refreshCommentListUI() {
        Log.d(TAG, "refreshCommentListUI() mPollInfo.pollItemList.size() = " + mPollInfo.pollItemList.size() + 
                " mDisplayCommentList.size() = " + mDisplayCommentList.size());

        if (mPollInfo.sponsor != null) {
            boolean isCreator = mPollInfo.sponsor.uid == AccountServiceUtils.getBorqsAccountID();
            mDetailAdapter.alterPollList(mPollInfo.pollItemList, mSelectItemsList, 
                mPollInfo.viewer_can_vote, mPollInfo.multi, mPollInfo.has_voted,
                mDisplayCommentList, mShowMoreButton, mPollInfo.comment_count, mPollInfo.can_add_items, 
                (int)mPollInfo.attend_status, mPollInfo.viewer_left, isCreator, mPollInfo.mode);
        }
    }

    DialogInterface.OnClickListener delPoolListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            delPoll();
        }
    };

    private static final long ONE_DAY_UNIT = 24*60*60*1000;

    private void setPollTimeUI() {
        Log.d(TAG, "mPollInfo.left_time = " + mPollInfo.left_time);
        if (mPollInfo.attend_status == GOING) {
            if (mPollInfo.left_time < 0) {
                mDeadLineDetail.setText(R.string.no_deadline);
            } else {
                //TODO: countdown function
                int day = (int) (mPollInfo.left_time/ONE_DAY_UNIT);
                float hour = mPollInfo.left_time%ONE_DAY_UNIT;
                if (day <= 0 && hour > 0) {
                    mDeadLineDetail.setText(String.format(mActivity.getString(R.string.vote_left_time), 1));
                } else if (day > 0){
                    int deadline = 0;
                    if (hour > 0) {
                        deadline += 1;
                    }
                    deadline += day;
                    mDeadLineDetail.setText(String.format(mActivity.getString(R.string.vote_left_time), deadline));
                }
            }
        } else if (mPollInfo.attend_status == INCOMING){
            mDeadLineDetail.setText(R.string.poll_incoming_title);
        } else if (mPollInfo.attend_status == ENDING){
            mDeadLineDetail.setText(R.string.vote_finished);
        } else {
            Log.d(TAG, "no such status, mPollInfo.attend_status = " + mPollInfo.attend_status);
        }
    }

    private void showPollButton(boolean show) {
        if (mCallBackListener != null) {
            mCallBackListener.showActionPollButton(show);
        }
    }

    private void setPollCountUI() {
    	if(mPollInfo != null && isDetached() == false && getActivity() != null) {
    		Log.d(TAG, "mPollInfo.viewer_left = " + mPollInfo.viewer_left);
    		
    		if(mPollInfo.attend_status == ENDING) {
    			if(mPollInfo.has_voted) {
    				mLeftTicketCount.setText(StringUtil.setVoteCountSpannable(mActivity, String.format(getString(R.string.vote_left_ticket_voted), mSelectItemsList.size())));
    			}else {
    				mLeftTicketCount.setText(R.string.vote_left_ticket_unvote);
    			}
    		}else if(mPollInfo.attend_status == GOING) {
    			if(mPollInfo.viewer_can_vote == false) {
        			mLeftTicketCount.setText(R.string.vote_left_ticket_zero);
        		}else {
        			mLeftTicketCount.setText(StringUtil.setVoteCountSpannable(mActivity, String.format(getString(R.string.vote_left_ticket_count), mPollInfo.viewer_left)));		
        		}
    		}else {
    			mLeftTicketCount.setVisibility(View.GONE);
    		}
    		
    		mPollTitle.setText(mPollInfo.title);
    	}
    }

    private boolean inLoadingPoll;
    private Object  mLockSyncPollInfo = new Object();

    public void syncPollInfo(final String poll_ids, final boolean with_member,
            boolean with_items) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "checkQiupuVersion, ignore while no connection.");
            return;
        }

        if (inLoadingPoll == true) {
            ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
            return;
        }

        synchronized (mLockSyncPollInfo) {
            inLoadingPoll = true;
        }

//        if (isNotOwner()) {
            begin();
//        }

        mAsyncQiupu.syncPollInfo(AccountServiceUtils.getSessionID(), poll_ids,
                with_items, new TwitterAdapter() {
                    public void getPollList(ArrayList<PollInfo> pollList) {
                        Log.d(TAG, "finish syncPollInfo=" + pollList.size());

                        if (pollList != null && pollList.size() > 0) {
                        	mPollInfo = pollList.get(0);
                        } else {
                            Log.d(TAG, "server return data error ");
//                            return;
                        }

                        Message msg = mHandler.obtainMessage(SYNC_POLL_END);
                        msg.getData().putBoolean("RESULT", true);
                        msg.sendToTarget();
                        synchronized (mLockSyncPollInfo) {
                            inLoadingPoll = false;
                        }
                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        synchronized (mLockSyncPollInfo) {
                            inLoadingPoll = false;
                        }
                        Message msg = mHandler.obtainMessage(SYNC_POLL_END);
                        msg.getData().putBoolean("RESULT", false);
                        msg.sendToTarget();
                    }
                });
    }

    public void votePoll() {
        mHandler.obtainMessage(VOTE_POLL).sendToTarget();
    }

    private boolean inLoadingVote;
    private Object  mLockVote = new Object();

    public void vote() {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "vote, ignore while no connection.");
            return;
        }
        if(mPollInfo == null) {
            Log.d(TAG, "poll info is null ");
            return;
        }
        
        if (mPollInfo.viewer_can_vote == false) {
            if (mPollInfo.has_voted && mPollInfo.viewer_left <= 0) {
                ToastUtil.showShortToast(mActivity, mHandler, R.string.has_voted_msg);
            } else if(mPollInfo.left_time < 0) {
                ToastUtil.showShortToast(mActivity, mHandler, R.string.can_vote_msg);
            }
            return;
        }
        
        String selectIds = getNewSelectIds();
        if(selectIds.length() <= 0) {
            ToastUtil.showShortToast(mActivity, mHandler, R.string.no_choice_msg);
            return;
        }
        
        if (inLoadingVote == true) {
            ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
            return;
        }
        
        synchronized (mLockVote) {
            inLoadingVote = true;
        }
        
//        begin();
        showProcessDialog(R.string.voting_progress_mes, false, true, false);
        
        mAsyncQiupu.vote(AccountServiceUtils.getSessionID(), mPollId,
                selectIds, new TwitterAdapter() {
            public void vote(PollInfo pollInfo) {
                Log.d(TAG, "finish pollInfo=" + pollInfo);
                
                if (pollInfo != null) {
                    mPollInfo = null;
                    mPollInfo = pollInfo;
                } else {
                    Log.d(TAG, "server return data is null");
//                            return;
                }
                
                Message msg = mHandler.obtainMessage(VOTE_POLL_END);
                msg.getData().putBoolean("RESULT", true);
                msg.sendToTarget();
                synchronized (mLockVote) {
                    inLoadingVote = false;
                }
            }
            
            public void onException(TwitterException ex,
                    TwitterMethod method) {
                synchronized (mLockVote) {
                    inLoadingVote = false;
                }
                Message msg = mHandler.obtainMessage(VOTE_POLL_END);
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
            }
        });
    }
    public void delPoll() {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "vote, ignore while no connection.");
            return;
        }
        if(mPollInfo == null) {
        	Log.d(TAG, "poll info is null ");
        	return;
        }


        synchronized (mLockVote) {
            inLoadingVote = true;
        }

//        begin();
        showProcessDialog(R.string.qiupu_deleting_apks, false, true, false);

        mAsyncQiupu.deletePoll(AccountServiceUtils.getSessionID(), mPollId, new TwitterAdapter() {
            
            @Override
            public void deletePoll(boolean suc) {
            super.deletePoll(suc);
                Message msg = mHandler.obtainMessage(DELETE_POLL_END);
                msg.getData().putBoolean("RESULT", true);
                msg.sendToTarget();
                synchronized (mLockVote) {
                    inLoadingVote = false;
                }
            }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        synchronized (mLockVote) {
                            inLoadingVote = false;
                        }
                        Message msg = mHandler.obtainMessage(DELETE_POLL_END);
                        msg.getData().putBoolean("RESULT", false);
                        msg.sendToTarget();
                    }
                });
    }

    private ArrayList<String> mSelectItemsList = new ArrayList<String>();
    
    private ArrayList<String> mNewSelectItemsList = new ArrayList<String>();
    private void generateSelectedItems(ArrayList<PollItemInfo> items) {
    	if(items != null ){
    		mSelectItemsList.clear();
    		for(int i=0; i<items.size(); i++) {
    			final PollItemInfo info = items.get(i);
    			if(info.viewer_voted) {
    				mSelectItemsList.add(info.item_id);
    			}
    		}
    	}
    }

    private void changeSelect(String bindid, boolean isSelected) {
		Log.d(TAG, "changeSelect: " + bindid);
		if (isSelected) {
			if (!mSelectItemsList.contains(bindid)) {
				mSelectItemsList.add(bindid);
			}
			if(!mNewSelectItemsList.contains(bindid)) {
				mNewSelectItemsList.add(bindid);
			}
		} else {
			if (mSelectItemsList.contains(bindid)) {
				mSelectItemsList.remove(bindid);
			}
			if(mNewSelectItemsList.contains(bindid)) {
				mNewSelectItemsList.remove(bindid);
			}
		}
		mDetailAdapter.setSelectItems(mSelectItemsList) ;
	}
    
	@Override
	public void changeItemSelect(String itemId, boolean isSelect) {
		changeSelect(itemId, isSelect);
	}

	@Override
	public int getSelectCount() {
		return mSelectItemsList != null ? mSelectItemsList.size() : 0;
	}
	
	@Override
	public boolean canClick(String itemsId) {
		if(!mSelectItemsList.contains(itemsId) && mSelectItemsList.size() >= mPollInfo.multi) {
			return false;
		}else {
			return true;
		}
	}

    public void refreshPollUI() {
        mForceRefresh = true;
        mHandler.obtainMessage(SYNC_POLL).sendToTarget();
        mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
    }

	private String getids(ArrayList<String> list) {
		if(list == null) {
			return "";
		}
		Log.d(TAG, "list.size(): " + list.size());
		
		StringBuilder ids = new StringBuilder();
		for(int i=0; i<list.size(); i++) {
			if(ids.length() > 0) {
				ids.append(",");
			}
			ids.append(list.get(i));
		}
		return ids.toString();
	}
	
	private String getAllSelectIds() {
		return getids(mSelectItemsList);
	}
	
	private String getNewSelectIds() {
	    if (mPollInfo.mode == MODE_REVERT) {
	        mNewSelectItemsList.clear();
	        for (PollItemInfo item: mPollInfo.pollItemList) {
	            if (item.selected == true) {
	                mNewSelectItemsList.add(item.item_id);
	            }
	        }
	    }
		return getids(mNewSelectItemsList);
	}
	
	private void showProcessDialog(int resId, boolean CanceledOnTouchOutside, boolean Indeterminate, boolean cancelable){
		mprogressDialog = DialogUtils.createProgressDialog(mActivity, 
				resId, CanceledOnTouchOutside, Indeterminate, cancelable);
		mprogressDialog.setInverseBackgroundForced(true);
		mprogressDialog.show();    	
	}
	
	private void initImageUI(String image_url)
	{
		ImageRun imagerun = new ImageRun(mHandler, image_url, 0);
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.noimage = true;
	    imagerun.addHostAndPath = true;
	    imagerun.setRoundAngle=true;
        imagerun.setImageView(mCreatorIcon);
        imagerun.post(null);
	}
	
	public interface PollDetailFragmentCallBack {
		public void getPollDetailFragment(PollDetailFragment fragment);
		public String getPollId();
		public void showDeleteButton(boolean isOwner, boolean canVote, View.OnClickListener listener);
		public void showRefreshButton(boolean show);
		public void showActionPollButton(boolean show);
		public void showExceptionToast(TwitterException ex);
		public void showCustomToastInPoll(int resString);
		public boolean ensureLogin();
		public void pollLikeComment(Stream_Post comment);
		public void pollUnLikeComment(Stream_Post comment);
	}

    private class SlideTopListener implements View.OnClickListener{
        
        @Override
        public void onClick(View v) {
            handleSlideView();
        }
    }

    private boolean mIsSlideOut = false;

    private void handleSlideView() {
        Animation anim = null;
        if (mIsSlideOut == true) {
            anim = AnimationUtils.loadAnimation(mActivity, R.anim.slide_top_to_bottom);
            mCommentListUI.setClickable(false);
        } else {
            mCommentListUI.setVisibility(View.VISIBLE);
            anim = AnimationUtils.loadAnimation(mActivity, R.anim.slide_bottom_to_top);
            mCommentListUI.setClickable(true);
//            mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
        }
        mIsSlideOut = !mIsSlideOut;

        anim.setAnimationListener(this);
        mCommentListUI.startAnimation(anim);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (mIsSlideOut) {
            
        } else {
            mCommentListUI.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        
    }

    private void showMoreButton(List<Stream_Post> commentList) {
        if (commentList.size() < PAGE_COUNT) {
            mShowMoreButton = false;
        }else {
            mShowMoreButton = true;
        }
    }

    private int mTotalCommentCount;
    protected final Object mLock = new Object();
    protected boolean inProcess = false;

    private void loadCommentFromServer(final int page) {
        if (inProcess == true) {
            Log.d(TAG, "in load poll comments");
//            showCustomToast(R.string.string_in_processing);
            return;
        }

        synchronized (mLock) {
            inProcess = true;
        }

        if (isNotOwner()) {
            begin();
        }
//        showProcessDialog(R.string.string_in_processing, false, true, false);

//        final int pageIndex = -1;
//        final int pageSize = -1;
        Log.d(TAG, "page = " + page);
        mAsyncQiupu.getCommentsList(AccountServiceUtils.getSessionID(), QiupuConfig.TYPE_POLL, mPollId, page/*pageIndex*/, PAGE_COUNT/*pageSize*/, new TwitterAdapter() {
            public void getCommentsList(List<Stream_Post> commentList) {
                if (commentList != null) {
                    showMoreButton(commentList);
                    Log.d(TAG, "=== commentList.size() = " + commentList.size() + "mForceRefresh = " + mForceRefresh);
                    if(mForceRefresh || page == 0) {
                        mDisplayCommentList.clear();
                    }
                    mDisplayCommentList.addAll(commentList);
//                    onCommentPageLoaded(commentList, pageIndex);
                } else {
                    Log.d(TAG, "commentList is null");
                }

                synchronized (mLock) {
                    inProcess = false;
                }

                Message msg = mHandler.obtainMessage(COMMENTS_GET_END);
                msg.getData().putBoolean("RESULT", true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "loadCommentFromServer exception:" + ex.getMessage());
                if (mActivity instanceof PollDetailFragmentCallBack) {
                    mCallBackListener.showExceptionToast(ex);
                }

                Message msg = mHandler.obtainMessage(COMMENTS_GET_END);
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
                synchronized (mLock) {
                    inProcess = false;
                }
            }
        });
    }

    private int getCurrentPage() {
        if(mDisplayCommentList != null) {
            if(mForceRefresh) {
                return 0;
            }else {
                Log.d(TAG, "getCurrentPage() : page === " + mDisplayCommentList.size() / PAGE_COUNT);
                return  mDisplayCommentList.size() / PAGE_COUNT;
            }
        }else {
            return 0;
        }
    }

    private ArrayList<Stream_Post> mDisplayCommentList = new ArrayList<Stream_Post>();

    private void onCommentPageLoaded(final List<Stream_Post> commentList, final int pageIndex) {
        mHandler.post(new Runnable() {
            public void run() {
                if (commentList.size() > 0) {
                    mBottomView.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "onCommentPageLoaded() commentList.size() = " + commentList.size());
                }
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

//                refreshCommentsUi();

                mTotalCommentCount = commentList.size();
//                Log.d(TAG, "mTotalCommentCount: " + mTotalCommentCount + " mDisplayCommentList.size : " + mDisplayCommentList.size());
//                onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);
            }
        });
    }

    private void refreshCommentsUi() {
        final CommentsAdapter sa = refreshCommentAdapter();
//        mPollCommentListView.setAdapter(sa);
    }

    private CommentsAdapter refreshCommentAdapter() {
        final CommentsAdapter sa;
        final boolean hasMore = mTotalCommentCount > mDisplayCommentList.size();

        if (mDisplayCommentList.size() > 0) {
            sa = new CommentsAdapter(mActivity, mDisplayCommentList, hasMore, isStreamOwner());
        } else {
            sa = null;
        }
        return sa;
    }

    private boolean isStreamOwner() {
        boolean ret = false;
//        if (null != streamForComment && null != streamForComment.fromUser) {
//            ret = streamForComment.fromUser.uid == getSaveUid();
//        } else {
//            Log.e(TAG, "isStreamOwner, unexpected status: " + streamForComment);
//        }

        return ret;
    }

    private boolean commentProcess = false;
    private Object commentLock = new Object();
    private static final int DIALOG_ADD_COMMENTS = 10;
    private static final int DIALOG_REMOVE_COMMENT = 20;
    private boolean isReadyForAddComment() {
        if (commentProcess == true) {
            Log.d(TAG, "isReadyForAddComment, in load stream");
            if (mActivity instanceof PollDetailFragmentCallBack) {
                mCallBackListener.showCustomToastInPoll(R.string.string_in_processing);
            }
            return false;
        }
        mActivity.showDialog(DIALOG_ADD_COMMENTS);
        synchronized (commentLock) {
            commentProcess = true;
        }
        return true;
    }

    private void postComment(String content) {
        if (!isReadyForAddComment()) {
            Log.d(TAG, "postComment, could not request add comment now.");
            return;
        }

        synchronized (commentLock) {
            commentProcess = true;
        }

        mAsyncQiupu.postStreamComment(AccountServiceUtils.getSessionID(), mPollId, mReferredCommentId, content, QiupuConfig.TYPE_POLL, new TwitterAdapter() {
            public void getPostComment(Stream_Post cominfo) {
                if (QiupuConfig.LOGD) Log.d(TAG, "postStreamComment, CommentInfo:" + cominfo);
                if (cominfo != null) {
                    mTotalCommentCount++;
//                    mDisplayCommentList.add(0, cominfo);
                    mDisplayCommentList.add(cominfo);
//                        onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);
                }

                onCommentAdded(true);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "postPollComment exception:" + ex.getMessage());
                if (mActivity instanceof PollDetailFragmentCallBack) {
                    mCallBackListener.showExceptionToast(ex);
                }
                onCommentAdded(false, ex.getMessage());
            }
        });
    }

    private void onCommentAdded(boolean result) {
        if (result) {
            mReferredCommentId = null;
        }
        onCommentAdded(result, "");
    }

    private void onCommentAdded(boolean result, String error) {
        Message msg = mHandler.obtainMessage(COMMENTS_ADD_END);
        msg.getData().putBoolean("RESULT", result);

        msg.sendToTarget();
        synchronized (commentLock) {
            commentProcess = false;
        }
    }

    private Stream_Post mComment;
    private String[] mDialogItems;

    @Override
    public void commentItemListener(Stream_Post comment) {
        mComment = comment;
        showCommentDialog();
    }

    private void showCommentDialog() {
        mDialogItems = buildDialogItemContent();
        String dialogTitle = String.format(getString(R.string.context_menu_comment_title), mComment.username);

        DialogUtils.showItemsDialog(mActivity, dialogTitle, R.drawable.home_screen_loop_icon_default,
                mDialogItems, mChooseItemClickListener);
    }

    private String[] buildDialogItemContent() {
        ArrayList<String> commentList = new ArrayList<String>();
        commentList.add(getString(R.string.reply));

        if (null != mComment) {
            if (mComment.isOwnBy(AccountServiceUtils.getBorqsAccountID())) {
                commentList.add(0, getString(R.string.delete_comment));
            }

            if (mComment.likerList.count > 0) {
                commentList.add(0, getString(R.string.favour_people));
            }

            if (mComment.iLike) {
                commentList.add(0, getString(R.string.cancel_favour));
            } else {
                commentList.add(0, getString(R.string.favour));
            }
        }

        String[] dialogItems = new String[commentList.size()];
        return commentList.toArray(dialogItems);
    }

    private DialogInterface.OnClickListener mChooseItemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mDialogItems[which].equals(getString(R.string.favour))) {
                if (mActivity instanceof PollDetailFragmentCallBack) {
                    mCallBackListener.pollLikeComment(mComment);
                }
            } else if (mDialogItems[which].equals(getString(R.string.cancel_favour))) {
                if (mActivity instanceof PollDetailFragmentCallBack) {
                    mCallBackListener.pollUnLikeComment(mComment);
                }
            } else if (mDialogItems[which].equals(getString(R.string.favour_people))) {
                seeFavourList();
            } else if (mDialogItems[which].equals(getString(R.string.delete_comment))) {
                deletetComment();
            } else if (mDialogItems[which].equals(getString(R.string.reply))) {
                setQuickReplyUser();
            } else {
                Log.d(TAG, "Dialog click listener, no such case");
            }
        }
    };

    private static final String TO_USERS_LINK = "<a href='borqs://profile/details?uid=%1$s'>%2$s</a>";
    private void setQuickReplyUser() {
        mReferredCommentId = String.valueOf(mComment.id);
        String insertToUser = String.format(TO_USERS_LINK, mComment.uid, "+" + mComment.username + " ");
        String tmpText = ConversationMultiAutoCompleteTextView.getConversationText(mCommentText);
        if (TextUtils.isEmpty(tmpText) == false) {
            insertToUser = tmpText + " " + insertToUser;
        }
        mCommentText.setText(Html.fromHtml(insertToUser));
        mCommentText.requestFocus();
        mCommentText.setSelection(mCommentText.getText().length());
    }

    private void seeFavourList() {
        if (null != mComment && mComment.likerList.count > 0) {
            IntentUtil.ShowUserList(mActivity, getString(R.string.dialog_like_user_title), 
                    mComment.likerList, DialogUtils.CommentLike, String.valueOf(mComment.id));
        }
    }

    private void deletetComment() {
        DialogUtils.showConfirmDialog(mActivity, R.string.delete_comment_title,
                R.string.delete_comment_message, R.string.label_ok, R.string.label_cancel, 
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteComments(mComment);
            }
        });
    }

    public void deleteComments(Stream_Post comment) {
        Message msg = mHandler.obtainMessage(COMMENTS_REMOVE);
        msg.getData().putLong("comment_id", comment.id);
        msg.sendToTarget();
    }

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
//                    onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);

                    break;
                }
            }
        } else {
            Log.d(TAG, "onTargetLikeCreated() targetType is : " + targetType);
        }
    }

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
//                    onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);

                    break;
                }
            }
        } else {
            Log.d(TAG, "onTargetLikeRemoved() targetType is : " + targetType);
        }
    }

    private void refreshCommentLikeUi(final long cId, final Stream_Post comment) {
        mHandler.post(new Runnable() {
            public void run() {
                final int count = mListView.getCount();
                for (int j = 0; j < count; j++) {
                    View v = mListView.getChildAt(j);
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
        mAsyncQiupu.deleteComments(AccountServiceUtils.getSessionID(), QiupuConfig.TYPE_POLL, commentId, new TwitterAdapter() {
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
//                    onCommentsUpdated(post_id, mDisplayCommentList, mTotalCommentCount);
                }
                synchronized (mremoveCommentLock) {
                    inremovecomment = false;
                }

                Message msg = mHandler.obtainMessage(COMMENTS_REMOVE_END);
                msg.getData().putBoolean("RESULT", suc);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                synchronized (mremoveCommentLock) {
                    inremovecomment = false;
                }
                if (mActivity instanceof PollDetailFragmentCallBack) {
                    mCallBackListener.showExceptionToast(ex);
                }
                Message msg = mHandler.obtainMessage(COMMENTS_REMOVE_END);
                msg.getData().putBoolean("RESULT", false);
                msg.sendToTarget();
            }
        });
    }

    @Override
    public int getCaptionResourceId() {
        if(inProcess) {
            return R.string.loading;
        }
        return R.string.list_view_more;
    }

    @Override
    public OnClickListener loaderMoreClickListener() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.obtainMessage(COMMENTS_GET).sendToTarget();
            }
        };
        return clickListener;
    }

    private boolean addPollItem;
    private Object  mLockAddPollItem = new Object();

    public void addPollItems(final String poll_id, final String item_ids, final ArrayList<String> msgList) {
        if (!ToastUtil.testValidConnectivity(mActivity)) {
            Log.i(TAG, "addPollItems(), ignore while no connection.");
            return;
        }

        if (addPollItem == true) {
            ToastUtil.showShortToast(mActivity, mHandler, R.string.string_in_processing);
            return;
        }

        synchronized (mLockAddPollItem) {
            addPollItem = true;
        }

        begin();

        mAsyncQiupu.addPollItems(AccountServiceUtils.getSessionID(), poll_id, item_ids, msgList,
                new TwitterAdapter() {
                    public void addPollItems(PollInfo pollInfo) {
                        Log.d(TAG, "finish addPollItems=" + pollInfo);

                        if (pollInfo != null) {
                            mPollInfo = pollInfo;
                        } else {
                            Log.d(TAG, "server return data error ");
//                            return;
                        }

                        Message msg = mHandler.obtainMessage(ADD_POLL_ITEM_END);
                        msg.getData().putBoolean("RESULT", true);
                        msg.sendToTarget();
                        synchronized (mLockAddPollItem) {
                            addPollItem = false;
                        }
                    }

                    public void onException(TwitterException ex,
                            TwitterMethod method) {
                        synchronized (mLockAddPollItem) {
                            addPollItem = false;
                        }
                        Message msg = mHandler.obtainMessage(ADD_POLL_ITEM_END);
                        msg.getData().putBoolean("RESULT", false);
                        msg.sendToTarget();
                    }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_insert_smiley).setVisible(mCommentText != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_insert_smiley == item.getItemId()) {
            showSmileyDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog mSmileyDialog;
    private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            mSmileyDialog =  DialogUtils.showSmileyDialog(getActivity(), mCommentText);
        } else {
            mSmileyDialog.show();
        }
    }

    @Override
    public void addPollItems(ArrayList<String> msgList) {
        Message msg = mHandler.obtainMessage(ADD_POLL_ITEM);
        msg.getData().putStringArrayList(ADD_POLL_ITEM_KEY, msgList);
        msg.sendToTarget();
    }

    @Override
    public int getSelectedItemCount() {
        getNewSelectIds();
        return mNewSelectItemsList.size();
    }

}
