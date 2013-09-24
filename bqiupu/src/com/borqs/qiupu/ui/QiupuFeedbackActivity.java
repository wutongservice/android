package com.borqs.qiupu.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import twitter4j.Stream;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;

public class QiupuFeedbackActivity extends BasicActivity {
    private final static String TAG = "qiupu.QiupuFeedbackActivity";
    private EditText mFeedbackContent;
    private static final int QIUPU_POST_ID = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qiupu_feedback);
        setHeadTitle(R.string.feedback_qiupu);

        enableLeftActionBtn(false);
        enableMiddleActionBtn(false);
        overrideRightActionBtn(R.drawable.actionbar_post, feedbackCommitLister);

        mFeedbackContent = (EditText) findViewById(R.id.feedback_content);
        
//        View commitActionView = findViewById(R.id.share_title_commit);
//        if (null != commitActionView) {
//            commitActionView.setOnClickListener(feedbackCommitLister);
//        }

        ensureAccountLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    View.OnClickListener feedbackCommitLister = new OnClickListener() {
        public void onClick(View arg0) {
            tryCommitFeedback();
        }
    };

    private static final int FEEDBACK_COMMIT = 101;
    private static final int FEEDBACK_COMMIT_END = 102;

    private class MainHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FEEDBACK_COMMIT: {
                    postfeedback();
                    break;
                }
                case FEEDBACK_COMMIT_END: {
                    dismissDialog(DIALOG_FEEDBACK);
                    boolean suc = msg.getData().getBoolean(RESULT);
                    if (suc) {
                        showCustomToast(R.string.feedback_submission_successful);
                        QiupuFeedbackActivity.this.finish();
                    } else {
                        showCustomToast(R.string.feedback_submission_failed);
                    }
                    break;
                }
            }
        }
    }

    private void postfeedback() {
        showDialog(DIALOG_FEEDBACK);

        if (inProcess == true) {
            if (QiupuConfig.LOGD) Log.d(TAG, "in postfeedback");
            return;
        }

        synchronized (mLock) {
            inProcess = true;
        }

        if (QiupuConfig.LOGD) Log.d(TAG, "postfeedback");

        asyncQiupu.postFeedbackAsync(getSavedTicket(),
                mFeedbackContent.getText().toString(), "", new TwitterAdapter() {
            public void postToWall(Stream post) {
                Log.d(TAG, "finish postFeedbackAsync=" + post);

                Message msg = mHandler.obtainMessage(FEEDBACK_COMMIT_END);
                msg.getData().putBoolean(RESULT, true);
                msg.sendToTarget();
            }

            public void onException(TwitterException ex, TwitterMethod method) {
                Message msg = mHandler.obtainMessage(FEEDBACK_COMMIT_END);
                msg.getData().putString(ERROR_MSG, ex.getMessage());
                msg.getData().putBoolean(RESULT, false);
                msg.sendToTarget();
            }
        });
    }

    @Override
    protected void createHandler() {
        mHandler = new MainHandler();
    }

    private void tryCommitFeedback() {
        if (ensureAccountLogin()) {
            if (!TextUtils.isEmpty(mFeedbackContent.getEditableText())) {
                mHandler.obtainMessage(FEEDBACK_COMMIT).sendToTarget();
            }
        }
    }


    @Override
    protected void onAccountLoginCancelled() {
        finish();
    }
}
