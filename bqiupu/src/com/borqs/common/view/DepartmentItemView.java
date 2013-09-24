package com.borqs.common.view;

import twitter4j.UserCircle;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class DepartmentItemView extends SNSItemView {

	private UserCircle mCircle;
	
	private TextView tv_name;
	private ImageView img_dep;
	private TextView mDesView;
	private boolean mIsGrid;

    public DepartmentItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public DepartmentItemView(Context context, UserCircle circle, boolean isgrid) {
        super(context);
        mCircle = circle;
        mIsGrid = isgrid;
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
        		getLayoutResourceId(), null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        		(int)getResources().getDimension(R.dimen.circle_grid_height)));
        addView(convertView);
        
        tv_name = (TextView) convertView.findViewById(R.id.tv_name);
    	img_dep =  (ImageView) convertView.findViewById(R.id.img_dep);
    	mDesView  = (TextView) convertView.findViewById(R.id.id_dep_destription);
    	
    	 setUI();
    }

    public void setDataInfo(UserCircle circle) {
    	mCircle = circle;
        setUI();
    }

    private void setUI() {
    	tv_name.setText(mCircle.name);
        if(!TextUtils.isEmpty(mCircle.profile_image_url)) {
        	setViewIcon(mCircle.profile_image_url, img_dep);
        }
        if(mDesView != null) {
        	mDesView.setText(mCircle.description);
        }
    }
    
    private void setViewIcon(final String url, final ImageView view) {
		final Resources resources = mContext.getResources();
		
		ImageRun imagerun = new ImageRun(null, url, 0);
		imagerun.width = resources.getDisplayMetrics().widthPixels;
		imagerun.height = imagerun.width;
//		imagerun.default_image_index = QiupuConfig.DEFAULT_IMAGE_INDEX_USER;
		imagerun.noimage = true;
		imagerun.addHostAndPath = true;
		imagerun.setRoundAngle=true;
		imagerun.setImageView(view);
		imagerun.post(null);
}

    public UserCircle getDataInfo() {
        return mCircle;
    }

    public long getDataId() {
        return mCircle.id;
    }
    
    protected int getLayoutResourceId() {
    	if(mIsGrid) {
    		return R.layout.department_grid_item_view;
    	}
        return R.layout.department_item_view;
    }
}
