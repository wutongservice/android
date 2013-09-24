package com.borqs.common.view;

import twitter4j.UserCircle;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;
import com.borqs.qiupu.ui.circle.EventDetailActivity;
import com.borqs.qiupu.util.CircleUtils;
import com.borqs.qiupu.util.DateUtil;
import com.borqs.qiupu.util.StringUtil;

public class SimpleEventItemView extends SNSItemView implements OnClickListener{

    private static final String TAG = "SimpleEventItemView";
    private UserCircle mCircle;
    private TextView mTime;
    private TextView mTilte;
    private TextView mAddress;
    private ImageView mCreatorIcon;
    
    public SimpleEventItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public SimpleEventItemView(Context context, UserCircle circle) {
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
        View convertView = factory.inflate(R.layout.simple_event_item_view, null);
        convertView.setOnClickListener(this);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addView(convertView);
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

	@Override
	public void onClick(View v) {
		if(mCircle != null) {
        	final Intent intent = new Intent(mContext, EventDetailActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString(CircleUtils.CIRCLE_NAME,
                    CircleUtils.getLocalCircleName(mContext, mCircle.circleid, mCircle.name));
            bundle.putLong(CircleUtils.CIRCLE_ID, mCircle.circleid);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }else {
            Log.d(TAG, "get circle is null.");
        }
	}
}

