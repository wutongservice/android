package com.borqs.common.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import twitter4j.PollItemInfo;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.borqs.common.util.DialogUtils;
import com.borqs.qiupu.R;
import com.borqs.qiupu.ui.bpc.FriendsListActivity;
import com.borqs.qiupu.util.ToastUtil;

public class PollChildItemView extends SNSItemView {

    private static final String TAG = "PollChildItemView";

    private PollItemInfo        mPollItemInfo;
    private static final int MODE_REVERT = 2;

    private CheckBox            mCheckBox;
    private TextView            mTitle;
    private TextView            mVoteCount;
    private boolean mCanVote = false;
    private boolean mHasVoted ;
    private int mMultiSize = 0;
    private Context mContext;
    private long mNotStart;
    private int mMode = -1;
    private HashMap<String, PollItemCheckActionListener> mCheckClickListenerMap;

    public PollChildItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public PollChildItemView(Context context, PollItemInfo pollItemInfo, boolean canVote, int multi, 
            boolean hasVoted, long notStart, int mode) {
        super(context);
        mContext = context;
        mPollItemInfo = pollItemInfo;
        mCanVote = canVote;
        mMultiSize = multi;
        mHasVoted = hasVoted;
        mNotStart = notStart;
        mMode = mode;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        removeAllViews();

        // child 1
        View convertView = LayoutInflater.from(mContext).inflate(
                R.layout.poll_child_item_view, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        		(int) mContext.getResources().getDimension(R.dimen.circle_list_item_height)));
        addView(convertView);

        mTitle = (TextView) convertView.findViewById(R.id.poll_item_title);
        mCheckBox = (CheckBox) convertView.findViewById(R.id.poll_item_checkbox);
        mVoteCount = (TextView) convertView.findViewById(R.id.poll_count);
        mCheckBox.setOnClickListener(checkListener);

        setUI();
    }

    private View.OnClickListener checkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	setCheckStatus();
        }
    };

    public void setPollItemInfo(PollItemInfo pollItemInfo, boolean canVote, int multi, boolean hasVoted,
            int notStart, int mode) {
        mPollItemInfo = pollItemInfo;
        mCanVote = canVote;
        mMultiSize = multi;
        mHasVoted = hasVoted;
        mNotStart = notStart;
        mMode = mode;
        setUI();
    }

    private View.OnClickListener pollUserListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPollItemInfo.userList.size() > 0) {
                FriendsListActivity.showUserList(mContext, mContext.getString(R.string.attend_friends_title), mPollItemInfo.userList);
            } else {
                ToastUtil.showCustomToast(mContext, R.string.no_friend_vote);
            }
        }
    };

    private void setUI() {
        if (mPollItemInfo != null) {
            mTitle.setText(mPollItemInfo.message);
            mCheckBox.setChecked(mPollItemInfo.selected);
            
            if(mHasVoted || (mCanVote == false && mNotStart != 0)) {
            	mVoteCount.setVisibility(View.VISIBLE);
        		mVoteCount.setText(String.valueOf(mPollItemInfo.count));
//        		mVoteCount.setOnClickListener(pollUserListener);
            }else {
            	mVoteCount.setVisibility(View.GONE);
            }

            Log.d(TAG, "mCanVote = " + mCanVote);
            if(!mCanVote) {
                if (mPollItemInfo.viewer_voted) {
                    mCheckBox.setChecked(true);
                }
                if (mMode != MODE_REVERT) {
                	mCheckBox.setEnabled(false);
                	mTitle.setEnabled(false);
                }
            }else {
            	if (mPollItemInfo.viewer_voted) {
            	    mCheckBox.setChecked(true);
            	    if (mMode != MODE_REVERT) {
                		mCheckBox.setEnabled(false);
                		mTitle.setEnabled(false);
            	    }
            	}else { 
            		mCheckBox.setEnabled(true);
            		mTitle.setEnabled(true);
            	}
            }
        }
    }

    public void swithCheck() {
        mPollItemInfo.selected = !mPollItemInfo.selected;
        mCheckBox.setChecked(mPollItemInfo.selected);
    }
    
    public void setCheckStatus() {
    	if (null != mCheckClickListenerMap) {
            Collection<PollItemCheckActionListener> listeners = mCheckClickListenerMap.values();
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
            	PollItemCheckActionListener checkListener = (PollItemCheckActionListener)it.next();
            	if(checkListener.canClick(mPollItemInfo.item_id) == false) {
            		mCheckBox.setChecked(false);
                    DialogUtils.showTipDialog(mContext, R.string.play_title, 
                            String.format(mContext.getString(R.string.restrict_msg), mMultiSize),
                            R.string.label_ok);
            	}else {
            		swithCheck();
            		checkListener.changeItemSelect(mPollItemInfo.item_id, mPollItemInfo.selected);
            	}
            }
        }
    }

    public boolean getCheckedStatus() {
        return mPollItemInfo.selected;
    }

    public PollItemInfo getPollItemInfo() {
        return mPollItemInfo;
    }

    public String getPollItemId() {
        return mPollItemInfo.item_id;
    }
    
    public void attachCheckListener(HashMap<String, PollItemCheckActionListener> listenerMap) {
        mCheckClickListenerMap = listenerMap;
    }

    
    public interface PollItemCheckActionListener {
    	public void changeItemSelect(String itemId, boolean isSelect);
    	public int getSelectCount();
    	public boolean canClick(String itemsId);
    }

}
