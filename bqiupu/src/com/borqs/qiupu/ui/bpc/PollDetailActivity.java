package com.borqs.qiupu.ui.bpc;

import java.util.ArrayList;

import twitter4j.PollInfo;
import twitter4j.Stream.Comments.Stream_Post;
import twitter4j.TwitterException;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;

import com.borqs.account.service.AccountServiceUtils;
import com.borqs.common.SelectionItem;
import com.borqs.common.api.BpcApiUtils;
import com.borqs.common.listener.TargetLikeActionListener;
import com.borqs.common.util.DialogUtils;
import com.borqs.common.view.CorpusSelectionItemView;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.QiupuHelper;
import com.borqs.qiupu.fragment.PollDetailFragment;
import com.borqs.qiupu.fragment.PollDetailFragment.PollDetailFragmentCallBack;
import com.borqs.qiupu.ui.BasicActivity;

public class PollDetailActivity extends BasicActivity implements PollDetailFragmentCallBack, TargetLikeActionListener {

    private final static String TAG         = "PollDetailActivity";

    public static String        POLL_ID_KEY = "POLL_ID_KEY";
    private PollDetailFragment mPollDetailFragment;
    private String mPollId;
    private PollInfo mPollInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poll_detail_activity);
        setHeadTitle(R.string.poll_title);

        mPollId = parseIntent(getIntent());

        showLeftActionBtn(true);
        showRightActionBtn(false);

        if (mPollInfo == null) {
//            showMiddleActionBtn(true);
//            overrideMiddleActionBtn(R.drawable.actionbar_icon_commit_normal, mVoteListener);
        } else {
            if (mPollInfo.attend_status == PollDetailFragment.ENDING || 
                    mPollInfo.attend_status == PollDetailFragment.INCOMING || mPollInfo.viewer_can_vote == false) {
                showMiddleActionBtn(false);
            } else {
//                showMiddleActionBtn(true);
//                overrideMiddleActionBtn(R.drawable.actionbar_icon_commit_normal, mVoteListener);
            }
        }

        mPollDetailFragment = new PollDetailFragment(mPollInfo);
        mPollDetailFragment.setHasOptionsMenu(true);

        getSupportFragmentManager().beginTransaction().add(R.id.poll_container,mPollDetailFragment).commit();

        QiupuHelper.registerTargetLikeListener(getClass().getName(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QiupuHelper.unRegisterTargetLikeListener(getClass().getName());
    };

    private View.OnClickListener mVoteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vote();
        }
    };

    private String parseIntent(Intent intent) {
        String poll_id = "";
        String url = getIntentURL(intent);
        Log.d(TAG, "parseIntent() url = " + url);
        if (TextUtils.isEmpty(url)) {
            mPollInfo = (PollInfo) getIntent().getSerializableExtra(POLL_ID_KEY);
            if (mPollInfo != null) {
                poll_id = mPollInfo.poll_id;
            } else {
                Log.d(TAG, "parseIntent() mPollInfo is null");
            }
        } else {
            poll_id = BpcApiUtils.parseSchemeValue(intent, BpcApiUtils.SEARCH_KEY_ID);
        }
        return poll_id;
    }

    public boolean ensureAccountLogin() {
        if (!AccountServiceUtils.isAccountReady()) {
            gotoLogin();
            return false;
        }

        return true;
    }

    private void vote() {
        if (ensureAccountLogin()) {
            mPollDetailFragment.vote();
        }
    }

    @Override
    protected void createHandler() {

    }

    @Override
    protected void loadRefresh() {
        mPollDetailFragment.refreshPollUI();
    }

	@Override
	public void getPollDetailFragment(PollDetailFragment fragment) {
		mPollDetailFragment = fragment;
	}

	@Override
	public String getPollId() {
		return mPollId;
	}

    @Override
    public void showDeleteButton(boolean isOwner, boolean canVote, OnClickListener listener) {
        if (isOwner) {
            Log.d(TAG, "canVote = " + canVote);
            if (canVote) {
                showLeftActionBtn(false);
                showMiddleActionBtn(true);
                showRightActionBtn(true);
                overrideMiddleActionBtn(R.drawable.actionbar_icon_commit_normal, mVoteListener);
                overrideRightActionBtn(R.drawable.ic_menu_moreoverflow, moreActionListener);
            } else {
                showLeftActionBtn(true);
                showMiddleActionBtn(false);
                showRightActionBtn(true);
                overrideRightActionBtn(R.drawable.icon_delete_normal, listener);
            }
        } else {
            if (canVote) {
                showLeftActionBtn(true);
                showMiddleActionBtn(true);
                showRightActionBtn(false);
                overrideMiddleActionBtn(R.drawable.actionbar_icon_commit_normal, mVoteListener);
            } else {
                showLeftActionBtn(true);
                showMiddleActionBtn(false);
                showRightActionBtn(false);
            }
        }
    }

    private View.OnClickListener moreActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<SelectionItem> items = new ArrayList<SelectionItem>();
            items.add(new SelectionItem("", getString(R.string.label_refresh)));
            items.add(new SelectionItem("", getString(R.string.compose_delete)));
            showCorpusSelectionDialog(items);
        }
    };

    protected void showCorpusSelectionDialog(ArrayList<SelectionItem> items) {
        if(mMiddleActionBtn != null) {
            int location[] = new int[2];
            mMiddleActionBtn.getLocationInWindow(location);
            int x = location[0];
            int y = getResources().getDimensionPixelSize(R.dimen.title_bar_height);
            DialogUtils.showCorpusSelectionDialog(this, x, y, items, itemClickListener);
        }
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (CorpusSelectionItemView.class.isInstance(view)) {
                CorpusSelectionItemView item = (CorpusSelectionItemView) view;
                onCorpusSelected(item.getText());
            } else {
                Log.d(TAG, "onItemClick() the view is not CorpusSelectionItemView, view is : " + view);
            }
            
        }
    };

    private void onCorpusSelected(String value) {
        if (getString(R.string.label_refresh).equals(value)) {
            loadRefresh();
        } else if (getString(R.string.compose_delete).equals(value)) {
            if (mPollDetailFragment != null) {
                mPollDetailFragment.showDeleteConfirmDialog();
            }
        } else {
            Log.d(TAG, "onCorpusSelected() no such value, value = " + value);
        }
    }

    @Override
    public void showExceptionToast(TwitterException ex) {
        preHandleTwitterException(ex);
    }

    @Override
    public void showCustomToastInPoll(int resString) {
        showCustomToast(resString);
    }

    @Override
    public boolean ensureLogin() {
        return ensureAccountLogin();
    }

    @Override
    public void pollLikeComment(Stream_Post comment) {
        likeComment(comment);
    }

    @Override
    public void pollUnLikeComment(Stream_Post comment) {
        unLikeComment(comment);
    }

    @Override
    public void onTargetLikeCreated(String targetId, String targetType) {
        mPollDetailFragment.onTargetLikeCreated(targetId, targetType);
    }

    @Override
    public void onTargetLikeRemoved(String targetId, String targetType) {
        mPollDetailFragment.onTargetLikeRemoved(targetId, targetType);
    }

    @Override
    public void showActionPollButton(boolean show) {
        showMiddleActionBtn(show);
        overrideMiddleActionBtn(R.drawable.actionbar_icon_commit_normal, mVoteListener);
    }

    @Override
    public void showRefreshButton(boolean show) {
        showProgressBtn(false);
        showLeftActionBtn(show);
    }

}