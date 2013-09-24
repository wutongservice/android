package com.borqs.common.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.common.ProfileHeadSourceItem;
import com.borqs.qiupu.R;

public class ProfileHeadSourceView extends SNSItemView {

    private static final String TAG = "ProfileHeadSourceView";
    private ImageView mIcon;
    private TextView mLabel;
    private ProfileHeadSourceItem mItem;
    private View mContentView;

    public ProfileHeadSourceView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }
    
    

    public ProfileHeadSourceView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
	}

	public ProfileHeadSourceView(Context context, ProfileHeadSourceItem info) {
        super(context);
        mItem = info;
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
        int width = getResources().getDisplayMetrics().widthPixels;
        MarginLayoutParams params = new MarginLayoutParams(width/4, MarginLayoutParams.WRAP_CONTENT);
//        p.leftMargin = 5;
//        p.rightMargin = 5;
//        p.topMargin = 10;
//        p.bottomMargin = 10;
//        ViewGroup.LayoutParams params = new LayoutParams(p);
        addView(mContentView, params);
//        setViewLayout();
        mLabel = (TextView) mContentView.findViewById(R.id.item_tv);
        mIcon = (ImageView) mContentView.findViewById(R.id.item_icon);
//        setUI();
    }

    private void setUI() {
    	
    	if(mItem != null) {
    		mIcon.setImageResource(mItem.mIconSource);
    		mLabel.setText(mItem.mLabel);
    		mLabel.setTextColor(getResources().getColor(R.color.white));
    		mContentView.setOnClickListener(mItem.mClickListener);
    	}
    }
    
    public void setLableColor(int colorid) {
    	mLabel.setTextColor(getResources().getColor(colorid));
    }
    
    public void setItem(ProfileHeadSourceItem item) {
    	if(item != null) {
    		mItem = item;
    		setUI();
    	}
    }
    
    public ProfileHeadSourceItem getItem() {
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
//    		if(mIsCircle) {
//    			width = (int) (width - mContext.getResources().getDimension(R.dimen.default_text_margin_left) 
//    					-mContext.getResources().getDimension(R.dimen.default_text_margin_right));
//    		}
    		ViewGroup.LayoutParams params = mContentView.getLayoutParams();
    		params.width = (width/7) - 5;
    		params.height = params.width;
    		mContentView.setPadding(5, 0, 5, 0);
    		mContentView.invalidate();
    	}
    }
}

