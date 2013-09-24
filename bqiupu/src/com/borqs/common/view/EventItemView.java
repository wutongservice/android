package com.borqs.common.view;

import twitter4j.UserCircle;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;

public class EventItemView extends SNSItemView {

    private static final String TAG = "EventItemView";
    private UserCircle mCircle;
    private TextView mTime;
    private TextView mTilte;
    private TextView mAddress;
    private ImageView mCover;
    private ImageView mCreatorIcon;
    
    public EventItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public EventItemView(Context context, UserCircle circle) {
        super(context);

        mCircle = circle;
        init();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {

        LayoutInflater factory = LayoutInflater.from(mContext);
        removeAllViews();

        //child 1
        View convertView = factory.inflate(R.layout.event_item_view, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addView(convertView);
        mCover = (ImageView) convertView.findViewById(R.id.cover);
        mTime = (TextView) convertView.findViewById(R.id.id_time);
        mTilte = (TextView) convertView.findViewById(R.id.id_event_title);
        mAddress = (TextView) convertView.findViewById(R.id.id_addess);
        mCreatorIcon = (ImageView) convertView.findViewById(R.id.creator_icon);
        mTilte.setSelected(true);
        setUI();
    }

    public void setCircle(UserCircle user) {
        mCircle = user;
        setUI();
    }

    private void setUI() {
    	if (mCircle != null) {
    		mTilte.setText(mCircle.name);
    		
    		if(StringUtil.isValidString(mCircle.location)) {
    			mAddress.setVisibility(View.VISIBLE);
    			mAddress.setText(mCircle.location);
    		}else {
    			mAddress.setVisibility(View.INVISIBLE);
    		}
    		
    		if(mCircle.mGroup != null) {
    			if(mCircle.circleid == 14000000000L) {
    				mCover.setImageResource(R.drawable.borqs_cover);
    			}else if(StringUtil.isEmpty(mCircle.mGroup.coverUrl)) {
    				mCover.setImageResource(R.drawable.event_default_cover);
    			}else {
    				setViewIcon(mCircle.mGroup.coverUrl, mCover);
    			}
    			StringBuilder timeStr = new StringBuilder();
    			timeStr.append(DateUtil.converToRelativeTime(mContext,
    					mCircle.mGroup.startTime));
    			if(mCircle.mGroup.endTime > 0) {
    				timeStr.append(" ~ ");
    				timeStr.append(DateUtil.converToRelativeTime(mContext,
    	    					mCircle.mGroup.endTime));
    			}
    			mTime.setText(timeStr.toString());
    			
    			if(mCircle.mGroup.creator != null) {
    				setViewIcon(mCircle.mGroup.creator.profile_image_url, mCreatorIcon);		
    			}else {
    				mCreatorIcon.setImageResource(R.drawable.default_public_circle);		
    			}
    		}
    	}
    }
    
    private void setViewIcon(final String url, final ImageView view) {
    	if(StringUtil.isValidString(url)) {
    		final Resources resources = mContext.getResources();
    		
    		ImageRun imagerun = new ImageRun(null, url, 0);
    		imagerun.width = resources.getDisplayMetrics().widthPixels;
    		imagerun.height = imagerun.width;
    		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
    		imagerun.noimage = true;
    		imagerun.addHostAndPath = true;
    		imagerun.setRoundAngle=true;
//        imagerun.need_scale = true;
    		imagerun.setImageView(view);
    		imagerun.post(null);
    	}else {
    		view.setImageResource(R.drawable.default_public_circle);
    	}
    }
    
    public UserCircle getItemView(){
    	return mCircle;
    }
    
    public long getDataItemId(){
        return mCircle.circleid;
    }
}

