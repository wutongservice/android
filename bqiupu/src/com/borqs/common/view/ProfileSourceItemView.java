package com.borqs.common.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.ShareSourceItem;
import com.borqs.qiupu.R;

public class ProfileSourceItemView extends SNSItemView {

    private static final String TAG = "ProfileSourceItemView";
    private TextView name;
    private ImageView mIcon;
    private ShareSourceItem mItem;
    private boolean mIsCircle = false;
    private View mContentView;

    public ProfileSourceItemView(Context context) {
        super(context);
    }

    
    
    public ProfileSourceItemView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	}



	@Override
    public String getText() {
        return null;
    }

    public ProfileSourceItemView(Context context, ShareSourceItem info, final boolean isCircleSource) {
        super(context);
        mItem = info;
        mIsCircle = isCircleSource;
        init();
        setUI();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {

        removeAllViews();
        LayoutInflater factory = LayoutInflater.from(mContext);

        //child 1
        mContentView = factory.inflate(R.layout.profile_share_source_item_view, null);
        setViewLayout();
        addView(mContentView);

        name = (TextView) mContentView.findViewById(R.id.item_tv);
        mIcon = (ImageView) mContentView.findViewById(R.id.item_icon);
//        setUI();
    }

    private void setUI() {
    	
    	if(mItem != null) {
    		try {
    			if(mItem.mId != null && mItem.mId.length() > 0){
    				mIcon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(mItem.mId));
            	}else{
            		mIcon.setImageDrawable(ShareSourceItem.getSorceItemIcon(mContext, mItem.mType));
            	}
            } catch (PackageManager.NameNotFoundException e) {
            	mIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_app_icon));
            }
    		
    		name.setText(ShareSourceItem.getSourceItemLabel(mContext, mItem.mLabel, mItem.mType));
    		if(mIsCircle) {
    			name.setTextColor(mContext.getResources().getColor(R.color.text_grey));
    		}else {
    			name.setTextColor(mContext.getResources().getColor(R.color.white));
    		}
    	}
    }
    
    public void setItem(ShareSourceItem item,boolean isCircleSource) {
    	if(item != null) {
    		mIsCircle =  isCircleSource;
    		mItem = item;
    		setViewLayout();
    		setUI();
    	}
    }
    public void setItem(ShareSourceItem item) {
    	if(item != null) {
    		mItem = item;
    		setViewLayout();
    		setUI();
    	}
    }
    
    public ShareSourceItem getItem() {
    	return mItem;
    }
    
    private void setViewLayout() {
    	if(mContentView != null) {
    		
    		int wd = 5;
    		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
    			wd = 7;
    		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    			wd = 5;
    		}
    		
    		int width = getResources().getDisplayMetrics().widthPixels;
    		if(mIsCircle) {
    			width = (int) (width - mContext.getResources().getDimension(R.dimen.default_text_margin_left) 
    					-mContext.getResources().getDimension(R.dimen.default_text_margin_right));
    		}
    		mContentView.setLayoutParams(new LayoutParams(mIsCircle ? width/wd : width/4, LayoutParams.WRAP_CONTENT));
    	}
    }
}

