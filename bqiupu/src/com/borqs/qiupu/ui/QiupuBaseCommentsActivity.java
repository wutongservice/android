package com.borqs.qiupu.ui;

import java.util.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.CommentActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.util.IntentUtil;
import com.borqs.common.util.SmileyParser;
import com.borqs.common.view.CommentItemView;
import com.borqs.common.view.ConversationMultiAutoCompleteTextView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.SNSItemView;
import com.borqs.common.view.UserSelectItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.util.ToastUtil;
import twitter4j.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.qiupu.cache.QiupuHelper;

public abstract class QiupuBaseCommentsActivity extends BasicActivity implements CommentActionListener {

    private static final String TAG = "Qiupu.QiupuBaseCommentsActivity";

    private MainHandler mParentHandler;
    private static final int POST_UPDATE_ACTION_END = 0;
    protected static final String SAME_PHOTO_COUNT = "SAME_PHOTO_COUNT";
    protected static final String DIFF_PHOTO_COUNT = "DIFF_PHOTO_COUNT";

    protected ConversationMultiAutoCompleteTextView mCommentText;
    protected String mReferredCommentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mReferredCommentId = null;
        super.onCreate(savedInstanceState);
        mParentHandler = new MainHandler();
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case POST_UPDATE_ACTION_END:
                    end();
                    boolean result = msg.getData().getBoolean(RESULT, false);
                    if (result) {
                        String post_id = msg.getData().getString("post_id");
                        boolean canComment = msg.getData().getBoolean("canComment");
                        boolean canLike = msg.getData().getBoolean("canLike");
                        boolean canReshare = msg.getData().getBoolean("canReshare");

                        QiupuHelper.updateStreamCommentStatus(post_id, canComment, canLike, canReshare);
                        updateCommentActivityUI(post_id, canComment, canLike, canReshare);
                    } else {
                        String ErrorMsg = msg.getData().getString(ERROR_MSG);
                        if (!TextUtils.isEmpty(ErrorMsg)) {
                            showOperationFailToast(ErrorMsg, true);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    protected void updateCommentActivityUI(String post_id, boolean canComment, boolean canLike, boolean canReshare) {
        
    }

    protected void updateStreamSetting(final String postId, final boolean canComment, final boolean canLike, final boolean canReshare) {

        begin();
        asyncQiupu.updateStreamSetting(getSavedTicket(), postId, canComment, canLike, canReshare, new TwitterAdapter(){
            @Override
            public void postUpdateSetting(boolean result) {
                Log.d(TAG, "postUpdateSetting() result = " + result);

                Message msg = mParentHandler.obtainMessage(POST_UPDATE_ACTION_END);
                msg.getData().putBoolean(RESULT, true);
                msg.getData().putString("post_id", postId);
                msg.getData().putBoolean("canComment", canComment);
                msg.getData().putBoolean("canLike", canLike);
                msg.getData().putBoolean("canReshare", canReshare);
                msg.sendToTarget();
            }

            @Override
            public void onException(TwitterException ex, TwitterMethod method) {
                super.onException(ex, method);

                Message msg = mParentHandler.obtainMessage(POST_UPDATE_ACTION_END);
                msg.getData().putBoolean(RESULT, false);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.sendToTarget();
            }
        });
    }


    @Override
    protected void onAccountLoginCancelled() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comment_option_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        List<QiupuSimpleUser> userList = getLikerList();
        if (null == userList || userList.isEmpty()) {
            menu.findItem(R.id.menu_view_liker).setVisible(false);
        } else {
            menu.findItem(R.id.menu_view_liker).setVisible(true);
        }

        if (isStreamOwner()) {
        	menu.findItem(R.id.menu_report_abuse).setVisible(false);
        	menu.findItem(R.id.menu_remove_post).setVisible(true);
            menu.findItem(R.id.menu_mute_post).setVisible(false);
        } else {
        	menu.findItem(R.id.menu_report_abuse).setVisible(true);
        	menu.findItem(R.id.menu_remove_post).setVisible(false);
            menu.findItem(R.id.menu_mute_post).setVisible(true);
        }

        menu.findItem(R.id.menu_refresh).setVisible(true);
        menu.findItem(R.id.menu_insert_smiley).setVisible(mCommentText != null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_mute_post) {
        	mutePost();
//            DialogUtils.showConfirmDialog(this,
//                    R.string.menu_mute_post, R.string.post_mute_question,
//                    R.string.label_ok, R.string.label_cancel,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            mutePost();
//                        }
//                    });
        } else if (i == R.id.menu_report_abuse) {
            DialogUtils.showConfirmDialog(this,
                    R.string.menu_report_abuse, R.string.post_report_question,
                    R.string.label_ok, R.string.label_cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reportAbuse();
                        }
                    });
        } else if (i == R.id.menu_remove_post) {
            removeStream();
        } else if (i == R.id.menu_view_liker) {
            viewLikePeople();
        } else if (i == R.id.menu_insert_smiley) {
            showSmileyDialog();
        } else {
            Log.e(TAG, "onOptionsItemSelected, pass unknown item to parent:" + item);

        }

