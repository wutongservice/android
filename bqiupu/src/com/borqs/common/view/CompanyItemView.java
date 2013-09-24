package com.borqs.common.view;

import twitter4j.Company;
import twitter4j.PollInfo;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.borqs.qiupu.QiupuConfig;
import com.borqs.qiupu.R;
import com.borqs.qiupu.cache.ImageRun;

public class CompanyItemView extends SNSItemView {

//    private static final String TAG = "CompanyItemView";

    private Company            mCompany;

    private TextView           mPersonCount;
    private ImageView mIcon;
    
   private TextView mCompanytitle;
	private ImageView mProfile_img_ui;
	private ImageView mCover;
	private TextView tv_website;

    public CompanyItemView(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return null;
    }

    public CompanyItemView(Context context, Company company) {
        super(context);

        mCompany = company;
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

//    private void init() {
//        removeAllViews();
//
//        // child 1
//        View convertView = LayoutInflater.from(mContext).inflate(
//                R.layout.company_item_view, null);
//        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
//        		LayoutParams.WRAP_CONTENT));
//        addView(convertView);
//
//        mTitle = (TextView) convertView.findViewById(R.id.tv_company_name);
//        mPersonCount = (TextView) convertView.findViewById(R.id.tv_person_count);
//        mIcon = (ImageView) convertView.findViewById(R.id.img_company_icon);
//
//        setUI();
//    }
    
    private void init() {
    	removeAllViews();

        // child 1
        View convertView = LayoutInflater.from(mContext).inflate(
                R.layout.company_item_view, null);
        convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT));
        addView(convertView);
        
    	mCompanytitle = (TextView) convertView.findViewById(R.id.company_title);
    	mCompanytitle.setSelected(true);
    	mProfile_img_ui =  (ImageView) convertView.findViewById(R.id.creator_icon);
    	mCover = (ImageView) convertView.findViewById(R.id.select_cover);
    	tv_website = (TextView) convertView.findViewById(R.id.tv_website);
    	
    	 setUI();
    }

    public void setCompanyInfo(Company company) {
        mCompany = company;
        setUI();
    }

    private void setUI() {
//    	if (mCompany != null) {
//    		mTitle.setText(mCompany.name);
//    		mPersonCount.setText(String.format(mContext.getString(R.string.company_person_count), mCompany.person_count));
//    		if(!TextUtils.isEmpty(mCompany.small_logo_url)) {
//    			setViewIcon(mCompany.small_logo_url, mIcon);
//    		}
//    	}
    	
    	mCompanytitle.setText(mCompany.name);
        if(!TextUtils.isEmpty(mCompany.large_logo_url)) {
        	setViewIcon(mCompany.large_logo_url, mProfile_img_ui);
        }
        tv_website.setText(mCompany.website);
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

    public Company getCompanyInfo() {
        return mCompany;
    }

    public long getCompanyId() {
        return mCompany.id;
    }
}
