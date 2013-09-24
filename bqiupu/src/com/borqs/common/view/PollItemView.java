package com.borqs.common.view;

import twitter4j.PollInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.ui.bpc.PollDetailActivity;

public class PollItemView extends SNSItemView {

    private static final String TAG = "PollItemView";

    private PollInfo            mPollInfo;

    private TextView          mTitle;
    private TextView           mPollStatus;
//    private TextView       mMyStatus;
    private TextView       mAttendCount;
//    private ImageView mIcon;
    private boolean mFromCircle;

    public PollItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public PollItemView(Context context, PollInfo pollInfo) {
        super(context);

        mPollInfo = pollInfo;
        init();
    }
    
    public PollItemView(Context context, PollInfo pollInfo, boolean fromCircle) {
        super(context);
        mFromCircle = fromCircle;
        mPollInfo = pollInfo;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        removeAllViews();

        View convertView = null;
        // child 1

//        mMyStatus = (TextView) convertView.findViewById(R.id.my_status);
        if(mFromCircle) {
        	
        	convertView = LayoutInflater.from(mContext).inflate(
        			R.layout.poll_item_circle, null);
        	convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        			(int)mContext.getResources().getDimension(R.dimen.circle_poll_item_height)));
        }else {
        	convertView = LayoutInflater.from(mContext).inflate(
        			R.layout.poll_item_view, null);
        	convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        			(int)mContext.getResources().getDimension(R.dimen.poll_item_height)));
        	mPollStatus = (TextView) convertView.findViewById(R.id.poll_status);
        	mAttendCount = (TextView) convertView.findViewById(R.id.attend_count);
        }
        addView(convertView);
        mTitle = (TextView) convertView.findViewById(R.id.poll_title);
//        mIcon = (ImageView) convertView.findViewById(R.id.profile_img_ui);
        setUI();
    }

    public void setPollInfo(PollInfo pollInfo) {
        mPollInfo = pollInfo;
        setUI();
    }

    private void setUI() {
        if (mPollInfo != null) {
            mTitle.setText(mPollInfo.title);
            if(mFromCircle) {
//            	Log.d(TAG, "is from circle, only show simple poll item.");
//            	mAttendCount.setVisibility(View.GONE);
//            	mPollStatus.setVisibility(View.GONE);
            	this.setBackgroundResource(R.drawable.list_selector_background);
            	this.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
				    	intent.setClass(mContext, PollDetailActivity.class);
				    	intent.putExtra(PollDetailActivity.POLL_ID_KEY, mPollInfo);
				    	mContext.startActivity(intent);
					}
				});
            	return;
            }else {
            	
            	mAttendCount.setText(String.valueOf(mPollInfo.attend_count));
            	
            	if (mPollInfo.has_voted) {
//                mMyStatus.setText(R.string.voted_poll);
            	} else {
//                mMyStatus.setText(R.string.not_voted_poll);
            	}
            	if(mPollInfo.sponsor != null) {
//            	Log.d(TAG, "!!!!!!!!!!!!! "  + mPollInfo.sponsor.profile_image_url);
//            	setViewIcon(mPollInfo.sponsor.profile_image_url, mIcon);
            	}
            	mAttendCount.setText(String.format(mContext.getString(R.string.attend_count), mPollInfo.attend_count));
            	
            	if (mPollInfo.attend_status == 0) {
            		mPollStatus.setBackgroundResource(R.drawable.poll_black_bg);
            		mPollStatus.setText(R.string.poll_incoming_title);
//                mPollStatus.setImageResource(R.drawable.btn_login_nor);
            	} else if (mPollInfo.attend_status == 1) {
            		mPollStatus.setBackgroundResource(R.drawable.poll_red_bg);
            		mPollStatus.setText(R.string.poll_going_title);
//                mPollStatus.setImageResource(R.drawable.btn_login_nor);
            	} else if (mPollInfo.attend_status == 2) {
            		mPollStatus.setBackgroundResource(R.drawable.poll_bule_bg);
            		mPollStatus.setText(R.string.poll_ending_title);
//                mPollStatus.setImageResource(R.drawable.btn_login_nor);
            	}
            }
            
            
        }
    }
    
    private void setViewIcon(final String url, final ImageView view) {
		final Resources resources = mContext.getResources();
		
		ImageRun imagerun = new ImageRun(null, url, 0);
		imagerun.width = resources.getDisplayMetrics().widthPixels;
		imagerun.height = imagerun.width;
		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.noimage = true;
		imagerun.addHostAndPath = true;
		imagerun.setRoundAngle=true;
		imagerun.setImageView(view);
		imagerun.post(null);
}

    public PollInfo getPollInfo() {
        return mPollInfo;
    }

    public String getPollId() {
        return mPollInfo.poll_id;
    }
}