        return super.onOptionsItemSelected(item);
    }


    protected void mutePost() {
        final int STREAM_MUTE_TYPE = 2; // 1 - user, 3 - comments.
        final String objectId = getObjectId();
        //no need show progress dialog;
//        final Dialog dlg = DialogUtils.showProgressDialog(this, R.string.menu_mute_post,
//                getString(R.string.status_update_summary));
        asyncQiupu.muteObject(getSavedTicket(), objectId, STREAM_MUTE_TYPE, new TwitterAdapter() {
            @Override
            public void muteObject(boolean result) {
                Log.d(TAG, "mutePost, get result: " + result);
                if (result) {
//                    updateStreamRemovedUI(objectId);
                }
//                if (dlg.isShowing()) {
//                    dlg.dismiss();
//                }
//                ToastUtil.showShortToast(QiupuBaseCommentsActivity.this, mHandler,
//                        result ? R.string.mute_object_suc : R.string.mute_object_failed);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "reportAbuse exception:" + ex.getMessage());
                preHandleTwitterException(ex);
            }
        });
    }

    protected void reportAbuse() {
        final String objectId = getObjectId();
        final Dialog dlg = DialogUtils.showProgressDialog(this, R.string.menu_report_abuse,
                getString(R.string.status_update_summary));
        asyncQiupu.reportAbusedObject(getSavedTicket(), objectId, new TwitterAdapter() {
            @Override
            public void reportAbuse(boolean result) {
                Log.d(TAG, "reportAbuse, get result: " + result);
                if (result) {
                    QiupuHelper.updateStreamRemovedUI(objectId);
                }
                if (dlg.isShowing()) {
                    dlg.dismiss();
                }
                ToastUtil.showShortToast(QiupuBaseCommentsActivity.this, mHandler,
                        result ? R.string.report_abuse_suc : R.string.report_abuse_failed);
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Log.d(TAG, "reportAbuse exception:" + ex.getMessage());
                preHandleTwitterException(ex);
            }
        });
    }

    protected void viewLikePeople() {
        DialogUtils.ShowDialogUserListDialog(this,
                R.string.dialog_like_user_title, getLikerList(),
                UserSelectItemView.userClickListener);
    }

    abstract protected String getObjectId();
    abstract protected boolean isStreamOwner();
    protected List<QiupuSimpleUser> getLikerList() {
        return null;
    }

    private AlertDialog mSmileyDialog;
    private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            mSmileyDialog =  DialogUtils.showSmileyDialog(this, mCommentText);
        } else {
            mSmileyDialog.show();
        }
    }

    @Override
    protected void setContextMenuItemVisibility(ContextMenu menu, View targetView) {
        if (SNSItemView.class.isInstance(targetView)) {
            menu.findItem(R.id.bpc_item_copy).setVisible(true);
        }
    }

    @Override
    protected boolean handleSelectedContextItem(int i1, View targetView) {
        if (R.id.bpc_item_copy == i1) {
            SNSItemView.copyItem(targetView);
            return true;
        }
        return super.handleSelectedContextItem(i1, targetView);
    }

    protected void doDeletePostCallBack() {
    }

    private void removeStream() {
        //            showDialog(DIALOG_DELETE_POST);
        DialogUtils.showConfirmDialog(this, R.string.delete_post_title,
                R.string.post_delete_question,
                R.string.label_ok, R.string.label_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        doDeletePostCallBack();
                    }
                });
    }


    private Stream.Comments.Stream_Post mComment;
    private String[] mDialogItems;
    @Override
    public void commentItemListener(Stream.Comments.Stream_Post comment) {
        mComment = comment;
        showCommentDialog();
    }

    private void showCommentDialog() {
        mDialogItems = buildDialogItemContent();
        String dialogTitle = getString(R.string.context_menu_comment_title, mComment.username);

        DialogUtils.showItemsDialog(this, dialogTitle, R.drawable.home_screen_loop_icon_default,
                mDialogItems, mChooseItemClickListener);
    }

    private String[] buildDialogItemContent() {
        ArrayList<String> commentList = new ArrayList<String>();
        commentList.add(getString(R.string.reply));

        if (null != mComment) {
            if (!TextUtils.isEmpty(mComment.message)) {
                commentList.add(getString(R.string.item_copy));
            }

            if (BpcApiUtils.isActivityReadyForAction(this, IntentUtil.WUTONG_ACTION_TAGS)) {
                commentList.add(getString(R.string.item_tags));
            }

            if (mComment.isOwnBy((getSaveUid())) || isStreamOwner()) {
                commentList.add(getString(R.string.delete_comment));
            }

            if (mComment.likerList.count > 0) {
                commentList.add(getString(R.string.favour_people));
            }

            if (mComment.iLike) {
                commentList.add(getString(R.string.cancel_favour));
            } else {
                commentList.add(getString(R.string.favour));
            }
        }

        String[] dialogItems = new String[commentList.size()];
        return commentList.toArray(dialogItems);
    }

    private DialogInterface.OnClickListener mChooseItemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String item = mDialogItems[which];
            if (item.equals(getString(R.string.favour))) {
                likeComment(mComment);
            } else if (item.equals(getString(R.string.cancel_favour))) {
                unLikeComment(mComment);
            } else if (item.equals(getString(R.string.favour_people))) {
                seeFavourList();
            } else if (item.equals(getString(R.string.delete_comment))) {
                deletetComment();
            } else if (item.equals(getString(R.string.reply))) {
                setQuickReplyUser();
            } else if (item.equals(getString(R.string.item_copy))) {
                SNSItemView.copyItemText(getApplicationContext(), mComment.message);
            } else if (item.equals(getString(R.string.item_tags))) {
                SNSItemView.tagItem(QiupuBaseCommentsActivity.this, CommentItemView.getTagBundle(mComment.id));
            } else {
                Log.d(TAG, "Dialog click listener, no such case");
            }
        }
    };


    private void seeFavourList() {
        if (null != mComment && mComment.likerList.count > 0) {
            IntentUtil.ShowUserList(this, getString(R.string.dialog_like_user_title),
                    mComment.likerList, DialogUtils.CommentLike, String.valueOf(mComment.id));
        }
    }

    abstract void deleteComments(Stream.Comments.Stream_Post comment);

    private void deletetComment() {
        DialogUtils.showConfirmDialog(this, R.string.delete_comment_title,
                R.string.delete_comment_message, R.string.label_ok, R.string.label_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteComments(mComment);
                    }
                });
    }

    private void setQuickReplyUser() {
        mReferredCommentId = String.valueOf(mComment.id);

        String insertToUser = mComment.asMentionUser();
        String tmpText = ConversationMultiAutoCompleteTextView.getConversationText(mCommentText);
        if (TextUtils.isEmpty(tmpText) == false) {
            insertToUser = tmpText + " " + insertToUser;
        }
        mCommentText.setText(Html.fromHtml(insertToUser));
        mCommentText.requestFocus();
        mCommentText.setSelection(mCommentText.getText().length());
    }

    abstract void onCommentAdded(boolean result, String error);
    protected void onCommentAdded(boolean result) {
        if (result) {
            mReferredCommentId = null;
        }
        onCommentAdded(result, "");
    }

}
